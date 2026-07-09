package clang.qualifiers

// BEGIN KRAPPER GEN for enum GC
enum class GC(val value: UInt) {
    GCNone(0u), Weak(1u), Strong(2u);

    companion object {
        fun fromValue(v: UInt): GC = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum GC
