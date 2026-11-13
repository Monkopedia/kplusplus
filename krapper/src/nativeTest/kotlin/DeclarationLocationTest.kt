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
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.filterRecursive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

/**
 * #288 — a drop row points at the C++ line that caused it: a dropped ARGUMENT at its parameter,
 * a dropped BASE at its base specifier, and a class or template forward-declared before its
 * definition at the definition.
 */
class DeclarationLocationTest {

    // Line numbers are load-bearing.
    private val header = """
        #pragma once
        namespace p {
        struct Hidden { int h; };
        struct Other { int o; };
        struct Shown;
        template <typename T> struct Tpl;
        struct Shown : Other,
                       Hidden {
          int take(int a,
                   Hidden h);
        };
        template <typename T> struct Tpl { T t; };
        }
    """.trimIndent()

    private class Parsed(val drops: List<DropRecord>, val tu: WrappedElement)

    private val parsed: Parsed by lazy {
        runBlocking {
            val dir = File.createTempDir("krapper_declaration_location")
            try {
                val headerFile = File(dir, "p.h").also { it.writeText(header) }
                val run = KrapperRun(GenerationContext())
                KrapperRun.using(run) {
                    val resolver = parseHeader(headerFile.path, "c++14")
                    // Hidden is unlisted, so the argument and the base naming it don't bind.
                    val allow = AllowListFilter(listOf("p::Shown", "p::Other"))
                    resolver.findClasses(allow.wrapperFilter())
                        .resolveAll(resolver, ReferencePolicy.IGNORE_MISSING)
                }
                Parsed(run.drops.drops.toList(), run.baseModelTu!!)
            } finally {
                dir.rmR()
            }
        }
    }

    private fun dropLine(predicate: (DropRecord) -> Boolean): Int? =
        parsed.drops.single(predicate).location?.line

    @Test
    fun dropped_argument_is_reported_at_its_parameter() {
        assertEquals(10, dropLine { it.symbol.startsWith("h: ") }, "${parsed.drops}")
    }

    @Test
    fun dropped_base_is_reported_at_its_base_specifier() {
        assertEquals(8, dropLine { "Base class p::Hidden" in it.reason }, "${parsed.drops}")
    }

    @Test
    fun forward_declared_class_and_template_are_located_at_their_definition() {
        val lines = parsed.tu.filterRecursive { it is WrappedClass || it is WrappedTemplate }
            .associate { it.toString() to it.sourceLocation?.line }
        assertEquals(7, lines.entries.single { "Shown" in it.key }.value, "$lines")
        assertEquals(12, lines.entries.single { "Tpl" in it.key }.value, "$lines")
    }
}
