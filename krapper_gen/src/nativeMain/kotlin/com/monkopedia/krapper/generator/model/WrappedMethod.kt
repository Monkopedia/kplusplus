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
import clang.CXCursorKind
import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.children
import com.monkopedia.krapper.generator.codegen.Operator
import com.monkopedia.krapper.generator.codegen.STACK_CONSTRUCTOR_CALLBACK
import com.monkopedia.krapper.generator.isConst
import com.monkopedia.krapper.generator.isStatic
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.lexicalParent
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.LONG_DOUBLE
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.VOID
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.const
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.resolved_model.AllocationStyle
import com.monkopedia.krapper.generator.resolved_model.AllocationStyle.DIRECT
import com.monkopedia.krapper.generator.resolved_model.AllocationStyle.STACK
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.NATIVE
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.RAW_CAST
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.REINT_CAST
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.STD_MOVE
import com.monkopedia.krapper.generator.resolved_model.MethodType.SIZE_OF
import com.monkopedia.krapper.generator.resolved_model.MethodType.STATIC
import com.monkopedia.krapper.generator.resolved_model.ResolvedArgument
import com.monkopedia.krapper.generator.resolved_model.ResolvedConstructor
import com.monkopedia.krapper.generator.resolved_model.ResolvedDestructor
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.ARG_CAST
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.COPY_CONSTRUCTOR
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.RETURN
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.RETURN_REFERENCE
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.STRING
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.STRING_POINTER
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.VOIDP
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.VOIDP_REFERENCE
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.result
import com.monkopedia.krapper.generator.semanticParent
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import com.monkopedia.krapper.generator.usr
import kotlinx.cinterop.CValue

val WrappedElement.parentClass: WrappedClass?
    get() = (parent as? WrappedClass) ?: parent?.parentClass
val WrappedElement.baseParent: WrappedElement
    get() = parent?.baseParent ?: parent ?: this

fun WrappedElement.createThisArg(resolverContext: ResolveContext): ResolvedArgument? {
    val pointerParent = pointerTo(
        parentClass?.type ?: return resolverContext.notifyFailed(
            this,
            parentClass?.type,
            "Can't find parent type"
        )
    )
    val type = resolverContext.resolve(pointerParent)
        ?: return resolverContext.notifyFailed(this, pointerParent, "Parent type not resolving")
    return ResolvedArgument(
        "thiz",
        type,
        type,
        castMode = REINT_CAST,
        needsDereference = true,
        hasDefault = false,
    )
}

typealias MethodType = com.monkopedia.krapper.generator.resolved_model.MethodType

class WrappedConstructor(
    name: String,
    returnType: WrappedType,
    var isCopyConstructor: Boolean,
    val isDefaultConstructor: Boolean,
    var allocationStyle: AllocationStyle = AllocationStyle.DIRECT
) : WrappedMethod(name, returnType, MethodType.CONSTRUCTOR) {
    override fun copy(
        name: String,
        returnType: WrappedType,
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

    override fun thizArg(resolverContext: ResolveContext): List<ResolvedArgument> {
        val type = resolverContext.resolve(pointerTo(VOID))!!
        return listOf(
            ResolvedArgument(
                "location",
                type,
                type,
                "",
                NATIVE,
                needsDereference = false,
                hasDefault = false
            )
        )
    }

    private fun postArgs(resolverContext: ResolveContext): List<ResolvedArgument>? {
        if (allocationStyle != STACK) return emptyList()
        val clsType = parentClass?.type?.let(resolverContext::resolve)
            ?: return resolverContext.notifyFailed(
                this,
                parentClass?.type,
                "constructor missing class"
            )
        val type = resolverContext.resolve(pointerTo(VOID))!!.copy(
            typeString = STACK_CONSTRUCTOR_CALLBACK,
            kotlinType = ResolvedKotlinType(
                listOf("(${clsType.kotlinType.name}) -> Unit"),
                false,
                emptyList(),
                false
            )
        )
        return listOf(
            ResolvedArgument(
                "callback",
                type,
                type,
                "",
                REINT_CAST,
                needsDereference = false,
                hasDefault = false
            )
        )
    }

    override fun resolve(
        resolverContext: ResolveContext
    ): ResolvedConstructor? =
        with(resolverContext.currentNamer) {
            val pointedType = (
                if (allocationStyle == DIRECT) parentClass?.type?.let(::pointerTo)
                else VOID
                ) ?: return resolverContext.notifyFailed(
                this@WrappedConstructor,
                null,
                "Constructor missing parent ${parentClass?.type}"
            )
            return ResolvedConstructor(
                name,
                resolverContext.resolve(pointedType) ?: return resolverContext.notifyFailed(
                    this@WrappedConstructor,
                    pointedType,
                    "Parent class resolve"
                ),
                isCopyConstructor,
                isDefaultConstructor,
                uniqueCName,
                thizArg(resolverContext) +
                    (resolveArguments(resolverContext) ?: return null) +
                    (postArgs(resolverContext) ?: return null),
                allocationStyle
            ).also {
                it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
            }
        }
}

class WrappedDestructor(
    name: String,
    returnType: WrappedType,
) : WrappedMethod(name, returnType, MethodType.DESTRUCTOR) {
    override fun copy(
        name: String,
        returnType: WrappedType,
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
                resolverContext.resolve(returnType) ?: return resolverContext.notifyFailed(
                    this@WrappedDestructor,
                    returnType,
                    "Destructor return type"
                ),
                uniqueCName,
                listOf(createThisArg(resolverContext) ?: return null)
            )
        }
}

