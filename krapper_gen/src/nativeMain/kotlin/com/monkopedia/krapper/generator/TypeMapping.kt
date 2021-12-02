package com.monkopedia.krapper.generator

import com.monkopedia.krapper.generator.model.MethodType.CONSTRUCTOR
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

typealias TypeMapping = (WrappedType, WrappedElement) -> MapResult<out WrappedType>

sealed class MapResult<T : WrappedElement>

object RemoveElement : MapResult<Nothing>()
object ElementUnchanged : MapResult<Nothing>()

data class ReplaceWith<T : WrappedElement>(val replacement: T) : MapResult<T>()

fun List<WrappedClass>.mapAll(
    typeMapper: TypeMapping
): List<WrappedClass> {
    return mapNotNull {
        when (val result = map(it, null, typeMapper)) {
            RemoveElement -> null
            ElementUnchanged -> it
            is ReplaceWith -> result.replacement
        }
    }
}

fun <T : WrappedElement> map(
    element: T,
    parent: WrappedElement?,
    typeMapper: TypeMapping
): MapResult<out T> {
    return mapInternal(element, parent, typeMapper)
}

fun <T : WrappedElement> mapInternal(
    element: T,
    parent: WrappedElement?,
    typeMapper: TypeMapping
): MapResult<out T> {
    try {
        var needsMutation = false
        val resolvedChildren = element.children.mapNotNull {
            when (val result = map(it, element, typeMapper)) {
                RemoveElement -> {
                    needsMutation = true
                    null
                }
                ElementUnchanged -> it
                is ReplaceWith -> {
                    needsMutation = true
                    result.replacement
                }
            }
        }
        when (element) {
            is WrappedTemplateType -> {
                val arguments = element.templateArgs.map {
                    when (val result = map(it, element, typeMapper)) {
                        RemoveElement -> return@mapInternal RemoveElement
                        ElementUnchanged -> it
                        is ReplaceWith -> {
                            needsMutation = true
                            result.replacement
                        }
                    }
                }
                val baseType =
                    when (val result = map(element.baseType, element, typeMapper)) {
                        RemoveElement -> return RemoveElement
                        ElementUnchanged -> element.baseType
                        is ReplaceWith -> {
                            needsMutation = true
                            result.replacement
                        }
                    }
                val type = if (needsMutation) {
                    WrappedTemplateType(baseType, arguments)
                } else element
                val newType = when (val result = typeMapper(type, element)) {
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
                val baseType =
                    when (val result = map(element.baseType, element, typeMapper)) {
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
                val newType = when (val result = typeMapper(type, element)) {
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
                val baseType =
                    when (val result = map(element.baseType, element, typeMapper)) {
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
                val newType = when (val result = typeMapper(type, element)) {
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
                return typeMapper(
                    element,
                    parent ?: error("WrappedType cannot be mapped directly without a parent")
                ) as MapResult<out T>
            }
            is WrappedMethod -> {
                if (element.args.size != resolvedChildren.filterIsInstance<WrappedArgument>().size) {
                    if (element.methodType == CONSTRUCTOR && (parent as? WrappedClass)?.name.toString().contains("TestClass")) {
                        println("Removing constructor $element\nargs: ${element.args}\nresolved: $resolvedChildren")
                    }
                    return RemoveElement
                }
                val returnType =
                    when (val result = map(element.returnType, element, typeMapper)) {
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
                    }.also {
                        if (element.name == "swap") {
                            println("Mapping swap $element $it inside $parent")
                        }
                    } as T
                ) else ElementUnchanged
            }
            is WrappedBase -> {
                val mappedType =
                    when (
                        val result =
                            map(element.type ?: return RemoveElement, element, typeMapper)
                    ) {
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
                val mappedType =
                    when (val result = map(element.targetType, element, typeMapper)) {
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
                val mappedType = when (val result = map(element.type, element, typeMapper)) {
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
                val mappedType =
                    when (val result = map(element.type, element, typeMapper)) {
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
            is WrappedClass -> {
                val specifiedType = element.specifiedType?.let { type ->
                    when (val result = map(type, element, typeMapper)) {
                        RemoveElement -> return RemoveElement
                        ElementUnchanged -> type
                        is ReplaceWith -> {
                            needsMutation = true
                            result.replacement
                        }
                    }
                }
                return if (needsMutation) ReplaceWith(
                    element.clone(specifiedType).also {
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
