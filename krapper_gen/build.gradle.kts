/*
 * Copyright 2022 Jason Monk
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
import org.jetbrains.kotlin.gradle.plugin.mpp.DisableCacheInKotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCacheApi

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.ksrpc)
    id("c")
    id("cpp")
    // R1 (#128): publish the krapper_gen release binary to mavenLocal as a normal
    // classified release artifact so a from-published consumer resolves the resolve+codegen
    // tool by coordinate. See the publication block near the bottom.
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    // Determine host preset.
    val hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    val hostTarget =
        when {
            hostOs == "Mac OS X" -> macosX64("native")

            hostOs == "Linux" -> linuxX64("native")

            hostOs.startsWith("Windows") -> mingwX64("native")

            else -> throw GradleException(
                "Host OS '$hostOs' is not supported in Kotlin/Native $project.",
            )
        }

    hostTarget.apply {
        binaries {
            executable()
            sharedLib("krapper") {
                export(libs.ksrpc.jni)
            }
            all {
                @OptIn(KotlinNativeCacheApi::class)
                disableNativeCache(
                    version = DisableCacheInKotlinVersion.`2_4_0`,
                    reason = "clikt 5.1.0 duplicate-symbol with cached native libs",
                )
            }
        }
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
    }
    jvm()
    sourceSets["commonMain"].dependencies {
        // The pure parse-output model (front-end seam, issue #44 brick 1b).
        api(project(":krapper_model"))
        implementation(libs.coroutines.core)
        implementation(libs.serialization.json)
        api(kotlin("stdlib"))
        api(libs.ksrpc.core)
    }
    sourceSets["nativeMain"].dependencies {
        implementation(libs.coroutines.core)
        implementation(libs.serialization.json)
        implementation(libs.clikt)
        api(kotlin("reflect"))
        api(libs.ksrpc.core)
        implementation(libs.ksrpc.sockets)
        // api so the sharedLib host binary can export it for JVM consumers.
        api(libs.ksrpc.jni)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// ---- R1 (#128): publish the krapper_gen release binary to mavenLocal ----
//
// krapper_gen is the resolve+codegen tool the kplusplus Gradle plugin invokes. A K/N linuxX64
// .kexe is not a jar (no software-component to wire), so — exactly like the krapper_parse
// release-binary publication — this is an ARTIFACT-ONLY MavenPublication: attach the raw
// executable with a `linuxX64` classifier and an empty extension. mavenLocal only; no signing.
//
// A Maven repo does NOT preserve the +x bit, so a consumer must chmod the resolved file (the
// krapper_parse consume-seam already does this for its binary; R2 wires the same for this one).
val krapperGenBinary = layout.buildDirectory
    .file("bin/native/releaseExecutable/krapper_gen.kexe").get().asFile

publishing {
    publications {
        create<MavenPublication>("releaseBinary") {
            artifactId = "krapper_gen"
            artifact(krapperGenBinary) {
                classifier = "linuxX64"
                extension = ""
            }
        }
    }
}

// Build the release binary before publishing it, and guard that it exists.
tasks.named("publishReleaseBinaryPublicationToMavenLocal") {
    dependsOn("linkReleaseExecutableNative")
    doFirst {
        check(krapperGenBinary.exists()) {
            "publishReleaseBinaryToMavenLocal: expected the krapper_gen release binary at " +
                "$krapperGenBinary — linkReleaseExecutableNative should have produced it."
        }
    }
}

tasks.register("publishReleaseBinaryToMavenLocal") {
    group = "kplusplus"
    description =
        "Build the krapper_gen release binary and publish it to mavenLocal as " +
            "com.monkopedia.kplusplus:krapper_gen:$version (classifier linuxX64)."
    dependsOn("publishReleaseBinaryPublicationToMavenLocal")
}
