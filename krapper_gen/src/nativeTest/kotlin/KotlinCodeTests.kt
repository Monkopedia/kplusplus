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
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedDestructor
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.type.WrappedType
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
            |value class EmptyClass public constructor(val source: Pair<COpaquePointer, MemScope>) {
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
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "Constructable",
                            WrappedType("TestLib::Constructable"),
                            false,
                            MethodType.CONSTRUCTOR
                        )
                    )
                )
            }
            builder.onGenerate(cls, cls.children.first() as WrappedMethod)
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
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedConstructor(
                            "Constructable",
                            WrappedType("TestLib::Constructable"),
                        ),
                        WrappedDestructor(
                            "Constructable",
                            WrappedType("TestLib::Constructable"),
                        )
                    )
                )
            }
            builder.onGenerate(cls, cls.children.first() as WrappedMethod)
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
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "setSome",
                            WrappedType.VOID,
                            false,
                            MethodType.METHOD
                        ).also {
                            it.addAllChildren(
                                listOf(
                                    WrappedArgument("a", WrappedType("int")),
                                    WrappedArgument("b", WrappedType("long")),
                                    WrappedArgument("c", WrappedType("long long"))
                                ),
                            )
                        }
                    )
                )
            }
            builder.onGenerate(cls, cls.children.first() as WrappedMethod)
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
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "setPointers",
                            WrappedType.VOID,
                            false,
                            MethodType.METHOD
                        ).also { m ->
                            m.addAllChildren(
                                listOf(
                                    WrappedArgument("a", WrappedType("int*")),
                                    WrappedArgument("b", WrappedType("long*")),
                                    WrappedArgument("c", WrappedType("long long*"))
                                ),
                            )
                        }
                    )
                )
            }
            builder.onGenerate(cls, cls.children.first() as WrappedMethod)
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
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "sum",
                            WrappedType("long"),
                            false,
                            MethodType.METHOD
                        )
                    )
                )
            }
            builder.onGenerate(cls, cls.children.first() as WrappedMethod)
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
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "at",
                            WrappedType("std::string"),
                            false,
                            MethodType.METHOD
                        ).also {
                            it.addAllChildren(
                                listOf(
                                    WrappedArgument("pos", WrappedType("size_t")),
                                ),
                            )
                        }
                    )
                )
            }
            builder.onGenerate(cls, cls.children.first() as WrappedMethod)
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
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "setPrivateFrom",
                            WrappedType.VOID,
                            false,
                            MethodType.METHOD
                        ).also {
                            it.addAllChildren(
                                listOf(
                                    WrappedArgument(
                                        "other",
                                        WrappedType("TestLib::OtherClass")
                                    ),
                                ),
                            )
                        }
                    )
                )
            }
            builder.onGenerate(cls, cls.children.first() as WrappedMethod)
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
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "setPrivateFrom",
                            WrappedType.VOID,

                            false,
                            MethodType.METHOD
                        ).also {
                            it.addAllChildren(
                                listOf(
                                    WrappedArgument(
                                        "other",
                                        WrappedType("TestLib::OtherClass")
                                    ),
                                ),
                            )
                        }
                    )
                )
            }
            builder.onGenerate(cls, cls.children.first() as WrappedMethod)
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
            |value class iterator__string public constructor(val source: Pair<COpaquePointer, MemScope>) {
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
