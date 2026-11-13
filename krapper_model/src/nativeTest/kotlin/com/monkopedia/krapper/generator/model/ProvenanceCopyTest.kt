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
import com.monkopedia.krapper.generator.model.type.WrappedEnumType
import com.monkopedia.krapper.generator.model.type.WrappedFunctionPointer
import com.monkopedia.krapper.generator.model.type.WrappedModifiedType
import com.monkopedia.krapper.generator.model.type.WrappedOpaqueType
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import com.monkopedia.krapper.generator.model.type.WrappedTypedefRef
import com.monkopedia.krapper.generator.model.type.WrappedTypename
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * #255: [WrappedElement.sourceLocation] is base-class provenance, so every subclass's
 * `clone()`, and `copy()` on the [WrappedMethod] family, must keep it. The parse-time return
 * rewrites (pair->bool, StringRef->std::string, unique_ptr->release) build their result with
 * `copy()`, so a dropped location there leaves the method's drop-ledger row with no position.
 *
 * Kotlin/Native has no subclass reflection, and [WrappedElement] can't be `sealed` (its
 * subclasses span the `model` and `model.type` packages), so the subclasses are listed by
 * hand: every concrete [WrappedElement] in this module, 12 elements and 10 types. A new
 * subclass belongs in [instances].
 */
class ProvenanceCopyTest {

    private val int = WrappedType("int")

    private fun instances(): List<WrappedElement> = listOf(
        WrappedTU(),
        WrappedNamespace("demo"),
        WrappedClass("Thing"),
        WrappedBase(WrappedType("demo::Base")),
        WrappedField("count", int),
        WrappedMethod("lookup", int),
        WrappedConstructor("Thing", WrappedType.VOID, false, true),
        WrappedDestructor("~Thing", WrappedType.VOID),
        WrappedArgument("key", int),
        WrappedTemplate("Box"),
        WrappedTemplateParam("T", "c:@T"),
        WrappedTypedef("id_t", int),
        // The types share WrappedType.clone() = this; listed so a type that starts
        // overriding clone() is pinned too.
        WrappedTypedefRef("c:@id"),
        WrappedTypename("T"),
        WrappedTemplateRef("T"),
        WrappedTypeReference("demo::Thing"),
        WrappedOpaqueType(WrappedType("demo::Thing")),
        WrappedModifiedType(int, "*"),
        WrappedPrefixedType(int, "const"),
        WrappedTemplateType(WrappedType("std::vector"), listOf(int)),
        WrappedEnumType("demo::Color", int),
        WrappedFunctionPointer("Transform", int, listOf(int))
    )

    private fun <T : WrappedElement> located(element: T, line: Int): T = element.also {
        it.sourceLocation = SourceLocation("/x/h.h", line, 5)
    }

    @Test
    fun every_subclass_is_listed_once() {
        assertEquals(22, instances().map { it::class }.toSet().size)
    }

    @Test
    fun clone_keeps_sourceLocation() {
        for ((i, element) in instances().withIndex()) {
            val original = located(element, i + 1)
            assertEquals(
                original.sourceLocation,
                original.clone().sourceLocation,
                "${original::class.simpleName}.clone()"
            )
        }
    }

    @Test
    fun copy_keeps_sourceLocation() {
        val methods = instances().filterIsInstance<WrappedMethod>()
        assertEquals(3, methods.size, "WrappedMethod, WrappedConstructor, WrappedDestructor")
        for ((i, method) in methods.withIndex()) {
            val original = located(method, i + 1)
            // The shape the parse-time return rewrites use.
            val copy = original.copy(returnType = WrappedType("std::string"))
            assertEquals(
                original.sourceLocation,
                copy.sourceLocation,
                "${original::class.simpleName}.copy()"
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
}
