package clang

// BEGIN KRAPPER GEN for enum MultiVersionKind
enum class MultiVersionKind(val value: Int) {
    None(0), Target(1), CPUSpecific(2), CPUDispatch(3), TargetClones(4), TargetVersion(5);

    companion object {
        fun fromValue(v: Int): MultiVersionKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum MultiVersionKind
