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

import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedBase
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Issue #107 — `Local<PolymorphicBase>.reference()` must return the CONCRETE base, not the
 * (often empty) base-interface marker.
 *
 * A class used as a base gets a generated `interface <Name>Api` whose method surface is
 * built by `baseInterfaceMethods`, which includes ONLY VIRTUAL instance methods (the
 * interface exists for polymorphic dispatch). When a polymorphic base's instance methods are
 * all NON-virtual (e.g. v8::Value's `Int32Value`/`IsString`), `<Name>Api` collapses to an
 * EMPTY marker (`interface ValueApi { val ptr: COpaquePointer }`).
 *
 * The dereference accessors `reference()`/`pointer_reference()` (C++ `operator*`/`operator->`
 * on a `Local<T>`/pointer wrapper) had their DECLARED return type widened to that marker via
 * the base->interface remap, so a dereferenced `Local<Value>` exposed none of the concrete
 * methods — a manual `as? Value` downcast was required. The fix keeps the deref return
 * CONCRETE (the body already constructs the concrete `Value(ptr, memScope)`); genuine
 * polymorphic METHOD positions still remap, so subclass substitution there is unaffected.
 *
 * This locks both halves: the marker really is empty for a non-virtual-method base (the root
 * cause), and the wrapper's `reference()` returns the concrete `Base` so its methods are
 * reachable without a cast.
 */
class PolymorphicReferenceTest {
    private val writer = KotlinWriter("test.pkg")
    private val testDir = File("/tmp/polyRefTestDir")

    @BeforeTest
    fun setup() {
        if (testDir.exists()) testDir.rmR()
    }

    @AfterTest
    fun tearDown() {
        if (testDir.exists()) testDir.rmR()
    }

    private fun method(name: String, returnType: WrappedType) =
        WrappedMethod(name, returnType, MethodType.METHOD)

    /**
     * Build + resolve a three-class model: a polymorphic `Base` with a NON-virtual instance
     * method, a `Derived` that names it as a base (so `Base` is treated as a base and gets a
     * `BaseApi`), and a `Local` wrapper whose `operator*` returns `Base`.
     */
    private fun resolveModel(): List<ResolvedClass> = runBlocking {
        // Install a fresh per-run KrapperRun (ledger + intern cache + rootPackage), as
        // `IndexedServiceImpl` does for every service call — brick B4 made that state
        // per-run rather than a process global that had to be reset first.
        KrapperRun.using(KrapperRun(GenerationContext())) {
            val baseType = WrappedType("Poly::Base")
            val base = WrappedClass("Base").apply {
                metadata.hasConstructor = true
                addChild(WrappedConstructor("Base", baseType, false, true))
                // Non-virtual instance method — the shape that makes BaseApi an empty marker.
                addChild(method("get_value", WrappedType("int")))
            }
            val derived = WrappedClass("Derived").apply {
                metadata.hasConstructor = true
                addChild(WrappedBase(baseType))
                addChild(WrappedConstructor("Derived", WrappedType("Poly::Derived"), false, true))
            }
            val local = WrappedClass("Local").apply {
                metadata.hasConstructor = true
                addChild(WrappedConstructor("Local", WrappedType("Poly::Local"), false, true))
                // operator* (the deref accessor) returns the base by reference.
                addChild(method("operator*", baseType))
            }
            val tu = WrappedTU().also { tu ->
                tu.addChild(
                    WrappedNamespace("Poly").also { ns ->
                        ns.parent = tu
                        listOf(base, derived, local).forEach {
                            ns.addChild(it)
                            it.parent = ns
                        }
                    }
                )
            }
            val resolver = ParsedResolver(tu)
            resolver.findClasses { defaultFilter() }
                .resolveAll(resolver, ReferencePolicy.IGNORE_MISSING)
                .filterIsInstance<ResolvedClass>()
        }
    }

    @Test
    fun referenceAccessorReturnsConcreteBaseNotEmptyApiMarker() {
        writer.generate(testDir, resolveModel())

        // Root cause: BaseApi is generated (Base is a base) but is an EMPTY marker — the
        // base's only instance method is NON-virtual, so baseInterfaceMethods excludes it.
        val baseApi = File(testDir, "poly_BaseApi.kt")
        assertTrue(baseApi.exists(), "BaseApi interface should be generated for the base class")
        val baseApiText = baseApi.readText()
        assertTrue("interface BaseApi" in baseApiText, "BaseApi marker should be emitted")
        assertFalse(
            "fun " in baseApiText,
            "BaseApi is an empty marker (non-virtual methods excluded); got:\n$baseApiText"
        )

        // The fix: the wrapper's `reference()` (operator*) returns the CONCRETE `Base`, so the
        // non-virtual `get_value()` is reachable through the deref — NOT the empty `BaseApi`.
        val localText = File(testDir, "poly_Local.kt").readText()
        assertTrue(
            "fun reference(): Base" in localText,
            "reference() must return the concrete Base; got:\n$localText"
        )
        assertFalse(
            "BaseApi" in localText,
            "reference() must NOT widen to the empty BaseApi marker (#107); got:\n$localText"
        )
    }
}
