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
rootProject.name = "minimal"
pluginManagement {
    repositories {
        // A TRUE from-published consumer (R2, #128): NO includeBuild. The kplusplus
        // compiler subplugin is resolved from its published mavenLocal plugin marker
        // (com.monkopedia.kplusplus.compiler → kplusplus-compiler-gradle 0.3.0), and
        // the tool binaries (krapper_gen / krapper_parse) are resolved by coordinate
        // from mavenLocal by the plugin itself. This is the same standalone path k++
        // uses to self-host as its own first consumer.
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}
