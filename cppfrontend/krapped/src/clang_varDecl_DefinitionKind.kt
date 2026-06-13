package clang.varDecl

// BEGIN KRAPPER GEN for enum DefinitionKind
enum class DefinitionKind(val value: UInt) {
    DeclarationOnly(0u), TentativeDefinition(1u), Definition(2u);

    companion object {
        fun fromValue(v: UInt): DefinitionKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum DefinitionKind
