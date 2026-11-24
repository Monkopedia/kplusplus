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
package com.monkopedia.krapper.generator

import com.github.ajalt.clikt.core.main
import com.monkopedia.krapper.AllowListFilter
import com.monkopedia.krapper.DefaultFilter
import com.monkopedia.krapper.Diagnostic
import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.KrapperService
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.RemoteLogger
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.resolvedmodel.resolvedSerializerModule
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.sockets.asConnection
import com.monkopedia.ksrpc.sockets.posixFileReadChannel
import com.monkopedia.ksrpc.sockets.posixFileWriteChannel
import com.monkopedia.ksrpc.toStub
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import platform.posix.SIGKILL
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.WNOHANG
import platform.posix.__environ
import platform.posix._exit
import platform.posix.close
import platform.posix.dup2
import platform.posix.execve
import platform.posix.fork
import platform.posix.getenv
import platform.posix.kill
import platform.posix.pipe
import platform.posix.readlink
import platform.posix.usleep
import platform.posix.waitpid

/**
 * #329 — `krapper -s` exits promptly once its driver closes the channel, after a failed call as
 * well as after a successful session.
 *
 * Before the fix it never did: the service mode's only exit path was the connection's
 * `onClose`, which ksrpc runs on a LOCAL close only — a peer closing the pipe ends the receive
 * loop without reaching it — so every session idled until the Gradle plugin's 10s grace ran out
 * and it was killed.
 *
 * The service is a real separate process, driven over pipes by a real ksrpc client exactly as
 * the plugin drives it (`setLogger`, `setConfig`, `index`, the generation calls, close). The
 * child is THIS test binary re-executed with [CHILD_ENV] set, whose [serviceChild] entry runs
 * the tool's own `Krapper().main(["-s"])` — the service mode the shipped binary runs — so the
 * cheap unit-test job need not link the 40MB executable to exercise it.
 */
class ServiceExitTest {

    @Test
    fun theServiceExitsPromptlyAfterAFailedCall() = assertExitsPromptlyAfter { krapper ->
        val missing = "/nonexistent/krapper329.h"
        val indexed = krapper.index(IndexRequest(listOf(missing), emptyList()))
        // Positive control: the call really failed, and the failure came back over the channel.
        val failure = assertFails { indexed.filterAndResolve(DefaultFilter) }
        assertTrue(missing in failure.message.orEmpty(), "unexpected: $failure")
    }

    @Test
    fun theServiceExitsPromptlyAfterASuccessfulSession() = assertExitsPromptlyAfter { krapper ->
        val dir = File.createTempDir("krapper_service_exit")
        try {
            val header = File(dir, "p.h")
            header.writeText("#pragma once\nnamespace p { struct A { int x; }; }\n")
            val out = File(dir, "out")
            val indexed = krapper.index(IndexRequest(listOf(header.path), emptyList()))
            indexed.filterAndResolve(AllowListFilter(listOf("p::A")))
            indexed.writeTo(out.path)
            indexed.close()
            // Positive control: the session did the work, so it ran to its end.
            assertTrue(out.listFiles().isNotEmpty(), "the session wrote nothing to $out")
        } finally {
            dir.rmR()
        }
    }

    /** Inert in the suite's own run; in the re-executed child it IS the service. */
    @Test
    fun serviceChild() {
        if (getenv(CHILD_ENV) == null) return
        Krapper().main(arrayOf("-s"))
    }

    private fun assertExitsPromptlyAfter(session: suspend (KrapperService) -> Unit) {
        val child = spawnService()
        try {
            runBlocking {
                val env = ksrpcEnvironment(Json { serializersModule = resolvedSerializerModule }) {}
                val connection =
                    (posixFileReadChannel(child.stdout) to posixFileWriteChannel(child.stdin))
                        .asConnection(env)
                try {
                    val krapper = connection.defaultChannel().toStub<KrapperService, String>()
                    krapper.setLogger(SilentLogger)
                    krapper.setConfig(CONFIG)
                    session(krapper)
                } finally {
                    connection.close()
                }
            }
            val status = child.awaitExit(EXIT_BOUND)
            assertEquals(0, status, "krapper -s status $EXIT_BOUND after close (null: running)")
        } finally {
            // A child left running holds the test's stderr open, which wedges the Gradle task.
            child.kill()
        }
    }

