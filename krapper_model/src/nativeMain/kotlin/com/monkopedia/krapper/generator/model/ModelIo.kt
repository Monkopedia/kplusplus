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

import com.monkopedia.krapper.generator.model.type.WrappedEnumConstant
import com.monkopedia.krapper.generator.model.type.WrappedEnumType
import com.monkopedia.krapper.generator.model.type.WrappedFunctionPointer
import com.monkopedia.krapper.generator.model.type.WrappedModifiedType
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import com.monkopedia.krapper.generator.model.type.WrappedTypedefRef
import com.monkopedia.krapper.generator.model.type.WrappedTypename
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// FULL-FIDELITY round-trip serialization of the parse-output model (#45 brick 1) — the
// `--frontend=cpp` handoff format. Unlike the sibling [SerializedElement] (a ONE-WAY,
// lossy-by-design comparable projection for the golden tree-diff), this DTO mirror
// carries EVERY field resolution and codegen consume (the ModelResolution.kt
// when-dispatch is the consumption map), and [ModelIo.decodeFromString] rebuilds a
// functionally identical WrappedTU from the JSON.
//
// Design decisions:
//  - A parallel DTO mirror rather than @Serializable on the model classes themselves:
//    the element classes carry a private constructor children list on the abstract
//    base, parent back-links, non-constructor mutable state (method flags, metadata,
//    template merge counters) and identity caches — a mirror keeps the model untouched
//    (flag-off behavior provably unchanged) and doubles as the explicit, reviewable
//    schema of the handoff format.
//  - Parent back-links are NOT serialized: [toModel] rebuilds them by reconstructing
//    bottom-up through addAllChildren (which re-parents every child), so the cyclic
//    reference never reaches the encoder.
//  - Polymorphism: one sealed NodeDto hierarchy covering BOTH element and type kinds —
//    WrappedType IS-A WrappedElement and types appear as children (e.g. under template
//    params), so the children list must be able to carry either.
//  - IDENTITY IS PART OF THE MODEL: the in-memory parse output is a DAG, not a tree —
//    one element instance can sit under two parents (a free function under both its
//    namespace and a class), interned/memoized types are shared by every use site, and
//    resolution MUTATES elements in place (metadata flags, synthetic sizeOf/alignOf
//    children, const-overload dedup), so two aliases observing one instance is
//    semantically different from two equal copies. A naive value-tree encode duplicated
//    shared nodes and the featuregen oracle caught it as codegen drift. Encoding is
//    therefore DAG-aware: the first occurrence of an identity-shared node is wrapped in
//    [DefDto] (assigning it an id) and every later occurrence collapses to a [RefDto]
//    back-reference; decode rebuilds the exact aliasing structure. Encode and decode
//    walk the graph in the same canonical order ([modelFields] then children), so a
//    definition is always materialized before its back-references resolve.
@Serializable
sealed class NodeDto {
    abstract val children: List<NodeDto>
}

@Serializable
@SerialName("tu")
data class TuDto(override val children: List<NodeDto> = emptyList()) : NodeDto()

@Serializable
@SerialName("ns")
data class NamespaceDto(val namespace: String, override val children: List<NodeDto> = emptyList()) :
    NodeDto()

@Serializable
@SerialName("cls")
data class ClassDto(
    val name: String,
    val isAbstract: Boolean = false,
    val specifiedType: TypeDto? = null,
    val metadata: ClassMetadata = ClassMetadata(),
    override val children: List<NodeDto> = emptyList()
) : NodeDto()

@Serializable
@SerialName("tmpl")
data class TemplateDto(
    val name: String,
    val metadata: ClassMetadata = ClassMetadata(),
    // The template-param merge cursor (WrappedTemplate.addChild): restored verbatim so
    // any post-load param add merges exactly as it would have on the parsed instance.
    val templateArgCounter: Int = 0,
    override val children: List<NodeDto> = emptyList()
) : NodeDto()

@Serializable
@SerialName("tparam")
data class TemplateParamDto(
    val name: String,
    val usr: String,
    // Same-name params merged from sibling declarations — held OUTSIDE the children
    // list (WrappedTemplateParam.merge), so they need their own slot.
    val otherParams: List<NodeDto> = emptyList(),
    override val children: List<NodeDto> = emptyList()
) : NodeDto()

