import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SourcesJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    // Publish this FIR kotlinc-plugin jar to the Central Portal (and mavenLocal) so the
    // Gradle plugin's SubpluginArtifact (com.monkopedia.kplusplus:kplusplus-compiler-plugin)
    // resolves from a repo for a from-published consumer. vanniktech supplies the Central-
    // valid POM + sources/javadoc jars + GPG signing; `publishToMavenLocal` keeps R1's coords.
    alias(libs.plugins.vannik.publish)
}

// Sign only when a key is present (CI); otherwise local publishToMavenLocal fails with "no
// configured signatory". See compiler/gradle/build.gradle.kts for the full rationale.
val signingConfigured =
    providers.gradleProperty("signingInMemoryKey").isPresent ||
        providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    if (signingConfigured) signAllPublications()
    configure(KotlinJvm(javadocJar = JavadocJar.Empty(), sourcesJar = SourcesJar.Sources()))
    pom {
        name.set("kplusplus compiler (FIR plugin)")
        description.set(
            "The kplusplus kotlinc (FIR) compiler plugin jar, loaded on the Kotlin/Native " +
                "compiler classpath by the com.monkopedia.kplusplus.compiler Gradle plugin.",
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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // The consumer-floor compiler API (`kotlin-api` in the catalog, #225), not the Kotlin
    // this build runs: the consumer's kotlinc loads this jar.
    compileOnly(libs.kotlin.compiler.embeddable)

    // #206: this module's first tests. `compileOnly` above keeps the compiler off the
    // published runtime classpath (the Kotlin compiler supplies it when it loads this
    // plugin), but the tests instantiate ClassId/FqName/Name for real, so they need it.
    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(kotlin("test-junit"))
}

tasks.withType<Test> {
    // #186 B2 / #206: the coverage gate's DENOMINATOR is featuregen's real instantiation
    // surface, read from the repo rather than hand-listed so a new feature row is covered
    // automatically. Two files make it up: the committed manifest the FIR checker itself grew,
    // and the `instantiate(...)` calls the build seeds (the user-template specs no compile can
    // request). `rootProject` is the `compiler` build root, one level below the repo root.
    //
    // (This replaces `kplusplus.test.krapperManglingSource`, which pointed at krapper's
    // `WrappedKotlinType.kt` so the plugin's hand-copied mangling could be cross-checked
    // against it. B2 deleted that copy, so there is nothing left to cross-check.)
    val requested = rootProject.file("../featuregen/krapped/requested.txt")
    val buildScript = rootProject.file("../featuregen/build.gradle.kts")
    systemProperty("kplusplus.test.featuregenRequested", requested.absolutePath)
    systemProperty("kplusplus.test.featuregenBuildScript", buildScript.absolutePath)
    // #237: a path string is not an input. Declared, a denominator change re-runs both gates
    // instead of leaving them UP-TO-DATE over the old one.
    inputs.files(requested, buildScript).withPropertyName("featuregenDenominator")
}

// #237: the SPELLING gate. BindingIndexSpellingGateTest resolves the denominator above against a
// binding-index.json the IN-TREE krapper emitted, re-spelled by THIS build's cppSpec — the one
// pairing that tests the code a PR changes (the root build consumes the published plugin, so a
// root-build test would measure last release's cppSpec). It needs `:featuregen:kplusplusSync` in
// the root build first, so it is its own task rather than part of `test`/`check`; the
// generator-suite job of root-build.yml runs it straight after its featuregen build.
val featuregenBindingIndex = rootProject.file("../featuregen/build/krapped-cpp/binding-index.json")
val spellingGate = "*.BindingIndexSpellingGateTest"

tasks.test {
    filter { excludeTestsMatching(spellingGate) }
}

tasks.register<Test>("bindingIndexGate") {
    group = "verification"
    description = "Resolve featuregen's specs, as this plugin spells them, in krapper's index."
    val testSources = sourceSets.test.get()
    testClassesDirs = testSources.output.classesDirs
    classpath = testSources.runtimeClasspath
    filter { includeTestsMatching(spellingGate) }
    inputs.files(featuregenBindingIndex)
    systemProperty("kplusplus.test.featuregenBindingIndex", featuregenBindingIndex.absolutePath)
    doFirst {
        check(featuregenBindingIndex.isFile) {
            "$featuregenBindingIndex does not exist: run `./gradlew :featuregen:kplusplusSync` " +
                "in the repo root first, so the in-tree krapper emits it."
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.add("-Xjvm-default=all")
        // Kotlin 2.3 FIR FirDeclarationChecker.check uses context parameters.
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
