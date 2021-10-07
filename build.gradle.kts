buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.github.autostyle:com.github.autostyle.gradle.plugin:3.1")
    }
}

allprojects {
    afterEvaluate {
        if (!plugins.hasPlugin("org.jetbrains.kotlin.jvm") && !plugins.hasPlugin(
                "org.jetbrains.kotlin.multiplatform"
            )
        ) {
            return@afterEvaluate
        }
        plugins.apply("com.github.autostyle")

        extensions.configure(com.github.autostyle.gradle.AutostyleExtension::class) {
            kotlinGradle {
                // Since kotlin doesn't pick up on multi platform projects
                filter.include("**/*.kt")
                filter.exclude("**/gen/**")
                filter.exclude("**/testout/**")
                ktlint("0.39.0") {
                    userData(mapOf("android" to "true"))
                }

                licenseHeader(
                    """
                |Copyright 2021 Jason Monk
                |
                |Licensed under the Apache License, Version 2.0 (the "License");
                |you may not use this file except in compliance with the License.
                |You may obtain a copy of the License at
                |
                |    https://www.apache.org/licenses/LICENSE-2.0
                |
                |Unless required by applicable law or agreed to in writing, software
                |distributed under the License is distributed on an "AS IS" BASIS,
                |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                |See the License for the specific language governing permissions and
                |limitations under the License.""".trimMargin()
                )
            }
        }
    }
}
