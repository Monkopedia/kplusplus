package clang

// BEGIN KRAPPER GEN for enum ExceptionSpecificationType
enum class ExceptionSpecificationType(val value: UInt) {
    EST_None(0u), EST_DynamicNone(1u), EST_Dynamic(2u), EST_MSAny(3u), EST_NoThrow(4u), EST_BasicNoexcept(5u), EST_DependentNoexcept(6u), EST_NoexceptFalse(7u), EST_NoexceptTrue(8u), EST_Unevaluated(9u), EST_Uninstantiated(10u), EST_Unparsed(11u);

    companion object {
        fun fromValue(v: UInt): ExceptionSpecificationType = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ExceptionSpecificationType
