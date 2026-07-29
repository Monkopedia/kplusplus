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
rootProject.name = "multiproject"
pluginManagement {
    repositories {
        // A from-published consumer, exactly like samples/minimal: the plugin (and the
        // `krapper` binary bundled inside it) is resolved by coordinate, with mavenLocal
        // first so a locally published build under test wins over the released one.
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

// THE POINT OF THIS SAMPLE (#194): a consumer with more than one project. Everything else in
// samples/ is single-project, where the Kotlin plugin and the kplusplus plugin are declared in
// the same `plugins { }` block and share one buildscript classloader — the arrangement in which
// the plugin's own runtime classpath can never be shadowed. Here the Kotlin plugin is declared
// at the ROOT and kplusplus is applied in a SUBPROJECT, so the plugin runs in a CHILD
// classloader whose parent already owns a (different, older) kotlinx-coroutines. That is the
// shape that shipped broken in 0.3.4.
include(":bindings")
