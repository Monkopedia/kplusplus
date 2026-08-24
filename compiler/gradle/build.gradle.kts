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
    // #194: SHADE the plugin's runtime dependencies into the jar (see the `shaded`
    // configuration + `shadowJar` block below for the full WHY).
    alias(libs.plugins.shadow)
}

// #194: the plugin's runtime dependencies are SHADED — bundled INTO the plugin jar with
// their packages relocated — instead of being declared for the consumer to resolve.
//
// WHY. Gradle gives every project's buildscript its own classloader whose PARENT is the root
// build's, and class loading is PARENT-FIRST. A multi-project build normally declares the
// Kotlin Gradle Plugin only at the ROOT, so the root classloader owns kotlin-compiler-runner's
// kotlinx-coroutines 1.8.0. A module that applies THIS plugin resolves coroutines 1.11.0
// correctly into its CHILD classloader — and never sees it, because `kotlinx.coroutines.Job`
// keeps loading from the parent at 1.8.0. ktor-io then calls `Job.invokeOnCompletion$default`,
// which coroutines 1.9+ moved onto the interface itself (-Xjvm-default=all) while 1.8.0 keeps
// it in `Job$DefaultImpls`: NoSuchMethodError on the first `kplusplusSync` of every
// multi-project consumer (#194 — reachable since #185 put ksrpc, hence ktor + coroutines, on
// the plugin's runtime classpath). Relocating those packages under `shadedPrefix` makes their
// names unique, so there is nothing for a parent classloader to shadow them with, and the
// plugin runs on the exact versions it was compiled against.
//
// These are `shaded` -> `compileOnly`: compiled against the real coordinates, shipped as
// relocated bytecode, and published as NO dependency at all.
val shaded = configurations.create("shaded") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val shadedPrefix = "com.monkopedia.kplusplus.compiler.shaded"

// Every third-party package that lands in the jar. Anything bundled but NOT listed here keeps
// its original name and can still collide, so this must cover the whole `shaded` resolution:
// ksrpc (+ the jnanoid id generator it uses), kotlinx coroutines/serialization/io, ktor-io.
val shadedPackages = listOf(
    "com.monkopedia.ksrpc",
    "com.aventrix.jnanoid",
    "kotlinx.coroutines",
    // kotlinx-coroutines' stack-trace-recovery marker package — real classes with real
    // methods, so a 1.8.0 copy on a parent classloader is the very hazard this fixes.
    "_COROUTINE",
    "kotlinx.serialization",
    "kotlinx.io",
    "io.ktor"
)
configurations.compileOnly { extendsFrom(shaded) }
// The tests exercise the session against the REAL (unrelocated) classes the main source set
// was compiled against, so they need those — and the un-bundled `shadow` set, which is on the
// compile classpath but on no test one — on their runtime classpath.
configurations.testImplementation { extendsFrom(shaded, configurations.shadow.get()) }

dependencies {
    // `shadow` is shadow's bucket for "compile against it, do NOT bundle it, DO declare it for
    // the consumer": it is the only configuration whose dependencies reach the published POM
    // once the shadow jar is the artifact (everything else is expected to be inside the jar).
    // These three are the Kotlin runtime pieces the Gradle daemon / KGP already supply on the
    // parent classloader — relocating them is not even meaningful.
    shadow(kotlin("stdlib"))
    shadow(kotlin("gradle-plugin-api"))
    // ksrpc-api reaches for kotlin-reflect at runtime; it used to arrive transitively.
    shadow(kotlin("reflect"))
    // For KotlinNativeCompilation (MPP-specific). compileOnly because the consumer's
    // build always has kotlin-gradle-plugin on the classpath.
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
    // #185: the typed channel to krapper. ksrpc-sockets supplies the JVM
    // `ProcessBuilder.asConnection` that speaks the protocol over the subprocess's stdio
    // pipe; ksrpc-core + serialization + coroutines are what the generated stubs run on.
    shaded(libs.ksrpc.core)
    shaded(libs.ksrpc.sockets)
    shaded(libs.serialization.json)
    shaded(libs.coroutines.core)

    // The subprocess-lifecycle tests fault-inject a stand-in `krapper` (see
    // KrapperSessionFailureTest); `java-gradle-plugin` already puts gradleApi() on the
    // classpath, which is where the Logger they need comes from.
    testImplementation(kotlin("test-junit"))
}

