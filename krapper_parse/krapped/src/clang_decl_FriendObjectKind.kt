package clang.decl

// BEGIN KRAPPER GEN for enum FriendObjectKind
enum class FriendObjectKind(val value: UInt) {
    FOK_None(0u), FOK_Declared(1u), FOK_Undeclared(2u);

    companion object {
        fun fromValue(v: UInt): FriendObjectKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum FriendObjectKind
