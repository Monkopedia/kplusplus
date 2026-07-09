package clang

// BEGIN KRAPPER GEN for enum ParameterABI
enum class ParameterABI(val value: Int) {
    Ordinary(0), SwiftIndirectResult(1), SwiftErrorResult(2), SwiftContext(3), SwiftAsyncContext(4), HLSLOut(5), HLSLInOut(6);

    companion object {
        fun fromValue(v: Int): ParameterABI = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ParameterABI
