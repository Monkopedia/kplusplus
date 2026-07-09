package clang

// BEGIN KRAPPER GEN for enum AutoTypeKeyword
enum class AutoTypeKeyword(val value: Int) {
    Auto(0), DecltypeAuto(1), GNUAutoType(2);

    companion object {
        fun fromValue(v: Int): AutoTypeKeyword = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum AutoTypeKeyword
