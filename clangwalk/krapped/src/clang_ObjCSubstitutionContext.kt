package clang

// BEGIN KRAPPER GEN for enum ObjCSubstitutionContext
enum class ObjCSubstitutionContext(val value: Int) {
    Ordinary(0), Result(1), Parameter(2), Property(3), Superclass(4);

    companion object {
        fun fromValue(v: Int): ObjCSubstitutionContext = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ObjCSubstitutionContext
