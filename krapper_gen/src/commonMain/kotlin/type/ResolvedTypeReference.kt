///*
// * Copyright 2021 Jason Monk
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.monkopedia.krapper.generator.resolved_model.type
//
//import kotlinx.serialization.Serializable
//
//private val NATIVE = listOf(
//    "size_t",
//    "uint16_t",
//    "void",
//    "bool",
//    "char",
//    "signed char",
//    "unsigned char",
//    "short",
//    "signed short",
//    "unsigned short",
//    "int",
//    "signed int",
//    "unsigned int",
//    "long",
//    "signed long",
//    "unsigned long",
//    "long long",
//    "signed long long",
//    "unsigned long long",
//    "float",
//    "double",
//)
//
//private const val STRING = "std::string"
//
//@Serializable
//data class ResolvedTypeReference(val name: String) : ResolvedType() {
//    override val isArray: Boolean
//        get() = name.endsWith("]")
//    val arraySize: Int
//        get() {
//            require(isArray) {
//                "Can't get array size of non-array"
//            }
//            val openIndex = name.indexOf("[")
//            val closeIndex = name.indexOf("]")
//            if (openIndex < 0 || closeIndex < 0) return -1
//            if (openIndex + 1 == closeIndex) return -1
//            return name.substring(openIndex + 1, closeIndex).toInt()
//        }
//    val arrayType: ResolvedTypeReference
//        get() {
//            require(isArray) {
//                "Can't get base type size of non-array"
//            }
//            val openIndex = name.indexOf("[")
//            require(openIndex >= 0) {
//                "Can't find type of array $name"
//            }
//            return ResolvedTypeReference(name.substring(0, openIndex).trim())
//        }
//    override val isPointer: Boolean
//        get() = false
//    override val isReference: Boolean
//        get() = false
//    override val pointed: ResolvedType
//        get() = error("Cannot get pointed of non-pointer type $this")
//    override val unreferenced: ResolvedType
//        get() = error("Cannot unreference of non-reference type $this")
//
//    override val isConst: Boolean
//        get() = false
//    override val unconst: ResolvedTypeReference
//        get() = this
//    override val isString: Boolean
//        get() = name == STRING
//    override val isNative: Boolean
//        get() = name in NATIVE || isString || (isArray && arrayType.isNative)
//    override val isReturnable: Boolean
//        get() = name in NATIVE || isString || name == LONG_DOUBLE_STR
//    override val isVoid: Boolean
//        get() = name == "void"
//    override val cType: ResolvedType
//        get() {
//            if (isString) {
//                return ResolvedTypeReference("const char*")
//            }
//            if (isNative) {
//                return this
//            }
//            if (name == LONG_DOUBLE_STR) {
//                return ResolvedTypeReference("double")
//            }
//            throw IllegalStateException("Don't know how to convert $name to C")
//        }
//
//    override fun toString(): String {
//        return name
//    }
//}