@Serializable
@SerialName("typedef")
data class TypedefDto(
    val name: String,
    val targetType: TypeDto,
    override val children: List<NodeDto> = emptyList()
) : NodeDto()

@Serializable
@SerialName("base")
data class BaseDto(
    val type: TypeDto? = null,
    val isPublic: Boolean = true,
    val isVirtualBase: Boolean = false,
    override val children: List<NodeDto> = emptyList()
) : NodeDto()

@Serializable
@SerialName("method")
data class MethodDto(
    val name: String,
    val returnType: TypeDto,
    val methodType: MethodType = MethodType.METHOD,
    val isVirtual: Boolean = false,
    val isConst: Boolean = false,
    val isDefaulted: Boolean = false,
    val returnsPairSecond: Boolean = false,
    val returnViaMemberCall: String? = null,
    val rangeElementType: String? = null,
    override val children: List<NodeDto> = emptyList()
) : NodeDto()

@Serializable
@SerialName("ctor")
data class ConstructorDto(
    val name: String,
    val returnType: TypeDto,
    val isCopyConstructor: Boolean = false,
    val isDefaultConstructor: Boolean = false,
    val allocationStyle: AllocationStyle = AllocationStyle.DIRECT,
    val isVirtual: Boolean = false,
    val isConst: Boolean = false,
    override val children: List<NodeDto> = emptyList()
) : NodeDto()

@Serializable
@SerialName("dtor")
data class DestructorDto(
    val name: String,
    val returnType: TypeDto,
    val isVirtual: Boolean = false,
    override val children: List<NodeDto> = emptyList()
) : NodeDto()

@Serializable
@SerialName("arg")
data class ArgumentDto(
    val name: String,
    val type: TypeDto,
    val usr: String = "",
    val hasDefault: Boolean = false,
    val defaultValue: String? = null,
    override val children: List<NodeDto> = emptyList()
) : NodeDto()

@Serializable
@SerialName("field")
data class FieldDto(
    val name: String,
    val type: TypeDto,
    override val children: List<NodeDto> = emptyList()
) : NodeDto()

// TypeDto is the subset valid where the model declares a WrappedType-typed slot. DefDto
// and RefDto are members so an identity-shared TYPE can be defined/back-referenced in a
// type position (decode checks the definition really is a type when consumed as one).
@Serializable
sealed class TypeDto : NodeDto()

@Serializable
@SerialName("def")
data class DefDto(
    val id: Int,
    val node: NodeDto,
    override val children: List<NodeDto> = emptyList()
) : TypeDto()

@Serializable
@SerialName("ref")
data class RefDto(val id: Int, override val children: List<NodeDto> = emptyList()) : TypeDto()

@Serializable
@SerialName("t.ref")
data class TypeRefDto(val name: String, override val children: List<NodeDto> = emptyList()) :
    TypeDto()

@Serializable
@SerialName("t.mod")
data class ModifiedTypeDto(
    val baseType: TypeDto,
    val modifier: String,
    override val children: List<NodeDto> = emptyList()
) : TypeDto()

@Serializable
@SerialName("t.pre")
data class PrefixedTypeDto(
    val baseType: TypeDto,
    val modifier: String,
    override val children: List<NodeDto> = emptyList()
) : TypeDto()

@Serializable
@SerialName("t.tmpl")
data class TemplateTypeDto(
    val baseType: TypeDto,
    val templateArgs: List<TypeDto> = emptyList(),
    override val children: List<NodeDto> = emptyList()
) : TypeDto()

@Serializable
@SerialName("t.tref")
data class TemplateRefDto(val target: String, override val children: List<NodeDto> = emptyList()) :
    TypeDto()

@Serializable
@SerialName("t.typedef")
data class TypedefRefDto(val usr: String, override val children: List<NodeDto> = emptyList()) :
    TypeDto()

@Serializable
@SerialName("t.typename")
data class TypenameDto(val target: String, override val children: List<NodeDto> = emptyList()) :
    TypeDto()

@Serializable
@SerialName("t.enum")
data class EnumTypeDto(
    val cppName: String,
    val underlying: TypeDto,
    val constants: List<WrappedEnumConstant> = emptyList(),
    override val children: List<NodeDto> = emptyList()
) : TypeDto()

