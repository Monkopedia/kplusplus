package clang

import kotlinx.cinterop.COpaquePointer

// BEGIN KRAPPER GEN for base interface DeclApi
interface DeclApi {
    val ptr: COpaquePointer
    fun isOutOfLine(): kotlin.Boolean
    fun hasBody(): kotlin.Boolean
}
// END KRAPPER GEN for base interface DeclApi
