package clang.qualType

// BEGIN KRAPPER GEN for enum PrimitiveDefaultInitializeKind
enum class PrimitiveDefaultInitializeKind(val value: UInt) {
    PDIK_Trivial(0u), PDIK_ARCStrong(1u), PDIK_ARCWeak(2u), PDIK_Struct(3u);

    companion object {
        fun fromValue(v: UInt): PrimitiveDefaultInitializeKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum PrimitiveDefaultInitializeKind
