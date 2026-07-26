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
package com.monkopedia.krapper.generator.codegen

import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.CodeGenerator
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.builders.ExternCClose
import com.monkopedia.krapper.generator.builders.ExternCOpen
import com.monkopedia.krapper.generator.builders.Raw
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.builders.appendLine
import com.monkopedia.krapper.generator.builders.comment
import com.monkopedia.krapper.generator.builders.define
import com.monkopedia.krapper.generator.builders.functionDeclaration
import com.monkopedia.krapper.generator.builders.ifdef
import com.monkopedia.krapper.generator.builders.ifndef
import com.monkopedia.krapper.generator.builders.includeSys
import com.monkopedia.krapper.generator.builders.type
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedField
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedFunctionPointer

class HeaderWriter(codeBuilder: CppCodeBuilder, policy: CodeGenerationPolicy = ThrowPolicy) :
    CodeGenerator<CppCodeBuilder>(codeBuilder, policy) {

    private var lookup: ClassLookup = ClassLookup(emptyList())

    // C function-pointer typedefs used by any wrapped method, deduped by name. The
    // typedef name lives only in the user's C++ header (which this C interop header
    // does not include), so we re-declare each one in the header preamble.
    private var functionPointers: List<ResolvedFunctionPointer> = emptyList()

    override fun generate(
        moduleName: String,
        headers: List<String>,
        classes: List<ResolvedElement>
    ) {
        lookup = ClassLookup(classes.filterIsInstance<ResolvedClass>())
        functionPointers = collectFunctionPointers(classes)
        super.generate(moduleName, headers, classes)
    }

    private fun collectFunctionPointers(
        elements: List<ResolvedElement>
    ): List<ResolvedFunctionPointer> {
        val byName = linkedMapOf<String, ResolvedFunctionPointer>()
        fun consider(type: ResolvedCppType) {
            type.funcPointer?.let { if (it.cName !in byName) byName[it.cName] = it }
        }
        fun visit(element: ResolvedElement) {
            if (element is ResolvedMethod) {
                consider(element.returnType)
                element.args.forEach { consider(it.type) }
            }
            element.children.forEach { visit(it) }
        }
        elements.forEach { visit(it) }
        return byName.values.toList()
    }

    override fun CppCodeBuilder.onGenerate(
        cls: ResolvedClass,
        handleChildren: CppCodeBuilder.() -> Unit
    ) {
        comment("BEGIN KRAPPER GEN for ${cls.type}")
        appendLine()
        handleChildren()
        generateCastHelperDeclarations(cls)
        generateDownCastHelperDeclarations(cls)
        appendLine()
        comment("END KRAPPER GEN for ${cls.type}")
        appendLine()
        appendLine()
    }

    // Declarations matching the upcast helpers CppWriter defines (`void* D_as_B(void*)`),
    // so cinterop surfaces each one as a callable Kotlin external. The helper name is
    // derived identically in both writers (see CastTarget.cHelperName).
    private fun CppCodeBuilder.generateCastHelperDeclarations(cls: ResolvedClass) {
        for (target in castTargetsFor(cls) { lookup[it] }) {
            functionDeclaration {
                retType = type(VOID_PTR)
                name = target.cHelperName
                define("p", VOID_PTR)
            }
            appendLine()
        }
    }

    // Declarations matching the down-cast helpers CppWriter defines (`void* B_dyncast_D(void*)`),
    // so cinterop surfaces each one as a callable Kotlin external. The helper name is derived
    // identically in both writers (see DownCastTarget.cHelperName). The cast MECHANISM
    // (`llvm::dyn_cast` vs `dynamic_cast`) is purely a body concern, so the void*/void*
    // signature here is the same for both.
    private fun CppCodeBuilder.generateDownCastHelperDeclarations(cls: ResolvedClass) {
        for (target in downCastTargetsFor(cls, lookup.allClasses) { lookup[it] }) {
            functionDeclaration {
                retType = type(VOID_PTR)
                name = target.cHelperName
                define("p", VOID_PTR)
            }
            appendLine()
        }
    }

    override fun CppCodeBuilder.onGenerate(
        moduleName: String,
        headers: List<String>,
        handleChildren: CppCodeBuilder.() -> Unit
    ) {
        val splitCamelcase = moduleName.splitCamelcase()
        val headerLabel = "__" + splitCamelcase.joinToString("_") { it.uppercase() } + "__"
        ifndef(headerLabel) {
            define(headerLabel)

            appendLine()
            includeSys("stdlib.h")
            includeSys("stdint.h")
            includeSys("stdbool.h")
            includeSys("stddef.h") // size_t, ptrdiff_t, wchar_t

            appendLine()
            ifdef("__cplusplus") {
                +ExternCOpen
            }
            appendLine()

            // Re-declare C function-pointer typedefs (e.g. `typedef int (*IntTransform)(int);`)
            // so methods taking/returning them type-check here. The same identical typedef
            // also comes in via the user header in the .cc; an identical re-typedef is legal.
            for (funcPointer in functionPointers) {
                +Raw(funcPointer.typedefDeclaration)
                appendLine()
            }
            if (functionPointers.isNotEmpty()) {
                appendLine()
            }

            this.handleChildren()

            appendLine()
            ifdef("__cplusplus") {
                +ExternCClose
            }
            appendLine()
        }
    }

    override fun CppCodeBuilder.onGenerate(cls: ResolvedClass, method: ResolvedMethod) =
        onGenerate(method)

    override fun CppCodeBuilder.onGenerate(method: ResolvedMethod) {
        functionDeclaration {
            generateMethodSignature(method)
            addArgs(method)
        }
        appendLine()
    }

    override fun CppCodeBuilder.onGenerate(cls: ResolvedClass, field: ResolvedField) {
        functionDeclaration {
            generateFieldGet(field)
        }
        appendLine()
        // Mirror CppWriter: const/reference members get no `_set` wrapper (it would
        // not compile), so don't declare one either.
        if (!field.isConst) {
            functionDeclaration {
                generateFieldSet(field)
            }
            appendLine()
        }
    }
}
