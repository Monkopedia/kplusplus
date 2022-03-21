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
package com.monkopedia.krapper.generator

import clang.CXAvailabilityKind
import clang.CXClientData
import clang.CXCursor
import clang.CXCursorKind
import clang.CXCursorVisitor
import clang.CXFile
import clang.CXIndex
import clang.CXRefQualifierKind
import clang.CXSourceLocation
import clang.CXSourceRange
import clang.CXString
import clang.CXToken
import clang.CXTokenKind
import clang.CXTranslationUnit
import clang.CXTranslationUnit_VisitImplicitAttributes
import clang.CXType
import clang.CXUnsavedFile
import clang.CX_CXXAccessSpecifier
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
import clang.clang_Cursor_getBriefCommentText
import clang.clang_Cursor_getMangling
import clang.clang_Cursor_getNumArguments
import clang.clang_Cursor_getNumTemplateArguments
import clang.clang_Cursor_getOffsetOfField
import clang.clang_Cursor_getRawCommentText
import clang.clang_Cursor_getTemplateArgumentKind
import clang.clang_Cursor_getTemplateArgumentType
import clang.clang_Cursor_getTemplateArgumentUnsignedValue
import clang.clang_Cursor_getTemplateArgumentValue
import clang.clang_Cursor_isAnonymous
import clang.clang_Cursor_isBitField
import clang.clang_EnumDecl_isScoped
import clang.clang_File_tryGetRealPathName
import clang.clang_IndexAction_create
import clang.clang_Type_getAlignOf
import clang.clang_Type_getCXXRefQualifier
import clang.clang_Type_getClassType
import clang.clang_Type_getModifiedType
import clang.clang_Type_getNamedType
import clang.clang_Type_getNumTemplateArguments
import clang.clang_Type_getTemplateArgumentAsType
import clang.clang_Type_getValueType
import clang.clang_annotateTokens
import clang.clang_createIndex
import clang.clang_createTranslationUnit
import clang.clang_defaultDiagnosticDisplayOptions
import clang.clang_defaultSaveOptions
import clang.clang_disposeIndex
import clang.clang_disposeString
import clang.clang_disposeTokens
import clang.clang_disposeTranslationUnit
import clang.clang_equalCursors
import clang.clang_equalTypes
import clang.clang_getArgType
import clang.clang_getArrayElementType
import clang.clang_getArraySize
import clang.clang_getCString
import clang.clang_getCXXAccessSpecifier
import clang.clang_getCanonicalCursor
import clang.clang_getCanonicalType
import clang.clang_getCursor
import clang.clang_getCursorAvailability
import clang.clang_getCursorDefinition
import clang.clang_getCursorDisplayName
import clang.clang_getCursorExtent
import clang.clang_getCursorKind
import clang.clang_getCursorLexicalParent
import clang.clang_getCursorLocation
import clang.clang_getCursorPrettyPrinted
import clang.clang_getCursorReferenceNameRange
import clang.clang_getCursorReferenced
import clang.clang_getCursorResultType
import clang.clang_getCursorSemanticParent
import clang.clang_getCursorSpelling
import clang.clang_getCursorType
import clang.clang_getCursorUSR
import clang.clang_getDeclObjCTypeEncoding
import clang.clang_getElementType
import clang.clang_getEnumConstantDeclUnsignedValue
import clang.clang_getEnumConstantDeclValue
import clang.clang_getEnumDeclIntegerType
import clang.clang_getIncludedFile
import clang.clang_getNullCursor
import clang.clang_getNumArgTypes
import clang.clang_getNumElements
import clang.clang_getNumOverloadedDecls
import clang.clang_getOverloadedDecl
import clang.clang_getPointeeType
import clang.clang_getResultType
import clang.clang_getTokenKind
import clang.clang_getTokenLocation
import clang.clang_getTokenSpelling
import clang.clang_getTranslationUnitCursor
import clang.clang_getTranslationUnitSpelling
import clang.clang_getTypeDeclaration
import clang.clang_getTypeSpelling
import clang.clang_getTypedefDeclUnderlyingType
import clang.clang_getTypedefName
import clang.clang_hashCursor
import clang.clang_isAttribute
import clang.clang_isConstQualifiedType
import clang.clang_isCursorDefinition
import clang.clang_isDeclaration
import clang.clang_isExpression
import clang.clang_isFunctionTypeVariadic
import clang.clang_isInvalid
import clang.clang_isPODType
import clang.clang_isPreprocessing
import clang.clang_isReference
import clang.clang_isRestrictQualifiedType
import clang.clang_isStatement
import clang.clang_isTranslationUnit
import clang.clang_isUnexposed
import clang.clang_isVirtualBase
import clang.clang_isVolatileQualifiedType
import clang.clang_parseTranslationUnit
import clang.clang_visitChildren
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.CValue
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCStringArray
import kotlinx.cinterop.toKString

