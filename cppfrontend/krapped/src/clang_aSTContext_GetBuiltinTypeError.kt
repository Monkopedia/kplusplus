package clang.aSTContext

// BEGIN KRAPPER GEN for enum GetBuiltinTypeError
enum class GetBuiltinTypeError(val value: UInt) {
    GE_None(0u), GE_Missing_type(1u), GE_Missing_stdio(2u), GE_Missing_setjmp(3u), GE_Missing_ucontext(4u);

    companion object {
        fun fromValue(v: UInt): GetBuiltinTypeError = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum GetBuiltinTypeError
