package clang

// BEGIN KRAPPER GEN for enum InClassInitStyle
enum class InClassInitStyle(val value: UInt) {
    ICIS_NoInit(0u), ICIS_CopyInit(1u), ICIS_ListInit(2u);

    companion object {
        fun fromValue(v: UInt): InClassInitStyle = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum InClassInitStyle
