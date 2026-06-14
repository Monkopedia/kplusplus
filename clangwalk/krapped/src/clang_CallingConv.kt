package clang

// BEGIN KRAPPER GEN for enum CallingConv
enum class CallingConv(val value: UInt) {
    CC_C(0u), CC_X86StdCall(1u), CC_X86FastCall(2u), CC_X86ThisCall(3u), CC_X86VectorCall(4u), CC_X86Pascal(5u), CC_Win64(6u), CC_X86_64SysV(7u), CC_X86RegCall(8u), CC_AAPCS(9u), CC_AAPCS_VFP(10u), CC_IntelOclBicc(11u), CC_SpirFunction(12u), CC_DeviceKernel(13u), CC_Swift(14u), CC_SwiftAsync(15u), CC_PreserveMost(16u), CC_PreserveAll(17u), CC_AArch64VectorCall(18u), CC_AArch64SVEPCS(19u), CC_M68kRTD(20u), CC_PreserveNone(21u), CC_RISCVVectorCall(22u), CC_RISCVVLSCall_32(23u), CC_RISCVVLSCall_64(24u), CC_RISCVVLSCall_128(25u), CC_RISCVVLSCall_256(26u), CC_RISCVVLSCall_512(27u), CC_RISCVVLSCall_1024(28u), CC_RISCVVLSCall_2048(29u), CC_RISCVVLSCall_4096(30u), CC_RISCVVLSCall_8192(31u), CC_RISCVVLSCall_16384(32u), CC_RISCVVLSCall_32768(33u), CC_RISCVVLSCall_65536(34u);

    companion object {
        fun fromValue(v: UInt): CallingConv = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum CallingConv
