/*
 * Copyright 2026 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.kplusplus.compiler.gradle

import java.net.URL

/** What the plugin jar carries for one tool on the current host. */
internal sealed interface BundledTool {
    /** An unbundled (in-tree/dev) jar: no binary for ANY host, so resolution falls through. */
    object None : BundledTool

    /** [resource] is the binary for host [target], the Kotlin/Native target naming this host. */
    class Found(val target: String, val resource: URL) : BundledTool

    /** The jar bundles the tool, but not for this host — running it would exec a foreign binary. */
    class UnsupportedHost(val message: String) : BundledTool
}

/**
 * The tool binaries bundled in the plugin jar, one per Kotlin/Native host target, at
 * `/com/monkopedia/kplusplus/tools/<target>/<tool>` (#208). The SUPPORTED hosts are whichever of
 * those resources the jar actually carries, so shipping another platform is a packaging change
 * only. The host and the resource lookup are injectable so tests can play any host and any jar.
 */
internal class BundledTools(
    private val osName: String = System.getProperty("os.name"),
    private val osArch: String = System.getProperty("os.arch"),
    private val resource: (String) -> URL? = { BundledTools::class.java.getResource(it) }
) {
    fun find(tool: String): BundledTool {
        val bundled = HOST_TARGETS.filter { resource(path(it, tool)) != null }
        if (bundled.isEmpty()) return BundledTool.None
        val host = hostTarget()
        if (host in bundled) return BundledTool.Found(host!!, resource(path(host, tool))!!)
        return BundledTool.UnsupportedHost(
            "kplusplus: this plugin bundles $tool for ${bundled.joinToString()} only, but this " +
                "host is $osName $osArch (${host ?: "no Kotlin/Native host target"}). Supply " +
                "your own $tool binary with -P$KRAPPER_OVERRIDE=<absolute-path> (or " +
                "$KRAPPER_OVERRIDE=... in gradle.properties), or build on a supported host."
        )
    }

    /** The Kotlin/Native target naming this host, or null for one K/N cannot host. */
    private fun hostTarget(): String? {
        val os = when {
            osName.startsWith("Linux") -> "linux"
            osName.startsWith("Mac") -> "macos"
            osName.startsWith("Windows") -> "mingw"
            else -> return null
        }
        val arch = when (osArch) {
            "amd64", "x86_64" -> "X64"
            "aarch64", "arm64" -> "Arm64"
            else -> return null
        }
        return os + arch
    }

    private fun path(target: String, tool: String) = "/com/monkopedia/kplusplus/tools/$target/$tool"

    companion object {
        /** `-Pkpp.krapper=<path>`: a user-supplied krapper, used as-is with no host check. */
        const val KRAPPER_OVERRIDE = "kpp.krapper"

        // Every host [hostTarget] can name — the candidates probed in the jar, not a support list.
        private val HOST_TARGETS =
            listOf("linuxX64", "linuxArm64", "macosX64", "macosArm64", "mingwX64")
    }
}
