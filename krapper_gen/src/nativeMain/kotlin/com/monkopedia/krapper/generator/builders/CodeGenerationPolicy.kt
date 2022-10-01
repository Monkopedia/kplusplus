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

import com.monkopedia.krapper.generator.Utils.printerrln
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.ResolvedField
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod

interface CodeGenerationPolicy {
    fun onGenerateModuleFailed(
        moduleName: String,
        headers: List<String>,
        classes: List<ResolvedElement>,
        t: Throwable
    )

    fun onGenerateMethodFailed(cls: ResolvedClass?, method: ResolvedMethod, t: Throwable)
    fun onGenerateFieldFailed(cls: ResolvedClass, field: ResolvedField, t: Throwable)
    fun onGenerateClassFailed(cls: ResolvedClass, t: Throwable)
}

object ThrowPolicy : CodeGenerationPolicy {
    override fun onGenerateModuleFailed(
        moduleName: String,
        headers: List<String>,
        classes: List<ResolvedElement>,
        t: Throwable
    ) = throw t

    override fun onGenerateMethodFailed(
        moduleName: ResolvedClass?,
        method: ResolvedMethod,
        t: Throwable
    ) = throw t

    override fun onGenerateFieldFailed(cls: ResolvedClass, field: ResolvedField, t: Throwable) =
        throw t

    override fun onGenerateClassFailed(cls: ResolvedClass, t: Throwable) =
        throw t
}

object LogPolicy : CodeGenerationPolicy {
    override fun onGenerateModuleFailed(
        moduleName: String,
        headers: List<String>,
        classes: List<ResolvedElement>,
        t: Throwable
    ) {
        printerrln("Error while generating module $moduleName ($headers $classes)")
        printerrln(t.stackTraceToString())
    }

    override fun onGenerateMethodFailed(cls: ResolvedClass?, method: ResolvedMethod, t: Throwable) {
        printerrln("Error while generating method $cls#$method")
        printerrln(t.stackTraceToString())
    }

    override fun onGenerateFieldFailed(cls: ResolvedClass, field: ResolvedField, t: Throwable) {
        printerrln("Error while generating field $cls#$field")
        printerrln(t.stackTraceToString())
    }

    override fun onGenerateClassFailed(cls: ResolvedClass, t: Throwable) {
        printerrln("Error while generating class $cls#$cls")
        printerrln(t.stackTraceToString())
    }
}
