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
package com.monkopedia.krapper.generator.model

import com.monkopedia.krapper.SourceLocation
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * #255: [WrappedElement.sourceLocation] is base-class provenance, so `clone()`, and `copy()`
 * on the [WrappedMethod] family, must keep it. The parse-time return rewrites (pair->bool,
 * StringRef->std::string, unique_ptr->release) build their result with `copy()`, so a dropped
 * location there leaves the method's drop-ledger row with no position.
 *
 * Both entry points are final and carry it themselves, so a subclass's impl can't drop it.
 * The dummies below are such subclasses: they rebuild only their own state, like a new
 * subclass that knows nothing about provenance.
 */
class ProvenanceCopyTest {

    private class WrappedDummy : WrappedElement() {
        override fun cloneImpl(): WrappedElement = WrappedDummy()
    }

    private class DummyMethod : WrappedMethod("dummy", WrappedType("int")) {
        override fun cloneImpl(): WrappedMethod = DummyMethod()

        override fun copyImpl(
            name: String,
            returnType: WrappedType,
            methodType: MethodType,
            children: List<WrappedElement>
        ): WrappedMethod = DummyMethod()
    }

    private val int = WrappedType("int")

    private fun <T : WrappedElement> located(element: T, line: Int = 12): T = element.also {
        it.sourceLocation = SourceLocation("/x/h.h", line, 5)
    }

    @Test
    fun clone_keeps_sourceLocation_for_a_subclass_that_does_not_carry_it() {
        for (original in listOf(located(WrappedDummy()), located(DummyMethod()))) {
            assertEquals(
                original.sourceLocation,
                original.clone().sourceLocation,
                "${original::class.simpleName}"
            )
        }
    }

    @Test
    fun copy_keeps_sourceLocation_for_every_method_kind() {
        val methods = listOf(
            WrappedMethod("lookup", int),
            WrappedConstructor("Thing", WrappedType.VOID, false, true),
            WrappedDestructor("~Thing", WrappedType.VOID),
            DummyMethod()
        )
        for (original in methods.map { located(it) }) {
            // The shape the parse-time return rewrites use.
            val copy = original.copy(returnType = WrappedType("std::string"))
            assertEquals(
                original.sourceLocation,
                copy.sourceLocation,
                "${original::class.simpleName}"
            )
        }
    }

    @Test
    fun cloneRecursive_keeps_sourceLocation_down_the_tree() {
        val cls = located(WrappedClass("Thing"), 1)
        val method = located(WrappedMethod("lookup", int), 2)
        cls.addChild(method)
        method.addChild(located(WrappedArgument("key", int), 3))

        val clone = cls.cloneRecursive()
        val clonedMethod = clone.children.single()
        assertEquals(cls.sourceLocation, clone.sourceLocation)
        assertEquals(method.sourceLocation, clonedMethod.sourceLocation)
        assertEquals(SourceLocation("/x/h.h", 3, 5), clonedMethod.children.single().sourceLocation)
    }

    /**
     * #288: these three lost `data` (whose `copy()` dropped provenance) and spell their value
     * equality out instead; it must still be by value, never by identity or by location.
     */
    @Test
    fun former_data_classes_keep_value_equality() {
        val pairs = listOf(
            { n: String -> WrappedField(n, WrappedTypeReference("int")) },
            { n: String -> WrappedTemplate(n) },
            { n: String -> WrappedTypeReference(n) }
        )
        for (make in pairs) {
            val a = make("a")
            val b = located(make("a"))
            assertEquals<Any>(a, b)
            assertEquals(a.hashCode(), b.hashCode())
            assertNotEquals<Any>(a, make("b"))
        }
        assertNotEquals<Any>(
            WrappedField("a", WrappedTypeReference("int")),
            WrappedField("a", WrappedTypeReference("long"))
        )
    }
}
