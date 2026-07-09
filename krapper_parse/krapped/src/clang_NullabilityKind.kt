package clang

// BEGIN KRAPPER GEN for enum NullabilityKind
enum class NullabilityKind(val value: UByte) {
    NonNull(0.toUByte()), Nullable(1.toUByte()), Unspecified(2.toUByte()), NullableResult(3.toUByte());

    companion object {
        fun fromValue(v: UByte): NullabilityKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum NullabilityKind
