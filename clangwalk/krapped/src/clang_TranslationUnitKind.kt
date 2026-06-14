package clang

// BEGIN KRAPPER GEN for enum TranslationUnitKind
enum class TranslationUnitKind(val value: UInt) {
    TU_Complete(0u), TU_Prefix(1u), TU_ClangModule(2u), TU_Incremental(3u);

    companion object {
        fun fromValue(v: UInt): TranslationUnitKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum TranslationUnitKind
