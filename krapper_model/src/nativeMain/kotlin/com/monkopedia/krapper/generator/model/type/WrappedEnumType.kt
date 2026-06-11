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

import kotlinx.serialization.Serializable

/** One constant of a C/C++ enum: its declared name and integer value. */
@Serializable
data class WrappedEnumConstant(val name: String, val value: Long)

/**
 * A C/C++ enum carried across the boundary.
 *
 * The C/C++ side reduces the enum to its underlying integer (the wrapper's C
 * signature is `int`/`unsigned int`/..., and the generated C++ casts `(Color)(x)`
 * / `(int)(call)` via `RAW_CAST` + `ENUM_RETURN`). This type carries the dual
 * identity such a value needs:
 *  - [toString] / [cppName] renders the real enum spelling, so the generated C++
 *    wrapper casts to and calls the actual enum type, which is required because
 *    C++ does not implicitly convert integer -> enum for an argument (nor enum ->
 *    integer for a *scoped* enum's return).
 *  - [cType] is the underlying integer, so the wrapper's C signature stays a
 *    plain `Int`/`UInt`/etc.
 *
 * When [constants] are available, the Kotlin binding surfaces this as a real
 * `enum class` (see WrappedKotlinType / KotlinWriter), converting `value.value`
 * (arg) and `Enum.fromValue(int)` (return) at the Kotlin/C edge. When constants
 * couldn't be recovered, it falls back to exposing the underlying integer on the
 * Kotlin side too.
 */
class WrappedEnumType(
    val cppName: String,
    val underlying: WrappedType,
    val constants: List<WrappedEnumConstant> = emptyList()
) : WrappedType() {
    override val isArray: Boolean
        get() = false
    override val isPointer: Boolean
        get() = false
    override val isReference: Boolean
        get() = false
    override val pointed: WrappedType
        get() = error("Cannot get pointed of non-pointer enum type $this")
    override val unreferenced: WrappedType
        get() = error("Cannot unreference non-reference enum type $this")
    override val isConst: Boolean
        get() = false
    override val unconst: WrappedType
        get() = this
    override val isString: Boolean
        get() = false

    // Not native: a native type would skip the boundary cast (castMethod NATIVE),
    // but a scoped enum needs an explicit (Color)/(int) cast in both directions.
    override val isNative: Boolean
        get() = false
    override val isEnum: Boolean
        get() = true

    // Returnable so it flows through the by-value return path rather than being
    // wrapped as a void* pointer.
    override val isReturnable: Boolean
        get() = true
    override val isVoid: Boolean
        get() = false
    override val cType: WrappedType
        get() = underlying

    override fun toString(): String = cppName
}
