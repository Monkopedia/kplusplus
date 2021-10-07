/*
 * Copyright 2021 Jason Monk
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

import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.model.WrappedClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import platform.posix.random

class ParseTest {
    @Test
    fun testParsing() = memScoped {
        val index = createIndex(0, 0) ?: error("Failed to create Index")
        defer { index.dispose() }
        val tmpFile = "/tmp/${random()}_${random()}"
        File(tmpFile).writeText(TestData.HEADER)
        val classes = parseHeader(index, listOf(tmpFile)).findClasses(WrappedClass::defaultFilter)
        val expectedClasses = Json.decodeFromString<List<WrappedClass>>(TestData.JSON)
        assertEquals(expectedClasses, classes)
    }
}
