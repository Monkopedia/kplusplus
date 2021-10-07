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

import clang.CXType
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import kotlinx.cinterop.CValue
import kotlinx.serialization.Serializable

private val NATIVE = listOf(
    "size_t",
    "uint16_t",
    "void",
    "bool",
    "char",
    "signed char",
    "unsigned char",
    "short",
    "signed short",
    "unsigned short",
    "int",
    "signed int",
    "unsigned int",
    "long",
    "signed long",
    "unsigned long",
    "long long",
    "signed long long",
    "unsigned long long",
    "float",
    "double",
)

private const val STRING = "std::string"

@ThreadLocal
private val existingTypes = mutableMapOf<String, WrappedTypeReference>()

@Serializable
data class WrappedTypeReference private constructor(val name: String) {
    val isPointerOrReference: Boolean
        get() = isPointer || isReference
    val kotlinType: WrappedKotlinType
        get() = typeToKotlinType(this)
    val isPointer: Boolean
        get() = name.endsWith("*")
    val isReference: Boolean
        get() = name.endsWith("&")
    val referenced: WrappedTypeReference
        get() {
            require(isReference) {
                "Can't get pointed for non-reference type"
            }
            return WrappedTypeReference(name.substring(0, name.length - 1).trim())
        }
    val pointed: WrappedTypeReference
        get() {
            require(isPointer) {
                "Can't get pointed for non-pointer type"
            }
            return WrappedTypeReference(name.substring(0, name.length - 1).trim())
        }
    val isArray: Boolean
        get() = name.endsWith("]")
    val arraySize: Int
        get() {
            require(isArray) {
                "Can't get array size of non-array"
            }
            val openIndex = name.indexOf("[")
            val closeIndex = name.indexOf("]")
            if (openIndex < 0 || closeIndex < 0) return -1
            if (openIndex + 1 == closeIndex) return -1
            return name.substring(openIndex + 1, closeIndex).toInt()
        }
    val arrayType: WrappedTypeReference
        get() {
            require(isArray) {
                "Can't get base type size of non-array"
            }
            val openIndex = name.indexOf("[")
            require(openIndex >= 0) {
                "Can't find type of array $name"
            }
            return WrappedTypeReference(name.substring(0, openIndex).trim())
        }
    val isConst: Boolean
        get() = name.startsWith("const ")
    val unconst: WrappedTypeReference
        get() = if (isConst) WrappedTypeReference(name.substring("const ".length).trim()) else this
    val unreferenced: WrappedTypeReference
        get() = if (isReference) referenced else this
    val isString: Boolean
        get() = unconst.name == STRING || (isReference && referenced.isString)
    val isNative: Boolean
        get() = unconst.name in NATIVE ||
            isString ||
            (isArray && arrayType.isNative) ||
            (isPointer && pointed.isNative)
    val isReturnable: Boolean
        get() = name in NATIVE || isString || name == "long double" || isPointer || isReference
    val isVoid: Boolean
        get() = name == "void"
    val cType: WrappedTypeReference
        get() {
            if (isString || (isPointer && pointed.isString)) {
                return WrappedTypeReference("const char*")
            }
            if (isNative) {
                if (isPointer) {
                    return pointerTo(pointed.cType)
                }
                return this
            }
            if (name == "long double") {
                return WrappedTypeReference("double")
            }
            if (isPointer || isReference) {
                return WrappedTypeReference("void*")
            }
            throw IllegalStateException("Don't know how to convert $name to C")
        }

    override fun toString(): String {
        return name
    }

    fun typedWith(vararg args: WrappedTypeReference): WrappedTypeReference {
        return WrappedTypeReference(
            "$name<${args.joinToString(", ") { it.name }}>".also { println("Typed: $it") }
        )
    }

    companion object :
        (String) -> WrappedTypeReference,
        (CValue<CXType>, ResolverBuilder) -> WrappedTypeReference {
        fun pointerTo(type: WrappedTypeReference): WrappedTypeReference {
            return WrappedTypeReference("$type*")
        }
        fun referenceTo(type: WrappedTypeReference): WrappedTypeReference {
            return WrappedTypeReference("$type&")
        }
        fun arrayOf(type: WrappedTypeReference): WrappedTypeReference {
            return WrappedTypeReference("$type[]")
        }

        val VOID = WrappedTypeReference("void")

        override fun invoke(type: String): WrappedTypeReference {
            if (type == "void") return VOID
            if (type == "std::size_t") return invoke("size_t")
            return existingTypes.getOrPut(type) {
                WrappedTypeReference(type)
            }
        }

        override fun invoke(
            type: CValue<CXType>,
            resolverBuilder: ResolverBuilder
        ): WrappedTypeReference {
            resolverBuilder.visit(type)
            return invoke(
                type.spelling.toKString()?.trim() ?: error("Missing spelling for type $type")
            )
        }
    }
}
