package clang

// BEGIN KRAPPER GEN for enum LanguageLinkage
enum class LanguageLinkage(val value: UInt) {
    CLanguageLinkage(0u), CXXLanguageLinkage(1u), NoLanguageLinkage(2u);

    companion object {
        fun fromValue(v: UInt): LanguageLinkage = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum LanguageLinkage
