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
package com.monkopedia.krapper.generator.codegen

import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedField
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod

/** What [generateBindings] emitted: the `.h` and `.cc` text, and the model all writers used. */
class GeneratedBindings(val header: String, val wrapper: String, val emitted: List<ResolvedElement>)

/**
 * Emit [classes] through every writer — the C interop header, the C++ wrapper, and the Kotlin
 * sources into [srcDir] — all-or-nothing per element (#260).
 *
 * The outputs only work together: the Kotlin calls what the header declares and the wrapper
 * defines, and `binding-index.json` advertises the Kotlin classes. So when [policy] swallows a
 * writer's failure (`ErrorPolicy.LOG`), the failed element is removed from the model and every
 * writer runs again without it, until a pass fails on nothing. A failed class leaves every
 * output; a failed method or field leaves every output while its class keeps the rest. Nothing
 * a writer half-wrote survives, and each failure is ledgered once. A clean model takes one pass.
 *
 * Each pass only removes elements, so this terminates. A later pass fails on something new
 * only because of an earlier removal (a derived class whose base was dropped), and that is
 * dropped and ledgered in turn.
 */
fun generateBindings(
    moduleName: String,
    headers: List<String>,
    classes: List<ResolvedElement>,
    cppFile: File,
    kotlinPkg: String,
    srcDir: File,
    policy: CodeGenerationPolicy
): GeneratedBindings {
    var model = classes
    while (true) {
        val failures = FailureCollector(policy)
        val header = CppCodeBuilder().also {
            HeaderWriter(it, failures).generate(moduleName, headers, model)
        }.toString()
        // qualifyFunctionPointers: the .cc wrapper names namespace-scoped function-pointer
        // typedefs with their fully-qualified spelling so they resolve in C++ (the .h header
        // keeps the unqualified extern-"C" form).
        val wrapper = CppCodeBuilder(qualifyFunctionPointers = true).also {
            CppWriter(cppFile, it, failures).generate(moduleName, headers, model)
        }.toString()
        KotlinWriter(kotlinPkg, failures).generate(srcDir, model)
        if (failures.failed.isEmpty()) return GeneratedBindings(header, wrapper, model)
        model = model.without(failures.failed)
    }
}

/**
 * Remembers every element a writer failed on, and reports each to [policy] the first time
 * only, so an element two writers fail on is ledgered once.
 */
private class FailureCollector(private val policy: CodeGenerationPolicy) : CodeGenerationPolicy {
    val failed = mutableListOf<ResolvedElement>()

    private fun isNew(element: ResolvedElement): Boolean =
        failed.none { it === element }.also { if (it) failed += element }

    override fun onGenerateModuleFailed(
        moduleName: String,
        headers: List<String>,
        classes: List<ResolvedElement>,
        t: Throwable
    ) = policy.onGenerateModuleFailed(moduleName, headers, classes, t)

    override fun onGenerateMethodFailed(cls: ResolvedClass?, method: ResolvedMethod, t: Throwable) {
        if (isNew(method)) policy.onGenerateMethodFailed(cls, method, t)
    }

    override fun onGenerateFieldFailed(cls: ResolvedClass, field: ResolvedField, t: Throwable) {
        if (isNew(field)) policy.onGenerateFieldFailed(cls, field, t)
    }

    override fun onGenerateClassFailed(cls: ResolvedClass, t: Throwable) {
        if (isNew(cls)) policy.onGenerateClassFailed(cls, t)
    }
}

/**
 * This model minus [failed] (matched by identity: a data-class field equals any same-named,
 * same-typed field of another class). A class that loses a member is cloned rather than
 * edited, so the caller's model is untouched.
 */
private fun List<ResolvedElement>.without(failed: List<ResolvedElement>): List<ResolvedElement> {
    fun ResolvedElement.isFailed() = failed.any { it === this }
    return filterNot { it.isFailed() }.map { element ->
        val kept = element.children.filterNot { it.isFailed() }
        if (kept.size == element.children.size) {
            element
        } else {
            element.cloneWithoutChildren().also { clone ->
                clone.addAllChildren(kept.map { it.clone() })
            }
        }
    }
}
