/*
 * Copyright 2026 Jason Monk
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
package com.monkopedia.kplusplus.compiler.gradle

import com.monkopedia.krapper.AllowListFilter
import com.monkopedia.krapper.DefaultFilter
import com.monkopedia.krapper.Diagnostic
import com.monkopedia.krapper.Fixup
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.IndexedService
import com.monkopedia.krapper.InstantiationRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.KrapperService
import com.monkopedia.krapper.RemoteLogger
import com.monkopedia.krapper.Severity
import com.monkopedia.krapper.generator.resolvedmodel.resolvedSerializerModule
import com.monkopedia.ksrpc.ErrorListener
import com.monkopedia.ksrpc.channels.Connection
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.sockets.asConnection
import com.monkopedia.ksrpc.toStub
import java.io.File
import java.util.Collections
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.gradle.api.logging.Logger

/**
 * What one `kplusplusSync` run asks krapper for. The same inputs the CLI took as flags, as
 * data — every field is forwarded over the service channel rather than spelled into argv.
 */
internal class KrapperJob(
    val moduleName: String,
    val config: KrapperConfig,
    val request: IndexRequest,
    /** Fully-qualified class names to bind; empty means "bind everything non-std". */
    val allowList: List<String>,
    /** Template specializations to force, e.g. `std::vector<int>`. */
    val instantiations: List<String>,
    val fixups: List<Fixup>,
    val outputDir: File
)

/** What krapper reported while running a [KrapperJob]. */
internal class KrapperRunReport(val diagnostics: List<Diagnostic>) {

    /** `N diagnostic(s) (E error, W warning)` — the one-line tally for a completed run. */
    fun summary(): String {
        if (diagnostics.isEmpty()) return "no diagnostics."
        val bySeverity = Severity.entries
            .mapNotNull { severity ->
                diagnostics.count { it.severity == severity }
                    .takeIf { it > 0 }
                    ?.let { "$it ${severity.name.lowercase()}" }
            }
        return "${diagnostics.size} diagnostic(s) (${bySeverity.joinToString(", ")})."
    }

    /**
     * The diagnostics, rendered, for a failure message — errors first (they explain the
     * failure), `INFO` context dropped, and capped, because a broad import can drop hundreds
     * of symbols and an unreadable exception helps nobody. The full set was already logged.
     */
    fun detail(): String {
        val ordered = diagnostics
            .filter { it.severity != Severity.INFO }
            .sortedByDescending { it.severity.ordinal }
        if (ordered.isEmpty()) return ""
        return ordered.take(MAX_DETAIL).joinToString(
            prefix = "\n\nkrapper reported ${summary().removeSuffix(".")}:\n  ",
            separator = "\n  ",
            postfix = if (ordered.size > MAX_DETAIL) {
                "\n  ... and ${ordered.size - MAX_DETAIL} more (re-run with --info)."
            } else {
                ""
            }
        ) { it.render() }
    }

    private companion object {
        const val MAX_DETAIL = 20
    }
}

/**
 * A krapper run that threw, carrying whatever it had already reported. The diagnostics are
 * the actionable part — the exception message alone is what the exit-code era already had.
 */
internal class KrapperRunException(
    message: String,
    val report: KrapperRunReport,
    cause: Throwable
) : Exception(message, cause)

/**
 * Drives the krapper tool over a ksrpc service channel on its stdio pipe (#185).
 *
 * Before this, the plugin spawned krapper with `inheritIO()` and learned exactly one fact
 * about the run — its exit code — while everything krapper knew about WHY a binding wasn't
 * produced (the drop ledger, the C++ source position of each dropped decl, the wrapper
 * compile's own diagnostics) was flattened into interleaved subprocess stdout for a human to
 * read. Now the tool hosts its already-existing [KrapperService] on stdin/stdout (its `-s`
 * mode, which nothing used to reach), the plugin drives it with typed calls, and krapper
 * streams [Diagnostic]s back through the bidirectional [RemoteLogger] channel.
 *
 * Deliberately NOT JNI: loading krapper as a shared library into the Gradle daemon would put
 * a second Kotlin/Native runtime in the same JVM, which is an open question (the old
 * `JniBridge` "Spike #2" never answered it, and had no call sites). A pipe has none of that
 * risk and is fast enough — one process per sync, exactly as before.
 *
 * The session is per-run: the process starts on [run] and is torn down when it returns.
 * Keeping it alive across builds (and adding query methods to interrogate a live model) is
 * #186, deliberately not done here.
 */
internal object KrapperSession {

