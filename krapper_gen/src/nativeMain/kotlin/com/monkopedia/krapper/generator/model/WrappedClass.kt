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
import com.monkopedia.krapper.generator.filterChildrenRecursive
import com.monkopedia.krapper.generator.fullyQualified
import com.monkopedia.krapper.generator.includedFile
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.type
import kotlinx.cinterop.CValue
import kotlinx.serialization.Serializable

private val CValue<CXCursor>.fileParent: CXFile?
    get() {
        return includedFile
    }

@Serializable
data class WrappedClass(
    val fullyQualified: String,
    var baseClass: WrappedTypeReference? = null,
    val fields: List<WrappedField> = emptyList(),
    val methods: List<WrappedMethod> = emptyList()
) {

    val type: WrappedTypeReference
        get() = WrappedTypeReference(fullyQualified)

    constructor(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        value.fullyQualified,
        value.filterChildrenRecursive {
            it.kind == CXCursorKind.CXCursor_CXXBaseSpecifier ||
                it.kind == CXCursorKind.CXCursor_CXXMethod ||
                it.kind == CXCursorKind.CXCursor_Constructor ||
                it.kind == CXCursorKind.CXCursor_Destructor ||
                it.kind == CXCursorKind.CXCursor_FieldDecl
        },
        resolverBuilder
    )

    constructor(name: String, children: List<CValue<CXCursor>>, resolverBuilder: ResolverBuilder) :
        this(
            name,
            children.findBaseClass(resolverBuilder),
            children.findFields(resolverBuilder),
            children.findMethods(resolverBuilder)
        )
//
//    init {
//        println("Created $fullyQualified")
//        Throwable().printStackTrace()
//    }

    override fun toString(): String {
        return buildString {
            append("class $fullyQualified {\n")
            baseClass?.let {
                append("    super $it")
            }
            append("\n")
            for (field in fields) {
                append("    $field\n")
            }
            append("\n")
            for (method in methods) {
                append("    $method\n")
            }
            append("\n")

            append("}\n")
        }
    }
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
): WrappedTypeReference? {
    return find {
        it.kind == CXCursorKind.CXCursor_CXXBaseSpecifier
    }?.let {
        WrappedTypeReference(it.type, resolverBuilder)
    }
}
