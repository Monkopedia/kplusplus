package clang.decl

// BEGIN KRAPPER GEN for enum ObjCDeclQualifier
enum class ObjCDeclQualifier(val value: UInt) {
    OBJC_TQ_None(0u), OBJC_TQ_In(1u), OBJC_TQ_Inout(2u), OBJC_TQ_Out(4u), OBJC_TQ_Bycopy(8u), OBJC_TQ_Byref(16u), OBJC_TQ_Oneway(32u), OBJC_TQ_CSNullability(64u);

    companion object {
        fun fromValue(v: UInt): ObjCDeclQualifier = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ObjCDeclQualifier
