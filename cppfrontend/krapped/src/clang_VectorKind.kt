package clang

// BEGIN KRAPPER GEN for enum VectorKind
enum class VectorKind(val value: Int) {
    Generic(0), AltiVecVector(1), AltiVecPixel(2), AltiVecBool(3), Neon(4), NeonPoly(5), SveFixedLengthData(6), SveFixedLengthPredicate(7), RVVFixedLengthData(8), RVVFixedLengthMask(9), RVVFixedLengthMask_1(10), RVVFixedLengthMask_2(11), RVVFixedLengthMask_4(12);

    companion object {
        fun fromValue(v: Int): VectorKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum VectorKind
