package com.monkopedia.krapper.generator

import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.IndexedService
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.KrapperService
import platform.posix.exit

class KrapperServiceImpl : KrapperService {
    private var config: KrapperConfig? = null

    override suspend fun ping(message: String): String {
        return "Krapper ping $message"
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
