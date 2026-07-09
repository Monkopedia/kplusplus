package clang.langOptionsBase

// BEGIN KRAPPER GEN for enum StrictFlexArraysLevelKind
enum class StrictFlexArraysLevelKind(val value: Int) {
    Default(0), OneZeroOrIncomplete(1), ZeroOrIncomplete(2), IncompleteOnly(3);

    companion object {
        fun fromValue(v: Int): StrictFlexArraysLevelKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum StrictFlexArraysLevelKind
