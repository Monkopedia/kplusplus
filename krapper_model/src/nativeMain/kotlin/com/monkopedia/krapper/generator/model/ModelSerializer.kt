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

import kotlinx.serialization.Serializable

// THE CANONICAL JSON projection of the parse-output model (#44 bricks 2+7). The
// WrappedElement hierarchy itself is NOT @Serializable (parent back-links + non-data
// classes), so the cpp front-end serializes this one-way structural DTO: kind + per-kind
// payload + children, types spelled via WrappedType.toString(). It was the comparison key
// for the Phase C golden tree-diff against krapper_gen's libclang-C reducer; that reducer
// (and the diff) were removed in the self-hosting flip (B5, #88) / cleanup (#92). The DTO
// remains :cppfrontend's --golden-emit projection and lives here in :krapper_model.
@Serializable
data class SerializedElement(
    val kind: String,
    val name: String? = null,
    val type: String? = null,
    val returnType: String? = null,
    val methodType: String? = null,
    val isConst: Boolean? = null,
    val isVirtual: Boolean? = null,
    val isAbstract: Boolean? = null,
    // Base-specifier payload (WrappedBase).
    val isPublic: Boolean? = null,
    val isVirtualBase: Boolean? = null,
    // Constructor payload (WrappedConstructor).
    val isCopyConstructor: Boolean? = null,
    val isDefaultConstructor: Boolean? = null,
    // Default-argument payload (WrappedArgument, brick 6): the resolution contract is the
    // pair — hasDefault feeds the trailing-defaulted-omit shortcut (isOmittableDefault),
    // defaultValue feeds KotlinWriter's Kotlin-default mapping.
    val hasDefault: Boolean? = null,
    val defaultValue: String? = null,
    // The names of the ClassMetadata flags set on a class (parse-time records about
    // FILTERED members — deleted/non-public ctors, hidden new/delete, const fields).
    val metadata: List<String>? = null,
    // Identity field — "cpp:<canonical Decl id>" from the C++-AST front-end vs a libclang
    // USR string from the libclang front-end; masked in the golden tree-diff (the two
    // front-ends never agree on the literal identity string, only on the structure).
    val usr: String? = null,
    val children: List<SerializedElement> = emptyList()
)

fun WrappedElement.serialized(): SerializedElement {
    val kids = children.map { it.serialized() }
    return when (this) {
        is WrappedTU -> SerializedElement("tu", children = kids)

        is WrappedClass -> SerializedElement(
            "class",
            name = name,
            isAbstract = isAbstract,
            metadata = metadata.flags().takeIf { it.isNotEmpty() },
            children = kids
        )

        // Brick 5: template declarations + their params, and typedef elements.
        is WrappedTemplate -> SerializedElement(
            "template",
            name = name,
            metadata = metadata.flags().takeIf { it.isNotEmpty() },
            children = kids
        )

        is WrappedTemplateParam -> SerializedElement(
            "templateParam",
            name = name,
            usr = usr,
            children = kids
        )

        is WrappedTypedef -> SerializedElement(
            "typedef",
            name = name,
            type = targetType.toString(),
            children = kids
        )

        is WrappedNamespace -> SerializedElement("namespace", name = namespace, children = kids)

        is WrappedBase -> SerializedElement(
            "base",
            type = type?.toString(),
            isPublic = isPublic,
            isVirtualBase = isVirtualBase,
            children = kids
        )

        // Before WrappedMethod: WrappedArgument/WrappedField are sibling element kinds, a
        // WrappedConstructor IS-A WrappedMethod with extra flags, and a WrappedDestructor
        // IS-A WrappedMethod distinguished by methodType alone.
        is WrappedConstructor -> SerializedElement(
            "method",
            name = name,
            returnType = returnType.toString(),
            methodType = methodType.name,
            isCopyConstructor = isCopyConstructor,
            isDefaultConstructor = isDefaultConstructor,
            children = kids
        )

        is WrappedArgument -> SerializedElement(
            "arg",
            name = name,
            type = type.toString(),
            usr = usr,
            hasDefault = hasDefault.takeIf { it },
            defaultValue = defaultValue,
            children = kids
        )

        is WrappedField -> SerializedElement(
            "field",
            name = name,
            type = type.toString(),
            children = kids
        )

        is WrappedMethod -> SerializedElement(
            "method",
            name = name,
            returnType = returnType.toString(),
            methodType = methodType.name,
            isConst = isConst,
            isVirtual = isVirtual,
            children = kids
        )

        else -> SerializedElement(this::class.simpleName ?: "element", children = kids)
    }
}

private fun ClassMetadata.flags(): List<String> = buildList {
    if (hasHiddenNew) add("hasHiddenNew")
    if (hasHiddenDelete) add("hasHiddenDelete")
    if (hasConstructor) add("hasConstructor")
    if (hasPrivateConstField) add("hasPrivateConstField")
    if (hasDefaultConstructor) add("hasDefaultConstructor")
    if (hasCopyConstructor) add("hasCopyConstructor")
    if (hasDeletedCopyConstructor) add("hasDeletedCopyConstructor")
    if (hasDeletedCopyAssignment) add("hasDeletedCopyAssignment")
    if (hasDeletedDefaultConstructor) add("hasDeletedDefaultConstructor")
}
