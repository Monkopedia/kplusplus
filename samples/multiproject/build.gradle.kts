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

// The Kotlin plugin is declared HERE, at the root, and applied in :bindings — the ordinary
// multi-project convention (one version pin for the whole build), and the one that made #194
// reachable: this block is what puts kotlin-compiler-runner's own kotlinx-coroutines on the
// ROOT buildscript classloader, the PARENT of the classloader :bindings' kplusplus plugin
// loads from. kplusplus is deliberately NOT declared here — a consumer should not have to
// know about any of this.
plugins {
    kotlin("multiplatform") version "2.4.0" apply false
}