inline fun CXTranslationUnit.annotateTokens(
    token: CPointer<CXToken>,
    count: UInt,
    cursor: CPointer<CXCursor>
) {
    clang_annotateTokens(this, token, count, cursor)
}

inline fun CXTranslationUnit.dispose() {
    clang_disposeTranslationUnit(this)
}

inline fun CXIndex.dispose() {
    clang_disposeIndex(this)
}

inline fun createIndex(excludeDeclarationsFromPCH: Int, displayDiagnostics: Int) =
    clang_createIndex(excludeDeclarationsFromPCH, displayDiagnostics)

inline fun CXIndex.createIndexAction() = clang_IndexAction_create(this)

inline fun createTranslationUnit(index: CXIndex, str: String) =
    clang_createTranslationUnit(index, str)

inline val CValue<CXCursor>.isConvertingConstructor: Boolean
    get() = clang_CXXConstructor_isConvertingConstructor(this) != 0U

inline val CValue<CXCursor>.isCopyConstructor: Boolean
    get() = clang_CXXConstructor_isCopyConstructor(this) != 0U

inline val CValue<CXCursor>.isDefaultConstructor: Boolean
    get() = clang_CXXConstructor_isDefaultConstructor(this) != 0U

inline val CValue<CXCursor>.isMoveConstructor: Boolean
    get() = clang_CXXConstructor_isMoveConstructor(this) != 0U

inline val CValue<CXCursor>.isMutable: Boolean
    get() = clang_CXXField_isMutable(this) != 0U

inline val CValue<CXCursor>.isConst: Boolean
    get() = clang_CXXMethod_isConst(this) != 0U

inline val CValue<CXCursor>.isDefaulted: Boolean
    get() = clang_CXXMethod_isDefaulted(this) != 0U

inline val CValue<CXCursor>.isPureVirtual: Boolean
    get() = clang_CXXMethod_isPureVirtual(this) != 0U

inline val CValue<CXCursor>.isStatic: Boolean
    get() = clang_CXXMethod_isStatic(this) != 0U

inline val CValue<CXCursor>.isVirtual: Boolean
    get() = clang_CXXMethod_isVirtual(this) != 0U

inline val CValue<CXCursor>.isAbstract: Boolean
    get() = clang_CXXRecord_isAbstract(this) != 0U

inline val CValue<CXCursor>.isScoped: Boolean
    get() = clang_EnumDecl_isScoped(this) != 0U

inline val defaultDiagnosticDisplayOptions: UInt
    get() = clang_defaultDiagnosticDisplayOptions()

inline val CXTranslationUnit.defaultSaveOptions: UInt
    get() = clang_defaultSaveOptions(this)

inline fun CValue<CXString>.dispose() {
    clang_disposeString(this)
}

inline fun CXTranslationUnit.dispose(token: CValue<CXToken>) {
    memScoped {
        clang_disposeTokens(this@dispose, token.ptr, 1U)
    }
}

inline fun CValue<CXCursor>.equals(other: CValue<CXCursor>): Boolean {
    return clang_equalCursors(this, other) != 0U
}

