package clang

// BEGIN KRAPPER GEN for enum ObjCStringFormatFamily
enum class ObjCStringFormatFamily(val value: UInt) {
    SFF_None(0u), SFF_NSString(1u), SFF_CFString(2u);

    companion object {
        fun fromValue(v: UInt): ObjCStringFormatFamily = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ObjCStringFormatFamily
