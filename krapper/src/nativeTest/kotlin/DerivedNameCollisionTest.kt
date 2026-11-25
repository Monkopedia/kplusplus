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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.AllowListFilter
import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.InstantiationRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * #333: a C++ class named like a file or type the generator derives (the namespace's
 * `<ns>_Functions.kt`, a base's `<Base>Api` interface, a template's `<T>_Facade.kt`) reached the
 * same generated source, and the overwrite backstop aborted the run. The C++ class keeps its
 * name; the derived one yields to the next free name.
 */
class DerivedNameCollisionTest {

    private fun generate(
        body: String,
        only: List<String>,
        instantiations: List<InstantiationRequest> = emptyList()
    ): Map<String, String> = runBlocking {
        val dir = File.createTempDir("krapper_derived_name")
        try {
            val headerFile = File(dir, "p.h")
                .also { it.writeText("#pragma once\nnamespace p {\n$body\n}\n") }
            val out = File(dir, "out")
            val service = KrapperServiceImpl()
            service.setConfig(
                KrapperConfig(
                    pkg = "p",
                    compiler = "clang++",
                    moduleName = "P",
                    errorPolicy = ErrorPolicy.LOG,
                    referencePolicy = ReferencePolicy.IGNORE_MISSING,
                    debug = false
                )
            )
            val indexed = service.index(IndexRequest(listOf(headerFile.path), emptyList()))
            indexed.filterAndResolve(AllowListFilter(only))
            instantiations.forEach { indexed.requestInstantiation(it) }
            indexed.writeTo(out.path)
            File(out, "src").listFiles().associate { it.name to it.readText() }
        } finally {
            dir.rmR()
        }
    }

    private fun Map<String, String>.file(name: String): String =
        this[name] ?: error("no $name in $keys")

    @Test
    fun a_class_named_functions_keeps_its_file_beside_the_free_functions() {
        val out = generate(
            "struct Functions { int i; };\nvoid f(int x);",
            listOf("p::Functions", "p::f")
        )
        val cls = out.file("p_Functions.kt")
        assertTrue("@krapper.CppBinding(\"p::Functions\")" in cls, cls)
        assertTrue("var i: Int" in cls, cls)
        val functions = out.file("p_Functions_.kt")
        assertTrue("package p\n" in functions, functions)
        assertTrue("fun f(x: Int)" in functions, functions)
    }

    @Test
    fun a_class_named_like_a_base_interface_keeps_its_name() {
        val out = generate(
            "struct Foo { virtual int v(); };\nstruct Bar : Foo {};\nstruct FooApi { int i; };",
            listOf("p::Foo", "p::Bar", "p::FooApi")
        )
        val cls = out.file("p_FooApi.kt")
        assertTrue("@krapper.CppBinding(\"p::FooApi\")" in cls, cls)
        assertTrue("class FooApi(" in cls, cls)
        val api = out.file("p_FooApi_.kt")
        assertTrue("interface FooApi_ {" in api, api)
        for (implementor in listOf("p_Foo.kt", "p_Bar.kt")) {
            val text = out.file(implementor)
            assertTrue("FooApi_" in text, text)
        }
        assertEquals(
            listOf("CppBinding.kt", "p_Bar.kt", "p_Foo.kt", "p_FooApi.kt", "p_FooApi_.kt"),
            out.keys.sorted()
        )
    }

    @Test
    fun a_class_named_like_a_template_facade_keeps_its_file() {
        val out = generate(
            // A facade is generated for a global-namespace template only.
            "}\ntemplate<class T> struct Box { T v; };\nstruct Box_Facade { int i; };\n" +
                "namespace p {",
            listOf("Box_Facade"),
            listOf(InstantiationRequest("Box", listOf("int")))
        )
        val cls = out.file("root_Box_Facade.kt")
        assertTrue("@krapper.CppBinding(\"Box_Facade\")" in cls, cls)
        val facade = out.file("root_Box_Facade_.kt")
        assertTrue("interface Box<T>" in facade, facade)
        assertTrue("fun <T> Box(): Box<T> =" in facade, facade)
    }
}
