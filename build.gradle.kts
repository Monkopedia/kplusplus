plugins {
    kotlin("multiplatform") version "2.3.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.8.0")
        android.set(true)
    }
}
