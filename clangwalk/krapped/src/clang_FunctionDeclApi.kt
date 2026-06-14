package clang

import kotlinx.cinterop.COpaquePointer

// BEGIN KRAPPER GEN for base interface FunctionDeclApi
interface FunctionDeclApi {
    val ptr: COpaquePointer
    fun hasBody(): kotlin.Boolean
    fun isOutOfLine(): kotlin.Boolean
}
// END KRAPPER GEN for base interface FunctionDeclApi
