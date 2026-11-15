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

import java.io.File
import java.net.URI
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

/**
 * THE BUNDLED TOOL MUST BE ONE THIS HOST CAN RUN (#208).
 *
 * The released jar carries `tools/linuxX64/krapper` only, and the plugin used to extract and exec
 * it on every host. These play each host against a fake jar: a host the jar has no binary for
 * fails with a message naming it, the hosts the jar does carry pass, and a user-supplied krapper
 * is never blocked.
 */
class BundledToolsTest {

    private fun jar(vararg targets: String): (String) -> URL? {
        val urls = targets.associate {
            "/com/monkopedia/kplusplus/tools/$it/krapper" to fake(it)
        }
        return urls::get
    }

    private fun fake(target: String) = URI("file:/fake/$target/krapper").toURL()

    private val releasedJar = jar("linuxX64")

    private fun unsupported(osName: String, osArch: String): String {
        val found = BundledTools(osName, osArch, releasedJar).find("krapper")
        return assertIs<BundledTool.UnsupportedHost>(found).message
    }

    @Test
    fun a_mac_host_is_refused_with_the_host_the_supported_set_and_the_override() {
        val message = unsupported("Mac OS X", "aarch64")
        for (part in listOf("Mac OS X aarch64", "macosArm64", "linuxX64", "-Pkpp.krapper=")) {
            assertTrue(part in message, "missing '$part' in: $message")
        }
    }

    @Test
    fun a_linux_arm64_host_is_refused() {
        val message = unsupported("Linux", "aarch64")
        assertTrue("Linux aarch64 (linuxArm64)" in message, message)
    }

    @Test
    fun a_linux_x64_host_gets_the_bundled_binary() {
        val found = BundledTools("Linux", "amd64", releasedJar).find("krapper")
        assertEquals(
            fake("linuxX64"),
            assertIs<BundledTool.Found>(found).resource
        )
    }

    // The supported set is whatever the jar carries: bundling a platform is enough to support it.
    @Test
    fun a_platform_added_to_the_jar_is_supported_with_no_code_change() {
        val found = BundledTools("Mac OS X", "aarch64", jar("linuxX64", "macosArm64"))
            .find("krapper")
        assertEquals(
            fake("macosArm64"),
            assertIs<BundledTool.Found>(found).resource
        )
    }

    @Test
    fun an_unbundled_dev_jar_falls_through_on_any_host() {
        assertIs<BundledTool.None>(BundledTools("Mac OS X", "aarch64", jar()).find("krapper"))
    }

    private fun macProject(configure: Project.() -> Unit = {}): Project {
        val project = ProjectBuilder.builder().withName("demo").build()
        project.extensions.extraProperties["kpp.frontend.demo"] = "cpp"
        project.configure()
        project.pluginManager.apply(KPlusPlusCompilerGradlePlugin::class.java)
        project.plugins.getPlugin(KPlusPlusCompilerGradlePlugin::class.java).bundledTools =
            BundledTools("Mac OS X", "aarch64", releasedJar)
        return project
    }

    // Configuring is fine on the unsupported host (`./gradlew tasks` works on a Mac); the
    // refusal comes when the sync first needs the tool, before anything is extracted or run.
    @Test
    fun the_refusal_surfaces_when_the_sync_runs_not_at_configuration() {
        val project = macProject()
        val sync = project.tasks.getByName("kplusplusSync")
        val failure = assertFailsWith<GradleException> {
            sync.actions.forEach { it.execute(sync) }
        }
        assertTrue("Mac OS X aarch64" in failure.message.orEmpty(), failure.message)
    }

    @Test
    fun a_user_supplied_krapper_skips_the_host_check() {
        val own = File("/opt/my/krapper")
        val project = macProject { extensions.extraProperties["kpp.krapper"] = own.path }
        val resolved = project.plugins.getPlugin(KPlusPlusCompilerGradlePlugin::class.java)
            .resolveKrapper(project)
        assertEquals(own, resolved.binary)
        assertNull(resolved.unavailable)
    }
}
