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
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.accessSpecifier
import com.monkopedia.krapper.generator.includedFile
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import kotlinx.cinterop.CValue

private val CValue<CXCursor>.fileParent: CXFile?
    get() {
        return includedFile
    }

class WrappedBase(val type: WrappedType?) : WrappedElement()

class WrappedClass(
    val name: String,
    val specifiedType: WrappedType? = null
) : WrappedElement() {
    val baseClass: WrappedType?
        get() = children.filterIsInstance<WrappedBase>().firstOrNull()?.type

    val type: WrappedType
        get() = specifiedType ?: WrappedType(qualified)

    private val qualified: String
        get() = withParents.mapNotNull { it.named }.joinToString("::")
    private val WrappedElement.withParents: List<WrappedElement>
        get() = this@withParents.parent?.withParents?.plus(listOf(this@withParents)) ?: listOf(this@withParents)
    private val WrappedElement.named: String?
        get() = when (this) {
            is WrappedClass -> this@named.name
            is WrappedNamespace -> this@named.namespace
            else -> null
        }

    constructor(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        value.spelling.toKString() ?: error("Missing name")
    )

    override fun clone(): WrappedClass {
        return WrappedClass(name, specifiedType).also {
            it.parent = parent
            it.children.addAll(children)
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
