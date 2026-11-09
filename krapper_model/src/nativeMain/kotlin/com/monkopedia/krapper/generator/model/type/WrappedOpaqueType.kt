/*
 * Copyright 2026 Jason Monk
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

/**
 * An unresolvable class kept under `OPAQUE_MISSING` (#268). It spells exactly as [type], so a
 * wrapper still casts its `void*` to the real C++ pointer, but it has no binding: Kotlin sees
 * `COpaquePointer`. Any by-value use is dropped: a copy needs the class complete and copyable,
 * and an unlisted class is often only forward-declared.
 */
class WrappedOpaqueType(val type: WrappedType) : WrappedType() {
    override val cType: WrappedType
        get() = pointerTo(VOID)

    override val isReturnable: Boolean
        get() = false
    override val isNative: Boolean
        get() = false
    override val isString: Boolean
        get() = false
    override val isVoid: Boolean
        get() = false

    override val pointed: WrappedType
        get() = error("Cannot get pointee of non-pointer opaque $this")
    override val isPointer: Boolean
        get() = false

    override val isArray: Boolean
        get() = false

    override val unreferenced: WrappedType
        get() = error("Cannot unreference non-reference opaque $this")
    override val isReference: Boolean
        get() = false

    override val isConst: Boolean
        get() = false
    override val unconst: WrappedType
        get() = this

    override fun toString(): String = type.toString()
}