@Serializable
@SerialName("t.fnptr")
data class FunctionPointerDto(
    val cName: String,
    val returnType: TypeDto,
    val argTypes: List<TypeDto> = emptyList(),
    // Only carried when it differs from cName (a namespace-qualified typedef).
    val cppName: String? = null,
    override val children: List<NodeDto> = emptyList()
) : TypeDto()

/** Identity (===) wrapper so HashSet/HashMap can key model nodes by instance, not value
 * (several model classes are value-equal data classes). hashCode delegates to the
 * element's own hashCode — identity hash for regular classes, a STABLE value hash for
 * the data classes (their hash only covers immutable constructor vals), so distinct
 * equal-value instances simply co-bucket while remaining distinct keys. */
class ElementIdentity(val element: WrappedElement) {
    override fun equals(other: Any?): Boolean =
        other is ElementIdentity && other.element === element

    override fun hashCode(): Int = element.hashCode()
}

/** The model's non-child object edges (WrappedType-valued fields + template-param merge
 * captures), in the CANONICAL traversal order shared by encode, decode and
 * [forEachIdentityPair]: fields first (matching decode, which constructs an element —
 * fields included — before decoding its children), then children. */
private fun WrappedElement.modelFields(): List<WrappedElement> = when (this) {
    is WrappedClass -> listOfNotNull(specifiedType)

    is WrappedTemplateParam -> otherParams

    is WrappedTypedef -> listOf(targetType)

    is WrappedBase -> listOfNotNull(type)

    // Covers WrappedConstructor/WrappedDestructor too.
    is WrappedMethod -> listOf(returnType)

    is WrappedArgument -> listOf(type)

    is WrappedField -> listOf(type)

    is WrappedModifiedType -> listOf(baseType)

    is WrappedPrefixedType -> listOf(baseType)

    is WrappedTemplateType -> listOf(baseType) + templateArgs

    is WrappedEnumType -> listOf(underlying)

    is WrappedFunctionPointer -> listOf(returnType) + argTypes

    else -> emptyList()
}

/** Pass 1 of encode: every node reachable through more than one edge (an alias). */
private fun collectShared(root: WrappedElement): Set<ElementIdentity> {
    val seen = HashSet<ElementIdentity>()
    val shared = HashSet<ElementIdentity>()

    fun visit(element: WrappedElement) {
        if (!seen.add(ElementIdentity(element))) {
            shared.add(ElementIdentity(element))
            return
        }
        element.modelFields().forEach(::visit)
        element.children.forEach(::visit)
    }
    visit(root)
    return shared
}

private class ModelEncoder(private val shared: Set<ElementIdentity>) {
    private val ids = HashMap<ElementIdentity, Int>()

    fun element(element: WrappedElement): NodeDto {
        val key = ElementIdentity(element)
        if (key in shared) {
            ids[key]?.let { return RefDto(it) }
            // Assign the id BEFORE recursing so a (pathological) cycle degrades to a
            // back-reference instead of hanging the encoder.
            val id = ids.size
            ids[key] = id
            return DefDto(id, raw(element))
        }
        return raw(element)
    }

    fun type(type: WrappedType): TypeDto = element(type) as TypeDto

