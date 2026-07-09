package clang.targetCXXABI

// BEGIN KRAPPER GEN for enum Kind
enum class Kind(val value: UInt) {
    GenericItanium(0u), GenericARM(1u), iOS(2u), AppleARM64(3u), WatchOS(4u), GenericAArch64(5u), GenericMIPS(6u), WebAssembly(7u), Fuchsia(8u), XL(9u), Microsoft(10u);

    companion object {
        fun fromValue(v: UInt): Kind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum Kind
