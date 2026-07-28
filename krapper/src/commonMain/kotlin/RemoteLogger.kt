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

@KsService
interface RemoteLogger : RpcService {
    @KsMethod("/e")
    suspend fun e(message: String)

    @KsMethod("/i")
    suspend fun i(message: String)

    @KsMethod("/w")
    suspend fun w(message: String)

    /**
     * Report structured [Diagnostic]s back to whoever drove this run (#185).
     *
     * Batched rather than one call per diagnostic: a broad import drops symbols in the
     * hundreds, and each ksrpc call over the stdio pipe is a round trip. Krapper flushes a
     * batch at each point it has a coherent set to report (the end-of-run drop ledger, a
     * failing wrapper compile), so the consumer still sees them as the run progresses.
     */
    @KsMethod("/diagnostics")
    suspend fun diagnostics(batch: List<Diagnostic>)
}
