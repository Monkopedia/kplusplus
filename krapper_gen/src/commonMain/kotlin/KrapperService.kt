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
package com.monkopedia.krapper

import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.annotation.KsMethod
import com.monkopedia.ksrpc.annotation.KsService
import kotlinx.serialization.Serializable

@Serializable
data class IndexRequest(
    val headers: List<String>,
    val libraries: List<String>,
    val headerDirectories: List<String> = headers.map { f ->
        f.split("/").let { it.subList(0, it.size - 1).joinToString("/") }
    }.distinct()
)

@KsService
interface KrapperService : RpcService {
    @KsMethod("/ping")
    suspend fun ping(message: String): String

    @KsMethod("/set_logger")
    suspend fun setLogger(logger: RemoteLogger)

    @KsMethod("/set_config")
    suspend fun setConfig(config: KrapperConfig)

    @KsMethod("/get_config")
    suspend fun getConfig(u: Unit): KrapperConfig

    @KsMethod("/index")
    suspend fun index(request: IndexRequest): IndexedService

    @KsMethod("/quit")
    suspend fun quit(u: Unit)
}

@KsService
interface IndexedService : RpcService {
    @KsMethod("/filter")
    suspend fun filterAndResolve(filter: FilterDefinition)

    @KsMethod("/execute")
    suspend fun addMapping(mappingService: MappingService)

    @KsMethod("/output")
    suspend fun writeTo(output: String)
}
