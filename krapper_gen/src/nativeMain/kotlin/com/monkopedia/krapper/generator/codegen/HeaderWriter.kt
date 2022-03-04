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
package com.monkopedia.krapper.generator.codegen

import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.CodeGenerator
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.builders.ExternCClose
import com.monkopedia.krapper.generator.builders.ExternCOpen
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.builders.appendLine
import com.monkopedia.krapper.generator.builders.comment
import com.monkopedia.krapper.generator.builders.define
import com.monkopedia.krapper.generator.builders.functionDeclaration
import com.monkopedia.krapper.generator.builders.ifdef
import com.monkopedia.krapper.generator.builders.ifndef
import com.monkopedia.krapper.generator.builders.includeSys
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedField
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod

class HeaderWriter(
    private val nameHandler: NameHandler,
    codeBuilder: CppCodeBuilder,
    policy: CodeGenerationPolicy = ThrowPolicy
) : CodeGenerator<CppCodeBuilder>(codeBuilder, policy) {

    private var lookup: ClassLookup = ClassLookup(emptyList())

    override fun generate(moduleName: String, headers: List<String>, classes: List<ResolvedClass>) {
        lookup = ClassLookup(classes)
        super.generate(moduleName, headers, classes)
    }

    override fun CppCodeBuilder.onGenerate(
        cls: ResolvedClass,
        handleChildren: CppCodeBuilder.() -> Unit
    ) {
        comment("BEGIN KRAPPER GEN for ${cls.type}")
        appendLine()
        handleChildren()
        appendLine()
        comment("END KRAPPER GEN for ${cls.type}")
        appendLine()
        appendLine()
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

            appendLine()
            ifdef("__cplusplus") {
                +ExternCOpen
            }
            appendLine()

            this.handleChildren()

            appendLine()
            ifdef("__cplusplus") {
                +ExternCClose
            }
            appendLine()
        }
    }

    override fun CppCodeBuilder.onGenerate(cls: ResolvedClass, method: ResolvedMethod) {
        functionDeclaration {
            val type = cls.type
            generateMethodSignature(method)
            addArgs(lookup, type, method)
        }
        appendLine()
    }

    override fun CppCodeBuilder.onGenerate(cls: ResolvedClass, field: ResolvedField) {
        functionDeclaration {
            val type = cls.type
            generateFieldGet(field)
        }
        appendLine()
        functionDeclaration {
            val type = cls.type
            generateFieldSet(field)
        }
        appendLine()
    }
}