// #185: ONE definition of the krapper service protocol, compiled for BOTH sides.
//
// The interfaces, the config/filter/fixup payloads and the resolved schema live in
// :krapper / :krapper_model `commonMain` in the ROOT build; this is a separate included
// build, so it cannot depend on those projects (and they are Kotlin/Native — there is no
// JVM artifact to resolve). Compiling the same source here keeps client and server on one
// definition instead of a hand-maintained mirror, which is what the old FixupDirective JSON
// mirror was. Both directories are pure common Kotlin — kotlinx.serialization + ksrpc
// annotations only, no cinterop, no codegen — so they compile unchanged on the JVM.
// #192: GENERATE the plugin's `PLUGIN_VERSION` from the project version instead of keeping a
// hand-maintained literal in plugin source. That literal is what the 0.3.4 prep forgot to
// bump, and nothing could catch it: it feeds the FIR-plugin coordinate, the published-tool
// coordinate, and — worst — the `~/.gradle/kplusplus/tools/$PLUGIN_VERSION/` cache key, so a
// stale-but-well-formed string is not a compile error, it is an upgrading consumer silently
// running the PREVIOUS release's tool binary. Generated, it cannot drift.
//
// A `const val` is inlined into its call sites at compile time, so this survives the #194
// shading unchanged (the constant's own package is ours and is not relocated anyway).
val generatePluginVersion =
    tasks.register("generatePluginVersion") {
        val pluginVersion = version.toString()
        val outputDir = layout.buildDirectory.dir("generated/pluginVersion/main")
        inputs.property("pluginVersion", pluginVersion)
        outputs.dir(outputDir)
        doLast {
            val pkg = outputDir.get().asFile.resolve("com/monkopedia/kplusplus/compiler/gradle")
            pkg.mkdirs()
            pkg.resolve("PluginVersion.kt").writeText(
                """
                |// GENERATED by :kplusplus-compiler-gradle:generatePluginVersion — do not edit.
                |// Source of truth: `version` in the repo root gradle.properties (#192).
                |package com.monkopedia.kplusplus.compiler.gradle
                |
                |internal const val PLUGIN_VERSION: String = "$pluginVersion"
                |
                """.trimMargin(),
            )
        }
    }

kotlin.sourceSets.named("main") {
    kotlin.srcDir("../../krapper/src/commonMain/kotlin")
    kotlin.srcDir("../../krapper_model/src/commonMain/kotlin")
    kotlin.srcDir(generatePluginVersion)
}

