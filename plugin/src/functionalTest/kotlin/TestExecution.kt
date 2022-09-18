package com.monkopedia.kplusplus.plugin

import com.monkopedia.krapper.KrapperService
import com.monkopedia.ksrpc.ErrorListener
import com.monkopedia.ksrpc.KsrpcEnvironment
import com.monkopedia.ksrpc.channels.Connection
import com.monkopedia.ksrpc.channels.asConnection
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.toStub
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class TestExecution {

    @Test
    fun testExecution(): Unit = runBlocking {
        withContext(Dispatchers.IO) {
            try {
                val executable =
                    File("../krapper_gen/build/bin/native/debugExecutable/krapper_gen.kexe")
                assertTrue(executable.exists())
                val process = ProcessBuilder()
                    .command(executable.absolutePath, "-s")
                val connection = process.asConnection(
                    ksrpcEnvironment {
                        errorListener = ErrorListener {
                            it.printStackTrace()
                        }
                    }
                )
                val service = connection.defaultChannel().toStub<KrapperService>()
                val ping = service.ping("A message")
                println("Response: $ping")
                connection.close()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    private suspend fun ProcessBuilder.asConnectionLogger(env: KsrpcEnvironment): Connection {
        val process = redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
        process.onExit().whenComplete { process, throwable ->
            println("Process exit ${process.exitValue()} ${throwable.stackTraceToString()}")
        }
        val input = process.inputStream
        val pipedInputInput = PipedInputStream()
        val pipedInputOutput = PipedOutputStream(pipedInputInput)
        thread(start = true) {
            input.copyTo(pipedInputOutput)
//            while (true) {
//                val byte = input.read()
//                if (byte == -1) continue
//                println("Read byte $byte (${byte.toChar()})")
//                pipedInputOutput.write(byte)
//                pipedInputOutput.flush()
//            }
        }
        val output = process.outputStream
        val pipedOutputInput = PipedInputStream()
        val pipedOutputOutput = PipedOutputStream(pipedOutputInput)
        thread(start = true) {
//            pipedOutputInput.copyTo(output)
            while (true) {
                val byte = pipedOutputInput.read()
                if (byte == -1) continue
                println("Write byte $byte (${byte.toChar()})")
                output.write(byte)
                output.flush()
            }
        }
        return (pipedInputInput to pipedOutputOutput).asConnection(env)
    }
}
