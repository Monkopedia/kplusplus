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
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedBase
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import kotlinx.serialization.Serializable

// JSON projection of the parse-output model (#44 brick 2). The WrappedElement hierarchy
// itself is NOT @Serializable (parent back-links + non-data classes), so the front-end
// serializes a one-way structural DTO: kind + per-kind payload + children, types spelled
// via WrappedType.toString(). Phase C's tree-diff wants ONE canonical projection emitted by
// BOTH front-ends, so the durable home for this is :krapper_model — kept frontend-side for
// the scaffold to leave the model module untouched (finding recorded on #44).
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
    // The names of the ClassMetadata flags set on a class (parse-time records about
    // FILTERED members — deleted/non-public ctors, hidden new/delete, const fields).
    val metadata: List<String>? = null,
    // Identity field — "cpp:<canonical Decl id>" here vs a libclang USR string from the
    // libclang front-end; maskable in the Phase C tree-diff (see ModelBuilder.kt).
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
            metadata = metadataFlags().takeIf { it.isNotEmpty() },
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

private fun WrappedClass.metadataFlags(): List<String> = buildList {
    if (metadata.hasHiddenNew) add("hasHiddenNew")
    if (metadata.hasHiddenDelete) add("hasHiddenDelete")
    if (metadata.hasConstructor) add("hasConstructor")
    if (metadata.hasPrivateConstField) add("hasPrivateConstField")
    if (metadata.hasDefaultConstructor) add("hasDefaultConstructor")
    if (metadata.hasCopyConstructor) add("hasCopyConstructor")
    if (metadata.hasDeletedCopyConstructor) add("hasDeletedCopyConstructor")
    if (metadata.hasDeletedCopyAssignment) add("hasDeletedCopyAssignment")
    if (metadata.hasDeletedDefaultConstructor) add("hasDeletedDefaultConstructor")
}
