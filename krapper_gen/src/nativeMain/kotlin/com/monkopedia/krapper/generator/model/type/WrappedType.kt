package com.monkopedia.krapper.generator.model.type

import clang.CXType
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.getTemplateArgumentType
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedKotlinType
import com.monkopedia.krapper.generator.model.WrappedTemplateParam
import com.monkopedia.krapper.generator.model.WrappedTypedef
import com.monkopedia.krapper.generator.model.typeToKotlinType
import com.monkopedia.krapper.generator.numTemplateArguments
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.typeDeclaration
import kotlinx.cinterop.CValue

@ThreadLocal
private val existingTypes = mutableMapOf<String, WrappedType>()

abstract class WrappedType : WrappedElement() {
    abstract val cType: WrappedType

    override fun clone(): WrappedType {
        return this
    }

    companion object :
        (String) -> WrappedType,
        (CValue<CXType>, ResolverBuilder) -> WrappedType {

        const val LONG_DOUBLE_STR = "long double"
        val LONG_DOUBLE = WrappedTypeReference(LONG_DOUBLE_STR)
        val VOID = WrappedTypeReference("void")

        override fun invoke(type: String): WrappedType {
            if (type == "void") return VOID
            if (type == "std::size_t") return invoke("size_t")
            return existingTypes.getOrPut(type) {
                if (type.endsWith("*")) {
                    return pointerTo(invoke(type.substring(0, type.length - 1).trim()))
                }
                if (type.endsWith("&")) {
                    return referenceTo(invoke(type.substring(0, type.length - 1).trim()))
                }
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
            if (type.numTemplateArguments > 0) {
                val templateReference = createForType(type, resolverBuilder)
                return WrappedTemplateType(
                    templateReference,
                    List(type.numTemplateArguments) {
                        WrappedType(type.getTemplateArgumentType(it.toUInt()), resolverBuilder)
                    }
                )
            }
            return createForType(type, resolverBuilder)
        }

        private fun createForType(
            type: CValue<CXType>,
            resolverBuilder: ResolverBuilder
        ): WrappedType {
            val type = resolverBuilder.visit(type)
            val spelling =
                type.spelling.toKString()?.trim() ?: error("Missing spelling for type $type")
            val referencedDecl =
                map(type.typeDeclaration, resolverBuilder) ?: return invoke(spelling)
            if (referencedDecl is WrappedTypedef) {
                return WrappedTypedefRef(referencedDecl)
            } else if (referencedDecl is WrappedTemplateParam) {
                return WrappedTemplateRef(referencedDecl)
            }
            return invoke(spelling)
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
        ?: (this as? WrappedModifiedType)?.isReturnable
        ?: false
val WrappedType.isVoid: Boolean
    get() = (this as? WrappedTypeReference)?.isVoid ?: false

private val WrappedModifiedType.privatePointed: WrappedType?
    get() = if (modifier == "*") baseType else null
val WrappedType.pointed: WrappedType
    get() = (this as? WrappedModifiedType)?.privatePointed
        ?: error("Can't find pointed of non pointer")
val WrappedType.isPointer: Boolean
    get() = ((this as? WrappedModifiedType)?.modifier == "*")

val WrappedType.isArray: Boolean
    get() = (this as? WrappedTypeReference)?.isArray
        ?: ((this as? WrappedModifiedType)?.modifier == "[]")

private val WrappedModifiedType.privateUnreferenced: WrappedType?
    get() = if (modifier == "&") baseType else null
val WrappedType.unreferenced: WrappedType
    get() = (this as? WrappedModifiedType)?.privateUnreferenced
        ?: error("Can't find unreferenced of non reference")

val WrappedType.isReference: Boolean
    get() = ((this as? WrappedModifiedType)?.modifier == "&")
val WrappedType.isConst: Boolean
    get() = (this as? WrappedTypeReference)?.isConst
        ?: (this as? WrappedModifiedType)?.baseType?.isConst
        ?: false
val WrappedType.unconst: WrappedTypeReference
    get() = (this as? WrappedTypeReference)?.unconst
        ?: (this as? WrappedModifiedType)?.baseType?.unconst
        ?: error("Can't unconst $this")
