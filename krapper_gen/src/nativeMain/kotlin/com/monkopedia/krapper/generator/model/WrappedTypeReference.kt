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
package com.monkopedia.krapper.generator.model

import clang.CXType
import clang.CXTypeKind.CXType_Elaborated
import clang.CXTypeKind.CXType_Pointer
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.namedType
import com.monkopedia.krapper.generator.pointeeType
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import kotlinx.serialization.Serializable

private val NATIVE = listOf(
    "size_t",
    "uint16_t",
    "void",
    "bool",
    "char",
    "signed char",
    "unsigned char",
    "short",
    "signed short",
    "unsigned short",
    "int",
    "signed int",
    "unsigned int",
    "long",
    "signed long",
    "unsigned long",
    "long long",
    "signed long long",
    "unsigned long long",
    "float",
    "double",
)

private const val STRING = "std::string"

@ThreadLocal
private val existingTypes = mutableMapOf<String, WrappedTypeReference>()

sealed class WrappedType : WrappedElement() {
    abstract val cType: WrappedTypeReference

    override fun clone(): WrappedType {
        return this
    }

    abstract fun typedWith(
        resolverBuilder: ResolverBuilder,
        values: Map<WrappedTemplateType, WrappedTypeReference>
    ): WrappedTypeReference

    companion object {
        fun pointerTo(type: WrappedType): WrappedType {
            if (type is WrappedTypeReference) {
                return WrappedTypeReference.pointerTo(type)
            }
            return WrappedModifiedType(type, "*")
        }

        fun referenceTo(type: WrappedType): WrappedType {
            if (type is WrappedTypeReference) {
                return WrappedTypeReference.referenceTo(type)
            }
            return WrappedModifiedType(type, "&")
        }

        fun arrayOf(type: WrappedType): WrappedType {
            if (type is WrappedTypeReference) {
                return WrappedTypeReference.arrayOf(type)
            }
            return WrappedModifiedType(type, "[]")
        }
    }
}

fun WrappedTemplateType(
    type: CValue<CXType>,
    resolverBuilder: ResolverBuilder
): WrappedType {
    return type.useContents {
        when (kind) {
            CXType_Pointer -> WrappedTemplateType(type.pointeeType, resolverBuilder)
            CXType_Elaborated -> WrappedTemplateType(type.namedType, resolverBuilder)
//            CXType_Typedef ->
            // CXType_Enum ->
            else -> WrappedTypeReference(type, resolverBuilder)
        }
    }
}

class WrappedModifiedType(val baseType: WrappedType, val modifier: String) : WrappedType() {
    override val cType: WrappedTypeReference
        get() = when (modifier) {
            "*" -> WrappedTypeReference.pointerTo(baseType.cType)
            "&" -> WrappedTypeReference.referenceTo(baseType.cType)
            "[]" -> WrappedTypeReference.arrayOf(baseType.cType)
            else -> error("Don't know how to handle $modifier")
        }

    override fun typedWith(
        resolverBuilder: ResolverBuilder,
        values: Map<WrappedTemplateType, WrappedTypeReference>
    ): WrappedTypeReference {
        return WrappedTypeReference(
            "${
            baseType.typedWith(
                resolverBuilder,
                values
            ).name
            }$modifier"
        ).also {
            resolverBuilder.visit(it)
        }
    }
}

class WrappedTemplateType(val name: String) : WrappedType() {
    override val cType: WrappedTypeReference
        get() = error("Can't convert template $name")

    override fun typedWith(
        resolverBuilder: ResolverBuilder,
        values: Map<WrappedTemplateType, WrappedTypeReference>
    ): WrappedTypeReference {
        return values[this] ?: error("Missing type mapping for $name")
    }

    override fun toString(): String {
        return name
    }
}

