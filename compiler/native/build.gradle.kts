import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Kotlin/Native loads a plugin compiled against the non-shaded compiler — at the
    // consumer floor (`kotlin-api` in the catalog, #225), like the JVM plugin module.
    compileOnly(libs.kotlin.compiler)
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

// #234: the main source set is `syncSource`'s gitignored copy of compiler/plugin's sources,
// which compiler/plugin lints; linting the copy would double-report and format the wrong files.
tasks.matching { it.name.startsWith("ktlintMainSourceSet") || it.name.endsWith("OverMainSourceSet") }
    .configureEach { enabled = false }
