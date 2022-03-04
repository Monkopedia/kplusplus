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
//class ResolvedTemplateRef(val target: String) : ResolvedType() {
//    override val cType: ResolvedType
//        get() = error("Can't convert template $target")
//
//    override val isReturnable: Boolean
//        get() = false
//    override val isNative: Boolean
//        get() = false
//    override val isString: Boolean
//        get() = false
//
//    override val isVoid: Boolean
//        get() = false
//
//    override val pointed: ResolvedType
//        get() = error("Cannot get pointee of non-pointer template $this")
//    override val isPointer: Boolean
//        get() = false
//
//    override val isArray: Boolean
//        get() = false
//
//    override val unreferenced: ResolvedType
//        get() = error("Cannot get unreference of non-reference template $this")
//
//    override val isReference: Boolean
//        get() = false
//    override val isConst: Boolean
//        get() = false
//    override val unconst: ResolvedTypeReference
//        get() = error("Cannot unconst non-const template $this")
//
//    override fun toString(): String {
//        return "template<$target>"
//    }
//}