class WrappedTemplatedReference(
    val fullyQualifiedTemplate: String,
    val templateArgs: List<WrappedType>
) : WrappedType() {
    override val cType: WrappedTypeReference
        get() = error("Can't convert template $fullyQualifiedTemplate")

    override fun typedWith(
        resolverBuilder: ResolverBuilder,
        values: Map<WrappedTemplateType, WrappedTypeReference>
    ): WrappedTypeReference {
        return WrappedTypeReference(fillName(values)).also {
            resolverBuilder.visit(it)
        }
    }

    private fun fillName(values: Map<WrappedTemplateType, WrappedTypeReference>): String {
        val templateArgs = templateArgs.toMutableList()
        return findQualifiers(fullyQualifiedTemplate).joinToString("::") {
            val section = fullyQualifiedTemplate.substring(it)
            val templateIndex = section.indexOf('<')
            if (templateIndex >= 0) {
                section.substring(
                    0,
                    templateIndex
                ) + "<" + findTemplates(section).joinToString(", ") {
                    values[templateArgs.removeFirst()]!!.name
                } + ">"
            } else section
        }.also {
            require(templateArgs.isEmpty()) {
                "Unsatisfied arguments $templateArgs in $fullyQualifiedTemplate"
            }
        }
    }
}

@Serializable
data class WrappedTypeReference private constructor(val name: String) : WrappedType() {
    val isPointerOrReference: Boolean
        get() = isPointer || isReference
    val isPointer: Boolean
        get() = name.endsWith("*")
    val isReference: Boolean
        get() = name.endsWith("&")
    val referenced: WrappedTypeReference
        get() {
            require(isReference) {
                "Can't get pointed for non-reference type"
            }
            return WrappedTypeReference(name.substring(0, name.length - 1).trim())
        }
    val pointed: WrappedTypeReference
        get() {
            require(isPointer) {
                "Can't get pointed for non-pointer type"
            }
            return WrappedTypeReference(name.substring(0, name.length - 1).trim())
        }
    val isArray: Boolean
        get() = name.endsWith("]")
    val arraySize: Int
        get() {
            require(isArray) {
                "Can't get array size of non-array"
            }
            val openIndex = name.indexOf("[")
            val closeIndex = name.indexOf("]")
            if (openIndex < 0 || closeIndex < 0) return -1
            if (openIndex + 1 == closeIndex) return -1
            return name.substring(openIndex + 1, closeIndex).toInt()
        }
    val arrayType: WrappedTypeReference
        get() {
            require(isArray) {
                "Can't get base type size of non-array"
            }
            val openIndex = name.indexOf("[")
            require(openIndex >= 0) {
                "Can't find type of array $name"
            }
            return WrappedTypeReference(name.substring(0, openIndex).trim())
        }
    val isConst: Boolean
        get() = name.startsWith("const ")
    val unconst: WrappedTypeReference
        get() = if (isConst) WrappedTypeReference(name.substring("const ".length).trim()) else this
    val unreferenced: WrappedTypeReference
        get() = if (isReference) referenced else this
    val isString: Boolean
        get() = unconst.name == STRING || (isReference && referenced.isString)
    val isNative: Boolean
        get() = unconst.name in NATIVE ||
            isString ||
            (isArray && arrayType.isNative) ||
            (isPointer && pointed.isNative)
    val isReturnable: Boolean
        get() = name in NATIVE || isString || name == "long double" || isPointer || isReference
    val isVoid: Boolean
        get() = name == "void"
    override val cType: WrappedTypeReference
        get() {
            if (isString || (isPointer && pointed.isString)) {
                return WrappedTypeReference("const char*")
            }
            if (isNative) {
                if (isPointer) {
                    return pointerTo(pointed.cType)
                }
                return this
            }
            if (name == "long double") {
                return WrappedTypeReference("double")
            }
            if (isPointer || isReference) {
                return WrappedTypeReference("void*")
            }
            throw IllegalStateException("Don't know how to convert $name to C")
        }

    override fun typedWith(
        resolverBuilder: ResolverBuilder,
        values: Map<WrappedTemplateType, WrappedTypeReference>
    ) = this