inline fun CValue<CXType>.equals(other: CValue<CXType>): Boolean {
    return clang_equalTypes(this, other) != 0U
}

inline fun CValue<CXType>.getArgType(index: UInt): CValue<CXType> {
    return clang_getArgType(this, index)
}

inline val CValue<CXType>.arrayElementType: CValue<CXType>
    get() = clang_getArrayElementType(this)

inline val CValue<CXType>.arraySize: Long
    get() = clang_getArraySize(this)

inline val CValue<CXCursor>.canonicalCursor: CValue<CXCursor>
    get() = clang_getCanonicalCursor(this)

inline val CValue<CXType>.canonicalType: CValue<CXType>
    get() = clang_getCanonicalType(this)

inline fun CValue<CXString>.toKString(): String? = clang_getCString(this)?.toKString()

inline fun CXTranslationUnit.getCursor(location: CValue<CXSourceLocation>) =
    clang_getCursor(this, location)

inline val CValue<CXCursor>.availability: CXAvailabilityKind
    get() = clang_getCursorAvailability(this)

inline val CValue<CXCursor>.definition: CValue<CXCursor>
    get() = clang_getCursorDefinition(this)

inline val CValue<CXCursor>.displayName: CValue<CXString>
    get() = clang_getCursorDisplayName(this)

inline val CValue<CXCursor>.extend: CValue<CXSourceRange>
    get() = clang_getCursorExtent(this)

inline val CValue<CXCursor>.lexicalParent: CValue<CXCursor>
    get() = clang_getCursorLexicalParent(this)

inline val CValue<CXCursor>.location: CValue<CXSourceLocation>
    get() = clang_getCursorLocation(this)

inline val CValue<CXCursor>.referenced: CValue<CXCursor>
    get() = clang_getCursorReferenced(this)

inline fun CValue<CXCursor>.getReferenceNameRange(start: UInt, end: UInt): CValue<CXSourceRange> =
    clang_getCursorReferenceNameRange(this, start, end)

inline val CValue<CXCursor>.resultType: CValue<CXType>
    get() = clang_getCursorResultType(this)

inline val CValue<CXCursor>.semanticParent: CValue<CXCursor>
    get() = clang_getCursorSemanticParent(this)

inline val CValue<CXCursor>.spelling: CValue<CXString>
    get() = clang_getCursorSpelling(this)

inline val CValue<CXCursor>.type: CValue<CXType>
    get() = clang_getCursorType(this)

inline val CValue<CXCursor>.usr: CValue<CXString>
    get() = clang_getCursorUSR(this)
inline val CValue<CXCursor>.prettyPrinted: CValue<CXString>
    get() = clang_getCursorPrettyPrinted(this, null)

inline val CValue<CXCursor>.mangling: CValue<CXString>
    get() = clang_Cursor_getMangling(this)

inline val CValue<CXCursor>.accessSpecifier: CX_CXXAccessSpecifier
    get() = clang_getCXXAccessSpecifier(this)

inline val CValue<CXCursor>.objcTypeEncoding: CValue<CXString>
    get() = clang_getDeclObjCTypeEncoding(this)

inline val CValue<CXType>.elementType: CValue<CXType>
    get() = clang_getElementType(this)
inline val CValue<CXCursor>.enumUnsignedValue: ULong
    get() = clang_getEnumConstantDeclUnsignedValue(this)
inline val CValue<CXCursor>.enumValue: Long
    get() = clang_getEnumConstantDeclValue(this)
inline val CValue<CXCursor>.integerType: CValue<CXType>
    get() = clang_getEnumDeclIntegerType(this)

inline val CXCursor.Companion.NULL: CValue<CXCursor>
    get() = clang_getNullCursor()

inline val CValue<CXType>.numArgTypes: Int
    get() = clang_getNumArgTypes(this)

inline val CValue<CXType>.numElements: Long
    get() = clang_getNumElements(this)

