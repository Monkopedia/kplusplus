package com.monkopedia.krapper

import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedType
import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.annotation.KsMethod
import com.monkopedia.ksrpc.annotation.KsService
import kotlinx.serialization.Serializable

@Serializable
sealed class MapResult

@Serializable
object RemoveChild : MapResult()
@Serializable
object RemoveParent : MapResult()
@Serializable
object NoChange : MapResult()

@Serializable
data class ReplaceChild(val newChild: ResolvedElement) : MapResult()
@Serializable
data class ReplaceParent(val newChild: ResolvedElement) : MapResult()
@Serializable
data class AddToParent(val newChild: ResolvedElement) : MapResult()
@Serializable
data class AddToChild(val newChild: ResolvedElement) : MapResult()

@Serializable
data class MapRequest(
    val parent: ResolvedElement,
    val childIndex: Int
) {
    val child: ResolvedElement
        get() = if (childIndex >= 0) parent.children[childIndex] else parent
}

@KsService
interface MappingService : RpcService {

    @KsMethod("/get_filter")
    suspend fun getFilter(resolver: ResolverService): FilterDefinition

    @KsMethod("/map_element")
    suspend fun mapElement(request: MapRequest): List<MapResult>
}

@KsService
interface ResolverService : RpcService {
    @KsMethod("/resolve")
    suspend fun resolvedType(typeStr: String): ResolvedType

    @KsMethod("/resolve_kotlin")
    suspend fun resolvedKotlinType(typeStr: String): ResolvedKotlinType

    @KsMethod("/resolve_c")
    suspend fun resolvedCType(typeStr: String): ResolvedCType
}
