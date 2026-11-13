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
package com.monkopedia.krapper.parser

import com.monkopedia.krapper.generator.model.ModelIo
import com.monkopedia.krapper.generator.model.filterRecursive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * #288: the #101 check compares parses made under different file names, and ModelIo now writes
 * every location. [fixtureModelJson] must cancel exactly that name and nothing else, or the
 * check goes red for a rename or, worse, green for a real divergence.
 */
class FixtureModelJsonTest {

    private val args = CppParser.driverArgs(std = "c++17", includeDirs = emptyList())

    private fun json(source: String, name: String) =
        fixtureModelJson(CppParser.parse(source, name, args), name)

    private val fixture = json(NONCOPYABLE_FIXTURE, "noncopyable_1.cc")

    @Test
    fun the_same_fixture_under_another_name_encodes_identically() {
        val model = ModelIo.decodeFromString(fixture)
        val located = model.filterRecursive { it.sourceLocation != null }
        assertTrue(located.isNotEmpty(), "precondition: the fixture model carries locations")
        assertEquals(fixture, json(NONCOPYABLE_FIXTURE, "noncopyable_2.cc"))
    }

    @Test
    fun a_non_location_difference_still_fails() {
        val changed = NONCOPYABLE_FIXTURE.replace("int get() const;", "int get();")
        assertNotEquals(NONCOPYABLE_FIXTURE, changed, "precondition: the fixture changed")
        assertNotEquals(fixture, json(changed, "noncopyable_2.cc"))
    }

    @Test
    fun a_moved_declaration_still_fails() {
        assertNotEquals(fixture, json("\n" + NONCOPYABLE_FIXTURE, "noncopyable_2.cc"))
    }
}
