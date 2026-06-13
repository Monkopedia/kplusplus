package clang.functionType

// BEGIN KRAPPER GEN for enum ArmStateValue
enum class ArmStateValue(val value: UInt) {
    ARM_None(0u), ARM_Preserves(1u), ARM_In(2u), ARM_Out(3u), ARM_InOut(4u);

    companion object {
        fun fromValue(v: UInt): ArmStateValue = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ArmStateValue
