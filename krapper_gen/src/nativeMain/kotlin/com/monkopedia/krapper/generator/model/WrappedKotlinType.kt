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

import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_OPAQUE_POINTER
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_VALUES_REF
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType

interface WrappedKotlinType {
    val isWrapper: Boolean
    val fullyQualified: List<String>
    val name: String
    val pkg: String
}

private val typeMap = mapOf(
    "size_t" to "platform.posix.size_t",
    "uint16_t" to "UShort",
    "void" to "Unit",
    "bool" to "Boolean",
    "char" to "Byte",
    "signed char" to "Byte",
    "unsigned char" to "UByte",
    "short" to "Short",
    "signed short" to "Short",
    "unsigned short" to "UShort",
    "int" to "Int",
    "signed int" to "Int",
    "unsigned int" to "UInt",
    "long" to "Long",
    "signed long" to "Long",
    "unsigned long" to "ULong",
    "long long" to "Long",
    "signed long long" to "Long",
    "unsigned long long" to "ULong",
    "float" to "Float",
    "double" to "Double",
    "long double" to "Double",
)

private val pointerTypeMap = mapOf(
    "bool" to "kotlinx.cinterop.BooleanVar",
    "char" to "kotlinx.cinterop.ByteVar",
    "signed char" to "kotlinx.cinterop.ByteVar",
    "unsigned char" to "kotlinx.cinterop.UByteVar",
    "short" to "kotlinx.cinterop.ShortVar",
    "signed short" to "kotlinx.cinterop.ShortVar",
    "unsigned short" to "kotlinx.cinterop.UShortVar",
    "int" to "kotlinx.cinterop.IntVar",
    "signed int" to "kotlinx.cinterop.IntVar",
    "unsigned int" to "kotlinx.cinterop.UIntVar",
    "long" to "kotlinx.cinterop.LongVar",
    "signed long" to "kotlinx.cinterop.LongVar",
    "unsigned long" to "kotlinx.cinterop.ULongVar",
    "long long" to "kotlinx.cinterop.LongVar",
    "signed long long" to "kotlinx.cinterop.LongVar",
    "unsigned long long" to "kotlinx.cinterop.ULongVar",
    "float" to "kotlinx.cinterop.FloatVar",
    "double" to "kotlinx.cinterop.DoubleVar",
)

fun typeToKotlinType(type: WrappedType): WrappedKotlinType = WrappedKotlinType(type)

fun WrappedKotlinType(type: WrappedType): WrappedKotlinType {
    if (type is WrappedTemplateType) {
        return WrappedKotlinType(WrappedKotlinType(type.baseType).pkg + "." + (listOf(type.baseType) + type.templateArgs).joinToString("__") {
            WrappedKotlinType(it).name.trimEnd('?')
        })
    }
    if (type is WrappedTemplateRef) throw IllegalArgumentException("Can't convert $type to kotlin")
    if (type.isString) return fullyQualifiedType("String?")
    if (type.toString() == "const char*") return fullyQualifiedType("String?")
    if (type.isPointer) {
        if (type == WrappedType.VOID) {
            return fullyQualifiedType(C_OPAQUE_POINTER)
        }
        if (type.pointed.isNative) {
            val pointerType = pointerTypeMap[type.pointed.toString()]
                ?: return WrappedKotlinType(type.pointed)
            return nullable(
                fullyQualifiedType(C_VALUES_REF).typedWith(
                    listOf(fullyQualifiedType(pointerType))
                )
            )
        }
        return nullable(WrappedKotlinType(type.pointed))
    }
    if (type.isReference) {
        return WrappedKotlinType(type.unreferenced)
    }
    if (type.isConst) {
        return WrappedKotlinType(type.unconst)
    }
    if (type.isNative || type == WrappedType.LONG_DOUBLE) {
        return fullyQualifiedType(typeMap[type.toString()] ?: type.toString())
    }
    val name = type.toString()
    if (name.contains("<")) {
        var templateTypes = mutableListOf<WrappedKotlinType>()
        return WrappedKotlinType(
            findQualifiers(name).joinToString("::") {
                val section = name.substring(it).trimStart(':')
                val start = section.indexOf('<')
                if (start < 0) section
                else {
                    val base = section.substring(0, start)
                    templateTypes += parseTypes(
                        section.substring(
                            start + 1,
                            section.length - 1
                        )
                    )
                    base
                }
            } + "__" + templateTypes.joinToString("__") { it.name }
        )
    }
    return WrappedKotlinType(name)
}

