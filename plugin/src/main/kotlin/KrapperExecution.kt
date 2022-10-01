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

import com.monkopedia.krapper.KrapperService
import com.monkopedia.krapper.generator.resolved_model.resolvedSerializerModule
import com.monkopedia.ksrpc.ErrorListener
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.toStub
import java.io.File
import java.lang.ProcessBuilder.Redirect
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object KrapperExecution {
    suspend fun executeWithService(executable: File, execute: suspend (KrapperService) -> Unit) {
        withContext(Dispatchers.IO) {
            val process = ProcessBuilder()
                .command(executable.absolutePath, "-s")
            val startedProcess = process.redirectInput(Redirect.PIPE)
                .redirectOutput(Redirect.PIPE)
                .start()
            try {
                val input = startedProcess.inputStream
                val output = startedProcess.outputStream
                val connection = createConnection(
                    input,
                    output,
                    ksrpcEnvironment {
                        serialization = Json {
                            serializersModule = resolvedSerializerModule
                        }
                        errorListener = ErrorListener {
                            it.printStackTrace()
                        }
                    }
                )
                val service = connection.defaultChannel().toStub<KrapperService>()
                execute(service)
                try {
                    connection.close()
                } catch (t: CancellationException) {
                }
            } catch (t: Throwable) {
                println("Caught error $t")
                t.printStackTrace()
                throw t
            }
        }
    }
}
