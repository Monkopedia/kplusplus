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
import clang.CXFile
import clang.CX_CXXAccessSpecifier
import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.accessSpecifier
import com.monkopedia.krapper.generator.codegen.BasicAssignmentOperator
import com.monkopedia.krapper.generator.codegen.Operator
import com.monkopedia.krapper.generator.includedFile
import com.monkopedia.krapper.generator.isAbstract
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import kotlinx.cinterop.CValue

private val CValue<CXCursor>.fileParent: CXFile?
    get() {
        return includedFile
    }

class WrappedBase(val type: WrappedType?) : WrappedElement() {
    override fun clone(): WrappedElement {
        return WrappedBase(type).also {
            it.addAllChildren(children.map { it.clone() })
        }
    }

    override fun resolve(resolverContext: ResolveContext): ResolvedElement? = null
}

class WrappedClass(
    val name: String,
    var isAbstract: Boolean = false,
    val specifiedType: WrappedType? = null
) : WrappedElement() {
    var hasConstructor: Boolean = false
        get() = field || children.any { (it as? WrappedConstructor) != null }
    var hasHiddenNew: Boolean = false
    var hasHiddenDelete: Boolean = false
    val baseClass: WrappedType?
        get() = children.filterIsInstance<WrappedBase>().firstOrNull()?.type
    val hasDefaultConstructor: Boolean
        get() = children.filterIsInstance<WrappedConstructor>().any { it.isDefaultConstructor }
    val hasCopyConstructor: Boolean
        get() = children.filterIsInstance<WrappedConstructor>().any { it.isCopyConstructor }

    val type: WrappedType
        get() = specifiedType ?: WrappedType(qualified)

    private val qualified: String
        get() = withParents.mapNotNull { it.named }.joinToString("::")
    private val WrappedElement.withParents: List<WrappedElement>
        get() = this@withParents.parent?.withParents?.plus(listOf(this@withParents))
            ?: listOf(this@withParents)
    private val WrappedElement.named: String?
        get() = when (this) {
            is WrappedClass -> this@named.name
            is WrappedNamespace -> this@named.namespace
            else -> null
        }

    constructor(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        wrapName(value, value.spelling.toKString() ?: error("Missing name")), value.isAbstract
    )

    override fun clone(): WrappedClass {
        return clone(this.specifiedType)
    }

    fun clone(specifiedType: WrappedType? = this.specifiedType): WrappedClass {
        return WrappedClass(name, isAbstract, specifiedType).also {
            it.parent = parent
            it.addAllChildren(children)
            if (hasConstructor) {
                it.hasConstructor = true
            }
            if (hasHiddenDelete) {
                it.hasHiddenDelete = true
            }
            if (hasHiddenNew) {
                it.hasHiddenNew = true
            }
        }
    }

    override fun resolve(resolverContext: ResolveContext): ResolvedClass? {
        val baseClasses = resolverContext.findBases(this)
        modifyMethodsIfNeeded(baseClasses)
        return ResolvedClass(
            name,
            isAbstract,
            specifiedType?.let {
                // If no type, thats fine, but if type exists, it needs to resolve.
                resolverContext.resolve(specifiedType) ?: return null
            },
            hasConstructor,
            hasHiddenNew,
            hasHiddenDelete,
            baseClass?.let {
                resolverContext.resolve(it) ?: return null
            },
            hasDefaultConstructor,
            hasCopyConstructor,
            resolverContext.resolve(type) ?: return null
        ).also {
            it.addAllChildren(children.mapNotNull { it.resolve(resolverContext + this) })
        }
    }

    fun modifyMethodsIfNeeded(baseClasses: List<WrappedClass>) {
        if (!isAbstract && !hasConstructor && !baseClasses.any { it.hasConstructor }) {
            addChild(
                WrappedConstructor(
                    "new",
                    type,
                    isCopyConstructor = false,
                    isDefaultConstructor = true
                )
            )
        }
        children.filterIsInstance<WrappedConstructor>().forEach {
            it.checkCopyConstructor(type)
        }
        // Manually pretend all assignment operators are void.
        val assignments = children.filterIsInstance<WrappedMethod>()
            .filter { Operator.from(it) is BasicAssignmentOperator }
        for (assignment in assignments) {
            removeChild(assignment)
            addChild(assignment.copy(returnType = WrappedType.VOID))
        }
        if (hasHiddenNew) {
            children.filterIsInstance<WrappedConstructor>().forEach {
                removeChild(it)
            }
        }
        if (hasHiddenDelete) {
            children.filterIsInstance<WrappedDestructor>().forEach {
                removeChild(it)
            }
        }
    }

    override fun toString(): String {
        return "cls($name)"
    }

//    constructor(name: String, children: List<WrappedElement>, resolverBuilder: ResolverBuilder)
//
//    init {
//        println("Created $fullyQualified")
//        Throwable().printStackTrace()
//    }

//    override fun toString(): String {
//        return buildString {
//            append("class $fullyQualified {\n")
//            baseClass?.let {
//                append("    super $it")
//            }
//            append("\n")
//            for (field in fields) {
//                append("    $field\n")
//            }
//            append("\n")
//            for (method in methods) {
//                append("    $method\n")
//            }
//            append("\n")
//
//            append("}\n")
//        }
//    }
}

private fun List<CValue<CXCursor>>.findMethods(
    resolverBuilder: ResolverBuilder
): List<WrappedMethod> {
    return filter {
        (
            it.kind == CXCursorKind.CXCursor_CXXMethod ||
                it.kind == CXCursorKind.CXCursor_Constructor ||
                it.kind == CXCursorKind.CXCursor_Destructor
            ) &&
            it.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPublic
    }.map {
        WrappedMethod(it, resolverBuilder)
    }
}

private fun List<CValue<CXCursor>>.findFields(
    resolverBuilder: ResolverBuilder
): List<WrappedField> {
    return filter {
        it.kind == CXCursorKind.CXCursor_FieldDecl &&
            it.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPublic
    }.map {
        WrappedField(it, resolverBuilder)
    }
}

private fun List<CValue<CXCursor>>.findBaseClass(
    resolverBuilder: ResolverBuilder
): WrappedType? {
    return find {
        it.kind == CXCursorKind.CXCursor_CXXBaseSpecifier
    }?.let {
        WrappedType(it.type, resolverBuilder)
    }
}

private fun wrapName(value: CValue<CXCursor>, name: String): String {
    val type = value.type.spelling.toKString() ?: return name
    val index = type.indexOf(name)
    if (index < 0) return name
    return type.substring(index)
}
