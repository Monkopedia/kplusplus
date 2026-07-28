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
    // #185: the plugin speaks ksrpc to the krapper tool, so it compiles the SAME @KsService
    // interfaces krapper does (see the shared source sets below) and needs both the
    // serialization and the ksrpc compiler plugins to generate their stubs on this side.
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksrpc)
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
    // #185: the typed channel to krapper. ksrpc-sockets supplies the JVM
    // `ProcessBuilder.asConnection` that speaks the protocol over the subprocess's stdio
    // pipe; ksrpc-core + serialization + coroutines are what the generated stubs run on.
    //
    // Each one EXCLUDES org.jetbrains:annotations: a Gradle buildscript classpath pins that
    // module to `strictly 13.0` (Gradle's embedded Kotlin), while coroutines asks for 23.0.0
    // — an unsatisfiable constraint that fails the consumer's build at configuration time.
    // The excludes are declared per-dependency (not on the configuration) so they are carried
    // in the published POM and protect a from-published consumer too. Dropping the module is
    // safe: its contents are CLASS-retention annotations, and Gradle's own kotlin-stdlib
    // supplies the 13.0 set anyway.
    implementation(libs.ksrpc.core) { excludeJetbrainsAnnotations() }
    implementation(libs.ksrpc.sockets) { excludeJetbrainsAnnotations() }
    implementation(libs.serialization.json) { excludeJetbrainsAnnotations() }
    implementation(libs.coroutines.core) { excludeJetbrainsAnnotations() }
}

fun ExternalModuleDependency.excludeJetbrainsAnnotations() =
    exclude(group = "org.jetbrains", module = "annotations")

// #185: ONE definition of the krapper service protocol, compiled for BOTH sides.
//
// The interfaces, the config/filter/fixup payloads and the resolved schema live in
// :krapper / :krapper_model `commonMain` in the ROOT build; this is a separate included
// build, so it cannot depend on those projects (and they are Kotlin/Native — there is no
// JVM artifact to resolve). Compiling the same source here keeps client and server on one
// definition instead of a hand-maintained mirror, which is what the old FixupDirective JSON
// mirror was. Both directories are pure common Kotlin — kotlinx.serialization + ksrpc
// annotations only, no cinterop, no codegen — so they compile unchanged on the JVM.
kotlin.sourceSets.named("main") {
    kotlin.srcDir("../../krapper/src/commonMain/kotlin")
    kotlin.srcDir("../../krapper_model/src/commonMain/kotlin")
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

// 0.4.0 distribution: BUNDLE the native tool binary INTO the plugin jar so a consumer never
// resolves a separate Maven coordinate — the released plugin carries its own tool
// (extractBundledTool in KPlusPlusCompilerGradlePlugin reads it back out at
// /com/monkopedia/kplusplus/tools/linuxX64/krapper). The compiler build is a SEPARATE included
// build from the tool module, so it can't depend on its link task; the release orchestration
// builds the binary in the root build and passes its absolute path in via
// -Pkpp.bundleTools.krapper=<path>. Absent (the dev/in-tree build) => an unbundled jar: dev
// consumers resolve the sibling :krapper link output instead, so no bundling is needed there.
// Only the released jar carries the binary.
providers.gradleProperty("kpp.bundleTools.krapper").orNull?.let { path ->
    val bin = file(path)
    tasks.named<ProcessResources>("processResources") {
        doFirst {
            check(bin.exists()) {
                "kpp.bundleTools.krapper=$path but no file exists there — build the krapper " +
                    "release binary before bundling it into the plugin jar."
            }
        }
        from(bin) {
            into("com/monkopedia/kplusplus/tools/linuxX64")
            rename { "krapper" }
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
