import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "2.4.0"
    // R1 (#128): publish this Gradle plugin to mavenLocal. With `java-gradle-plugin`,
    // `maven-publish` auto-creates BOTH the main jar publication (`pluginMaven`) AND the
    // plugin-marker publication (com.monkopedia.kplusplus.compiler:...gradle.plugin) that
    // makes `id("com.monkopedia.kplusplus.compiler") version "0.3.0"` resolvable from a repo.
    `maven-publish`
}

group = "com.monkopedia.kplusplus"
version = "0.3.0"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin-api"))
    // For KotlinNativeCompilation (MPP-specific). compileOnly because the consumer's
    // build always has kotlin-gradle-plugin on the classpath.
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins.create("kplusplusCompiler") {
        id = "com.monkopedia.kplusplus.compiler"
        implementationClass =
            "com.monkopedia.kplusplus.compiler.gradle.KPlusPlusCompilerGradlePlugin"
        displayName = "kplusplus compiler plugin"
        description = "Usage-driven C++ template instantiation for kplusplus"
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}