    private class Child(val pid: Int, val stdin: Int, val stdout: Int) {
        private var reaped = false

        /** The wait status, once the child exits within [bound]; null if it is still running. */
        fun awaitExit(bound: Duration): Int? = memScoped {
            val status = alloc<IntVar>()
            val deadline = TimeSource.Monotonic.markNow() + bound
            while (deadline.hasNotPassedNow()) {
                if (waitpid(pid, status.ptr, WNOHANG) == pid) {
                    reaped = true
                    return status.value
                }
                usleep(POLL_MICROS)
            }
            null
        }

        fun kill() {
            if (reaped) return
            kill(pid, SIGKILL)
            waitpid(pid, null, 0)
            reaped = true
        }
    }

    /** Re-execute this binary as the service, its stdin/stdout on two fresh pipes. */
    private fun spawnService(): Child = memScoped {
        val toChild = allocArray<IntVar>(2).also { check(pipe(it) == 0) }
        val fromChild = allocArray<IntVar>(2).also { check(pipe(it) == 0) }
        val exe = selfExecutable()
        // Everything the child needs is built BEFORE the fork: between fork and exec it runs
        // only raw syscalls.
        val argv = cStrings(
            listOf(exe, "--ktest_logger=SILENT", "--ktest_filter=*.${::serviceChild.name}")
        )
        val inherited = generateSequence(0) { it + 1 }
            .map { __environ!![it] }
            .takeWhile { it != null }
            .map { it!!.toKString() }
        val envp = cStrings(inherited.toList() + "$CHILD_ENV=1")
        val pid = fork()
        check(pid >= 0) { "fork failed" }
        if (pid == 0) {
            dup2(toChild[0], STDIN_FILENO)
            dup2(fromChild[1], STDOUT_FILENO)
            // The child must not hold its own stdin's write end, or it never sees EOF.
            for (fd in intArrayOf(toChild[0], toChild[1], fromChild[0], fromChild[1])) close(fd)
            execve(exe, argv, envp)
            _exit(127)
        }
        close(toChild[0])
        close(fromChild[1])
        Child(pid, stdin = toChild[1], stdout = fromChild[0])
    }

    private fun MemScope.cStrings(strings: List<String>): CPointer<CPointerVar<ByteVar>> =
        allocArray<CPointerVar<ByteVar>>(strings.size + 1).also { array ->
            strings.forEachIndexed { i, s -> array[i] = s.cstr.ptr }
            array[strings.size] = null
        }

    private fun selfExecutable(): String = memScoped {
        val buffer = allocArray<ByteVar>(PATH_MAX)
        val written = readlink("/proc/self/exe", buffer, (PATH_MAX - 1).toULong())
        check(written > 0) { "readlink(/proc/self/exe) failed" }
        buffer[written] = 0
        buffer.toKString()
    }

    private object SilentLogger : RemoteLogger {
        override suspend fun e(message: String) = Unit
        override suspend fun i(message: String) = Unit
        override suspend fun w(message: String) = Unit
        override suspend fun diagnostics(batch: List<Diagnostic>) = Unit
    }

    private companion object {
        const val CHILD_ENV = "KRAPPER_SERVICE_EXIT_CHILD"
        const val PATH_MAX = 4096
        const val POLL_MICROS = 10_000u

        // The plugin's backstop is 10s; well under a second is what "promptly" means here, and
        // 2s leaves a loaded CI box room without letting the old idle-until-killed pass.
        val EXIT_BOUND = 2.seconds

        val CONFIG = KrapperConfig(
            pkg = "p",
            compiler = "clang++",
            moduleName = "P",
            errorPolicy = ErrorPolicy.LOG,
            referencePolicy = ReferencePolicy.IGNORE_MISSING,
            debug = false
        )
    }
}
