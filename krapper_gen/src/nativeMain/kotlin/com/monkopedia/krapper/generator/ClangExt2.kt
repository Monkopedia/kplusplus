package com.monkopedia.krapper.generator

import clang.CXAvailabilityKind
import clang.CXAvailabilityKind.CXAvailability_NotAccessible
import clang.CXCallingConv
import clang.CXCursor
import clang.CXCursorKind
import clang.CXCursorKind.CXCursor_InvalidCode
import clang.CXCursorKind.CXCursor_InvalidFile
import clang.CXLanguageKind
import clang.CXLanguageKind.CXLanguage_Invalid
import clang.CXTLSKind
import clang.CXTemplateArgumentKind
import clang.CXType
import clang.CXTypeKind
import clang.CXTypeKind.CXType_Invalid
import clang.CXVisibilityKind
import clang.CXVisibilityKind.CXVisibility_Invalid
import clang.CX_CXXAccessSpecifier
import clang.CX_CXXAccessSpecifier.CX_CXXInvalidAccessSpecifier
import clang.CX_StorageClass
import clang.CX_StorageClass.CX_SC_Invalid
import clang.clang_CXXConstructor_isConvertingConstructor
import clang.clang_CXXConstructor_isCopyConstructor
import clang.clang_CXXConstructor_isDefaultConstructor
import clang.clang_CXXConstructor_isMoveConstructor
import clang.clang_CXXField_isMutable
import clang.clang_CXXMethod_isConst
import clang.clang_CXXMethod_isDefaulted
import clang.clang_CXXMethod_isPureVirtual
import clang.clang_CXXMethod_isStatic
import clang.clang_CXXMethod_isVirtual
import clang.clang_CXXRecord_isAbstract
import clang.clang_Cursor_getArgument
import clang.clang_Cursor_getNumArguments
import clang.clang_Cursor_getNumTemplateArguments
import clang.clang_Cursor_getReceiverType
import clang.clang_Cursor_getStorageClass
import clang.clang_Cursor_getTemplateArgumentKind
import clang.clang_Cursor_getTemplateArgumentType
import clang.clang_Cursor_getTemplateArgumentUnsignedValue
import clang.clang_Cursor_getTemplateArgumentValue
import clang.clang_Cursor_isAnonymousRecordDecl
import clang.clang_Cursor_isBitField
import clang.clang_Cursor_isDynamicCall
import clang.clang_Cursor_isFunctionInlined
import clang.clang_Cursor_isInlineNamespace
import clang.clang_Cursor_isMacroBuiltin
import clang.clang_Cursor_isMacroFunctionLike
import clang.clang_EnumDecl_isScoped
import clang.clang_Type_getModifiedType
import clang.clang_Type_getSizeOf
import clang.clang_Type_getValueType
import clang.clang_Type_isTransparentTagTypedef
import clang.clang_getAddressSpace
import clang.clang_getArgType
import clang.clang_getCXXAccessSpecifier
import clang.clang_getCursorAvailability
import clang.clang_getCursorLanguage
import clang.clang_getCursorTLSKind
import clang.clang_getCursorVisibility
import clang.clang_getEnumDeclIntegerType
import clang.clang_getFieldDeclBitWidth
import clang.clang_getFunctionTypeCallingConv
import clang.clang_getIBOutletCollectionType
import clang.clang_getNumArgTypes
import clang.clang_getNumOverloadedDecls
import clang.clang_getOverloadedDecl
import clang.clang_getSpecializedCursorTemplate
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents

