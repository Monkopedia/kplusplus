package clang

// BEGIN KRAPPER GEN for enum Visibility
enum class Visibility(val value: UInt) {
    HiddenVisibility(0u), ProtectedVisibility(1u), DefaultVisibility(2u);

    companion object {
        fun fromValue(v: UInt): Visibility = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum Visibility
