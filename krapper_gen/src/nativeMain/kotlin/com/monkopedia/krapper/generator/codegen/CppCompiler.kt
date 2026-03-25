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
package com.monkopedia.krapper.generator.codegen

import platform.posix.remove
import platform.posix.system

class CppCompiler(private val outputFile: File, private val compiler: String) {

    fun compile(cppFile: File, header: List<String>, library: List<String>) {
        val flags = CompileFlags(header, library, linkStatics = true)
        val command = "$compiler -c -fPIE -o ${outputFile.path} ${flags.includeDirs ?: ""} " +
            "${flags.linkerOpts ?: ""} ${cppFile.path}"
        val logFile = "${outputFile.path}.compile.log"
        val wrappedCommand = "$command >$logFile 2>&1"
        val result = system(wrappedCommand)
        val output = try {
            File(logFile).readText()
        } catch (_: Throwable) {
            ""
        }
        remove(logFile)
        require(result == 0) {
            "Compilation failed (exit $result):\n$command\n\n$output"
        }
    }
}
