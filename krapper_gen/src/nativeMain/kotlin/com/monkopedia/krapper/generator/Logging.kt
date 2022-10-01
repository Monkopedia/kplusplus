package com.monkopedia.krapper.generator

import com.monkopedia.krapper.RemoteLogger

object StdOutLogger : RemoteLogger {
    override suspend fun e(message: String) {
        println(message)
    }

    override suspend fun i(message: String) {
        println(message)
    }

    override suspend fun w(message: String) {
        println(message)
    }
}

object Log {
    var loggerImpl: RemoteLogger = StdOutLogger

    suspend fun w(str: String) {
        loggerImpl.w(str)
    }

    suspend fun e(str: String) {
        loggerImpl.e(str)
    }

    suspend fun i(str: String) {
        loggerImpl.i(str)
    }
}