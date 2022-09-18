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
    id("com.gradle.plugin-publish") version "1.0.0-rc-1"
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

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    implementation("com.google.guava:guava:31.1-jre")
    implementation(project(":krapper_gen"))
    implementation("com.monkopedia:ksrpc:0.6.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testApi("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.1-native-mt")
}

java {
    withJavadocJar()
    withSourcesJar()
}

gradlePlugin {
    val kplusplus by plugins.creating {
        id = "com.monkopedia.kplusplus.plugin"
        implementationClass = "com.monkopedia.kplusplus.KPlusPlusPlugin"
        displayName = "K++ Gradle Plugin"
        description = project.description
    }
}

//val functionalTestSourceSet = sourceSets.create("functionalTest") {
//}

//configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

//// Add a task to run the functional tests
//val functionalTest by tasks.registering(Test::class) {
    //testClassesDirs = functionalTestSourceSet.output.classesDirs
    //classpath = functionalTestSourceSet.runtimeClasspath
    //dependsOn(
        //rootProject.findProject(":krapper_gen")?.tasks?.findByName(":linkDebugExecutableNative")
    //)
//}

//gradlePlugin.testSourceSets(functionalTestSourceSet)

//tasks.named<Task>("check") {
    //dependsOn(functionalTest)
//}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
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

pluginBundle {
    website = "https://github.com/monkopedia/kplusplus"
    vcsUrl = "https://github.com/monkopedia/kplusplus"

    description = project.description
    tags = listOf("kotlin", "kotlin/native", "c++", "interop")
}

signing {
    useGpgCmd()
}