fun determineReturnStyle(returnType: WrappedType, resolverContext: ResolveContext): ReturnStyle =
    when {
        returnType.isVoid -> ReturnStyle.VOID
        !returnType.isReturnable ->
            if (resolverContext.canAssign(returnType)) ARG_CAST else COPY_CONSTRUCTOR
        returnType.isString -> STRING
        returnType.isPointer && returnType.pointed.isString -> STRING_POINTER
        returnType.isNative || returnType == LONG_DOUBLE ->
            if (returnType.isReference) RETURN_REFERENCE else RETURN
        returnType.isReference -> VOIDP_REFERENCE
        else -> VOIDP
    }

open class WrappedMethod(
    val name: String,
    val returnType: WrappedType,
    val methodType: MethodType = MethodType.METHOD
) : WrappedElement() {
    val args: List<WrappedArgument>
        get() = children.filterIsInstance<WrappedArgument>()

    constructor(method: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        method.referenced.spelling.toKString() ?: error("Can't find name of $method"),
        WrappedType(method.type.result, resolverBuilder).let {
            if (method.isConst) const(it) else it
        },
        if (method.isStatic ||
            (method.semanticParent.kind !in clsParents && method.lexicalParent.kind !in clsParents)
        ) MethodType.STATIC
        else MethodType.METHOD
    )

    open fun copy(
        name: String = this.name,
        returnType: WrappedType = this.returnType,
        methodType: MethodType = this.methodType,
        children: List<WrappedElement> = this.children.toList()
    ): WrappedMethod {
        return WrappedMethod(name, returnType, methodType).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    override fun clone(): WrappedMethod {
        return WrappedMethod(name, returnType, methodType).also {
            it.parent = parent
            it.addAllChildren(children)
        }
    }

    protected open fun thizArg(resolverContext: ResolveContext): List<ResolvedArgument>? {
        if (methodType == SIZE_OF || methodType == STATIC) return emptyList()
        return listOf(createThisArg(resolverContext) ?: return null)
    }

    override fun resolve(resolverContext: ResolveContext): ResolvedMethod? =
        with(resolverContext.currentNamer) {
            val (rawMapping, rawResolved) = resolverContext.mapAndResolve(returnType)
                ?: return resolverContext.notifyFailed(
                    this@WrappedMethod,
                    returnType,
                    "Couldn't resolve return"
                )
            val returnStyle = determineReturnStyle(rawMapping, resolverContext)
            val type =
                if (!rawMapping.isPointer && !rawMapping.isReturnable) pointerTo(rawMapping)
                else rawMapping
            val resolvedReturnType = resolverContext.resolve(type)
                ?: return resolverContext.notifyFailed(
                    this@WrappedMethod,
                    type,
                    "Couldn't resolve pointed return"
                )
            resolvedReturnType.kotlinType = rawResolved.kotlinType
            val argCastNeedsPointer = if (returnStyle == ARG_CAST) {
                val type =
                    if (rawMapping.isReference) rawMapping.unreferenced
                    else rawMapping
                !type.isPointer
            } else false
            if (argCastNeedsPointer) {
                resolvedReturnType.cType =
                    resolverContext.resolve(pointerTo(rawMapping))?.cType
                        ?: return resolverContext.notifyFailed(
                            this@WrappedMethod,
                            pointerTo(rawMapping),
                            "Couldn't resolve argCast"
                        )
            }
            return ResolvedMethod(
                name,
                resolvedReturnType,
                methodType,
                uniqueCName,
                Operator.from(this@WrappedMethod)?.resolvedOperator,
                (thizArg(resolverContext) ?: return null) +
                    (resolveArguments(resolverContext) ?: return null),
                returnStyle,
                argCastNeedsPointer,
                qualified
            ).also {
                it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
            }
        }

    protected fun resolveArguments(resolverContext: ResolveContext): List<ResolvedArgument>? {
        val retArgs = mutableListOf<ResolvedArgument>()

        args.forEachIndexed { index, wrappedArgument ->
            val resolved = wrappedArgument.resolveArgument(resolverContext)
            if (resolved != null) {
                retArgs.add(resolved)
            } else {
                if (args.subList(index, args.size).all { it.hasDefault }) {
                    return retArgs
                }
                return resolverContext.notifyFailed(
                    this,
                    null,
                    "Method failed from argument $wrappedArgument"
                )
            }
        }
        return retArgs
    }

    override fun toString(): String {
        return "fun $name(${args.joinToString(", ")}): $returnType"
    }

    companion object {
        private val clsParents = listOf(
            CXCursorKind.CXCursor_ClassTemplate,
            CXCursorKind.CXCursor_ClassDecl,
            CXCursorKind.CXCursor_StructDecl
        )
    }
}

class WrappedArgument(
    val name: String,
    val type: WrappedType,
    val usr: String = "",
    val hasDefault: Boolean = false
) : WrappedElement() {
    constructor(arg: CValue<CXCursor>, resolverBuilder: ResolverBuilder, index: Int = 0) : this(
        arg.spelling.toKString()?.takeIf { it.isNotBlank() } ?: "_arg_$index",
        WrappedType(arg.type, resolverBuilder),
        arg.usr.toKString() ?: "",
        hasDefault(arg)
    )

    override fun clone(): WrappedArgument {
        return WrappedArgument(name, type, usr, hasDefault).also {
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
        val unreferencedType = if (type.isReference) type.unreferenced else type
        val (type, resolved) =
            resolverContext.mapAndResolve(unreferencedType)
                ?: return resolverContext.notifyFailed(this, unreferencedType, "Missing type $type")
        val needsDereference = !type.isPointer && !type.isNative && type != LONG_DOUBLE
        val resolvedArgType =
            if (needsDereference) {
                val pointerType = pointerTo(type)
                resolverContext.resolve(pointerType) ?: return resolverContext.notifyFailed(
                    this,
                    pointerType,
                    "Argument type"
                )
            } else resolved
        return ResolvedArgument(
            name,
            resolved,
            resolvedArgType,
            usr,
            determineArgumentCastMode(type, this.type.isReference, resolverContext),
            needsDereference,
            hasDefault
        )
    }

    companion object {
        fun hasDefault(value: CValue<CXCursor>): Boolean {
            val lastKind = (value.children.lastOrNull()?.kind ?: return false)
            return lastKind !in listOf(
                CXCursorKind.CXCursor_TypeRef,
                CXCursorKind.CXCursor_TemplateRef,
                CXCursorKind.CXCursor_NamespaceRef
            )
        }
    }
}

fun determineArgumentCastMode(
    type: WrappedType,
    // Resolve ditches the reference, so pass it in manually.
    isReference: Boolean,
    resolverContext: ResolveContext
) = when {
    type.isString -> ArgumentCastMode.STRING
    type.isNative -> NATIVE
    type == LONG_DOUBLE -> RAW_CAST
    // TODO: Real type check rather than prefix.
    !isReference && !type.isPointer && type.toString().startsWith("std::unique_ptr") -> STD_MOVE
    else -> REINT_CAST
}
