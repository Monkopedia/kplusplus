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
package com.monkopedia.krapper.generator.model.type

import com.monkopedia.krapper.generator.GenerationContext
import com.monkopedia.krapper.generator.model.WrappedElement

abstract class WrappedType : WrappedElement() {
    abstract val cType: WrappedType

    override fun clone(): WrappedType = this

    abstract val isNative: Boolean
    open val isEnum: Boolean
        get() = false
    abstract val isString: Boolean
    abstract val isReturnable: Boolean
    abstract val isVoid: Boolean

    abstract val pointed: WrappedType
    abstract val isPointer: Boolean

    abstract val isArray: Boolean

    abstract val unreferenced: WrappedType

    abstract val isReference: Boolean
    abstract val isConst: Boolean
    abstract val unconst: WrappedType

    // Construction from a libclang CXType lives in the front-end (TypeFactories.kt) as
    // `WrappedType.Companion.invoke(CValue<CXType>, ...)` extensions — the type model
    // itself carries no clang/cinterop dependency (the front-end seam, issue #44).
    companion object : (String) -> WrappedType {

        const val LONG_DOUBLE_STR = "long double"
        val LONG_DOUBLE = WrappedTypeReference(LONG_DOUBLE_STR)
        val VOID = WrappedTypeReference("void")

        override fun invoke(type: String): WrappedType {
            if (type == "void") return VOID
            if (type == "std::size_t") return invoke("size_t")
            return GenerationContext.current.internedTypes.getOrPut(type) {
                if (type.startsWith("const ")) return const(invoke(type.substring("const ".length)))
                if (type.startsWith("typename ")) {
                    return WrappedTypename(type.substring("typename ".length))
                }
                if (type.endsWith("*")) {
                    return pointerTo(invoke(type.substring(0, type.length - 1).trim()))
                }
                if (type.endsWith("&")) {
                    return referenceTo(invoke(type.substring(0, type.length - 1).trim()))
                }
                if (type.isEmpty()) {
                    throw IllegalArgumentException("Empty type")
                }
                WrappedTypeReference(type)
            }
        }

        fun pointerTo(type: WrappedType): WrappedType = WrappedModifiedType(type, "*")

        fun referenceTo(type: WrappedType): WrappedType = WrappedModifiedType(type, "&")

        fun arrayOf(type: WrappedType): WrappedType = WrappedModifiedType(type, "[]")

        fun const(type: WrappedType): WrappedType {
            if (type.isConst) return type
            return WrappedPrefixedType(type, "const")
        }

        val UNRESOLVABLE: WrappedTypeReference
            get() = WrappedTypeReference("unresolveable")
    }
}
