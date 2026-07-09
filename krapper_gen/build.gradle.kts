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
    // Publish the krapper_gen release binary as a classified release artifact so a
    // from-published consumer resolves the resolve+codegen tool by coordinate. A K/N .kexe
    // is not a jar (no software-component the vanniktech convenience plugin could wire), so
    // this stays a hand-rolled artifact-only MavenPublication. We apply the vanniktech BASE
    // plugin (no auto-config): it contributes the Central Portal upload + `signAllPublications`
    // bundling that also covers our hand-created publication, making it Central-valid (full
    // POM + stub sources/javadoc jars below + GPG signing). See the publication block.
    `maven-publish`
    alias(libs.plugins.vannik.publish.base)
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

// ---- Publish the krapper_gen release binary ----
//
// krapper_gen is the resolve+codegen tool the kplusplus Gradle plugin invokes. A K/N linuxX64
// .kexe is not a jar (no software-component to wire), so — exactly like the krapper_parse
// release-binary publication — this is an ARTIFACT-ONLY MavenPublication: attach the raw
// executable with a `linuxX64` classifier and an empty extension.
//
// Central-validity for a classified-binary-only artifact: the Central Portal still requires a
// complete POM + a sources jar + a javadoc jar + GPG signatures for every file. There is no
// real source/javadoc for a compiled native binary, so we attach EMPTY (stub) sources+javadoc
// jars at the base coordinate; the vanniktech base plugin (applied above) signs every
// publication and uploads the bundle to the Portal. `publishToMavenLocal` still yields the
// same coordinate/classifier R1 published, so the from-published consumer is unchanged.
//
// A Maven repo does NOT preserve the +x bit, so a consumer must chmod the resolved file (the
// consume-seam already does this for the parser binary; the same applies here).
val krapperGenBinary =
    layout.buildDirectory
        .file("bin/native/releaseExecutable/krapper_gen.kexe")
        .get()
        .asFile

// Empty sources + javadoc jars: Central requires them, but a native binary has neither.
val emptySourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
}
val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("releaseBinary") {
            artifactId = "krapper_gen"
            artifact(krapperGenBinary) {
                classifier = "linuxX64"
                extension = ""
            }
            artifact(emptySourcesJar)
            artifact(emptyJavadocJar)
            pom {
                name.set("krapper_gen")
                description.set(
                    "The kplusplus resolve + codegen tool: consumes a parsed model, forces " +
                        "template instantiations, and generates the C++ wrapper + Kotlin/" +
                        "Native bindings (linuxX64 native binary).",
                )
                url.set("https://github.com/Monkopedia/kplusplus")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("Monkopedia")
                        name.set("Jason Monk")
                        email.set("monkopedia@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/Monkopedia/kplusplus")
                    connection.set("scm:git:git://github.com/Monkopedia/kplusplus.git")
                    developerConnection.set(
                        "scm:git:ssh://git@github.com/Monkopedia/kplusplus.git",
                    )
                }
            }
        }
    }
}

// vanniktech base: Central Portal endpoint + sign all publications (incl. the hand-rolled
// releaseBinary above). Sign only when a key is present (CI) — otherwise the sign task fails
// with "no configured signatory", breaking even a plain unsigned publishToMavenLocal. So
// local mavenLocal stays credential-free; CI supplies the in-memory GPG key. See
// docs/releasing.md.
val signingConfigured =
    providers.gradleProperty("signingInMemoryKey").isPresent ||
        providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent

mavenPublishing {
    publishToMavenCentral(automaticRelease = false)
    if (signingConfigured) signAllPublications()
}

// Ship ONLY the release binary to Central. The Kotlin Multiplatform plugin auto-registers
// `jvm`/`native`/`kotlinMultiplatform` publications for this module, but krapper_gen is a
// TOOL (invoked as a binary), not a KMP library anyone consumes by coordinate — publishing
// its .klib/JVM jar to Central would be noise AND fail validation (no sources/javadoc for
// them). Disable their Central-repository publish tasks so vanniktech's Portal bundle
// contains only the Central-valid `releaseBinary`. (mavenLocal is unaffected — those tasks
// stay enabled there, matching R1.)
listOf("Jvm", "KotlinMultiplatform", "Native").forEach { name ->
    tasks
        .matching {
            it.name == "publish${name}PublicationToMavenCentralRepository"
        }.configureEach { enabled = false }
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
