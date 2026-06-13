package clang

// BEGIN KRAPPER GEN for enum AccessSpecifier
enum class AccessSpecifier(val value: UByte) {
    AS_public(0.toUByte()), AS_protected(1.toUByte()), AS_private(2.toUByte()), AS_none(3.toUByte());

    companion object {
        fun fromValue(v: UByte): AccessSpecifier = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum AccessSpecifier
