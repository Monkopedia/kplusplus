package kppbridge

import clang.ASTUnit
import clang.NamedDeclApi
import clang.ParmVarDecl
import clang.QualType
import clang.QualType.Companion.QualType_Holder
import clang.TemplateTypeParmDecl
import kotlin.Int
import kotlin.String
import kotlin.UInt
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.toKString
import krapper.cppfrontend.internal.kppbridge_build_ast_with_args
import krapper.cppfrontend.internal.kppbridge_default_arg_text
import krapper.cppfrontend.internal.kppbridge_default_arg_type
import krapper.cppfrontend.internal.kppbridge_num_template_args
import krapper.cppfrontend.internal.kppbridge_qualified_name
import krapper.cppfrontend.internal.kppbridge_template_arg_as_type
import krapper.cppfrontend.internal.kppbridge_template_base_name
import platform.linux.free

// BEGIN KRAPPER GEN for kppbridge Functions
inline fun MemScope.buildASTWithArgs(code: String?, filename: String?, joinedArgs: String?): ASTUnit? {
    return ASTUnit((kppbridge_build_ast_with_args(code, filename, joinedArgs) ?: return null), memScope)
}
inline fun MemScope.numTemplateArgs(type: QualType): Int {
    return kppbridge_num_template_args(type.ptr)
}
inline fun MemScope.templateArgAsType(type: QualType, index: UInt): QualType {
    val retValue: QualType = memScope.QualType_Holder()
    kppbridge_template_arg_as_type(type.ptr, index, retValue.ptr)
    return retValue
}
inline fun MemScope.qualifiedName(decl: NamedDeclApi?): String? {
    val str: CPointer<ByteVar>? = kppbridge_qualified_name(decl?.ptr)
    val ret: String? = str?.toKString()
    free(str)
    return ret
}
inline fun MemScope.templateBaseName(type: QualType): String? {
    val str: CPointer<ByteVar>? = kppbridge_template_base_name(type.ptr)
    val ret: String? = str?.toKString()
    free(str)
    return ret
}
inline fun MemScope.defaultArgType(parm: TemplateTypeParmDecl?): QualType {
    val retValue: QualType = memScope.QualType_Holder()
    kppbridge_default_arg_type(parm?.ptr, retValue.ptr)
    return retValue
}
inline fun MemScope.defaultArgText(parm: ParmVarDecl?): String? {
    val str: CPointer<ByteVar>? = kppbridge_default_arg_text(parm?.ptr)
    val ret: String? = str?.toKString()
    free(str)
    return ret
}
inline fun MemScope._buildASTWithArgs(code: String?, filename: String?, joinedArgs: String?): ASTUnit? {
    return ASTUnit((kppbridge_build_ast_with_args(code, filename, joinedArgs) ?: return null), memScope)
}
inline fun MemScope._numTemplateArgs(type: QualType): Int {
    return kppbridge_num_template_args(type.ptr)
}
inline fun MemScope._templateArgAsType(type: QualType, index: UInt): QualType {
    val retValue: QualType = memScope.QualType_Holder()
    kppbridge_template_arg_as_type(type.ptr, index, retValue.ptr)
    return retValue
}
inline fun MemScope._qualifiedName(decl: NamedDeclApi?): String? {
    val str: CPointer<ByteVar>? = kppbridge_qualified_name(decl?.ptr)
    val ret: String? = str?.toKString()
    free(str)
    return ret
}
inline fun MemScope._templateBaseName(type: QualType): String? {
    val str: CPointer<ByteVar>? = kppbridge_template_base_name(type.ptr)
    val ret: String? = str?.toKString()
    free(str)
    return ret
}
inline fun MemScope._defaultArgType(parm: TemplateTypeParmDecl?): QualType {
    val retValue: QualType = memScope.QualType_Holder()
    kppbridge_default_arg_type(parm?.ptr, retValue.ptr)
    return retValue
}
inline fun MemScope._defaultArgText(parm: ParmVarDecl?): String? {
    val str: CPointer<ByteVar>? = kppbridge_default_arg_text(parm?.ptr)
    val ret: String? = str?.toKString()
    free(str)
    return ret
}
inline fun MemScope.__buildASTWithArgs(code: String?, filename: String?, joinedArgs: String?): ASTUnit? {
    return ASTUnit((kppbridge_build_ast_with_args(code, filename, joinedArgs) ?: return null), memScope)
}
inline fun MemScope.__numTemplateArgs(type: QualType): Int {
    return kppbridge_num_template_args(type.ptr)
}
inline fun MemScope.__templateArgAsType(type: QualType, index: UInt): QualType {
    val retValue: QualType = memScope.QualType_Holder()
    kppbridge_template_arg_as_type(type.ptr, index, retValue.ptr)
    return retValue
}
inline fun MemScope.__qualifiedName(decl: NamedDeclApi?): String? {
    val str: CPointer<ByteVar>? = kppbridge_qualified_name(decl?.ptr)
    val ret: String? = str?.toKString()
    free(str)
    return ret
}
inline fun MemScope.__templateBaseName(type: QualType): String? {
    val str: CPointer<ByteVar>? = kppbridge_template_base_name(type.ptr)
    val ret: String? = str?.toKString()
    free(str)
    return ret
}
inline fun MemScope.__defaultArgType(parm: TemplateTypeParmDecl?): QualType {
    val retValue: QualType = memScope.QualType_Holder()
    kppbridge_default_arg_type(parm?.ptr, retValue.ptr)
    return retValue
}
inline fun MemScope.__defaultArgText(parm: ParmVarDecl?): String? {
    val str: CPointer<ByteVar>? = kppbridge_default_arg_text(parm?.ptr)
    val ret: String? = str?.toKString()
    free(str)
    return ret
}
// END KRAPPER GEN for kppbridge Functions
