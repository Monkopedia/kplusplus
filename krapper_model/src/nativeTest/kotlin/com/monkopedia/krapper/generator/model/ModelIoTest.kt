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
package com.monkopedia.krapper.generator.model

import com.monkopedia.krapper.generator.model.type.WrappedEnumConstant
import com.monkopedia.krapper.generator.model.type.WrappedEnumType
import com.monkopedia.krapper.generator.model.type.WrappedFunctionPointer
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedTypedefRef
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.STACK
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Fast structural signal for the ModelIo round-trip: a hand-built model exercising every
 * node kind survives encode -> decode with state, tree shape, and parent back-links
 * intact. The REAL gate is the featuregen oracle (`KRAPPER_ROUNDTRIP_MODEL=1` sync must
 * be byte-identical), which covers the full parse surface; this test pins the basics and
 * fails fast on schema regressions.
 */
class ModelIoTest {

    private fun buildModel(): WrappedTU {
        val tu = WrappedTU()
        val ns = WrappedNamespace("demo")
        tu.addChild(ns)

        val cls = WrappedClass("Thing", isAbstract = true)
        cls.metadata.hasConstructor = true
        cls.metadata.hasDeletedCopyConstructor = true
        ns.addChild(cls)
        cls.addChild(WrappedBase(WrappedType("demo::Base"), isPublic = false, isVirtualBase = true))
        cls.addChild(WrappedField("count", WrappedType("int")))

        val method = WrappedMethod(
            "lookup",
            WrappedType.referenceTo(WrappedType.const(WrappedType("std::string"))),
            MethodType.METHOD
        )
        method.isVirtual = true
        method.isConst = true
        method.isDefaulted = true
        method.returnsPairSecond = true
        method.returnViaMemberCall = ".str()"
        method.rangeElementType = "clang::Decl"
        method.addChild(
            WrappedArgument(
                "key",
                WrappedType("int"),
                "c:@key",
                hasDefault = true,
                defaultValue = "10"
            )
        )
        method.addChild(
            WrappedArgument(
                "cb",
                WrappedFunctionPointer(
                    "Transform",
                    WrappedType("int"),
                    listOf(WrappedType("int")),
                    "demo::Transform"
                )
            )
        )
        method.addChild(
            WrappedArgument(
                "items",
                WrappedTemplateType(
                    WrappedType("std::vector"),
                    listOf(WrappedType.pointerTo(WrappedType("int")))
                )
            )
        )
        method.addChild(
            WrappedArgument(
                "color",
                WrappedEnumType(
                    "demo::Color",
                    WrappedType("int"),
                    listOf(WrappedEnumConstant("RED", 1))
                )
            )
        )
        cls.addChild(method)

        val ctor = WrappedConstructor(
            "Thing",
            WrappedType.VOID,
            isCopyConstructor = true,
            isDefaultConstructor = false,
            allocationStyle = STACK
        )
        cls.addChild(ctor)
        cls.addChild(WrappedDestructor("~Thing", WrappedType.VOID).also { it.isVirtual = true })

        val template = WrappedTemplate("Box")
        template.metadata.hasDefaultConstructor = true
        ns.addChild(template)
        val param = WrappedTemplateParam("T", "c:@T")
        param.addChild(WrappedType("int"))
        param.addChild(WrappedTemplateRef("T"))
        template.addChild(param)
        // A merged same-name param lands in otherParams (outside children) — reset the
        // merge cursor the way mapAll does when a template's declaration re-appears.
        template.templateArgCounter = 0
        template.addChild(WrappedTemplateParam("T", "c:@T2"))

        ns.addChild(WrappedTypedef("id_t", WrappedTypedefRef("c:@id")))
        return tu
    }

    @Test
    fun roundTrip_isStable() {
        val tu = buildModel()
        val json = ModelIo.encodeToString(tu)
        val restored = ModelIo.decodeFromString(json)
        // Re-encoding the restored model must reproduce the identical JSON — every field
        // either survived or was never captured, and this catches the former.
        assertEquals(json, ModelIo.encodeToString(restored))
    }

    @Test
    fun roundTrip_restoresStateAndParents() {
        val restored = ModelIo.decodeFromString(ModelIo.encodeToString(buildModel()))

        val ns = assertIs<WrappedNamespace>(restored.children.single())
        assertSame(restored, ns.parent)
        val cls = assertIs<WrappedClass>(ns.children[0])
        assertSame(ns, cls.parent)
        assertEquals("demo::Thing", cls.qualified)
        assertTrue(cls.isAbstract)
        assertTrue(cls.metadata.hasConstructor)
        assertTrue(cls.metadata.hasDeletedCopyConstructor)

        val base = assertIs<WrappedBase>(cls.children[0])
        assertTrue(base.isVirtualBase)
        assertEquals(false, base.isPublic)
        assertEquals("demo::Base", base.type.toString())

        val method = cls.children.filterIsInstance<WrappedMethod>()
            .first { it.methodType == MethodType.METHOD }
        assertSame(cls, method.parent)
        assertEquals("const std::string&", method.returnType.toString())
        assertTrue(method.isVirtual)
        assertTrue(method.isConst)
        assertTrue(method.isDefaulted)
        assertTrue(method.returnsPairSecond)
        assertEquals(".str()", method.returnViaMemberCall)
        assertEquals("clang::Decl", method.rangeElementType)
        val args = method.args
        assertEquals(listOf("key", "cb", "items", "color"), args.map { it.name })
        assertSame(method, args[0].parent)
        assertEquals("c:@key", args[0].usr)
        assertTrue(args[0].hasDefault)
        assertEquals("10", args[0].defaultValue)
        val fnPtr = assertIs<WrappedFunctionPointer>(args[1].type)
        assertEquals("demo::Transform", fnPtr.cppName)
        val vec = assertIs<WrappedTemplateType>(args[2].type)
        assertEquals("std::vector<int*>", vec.toString())
        val enum = assertIs<WrappedEnumType>(args[3].type)
        assertEquals(listOf(WrappedEnumConstant("RED", 1)), enum.constants)
        // Identity preservation: both `int` uses were ONE interned instance in the
        // original model, so the DAG encode (def/ref) must restore them as ONE instance.
        assertSame(args[0].type, enum.underlying)

        val ctor = cls.children.filterIsInstance<WrappedConstructor>().single()
        assertTrue(ctor.isCopyConstructor)
        assertEquals(false, ctor.isDefaultConstructor)
        assertEquals(STACK, ctor.allocationStyle)
        assertTrue(cls.children.filterIsInstance<WrappedDestructor>().single().isVirtual)

        val template = assertIs<WrappedTemplate>(ns.children[1])
        assertTrue(template.metadata.hasDefaultConstructor)
        val param = template.templateArgs.single()
        assertEquals("c:@T", param.usr)
        // The merged sibling param survives in otherParams, and the default-type children
        // (a WrappedType + a WrappedTemplateRef) survive as children of the param.
        assertEquals(listOf("c:@T2"), param.otherParams.map { it.usr })
        assertEquals("int<template<T>>", param.defaultType.toString())

        val typedef = assertIs<WrappedTypedef>(ns.children[2])
        assertEquals("unresolved_typedef(c:@id)", typedef.targetType.toString())
    }
}
