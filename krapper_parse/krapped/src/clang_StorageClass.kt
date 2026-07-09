package clang

// BEGIN KRAPPER GEN for enum StorageClass
enum class StorageClass(val value: UInt) {
    SC_None(0u), SC_Extern(1u), SC_Static(2u), SC_PrivateExtern(3u), SC_Auto(4u), SC_Register(5u);

    companion object {
        fun fromValue(v: UInt): StorageClass = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum StorageClass
