package clang

// BEGIN KRAPPER GEN for enum GVALinkage
enum class GVALinkage(val value: UInt) {
    GVA_Internal(0u), GVA_AvailableExternally(1u), GVA_DiscardableODR(2u), GVA_StrongExternal(3u), GVA_StrongODR(4u);

    companion object {
        fun fromValue(v: UInt): GVALinkage = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum GVALinkage
