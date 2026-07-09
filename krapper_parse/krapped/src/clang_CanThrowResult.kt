package clang

// BEGIN KRAPPER GEN for enum CanThrowResult
enum class CanThrowResult(val value: UInt) {
    CT_Cannot(0u), CT_Dependent(1u), CT_Can(2u);

    companion object {
        fun fromValue(v: UInt): CanThrowResult = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum CanThrowResult
