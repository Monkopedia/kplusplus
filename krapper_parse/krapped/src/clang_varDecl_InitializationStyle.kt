package clang.varDecl

// BEGIN KRAPPER GEN for enum InitializationStyle
enum class InitializationStyle(val value: UInt) {
    CInit(0u), CallInit(1u), ListInit(2u), ParenListInit(3u);

    companion object {
        fun fromValue(v: UInt): InitializationStyle = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum InitializationStyle
