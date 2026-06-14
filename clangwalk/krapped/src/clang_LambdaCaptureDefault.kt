package clang

// BEGIN KRAPPER GEN for enum LambdaCaptureDefault
enum class LambdaCaptureDefault(val value: UInt) {
    LCD_None(0u), LCD_ByCopy(1u), LCD_ByRef(2u);

    companion object {
        fun fromValue(v: UInt): LambdaCaptureDefault = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum LambdaCaptureDefault