inline val CValue<CXCursor>.numOverloadedDecls: UInt
    get() = clang_getNumOverloadedDecls(this)

inline fun CValue<CXCursor>.getOverload(index: UInt): CValue<CXCursor> {
    return clang_getOverloadedDecl(this, index)
}

inline val CValue<CXType>.pointeeType: CValue<CXType>
    get() = clang_getPointeeType(this)
inline val CValue<CXType>.modifiedType: CValue<CXType>
    get() = clang_Type_getModifiedType(this)

inline val CValue<CXCursor>.kind: CXCursorKind
    get() = clang_getCursorKind(this)

inline val CValue<CXToken>.kind: CXTokenKind
    get() = clang_getTokenKind(this)

inline fun CValue<CXToken>.getLocation(tu: CXTranslationUnit): CValue<CXSourceLocation> =
    clang_getTokenLocation(tu, this)

inline fun CValue<CXToken>.getSpelling(tu: CXTranslationUnit): CValue<CXString> =
    clang_getTokenSpelling(tu, this)

inline val CXTranslationUnit.cursor: CValue<CXCursor>
    get() = clang_getTranslationUnitCursor(this)

inline val CXTranslationUnit.spelling: CValue<CXString>
    get() = clang_getTranslationUnitSpelling(this)

inline val CValue<CXType>.typeDeclaration: CValue<CXCursor>
    get() = clang_getTypeDeclaration(this)

inline val CValue<CXCursor>.typedefDeclUnderlyingType: CValue<CXType>
    get() = clang_getTypedefDeclUnderlyingType(this)
inline val CValue<CXType>.typedefName: CValue<CXString>
    get() = clang_getTypedefName(this)

inline val CValue<CXType>.spelling: CValue<CXString>
    get() = clang_getTypeSpelling(this)

inline val CValue<CXCursor>.hash: UInt
    get() = clang_hashCursor(this)

inline val CXCursorKind.isAttribute: Boolean
    get() = clang_isAttribute(this) != 0U

inline val CValue<CXType>.isConstQualifiedType: Boolean
    get() = clang_isConstQualifiedType(this) != 0U

inline val CValue<CXCursor>.isCursorDefinition: Boolean
    get() = clang_isCursorDefinition(this) != 0U

inline val CXCursorKind.isDeclaration: Boolean
    get() = clang_isDeclaration(this) != 0U

inline val CXCursorKind.isExpression: Boolean
    get() = clang_isExpression(this) != 0U

inline val CValue<CXType>.isFunctionTypeVariadic: Boolean
    get() = clang_isFunctionTypeVariadic(this) != 0U

inline val CXCursorKind.isInvalid: Boolean
    get() = clang_isInvalid(this) != 0U

inline val CValue<CXType>.isPODType: Boolean
    get() = clang_isPODType(this) != 0U

inline val CXCursorKind.isPreprocessing: Boolean
    get() = clang_isPreprocessing(this) != 0U

inline val CXCursorKind.isReference: Boolean
    get() = clang_isReference(this) != 0U

inline val CValue<CXType>.isRestrictQualifiedType: Boolean
    get() = clang_isRestrictQualifiedType(this) != 0U

inline val CXCursorKind.isStatement: Boolean
    get() = clang_isStatement(this) != 0U

inline val CXCursorKind.isTranslationUnit: Boolean
    get() = clang_isTranslationUnit(this) != 0U

inline val CXCursorKind.isUnexposed: Boolean
    get() = clang_isUnexposed(this) != 0U

inline val CValue<CXCursor>.isVirtualBase: Boolean
    get() = clang_isVirtualBase(this) != 0U

inline val CValue<CXType>.isVolatileQualifiedType: Boolean
    get() = clang_isVolatileQualifiedType(this) != 0U
inline val CValue<CXType>.result: CValue<CXType>
    get() = clang_getResultType(this)

