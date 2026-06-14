package clang

// BEGIN KRAPPER GEN for enum TagTypeKind
enum class TagTypeKind(val value: Int) {
    Struct(0), Interface(1), Union(2), Class(3), Enum(4);

    companion object {
        fun fromValue(v: Int): TagTypeKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum TagTypeKind