fun nullable(base: WrappedKotlinType): WrappedKotlinType {
    return object : WrappedKotlinType {
        override val isWrapper: Boolean
            get() = base.isWrapper
        override val fullyQualified: List<String>
            get() = base.fullyQualified
        override val name: String
            get() = base.name + "?"
        override val pkg: String
            get() = base.pkg

        override fun toString(): String {
            return "$base?"
        }
    }
}

fun WrappedKotlinType(nameIn: String): WrappedKotlinType {
    val name = nameIn.trim()
    if (name.contains("<")) {
        val start = name.indexOf('<')
        val base = name.substring(0, start)
        return WrappedKotlinType(base).typedWith(
            parseTypes(
                name.substring(
                    start + 1,
                    name.length - 1
                )
            )
        )
    }
    return fullyQualifiedType(name.replace("::", "."), isWrapper = true)
}

fun WrappedKotlinType.typedWith(parseTypes: List<WrappedKotlinType>): WrappedKotlinType {
    val baseType = this
    return object : WrappedKotlinType {
        override val isWrapper: Boolean
            get() = baseType.isWrapper
        override val fullyQualified: List<String>
            get() = parseTypes.flatMap { it.fullyQualified } + baseType.fullyQualified
        override val name: String
            get() = "${baseType.name}<${parseTypes.joinToString(", ") { it.name }}>"
        override val pkg: String
            get() = baseType.pkg

        override fun toString(): String {
            return "$baseType<${parseTypes.joinToString(", ")}>"
        }
    }
}

fun parseTypes(argList: String): List<WrappedKotlinType> {
    return findTemplates(argList).map {
        argList.substring(it).trimStart(',')
    }.map(::WrappedKotlinType)
}

fun findQualifiers(argList: String): List<IntRange> {
    return sequence {
        var last = -1
        var current = 1
        var lastColon = false
        while (current < argList.length) {
            val c = argList[current]
            if (c == '<') {
                current = argList.findEnd(start = current)
            } else if (c == ':' && lastColon) {
                yield(IntRange(last + 1, current - 2))
                last = current
            }
            lastColon = c == ':'
            current++
        }
        yield(IntRange(last + 1, current - 1))
    }.toList()
}

fun findTemplates(argList: String): List<IntRange> {
    return sequence {
        var last = 0
        var current = 1
        while (current < argList.length) {
            if (argList[current] == '<') {
                current = argList.findEnd(start = current)
            } else if (argList[current] == ',') {
                yield(IntRange(last, current - 1))
                last = current
            }
            current++
        }
        yield(IntRange(last, current - 1))
    }.toList()
}

private fun String.findEnd(start: Int): Int {
    var startSearch = start
    var openIndex = indexOf('<', startSearch + 1)
    while (openIndex >= 0) {
        startSearch = findEnd(openIndex)
        openIndex = indexOf('<', startSearch + 1)
    }
    val end = indexOf('>', startSearch + 1)
    if (end < 0) {
        throw IllegalStateException("Cannot find end of $this[$start]")
    }
    return end
}

fun fullyQualifiedType(name: String, isWrapper: Boolean = false): WrappedKotlinType {
    return object : WrappedKotlinType {
        override val isWrapper: Boolean
            get() = isWrapper
        override val fullyQualified: List<String>
            get() = listOf(name)
        override val name: String
            get() = name.split(".").last()
        override val pkg: String
            get() = name.split(".").toMutableList().also { it.removeLast() }.joinToString(".")

        override fun toString(): String {
            return name
        }
    }
}
