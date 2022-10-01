package com.monkopedia.krapper

import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.annotation.KsMethod
import com.monkopedia.ksrpc.annotation.KsService

@KsService
interface RemoteLogger : RpcService {
    @KsMethod("/e")
    suspend fun e(message: String)
    @KsMethod("/i")
    suspend fun i(message: String)
    @KsMethod("/w")
    suspend fun w(message: String)
}