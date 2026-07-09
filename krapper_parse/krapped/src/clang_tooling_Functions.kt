package clang.tooling

import clang.ASTUnit
import kotlin.String
import kotlinx.cinterop.MemScope
import krapper.krapper_parse.internal.clang_tooling_build_ast_from_code

// BEGIN KRAPPER GEN for clang.tooling Functions
inline fun MemScope.buildASTFromCode(Code: String?, FileName: String?): ASTUnit? {
    return ASTUnit((clang_tooling_build_ast_from_code(Code, FileName) ?: return null), memScope)
}
inline fun MemScope._buildASTFromCode(Code: String?, FileName: String?): ASTUnit? {
    return ASTUnit((clang_tooling_build_ast_from_code(Code, FileName) ?: return null), memScope)
}
inline fun MemScope.__buildASTFromCode(Code: String?, FileName: String?): ASTUnit? {
    return ASTUnit((clang_tooling_build_ast_from_code(Code, FileName) ?: return null), memScope)
}
// END KRAPPER GEN for clang.tooling Functions
