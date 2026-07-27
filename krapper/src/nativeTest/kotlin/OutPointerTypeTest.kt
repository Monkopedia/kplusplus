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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.generator.model.kotlinType
import com.monkopedia.krapper.generator.model.type.WrappedEnumConstant
import com.monkopedia.krapper.generator.model.type.WrappedEnumType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.referenceTo
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Issue #103, Family 1 — an OUT-POINTER (`T*` / `T& out`) whose pointee `T` reduces to a
 * Kotlin scalar/enum must keep the POINTER indirection at the Kotlin/C boundary, NOT
 * collapse to the by-value pointee.
 *
 * The C wrapper signature for such a param is a real pointer (`size_t*`, `void*` for an
 * enum, ...), so cinterop types the extern as a `CValuesRef<...>?` / `COpaquePointer`.
 * Before the fix the Kotlin facade collapsed the pointer to a bare `size_t` scalar or to
 * the generated `enum class`, then `KotlinWriter.reference()` passed that value (or
 * `arg.value`, an Int) where the extern wanted a pointer → the generated Kotlin did not
 * compile. These shapes surfaced on the v8 surface (brick 3, #102) and were worked around
 * at the example config level; this locks the source fix in `typeToKotlinType`.
 */
class OutPointerTypeTest {

    private val sizeT = WrappedType("size_t")

    // A value-reduced enum: at the boundary it is its underlying integer, and it is NOT a
    // `.ptr`-backed wrapper — so a pointer to one cannot be the `enum class`.
    private val enumType = WrappedEnumType(
        cppName = "PropertyAttribute",
        underlying = WrappedType("int"),
        constants = listOf(WrappedEnumConstant("None", 0), WrappedEnumConstant("ReadOnly", 1))
    )

    @Test
    fun sizeTPointerIsACValuesRefPointer() {
        // `size_t*` (v8 Isolate::GetCodeRange(..., size_t*)) — was the bare `size_t` scalar.
        assertEquals("CValuesRef<ULongVar>?", pointerTo(sizeT).kotlinType.name)
    }

    @Test
    fun sizeTReferenceIsACValuesRefPointer() {
        // `size_t&` (v8 TraceBufferChunk::AddTraceEvent(size_t& index)) — a reference to a
        // native scalar marshals through the same pointer slot as `size_t*`.
        assertEquals("CValuesRef<ULongVar>?", referenceTo(sizeT).kotlinType.name)
    }

    @Test
    fun enumPointerIsAnOpaquePointer() {
        // `enum*` (v8 Maybe::To(PropertyAttribute*)) — the wrapper's cType is `void*`, so
        // the out-param is an opaque pointer, NOT the `enum class` (which would emit
        // `arg.value` into a pointer slot).
        assertEquals("void*", pointerTo(enumType).cType.toString())
        assertEquals("COpaquePointer", pointerTo(enumType).kotlinType.name)
    }

    @Test
    fun voidPointerPointerIsACValuesRefOfOpaquePointers() {
        // `void**` (v8 Isolate::GetCodeRange(void**, ...)) — already correct; regression-lock.
        assertEquals(
            "CValuesRef<COpaquePointerVar>?",
            pointerTo(pointerTo(WrappedType.VOID)).kotlinType.name
        )
    }

    @Test
    fun byValueEnumStillSurfacesAsTheEnumClass() {
        // The fix is scoped to a POINTER pointee: a by-value enum keeps the generated
        // `enum class` (and its `.value`/`fromValue` boundary conversions).
        assertEquals("PropertyAttribute", enumType.kotlinType.name)
    }

    @Test
    fun enumReferenceStillSurfacesAsTheEnumClass() {
        // An enum REFERENCE is deliberately a by-value enum at the boundary
        // (WrappedModifiedType.isEnum); only a `*` pointer becomes opaque.
        assertEquals("PropertyAttribute?", referenceTo(enumType).kotlinType.name)
    }
}
