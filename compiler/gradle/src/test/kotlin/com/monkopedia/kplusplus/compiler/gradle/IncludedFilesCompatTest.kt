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

import com.monkopedia.krapper.FilterDefinition
import com.monkopedia.krapper.Fixup
import com.monkopedia.krapper.IndexedService
import com.monkopedia.krapper.InstantiationRequest
import com.monkopedia.krapper.MappingService
import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.annotation.KsMethod
import com.monkopedia.ksrpc.annotation.KsService
import com.monkopedia.ksrpc.channels.registerDefault
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.sockets.asConnection
import com.monkopedia.ksrpc.toStub
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/** The `/output` half of the [IndexedService] krapper 0.3.7 ships: no `/included_files`. */
@KsService
interface LegacyIndexedService : RpcService {
    @KsMethod("/output")
    suspend fun writeTo(output: String)
}

/**
 * The include set crosses a release boundary (#297): the plugin and the krapper it drives can be
 * a release apart, so each pairing must keep working over the real channel.
 */
class IncludedFilesCompatTest {

    private val written = mutableListOf<String>()

    private val dir = createTempDirectory("kpp-includes").toFile()

    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    /** Host [service] on one end of a pipe pair and hand [client] a [T] stub of the other. */
    private inline fun <reified H : RpcService, reified T : RpcService> session(
        service: H,
        crossinline client: suspend (T) -> Unit
    ) = runBlocking {
        val env = ksrpcEnvironment(Json) { }
        val toServer = PipedOutputStream()
        val toClient = PipedOutputStream()
        val server = (PipedInputStream(toServer) to toClient).asConnection(env)
        server.registerDefault(service)
        val connection = (PipedInputStream(toClient) to toServer).asConnection(env)
        try {
            client(connection.defaultChannel().toStub<T, String>())
        } finally {
            connection.close()
            server.close()
        }
    }

    private val legacy = object : LegacyIndexedService {
        override suspend fun writeTo(output: String) {
            written += output
        }
    }

    private val current = object : IndexedService {
        override suspend fun filterAndResolve(filter: FilterDefinition) = Unit
        override suspend fun addMapping(mappingService: MappingService) = Unit
        override suspend fun requestInstantiation(request: InstantiationRequest) = Unit
        override suspend fun applyFixups(fixups: List<Fixup>) = Unit
        override suspend fun writeTo(output: String) {
            written += output
        }
        override suspend fun includedFiles(u: Unit) = listOf("/inc/inner.h")
    }

    @Test
    fun a_new_plugin_reads_the_include_set_from_a_new_krapper() =
        session<IndexedService, IndexedService>(current) {
            assertEquals(listOf("/inc/inner.h"), it.includedFilesIfKnown())
        }

    // The old krapper answers endpoint-not-found, and the session carries on after it.
    @Test
    fun a_new_plugin_gets_no_include_set_from_an_old_krapper() =
        session<LegacyIndexedService, IndexedService>(legacy) {
            assertNull(it.includedFilesIfKnown())
            it.writeTo("out")
            assertEquals(listOf("out"), written, "the session must survive the unknown call")
        }

    @Test
    fun an_old_plugin_drives_a_new_krapper() =
        session<IndexedService, LegacyIndexedService>(current) {
            it.writeTo("out")
            assertEquals(listOf("out"), written)
        }

    // And with no include set recorded, the declared inputs alone decide, as before #297.
    @Test
    fun no_recorded_include_set_leaves_the_sync_to_its_declared_inputs() {
        assertTrue(includesUnchanged(File(dir, "includes.txt")))
    }

    @Test
    fun a_recorded_file_edited_after_the_run_started_is_out_of_date() {
        val header = File(dir, "inner.h").apply { writeText("v1\n") }
        val record = File(dir, "includes.txt").apply { writeText("${header.path}\n") }
        header.setLastModified(record.lastModified() - 2000)
        assertTrue(includesUnchanged(record), "control: nothing changed since the run")
        header.setLastModified(record.lastModified() + 2000)
        assertTrue(!includesUnchanged(record), "an edited include must be out of date")
        header.delete()
        assertTrue(!includesUnchanged(record), "a deleted include must be out of date")
    }
}
