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

import com.monkopedia.ksrpc.RpcBidiService
import com.monkopedia.ksrpc.annotation.KsMethod
import com.monkopedia.ksrpc.annotation.KsService
import kotlinx.serialization.Serializable

@Serializable
data class IndexRequest(
    val headers: List<String>,
    val libraries: List<String>,
    val headerDirectories: List<String> = headers.map { f ->
        f.split("/").let { it.subList(0, it.size - 1).joinToString("/") }
    }.distinct(),
    /**
     * Extra `-I` roots for the PARSE (the module's `headerDirectory(...)` plus the
     * consumer's LLVM include dir). Distinct from [headerDirectories], which additionally
     * carries each header's own parent dir and drives the WRAPPER COMPILE's `-I` set — the
     * parse must not silently gain those, so the two stay separate.
     *
     * Carried on the request rather than set as a CLI-scoped global, so a caller driving
     * krapper over the service channel configures the parse the same way the CLI does.
     */
    val includeDirs: List<String> = emptyList(),
    /** Debug aid: write each parsed tree to this dir as ModelIo JSON (`--dump-model`). */
    val dumpModelDir: String? = null
)

@Serializable
data class InstantiationRequest(val base: String, val args: List<String>) {
    /**
     * The canonical `base<arg, arg>` specialization spelling. This is the string every
     * consumer keys instantiations on (forcing-model paths, the requested set), so it lives
     * here to keep those sites in lockstep instead of being hand-rebuilt at each one.
     */
    val spec: String get() = "$base<${args.joinToString(", ")}>"
}

@KsService
interface KrapperService : RpcBidiService {
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
interface IndexedService : RpcBidiService {
    @KsMethod("/filter")
    suspend fun filterAndResolve(filter: FilterDefinition)

    @KsMethod("/execute")
    suspend fun addMapping(mappingService: MappingService)

    @KsMethod("/instantiate")
    suspend fun requestInstantiation(request: InstantiationRequest)

    /**
     * Register the declarative [Fixup] directives for this run (#185).
     *
     * [addMapping] can express the same thing, but only by hosting the mapper on the CALLER's
     * side of the channel — and krapper then calls back per matching element, which for a
     * remote (Gradle-plugin) caller means a round trip and a full element tree on the wire for
     * every method in the model. Fixups are DATA, so they travel as data and are applied
     * in-process; [addMapping] remains for a caller that genuinely needs to run its own code.
     */
    @KsMethod("/fixups")
    suspend fun applyFixups(fixups: List<Fixup>)

    @KsMethod("/output")
    suspend fun writeTo(output: String)
}
