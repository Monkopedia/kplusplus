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

suspend inline fun IndexedService.addMapping(
    crossinline filter: FilterDsl.() -> FilterDefinition,
    crossinline handler: suspend (MapRequest) -> List<MapResult>
) {
    addMapping(mapping(filter, handler))
}

suspend inline fun <T : ResolvedElement> IndexedService.addTypedMapping(
    typeTarget: TypeTarget<T>,
    crossinline filter: FilterDsl.() -> FilterDefinition,
    crossinline handler: suspend MappingScope.(T) -> Unit
) {
    addMapping(typedMapping(typeTarget, filter, handler))
}

inline fun <T : ResolvedElement> typedMapping(
    typeTarget: TypeTarget<T>,
    crossinline filter: FilterDsl.() -> FilterDefinition,
    crossinline handler: suspend MappingScope.(T) -> Unit
) = object : Mapping<T>(
    filter {
        (thiz isType typeTarget) and filter()
    }
) {
    override suspend fun runMapping(element: T) {
        handler(element)
    }
}

inline fun mapping(
    crossinline filter: FilterDsl.() -> FilterDefinition,
    crossinline handler: suspend (MapRequest) -> List<MapResult>
): MappingService {
    return object : MappingService {
        override suspend fun getFilter(resolver: ResolverService): FilterDefinition {
            return filter(filter)
        }

        override suspend fun mapElement(request: MapRequest): List<MapResult> {
            return handler(request)
        }
    }
}

interface MappingScope {
    fun ResolvedElement.remove()
    fun ResolvedElement.replaceWith(other: ResolvedElement)
    fun ResolvedElement.add(newChild: ResolvedElement)

    suspend fun resolvedKotlinType(type: String): ResolvedKotlinType
    suspend fun resolvedCType(type: String): ResolvedCType
    suspend fun resolvedType(type: String): ResolvedType
}

abstract class Mapping<T>(private val filter: FilterDefinition) : MappingScope, MappingService {

    private val modifications = mutableListOf<MapResult>()
    private var currentElement: ResolvedElement? = null
    private var resolverService: ResolverService? = null

    override suspend fun getFilter(resolver: ResolverService): FilterDefinition = filter.also {
        this.resolverService = resolver
    }

    override suspend fun mapElement(request: MapRequest): List<MapResult> {
        modifications.clear()

        request.parent.setParents()
        currentElement = request.child
        runMapping((currentElement as? T) ?: error("${request.child} is not the expected type"))

        return modifications.toList()
    }

    override fun ResolvedElement.remove() {
        if (currentElement === this) {
            modifications.add(RemoveChild)
        } else if (currentElement?.parent === this) {
            modifications.add(RemoveParent)
        } else {
            val parent = parent ?: return
            parent.replaceWith(
                parent.cloneWithoutChildren().also {
                    for (child in parent.children) {
                        if (child === this@remove) {
//                        it.addChild(it)
                        } else {
                            it.addChild(child)
                        }
                    }
                }
            )
        }
    }

    override fun ResolvedElement.replaceWith(other: ResolvedElement) {
        if (currentElement === this) {
            modifications.add(ReplaceChild(other))
        } else if (currentElement?.parent === this) {
            modifications.add(ReplaceParent(other))
        } else {
            val parent = parent ?: return
            parent.replaceWith(
                parent.cloneWithoutChildren().also {
                    for (child in parent.children) {
                        if (child === this@replaceWith) {
                            it.addChild(other)
                        } else {
                            it.addChild(child)
                        }
                    }
                }
            )
        }
    }

    override fun ResolvedElement.add(other: ResolvedElement) {
        if (currentElement === this) {
            modifications.add(AddToChild(other))
        } else if (currentElement?.parent === this) {
            modifications.add(AddToParent(other))
        } else {
            val parent = parent ?: return
            parent.replaceWith(
                parent.cloneWithoutChildren().also {
                    for (child in parent.children) {
                        if (child === this@add) {
                            it.addChild(other)
                        } else {
                            it.addChild(child)
                        }
                    }
                }
            )
        }
    }

    override suspend fun resolvedCType(type: String): ResolvedCType {
        return resolverService?.resolvedCType(type) ?: error("Missing resolverService")
    }

    override suspend fun resolvedKotlinType(type: String): ResolvedKotlinType {
        return resolverService?.resolvedKotlinType(type) ?: error("Missing resolverService")
    }

    override suspend fun resolvedType(type: String): ResolvedType {
        return resolverService?.resolvedType(type) ?: error("Missing resolverService")
    }

    abstract suspend fun runMapping(element: T)
}
