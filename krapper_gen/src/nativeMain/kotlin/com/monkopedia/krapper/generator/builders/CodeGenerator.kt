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
package com.monkopedia.krapper.generator.builders

import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.ResolvedField
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod

abstract class CodeGenerator<T : CodeBuilder<*>>(
    val codeBuilder: T,
    codeGenerationPolicy: CodeGenerationPolicy
) : CodeGeneratorBase<T>(codeGenerationPolicy) {
    open fun generate(moduleName: String, headers: List<String>, classes: List<ResolvedElement>) {
        try {
            codeBuilder.onGenerate(moduleName, headers) {
                for (cls in classes.filterIsInstance<ResolvedClass>()) {
                    generate(cls)
                }
                for (method in classes.filterIsInstance<ResolvedMethod>()) {
                    try {
                        onGenerate(method)
                    } catch (t: Throwable) {
                        codeGenerationPolicy.onGenerateMethodFailed(null, method, t)
                    }
                }
            }
        } catch (t: Throwable) {
            codeGenerationPolicy.onGenerateModuleFailed(moduleName, headers, classes, t)
        }
    }

    abstract fun T.onGenerate(
        moduleName: String,
        headers: List<String>,
        handleChildren: T.() -> Unit
    )
}

abstract class CodeGeneratorBase<T : CodeBuilder<*>>(
    val codeGenerationPolicy: CodeGenerationPolicy
) {

    abstract fun T.onGenerate(cls: ResolvedClass, handleChildren: T.() -> Unit)

    abstract fun T.onGenerate(cls: ResolvedClass, method: ResolvedMethod)
    abstract fun T.onGenerate(method: ResolvedMethod)
    abstract fun T.onGenerate(cls: ResolvedClass, field: ResolvedField)

    fun T.generate(cls: ResolvedClass) {
        try {
            onGenerate(cls) {
                onGenerateMethods(cls)
                for (field in cls.children.filterIsInstance<ResolvedField>()) {
                    try {
                        onGenerate(cls, field)
                    } catch (t: Throwable) {
                        codeGenerationPolicy.onGenerateFieldFailed(cls, field, t)
                    }
                }
            }
        } catch (t: Throwable) {
            codeGenerationPolicy.onGenerateClassFailed(cls, t)
        }
    }

    protected open fun T.onGenerateMethods(cls: ResolvedClass) {
        for (method in cls.children.filterIsInstance<ResolvedMethod>()) {
            try {
                onGenerate(cls, method)
            } catch (t: Throwable) {
                codeGenerationPolicy.onGenerateMethodFailed(cls, method, t)
            }
        }
    }
}
