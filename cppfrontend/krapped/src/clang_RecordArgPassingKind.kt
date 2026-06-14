package clang

// BEGIN KRAPPER GEN for enum RecordArgPassingKind
enum class RecordArgPassingKind(val value: Int) {
    CanPassInRegs(0), CannotPassInRegs(1), CanNeverPassInRegs(2);

    companion object {
        fun fromValue(v: Int): RecordArgPassingKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum RecordArgPassingKind
