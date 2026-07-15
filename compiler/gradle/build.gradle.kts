import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.language.jvm.tasks.ProcessResources
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
version = "0.3.3"

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
    // Central Portal endpoint (OSSRH is sunset). automaticRelease=true: once the upload passes
    // Portal validation it is released automatically — no manual step at central.sonatype.com.
    // (A bundle that fails validation is NOT released, so this isn't reckless.)
    publishToMavenCentral(automaticRelease = true)
    // Sign every publication with the in-memory GPG key supplied by CI env
    // (ORG_GRADLE_PROJECT_signingInMemoryKey / ...Password, from the OSSRH_GPG_SECRET_KEY /
    // OSSRH_GPG_SECRET_KEY_PASSWORD secrets) — only when that key is present (see above).
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

// 0.4.0 distribution: BUNDLE the native tool binaries INTO the plugin jar so a consumer never
// resolves a separate krapper_gen/krapper_parse Maven coordinate — the released plugin carries its
// own tools (extractBundledTool in KPlusPlusCompilerGradlePlugin reads them back out at
// /com/monkopedia/kplusplus/tools/linuxX64/<tool>). The compiler build is a SEPARATE included build
// from the tool modules, so it can't depend on their link tasks; the release orchestration builds
// the binaries in the root build and passes their absolute paths in via
// -Pkpp.bundleTools.<tool>=<path>. Absent (the dev/in-tree build) => an unbundled jar: dev
// consumers resolve the sibling :krapper_gen/:krapper_parse link output instead, so no bundling is
// needed there. Only the released jar carries binaries.
listOf("krapperGen" to "krapper_gen", "krapperParse" to "krapper_parse").forEach { (prop, tool) ->
    providers.gradleProperty("kpp.bundleTools.$prop").orNull?.let { path ->
        val bin = file(path)
        tasks.named<ProcessResources>("processResources") {
            doFirst {
                check(bin.exists()) {
                    "kpp.bundleTools.$prop=$path but no file exists there — build the $tool " +
                        "release binary before bundling it into the plugin jar."
                }
            }
            from(bin) {
                into("com/monkopedia/kplusplus/tools/linuxX64")
                rename { tool }
            }
        }
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
