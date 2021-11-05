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

import clang.CXCursorKind.CXCursor_TemplateRef
import clang.CXCursorKind.CXCursor_TypedefDecl
import clang.CXType
import clang.CXTypeKind.CXType_Accum
import clang.CXTypeKind.CXType_Atomic
import clang.CXTypeKind.CXType_Attributed
import clang.CXTypeKind.CXType_Auto
import clang.CXTypeKind.CXType_BFloat16
import clang.CXTypeKind.CXType_BlockPointer
import clang.CXTypeKind.CXType_Bool
import clang.CXTypeKind.CXType_Char16
import clang.CXTypeKind.CXType_Char32
import clang.CXTypeKind.CXType_Char_S
import clang.CXTypeKind.CXType_Char_U
import clang.CXTypeKind.CXType_Complex
import clang.CXTypeKind.CXType_ConstantArray
import clang.CXTypeKind.CXType_Dependent
import clang.CXTypeKind.CXType_DependentSizedArray
import clang.CXTypeKind.CXType_Double
import clang.CXTypeKind.CXType_Elaborated
import clang.CXTypeKind.CXType_Enum
import clang.CXTypeKind.CXType_ExtVector
import clang.CXTypeKind.CXType_Float
import clang.CXTypeKind.CXType_Float128
import clang.CXTypeKind.CXType_Float16
import clang.CXTypeKind.CXType_FunctionNoProto
import clang.CXTypeKind.CXType_FunctionProto
import clang.CXTypeKind.CXType_Half
import clang.CXTypeKind.CXType_IncompleteArray
import clang.CXTypeKind.CXType_Int
import clang.CXTypeKind.CXType_Int128
import clang.CXTypeKind.CXType_Invalid
import clang.CXTypeKind.CXType_LValueReference
import clang.CXTypeKind.CXType_Long
import clang.CXTypeKind.CXType_LongAccum
import clang.CXTypeKind.CXType_LongDouble
import clang.CXTypeKind.CXType_LongLong
import clang.CXTypeKind.CXType_MemberPointer
import clang.CXTypeKind.CXType_NullPtr
import clang.CXTypeKind.CXType_OCLEvent
import clang.CXTypeKind.CXType_OCLImage1dArrayRO
import clang.CXTypeKind.CXType_OCLImage1dArrayRW
import clang.CXTypeKind.CXType_OCLImage1dArrayWO
import clang.CXTypeKind.CXType_OCLImage1dBufferRO
import clang.CXTypeKind.CXType_OCLImage1dBufferRW
import clang.CXTypeKind.CXType_OCLImage1dBufferWO
import clang.CXTypeKind.CXType_OCLImage1dRO
import clang.CXTypeKind.CXType_OCLImage1dRW
import clang.CXTypeKind.CXType_OCLImage1dWO
import clang.CXTypeKind.CXType_OCLImage2dArrayDepthRO
import clang.CXTypeKind.CXType_OCLImage2dArrayDepthRW
import clang.CXTypeKind.CXType_OCLImage2dArrayDepthWO
import clang.CXTypeKind.CXType_OCLImage2dArrayMSAADepthRO
import clang.CXTypeKind.CXType_OCLImage2dArrayMSAADepthRW
import clang.CXTypeKind.CXType_OCLImage2dArrayMSAADepthWO
import clang.CXTypeKind.CXType_OCLImage2dArrayMSAARO
import clang.CXTypeKind.CXType_OCLImage2dArrayMSAARW
import clang.CXTypeKind.CXType_OCLImage2dArrayMSAAWO
import clang.CXTypeKind.CXType_OCLImage2dArrayRO
import clang.CXTypeKind.CXType_OCLImage2dArrayRW
import clang.CXTypeKind.CXType_OCLImage2dArrayWO
import clang.CXTypeKind.CXType_OCLImage2dDepthRO
import clang.CXTypeKind.CXType_OCLImage2dDepthRW
import clang.CXTypeKind.CXType_OCLImage2dDepthWO
import clang.CXTypeKind.CXType_OCLImage2dMSAADepthRO
import clang.CXTypeKind.CXType_OCLImage2dMSAADepthRW
import clang.CXTypeKind.CXType_OCLImage2dMSAADepthWO
import clang.CXTypeKind.CXType_OCLImage2dMSAARO
import clang.CXTypeKind.CXType_OCLImage2dMSAARW
import clang.CXTypeKind.CXType_OCLImage2dMSAAWO
import clang.CXTypeKind.CXType_OCLImage2dRO
import clang.CXTypeKind.CXType_OCLImage2dRW
import clang.CXTypeKind.CXType_OCLImage2dWO
import clang.CXTypeKind.CXType_OCLImage3dRO
import clang.CXTypeKind.CXType_OCLImage3dRW
import clang.CXTypeKind.CXType_OCLImage3dWO
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCImeDualRefStreamin
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCImePayload
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCImeResult
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCImeResultDualRefStreamout
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCImeResultSingleRefStreamout
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCImeSingleRefStreamin
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCMcePayload
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCMceResult
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCRefPayload
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCRefResult
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCSicPayload
import clang.CXTypeKind.CXType_OCLIntelSubgroupAVCSicResult
import clang.CXTypeKind.CXType_OCLQueue
import clang.CXTypeKind.CXType_OCLReserveID
import clang.CXTypeKind.CXType_OCLSampler
import clang.CXTypeKind.CXType_ObjCClass
import clang.CXTypeKind.CXType_ObjCId
import clang.CXTypeKind.CXType_ObjCInterface
import clang.CXTypeKind.CXType_ObjCObject
import clang.CXTypeKind.CXType_ObjCObjectPointer
import clang.CXTypeKind.CXType_ObjCSel
import clang.CXTypeKind.CXType_ObjCTypeParam
import clang.CXTypeKind.CXType_Overload
import clang.CXTypeKind.CXType_Pipe
import clang.CXTypeKind.CXType_Pointer
import clang.CXTypeKind.CXType_RValueReference
import clang.CXTypeKind.CXType_Record
import clang.CXTypeKind.CXType_SChar
import clang.CXTypeKind.CXType_Short
import clang.CXTypeKind.CXType_ShortAccum
import clang.CXTypeKind.CXType_Typedef
import clang.CXTypeKind.CXType_UAccum
import clang.CXTypeKind.CXType_UChar
import clang.CXTypeKind.CXType_UInt
import clang.CXTypeKind.CXType_UInt128
import clang.CXTypeKind.CXType_ULong
import clang.CXTypeKind.CXType_ULongAccum
import clang.CXTypeKind.CXType_ULongLong
import clang.CXTypeKind.CXType_UShort
import clang.CXTypeKind.CXType_UShortAccum
import clang.CXTypeKind.CXType_Unexposed
import clang.CXTypeKind.CXType_VariableArray
import clang.CXTypeKind.CXType_Vector
import clang.CXTypeKind.CXType_Void
import clang.CXTypeKind.CXType_WChar
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.namedType
import com.monkopedia.krapper.generator.pointeeType
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.typeDeclaration
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

