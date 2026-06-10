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

// The pure parse-output model (issue #44 brick 1b): the WrappedElement/WrappedType
// hierarchy a front-end produces and the resolver consumes. No libclang cinterop, no
// codegen — both the libclang-C front-end (:krapper_gen) and the future C++-AST
// front-end depend on this module. Targets mirror :krapper_gen (host native + jvm,
// the jvm side carrying the commonMain enums shared with the resolved schema).
plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    // Determine host preset, matching :krapper_gen.
    val hostOs = System.getProperty("os.name")
    when {
        hostOs == "Mac OS X" -> macosX64("native")

        hostOs == "Linux" -> linuxX64("native")

        hostOs.startsWith("Windows") -> mingwX64("native")

        else -> throw GradleException(
            "Host OS '$hostOs' is not supported in Kotlin/Native $project.",
        )
    }
    jvm()
    sourceSets["commonMain"].dependencies {
        implementation(libs.serialization.json)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
