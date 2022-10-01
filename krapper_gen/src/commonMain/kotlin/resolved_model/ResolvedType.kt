/*
 * Copyright 2022 Jason Monk
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

import com.monkopedia.krapper.FilterableTypes.TYPE
import com.monkopedia.krapper.TypeTarget
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class CastMethod {
    STRING_CAST,
    POINTED_STRING_CAST,
    CAST,
    NATIVE,
}

@Serializable
@SerialName("type")
sealed class ResolvedType(
    open val typeString: String
) : ResolvedElement() {

    override fun toString(): String = typeString

    abstract override fun cloneWithoutChildren(): ResolvedType

    companion object : TypeTarget<ResolvedType>(TYPE, ResolvedType::class) {

        const val LONG_DOUBLE_STR = "long double"

        val KSTRING = ResolvedKotlinType("kotlin.String", false)
        val CSTRING = ResolvedCType("char*", false)
        val STRING = ResolvedCppType("std::string", KSTRING, CSTRING, CastMethod.STRING_CAST)
        val PSTRING = ResolvedCppType("std::string*", KSTRING, CSTRING, CastMethod.STRING_CAST)

        val LONG_DOUBLE_C = ResolvedCType(LONG_DOUBLE_STR)
        val LONG_DOUBLE_KOTLIN = ResolvedKotlinType("Double", false)
        val LONG_DOUBLE =
            ResolvedCppType(LONG_DOUBLE_STR, LONG_DOUBLE_KOTLIN, LONG_DOUBLE_C, CastMethod.NATIVE)
        val UNIT = ResolvedKotlinType("kotlin.Unit", false)
        val CVOID = ResolvedCType("void", true)
        val VOID = ResolvedCppType("void", UNIT, CVOID, CastMethod.NATIVE, true)
    }
}

@Serializable
@SerialName("cppType")
class ResolvedCppType(
    @SerialName("cppTypeString")
    val type: String,
    val kotlinType: ResolvedKotlinType,
    val cType: ResolvedCType,
    val castMethod: CastMethod,
    val isVoid: Boolean = false
) : ResolvedType(type) {
    override val typeString: String
        get() = type

    fun copy(
        typeString: String = this.typeString,
        kotlinType: ResolvedKotlinType = this.kotlinType,
        cType: ResolvedCType = this.cType,
        castMethod: CastMethod = this.castMethod,
        isVoid: Boolean = this.isVoid
    ): ResolvedCppType {
        return ResolvedCppType(
            typeString,
            kotlinType.copy(),
            cType.copy(),
            castMethod,
            isVoid
        )
    }

    override fun cloneWithoutChildren(): ResolvedCppType {
        return copy()
    }
}

@Serializable
@SerialName("c_type")
data class ResolvedCType(
    @SerialName("cTypeString")
    val type: String,
    val isVoid: Boolean = false
) : ResolvedType(type) {

    override val typeString: String
        get() = type

    override fun toString(): String = typeString

    override fun cloneWithoutChildren(): ResolvedCType {
        return copy()
    }
}

@Serializable
@SerialName("kotlin_type")
data class ResolvedKotlinType(
    private val qualifyList: List<String>,
    val isWrapper: Boolean,
    val templates: List<ResolvedKotlinType> = emptyList(),
    val isNullable: Boolean = false
) : ResolvedType(qualifyList.last()), FqSymbol {
    private val mappedName: String
        get() {
            return remap?.get(fullyQualified) ?: qualifyList.last()
        }
    val name: String
        get() = if (templates.isNotEmpty()) {
            "$mappedName<${templates.joinToString(", ") { it.name }}>" +
                if (isNullable) "?" else ""
        } else "$mappedName${if (isNullable) "?" else ""}"
    val pkg: String
        get() = qualifyList.subList(0, qualifyList.size - 1).joinToString(".") {
            it.replaceFirstChar { it.lowercase() }
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

    override fun cloneWithoutChildren(): ResolvedKotlinType {
        return copy(templates = templates.map { it.cloneWithoutChildren() })
    }

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
    return ResolvedKotlinType(
        fullyQualified = name.trimEnd('?'),
        isWrapper = isWrapper,
        isNullable = isWrapper || name.endsWith('?')
    )
}