    // NOTE: every branch encodes [modelFields] (constructor-argument order) before
    // kids() — the canonical traversal order decode and the identity zip share.
    private fun raw(element: WrappedElement): NodeDto {
        val kids = { element.children.map { element(it) } }
        return when (element) {
            is WrappedType -> rawType(element, kids)

            is WrappedTU -> TuDto(kids())

            is WrappedNamespace -> NamespaceDto(element.namespace, kids())

            is WrappedClass -> ClassDto(
                element.name,
                element.isAbstract,
                element.specifiedType?.let(::type),
                element.metadata.copy(),
                kids()
            )

            is WrappedTemplate ->
                TemplateDto(
                    element.name,
                    element.metadata.copy(),
                    element.templateArgCounter,
                    kids()
                )

            is WrappedTemplateParam ->
                TemplateParamDto(
                    element.name,
                    element.usr,
                    element.otherParams.map(::element),
                    kids()
                )

            is WrappedTypedef -> TypedefDto(element.name, type(element.targetType), kids())

            is WrappedBase ->
                BaseDto(element.type?.let(::type), element.isPublic, element.isVirtualBase, kids())

            // Order matters: WrappedConstructor/WrappedDestructor IS-A WrappedMethod.
            is WrappedConstructor -> ConstructorDto(
                element.name,
                type(element.returnType),
                element.isCopyConstructor,
                element.isDefaultConstructor,
                element.allocationStyle,
                element.isVirtual,
                element.isConst,
                kids()
            )

            is WrappedDestructor ->
                DestructorDto(element.name, type(element.returnType), element.isVirtual, kids())

            is WrappedMethod -> MethodDto(
                element.name,
                type(element.returnType),
                element.methodType,
                element.isVirtual,
                element.isConst,
                element.isDefaulted,
                element.returnsPairSecond,
                element.returnViaMemberCall,
                element.rangeElementType,
                kids()
            )

            is WrappedArgument -> ArgumentDto(
                element.name,
                type(element.type),
                element.usr,
                element.hasDefault,
                element.defaultValue,
                kids()
            )

            is WrappedField -> FieldDto(element.name, type(element.type), kids())

            // Fail loudly: a silently-dropped kind would surface as inexplicable codegen
            // drift; an unknown kind means this schema needs a new DTO.
            else -> throw IllegalArgumentException(
                "No DTO mapping for element kind ${element::class}"
            )
        }
    }

    private fun rawType(t: WrappedType, kids: () -> List<NodeDto>): TypeDto = when (t) {
        is WrappedTypeReference -> TypeRefDto(t.name, kids())

        is WrappedModifiedType -> ModifiedTypeDto(type(t.baseType), t.modifier, kids())

        is WrappedPrefixedType -> PrefixedTypeDto(type(t.baseType), t.modifier, kids())

        is WrappedTemplateType ->
            TemplateTypeDto(type(t.baseType), t.templateArgs.map(::type), kids())

        is WrappedTemplateRef -> TemplateRefDto(t.target, kids())

        is WrappedTypedefRef -> TypedefRefDto(t.usr, kids())

        is WrappedTypename -> TypenameDto(t.target, kids())

        is WrappedEnumType -> EnumTypeDto(t.cppName, type(t.underlying), t.constants, kids())

        is WrappedFunctionPointer -> FunctionPointerDto(
            t.cName,
            type(t.returnType),
            t.argTypes.map(::type),
            t.cppName.takeIf { it != t.cName },
            kids()
        )

        else -> throw IllegalArgumentException("No DTO mapping for type kind ${t::class}")
    }
}

private class ModelDecoder {
    private val byId = HashMap<Int, WrappedElement>()

    fun element(dto: NodeDto): WrappedElement {
        when (dto) {
            is RefDto -> return byId[dto.id]
                ?: throw IllegalArgumentException("Back-reference to undefined node ${dto.id}")

            is DefDto -> return element(dto.node).also { byId[dto.id] = it }

            else -> {}
        }
        val element = when (dto) {
            is TypeDto -> rawType(dto)

            is TuDto -> WrappedTU()

            is NamespaceDto -> WrappedNamespace(dto.namespace)

            is ClassDto -> WrappedClass(dto.name, dto.isAbstract, dto.specifiedType?.let(::type))
                .also { it.metadata = dto.metadata.copy() }

            is TemplateDto -> WrappedTemplate(dto.name).also {
                it.metadata = dto.metadata.copy()
                it.templateArgCounter = dto.templateArgCounter
            }

            is TemplateParamDto -> WrappedTemplateParam(dto.name, dto.usr).also { param ->
                param.otherParams.addAll(
                    dto.otherParams.map { it.toParam() }
                )
            }

            is TypedefDto -> WrappedTypedef(dto.name, type(dto.targetType))

            is BaseDto -> WrappedBase(dto.type?.let(::type), dto.isPublic, dto.isVirtualBase)

            is ConstructorDto -> WrappedConstructor(
                dto.name,
                type(dto.returnType),
                dto.isCopyConstructor,
                dto.isDefaultConstructor,
                dto.allocationStyle
            ).also {
                it.isVirtual = dto.isVirtual
                it.isConst = dto.isConst
            }

            is DestructorDto -> WrappedDestructor(dto.name, type(dto.returnType)).also {
                it.isVirtual = dto.isVirtual
            }

            is MethodDto -> WrappedMethod(dto.name, type(dto.returnType), dto.methodType).also {
                it.isVirtual = dto.isVirtual
                it.isConst = dto.isConst
                it.isDefaulted = dto.isDefaulted
                it.returnsPairSecond = dto.returnsPairSecond
                it.returnViaMemberCall = dto.returnViaMemberCall
                it.rangeElementType = dto.rangeElementType
            }

            is ArgumentDto ->
                WrappedArgument(dto.name, type(dto.type), dto.usr, dto.hasDefault, dto.defaultValue)

            is FieldDto -> WrappedField(dto.name, type(dto.type))
        }
        // Rebuild the tree (and thereby every parent back-link) bottom-up. NOTE:
        // base-class addAllChildren appends + re-parents WITHOUT the subclass addChild
        // hooks, so the template param-merge logic doesn't re-fire on already-merged
        // children, and re-adding an identity-shared node under its second parent
        // re-points its parent the same way the original parse did (last add wins).
        element.addAllChildren(dto.children.map { element(it) })
        return element
    }

