package com.monkopedia.krapper.generator.model.type

import clang.CXCursorKind.CXCursor_ClassDecl
import clang.CXCursorKind.CXCursor_ClassTemplate
import clang.CXCursorKind.CXCursor_NoDeclFound
import clang.CXCursorKind.CXCursor_TemplateTypeParameter
import clang.CXCursorKind.CXCursor_TypedefDecl
import clang.CXType
import clang.CXTypeKind.CXType_Unexposed
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.fullyQualified
import com.monkopedia.krapper.generator.getTemplateArgumentType
import com.monkopedia.krapper.generator.isConstQualifiedType
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedKotlinType
import com.monkopedia.krapper.generator.model.typeToKotlinType
import com.monkopedia.krapper.generator.modifiedType
import com.monkopedia.krapper.generator.namedType
import com.monkopedia.krapper.generator.numTemplateArguments
import com.monkopedia.krapper.generator.pointeeType
import com.monkopedia.krapper.generator.prettyPrinted
import com.monkopedia.krapper.generator.result
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.typeDeclaration
import com.monkopedia.krapper.generator.typedefName
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

    companion object :
        (String) -> WrappedType,
        (CValue<CXType>, ResolverBuilder) -> WrappedType {

        const val LONG_DOUBLE_STR = "long double"
        val LONG_DOUBLE = WrappedTypeReference(LONG_DOUBLE_STR)
        val VOID = WrappedTypeReference("void")

        override fun invoke(type: String): WrappedType {
            if (type == "void") return VOID
            if (type == "std::size_t") return invoke("size_t")
            if (type.startsWith("const ")) return const(invoke(type.substring("const ".length)))
            if (type.startsWith("typename ")) {
                return WrappedTypename(type.substring("typename ".length))
            }
            return existingTypes.getOrPut(type) {
                if (type.endsWith("*")) {
                    return pointerTo(invoke(type.substring(0, type.length - 1).trim()))
                }
                if (type.endsWith("&")) {
                    return referenceTo(invoke(type.substring(0, type.length - 1).trim()))
                }
//                if (type == "_Alloc" || type == "_Alloc*") {
//                    Throwable("Alloc $type").printStackTrace()
//                }
                WrappedTypeReference(type)
            }
        }

//        fun WrappedTemplateType(
//            type: CValue<CXType>,
//            resolverBuilder: ResolverBuilder
//        ): WrappedType {
//            return type.useContents {
//                when (kind) {
//                    CXTypeKind.CXType_Pointer -> WrappedTemplateType(type.pointeeType, resolverBuilder)
//                    CXTypeKind.CXType_Elaborated -> WrappedTemplateType(type.namedType, resolverBuilder)
// //            CXType_Typedef ->
//                    // CXType_Enum ->
//                    else -> WrappedType(type, resolverBuilder)
//                }
//            }
//        }

        override fun invoke(
            type: CValue<CXType>,
            resolverBuilder: ResolverBuilder
        ): WrappedType {
            val spelling = type.spelling.toKString()
            if (spelling?.endsWith("*") == true) {
                return pointerTo(invoke(type.pointeeType, resolverBuilder)).maybeConst(type.isConstQualifiedType)
            }
            if (spelling?.endsWith("&") == true) {
                return referenceTo(invoke(type.pointeeType, resolverBuilder)).maybeConst(type.isConstQualifiedType)
            }
            if (type.numTemplateArguments > 0) {
                val templateReference = createForType(type, resolverBuilder)
                return WrappedTemplateType(
                    templateReference,
                    List(type.numTemplateArguments) {
                        invoke(type.getTemplateArgumentType(it.toUInt()), resolverBuilder)
                    }
                ).maybeConst(type.isConstQualifiedType)
            }
            return createForType(type, resolverBuilder).maybeConst(type.isConstQualifiedType)
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
                type.useContents { kind } == CXType_Unexposed && referencedDecl.kind == CXCursor_NoDeclFound && !spelling.startsWith("typename ") -> {
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
            return WrappedPrefixedType(type, "const")
        }
    }
}

