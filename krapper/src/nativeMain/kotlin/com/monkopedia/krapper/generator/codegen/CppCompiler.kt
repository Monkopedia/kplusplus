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

    internal companion object {
        // The `<file>:<line>:<col>: <severity>: <message>` line every C++ compiler emits for a
        // positioned diagnostic. Everything else in the log (caret art, "N errors generated")
        // is not a diagnostic and stays in the exception text.
        val DIAGNOSTIC_LINE =
            Regex("""^(.+?):(\d+):(\d+): (fatal error|error|warning|note): (.*)$""")

        fun parseCompilerOutput(output: String): List<Diagnostic> = output.lineSequence()
            .mapNotNull { DIAGNOSTIC_LINE.matchEntire(it) }
            .map { match ->
                val (file, line, column, severity, message) = match.destructured
                val isError = severity.endsWith("error")
                Diagnostic(
                    severity = when {
                        isError -> Severity.ERROR
                        severity == "warning" -> Severity.WARNING
                        else -> Severity.INFO
                    },
                    message = message,
                    location = SourceLocation(file, line.toInt(), column.toInt()),
                    phase = DiagnosticPhase.COMPILE,
                    // Only an error carries the "so what" — a warning or a `note:` here is
                    // context for one of them, not an independent problem.
                    hint = "the generated wrapper for this declaration cannot be compiled"
                        .takeIf { isError }
                )
            }.toList()
    }
}
