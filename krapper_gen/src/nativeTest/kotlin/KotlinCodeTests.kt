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

import com.monkopedia.krapper.generator.ReferencePolicy.INCLUDE_MISSING
import com.monkopedia.krapper.generator.builders.CodeStringBuilder
import com.monkopedia.krapper.generator.builders.KotlinCodeBuilder
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedDestructor
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class KotlinCodeTests {
    val writer = KotlinWriter("test.pkg")
    val testDir = File("/tmp/testDir")

    @BeforeTest
    fun setup() {
        if (testDir.exists()) {
            testDir.rmR()
        }
    }

    @Test
    fun testEmptyFile(): Unit = runBlocking {
        val cls = WrappedClass("EmptyClass").apply {
            addChild(WrappedType("std::string")) // avoid being empty/removed.
            metadata.hasConstructor = true
        }
        val tu = WrappedTU().also {
            it.addChild(
                WrappedNamespace("TestLib").also {
                    it.addChild(cls)
                }
            )
        }
        val ctx = ResolveContext.Empty
            .withClasses(listOf(cls))
            .copy(resolver = ParsedResolver(tu))
            .withPolicy(INCLUDE_MISSING)
        ctx.resolve(cls.type)
        val rcls =
            ctx.tracker.resolvedClasses[cls.type.toString()] ?: error("Resolve failed for $cls")
        writer.generate(testDir, listOf(rcls))
        val output = File(testDir, "testLib_EmptyClass.kt")
        assertTrue(output.exists())
        assertCode(
            """
            |package testLib
            |
            |import kotlin.Int
            |import kotlinx.cinterop.COpaquePointer
            |import kotlinx.cinterop.MemScope
            |import kotlinx.cinterop.interpretCPointer
            |import test.pkg.TestLib_EmptyClass_size_of
            |
            |// BEGIN KRAPPER GEN for TestLib::EmptyClass
            |
            |class EmptyClass(
            |    val ptr: COpaquePointer,
            |    val memScope: MemScope,
            |) {
            |    companion object {
            |        val size: Int
            |            inline get() {
            |                return TestLib_EmptyClass_size_of()
            |            }
            |
            |        fun MemScope.EmptyClass_Holder(): EmptyClass {
            |            val memory: COpaquePointer = (interpretCPointer(alloc(size, size).rawPtr) ?: error("Allocation failed"))
            |            return EmptyClass(memory, this)
            |        }
            |    }
            |}
            |
            |// END KRAPPER GEN for TestLib::EmptyClass
            |""".trimMargin(),
            output.readText()
        )
    }

    @Test
    fun testConstructor_noDestructor(): Unit = runBlocking {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "Constructable",
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedConstructor(
                            "Constructable",
                            WrappedType("TestLib::Constructable"),
                            false,
                            false,
                        ),
                        WrappedNamespace("A")
                    )
                )
            }
            val tu = WrappedTU().also {
                it.addChild(
                    WrappedNamespace("TestLib").also {
                        it.addChild(cls)
                    }
                )
            }
            val ctx = ResolveContext.Empty
                .withClasses(listOf(cls))
                .copy(resolver = ParsedResolver(tu))
                .withPolicy(INCLUDE_MISSING)
            ctx.resolve(cls.type)
            val rcls =
                ctx.tracker.resolvedClasses[cls.type.toString()] ?: error("Resolve failed for $cls")
            builder.onGenerate(
                rcls, rcls.children.first() as ResolvedMethod,
                object : LocalVar {
                    override val name: String
                        get() = "size"

                    override fun build(builder: CodeStringBuilder) {
                        builder.append("size")
                    }
                }
            )
        }
        assertCode(
            """
            |fun MemScope.Constructable(): Constructable {
            |    val memory: COpaquePointer = (interpretCPointer(alloc(size, size).rawPtr) ?: error("Allocation failed"))
            |    val obj: COpaquePointer = (TestLib_Constructable_new(memory) ?: error("Creation failed"))
            |    return Constructable(obj, this)
            |}
            |""".trimMargin(),
            builder.toString()
        )
    }

    @Test
    fun testConstructor_destructor(): Unit = runBlocking {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "Constructable",
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedConstructor(
                            "Constructable",
                            WrappedType("TestLib::Constructable"),
                            false,
                            false
                        ),
                        WrappedDestructor(
                            "Constructable",
                            WrappedType("TestLib::Constructable"),
                        )
                    )
                )
            }
            val tu = WrappedTU().also {
                it.addChild(
                    WrappedNamespace("TestLib").also {
                        it.addChild(cls)
                    }
                )
            }
            val ctx = ResolveContext.Empty
                .withClasses(listOf(cls))
                .copy(resolver = ParsedResolver(tu))
                .withPolicy(INCLUDE_MISSING)
            ctx.resolve(cls.type)
            val rcls =
                ctx.tracker.resolvedClasses[cls.type.toString()] ?: error("Resolve failed for $cls")
            builder.onGenerate(
                rcls, rcls.children.first() as ResolvedMethod,
                object : LocalVar {
                    override val name: String
                        get() = "size"

                    override fun build(builder: CodeStringBuilder) {
                        builder.append("size")
                    }
                }
            )
        }
        assertCode(
            """
            |fun MemScope.Constructable(): Constructable {
            |    val memory: COpaquePointer = (interpretCPointer(alloc(size, size).rawPtr) ?: error("Allocation failed"))
            |    val obj: COpaquePointer = (TestLib_Constructable_new(memory) ?: error("Creation failed"))
            |    defer {
            |        TestLib_Constructable_dispose(obj)
            |    }
            |    return Constructable(obj, this)
            |}
            |""".trimMargin(),
            builder.toString()
        )
    }

    @Test
    fun testMethod_voidReturn(): Unit = runBlocking {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestClass",
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "setSome",
                            WrappedType.VOID,
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
            val tu = WrappedTU().also {
                it.addChild(
                    WrappedNamespace("TestLib").also {
                        it.addChild(cls)
                    }
                )
            }
            val ctx = ResolveContext.Empty
                .withClasses(listOf(cls))
                .copy(resolver = ParsedResolver(tu))
                .withPolicy(INCLUDE_MISSING)
            ctx.resolve(cls.type)
            val rcls =
                ctx.tracker.resolvedClasses[cls.type.toString()] ?: error("Resolve failed for $cls")
            builder.onGenerate(rcls, rcls.children.first() as ResolvedMethod)
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
    fun testMethod_pointerInput(): Unit = runBlocking {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestClass",
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "setPointers",
                            WrappedType.VOID,
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
            val tu = WrappedTU().also {
                it.addChild(
                    WrappedNamespace("TestLib").also {
                        it.addChild(cls)
                    }
                )
            }
            val ctx = ResolveContext.Empty
                .withClasses(listOf(cls))
                .copy(resolver = ParsedResolver(tu))
                .withPolicy(INCLUDE_MISSING)
            ctx.resolve(cls.type)
            val rcls =
                ctx.tracker.resolvedClasses[cls.type.toString()] ?: error("Resolve failed for $cls")
            builder.onGenerate(rcls, rcls.children.first() as ResolvedMethod)
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
    fun testMethod_returnValue(): Unit = runBlocking {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestClass",
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "sum",
                            WrappedType("long"),
                            MethodType.METHOD
                        )
                    )
                )
            }
            val tu = WrappedTU().also {
                it.addChild(
                    WrappedNamespace("TestLib").also {
                        it.addChild(cls)
                    }
                )
            }
            val ctx = ResolveContext.Empty
                .withClasses(listOf(cls))
                .copy(resolver = ParsedResolver(tu))
                .withPolicy(INCLUDE_MISSING)
            ctx.resolve(cls.type)
            val rcls =
                ctx.tracker.resolvedClasses[cls.type.toString()] ?: error("Resolve failed for $cls")
            builder.onGenerate(rcls, rcls.children.first() as ResolvedMethod)
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
    fun testMethod_returnString(): Unit = runBlocking {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "vector<std::string>",
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "at",
                            WrappedType("std::string"),
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
            val tu = WrappedTU().also {
                it.addChild(
                    WrappedNamespace("std").also {
                        it.addChild(cls)
                    }
                )
            }
            val ctx = ResolveContext.Empty
                .withClasses(listOf(cls))
                .copy(resolver = ParsedResolver(tu))
                .withPolicy(INCLUDE_MISSING)
            ctx.resolve(cls.type)
            val rcls =
                ctx.tracker.resolvedClasses[cls.type.toString()] ?: error("Resolve failed for $cls")
            builder.onGenerate(rcls, rcls.children.first() as ResolvedMethod)
        }
        assertCode(
            """
            |inline fun at(pos: Size_t): String? {
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
    fun testMethod_inputWrapped(): Unit = runBlocking {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestClass",
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "setPrivateFrom",
                            WrappedType.VOID,
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
            val tu = WrappedTU().also {
                it.addChild(
                    WrappedNamespace("TestLib").also {
                        it.addChild(cls)
                        it.addChild(
                            WrappedClass("OtherClass").apply {
                                addChild(WrappedType("std::string")) // avoid being empty/removed.
                                metadata.hasConstructor = true
                            }
                        )
                    }
                )
            }
            val ctx = ResolveContext.Empty
                .withClasses(listOf(cls))
                .copy(resolver = ParsedResolver(tu))
                .withPolicy(INCLUDE_MISSING)
            ctx.resolve(cls.type)
            val rcls =
                ctx.tracker.resolvedClasses[cls.type.toString()] ?: error("Resolve failed for $cls")
            builder.onGenerate(rcls, rcls.children.first() as ResolvedMethod)
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
    fun testMethod_returnWrapped(): Unit = runBlocking {
        val builder = KotlinCodeBuilder()
        with(writer) {
            val cls = WrappedClass(
                "TestClass",
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedMethod(
                            "setPrivateFrom",
                            WrappedType.VOID,

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
            val tu = WrappedTU().also {
                it.addChild(
                    WrappedNamespace("TestLib").also {
                        it.addChild(cls)
                        it.addChild(
                            WrappedClass("OtherClass").apply {
                                addChild(WrappedType("std::string")) // avoid being empty/removed.
                                metadata.hasConstructor = true
                            }
                        )
                    }
                )
            }
            val ctx = ResolveContext.Empty
                .withClasses(listOf(cls))
                .copy(resolver = ParsedResolver(tu))
                .withPolicy(INCLUDE_MISSING)
            ctx.resolve(cls.type)
            val rcls =
                ctx.tracker.resolvedClasses[cls.type.toString()] ?: error("Resolve failed for $cls")
            builder.onGenerate(rcls, rcls.children.first() as ResolvedMethod)
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
    fun testTemplateNaming(): Unit = runBlocking {
        val cls = WrappedClass("vector<std::string>::iterator").apply {
            addChild(WrappedType("std::string")) // Avoid being empty/removed
            metadata.hasConstructor = true
        }
        val tu = WrappedTU().also {
            it.addChild(
                WrappedNamespace("std").also {
                    it.addChild(cls)
                }
            )
        }
        val ctx = ResolveContext.Empty
            .withClasses(listOf(cls))
            .copy(resolver = ParsedResolver(tu))
            .withPolicy(INCLUDE_MISSING)
        ctx.resolve(cls.type)
        val rcls =
            ctx.tracker.resolvedClasses[cls.type.toString()] ?: error("Resolve failed for $cls")
        writer.generate(testDir, listOf(rcls))
        testDir.listFiles().forEach {
            println("Found ${it.path}")
        }
        val output = File(testDir, "std_vector_Iterator__String.kt")
        assertTrue(output.exists())
        assertCode(
            """
            |package std.vector
            |
            |import kotlin.Int
            |import kotlinx.cinterop.COpaquePointer
            |import kotlinx.cinterop.MemScope
            |import kotlinx.cinterop.interpretCPointer
            |import test.pkg.std_vector_std_string_iterator_size_of
            |
            |// BEGIN KRAPPER GEN for std::vector<std::string>::iterator
            |
            |class Iterator__String(
            |    val ptr: COpaquePointer,
            |    val memScope: MemScope,
            |) {
            |    companion object {
            |        val size: Int
            |            inline get() {
            |                return std_vector_std_string_iterator_size_of()
            |            }
            |
            |        fun MemScope.Iterator__String_Holder(): Iterator__String {
            |            val memory: COpaquePointer = (interpretCPointer(alloc(size, size).rawPtr) ?: error("Allocation failed"))
            |            return Iterator__String(memory, this)
            |        }
            |    }
            |}
            |
            |// END KRAPPER GEN for std::vector<std::string>::iterator
            |""".trimMargin(),
            output.readText()
        )
    }
}
