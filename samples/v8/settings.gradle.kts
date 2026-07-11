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
rootProject.name = "v8"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    // This example is the IN-TREE dev loop: it composites the whole repo so edits to the
    // plugin AND the tools take effect immediately. `../../` supplies the tool modules
    // (krapper_gen/krapper_parse); `../../compiler` supplies the plugin itself. (The main repo is
    // now a FLAT build that consumes the PUBLISHED plugin, so it no longer includeBuilds compiler
    // — the composite lives here in the example, per design.) `../../` also works from a worktree
    // under .claude/worktrees/<id>/samples/v8, where it is the worktree root.
    includeBuild("../../")
    includeBuild("../../compiler")
}
