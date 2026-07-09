package clang.varDecl

// BEGIN KRAPPER GEN for enum TLSKind
enum class TLSKind(val value: UInt) {
    TLS_None(0u), TLS_Static(1u), TLS_Dynamic(2u);

    companion object {
        fun fromValue(v: UInt): TLSKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum TLSKind
