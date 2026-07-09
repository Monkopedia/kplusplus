import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    // Publish the Gradle plugin. Two cooperating plugins (the de-facto standard combo):
    //   * com.vanniktech.maven.publish — POM metadata, sources+javadoc jars, GPG signing,
    //     and the Central Portal upload for BOTH the plugin jar AND its marker publication,
    //     so `mavenCentral()` consumers can resolve `id(...) version "..."`.
    //   * com.gradle.plugin-publish — the Gradle Plugin Portal upload (`publishPlugins`).
    // With `java-gradle-plugin`, vanniktech's `GradlePlugin` platform wires the main
    // `pluginMaven` publication AND the `...gradle.plugin` marker; the plugin-publish plugin
    // owns the Portal side. `publishToMavenLocal` still yields the same coordinates R1 did.
    alias(libs.plugins.vannik.publish)
    alias(libs.plugins.gradle.plugin.publish)
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
    website.set("https://github.com/Monkopedia/kplusplus")
    vcsUrl.set("https://github.com/Monkopedia/kplusplus.git")
    plugins.create("kplusplusCompiler") {
        id = "com.monkopedia.kplusplus.compiler"
        implementationClass =
            "com.monkopedia.kplusplus.compiler.gradle.KPlusPlusCompilerGradlePlugin"
        displayName = "kplusplus compiler plugin"
        description = "Usage-driven C++ template instantiation for kplusplus"
        tags.set(listOf("kotlin", "native", "cpp", "cinterop", "bindings"))
    }
}

// Sign only when a key is actually available (CI). Vanniktech's signAllPublications() wires
// signing unconditionally, and with no signatory present the sign task FAILS ("no configured
// signatory") — including for a plain, unsigned `publishToMavenLocal`. Gate it on the
// in-memory key so local mavenLocal stays credential-free while CI signs. The release
// workflow exports ORG_GRADLE_PROJECT_signingInMemoryKey from the SIGNING_KEY secret.
val signingConfigured =
    providers.gradleProperty("signingInMemoryKey").isPresent ||
        providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent

mavenPublishing {
    // Central Portal endpoint (OSSRH is sunset). automaticRelease=false: the CI upload lands
    // in a Portal staging deployment the owner releases from central.sonatype.com.
    publishToMavenCentral(automaticRelease = false)
    // Sign every publication with the in-memory GPG key supplied by CI env
    // (ORG_GRADLE_PROJECT_signingInMemoryKey / ...Password, from the SIGNING_KEY /
    // SIGNING_PASSWORD secrets) — only when that key is present (see above).
    if (signingConfigured) signAllPublications()
    // `java-gradle-plugin` already registers a real `javadocJar` + `sourcesJar` on the java
    // component (via withJavadocJar()/withSourcesJar()). Tell vanniktech NOT to add its own
    // (JavadocJar.None()) — adding an empty one duplicates the classifier and the publication
    // is rejected. The java-plugin javadoc jar (empty in practice, but a valid jar) satisfies
    // Central's per-artifact javadoc rule.
    configure(GradlePlugin(javadocJar = JavadocJar.None(), sourcesJar = SourcesJar.Sources()))
    pom {
        name.set("kplusplus compiler Gradle plugin")
        description.set(
            "The com.monkopedia.kplusplus.compiler Gradle plugin: usage-driven C++ template " +
                "instantiation and Kotlin/Native bindings generation for kplusplus."
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
            developerConnection.set("scm:git:ssh://git@github.com/Monkopedia/kplusplus.git")
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

// Gradle 9.4 strict task-dependency validation trips when the `pluginMaven` module-metadata
// generator reads vanniktech's empty-javadoc jar without a declared task dependency (the
// java-gradle-plugin + empty-javadoc combination — a vanniktech/Gradle-9 interaction).
// Gradle Module Metadata (.module) is OPTIONAL for both the Central and the Gradle Plugin
// Portal — a Gradle plugin resolves from its marker POM + the plugin jar POM (R1's markers
// were POM-only). Disable .module generation to sidestep the false-positive validation; the
// POM + sources + javadoc jars (what Central actually validates) are unaffected.
tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}
