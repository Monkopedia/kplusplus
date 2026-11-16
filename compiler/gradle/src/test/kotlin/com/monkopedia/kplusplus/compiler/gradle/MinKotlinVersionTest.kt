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
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

/**
 * THE KOTLIN GUARD SITS EXACTLY ON THE API FLOOR (#225).
 *
 * `MIN_KOTLIN_VERSION` sat at 2.3.20 for five months while the compiler API it guards moved to
 * 2.4.0, so a 2.3.x consumer passed the guard and died mid-compilation. It is generated from the
 * catalog's `kotlin-api` now; these pin that wiring from both ends.
 */
class MinKotlinVersionTest {

    @Test
    fun minKotlinVersionIsTheCatalogApiFloor() {
        val catalog = File(System.getProperty("kplusplus.test.versionCatalog")).readText()
        val floor =
            Regex("""^kotlin-api\s*=\s*"([^"]+)"""", RegexOption.MULTILINE)
                .findAll(catalog).map { it.groupValues[1] }.toList()
        assertEquals(listOf(MIN_KOTLIN_VERSION), floor)
    }

    // The KGP on this test classpath is the catalog's floor (`kgpUnderTest`), so the guard must
    // report exactly that version and accept it: a MIN_KOTLIN_VERSION above the floor rejects
    // every consumer the plugin was built for.
    @Test
    fun theFloorKotlinPassesTheGuard() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        val kotlin = project.plugins.withType(KotlinBasePlugin::class.java).single()
        assertEquals(MIN_KOTLIN_VERSION, kotlin.pluginVersion)
        project.pluginManager.apply(KPlusPlusCompilerGradlePlugin::class.java)
    }
}