inline fun CXIndex.parseTranslationUnit(
    script: String,
    command_line_args: Array<String>? = null,
    unsaved_files: Array<CValue<CXUnsavedFile>>? = null,
    options: UInt = 0U or CXTranslationUnit_VisitImplicitAttributes
): CXTranslationUnit? = memScoped {
    val array = unsaved_files?.let {
        allocArray<CXUnsavedFile>(it.size) { index ->
            it[index]
        }
    }
    parseTranslationUnit(
        script,
        command_line_args?.toCStringArray(this),
        command_line_args?.size ?: 0,
        array,
        (unsaved_files?.size ?: 0).toUInt(),
        options
    )
}

inline fun CXIndex.parseTranslationUnit(
    script: String,
    command_line_args: CValuesRef<CPointerVar<ByteVar>>?,
    num_command_line_args: Int,
    unsaved_files: CValuesRef<CXUnsavedFile>?,
    num_unsaved_files: UInt,
    options: UInt
): CXTranslationUnit? {
    return clang_parseTranslationUnit(
        this,
        script,
        command_line_args,
        num_command_line_args,
        unsaved_files,
        num_unsaved_files,
        options
    )
}

inline val CValue<CXCursor>.includedFile: CXFile?
    get() = clang_getIncludedFile(this)

inline val CXFile.path: CValue<CXString>
    get() = clang_File_tryGetRealPathName(this)

inline fun CValue<CXCursor>.visitChildren(
    visitor: CXCursorVisitor?,
    client_data: CXClientData?
): UInt {
    return clang_visitChildren(this, visitor, client_data)
}

inline val CValue<CXCursor>.numArguments: Int
    get() = clang_Cursor_getNumArguments(this)

inline fun CValue<CXCursor>.getArgument(index: UInt) = clang_Cursor_getArgument(this, index)

inline val CValue<CXCursor>.numTemplateArguments: Int
    get() = clang_Cursor_getNumTemplateArguments(this)

inline fun CValue<CXCursor>.getTemplateArgumentKind(index: UInt) =
    clang_Cursor_getTemplateArgumentKind(this, index)

inline fun CValue<CXCursor>.getTemplateArgumentType(index: UInt) =
    clang_Cursor_getTemplateArgumentType(this, index)

inline fun CValue<CXCursor>.getTemplateArgumentValue(index: UInt) =
    clang_Cursor_getTemplateArgumentValue(this, index)

inline fun CValue<CXCursor>.getTemplateArgumentUnsignedValue(index: UInt) =
    clang_Cursor_getTemplateArgumentUnsignedValue(this, index)

inline val CValue<CXCursor>.isAnonymous: Boolean
    get() = clang_Cursor_isAnonymous(this) != 0U
inline val CValue<CXCursor>.isBitField: Boolean
    get() = clang_Cursor_isBitField(this) != 0U
inline val CValue<CXCursor>.briefCommentText: CValue<CXString>
    get() = clang_Cursor_getBriefCommentText(this)
inline val CValue<CXCursor>.rawCommentText: CValue<CXString>
    get() = clang_Cursor_getRawCommentText(this)
inline val CValue<CXCursor>.offsetOfField: Long
    get() = clang_Cursor_getOffsetOfField(this)
inline val CValue<CXType>.align: Long
    get() = clang_Type_getAlignOf(this)
inline val CValue<CXType>.classType: CValue<CXType>
    get() = clang_Type_getClassType(this)

inline val CValue<CXType>.numTemplateArguments: Int
    get() = clang_Type_getNumTemplateArguments(this)

inline fun CValue<CXType>.getTemplateArgumentType(index: UInt) =
    clang_Type_getTemplateArgumentAsType(this, index)

inline val CValue<CXType>.refQualifier: CXRefQualifierKind
    get() = clang_Type_getCXXRefQualifier(this)
inline val CValue<CXType>.namedType: CValue<CXType>
    get() = clang_Type_getNamedType(this)
inline val CValue<CXType>.valueType: CValue<CXType>
    get() = clang_Type_getValueType(this)
