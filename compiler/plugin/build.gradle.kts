import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.0"
}

group = "com.monkopedia.kplusplus"
version = "0.2.2"

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
