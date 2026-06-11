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
//  - Type interning/identity: GenerationContext.internedTypes only ever caches plain
//    [WrappedTypeReference]s (WrappedType.invoke's other branches non-local-return past
//    the getOrPut), and WrappedTypeReference is a value-equal data class — so referential
//    identity of types is not load-bearing for equality and decode constructs fresh
//    instances without touching the intern cache.
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
    val otherParams: List<TemplateParamDto> = emptyList(),
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

@Serializable
sealed class TypeDto : NodeDto()

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

fun WrappedElement.toDto(): NodeDto {
    val kids = children.map { it.toDto() }
    return when (this) {
        is WrappedType -> toTypeDto(kids)

        is WrappedTU -> TuDto(kids)

        is WrappedNamespace -> NamespaceDto(namespace, kids)

        is WrappedClass -> ClassDto(name, isAbstract, specifiedType?.toDto(), metadata.copy(), kids)

        is WrappedTemplate -> TemplateDto(name, metadata.copy(), templateArgCounter, kids)

        is WrappedTemplateParam ->
            TemplateParamDto(name, usr, otherParams.map { it.toDto() as TemplateParamDto }, kids)

        is WrappedTypedef -> TypedefDto(name, targetType.toDto(), kids)

        is WrappedBase -> BaseDto(type?.toDto(), isPublic, isVirtualBase, kids)

        // Order matters: WrappedConstructor/WrappedDestructor IS-A WrappedMethod.
        is WrappedConstructor -> ConstructorDto(
            name,
            returnType.toDto(),
            isCopyConstructor,
            isDefaultConstructor,
            allocationStyle,
            isVirtual,
            isConst,
            kids
        )

        is WrappedDestructor -> DestructorDto(name, returnType.toDto(), isVirtual, kids)

        is WrappedMethod -> MethodDto(
            name,
            returnType.toDto(),
            methodType,
            isVirtual,
            isConst,
            returnsPairSecond,
            returnViaMemberCall,
            rangeElementType,
            kids
        )

        is WrappedArgument -> ArgumentDto(name, type.toDto(), usr, hasDefault, defaultValue, kids)

        is WrappedField -> FieldDto(name, type.toDto(), kids)

        // Fail loudly: a silently-dropped kind would surface as inexplicable codegen
        // drift; an unknown kind means this schema needs a new DTO.
        else -> throw IllegalArgumentException("No DTO mapping for element kind ${this::class}")
    }
}

fun WrappedType.toDto(): TypeDto = toTypeDto(children.map { it.toDto() })

private fun WrappedType.toTypeDto(kids: List<NodeDto>): TypeDto = when (this) {
    is WrappedTypeReference -> TypeRefDto(name, kids)

    is WrappedModifiedType -> ModifiedTypeDto(baseType.toDto(), modifier, kids)

    is WrappedPrefixedType -> PrefixedTypeDto(baseType.toDto(), modifier, kids)

    is WrappedTemplateType ->
        TemplateTypeDto(baseType.toDto(), templateArgs.map { it.toDto() }, kids)

    is WrappedTemplateRef -> TemplateRefDto(target, kids)

    is WrappedTypedefRef -> TypedefRefDto(usr, kids)

    is WrappedTypename -> TypenameDto(target, kids)

    is WrappedEnumType -> EnumTypeDto(cppName, underlying.toDto(), constants, kids)

    is WrappedFunctionPointer -> FunctionPointerDto(
        cName,
        returnType.toDto(),
        argTypes.map { it.toDto() },
        cppName.takeIf { it != cName },
        kids
    )

    else -> throw IllegalArgumentException("No DTO mapping for type kind ${this::class}")
}

fun NodeDto.toModel(): WrappedElement {
    val element = when (this) {
        is TypeDto -> toType()

        is TuDto -> WrappedTU()

        is NamespaceDto -> WrappedNamespace(namespace)

        is ClassDto -> WrappedClass(name, isAbstract, specifiedType?.toType()).also {
            it.metadata = metadata.copy()
        }

        is TemplateDto -> WrappedTemplate(name).also {
            it.metadata = metadata.copy()
            it.templateArgCounter = templateArgCounter
        }

        is TemplateParamDto -> WrappedTemplateParam(name, usr).also { param ->
            param.otherParams.addAll(otherParams.map { it.toModel() as WrappedTemplateParam })
        }

        is TypedefDto -> WrappedTypedef(name, targetType.toType())

        is BaseDto -> WrappedBase(type?.toType(), isPublic, isVirtualBase)

        is ConstructorDto -> WrappedConstructor(
            name,
            returnType.toType(),
            isCopyConstructor,
            isDefaultConstructor,
            allocationStyle
        ).also {
            it.isVirtual = isVirtual
            it.isConst = isConst
        }

        is DestructorDto -> WrappedDestructor(name, returnType.toType()).also {
            it.isVirtual = isVirtual
        }

        is MethodDto -> WrappedMethod(name, returnType.toType(), methodType).also {
            it.isVirtual = isVirtual
            it.isConst = isConst
            it.returnsPairSecond = returnsPairSecond
            it.returnViaMemberCall = returnViaMemberCall
            it.rangeElementType = rangeElementType
        }

        is ArgumentDto -> WrappedArgument(name, type.toType(), usr, hasDefault, defaultValue)

        is FieldDto -> WrappedField(name, type.toType())
    }
    // Rebuild the tree (and thereby every parent back-link) bottom-up. NOTE: base-class
    // addAllChildren appends + re-parents WITHOUT the subclass addChild hooks, so the
    // template param-merge logic doesn't re-fire on already-merged children.
    element.addAllChildren(children.map { it.toModel() })
    return element
}

fun TypeDto.toType(): WrappedType {
    val type = when (this) {
        is TypeRefDto -> WrappedTypeReference(name)

        is ModifiedTypeDto -> WrappedModifiedType(baseType.toType(), modifier)

        is PrefixedTypeDto -> WrappedPrefixedType(baseType.toType(), modifier)

        is TemplateTypeDto ->
            WrappedTemplateType(baseType.toType(), templateArgs.map { it.toType() })

        is TemplateRefDto -> WrappedTemplateRef(target)

        is TypedefRefDto -> WrappedTypedefRef(usr)

        is TypenameDto -> WrappedTypename(target)

        is EnumTypeDto -> WrappedEnumType(cppName, underlying.toType(), constants)

        is FunctionPointerDto -> WrappedFunctionPointer(
            cName,
            returnType.toType(),
            argTypes.map { it.toType() },
            cppName ?: cName
        )
    }
    type.addAllChildren(children.map { it.toModel() })
    return type
}

object ModelIo {
    // Discriminator can't be the default "type" — several DTOs carry a real `type`
    // property. Defaults omitted to keep the (large, whole-TU) payload compact.
    val json = Json {
        classDiscriminator = "k"
        encodeDefaults = false
    }

    fun encodeToString(tu: WrappedTU): String =
        json.encodeToString(NodeDto.serializer(), tu.toDto())

    fun decodeFromString(text: String): WrappedTU =
        json.decodeFromString(NodeDto.serializer(), text).toModel() as WrappedTU
}
