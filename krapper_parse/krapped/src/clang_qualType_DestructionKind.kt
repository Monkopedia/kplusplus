package clang.qualType

// BEGIN KRAPPER GEN for enum DestructionKind
enum class DestructionKind(val value: UInt) {
    DK_none(0u), DK_cxx_destructor(1u), DK_objc_strong_lifetime(2u), DK_objc_weak_lifetime(3u), DK_nontrivial_c_struct(4u);

    companion object {
        fun fromValue(v: UInt): DestructionKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum DestructionKind
