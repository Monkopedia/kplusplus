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

import com.monkopedia.krapper.generator.DropPhase
import com.monkopedia.krapper.generator.Utils.printerrln
import com.monkopedia.krapper.generator.dropLedger
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedField
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod

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

    override fun onGenerateClassFailed(cls: ResolvedClass, t: Throwable) = throw t
}

/**
 * Skip-not-crash for code generation: log the failure, and ledger the element as a GENERATE
 * drop so `failOnDrop` sees it (#253). A class-level failure is reported back by
 * [CodeGeneratorBase.generate], so the Kotlin writer can discard the half-written class.
 */
object LogPolicy : CodeGenerationPolicy {
    override fun onGenerateModuleFailed(
        moduleName: String,
        headers: List<String>,
        classes: List<ResolvedElement>,
        t: Throwable
    ) = drop(moduleName, t)

    override fun onGenerateMethodFailed(cls: ResolvedClass?, method: ResolvedMethod, t: Throwable) =
        drop("${cls?.type ?: method.qualified}::${method.name}", t)

    override fun onGenerateFieldFailed(cls: ResolvedClass, field: ResolvedField, t: Throwable) =
        drop("${cls.type}::${field.name}", t)

    override fun onGenerateClassFailed(cls: ResolvedClass, t: Throwable) =
        drop(cls.type.toString(), t)

    private fun drop(symbol: String, t: Throwable) {
        printerrln("Error while generating $symbol")
        printerrln(t.stackTraceToString())
        dropLedger.record(symbol, "Code generation failed: ${t.message}", DropPhase.GENERATE)
    }
}
