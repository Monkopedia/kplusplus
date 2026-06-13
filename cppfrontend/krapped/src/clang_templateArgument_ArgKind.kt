package clang.templateArgument

// BEGIN KRAPPER GEN for enum ArgKind
enum class ArgKind(val value: UInt) {
    Null(0u), Type(1u), Declaration(2u), NullPtr(3u), Integral(4u), StructuralValue(5u), Template(6u), TemplateExpansion(7u), Expression(8u), Pack(9u);

    companion object {
        fun fromValue(v: UInt): ArgKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ArgKind
