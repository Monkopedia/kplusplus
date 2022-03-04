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
//class ResolvedModifiedType(val baseType: ResolvedType, val modifier: String) : ResolvedType() {
//    override val isReturnable: Boolean
//        get() = modifier == "*" || modifier == "&" || baseType.isReturnable
//    override val cType: ResolvedType
//        get() = if (baseType.isString) baseType.cType else when (modifier) {
//            "*",
//            "&" -> pointerTo(
//                if (baseType.isNative || (baseType == LONG_DOUBLE)) baseType.cType
//                else VOID
//            )
//            "[]" -> arrayOf(baseType.cType)
//            else -> error("Don't know how to handle $modifier")
//        }
//
//    override val isNative: Boolean
//        get() = baseType.isNative
//    override val isString: Boolean
//        get() = baseType.isString
//
//    override val isVoid: Boolean
//        get() = false
//
//    override val pointed: ResolvedType
//        get() = if (modifier == "*") baseType else error("Cannot find pointed of non-pointer $this")
//    override val isPointer: Boolean
//        get() = ((this as? ResolvedModifiedType)?.modifier == "*")
//
//    override val isArray: Boolean
//        get() = modifier == "[]"
//
//    override val unreferenced: ResolvedType
//        get() = if (modifier == "&") baseType else error("Cannot unreference non-reference $this")
//
//    override val isReference: Boolean
//        get() = modifier == "&"
//    override val isConst: Boolean
//        get() = baseType.isConst
//    override val unconst: ResolvedType
//        get() = baseType.unconst
//
//    override fun toString(): String {
//        return "${baseType}$modifier"
//    }
//}
//
//class ResolvedPrefixedType(val baseType: ResolvedType, val modifier: String) : ResolvedType() {
//    override val isReturnable: Boolean
//        get() = baseType.isReturnable
//    override val cType: ResolvedType
//        get() = if (baseType.isString) baseType.cType else when (modifier) {
//            "const" -> const(baseType.cType)
//            else -> error("Don't know how to handle $modifier")
//        }
//
//    override val isNative: Boolean
//        get() = baseType.isNative
//    override val isString: Boolean
//        get() = baseType.isString
//
//    override val isVoid: Boolean
//        get() = false
//
//    override val pointed: ResolvedType
//        get() =
//            if (baseType.isPointer) ResolvedPrefixedType(baseType.pointed, modifier)
//            else error("Cannot find pointed of non-pointer $this")
//    override val isPointer: Boolean
//        get() = baseType.isPointer
//
//    override val isArray: Boolean
//        get() = baseType.isArray
//
//    override val unreferenced: ResolvedType
//        get() = const(baseType.unreferenced)
//
//    override val isReference: Boolean
//        get() = baseType.isReference
//    override val isConst: Boolean
//        get() = modifier == "const" || baseType.isConst
//    override val unconst: ResolvedType
//        get() =
//            if (modifier == "const") baseType
//            else ResolvedPrefixedType(baseType.unconst, modifier)
//
//    override fun toString(): String {
//        return "$modifier $baseType"
//    }
//}