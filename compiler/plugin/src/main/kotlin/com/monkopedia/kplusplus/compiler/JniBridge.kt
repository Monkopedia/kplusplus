package com.monkopedia.kplusplus.compiler

import com.monkopedia.krapper.KrapperNativeHost
import com.monkopedia.ksrpc.jni.KsrpcNativeHost
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

/**
 * Spike #2 first-cut: does the Kotlin/Native compiler daemon survive loading
 * libkrapper.so (a K/N-compiled .so hosting a ksrpc service) into its JVM and
 * establishing a JNI ksrpc connection? Two K/N runtimes in one process is the
 * uncertain bit — empirical probe.
 *
 * Best-effort + idempotent: runs at most once per compiler-daemon lifetime; all
 * errors are caught + logged so failure here cannot break an otherwise-good
 * compile. The compiler plugin's own classpath does NOT include krapper, so
 * this probe only opens the connection (no service calls); a follow-up uses
 * connection.defaultChannel().call(...) (raw ksrpc) or pulls KrapperService onto
 * the plugin's classpath.
 */
internal object JniBridge {

    @Volatile
    private var attempted = false

    fun probeOnce() {
        if (attempted) return
        synchronized(this) {
            if (attempted) return
            attempted = true
            runCatching {
                val path = System.getProperty(LIB_PROPERTY) ?: DEFAULT_LIB_PATH
                val lib = File(path)
                check(lib.exists()) { "libkrapper.so not found at $path" }
                System.load(lib.absolutePath)
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                val connection = runBlocking {
                    KsrpcNativeHost.connect(scope, KrapperNativeHost::initialize)
                }
                println(
                    "[kplusplus-jni-bridge] connected to libkrapper.so via JNI: $connection"
                )
            }.onFailure { t ->
                System.err.println(
                    "[kplusplus-jni-bridge] init failed: " +
                        "${t.javaClass.simpleName}: ${t.message}"
                )
            }
        }
    }

    private const val LIB_PROPERTY = "kplusplus.krapper.lib"
    private const val DEFAULT_LIB_PATH =
        "/home/jmonk/git/kplusplus/krapper/build/bin/native/krapperDebugShared/libkrapper.so"
}
