package com.monkopedia.krapper.generator.resolved_model.type

interface FqSymbol {
    val fqNames: List<String>

    fun setNameRemap(map: Map<String, String>)
}
