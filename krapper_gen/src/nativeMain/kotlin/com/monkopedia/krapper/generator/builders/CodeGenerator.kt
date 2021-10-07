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
package com.monkopedia.krapper.generator.builders

import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod

abstract class CodeGenerator<T : CodeBuilder<*>>(
    val codeBuilder: T,
    codeGenerationPolicy: CodeGenerationPolicy
) : CodeGeneratorBase<T>(codeGenerationPolicy) {
    open fun generate(moduleName: String, headers: List<String>, classes: List<WrappedClass>) {
        try {
            codeBuilder.onGenerate(moduleName, headers) {
                for (cls in classes) {
                    generate(cls)
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

    abstract fun T.onGenerate(cls: WrappedClass, handleChildren: T.() -> Unit)

    abstract fun T.onGenerate(cls: WrappedClass, method: WrappedMethod)
    abstract fun T.onGenerate(cls: WrappedClass, field: WrappedField)

    fun T.generate(cls: WrappedClass) {
        try {
            onGenerate(cls) {
                onGenerateMethods(cls)
                for (field in cls.fields) {
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

    protected open fun T.onGenerateMethods(cls: WrappedClass) {
        for (method in cls.methods) {
            try {
                onGenerate(cls, method)
            } catch (t: Throwable) {
                codeGenerationPolicy.onGenerateMethodFailed(cls, method, t)
            }
        }
    }
}
