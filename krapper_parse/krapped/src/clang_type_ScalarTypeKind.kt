package clang.type

// BEGIN KRAPPER GEN for enum ScalarTypeKind
enum class ScalarTypeKind(val value: UInt) {
    STK_CPointer(0u), STK_BlockPointer(1u), STK_ObjCObjectPointer(2u), STK_MemberPointer(3u), STK_Bool(4u), STK_Integral(5u), STK_Floating(6u), STK_IntegralComplex(7u), STK_FloatingComplex(8u), STK_FixedPoint(9u);

    companion object {
        fun fromValue(v: UInt): ScalarTypeKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ScalarTypeKind
