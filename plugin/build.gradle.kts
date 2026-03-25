/*
 * Copyright 2022 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`

    id("org.jetbrains.kotlin.jvm")
    id("com.gradle.plugin-publish") version "2.1.0"
    `maven-publish`
    `signing`
}

repositories {
    mavenCentral()
    mavenLocal()
}

group = "com.monkopedia.kplusplus"
description = "Tool to link kotlin/native binaries with clang or other linkers"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("com.google.guava:guava:33.5.0-jre")
    api(project(":krapper_gen"))
    implementation("com.monkopedia.ksrpc:ksrpc-core:0.11.0")
    implementation("com.monkopedia.ksrpc:ksrpc-sockets:0.11.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testApi("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.10.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withJavadocJar()
    withSourcesJar()
}

gradlePlugin {
    website.set("https://github.com/monkopedia/kplusplus")
    vcsUrl.set("https://github.com/monkopedia/kplusplus")
    val kplusplus by plugins.creating {
        id = "com.monkopedia.kplusplus.plugin"
        implementationClass = "com.monkopedia.kplusplus.KPlusPlusPlugin"
        displayName = "K++ Gradle Plugin"
        description = project.description
        tags.set(listOf("kotlin", "kotlin/native", "c++", "interop"))
    }
}

val functionalTestSourceSet =
    sourceSets.create("functionalTest") {
    }

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    rootProject.findProject(":krapper_gen")?.tasks?.findByName("linkDebugExecutableNative")?.let {
        dependsOn(it)
    }
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
    dependsOn(functionalTest)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

val krapperGen = rootProject.findProject(":krapper_gen")!!

val debugFrontend = true
val copy =
    tasks.register<Copy>("copyKrapperExecutable") {
        if (debugFrontend) {
            from(
                krapperGen.layout.buildDirectory.file(
                    "bin/native/debugExecutable/krapper_gen.kexe",
                ),
            )
        } else {
            from(
                krapperGen.layout.buildDirectory.file(
                    "bin/native/releaseExecutable/krapper_gen.kexe",
                ),
            )
        }
        into(layout.buildDirectory.dir("processedResources"))
    }

afterEvaluate {

    tasks.named("copyKrapperExecutable") {
        if (debugFrontend) {
            dependsOn(krapperGen.tasks["linkDebugExecutableNative"])
            mustRunAfter(krapperGen.tasks["linkDebugExecutableNative"])
        } else {
            dependsOn(krapperGen.tasks["linkReleaseExecutableNative"])
            mustRunAfter(krapperGen.tasks["linkReleaseExecutableNative"])
        }
    }

    tasks.named("jar") {
        mustRunAfter("copyKrapperExecutable")
    }
    tasks.named("sourcesJar") {
        mustRunAfter("copyKrapperExecutable")
    }
}

sourceSets {
    main {
        afterEvaluate {
            tasks.named(processResourcesTaskName) {
                dependsOn(copy)
            }
        }
        resources {
            srcDir(layout.buildDirectory.dir("processedResources"))
            compiledBy(copy)
        }
    }
}

publishing {
    publications.all {
        if (this !is MavenPublication) return@all

        afterEvaluate {
            pom {
                name.set("kplusplus-gradle-plugin")
                description.set(project.description)
                url.set("http://www.github.com/Monkopedia/kplusplus")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("monkopedia")
                        name.set("Jason Monk")
                        email.set("monkopedia@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Monkopedia/kplusplus.git")
                    developerConnection.set("scm:git:ssh://github.com/Monkopedia/kplusplus.git")
                    url.set("http://github.com/Monkopedia/kplusplus/")
                }
            }
        }
    }
    repositories {
        maven(url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
            name = "OSSRH"
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    useGpgCmd()
}