    fun type(dto: TypeDto): WrappedType = element(dto) as? WrappedType
        ?: throw IllegalArgumentException("Expected a type at ${dto::class}")

    private fun NodeDto.toParam(): WrappedTemplateParam = element(this) as? WrappedTemplateParam
        ?: throw IllegalArgumentException("Expected a template param at ${this::class}")

    private fun rawType(dto: TypeDto): WrappedType = when (dto) {
        is TypeRefDto -> WrappedTypeReference(dto.name)

        is ModifiedTypeDto -> WrappedModifiedType(type(dto.baseType), dto.modifier)

        is PrefixedTypeDto -> WrappedPrefixedType(type(dto.baseType), dto.modifier)

        is TemplateTypeDto ->
            WrappedTemplateType(type(dto.baseType), dto.templateArgs.map(::type))

        is TemplateRefDto -> WrappedTemplateRef(dto.target)

        is TypedefRefDto -> WrappedTypedefRef(dto.usr)

        is TypenameDto -> WrappedTypename(dto.target)

        is EnumTypeDto -> WrappedEnumType(dto.cppName, type(dto.underlying), dto.constants)

        is FunctionPointerDto -> WrappedFunctionPointer(
            dto.cName,
            type(dto.returnType),
            dto.argTypes.map(::type),
            dto.cppName ?: dto.cName
        )

        is DefDto, is RefDto -> error("def/ref handled in element()")
    }
}

/**
 * Walk [original] and [restored] (its round-tripped copy) in lockstep and emit every
 * (original, restored) instance pair exactly once — identity-shared originals pair with
 * their single restored alias. The oracle harness uses this to point the cross-parse
 * cursor->element memo at the restored instances, so post-round-trip parses keep
 * accumulating state on the SAME objects resolution sees, exactly like a non-flag run.
 */
fun forEachIdentityPair(
    original: WrappedElement,
    restored: WrappedElement,
    onPair: (WrappedElement, WrappedElement) -> Unit
) {
    val seen = HashSet<ElementIdentity>()

    fun visit(old: WrappedElement, new: WrappedElement) {
        if (!seen.add(ElementIdentity(old))) return
        onPair(old, new)
        val oldFields = old.modelFields()
        val newFields = new.modelFields()
        require(oldFields.size == newFields.size && old.children.size == new.children.size) {
            "Round-trip shape mismatch at $old"
        }
        oldFields.zip(newFields).forEach { (o, n) -> visit(o, n) }
        old.children.zip(new.children).forEach { (o, n) -> visit(o, n) }
    }
    visit(original, restored)
}

object ModelIo {
    // Discriminator can't be the default "type" — several DTOs carry a real `type`
    // property. Defaults omitted to keep the (large, whole-TU) payload compact.
    val json = Json {
        classDiscriminator = "k"
        encodeDefaults = false
    }

    fun encodeToString(tu: WrappedTU): String =
        json.encodeToString(NodeDto.serializer(), ModelEncoder(collectShared(tu)).element(tu))

    fun decodeFromString(text: String): WrappedTU =
        ModelDecoder().element(json.decodeFromString(NodeDto.serializer(), text)) as WrappedTU
}
