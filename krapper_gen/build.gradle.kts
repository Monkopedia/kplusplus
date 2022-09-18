/*
 * Copyright 2021 Jason Monk
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
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.7.10"

    id("com.monkopedia.ksrpc.plugin") version "0.6.0"
    id("c")
    id("cpp")
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

val cflags = arrayOf<String>("-I/usr/include")

val ldflags = emptyArray<String>()

kotlin {
    // Determine host preset.
    val hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs.startsWith("Windows") -> mingwX64("native")
        else -> throw GradleException(
            "Host OS '$hostOs' is not supported in Kotlin/Native $project."
        )
    }

    hostTarget.apply {
        binaries {
            executable()
        }
        compilations["main"].cinterops {
            this.create("libclang") {
                this.defFile = file("clang.def")
                this.compilerOpts += cflags
                this.linkerOpts += ldflags
            }
        }
    }
    jvm()
    sourceSets["commonMain"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
        api(kotlin("stdlib"))
        api("com.monkopedia:ksrpc:0.6.0")
    }
    sourceSets["nativeMain"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
        implementation("com.github.ajalt.clikt:clikt:3.2.0")
        api(kotlin("reflect"))
        api("com.monkopedia:ksrpc:0.6.0")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xskip-prerelease-check"
        freeCompilerArgs += "-Xno-param-assertions"
    }
}