class KXCursor private constructor(
    var kind: CXCursorKind,
    var children: List<KXCursor> = emptyList(),
    var visibility: CXVisibilityKind = CXVisibility_Invalid,
    var availablity: CXAvailabilityKind = CXAvailability_NotAccessible,
    var languageKind: CXLanguageKind = CXLanguage_Invalid,
    var tlsKind: CXTLSKind? = null,
    var lexicalParent: KXCursor? = null,
    var semanticParent: KXCursor? = null,
    var type: KXType? = null,
    var typedefDeclUnderlyingType: KXType? = null,
    var enumDeclIntegerType: KXType? = null,
    var enumConstantDeclValue: Long? = null,
    var enumConstantDeclUnsignedValue: ULong? = null,
    var fieldDeclBitWidth: Int? = null,
    var numArguments: Int? = null,
    var arguments: List<KXCursor?> = emptyList(),
    var numTemplateArguments: Int? = null,
    var templateArguments: List<KXTemplateArgument> = emptyList(),
    var isMacroFunctionLike: Boolean? = null,
    var isMacroBultin: Boolean? = null,
    var isFunctionInlined: Boolean? = null,
    var resultType: KXType? = null,
    var offsetOfField: Long? = null,
    var isAnonymous: Boolean? = null,
    var isAnonymousRecordDecl: Boolean? = null,
    var isInlineNamespace: Boolean? = null,
    var isBitfield: Boolean? = null,
    var accessSpecifier: CX_CXXAccessSpecifier = CX_CXXInvalidAccessSpecifier,
    var storageClass: CX_StorageClass = CX_SC_Invalid,
    var numOverloadedDecls: Int? = null,
    var overloadedDecls: List<KXCursor?> = emptyList(),
    var ibOutletCollectionType: KXType? = null,
    var usr: String? = null,
    var spelling: String? = null,
    var referenced: KXCursor? = null,
    var definition: KXCursor? = null,
    var isCursorDefinition: Boolean? = null,
    var canonical: KXCursor? = null,
    var isDynamicCall: Boolean? = null,
    var receiverType: KXType? = null,
    var isConvertingCursor: Boolean? = null,
    var isCopyConstructor: Boolean? = null,
    var isDefaultConstructor: Boolean? = null,
    var isMoveConstructor: Boolean? = null,
    var isMutable: Boolean? = null,
    var isDefaulted: Boolean? = null,
    var isPureVirtual: Boolean? = null,
    var isStatic: Boolean? = null,
    var isVirtual: Boolean? = null,
    var isAbstract: Boolean? = null,
    var isScoped: Boolean? = null,
    var isConst: Boolean? = null,
    var specializedCursorTemplate: KXCursor? = null,
) {
    companion object {
        private val cursorLookup = mutableMapOf<String, KXCursor>()
        fun generate(cursor: CValue<CXCursor>): KXCursor? {
            val kind = cursor.kind
            if (kind == CXCursor_InvalidCode || kind == CXCursor_InvalidFile || kind == CXCursorKind.CXCursor_UnexposedDecl) {
                return null
            }
            return cursorLookup.getOrPut(cursor.usr.toKString() ?: error("No USR")) {
                KXCursor(kind)
            }.also { ret ->
                ret.visibility = clang_getCursorVisibility(cursor)
                ret.availablity = clang_getCursorAvailability(cursor)
                ret.languageKind = clang_getCursorLanguage(cursor)
                ret.tlsKind = clang_getCursorTLSKind(cursor)
                ret.type = KXType.generate(cursor.type)
                ret.typedefDeclUnderlyingType = KXType.generate(cursor.typedefDeclUnderlyingType)
                ret.enumDeclIntegerType = KXType.generate(clang_getEnumDeclIntegerType(cursor))
                ret.enumConstantDeclValue = cursor.enumValue
                ret.enumConstantDeclUnsignedValue = cursor.enumUnsignedValue
                ret.fieldDeclBitWidth = clang_getFieldDeclBitWidth(cursor)
                ret.numArguments = clang_Cursor_getNumArguments(cursor)
                ret.numTemplateArguments = clang_Cursor_getNumTemplateArguments(cursor)
                ret.isMacroFunctionLike = clang_Cursor_isMacroFunctionLike(cursor) != 0U
                ret.isMacroBultin = clang_Cursor_isMacroBuiltin(cursor) != 0U
                ret.isFunctionInlined = clang_Cursor_isFunctionInlined(cursor) != 0U
                ret.resultType = KXType.generate(cursor.resultType)
                ret.offsetOfField = cursor.offsetOfField
                ret.isAnonymous = cursor.isAnonymous
                ret.isAnonymousRecordDecl = clang_Cursor_isAnonymousRecordDecl(cursor) != 0U
                ret.isInlineNamespace = clang_Cursor_isInlineNamespace(cursor) != 0U
                ret.isBitfield = clang_Cursor_isBitField(cursor) != 0U
                ret.accessSpecifier = clang_getCXXAccessSpecifier(cursor)
                ret.storageClass = clang_Cursor_getStorageClass(cursor)
                ret.numOverloadedDecls = clang_getNumOverloadedDecls(cursor).toInt()
                ret.ibOutletCollectionType =
                    KXType.generate(clang_getIBOutletCollectionType(cursor))
                ret.usr = cursor.usr.toString()
                ret.spelling = cursor.spelling.toKString()
                ret.referenced = generate(cursor.referenced)
                ret.definition = generate(cursor.definition)
                ret.isCursorDefinition = cursor.isCursorDefinition
                ret.canonical = generate(cursor.canonicalCursor)
                ret.isDynamicCall = clang_Cursor_isDynamicCall(cursor) != 0
                ret.receiverType = KXType.generate(clang_Cursor_getReceiverType(cursor))
                ret.isConvertingCursor = clang_CXXConstructor_isConvertingConstructor(cursor) != 0U
                ret.isCopyConstructor = clang_CXXConstructor_isCopyConstructor(cursor) != 0U
                ret.isDefaultConstructor = clang_CXXConstructor_isDefaultConstructor(cursor) != 0U
                ret.isMoveConstructor = clang_CXXConstructor_isMoveConstructor(cursor) != 0U
                ret.isMutable = clang_CXXField_isMutable(cursor) != 0U
                ret.isDefaulted = clang_CXXMethod_isDefaulted(cursor) != 0U
                ret.isPureVirtual = clang_CXXMethod_isPureVirtual(cursor) != 0U
                ret.isStatic = clang_CXXMethod_isStatic(cursor) != 0U
                ret.isVirtual = clang_CXXMethod_isVirtual(cursor) != 0U
                ret.isAbstract = clang_CXXRecord_isAbstract(cursor) != 0U
                ret.isScoped = clang_EnumDecl_isScoped(cursor) != 0U
                ret.isConst = clang_CXXMethod_isConst(cursor) != 0U
                ret.overloadedDecls = List(ret.numOverloadedDecls ?: 0) {
                    generate(clang_getOverloadedDecl(cursor, it.toUInt()))
                }
                ret.templateArguments = List(ret.numTemplateArguments ?: 0) {
                    KXTemplateArgument(
                        clang_Cursor_getTemplateArgumentKind(cursor, it.toUInt()),
                        KXType.generate(clang_Cursor_getTemplateArgumentType(cursor, it.toUInt())),
                        clang_Cursor_getTemplateArgumentValue(cursor, it.toUInt()),
                        clang_Cursor_getTemplateArgumentUnsignedValue(cursor, it.toUInt()),
                    )
                }
                ret.arguments = List(ret.numArguments ?: 0) {
                    generate(clang_Cursor_getArgument(cursor, it.toUInt()))
                }
                ret.specializedCursorTemplate = generate(clang_getSpecializedCursorTemplate(cursor))
                ret.lexicalParent = generate(cursor.lexicalParent)
                ret.semanticParent = generate(cursor.semanticParent)
                ret.children = cursor.filterChildren { true }.mapNotNull { generate(it) }
            }
        }
    }
}

