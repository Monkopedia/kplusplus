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
    // v2 plugin lives in the main repo as an included build. Two layouts
    // are supported:
    //   * The canonical /home/jmonk/git/kplusplus checkout — `../../`.
    //   * A worktree under .claude/worktrees/<id>/samples/v8,
    //     where `../../` is the worktree root.
    // Either way, `../../` is the kplusplus root that knows about the
    // compiler subplugin via its own settings.gradle.kts.
    includeBuild("../../")
}
