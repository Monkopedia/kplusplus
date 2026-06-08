package com.monkopedia.kplusplus.compiler.fir

import java.io.File

/**
 * Side-effect output of the FIR SYNC_REQUIRED checker: appends each requested
 * C++ instantiation spec (e.g. `std::vector<float>`) to a build-tracked manifest
 * the kplusplusSync task reads to know what to generate. Closes the
 * detect→generate link so the consumer's build never needs to maintain an
 * --instantiate list by hand.
 *
 * v1: the manifest path is supplied per-compilation via the
 * `requestManifestPath` SubpluginOption. [configure] is called from
 * `KPlusPlusComponentRegistrar.registerExtensions` once per compilation; the
 * checker then resolves the configured path lazily on [append]. If no path was
 * configured we warn once and no-op rather than crashing the compile.
 */
internal object RequestedManifest {

    @Volatile
    private var manifestPath: String? = null

    @Volatile
    private var seen: MutableSet<String>? = null

    @Volatile
    private var warnedMissingConfig: Boolean = false

    fun configure(path: String) {
        synchronized(this) {
            if (manifestPath != path) {
                manifestPath = path
                // Re-load existing entries on path change so subsequent appends
                // dedupe against the right file.
                seen = null
            }
        }
    }

    fun append(spec: String) {
        synchronized(this) {
            val path = manifestPath
            if (path == null) {
                if (!warnedMissingConfig) {
                    warnedMissingConfig = true
                    System.err.println(
                        "kplusplus: requestManifestPath not configured; not recording " +
                            "requested instantiation '$spec'. The kplusplus gradle " +
                            "subplugin should pass this option per-compilation."
                    )
                }
                return
            }
            val state = seen ?: loadExisting(path).also { seen = it }
            if (!state.add(spec)) return
            val file = File(path)
            file.parentFile?.mkdirs()
            // Append-only; the on-disk file is the single source of truth and is
            // re-read between compiler-daemon lifetimes (loadExisting at first call).
            file.appendText(spec + "\n")
        }
    }

    private fun loadExisting(path: String): MutableSet<String> {
        val file = File(path)
        if (!file.exists()) return mutableSetOf()
        return file.readLines().filter { it.isNotBlank() }.toMutableSet()
    }
}
