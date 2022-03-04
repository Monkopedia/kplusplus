/*
 * Copyright 2021 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.krapper.generator.model

import clang.CXCursor
import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.codegen.Operator
import com.monkopedia.krapper.generator.isConst
import com.monkopedia.krapper.generator.isStatic
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.LONG_DOUBLE
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.const
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.NATIVE
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.RAW_CAST
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.REINT_CAST
import com.monkopedia.krapper.generator.resolved_model.ResolvedArgument
import com.monkopedia.krapper.generator.resolved_model.ResolvedConstructor
import com.monkopedia.krapper.generator.resolved_model.ResolvedDestructor
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.ARG_CAST
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.RETURN
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.STRING
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.STRING_POINTER
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.VOID
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.VOIDP
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.VOIDP_REFERENCE
import com.monkopedia.krapper.generator.result
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import com.monkopedia.krapper.generator.usr
import kotlinx.cinterop.CValue

val WrappedElement.parentClass: WrappedClass?
    get() = (parent as? WrappedClass) ?: parent?.parentClass

fun WrappedElement.createThisArg(resolverContext: ResolveContext): ResolvedArgument? {
    val type = resolverContext.resolve(
        pointerTo(
            parentClass?.type ?: return null
        )
    )
        ?: return null
    return ResolvedArgument(
        "thiz",
        type,
        type,
        castMode = REINT_CAST,
        needsDereference = true,
    )
}

typealias MethodType = com.monkopedia.krapper.generator.resolved_model.MethodType

class WrappedConstructor(
    name: String,
    returnType: WrappedType,
    var isCopyConstructor: Boolean,
    val isDefaultConstructor: Boolean
) : WrappedMethod(name, returnType, false, MethodType.CONSTRUCTOR) {
    override fun copy(
        name: String,
        returnType: WrappedType,
        isStatic: Boolean,
        methodType: MethodType,
        children: List<WrappedElement>
    ): WrappedMethod {
        return WrappedConstructor(name, returnType, isCopyConstructor, isDefaultConstructor).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    override fun clone(): WrappedConstructor {
        return WrappedConstructor(name, returnType, isCopyConstructor, isDefaultConstructor).also {
            it.parent = parent
            it.addAllChildren(children)
        }
    }

    fun checkCopyConstructor(type: WrappedType) {
        if (args.size == 1 && args.first().type == type) {
            isCopyConstructor = true
        }
    }

    override fun thizArg(resolverContext: ResolveContext): List<ResolvedArgument>? {
        return emptyList()
    }

    override fun resolve(resolverContext: ResolveContext): ResolvedConstructor? =
        with(resolverContext.currentNamer) {
            return ResolvedConstructor(
                name,
                resolverContext.resolve(
                    pointerTo(
                        parentClass?.type ?: return null
                    )
                ) ?: return null,
                isCopyConstructor,
                isDefaultConstructor,
                uniqueCName,
                args.map {
                    it.resolveArgument(resolverContext)
                        ?: return null
                }
            ).also {
                it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
            }
        }
}

class WrappedDestructor(
    name: String,
    returnType: WrappedType,
) : WrappedMethod(name, returnType, false, MethodType.DESTRUCTOR) {
    override fun copy(
        name: String,
        returnType: WrappedType,
        isStatic: Boolean,
        methodType: MethodType,
        children: List<WrappedElement>
    ): WrappedMethod {
        return WrappedDestructor(name, returnType).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    override fun clone(): WrappedDestructor {
        return WrappedDestructor(name, returnType).also {
            it.parent = parent
            it.addAllChildren(children)
        }
    }

    override fun resolve(resolverContext: ResolveContext): ResolvedDestructor? =
        with(resolverContext.currentNamer) {
            return ResolvedDestructor(
                name,
                resolverContext.resolve(returnType) ?: return null,
                uniqueCName,
                listOf(createThisArg(resolverContext) ?: return null)
            )
        }
}

fun determineReturnStyle(returnType: WrappedType, resolverContext: ResolveContext) =
    when {
        returnType.isVoid -> VOID
        !returnType.isReturnable -> ARG_CAST
        returnType.isString -> STRING
        returnType.isPointer && returnType.pointed.isString -> STRING_POINTER
        returnType.isNative || returnType == LONG_DOUBLE -> RETURN
        returnType.isReference -> VOIDP_REFERENCE
        else -> VOIDP
    }

open class WrappedMethod(
    val name: String,
    val returnType: WrappedType,
    val isStatic: Boolean,
    val methodType: MethodType = MethodType.METHOD
) : WrappedElement() {
    val args: List<WrappedArgument>
        get() = children.filterIsInstance<WrappedArgument>()

    constructor(method: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        method.referenced.spelling.toKString() ?: error("Can't find name of $method"),
        WrappedType(method.type.result, resolverBuilder).let {
            if (method.isConst) const(it) else it
        },
        method.isStatic,
        MethodType.METHOD
    )

    open fun copy(
        name: String = this.name,
        returnType: WrappedType = this.returnType,
        isStatic: Boolean = this.isStatic,
        methodType: MethodType = this.methodType,
        children: List<WrappedElement> = this.children.toList()
    ): WrappedMethod {
        return WrappedMethod(name, returnType, isStatic, methodType).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    override fun clone(): WrappedMethod {
        return WrappedMethod(name, returnType, isStatic, methodType).also {
            it.parent = parent
            it.addAllChildren(children)
        }
    }

    protected open fun thizArg(resolverContext: ResolveContext): List<ResolvedArgument>? {
        return listOf(createThisArg(resolverContext) ?: return null)
    }

    override fun resolve(resolverContext: ResolveContext): ResolvedMethod? =
        with(resolverContext.currentNamer) {
            val (rawMapping, rawResolved) = resolverContext.mapAndResolve(returnType) ?: return null
            val type =
                if (!rawMapping.isPointer && !rawMapping.isReturnable) pointerTo(rawMapping) else rawMapping
            val resolvedReturnType = resolverContext.resolve(type) ?: return null
            resolvedReturnType.kotlinType = rawResolved.kotlinType
            val returnStyle = determineReturnStyle(rawMapping, resolverContext)
            val argCastNeedsPointer = if (returnStyle == ARG_CAST) {
                val type =
                    if (rawMapping.isReference) rawMapping.unreferenced
                    else rawMapping
                !type.isPointer
            } else false
            if (argCastNeedsPointer) {
                resolvedReturnType.cType =
                    resolverContext.resolve(pointerTo(rawMapping))?.cType ?: return null
            }
            return ResolvedMethod(
                name,
                resolvedReturnType,
                isStatic,
                methodType,
                uniqueCName,
                Operator.from(this@WrappedMethod)?.resolvedOperator,
                (thizArg(resolverContext) ?: return null) +
                    args.map { it.resolveArgument(resolverContext) ?: return null },
                returnStyle,
                argCastNeedsPointer
            ).also {
                it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
            }
        }

    override fun toString(): String {
        return "${if (isStatic) "static " else ""}fun $name(${args.joinToString(", ")}): " +
            returnType
    }
}

class WrappedArgument(val name: String, val type: WrappedType, val usr: String = "") :
    WrappedElement() {
    constructor(arg: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        arg.spelling.toKString() ?: error("Can't find name of $arg"),
        WrappedType(arg.type, resolverBuilder),
        arg.usr.toKString() ?: ""
    )

    override fun clone(): WrappedArgument {
        return WrappedArgument(name, type, usr).also {
            it.parent = parent
            it.addAllChildren(children)
        }
    }

    override fun toString(): String {
        return "$name: $type"
    }

    override fun equals(other: Any?): Boolean {
        return (other as? WrappedArgument)?.name == name && other.type == type
    }

    override fun resolve(resolverContext: ResolveContext): ResolvedElement? = null

    fun resolveArgument(resolverContext: ResolveContext): ResolvedArgument? {
        val (type, resolved) =
            resolverContext.mapAndResolve(if (type.isReference) type.unreferenced else type)
                ?: return null
        val needsDereference = !type.isPointer && !type.isNative
        val resolvedArgType =
            if (needsDereference) resolverContext.resolve(pointerTo(type)) ?: return null
            else resolved
        return ResolvedArgument(
            name,
            resolved,
            resolvedArgType,
            usr,
            determineArgumentCastMode(type, resolverContext),
            needsDereference
        )
    }
}

fun determineArgumentCastMode(type: WrappedType, resolverContext: ResolveContext) =
    when {
        type.isString -> ArgumentCastMode.STRING
        type.isNative -> NATIVE
        type == LONG_DOUBLE -> RAW_CAST
        else -> REINT_CAST
    }
