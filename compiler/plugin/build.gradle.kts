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
                "compiler classpath by the com.monkopedia.kplusplus.compiler Gradle plugin."
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
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")

    // #206: this module's first tests. `compileOnly` above keeps the compiler off the
    // published runtime classpath (the Kotlin compiler supplies it when it loads this
    // plugin), but the tests instantiate ClassId/FqName/Name for real, so they need it.
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")
    testImplementation(kotlin("test-junit"))
}

tasks.withType<Test> {
    // #206: krapper GENERATES the binding classes this plugin looks up, and its mangling is
    // the authority. It lives in `krapper/src/nativeMain`, which is Kotlin/Native and is NOT
    // among the shared source dirs this build compiles, so the cross-check test reads the
    // rule out of krapper's source file instead of calling it. Point it at that file.
    // (`rootProject` is the `compiler` build root, one level below the repo root.)
    systemProperty(
        "kplusplus.test.krapperManglingSource",
        rootProject.file(
            "../krapper/src/nativeMain/kotlin/com/monkopedia/krapper/generator/model/" +
                "WrappedKotlinType.kt"
        ).absolutePath
    )
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.add("-Xjvm-default=all")
        // Kotlin 2.3 FIR FirDeclarationChecker.check uses context parameters.
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
