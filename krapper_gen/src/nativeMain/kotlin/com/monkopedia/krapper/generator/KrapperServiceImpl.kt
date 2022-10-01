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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.IndexedService
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.KrapperService
import com.monkopedia.krapper.RemoteLogger
import platform.posix.exit

class KrapperServiceImpl : KrapperService {
    private var config: KrapperConfig? = null

    override suspend fun ping(message: String): String {
        return "Krapper ping $message"
    }

    override suspend fun setLogger(logger: RemoteLogger) {
        Log.loggerImpl = logger
    }

    override suspend fun setConfig(config: KrapperConfig) {
        this.config = config
    }

    override suspend fun getConfig(u: Unit): KrapperConfig {
        return config ?: error("Config has not been set")
    }

    override suspend fun index(request: IndexRequest): IndexedService {
        return IndexedServiceImpl(getConfig(Unit), request)
    }

    override suspend fun quit(u: Unit) {
        exit(0)
    }
}
