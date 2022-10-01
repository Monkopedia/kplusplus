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
package com.monkopedia.krapper.generator.model.type

import clang.CXCursorKind.CXCursor_ClassDecl
import clang.CXCursorKind.CXCursor_ClassTemplate
import clang.CXCursorKind.CXCursor_NoDeclFound
import clang.CXCursorKind.CXCursor_StructDecl
import clang.CXCursorKind.CXCursor_TemplateTypeParameter
import clang.CXCursorKind.CXCursor_TypedefDecl
import clang.CXType
import clang.CXTypeKind.CXType_Invalid
import clang.CXTypeKind.CXType_RValueReference
import clang.CXTypeKind.CXType_Unexposed
import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.fullyQualified
import com.monkopedia.krapper.generator.getTemplateArgumentType
import com.monkopedia.krapper.generator.isConstQualifiedType
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedKotlinType
import com.monkopedia.krapper.generator.model.typeToKotlinType
import com.monkopedia.krapper.generator.numTemplateArguments
import com.monkopedia.krapper.generator.pointeeType
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.typeDeclaration
import com.monkopedia.krapper.generator.usr
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents

@ThreadLocal
private val existingTypes = mutableMapOf<String, WrappedType>()

abstract class WrappedType : WrappedElement() {
    abstract val cType: WrappedType

    override fun clone(): WrappedType {
        return this
    }

    abstract val isNative: Boolean
    abstract val isString: Boolean
    val kotlinType: WrappedKotlinType
        get() = typeToKotlinType(this)
    abstract val isReturnable: Boolean
    abstract val isVoid: Boolean

    abstract val pointed: WrappedType
    abstract val isPointer: Boolean

    abstract val isArray: Boolean

    abstract val unreferenced: WrappedType

    abstract val isReference: Boolean
    abstract val isConst: Boolean
    abstract val unconst: WrappedType

    override suspend fun resolve(resolverContext: ResolveContext): ResolvedElement? {
        return resolverContext.resolve(this)
    }

    companion object :
        (String) -> WrappedType,
        (CValue<CXType>, ResolverBuilder) -> WrappedType,
        (CValue<CXType>, ResolverBuilder, Boolean) -> WrappedType {

        const val LONG_DOUBLE_STR = "long double"
        val LONG_DOUBLE = WrappedTypeReference(LONG_DOUBLE_STR)
        val VOID = WrappedTypeReference("void")

        override fun invoke(type: String): WrappedType {
            if (type == "void") return VOID
            if (type == "std::size_t") return invoke("size_t")
            return existingTypes.getOrPut(type) {
                if (type.startsWith("const ")) return const(invoke(type.substring("const ".length)))
                if (type.startsWith("typename ")) {
                    return WrappedTypename(type.substring("typename ".length))
                }
                if (type.endsWith("*")) {
                    return pointerTo(invoke(type.substring(0, type.length - 1).trim()))
                }
                if (type.endsWith("&")) {
                    return referenceTo(invoke(type.substring(0, type.length - 1).trim()))
                }
                if (type.isEmpty()) {
                    throw IllegalArgumentException("Empty type")
                }
                WrappedTypeReference(type)
            }
        }

        override fun invoke(
            type: CValue<CXType>,
            resolverBuilder: ResolverBuilder
        ): WrappedType = invoke(type, resolverBuilder, throwOnError = false)

        override fun invoke(
            type: CValue<CXType>,
            resolverBuilder: ResolverBuilder,
            throwOnError: Boolean
        ): WrappedType {
            val kind = type.useContents { kind }
            try {
                if (kind == CXType_Invalid) {
                    throw IllegalArgumentException("Invalid type")
                } else if (kind == CXType_RValueReference) {
                    throw IllegalArgumentException("RValues unsupported at the moment")
                }
                val spelling = type.spelling.toKString()
                if (spelling?.endsWith("*") == true) {
                    return pointerTo(invoke(type.pointeeType, resolverBuilder))
                        .maybeConst(type.isConstQualifiedType)
                }
                if (spelling?.endsWith("&") == true) {
                    return referenceTo(invoke(type.pointeeType, resolverBuilder))
                        .maybeConst(type.isConstQualifiedType)
                }
                if (type.numTemplateArguments > 0) {
                    val templateReference = createForType(type, resolverBuilder)
                    return WrappedTemplateType(
                        templateReference,
                        List(type.numTemplateArguments) {
                            val tempType = type.getTemplateArgumentType(it.toUInt())
                            if (tempType.useContents { kind } == CXType_Invalid) null
                            else invoke(tempType, resolverBuilder)
                        }.filterNotNull()
                    ).maybeConst(type.isConstQualifiedType)
                }
                return createForType(type, resolverBuilder).maybeConst(type.isConstQualifiedType)
            } catch (t: IllegalArgumentException) {
                if (throwOnError) {
                    throw IllegalArgumentException(
                        "Failed to create type for ${type.spelling.toKString()}",
                        t
                    )
                } else {
                    return UNRESOLVABLE
                }
            }
        }

        private inline fun WrappedType.maybeConst(isConst: Boolean): WrappedType {
            return if (isConst) const(this) else this
        }

        private fun createForType(
            type: CValue<CXType>,
            resolverBuilder: ResolverBuilder
        ): WrappedType {
            val type = resolverBuilder.visit(type)
            var spelling =
                type.spelling.toKString()?.trim() ?: error("Missing spelling for type $type")
            if (spelling.startsWith("const ")) {
                spelling = spelling.substring("const ".length)
            }
            if (spelling.contains('<')) {
                spelling = spelling.substring(0, spelling.indexOf('<'))
            }
            val referencedDecl = type.typeDeclaration
            return when {
                referencedDecl.kind == CXCursor_TypedefDecl -> {
                    WrappedTypedefRef(
                        referencedDecl.usr.toKString() ?: error("Declaration missing usr")
                    )
                }
                referencedDecl.kind == CXCursor_TemplateTypeParameter -> {
                    WrappedTemplateRef(
                        referencedDecl.usr.toKString() ?: error("Declaration missing usr")
                    )
                }
                referencedDecl.kind == CXCursor_ClassTemplate -> {
                    invoke(referencedDecl.fullyQualified)
                }
                referencedDecl.kind == CXCursor_ClassDecl -> {
                    invoke(referencedDecl.fullyQualified)
                }
                referencedDecl.kind == CXCursor_StructDecl -> {
                    invoke(referencedDecl.fullyQualified)
                }
                type.useContents { kind } == CXType_Unexposed &&
                    referencedDecl.kind == CXCursor_NoDeclFound &&
                    !spelling.startsWith("typename ") -> {
                    WrappedTemplateRef(spelling)
                }
                else -> {
                    invoke(spelling)
                }
            }
        }

        fun pointerTo(type: WrappedType): WrappedType {
            return WrappedModifiedType(type, "*")
        }

        fun referenceTo(type: WrappedType): WrappedType {
            return WrappedModifiedType(type, "&")
        }

        fun arrayOf(type: WrappedType): WrappedType {
            return WrappedModifiedType(type, "[]")
        }

        fun const(type: WrappedType): WrappedType {
            if (type.isConst) return type
            return WrappedPrefixedType(type, "const")
        }

        val UNRESOLVABLE: WrappedTypeReference
            get() = WrappedTypeReference("unresolveable")
    }
}
