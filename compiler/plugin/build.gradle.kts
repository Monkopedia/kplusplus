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

group = "com.monkopedia.kplusplus"
version = "0.3.3"

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
