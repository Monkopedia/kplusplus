package clang

// BEGIN KRAPPER GEN for enum ArraySizeModifier
enum class ArraySizeModifier(val value: Int) {
    Normal(0), Static(1), Star(2);

    companion object {
        fun fromValue(v: Int): ArraySizeModifier = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ArraySizeModifier
