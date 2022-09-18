package com.monkopedia.krapper

import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.annotation.KsMethod
import com.monkopedia.ksrpc.annotation.KsService
import kotlinx.serialization.Serializable

@Serializable
sealed class MapResult

object RemoveChild : MapResult()
object RemoveParent : MapResult()
object NoChange : MapResult()

data class ReplaceChild(val newChild: ResolvedElement) : MapResult()
data class ReplaceParent(val newChild: ResolvedElement) : MapResult()
data class AddToParent(val newChild: ResolvedElement) : MapResult()
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
    suspend fun getFilter(u: Unit): FilterDefinition

    @KsMethod("/map_element")
    suspend fun mapElement(request: MapRequest): List<MapResult>
}
