package clang.aSTContext

// BEGIN KRAPPER GEN for enum InlineVariableDefinitionKind
enum class InlineVariableDefinitionKind(val value: Int) {
    None(0), Weak(1), WeakUnknown(2), Strong(3);

    companion object {
        fun fromValue(v: Int): InlineVariableDefinitionKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum InlineVariableDefinitionKind
