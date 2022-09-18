package com.monkopedia.krapper

import kotlinx.serialization.Serializable

enum class ErrorPolicy {
    FAIL,
    LOG
}

enum class ReferencePolicy {
    IGNORE_MISSING,
    OPAQUE_MISSING,
    THROW_MISSING,
    INCLUDE_MISSING
}

@Serializable
data class KrapperConfig(
    val pkg: String,
    val compiler: String,
    val moduleName: String,
    val errorPolicy: ErrorPolicy,
    val referencePolicy: ReferencePolicy,
    val debug: Boolean
)