class KXTemplateArgument(
    var kind: CXTemplateArgumentKind,
    var type: KXType? = null,
    var value: Long? = null,
    var unsignedValue: ULong? = null
)

class KXType(
    var kind: CXTypeKind,
    var typeSpelling: String? = null,
    var canonicalType: KXType? = null,
    var isConstQualifiedType: Boolean? = null,
    var isVolatileQualifiedType: Boolean? = null,
    var isRestrictQualifiedType: Boolean? = null,
    var addressSpace: UInt? = null,
    var typedefName: String? = null,
    var pointeeType: KXType? = null,
    var typeDeclaration: KXCursor? = null,
    var functionTypeCallingConv: CXCallingConv = CXCallingConv.CXCallingConv_Invalid,
    var resultType: KXType? = null,
    var numArgTypes: Int? = null,
    var argTypes: List<KXType?> = emptyList(),
    var isTypeVariadic: Boolean? = null,
    var isPlainOldData: Boolean? = null,
    var elementType: KXType? = null,
    var numElements: Long? = null,
    var arrayElementType: KXType? = null,
    var namedType: KXType? = null,
    var isTransparentTagTypedef: Boolean? = null,
    var classType: KXType? = null,
    var size: Long? = null,
    var modifiedType: KXType? = null,
    var valueType: KXType? = null,
    var numTemplateArguments: Int? = null,
    var templateArguments: List<KXType?> = emptyList(),
) {
    companion object {
        fun generate(type: CValue<CXType>): KXType? {
            val kind = type.useContents { kind }
            if (kind == CXType_Invalid) return null
            return KXType(
                kind = kind,
                typeSpelling = type.spelling.toKString(),
                canonicalType = generate(type.canonicalType),
                isConstQualifiedType = type.isConstQualifiedType,
                isVolatileQualifiedType = type.isVolatileQualifiedType,
                isRestrictQualifiedType = type.isRestrictQualifiedType,
                addressSpace = clang_getAddressSpace(type),
                typedefName = type.typedefName.toKString(),
                pointeeType = generate(type.pointeeType),
                typeDeclaration = KXCursor.generate(type.typeDeclaration),
                functionTypeCallingConv = clang_getFunctionTypeCallingConv(type),
                resultType = generate(type.result),
                numArgTypes = clang_getNumArgTypes(type),
                argTypes = List(clang_getNumArgTypes(type)) {
                    generate(clang_getArgType(type, it.toUInt()))
                },
                isTypeVariadic = type.isFunctionTypeVariadic,
                isPlainOldData = type.isPODType,
                elementType = generate(type.elementType),
                numElements = type.numElements,
                arrayElementType = generate(type.arrayElementType),
                namedType = generate(type.namedType),
                isTransparentTagTypedef = clang_Type_isTransparentTagTypedef(type) != 0U,
                classType = generate(type.classType),
                size = clang_Type_getSizeOf(type),
                modifiedType = generate(clang_Type_getModifiedType(type)),
                valueType = generate(clang_Type_getValueType(type)),
                numTemplateArguments = type.numTemplateArguments,
                templateArguments = List(type.numTemplateArguments) {
                    generate(type.getTemplateArgumentKind(it.toUInt()))
                },
            )
        }
    }
}
