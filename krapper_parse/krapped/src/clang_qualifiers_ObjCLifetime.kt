package clang.qualifiers

// BEGIN KRAPPER GEN for enum ObjCLifetime
enum class ObjCLifetime(val value: UInt) {
    OCL_None(0u), OCL_ExplicitNone(1u), OCL_Strong(2u), OCL_Weak(3u), OCL_Autoreleasing(4u);

    companion object {
        fun fromValue(v: UInt): ObjCLifetime = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ObjCLifetime
