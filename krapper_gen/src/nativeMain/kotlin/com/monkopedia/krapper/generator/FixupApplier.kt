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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.AddToChild
import com.monkopedia.krapper.AddUniquePtrGet
import com.monkopedia.krapper.Fixup
import com.monkopedia.krapper.IndexedService
import com.monkopedia.krapper.NoChange
import com.monkopedia.krapper.RemoveChild
import com.monkopedia.krapper.RemoveMethodByCName
import com.monkopedia.krapper.ReplaceChild
import com.monkopedia.krapper.ScriptOriginOptionsFix
import com.monkopedia.krapper.StripConstFromReturnType
import com.monkopedia.krapper.addMapping
import com.monkopedia.krapper.generator.model.kotlinType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.REINT_CAST
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.METHOD
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedArgument
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.COPY_CONSTRUCTOR
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.VOIDP
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCType

/**
 * Translate the declarative [Fixup] directives into the existing krapper_gen
 * mapping pipeline (addMapping). One [apply] call registers mappings for all
 * supplied fixups; the registered mappings fire during the normal writeTo()
 * flow on whichever resolved elements match.
 *
 * Each fixup type maps to one mapping registration. The bodies are lifted
 * verbatim from the v1 hardcoded v8-specific mappings in App.kt (now removed)
 * and the v1 DSL mappings in example/kotlin_project/build.gradle.kts — same
 * semantics, just expressed as a narrow data API instead of arbitrary
 * closures.
 *
 * Implementation note — every fixup uses [addMapping] (not [addTypedMapping])
 * with a string-shaped filter and runtime type-checks in the handler. The
 * typed-mapping path has an inlining-related issue where a prior fixup's
 * ReplaceChild on the same element causes a later typed mapping to receive
 * the parent class (childIndex becomes -1 after removal) and the unchecked
 * cast fails at the handler boundary. The untyped [addMapping] returns a
 * MapRequest with an explicit child accessor, so we can detect that case
 * and no-op safely.
 */
internal object FixupApplier {

    suspend fun apply(indexService: IndexedService, fixups: List<Fixup>) {
        for (fixup in fixups) {
            when (fixup) {
                is RemoveMethodByCName -> registerRemoveMethod(indexService, fixup.uniqueCName)

                is StripConstFromReturnType ->
                    registerStripConstReturnType(indexService, fixup.matchPrefix)

                ScriptOriginOptionsFix -> registerScriptOriginOptionsFix(indexService)

                AddUniquePtrGet -> registerAddUniquePtrGet(indexService)
            }
        }
    }

    private suspend fun registerRemoveMethod(indexService: IndexedService, cName: String) {
        // No selector for uniqueCName, so match every ResolvedMethod and
        // discriminate in the handler. Cheap enough for the v8-scale fixup
        // list (single-digit count).
        indexService.addMapping(
            filter = { thiz isType ResolvedMethod },
            handler = { request ->
                val element = request.child as? ResolvedMethod ?: return@addMapping noChange()
                if (element.uniqueCName != cName) return@addMapping noChange()
                Log.i("Removing $element (fixup: removeMethod $cName)")
                listOf(RemoveChild)
            }
        )
    }

    private suspend fun registerStripConstReturnType(
        indexService: IndexedService,
        matchPrefix: String
    ) {
        val constMatch = "const $matchPrefix"
        indexService.addMapping(
            filter = { (thiz isType ResolvedMethod) and (methodReturnType startsWith constMatch) },
            handler = { request ->
                val element = request.child as? ResolvedMethod ?: return@addMapping noChange()
                Log.i(
                    "Stripping const from return type of $element " +
                        "(fixup: stripConstFromReturnType $matchPrefix)"
                )
                listOf(
                    ReplaceChild(
                        element.copy(
                            returnType = element.returnType.copy(
                                typeString = element.returnType.typeString.removePrefix("const ")
                            )
                        )
                    )
                )
            }
        )
    }

    private suspend fun registerScriptOriginOptionsFix(indexService: IndexedService) {
        indexService.addMapping(
            filter = {
                (thiz isType ResolvedMethod) and
                    parent(qualified eq "v8::ScriptOrigin") and
                    (methodName eq "options")
            },
            handler = { request ->
                val element = request.child as? ResolvedMethod ?: return@addMapping noChange()
                Log.i(
                    "Applying scriptOriginOptionsFix to $element " +
                        "(strip const, trim trailing *, switch to COPY_CONSTRUCTOR)"
                )
                listOf(
                    ReplaceChild(
                        element.copy(
                            returnStyle = COPY_CONSTRUCTOR,
                            returnType = element.returnType.copy(
                                typeString = element.returnType.typeString
                                    .removePrefix("const ")
                                    .trimEnd('*')
                            )
                        )
                    )
                )
            }
        )
    }

    private suspend fun registerAddUniquePtrGet(indexService: IndexedService) {
        indexService.addMapping(
            filter = {
                (thiz isType ResolvedClass) and
                    (qualified startsWith "std::unique_ptr") and
                    (className eq "unique_ptr")
            },
            handler = { request ->
                val parent = request.child as? ResolvedClass ?: return@addMapping noChange()
                val wrappedType = WrappedType(
                    parent.type.typeString
                        .replace("std::unique_ptr<", "")
                        .removeSuffix(">")
                )
                val thisPtrType = parent.type.copy(
                    typeString = parent.type.type + "*",
                    cType = ResolvedCType("void*", false)
                )
                listOf(
                    AddToChild(
                        ResolvedMethod(
                            name = "get",
                            returnType = parent.type.copy(
                                typeString = wrappedType.toString(),
                                kotlinType = toResolvedKotlinType(wrappedType.kotlinType),
                                cType = toResolvedCType(wrappedType.cType)
                            ),
                            methodType = METHOD,
                            uniqueCName = "_custom_unique_ptr_get_${
                                parent.type.typeString
                                    .replace("<", "_")
                                    .replace(">", "_")
                                    .replace("::", "_")
                            }",
                            operator = null,
                            args = listOf(
                                ResolvedArgument(
                                    "thiz",
                                    thisPtrType,
                                    thisPtrType,
                                    "",
                                    REINT_CAST,
                                    needsDereference = true,
                                    hasDefault = false
                                )
                            ),
                            returnStyle = VOIDP,
                            argCastNeedsPointer = false,
                            qualified = parent.type.typeString
                        ).also {
                            Log.i("Adding $it to $parent (fixup: addUniquePtrGet)")
                        }
                    )
                )
            }
        )
    }

    private fun noChange() = listOf(NoChange)
}
