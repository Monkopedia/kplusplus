package clang

// BEGIN KRAPPER GEN for enum StorageDuration
enum class StorageDuration(val value: UInt) {
    SD_FullExpression(0u), SD_Automatic(1u), SD_Thread(2u), SD_Static(3u), SD_Dynamic(4u);

    companion object {
        fun fromValue(v: UInt): StorageDuration = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum StorageDuration
