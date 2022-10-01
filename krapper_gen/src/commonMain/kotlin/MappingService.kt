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
