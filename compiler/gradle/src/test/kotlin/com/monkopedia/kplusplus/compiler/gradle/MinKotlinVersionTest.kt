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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

/**
 * THE KOTLIN GUARD SITS EXACTLY ON THE API FLOOR (#225).
 *
 * `MIN_KOTLIN_VERSION` sat at 2.3.20 while the compiler API it guards moved to
 * 2.4.0, so a 2.3.x consumer passed the guard and died mid-compilation. It is generated from the
 * catalog's `kotlin-api` now; these pin that value and that the guard enforces it.
 */
class MinKotlinVersionTest {

    // The guard's own constant, as the shipped class exposes it (a const in the private companion
    // is a public static field), so a literal written back over the derivation is caught too.
    private val minKotlinVersion =
        KPlusPlusCompilerGradlePlugin::class.java.getField("MIN_KOTLIN_VERSION").get(null) as String

    @Test
    fun minKotlinVersionIsTheCatalogApiFloor() {
        val catalog = File(System.getProperty("kplusplus.test.versionCatalog")).readText()
        val floor =
            Regex("""^kotlin-api\s*=\s*"([^"]+)"""", RegexOption.MULTILINE)
                .findAll(catalog).map { it.groupValues[1] }.toList()
        assertEquals(listOf(minKotlinVersion), floor)
    }

    @Test
    fun theGuardAcceptsTheFloor() {
        applyUnder(minKotlinVersion)
    }

    // The case #225 is about: on 0.3.7 a 2.3.20 consumer passed this guard and then failed
    // inside kotlinc; it must get the guard's own error instead.
    @Test
    fun theGuardRejectsKotlinBelowTheFloor() {
        val failure = assertFails { applyUnder("2.3.20") }
        val messages =
            generateSequence(failure, Throwable::cause).mapNotNull { it.message }.toList()
        val expected = "requires Kotlin $minKotlinVersion or later (found 2.3.20)"
        assertTrue(
            messages.any { expected in it },
            "Expected the MIN_KOTLIN_VERSION guard's error, got: $messages"
        )
    }

    // The real KGP on this classpath is aligned up past the floor, so a fake drives the version.
    private fun applyUnder(kotlinVersion: String) {
        FakeKotlinPlugin.reported = kotlinVersion
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(FakeKotlinPlugin::class.java)
        project.pluginManager.apply(KPlusPlusCompilerGradlePlugin::class.java)
    }
}

/** A Kotlin plugin that reports whatever version the test sets, and does nothing else. */
class FakeKotlinPlugin : KotlinBasePlugin {
    override val pluginVersion: String get() = reported

    override fun apply(target: Project) = Unit

    companion object {
        var reported = ""
    }
}
