package clang.aSTContext

// BEGIN KRAPPER GEN for enum OperatorDeleteKind
enum class OperatorDeleteKind(val value: UInt) {
    Regular(0u), GlobalRegular(1u), Array(2u), ArrayGlobal(3u);

    companion object {
        fun fromValue(v: UInt): OperatorDeleteKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum OperatorDeleteKind