    fun run(
        binary: File,
        job: KrapperJob,
        logger: Logger,
        environment: Map<String, String> = emptyMap()
    ): KrapperRunReport = runBlocking {
        // The tool calls back on the RemoteLogger from its own coroutine, so the sink is
        // written from a different thread than the one that reads it after the run.
        val diagnostics = Collections.synchronizedList(mutableListOf<Diagnostic>())
        val sink = GradleLogSink(logger, job.moduleName, diagnostics)
        val connection = connect(binary, environment, logger)
        try {
            val krapper = connection.defaultChannel().toStub<KrapperService, String>()
            krapper.setLogger(sink)
            krapper.setConfig(job.config)
            val indexed = krapper.index(job.request)
            try {
                indexed.generate(job)
            } finally {
                runCatching { indexed.close() }
            }
        } catch (t: Throwable) {
            throw KrapperRunException(
                t.message ?: t::class.simpleName.orEmpty(),
                KrapperRunReport(diagnostics.toList()),
                t
            )
        } finally {
            // Closing the connection closes the tool's stdio, which is how its service mode
            // is told to exit (App.runService's `connection.onClose { exitProcess(0) }`).
            // `quit` would exit the process mid-call and leave this side awaiting a response
            // that can never arrive.
            runCatching { connection.close() }
        }
        KrapperRunReport(diagnostics.toList())
    }

    /** The generation flow — the same sequence the CLI's `run()` performs in-process. */
    private suspend fun IndexedService.generate(job: KrapperJob) {
        if (job.request.headers.isNotEmpty()) {
            filterAndResolve(
                if (job.allowList.isEmpty()) DefaultFilter else AllowListFilter(job.allowList)
            )
        }
        for (spec in job.instantiations) {
            requestInstantiation(spec.toInstantiationRequest())
        }
        applyFixups(job.fixups)
        writeTo(job.outputDir.absolutePath)
    }

    private suspend fun connect(
        binary: File,
        environment: Map<String, String>,
        logger: Logger
    ): Connection<String> {
        val env = ksrpcEnvironment(Json { serializersModule = resolvedSerializerModule }) {
            // Channel-level failures (a torn-down pipe during teardown, a serialization
            // mismatch) are already surfaced to the caller as a thrown RPC exception; this
            // listener only keeps the detail available under --info.
            errorListener = ErrorListener { logger.info("krapper channel error: $it") }
        }
        return ProcessBuilder(binary.absolutePath, "-s")
            // stdin/stdout ARE the protocol; stderr stays the tool's (and Clang's) own
            // channel and is inherited so a parse abort is still visible verbatim.
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .also { it.environment().putAll(environment) }
            .asConnection(env)
    }
}

/**
 * `Base<Arg, Arg>` -> the request krapper keys instantiations on. A bare name with no `<`
 * is a zero-argument instantiation.
 */
internal fun String.toInstantiationRequest(): InstantiationRequest {
    val lt = indexOf('<')
    if (lt < 0) return InstantiationRequest(trim(), emptyList())
    val args = substring(lt + 1, lastIndexOf('>'))
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    return InstantiationRequest(substring(0, lt).trim(), args)
}

/**
 * The plugin's end of the bidirectional logging channel.
 *
 * krapper's progress narration lands at `info` — it is the wall of text the sync used to
 * dump on every build, and it is now available with `--info` instead of unconditionally.
 * Structured [Diagnostic]s are what stays visible: each is logged at its own severity in the
 * `file:line:col: severity: message` shape compilers use, which IDEs turn into a clickable
 * marker, and collected so a failure can name them.
 */
private class GradleLogSink(
    private val logger: Logger,
    private val moduleName: String,
    private val sink: MutableList<Diagnostic>
) : RemoteLogger {

    override suspend fun i(message: String) = logger.info(prefixed(message))

    override suspend fun w(message: String) = logger.warn(prefixed(message))

    override suspend fun e(message: String) = logger.error(prefixed(message))

    override suspend fun diagnostics(batch: List<Diagnostic>) {
        sink.addAll(batch)
        for (diagnostic in batch) {
            val rendered = prefixed(diagnostic.render())
            when {
                diagnostic.severity == Severity.ERROR -> logger.error(rendered)
                // A broad import legitimately drops symbols in the hundreds. Surface enough
                // to be actionable, then stop shouting — the rest stay at info, and every
                // one of them is still in the collected list a failure reports from.
                diagnostic.severity == Severity.WARNING && warned++ < MAX_WARNINGS ->
                    logger.warn(rendered)

                diagnostic.severity == Severity.WARNING && warned == MAX_WARNINGS + 1 -> {
                    logger.info(rendered)
                    logger.warn(
                        prefixed(
                            "... further warnings suppressed; re-run with --info to see all."
                        )
                    )
                }

                else -> logger.info(rendered)
            }
        }
    }

    private var warned = 0

    private fun prefixed(message: String) = "kplusplus[$moduleName]: $message"

    private companion object {
        const val MAX_WARNINGS = 25
    }
}
