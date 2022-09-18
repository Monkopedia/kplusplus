package com.monkopedia.krapper

import com.monkopedia.krapper.generator.resolved_model.ResolvedElement

suspend inline fun IndexedService.addMapping(
    crossinline filter: FilterDsl.() -> FilterDefinition,
    crossinline handler: (MapRequest) -> List<MapResult>
) {
    addMapping(mapping(filter, handler))
}

suspend inline fun <T : ResolvedElement> IndexedService.addTypedMapping(
    typeTarget: TypeTarget<T>,
    crossinline filter: FilterDsl.() -> FilterDefinition,
    crossinline handler: MappingScope.(T) -> Unit
) {
    addMapping(object : Mapping<T>(
        filter {
            (thiz isType typeTarget) and filter()
        }
    ) {
            override fun runMapping(element: T) {
                handler(element)
            }
        })
}

inline fun mapping(
    crossinline filter: FilterDsl.() -> FilterDefinition,
    crossinline handler: (MapRequest) -> List<MapResult>
): MappingService {
    return object : MappingService {
        override suspend fun getFilter(u: Unit): FilterDefinition {
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
}

abstract class Mapping<T>(private val filter: FilterDefinition) : MappingScope, MappingService {

    private val modifications = mutableListOf<MapResult>()
    private var currentElement: ResolvedElement? = null

    override suspend fun getFilter(u: Unit): FilterDefinition = filter

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

    abstract fun runMapping(element: T)
}
