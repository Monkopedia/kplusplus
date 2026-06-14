package clang

// BEGIN KRAPPER GEN for enum TypeOfKind
enum class TypeOfKind(val value: UByte) {
    Qualified(0.toUByte()), Unqualified(1.toUByte());

    companion object {
        fun fromValue(v: UByte): TypeOfKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum TypeOfKind
