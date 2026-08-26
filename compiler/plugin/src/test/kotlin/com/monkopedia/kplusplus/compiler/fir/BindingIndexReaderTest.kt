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
package com.monkopedia.kplusplus.compiler.fir

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The hand-written JSON reader ([BindingIndexReader]) that stands in for kotlinx.serialization,
 * which may not go on the kotlinc plugin's classpath (`docs/design/live-service.md` §4.4 — "the
 * one hard rule in this section").
 *
 * [REAL_SHAPE] is shaped exactly like krapper's real output: pretty-printed with a four-space
 * indent, `encodeDefaults` so `schemaVersion` is present, fields in data-class declaration
 * order, and the `templates`/`kotlinTypeMap`/`drops` members the reader must walk past. `drops`
 * is 95% of a real index (~4k entries in featuregen's 2MB file) and is never read.
 */
class BindingIndexReaderTest {

    @Test
    fun readsTheFieldsTheResolverNeeds() {
        val index = loaded(REAL_SHAPE)
        assertEquals(2, index.schemaVersion)
        assertNull(index.rootPackage)
        assertEquals(4, index.bindingCount)
        assertEquals("std.Vector__Int", index.classIdFor("std::vector<int>"))
        // The two shapes #206 is about: a pointer element and a nested template.
        assertEquals("std.Vector__Thing_P", index.classIdFor("std::vector<Thing*>"))
        assertEquals(
            "std.Vector__Vector_int_",
            index.classIdFor("std::vector<std::vector<int>>")
        )
        assertEquals("root.Box__Int", index.classIdFor("Box<int>"))
        assertNull(index.classIdFor("std::vector<double>"))
    }

    @Test
    fun readsARootPackage() {
        val rooted = REAL_SHAPE.replace("\"rootPackage\": null", "\"rootPackage\": \"app\"")
        assertEquals("app", loaded(rooted).rootPackage)
    }

    /**
     * A C++ spec cannot contain a quote or a backslash, but a drop `message` can, and the reader
     * walks past those on its way to the bindings. Decoding is exercised on a `spec` because
     * that is the value whose exactness the lookup depends on.
     */
    @Test
    fun decodesStringEscapes() {
        val doc = """
            {
                "schemaVersion": 2,
                "runId": "0",
                "rootPackage": null,
                "bindings": [
                    {
                        "spec": "Box<$BS${QT}q$BS${BS}A$BS$QT>",
                        "classId": "root.Box__Quoted",
                        "unknownFutureField": { "nested": [1, 2, "x"] }
                    },
                    { "spec": "caf${BS}u00e9", "classId": "root.Cafe" }
                ],
                "drops": []
            }
        """.trimIndent()
        val index = loaded(doc)
        assertEquals("root.Box__Quoted", index.classIdFor("Box<\"q\\A\">"))
        assertEquals("root.Cafe", index.classIdFor("café"))
    }

    @Test
    fun anEmptyBindingListIsAValidIndex() {
        val index = loaded(
            """
            {
                "schemaVersion": 2,
                "runId": "0",
                "rootPackage": null,
                "bindings": [],
                "templates": null,
                "kotlinTypeMap": {},
                "drops": []
            }
            """.trimIndent()
        )
        assertEquals(0, index.bindingCount)
    }

    @Test
    fun aMissingBindingsArrayIsNotAnIndex() {
        assertTrue("bindings" in unavailable("""{ "schemaVersion": 2, "runId": "0" }"""))
    }

    @Test
    fun aMissingSchemaVersionIsNotAnIndex() {
        assertTrue("schemaVersion" in unavailable("""{ "runId": "0", "bindings": [] }"""))
    }

    /**
     * `BindingIndex.SCHEMA_VERSION` is bumped exactly when the shape changes incompatibly, so
     * reading a newer index optimistically is the sort of assumption #206 exists to stop.
     */
    @Test
    fun anUnknownSchemaVersionIsRefusedRatherThanGuessedAt() {
        val future = REAL_SHAPE.replace("\"schemaVersion\": 2", "\"schemaVersion\": 9")
        val reason = unavailable(future)
        val range = "${BindingIndexReader.MIN_SCHEMA}..${BindingIndexReader.MAX_SCHEMA}"
        assertTrue("schemaVersion 9" in reason, reason)
        assertTrue(range in reason, reason)
    }

    /** Every malformed shape comes back as a refusal, never as a thrown exception. */
    @Test
    fun malformedBytesAreRefusedNotThrown() {
        listOf(
            "{ this is not json",
            """{ "schemaVersion": """,
            "",
            "[]",
            """{ "schemaVersion": 2, "bindings": [ { "spec": "x" } ] }""",
            """{ "schemaVersion": 2, "bindings": [ { "spec": "x", "classId": "y" } """
        ).forEach {
            assertIs<BindingIndexLoad.Unavailable>(
                BindingIndexReader.parse(it, "/x"),
                "Expected a refusal for: $it"
            )
        }
    }

    @Test
    fun anAbsentPathIsRefusedWithAnActionableReason() {
        val load = assertIs<BindingIndexLoad.Unavailable>(BindingIndexReader.load(null))
        assertTrue("bindingIndexPath" in load.reason, load.reason)
    }

    @Test
    fun aMissingFileIsRefusedWithItsPathAndTheFix() {
        val absent = "/definitely/not/here/binding-index.json"
        val load = assertIs<BindingIndexLoad.Unavailable>(BindingIndexReader.load(absent))
        assertTrue(absent in load.reason, load.reason)
        assertTrue("kplusplusSync" in load.reason, load.reason)
    }

    private fun loaded(text: String): LoadedBindingIndex =
        assertIs<BindingIndexLoad.Loaded>(
            BindingIndexReader.parse(text, "/tmp/binding-index.json")
        ).index

    private fun unavailable(text: String): String =
        assertIs<BindingIndexLoad.Unavailable>(
            BindingIndexReader.parse(text, "/tmp/binding-index.json")
        ).reason

    private companion object {
        /** A single backslash / double quote, spelled once so the JSON below stays legible. */
        const val BS = "\\"
        const val QT = "\""

        val REAL_SHAPE = """
            {
                "schemaVersion": 2,
                "runId": "3490e82b1b423798",
                "rootPackage": null,
                "bindings": [
                    {
                        "spec": "Box<int>",
                        "classId": "root.Box__Int",
                        "pkg": "root",
                        "simpleName": "Box__Int",
                        "cppBase": "Box",
                        "templateArgs": null
                    },
                    {
                        "spec": "std::vector<Thing*>",
                        "classId": "std.Vector__Thing_P",
                        "pkg": "std",
                        "simpleName": "Vector__Thing_P",
                        "cppBase": "std::vector",
                        "templateArgs": null
                    },
                    {
                        "spec": "std::vector<int>",
                        "classId": "std.Vector__Int",
                        "pkg": "std",
                        "simpleName": "Vector__Int",
                        "cppBase": "std::vector",
                        "templateArgs": null
                    },
                    {
                        "spec": "std::vector<std::vector<int>>",
                        "classId": "std.Vector__Vector_int_",
                        "pkg": "std",
                        "simpleName": "Vector__Vector_int_",
                        "cppBase": "std::vector",
                        "templateArgs": null
                    }
                ],
                "templates": null,
                "kotlinTypeMap": {
                    "root.Box__Int": "Box<int>",
                    "std.Vector__Int": "std::vector<int>"
                },
                "drops": [
                    {
                        "severity": "INFO",
                        "message": "Non-const overload duplicates a const overload",
                        "symbol": "Buffer::at",
                        "location": {
                            "file": "../../src/cppMain/include/strings_feature.h",
                            "line": 339,
                            "column": 11
                        },
                        "phase": "DEDUP",
                        "hint": "no Kotlin binding is generated for this symbol"
                    },
                    {
                        "severity": "WARNING",
                        "message": "a message with a ${BS}${QT}quote${BS}${QT} and a ${BS}${BS}",
                        "symbol": null,
                        "location": null,
                        "phase": null,
                        "hint": null
                    }
                ]
            }
        """.trimIndent()
    }
}
