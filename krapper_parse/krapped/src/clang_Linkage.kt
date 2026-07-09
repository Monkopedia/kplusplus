package clang

// BEGIN KRAPPER GEN for enum Linkage
enum class Linkage(val value: UByte) {
    Invalid(0.toUByte()), None(1.toUByte()), Internal(2.toUByte()), UniqueExternal(3.toUByte()), VisibleNone(4.toUByte()), Module(5.toUByte()), External(6.toUByte());

    companion object {
        fun fromValue(v: UByte): Linkage = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum Linkage
