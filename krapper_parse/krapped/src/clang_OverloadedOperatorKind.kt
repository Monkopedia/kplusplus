package clang

// BEGIN KRAPPER GEN for enum OverloadedOperatorKind
enum class OverloadedOperatorKind(val value: Int) {
    OO_None(0), OO_New(1), OO_Delete(2), OO_Array_New(3), OO_Array_Delete(4), OO_Plus(5), OO_Minus(6), OO_Star(7), OO_Slash(8), OO_Percent(9), OO_Caret(10), OO_Amp(11), OO_Pipe(12), OO_Tilde(13), OO_Exclaim(14), OO_Equal(15), OO_Less(16), OO_Greater(17), OO_PlusEqual(18), OO_MinusEqual(19), OO_StarEqual(20), OO_SlashEqual(21), OO_PercentEqual(22), OO_CaretEqual(23), OO_AmpEqual(24), OO_PipeEqual(25), OO_LessLess(26), OO_GreaterGreater(27), OO_LessLessEqual(28), OO_GreaterGreaterEqual(29), OO_EqualEqual(30), OO_ExclaimEqual(31), OO_LessEqual(32), OO_GreaterEqual(33), OO_Spaceship(34), OO_AmpAmp(35), OO_PipePipe(36), OO_PlusPlus(37), OO_MinusMinus(38), OO_Comma(39), OO_ArrowStar(40), OO_Arrow(41), OO_Call(42), OO_Subscript(43), OO_Conditional(44), OO_Coawait(45), NUM_OVERLOADED_OPERATORS(46);

    companion object {
        fun fromValue(v: Int): OverloadedOperatorKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum OverloadedOperatorKind
