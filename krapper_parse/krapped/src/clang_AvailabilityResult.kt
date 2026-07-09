package clang

// BEGIN KRAPPER GEN for enum AvailabilityResult
enum class AvailabilityResult(val value: UInt) {
    AR_Available(0u), AR_NotYetIntroduced(1u), AR_Deprecated(2u), AR_Unavailable(3u);

    companion object {
        fun fromValue(v: UInt): AvailabilityResult = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum AvailabilityResult
