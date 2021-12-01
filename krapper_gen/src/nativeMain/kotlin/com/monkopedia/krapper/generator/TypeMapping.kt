package com.monkopedia.krapper.generator

import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedBase
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTypedef
import com.monkopedia.krapper.generator.model.type.WrappedModifiedType
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType

typealias TypeMapping = (WrappedType) -> MapResult<out WrappedType>

sealed class MapResult<T : WrappedElement>

object RemoveElement : MapResult<Nothing>()
object ElementUnchanged : MapResult<Nothing>()

data class ReplaceWith<T : WrappedElement>(val replacement: T) : MapResult<T>()

fun List<WrappedClass>.mapAll(
    typeMapper: TypeMapping
): List<WrappedClass> {
    return mapNotNull {
        when (val result = map(it, typeMapper)) {
            RemoveElement -> null
            ElementUnchanged -> it
            is ReplaceWith -> result.replacement
        }
    }
}

fun <T : WrappedElement> map(element: T, typeMapper: TypeMapping): MapResult<out T> {
    try {
        var needsMutation = false
        val resolvedChildren = element.children.mapNotNull {
            when (val result = map(it, typeMapper)) {
                RemoveElement -> null
                ElementUnchanged -> it
                is ReplaceWith -> {
                    needsMutation = true
                    result.replacement
                }
            }
        }
        when (element) {
            is WrappedTemplateType -> {
                val baseType = when (val result = map(element.baseType, typeMapper)) {
                    RemoveElement -> return RemoveElement
                    ElementUnchanged -> element.baseType
                    is ReplaceWith -> {
                        needsMutation = true
                        result.replacement
                    }
                }
                val arguments = element.templateArgs.map {
                    when (val result = map(it, typeMapper)) {
                        RemoveElement -> return RemoveElement
                        ElementUnchanged -> it
                        is ReplaceWith -> {
                            needsMutation = true
                            result.replacement
                        }
                    }
                }
                val type = if (needsMutation) {
                    WrappedTemplateType(baseType, arguments)
                } else element
                val newType = when (val result = typeMapper(type)) {
                    RemoveElement -> return RemoveElement
                    ElementUnchanged -> type
                    is ReplaceWith -> {
                        needsMutation = true
                        result.replacement
                    }
                }
                return if (needsMutation) ReplaceWith(newType as T) else ElementUnchanged
            }
            is WrappedPrefixedType -> {
                val baseType = when (val result = map(element.baseType, typeMapper)) {
                    RemoveElement -> return RemoveElement
                    ElementUnchanged -> element.baseType
                    is ReplaceWith -> {
                        needsMutation = true
                        result.replacement
                    }
                }
                val type = if (needsMutation) {
                    WrappedPrefixedType(baseType, element.modifier)
                } else element
                val newType = when (val result = typeMapper(type)) {
                    RemoveElement -> return RemoveElement
                    ElementUnchanged -> type
                    is ReplaceWith -> {
                        needsMutation = true
                        result.replacement
                    }
                }
                return if (needsMutation) ReplaceWith(newType as T) else ElementUnchanged
            }
            is WrappedModifiedType -> {
                val baseType = when (val result = map(element.baseType, typeMapper)) {
                        RemoveElement -> return RemoveElement
                    ElementUnchanged -> element.baseType
                    is ReplaceWith -> {
                        needsMutation = true
                        result.replacement
                    }
                }
                val type = if (needsMutation) {
                    WrappedModifiedType(baseType, element.modifier)
                } else element
                val newType = when (val result = typeMapper(type)) {
                    RemoveElement -> return RemoveElement
                    ElementUnchanged -> type
                    is ReplaceWith -> {
                        needsMutation = true
                        result.replacement
                    }
                }
                return if (needsMutation) ReplaceWith(newType as T) else ElementUnchanged
            }
            is WrappedType -> {
                return typeMapper(element) as MapResult<out T>
            }
            is WrappedMethod -> {
                val returnType = when (val result = map(element.returnType, typeMapper)) {
                    RemoveElement -> return RemoveElement
                    ElementUnchanged -> element.returnType
                    is ReplaceWith -> {
                        needsMutation = true
                        result.replacement
                    }
                }
                return if (needsMutation) ReplaceWith(
                    element.copy(returnType = returnType).also {
                        it.clearChildren()
                        it.addAllChildren(resolvedChildren)
                        it.parent = element.parent
                    } as T
                ) else ElementUnchanged
            }
            is WrappedBase -> {
                val mappedType =
                    when (val result = map(element.type ?: return RemoveElement, typeMapper)) {
                        RemoveElement -> return RemoveElement
                        ElementUnchanged -> element.type
                        is ReplaceWith -> {
                            needsMutation = true
                            result.replacement
                        }
                    }
                return if (needsMutation) ReplaceWith(
                    WrappedBase(mappedType).also {
                        it.clearChildren()
                        it.addAllChildren(resolvedChildren)
                        it.parent = element.parent
                    } as T
                ) else ElementUnchanged
            }
            is WrappedTypedef -> {
                val mappedType = when (val result = map(element.targetType, typeMapper)) {
                    RemoveElement -> return RemoveElement
                    ElementUnchanged -> element.targetType
                    is ReplaceWith -> {
                        needsMutation = true
                        result.replacement
                    }
                }
                return if (needsMutation) ReplaceWith(
                    WrappedTypedef(element.name, mappedType).also {
                        it.clearChildren()
                        it.addAllChildren(resolvedChildren)
                        it.parent = element.parent
                    } as T
                ) else ElementUnchanged
            }
            is WrappedField -> {
                val mappedType = when (val result = map(element.type, typeMapper)) {
                    RemoveElement -> return RemoveElement
                    ElementUnchanged -> element.type
                    is ReplaceWith -> {
                        needsMutation = true
                        result.replacement
                    }
                }
                return if (needsMutation) ReplaceWith(
                    WrappedField(element.name, mappedType).also {
                        it.clearChildren()
                        it.addAllChildren(resolvedChildren)
                        it.parent = element.parent
                    } as T
                ) else ElementUnchanged
            }
            is WrappedArgument -> {
                val mappedType = when (val result = map(element.type, typeMapper)) {
                    RemoveElement -> return RemoveElement
                    ElementUnchanged -> element.type
                    is ReplaceWith -> {
                        needsMutation = true
                        result.replacement
                    }
                }
                return if (needsMutation) ReplaceWith(
                    WrappedArgument(
                        element.name,
                        mappedType
                    ).also {
                        it.clearChildren()
                        it.addAllChildren(resolvedChildren)
                        it.parent = element.parent
                    } as T
                ) else ElementUnchanged
            }
        }
        if (!needsMutation) return ElementUnchanged

        return ReplaceWith(
            element.clone().also {
                it.clearChildren()
                it.addAllChildren(resolvedChildren)
                it.parent = element.parent
            } as T
        )
    } catch (t: Throwable) {
        throw IllegalArgumentException("Failed mapping $element", t)
    }
}
