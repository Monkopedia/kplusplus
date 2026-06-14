package clang

// BEGIN KRAPPER GEN for enum ConstexprSpecKind
enum class ConstexprSpecKind(val value: Int) {
    Unspecified(0), Constexpr(1), Consteval(2), Constinit(3);

    companion object {
        fun fromValue(v: Int): ConstexprSpecKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ConstexprSpecKind
