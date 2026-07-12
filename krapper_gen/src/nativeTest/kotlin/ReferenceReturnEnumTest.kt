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

import com.monkopedia.krapper.ReferencePolicy.INCLUDE_MISSING
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.codegen.CppWriter
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.type.WrappedEnumConstant
import com.monkopedia.krapper.generator.model.type.WrappedEnumType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.referenceTo
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Issue #105, Family 2 — a REFERENCE-return of a value-reduced type. A method returning
 * `Enum&` (e.g. the `std::byte&`/`_Ios_Fmtflags&` compound-assignment operators from
 * `<ios>`) must be lowered by VALUE: an enum has no `.ptr` storage to wrap, so reading
 * through the reference just yields the enum value, exactly like a by-value enum return.
 *
 * Before the fix `determineReturnStyle` selected ENUM_RETURN (correct) but the resolved
 * return type was still the REFERENCE, whose cType is `void*` (a `&` to a non-native
 * pointee). So the ENUM_RETURN C cast became the ill-formed `return (void*)(enumLvalue)`
 * and the extern returned a pointer the Kotlin `fromValue(Int)` couldn't consume. The fix
 * strips the reference so the return resolves as a by-value enum (cType = the underlying
 * integer): the wrapper emits `return (<underlying>)(call)` and Kotlin reads it via
 * `fromValue`, identical to any other enum return.
 */
class ReferenceReturnEnumTest {

    private val flag = WrappedEnumType(
        cppName = "Flag",
        underlying = WrappedType("unsigned int"),
        constants = listOf(
            WrappedEnumConstant("FlagNone", 0),
            WrappedEnumConstant("FlagA", 1)
        )
    )

    // A class with a method returning `Flag&` (the value-reduced enum reference),
    // attached to a TU so the ParsedResolver can resolve it (mirrors CppCodeTests).
    private val tu = WrappedTU()
    private val cls = WrappedClass("FlagBox").also {
        it.parent = tu
        tu.addChild(it)
        it.addChild(
            WrappedMethod("orInto", referenceTo(flag), MethodType.METHOD)
        )
    }

    @Test
    fun enumReferenceReturnResolvesByValue() = runBlocking {
        val (_, method) = resolve()
        // Selected the by-value enum return path...
        assertEquals(ReturnStyle.ENUM_RETURN, method.returnStyle)
        // ...and the wrapper's C return type is the underlying integer, NOT `void*`.
        assertEquals("unsigned int", method.returnType.cType.toString())
    }

    @Test
    fun enumReferenceReturnCWrapperCastsToUnderlyingInteger() = runBlocking {
        val code = generateCpp()
        // The extern hands back the underlying integer, not a pointer.
        assertTrue(
            "unsigned int FlagBox_or_into" in code,
            "expected an `unsigned int`-returning wrapper, got:\n$code"
        )
        val returnLine = code.lineSequence().first { it.trim().startsWith("return") }
        // The RETURN reads the enum value through the reference and casts it to the
        // underlying integer — NOT the ill-formed `(void*)(enumLvalue)` / `&(local)`.
        assertTrue(
            "(unsigned int)" in returnLine,
            "expected an underlying-integer cast on the return, got:\n$returnLine"
        )
        assertFalse(
            "void*" in returnLine || "&(" in returnLine,
            "enum-reference return must not marshal as a pointer, got:\n$returnLine"
        )
    }

    private suspend fun resolve(): Pair<ResolvedClass, ResolvedMethod> {
        val ctx = ResolveContext.Empty
            .withClasses(emptyList())
            .copy(resolver = ParsedResolver(tu))
            .withPolicy(INCLUDE_MISSING)
        val rcls = cls.resolve(ctx) ?: error("Resolve failed for $cls")
        val method = (cls.children[0] as WrappedMethod).resolve(ctx + cls)
            ?: error("Resolve failed for orInto")
        return rcls to method as ResolvedMethod
    }

    private suspend fun generateCpp(): String {
        val code = CppCodeBuilder()
        val writer = CppWriter(File("/tmp/ref_return_enum.cpp"), code)
        val (rcls, method) = resolve()
        with(writer) {
            code.onGenerate(rcls, method)
        }
        return code.toString()
    }
}
