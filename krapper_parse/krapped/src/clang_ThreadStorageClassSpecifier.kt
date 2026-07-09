package clang

// BEGIN KRAPPER GEN for enum ThreadStorageClassSpecifier
enum class ThreadStorageClassSpecifier(val value: UInt) {
    TSCS_unspecified(0u), TSCS___thread(1u), TSCS_thread_local(2u), TSCS__Thread_local(3u);

    companion object {
        fun fromValue(v: UInt): ThreadStorageClassSpecifier = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ThreadStorageClassSpecifier
