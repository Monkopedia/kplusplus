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

import com.monkopedia.krapper.Diagnostic
import com.monkopedia.krapper.DiagnosticPhase
import com.monkopedia.krapper.Severity
import com.monkopedia.krapper.SourceLocation
import com.monkopedia.krapper.generator.Log
import platform.posix.remove
import platform.posix.system

class CppCompiler(
    private val outputFile: File,
    private val compiler: String,
    private val cppStandard: String = "c++14"
) {

    suspend fun compile(
        cppFile: File,
        header: List<String>,
        library: List<String>,
        // Extra -I roots from the module's headerDirectory(...); see CompileFlags.
        extraIncludeDirs: List<String> = emptyList()
    ) {
        val flags = CompileFlags(
            header,
            library,
            linkStatics = true,
            extraIncludeDirs = extraIncludeDirs
        )
        // The wrapper compile must use the same standard as the libclang parse,
        // or C++17/20 types (std::string_view, char8_t) parse but fail to compile.
        val command = "$compiler -std=$cppStandard -c -fPIE -o ${outputFile.path} " +
            "${flags.includeDirs ?: ""} ${flags.linkerOpts ?: ""} ${cppFile.path}"
        val logFile = "${outputFile.path}.compile.log"
        val wrappedCommand = "$command >$logFile 2>&1"
        val result = system(wrappedCommand)
        val output = try {
            File(logFile).readText()
        } catch (_: Throwable) {
            ""
        }
        remove(logFile)
        if (result == 0) return
        // #185: the wrapper compile is where a binding gap actually BITES, and the compiler
        // already told us exactly where — hand the build the parsed positions instead of one
        // exit code plus a wall of text. The text is still in the exception for the CLI.
        Log.diagnostics(parseCompilerOutput(output))
        error("Compilation failed (exit $result):\n$command\n\n$output")
    }

    private companion object {
        // `<file>:<line>:<col>: <severity>: <message>` — the shape clang/gcc use for every
        // positioned diagnostic. Anything else in the log (the caret art, "N errors
        // generated") is not a diagnostic and is left to the exception text.
        val SEVERITIES = mapOf(
            "error" to Severity.ERROR,
            "fatal error" to Severity.ERROR,
            "warning" to Severity.WARNING,
            "note" to Severity.INFO
        )

        fun parseCompilerOutput(output: String): List<Diagnostic> = output.lineSequence()
            .mapNotNull { line ->
                val severity = SEVERITIES.entries.firstOrNull { (name, _) ->
                    ": $name: " in line
                } ?: return@mapNotNull null
                val marker = ": ${severity.key}: "
                val location = SourceLocation.parse(line.substringBefore(marker))
                    ?: return@mapNotNull null
                Diagnostic(
                    severity = severity.value,
                    message = line.substringAfter(marker),
                    location = location,
                    phase = DiagnosticPhase.COMPILE,
                    hint = "the generated C++ wrapper does not compile; the binding it " +
                        "belongs to cannot be produced"
                )
            }.toList()
    }
}
