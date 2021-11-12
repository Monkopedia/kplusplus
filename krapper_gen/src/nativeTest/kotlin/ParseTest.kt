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

import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.findQualifiers
import kotlinx.cinterop.memScoped
import platform.posix.random
import kotlin.test.Test
import kotlin.test.assertEquals

class ParseTest {
//    @Test
//    fun testParsing() = memScoped {
//        val index = createIndex(0, 0) ?: error("Failed to create Index")
//        defer { index.dispose() }
//        val tmpFile = "/tmp/${random()}_${random()}"
//        File(tmpFile).writeText(TestData.HEADER)
//        val classes = parseHeader(index, listOf(tmpFile)).findClasses(WrappedClass::defaultFilter)
//        val expectedClasses = Json.decodeFromString<List<WrappedClass>>(TestData.JSON)
//        assertEquals(expectedClasses, classes)
//    }

    @Test
    fun testResolve() {
        val resolver = ParsedResolver(TestData.TU)
        val cls = resolver.findClasses(WrappedClass::defaultFilter)
        val resolved = cls.resolveAll(resolver, ReferencePolicy.INCLUDE_MISSING)
        println("Classes: $cls")
    }

    @Test
    fun testTemplate() = memScoped {
        val index = createIndex(0, 0) ?: error("Failed to create Index")
        defer { index.dispose() }
        val tmpFile = "/tmp/${random()}_${random()}"
        File(tmpFile).writeText(TestData.TEMPLATE_HEADER)
        val classes = parseHeader(index, listOf(tmpFile)).also { println("Parsed") }.findClasses {
            println("Filter $this")
            defaultFilter()
        }
        println("Found classes ${classes.size}")
        println(classes)
    }

    @Test
    fun testQualifiers() = memScoped {
        val str = "std::vector<std::string>::iterator"
        val indices = findQualifiers(str)
        assertEquals(
            listOf(
                "std",
                "vector<std::string>",
                "iterator"
            ),
            indices.map { str.substring(it.start, it.endInclusive + 1) }
        )
    }
}
