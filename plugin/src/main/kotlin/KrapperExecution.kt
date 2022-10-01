package com.monkopedia.kplusplus

import com.monkopedia.krapper.KrapperService
import com.monkopedia.krapper.generator.resolved_model.resolvedSerializerModule
import com.monkopedia.ksrpc.ErrorListener
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.toStub
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.ProcessBuilder.Redirect

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