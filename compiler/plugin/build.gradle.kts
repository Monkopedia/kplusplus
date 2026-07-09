import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.0"
    // R1 (#128): publish this FIR kotlinc-plugin jar to mavenLocal so the Gradle plugin's
    // SubpluginArtifact (com.monkopedia.kplusplus:kplusplus-compiler-plugin) resolves from a
    // repo for a from-published consumer, not just an includeBuild sibling.
    `maven-publish`
}

group = "com.monkopedia.kplusplus"
version = "0.3.0"

// The default `maven` publication + jar. artifactId defaults to the project name
// (kplusplus-compiler-plugin), which is exactly what KPlusPlusCompilerGradlePlugin's
// SubpluginArtifact requests. Publishing to mavenLocal only (R1, #128) — no signing, no
// remote repo. The published POM carries the ksrpc-jni runtime dep so the kotlinc plugin
// classpath resolves it transitively.
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")
    // Spike #2: in-compiler JNI bridge to libkrapper.so for live libclang access.
    implementation(libs.ksrpc.jni)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.add("-Xjvm-default=all")
        // Kotlin 2.3 FIR FirDeclarationChecker.check uses context parameters.
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
