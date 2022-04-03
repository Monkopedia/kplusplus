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
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.findQualifiers
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedConstructor
import kotlinx.cinterop.memScoped
import platform.posix.random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        try {
            val resolver = ParsedResolver(TestData.TU)
            val cls = resolver.findClasses(WrappedElement::defaultFilter)
            val resolved = cls.resolveAll(resolver, ReferencePolicy.INCLUDE_MISSING)
            println("Classes: $cls")
        } catch (t: Throwable) {
            t.printStackTrace()
            throw t
        }
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

    @Test
    fun testResolveConstRef() = memScoped {
        val result = listOf(TestData.TestClass.cls).resolveAll(
            object : Resolver {
                override fun resolve(type: WrappedType, context: ResolveContext): Pair<ResolvedClass, WrappedClass>? {
                    error("Not supported")
                }

                override fun resolveTemplate(
                    type: WrappedType,
                    context: ResolveContext
                ): WrappedTemplate {
                    error("Not supported")
                }

                override fun findClasses(filter: ElementFilter): List<WrappedClass> {
                    return emptyList()
                }
            },
            ReferencePolicy.IGNORE_MISSING
        )
        assertTrue(result.first().children.any { (it as? ResolvedConstructor)?.isCopyConstructor == true })
    }
}
