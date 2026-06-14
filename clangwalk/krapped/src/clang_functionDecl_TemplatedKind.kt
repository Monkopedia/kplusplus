package clang.functionDecl

// BEGIN KRAPPER GEN for enum TemplatedKind
enum class TemplatedKind(val value: UInt) {
    TK_NonTemplate(0u), TK_FunctionTemplate(1u), TK_MemberSpecialization(2u), TK_FunctionTemplateSpecialization(3u), TK_DependentFunctionTemplateSpecialization(4u), TK_DependentNonTemplate(5u);

    companion object {
        fun fromValue(v: UInt): TemplatedKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum TemplatedKind
