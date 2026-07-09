package clang.qualType

// BEGIN KRAPPER GEN for enum PrimitiveCopyKind
enum class PrimitiveCopyKind(val value: UInt) {
    PCK_Trivial(0u), PCK_VolatileTrivial(1u), PCK_ARCStrong(2u), PCK_ARCWeak(3u), PCK_PtrAuth(4u), PCK_Struct(5u);

    companion object {
        fun fromValue(v: UInt): PrimitiveCopyKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum PrimitiveCopyKind
