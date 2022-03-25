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
package com.monkopedia.krapper.generator.resolved_model.type

import com.monkopedia.krapper.generator.resolved_model.ResolvedElement

enum class CastMethod {
    STRING_CAST,
    POINTED_STRING_CAST,
    CAST,
    NATIVE,
}

sealed class ResolvedType(
    var typeString: String
) : ResolvedElement() {

    override fun toString(): String {
        return typeString
    }

    companion object {

        const val LONG_DOUBLE_STR = "long double"

        val KSTRING = ResolvedKotlinType("kotlin.String", false)
        val CSTRING = ResolvedCType("char*", false)
        val STRING = ResolvedCppType("std::string", KSTRING, CSTRING, CastMethod.STRING_CAST)
        val PSTRING = ResolvedCppType("std::string*", KSTRING, CSTRING, CastMethod.STRING_CAST)

        val LONG_DOUBLE_C = ResolvedCType(LONG_DOUBLE_STR)
        val LONG_DOUBLE_KOTLIN = ResolvedKotlinType("Double", false)
        val LONG_DOUBLE =
            ResolvedCppType(LONG_DOUBLE_STR, LONG_DOUBLE_KOTLIN, LONG_DOUBLE_C, CastMethod.NATIVE)
        val UNIT = ResolvedKotlinType("Unit", false)
        val CVOID = ResolvedCType("void", true)
        val VOID = ResolvedCppType("void", UNIT, CVOID, CastMethod.NATIVE, true)
    }
}

class ResolvedCppType(
    typeString: String,
    var kotlinType: ResolvedKotlinType,
    var cType: ResolvedCType,
    var castMethod: CastMethod,
    var isVoid: Boolean = false,
) : ResolvedType(typeString)

class ResolvedCType(
    typeString: String,
    var isVoid: Boolean = false
) : ResolvedType(typeString)

data class ResolvedKotlinType(
    private var qualifyList: List<String>,
    var isWrapper: Boolean,
    var templates: List<ResolvedKotlinType> = emptyList(),
    var isNullable: Boolean = false
) : ResolvedType(qualifyList.last()), FqSymbol {
    private val mappedName: String
        get() {
            return remap?.get(fullyQualified) ?: qualifyList.last()
        }
    var name: String
        get() = if (templates.isNotEmpty()) {
            "$mappedName<${templates.joinToString(", ") { it.name }}>" +
                "${if (isNullable) "?" else ""}"
        } else "$mappedName${if (isNullable) "?" else ""}"
        set(value) {
            qualifyList = qualifyList.subList(0, qualifyList.size - 1) + value
        }
    var pkg: String
        get() = qualifyList.subList(0, qualifyList.size - 1).joinToString(".") {
            it.replaceFirstChar { it.lowercase() }
        }
        set(value) {
            qualifyList = value.split(".") + qualifyList.last()
        }
    val fullyQualified: String
        get() = qualifyList.mapIndexed { i, s ->
            if (i != qualifyList.size - 1) {
                s.replaceFirstChar { it.lowercase() }
            } else s
        }.joinToString(".")
    override val fqNames: List<String>
        get() = listOf(fullyQualified) + templates.flatMap { it.fqNames }

    constructor(fullyQualified: String, isWrapper: Boolean, isNullable: Boolean = false) : this(
        fullyQualified.split('.'),
        isWrapper,
        isNullable = isNullable
    )

    private var remap: Map<String, String>? = null

    override fun setNameRemap(map: Map<String, String>) {
        remap = map
        templates.forEach { it.setNameRemap(map) }
    }

    override fun toString(): String {
        return name
    }
}

fun nullable(base: ResolvedKotlinType): ResolvedKotlinType {
    return base.copy(isNullable = true)
}

fun ResolvedKotlinType.typedWith(parseTypes: List<ResolvedKotlinType>): ResolvedKotlinType {
    return copy(templates = parseTypes)
}

fun fullyQualifiedType(name: String, isWrapper: Boolean = false): ResolvedKotlinType {
    return ResolvedKotlinType(fullyQualified = name.trimEnd('?'), isWrapper = isWrapper, isNullable = isWrapper || name.endsWith('?'))
}
