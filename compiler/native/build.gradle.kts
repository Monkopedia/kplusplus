import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "com.monkopedia.kplusplus"
version = "0.3.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Kotlin/Native loads a plugin compiled against the non-shaded compiler.
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:2.4.0")
    // Spike #2: in-compiler JNI bridge to libkrapper.so for live libclang access.
    // `api` (not `implementation`) so the dep surfaces to the K/N compiler's
    // plugin-classpath resolution — `implementation` is hidden from consumers.
    api(libs.ksrpc.jni)
}

// The native plugin shares its sources with the JVM plugin module; sync them in
// so there is a single source of truth.
val syncSource = tasks.register<Sync>("syncSource") {
    from("../plugin/src/main/kotlin")
    into("src/main/kotlin")
}

afterEvaluate {
    tasks.named("compileKotlin") { dependsOn(syncSource) }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.add("-Xjvm-default=all")
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
