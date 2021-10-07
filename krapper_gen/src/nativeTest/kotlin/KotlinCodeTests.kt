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

import com.monkopedia.krapper.generator.builders.KotlinCodeBuilder
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.NameHandler
import com.monkopedia.krapper.generator.codegen.WrappedKotlinWriter
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTypeReference
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class KotlinCodeTests {
    val writer = WrappedKotlinWriter(NameHandler(), "test.pkg")
    val testDir = File("/tmp/testDir")

    @BeforeTest
    fun setup() {
        if (testDir.exists()) {
            testDir.rmR()
        }
    }

    @Test
    fun testEmptyFile() {
        writer.generate(testDir, listOf(WrappedClass("TestLib::EmptyClass")))
        val output = File(testDir, "EmptyClass.kt")
        assertTrue(output.exists())
        assertCode(
            """
            |package TestLib
            |
            |import kotlin.Pair
            |import kotlinx.cinterop.COpaquePointer
            |import kotlinx.cinterop.MemScope
            |
            |// BEGIN KRAPPER GEN for TestLib::EmptyClass
            |
            |value class EmptyClass private constructor(val source: Pair<COpaquePointer, MemScope>) {
            |    val ptr: COpaquePointer
            |        inline get() {
            |            return source.first
            |        }
            |
            |    val memScope: MemScope
            |        inline get() {
            |            return source.second
            |        }
            |
            |    companion object {
            |    }
            |}
            |
            |// END KRAPPER GEN for TestLib::EmptyClass
            |""".trimMargin(),
            output.readText()
        )
    }

    @Test
    fun testConstructor_noDestructor() {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestLib::Constructable",
                methods = listOf(
                    WrappedMethod(
                        "Constructable",
                        WrappedTypeReference("TestLib::Constructable"),
                        emptyList(),
                        false,
                        MethodType.CONSTRUCTOR
                    )
                )
            )
            builder.onGenerate(cls, cls.methods.first())
        }
        assertCode(
            """
            |fun MemScope.Constructable(): Constructable {
            |    val obj: COpaquePointer = (TestLib_Constructable_new() ?: error("Creation failed"))
            |    defer {
            |        free(obj)
            |    }
            |    return Constructable((obj to this))
            |}
            |""".trimMargin(),
            builder.toString()
        )
    }

    @Test
    fun testConstructor_destructor() {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestLib::Constructable",
                methods = listOf(
                    WrappedMethod(
                        "Constructable",
                        WrappedTypeReference("TestLib::Constructable"),
                        emptyList(),
                        false,
                        MethodType.CONSTRUCTOR
                    ),
                    WrappedMethod(
                        "Constructable",
                        WrappedTypeReference("TestLib::Constructable"),
                        emptyList(),
                        false,
                        MethodType.DESTRUCTOR
                    )
                )
            )
            builder.onGenerate(cls, cls.methods.first())
        }
        assertCode(
            """
            |fun MemScope.Constructable(): Constructable {
            |    val obj: COpaquePointer = (TestLib_Constructable_new() ?: error("Creation failed"))
            |    defer {
            |        TestLib_Constructable_dispose(obj)
            |    }
            |    return Constructable((obj to this))
            |}
            |""".trimMargin(),
            builder.toString()
        )
    }

    @Test
    fun testMethod_voidReturn() {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestLib::TestClass",
                methods = listOf(
                    WrappedMethod(
                        "setSome",
                        WrappedTypeReference.VOID,
                        listOf(
                            WrappedArgument("a", WrappedTypeReference("int")),
                            WrappedArgument("b", WrappedTypeReference("long")),
                            WrappedArgument("c", WrappedTypeReference("long long"))
                        ),
                        false,
                        MethodType.METHOD
                    )
                )
            )
            builder.onGenerate(cls, cls.methods.first())
        }
        assertCode(
            """
            |inline fun setSome(a: Int, b: Long, c: Long): Unit {
            |    return TestLib_TestClass_set_some(ptr, a, b, c)
            |}
            |""".trimMargin(),
            builder.toString()
        )
    }

    @Test
    fun testMethod_pointerInput() {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestLib::TestClass",
                methods = listOf(
                    WrappedMethod(
                        "setPointers",
                        WrappedTypeReference.VOID,
                        listOf(
                            WrappedArgument("a", WrappedTypeReference("int*")),
                            WrappedArgument("b", WrappedTypeReference("long*")),
                            WrappedArgument("c", WrappedTypeReference("long long*"))
                        ),
                        false,
                        MethodType.METHOD
                    )
                )
            )
            builder.onGenerate(cls, cls.methods.first())
        }
        assertCode(
            """
            |inline fun setPointers(a: CValuesRef<IntVar>?, b: CValuesRef<LongVar>?, c: CValuesRef<LongVar>?): Unit {
            |    return TestLib_TestClass_set_pointers(ptr, a, b, c)
            |}
            |""".trimMargin(),
            builder.toString()
        )
    }

    @Test
    fun testMethod_returnValue() {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestLib::TestClass",
                methods = listOf(
                    WrappedMethod(
                        "sum",
                        WrappedTypeReference("long"),
                        listOf(),
                        false,
                        MethodType.METHOD
                    )
                )
            )
            builder.onGenerate(cls, cls.methods.first())
        }
        assertCode(
            """
            |inline fun sum(): Long {
            |    return TestLib_TestClass_sum(ptr)
            |}
            |""".trimMargin(),
            builder.toString()
        )
    }

    @Test
    fun testMethod_returnString() {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "std::vector<std::string>",
                methods = listOf(
                    WrappedMethod(
                        "at",
                        WrappedTypeReference("std::string"),
                        listOf(
                            WrappedArgument("pos", WrappedTypeReference("size_t")),
                        ),
                        false,
                        MethodType.METHOD
                    )
                )
            )
            builder.onGenerate(cls, cls.methods.first())
        }
        assertCode(
            """
            |inline fun at(pos: size_t): String? {
            |    val str: CPointer<ByteVar>? = std_vector_std_string_at(ptr, pos)
            |    val ret: String? = str?.toKString()
            |    free(str)
            |    return ret
            |}
            |""".trimMargin(),
            builder.toString()
        )
    }

    @Test
    fun testMethod_inputWrapped() {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestLib::TestClass",
                methods = listOf(
                    WrappedMethod(
                        "setPrivateFrom",
                        WrappedTypeReference.VOID,
                        listOf(
                            WrappedArgument("other", WrappedTypeReference("TestLib::OtherClass")),
                        ),
                        false,
                        MethodType.METHOD
                    )
                )
            )
            builder.onGenerate(cls, cls.methods.first())
        }
        assertCode(
            """
            |inline fun setPrivateFrom(other: OtherClass): Unit {
            |    return TestLib_TestClass_set_private_from(ptr, other.ptr)
            |}
            |""".trimMargin(),
            builder.toString()
        )
    }

    @Test
    fun testMethod_returnWrapped() {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestLib::TestClass",
                methods = listOf(
                    WrappedMethod(
                        "setPrivateFrom",
                        WrappedTypeReference.VOID,
                        listOf(
                            WrappedArgument("other", WrappedTypeReference("TestLib::OtherClass")),
                        ),
                        false,
                        MethodType.METHOD
                    )
                )
            )
            builder.onGenerate(cls, cls.methods.first())
        }
        assertCode(
            """
            |inline fun setPrivateFrom(other: OtherClass): Unit {
            |    return TestLib_TestClass_set_private_from(ptr, other.ptr)
            |}
            |""".trimMargin(),
            builder.toString()
        )
    }

    @Test
    fun testTemplateNaming() {
        writer.generate(testDir, listOf(WrappedClass("std::vector<std::string>::iterator")))
        testDir.listFiles().forEach {
            println("Found ${it.path}")
        }
        val output = File(testDir, "iterator__string.kt")
        assertTrue(output.exists())
        assertCode(
            """
            |package std.vector
            |
            |import kotlin.Pair
            |import kotlinx.cinterop.COpaquePointer
            |import kotlinx.cinterop.MemScope
            |
            |// BEGIN KRAPPER GEN for std::vector<std::string>::iterator
            |
            |value class iterator__string private constructor(val source: Pair<COpaquePointer, MemScope>) {
            |    val ptr: COpaquePointer
            |        inline get() {
            |            return source.first
            |        }
            |
            |    val memScope: MemScope
            |        inline get() {
            |            return source.second
            |        }
            |
            |    companion object {
            |    }
            |}
            |
            |// END KRAPPER GEN for std::vector<std::string>::iterator
            |""".trimMargin(),
            output.readText()
        )
    }
}