sealed interface WrappedType {
    fun typedWith(
        resolverBuilder: ResolverBuilder,
        values: Map<WrappedTemplateType, WrappedTypeReference>
    ): WrappedTypeReference

    companion object {
        fun pointerTo(type: WrappedType): WrappedType {
            return WrappedModifiedType(type, "*")
        }

        fun referenceTo(type: WrappedType): WrappedType {
            return WrappedModifiedType(type, "&")
        }

        fun arrayOf(type: WrappedType): WrappedType {
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
            CXType_Elaborated ->WrappedTemplateType(type.namedType, resolverBuilder)
            CXType_Typedef ->
            //CXType_Enum ->
            else -> WrappedTypeReference(type, resolverBuilder)
        }
    }
}

@Serializable
data class WrappedModifiedType(val baseType: WrappedType, val modifier: String) : WrappedType {
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

@Serializable
data class WrappedTemplateType(val name: String) : WrappedType {
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

@Serializable
data class WrappedTemplatedReference(
    val fullyQualifiedTemplate: String,
    val templateArgs: List<WrappedType>
) : WrappedType {
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
data class WrappedTypeReference private constructor(val name: String) : WrappedType {
    val isPointerOrReference: Boolean
        get() = isPointer || isReference
    val kotlinType: WrappedKotlinType
        get() = typeToKotlinType(this)
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
    val cType: WrappedTypeReference
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
