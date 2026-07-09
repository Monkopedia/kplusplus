package clang

// BEGIN KRAPPER GEN for enum TemplateSpecializationKind
enum class TemplateSpecializationKind(val value: UInt) {
    TSK_Undeclared(0u), TSK_ImplicitInstantiation(1u), TSK_ExplicitSpecialization(2u), TSK_ExplicitInstantiationDeclaration(3u), TSK_ExplicitInstantiationDefinition(4u);

    companion object {
        fun fromValue(v: UInt): TemplateSpecializationKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum TemplateSpecializationKind
