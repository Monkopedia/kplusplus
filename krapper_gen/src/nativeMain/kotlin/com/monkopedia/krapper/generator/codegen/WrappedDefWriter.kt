/*
 * Copyright 2021 Jason Monk
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
package com.monkopedia.krapper.generator.codegen

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.getcwd

class WrappedDefWriter(private val namer: NameHandler) {

    fun generateDef(
        outputBase: File,
        pkg: String,
        moduleName: String,
        headers: List<String>,
        libraries: List<String>
    ): String {
        return buildString {
            val flags = CompileFlags(
                listOf(File(outputBase, "$moduleName.h").path),
                libraries + File(outputBase, "lib$moduleName.a").path
            )
            appendLine("headers = ${flags.headerFiles.joinToString(" ") { it.name }}")
            flags.includeDirs?.let {
                appendLine("compilerOpts = $it")
            }
            flags.linkerOpts?.let {
                appendLine("linkerOpts = $it")
            }

            flags.libraryOpts?.let {
                appendLine("staticLibraries = $it")
            }
            flags.libraryPaths?.let {
                appendLine("libraryPaths = $it")
            }
            appendLine("package = $pkg")
        }
    }
}

fun getcwd(): String {
    return memScoped {
        val buffer = allocArray<ByteVar>(256)
        getcwd(buffer, 256)
        buffer.toKString()
    }
}
