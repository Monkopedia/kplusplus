package clang

// BEGIN KRAPPER GEN for enum RefQualifierKind
enum class RefQualifierKind(val value: UInt) {
    RQ_None(0u), RQ_LValue(1u), RQ_RValue(2u);

    companion object {
        fun fromValue(v: UInt): RefQualifierKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum RefQualifierKind