// #192 (part two): the four CONSUMED plugin pins — the repo's own `pluginManagement` pin, the
// two from-published samples, and the quickstart snippet in docs/getting-started.md — are
// deliberately LAGGING: they name the last PUBLISHED release, so between the release-prep
// version bump and the post-release migration they are legitimately behind the version above.
// There is no single value to derive them from (the samples are standalone builds that must
// stay copy-pasteable for a real consumer, and the doc is prose), so assert the two properties
// that hold in BOTH phases instead: they all name the same version, and none of them is ahead
// of what this build declares. That catches a half-done migration — which is how
// samples/minimal went stale for a whole release cycle (#195).
//
// #218: the doc pin is here for the same reason but with a worse failure mode — the other
// three drift for at most one release cycle by construction, whereas nothing at all bounds a
// stale quickstart, and it is the first thing a new external consumer copy-pastes. It is
// matched by the SAME `pin` regex below, which anchors on the plugin-id line and requires a
// `version "…"` clause. That is what keeps it safe on a markdown file: the historical version
// MENTIONS in the surrounding prose ("0.3.5 shades that runtime into the plugin jar") and the
// versionless `id(...) apply false` workaround snippet are true statements about past
// releases that must NOT track the current version, and neither matches a pin.
val verifyConsumedPluginPins =
    tasks.register("verifyConsumedPluginPins") {
        group = "verification"
        description =
            "Check the consumed kplusplus plugin pins (root settings + samples + the " +
                "getting-started quickstart) agree with each other and are not ahead of the " +
                "declared project version."
        val declaredVersion = version.toString()
        val repoRoot = layout.projectDirectory.dir("../..")
        val pinFiles =
            listOf(
                "settings.gradle.kts",
                "samples/minimal/build.gradle.kts",
                "samples/multiproject/bindings/build.gradle.kts",
                // The copy-paste quickstart block a real external consumer runs first (#218).
                "docs/getting-started.md",
            ).map { repoRoot.file(it).asFile }
        doLast {
            val pin = Regex("""id\("com\.monkopedia\.kplusplus\.compiler"\)\s+version\s+"([^"]+)"""")
            val found =
                pinFiles.associateWith { file ->
                    val hits = pin.findAll(file.readText()).map { it.groupValues[1] }.toList()
                    check(hits.size == 1) {
                        "Expected exactly one `id(\"com.monkopedia.kplusplus.compiler\") " +
                            "version \"…\"` pin in $file, found ${hits.size}. Either the pin moved " +
                            "or this check (compiler/gradle/build.gradle.kts, #192) needs its " +
                            "file list updated."
                    }
                    hits.single()
                }
            val listing = found.entries.joinToString("\n") { "  ${it.value}  ${it.key}" }
            check(found.values.distinct().size == 1) {
                "The consumed kplusplus plugin pins disagree:\n$listing\n" +
                    "All four must name the SAME published release — they move together in the " +
                    "post-release migration. Bump whichever was missed."
            }
            // Numeric-component compare, zero-padded to equal width; a `-SNAPSHOT`-style
            // suffix contributes nothing, which is what we want (only the release digits rank).
            val pinned = found.values.first()
            val digits = { v: String -> v.split('.', '-').mapNotNull(String::toIntOrNull) }
            val width = maxOf(digits(pinned).size, digits(declaredVersion).size)
            val pad = { v: String -> List(width) { digits(v).getOrElse(it) { 0 } } }
            val ahead = pad(pinned).zip(pad(declaredVersion)).firstOrNull { it.first != it.second }
            check(ahead == null || ahead.first < ahead.second) {
                "The consumed kplusplus plugin pin ($pinned) is AHEAD of the version this build " +
                    "declares ($declaredVersion, from the repo root gradle.properties). A pin may " +
                    "lag a release, never lead one:\n$listing"
            }
        }
    }

tasks.check {
    dependsOn(verifyConsumedPluginPins)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// #194: the PUBLISHED jar is the shadow jar — this module's own classes plus the `shaded`
// configuration, every bundled package moved under `shadedPrefix` (see the `shaded` block
// above for why). Only third-party runtime packages move; `com.monkopedia.kplusplus.**` (the
// plugin) and `com.monkopedia.krapper.**` (the wire protocol, compiled from shared source)
// keep their names — they are what the plugin IS.
tasks.shadowJar {
    // Take the main jar's name: this IS the plugin artifact, not a fat side-car.
    archiveClassifier.set("")
    // Transformers (below) only see entries the copy spec did not already drop, and the Jar
    // default (EXCLUDE) drops same-path duplicates BEFORE they reach one.
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    configurations.set(listOf(shaded))
    for (pkg in shadedPackages) {
        relocate(pkg, "$shadedPrefix.$pkg")
    }
    // ServiceLoader registrations are resource FILES, so relocating the bytecode alone would
    // leave them pointing at (and named after) classes that no longer exist —
    // kotlinx-coroutines' CoroutineExceptionHandler / MainDispatcherFactory providers among
    // them. mergeServiceFiles() rewrites both the entry names and their contents.
    mergeServiceFiles()
    // Never BUNDLE what the Gradle runtime already provides on the parent classloader, and
    // what relocating would break anyway: the Kotlin runtime (stdlib + reflect), slf4j-api
    // (Gradle's own logging API — a relocated copy would find no provider), and
    // org.jetbrains:annotations (CLASS-retention only). Filtered here rather than as
    // configuration-level excludes because `compileOnly` extends `shaded`, and exclude rules
    // are inherited down that chain — they would strip the Kotlin API off our OWN compile
    // classpath.
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*:.*"))
        exclude(dependency("org.jetbrains:annotations:.*"))
        exclude(dependency("org.slf4j:.*:.*"))
    }
    // Dependency-jar metadata that must not be republished: signatures (invalid once the jar
    // is rebuilt) and module-info descriptors (they name the ORIGINAL modules/packages).
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    exclude("**/module-info.class")
}

// The plain `jar` would write the same archive name as the shadow jar above, and there is
// nothing left for it to produce: the shadow plugin already makes its jar the artifact of the
// `java` component, so the shadow jar is what gets published, what the Plugin Portal uploads,
// and what an `includeBuild` consumer substitutes to.
tasks.jar {
    enabled = false
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
