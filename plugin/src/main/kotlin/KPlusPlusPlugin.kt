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
package com.monkopedia.kplusplus

import com.monkopedia.krapper.DefaultFilter
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.RemoteLogger
import java.io.File
import java.util.Properties
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.konan.target.KonanTarget

open class KPlusPlusPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create(
            "kplusplus",
            KPlusPlusExtension::class.java,
            KPlusPlusConfig(moduleName = target.name)
        )
        target.afterEvaluate {
            val ext = target.extensions.getByName("kplusplus") as KPlusPlusExtension
            val kotlinExt = target.extensions.getByType(KotlinMultiplatformExtension::class.java)

            val importsDir = File(target.buildDir, "krapped_imports").also {
                it.mkdirs()
            }
            for ((index, import) in ext.imports.withIndex()) {
                val compilations = import.compilations?.takeIf { it.isNotEmpty() }
                    ?: kotlinExt.targets.flatMap {
                        it.compilations.filterIsInstance<KotlinNativeCompilation>()
                    }
                compilations.forEach { compilation ->
                    val importName =
                        (import.name ?: "${target.name}${if (index != 0) index else ""}") +
                            compilation.name.capitalize()
                    val outputDir = File(importsDir, importName).also {
                        it.mkdirs()
                    }
                    val krapTask =
                        target.tasks.register(importName, RunKrapperGenTask::class.java) { task ->
                            task.target = compilation.konanTarget
                            task.config = ext.config
                            task.import = import
                            task.exeHome = File(target.buildDir, "krapperExe").also {
                                it.mkdirs()
                            }
                            task.outputDirectory = outputDir
                        }
                    compilation.cinterops { interops ->
                        interops.create(importName) { interop ->
                            interop.defFile = File(outputDir, "${ext.config.moduleName}.def")
                            val targetTaskName = interop.interopProcessingTaskName
                            target.tasks.all { task ->
                                if (task.name == targetTaskName) {
                                    task.dependsOn(krapTask)
                                    task.mustRunAfter(krapTask)
                                }
                            }
                            interop.interopProcessingTaskName
                        }
                    }
                    compilation.defaultSourceSet.kotlin.apply {
                        srcDir(File(outputDir, "src"))
                    }
                }
            }
        }
    }
}

abstract class RunKrapperGenTask : DefaultTask() {
    @Input
    var target: KonanTarget? = null

    @Nested
    var config: KPlusPlusConfig? = null

    @Nested
    var import: ImportConfig? = null

    @OutputDirectory
    var outputDirectory: File? = null

    @Internal
    var exeHome: File? = null

    @TaskAction
    fun execute() = try {
        runBlocking {
            val exe = KrapperGenExecutable.getExeFile(exeHome ?: error("Missing home"))
            KrapperExecution.executeWithService(exe) { service ->
                val config = config ?: error("Missing config")
                val import = import ?: error("Missing import")
                service.setLogger(object : RemoteLogger {
                    override suspend fun e(message: String) {
                        println(message)
                    }

                    override suspend fun i(message: String) {
                        println(message)
                    }

                    override suspend fun w(message: String) {
                        println(message)
                    }
                })
                service.setConfig(
                    KrapperConfig(
                        config.pkg ?: "krapper.${config.moduleName}",
                        config.compiler ?: findCompiler(),
                        config.moduleName,
                        config.errorPolicy,
                        config.referencePolicy,
                        config.debug
                    ).also {
                        println("Setting krapper config to $it")
                    }
                )
                val index = service.index(
                    IndexRequest(
                        import.headers.files.map { it.absolutePath },
                        import.library.files.map { it.absolutePath }
                    ).also {
                        println("Requesting index $it")
                    }
                )
                println("Filtering")
                index.filterAndResolve(import.classFilter ?: DefaultFilter)

                for (mapping in import.mappings) {
                    println("Adding mapping")
                    index.addMapping(mapping)
                }
                println("Writing output")
                index.writeTo((outputDirectory ?: error("No output directory")).absolutePath)
                println("Closing imdex")
                try {
                    index.close()
                } catch (t: Throwable) {
                }
            }
            println("Done with service")
        }
        println("Done with task")
    } catch (t: Throwable) {
        throw RuntimeException("Problem executing krapper", t)
    }

    private fun findCompiler(): String {
        val kotlinNativeDataPath = project.extensions.extraProperties.get("konanHome").toString()
        val konanDir = File(File(kotlinNativeDataPath), "konan")
        if (!konanDir.exists()) {
            throw GradleException("Can't find konan dir in $kotlinNativeDataPath")
        }
        val propertiesFile = File(konanDir, "konan.properties")
        if (!propertiesFile.exists()) {
            throw GradleException("Can't find konan properties in $kotlinNativeDataPath")
        }
        val props = Properties().also { props ->
            propertiesFile.inputStream().use {
                props.load(it)
            }
        }
        // /home/jmonk/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/bin/x86_64-unknown-linux-gnu-g++
        // ~/.konan/kotlin-native-prebuilt-linux-x86_64-1.7.10/konan/konan.properties
        val toolchain = props["gccToolchain.${target!!.name}"]
            ?: error("Can't find default gcc, please specify compiler manually")
        val konanHomeParent = konanDir.parentFile.parentFile
        val dependencies = File(konanHomeParent, "dependencies")
        val gccDir = File(dependencies, toolchain.toString())
        if (!gccDir.exists()) {
            error("Can't find default gcc, please specify compiler manually")
        }
        val gppFile = gccDir.walkBottomUp().find { file ->
            file.isFile && file.canExecute() && file.name.endsWith("g++")
        }
        if (gppFile == null || !gppFile.exists()) {
            error("Can't find default gcc, please specify compiler manually")
        }
        return gppFile.absolutePath
    }
}