    override fun toString(): String {
        return name
    }

    fun typedWith(vararg args: WrappedTypeReference): WrappedTypeReference {
        return WrappedTypeReference(
            "$name<${args.joinToString(", ") { it.name }}>".also { println("Typed: $it") }
        )
    }

    companion object :
        (String) -> WrappedTypeReference,
        (CValue<CXType>, ResolverBuilder) -> WrappedTypeReference {
        fun pointerTo(type: WrappedTypeReference): WrappedTypeReference {
            return WrappedTypeReference("$type*")
        }

        fun referenceTo(type: WrappedTypeReference): WrappedTypeReference {
            return WrappedTypeReference("$type&")
        }

        fun arrayOf(type: WrappedTypeReference): WrappedTypeReference {
            return WrappedTypeReference("$type[]")
        }

        val VOID = WrappedTypeReference("void")

        override fun invoke(type: String): WrappedTypeReference {
            if (type == "void") return VOID
            if (type == "std::size_t") return invoke("size_t")
            return existingTypes.getOrPut(type) {
                WrappedTypeReference(type)
            }
        }

        override fun invoke(
            type: CValue<CXType>,
            resolverBuilder: ResolverBuilder
        ): WrappedTypeReference {
            val type = resolverBuilder.visit(type)
            return invoke(
                type.spelling.toKString()?.trim() ?: error("Missing spelling for type $type")
            )
        }
    }
}

val WrappedType.isNative: Boolean
    get() = (
        (this as? WrappedTypeReference)?.isNative
            ?: (this as? WrappedModifiedType)?.baseType?.isNative
        ) == true
val WrappedType.isString: Boolean
    get() = (
        (this as? WrappedTypeReference)?.isString
            ?: (this as? WrappedModifiedType)?.baseType?.isString
        ) == true
val WrappedType.kotlinType: WrappedKotlinType
    get() = typeToKotlinType(this)

val WrappedType.isReturnable: Boolean
    get() = (this as? WrappedTypeReference)?.isReturnable
        ?: (this as? WrappedModifiedType)?.baseType?.isReturnable
        ?: false
val WrappedType.isVoid: Boolean
    get() = (this as? WrappedTypeReference)?.isVoid ?: false

private val WrappedModifiedType.privatePointed: WrappedType?
    get() = if (modifier == "*") baseType else null
val WrappedType.pointed: WrappedType
    get() = (this as? WrappedTypeReference)?.pointed
        ?: (this as? WrappedModifiedType)?.privatePointed
        ?: error("Can't find pointed of non pointer")
val WrappedType.isPointer: Boolean
    get() = (this as? WrappedTypeReference)?.isPointer
        ?: ((this as? WrappedModifiedType)?.modifier == "*")

val WrappedType.isArray: Boolean
    get() = (this as? WrappedTypeReference)?.isArray
        ?: ((this as? WrappedModifiedType)?.modifier == "[]")

private val WrappedModifiedType.privateUnreferenced: WrappedType?
    get() = if (modifier == "&") baseType else null
val WrappedType.unreferenced: WrappedType
    get() = (this as? WrappedTypeReference)?.unreferenced
        ?: (this as? WrappedModifiedType)?.privateUnreferenced
        ?: error("Can't find unreferenced of non reference")

val WrappedType.isReference: Boolean
    get() = (this as? WrappedTypeReference)?.isReference
        ?: ((this as? WrappedModifiedType)?.modifier == "&")
val WrappedType.isConst: Boolean
    get() = (this as? WrappedTypeReference)?.isConst
        ?: (this as? WrappedModifiedType)?.baseType?.isConst
        ?: false
val WrappedType.unconst: WrappedTypeReference
    get() = (this as? WrappedTypeReference)?.unconst
        ?: (this as? WrappedModifiedType)?.baseType?.unconst
        ?: error("Can't unconst $this")
