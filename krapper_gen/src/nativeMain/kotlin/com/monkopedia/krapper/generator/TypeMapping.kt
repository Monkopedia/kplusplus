/*
 * Copyright 2021 Jason Monk
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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.generator.model.type.WrappedType

typealias TypeMapping = (WrappedType, ResolveContext) -> MapResult

sealed class MapResult

object RemoveElement : MapResult()
object ElementUnchanged : MapResult()

data class ReplaceWith(val replacement: WrappedType) : MapResult()

//fun List<WrappedClass>.mapAll(
//    typeMapper: TypeMapping
//): List<WrappedClass> {
//    return mapNotNull {
//        when (val result = map(it, null, typeMapper)) {
//            RemoveElement -> null
//            ElementUnchanged -> it
//            is ReplaceWith -> result.replacement
//        }
//    }
//}
//
//fun <T : WrappedElement> map(
//    element: T,
//    parent: WrappedElement?,
//    typeMapper: TypeMapping
//): MapResult<out T> {
//    return mapInternal(element, parent, typeMapper)
//}

//fun <T : WrappedElement> mapInternal(
//    element: T,
//    parent: WrappedElement?,
//    typeMapper: TypeMapping
//): MapResult<out T> {
//    try {
//        var needsMutation = false
//        val resolvedChildren = element.children.mapNotNull {
//            when (val result = map(it, element, typeMapper)) {
//                RemoveElement -> {
//                    needsMutation = true
//                    null
//                }
//                ElementUnchanged -> it
//                is ReplaceWith -> {
//                    needsMutation = true
//                    result.replacement
//                }
//            }
//        }
//        when (element) {
//            is WrappedTemplateType -> {
//                val arguments = element.templateArgs.map {
//                    when (val result = map(it, element, typeMapper)) {
//                        RemoveElement -> return@mapInternal RemoveElement
//                        ElementUnchanged -> it
//                        is ReplaceWith -> {
//                            needsMutation = true
//                            result.replacement
//                        }
//                    }
//                }
//                val baseType =
//                    when (val result = map(element.baseType, element, typeMapper)) {
//                        RemoveElement -> return RemoveElement
//                        ElementUnchanged -> element.baseType
//                        is ReplaceWith -> {
//                            needsMutation = true
//                            result.replacement
//                        }
//                    }
//                val type = if (needsMutation) {
//                    WrappedTemplateType(baseType, arguments)
//                } else element
//                val newType = when (val result = typeMapper(type, element)) {
//                    RemoveElement -> return RemoveElement
//                    ElementUnchanged -> type
//                    is ReplaceWith -> {
//                        needsMutation = true
//                        result.replacement
//                    }
//                }
//                return if (needsMutation) ReplaceWith(newType as T) else ElementUnchanged
//            }
//            is WrappedPrefixedType -> {
//                val baseType =
//                    when (val result = map(element.baseType, element, typeMapper)) {
//                        RemoveElement -> return RemoveElement
//                        ElementUnchanged -> element.baseType
//                        is ReplaceWith -> {
//                            needsMutation = true
//                            result.replacement
//                        }
//                    }
//                val type = if (needsMutation) {
//                    WrappedPrefixedType(baseType, element.modifier)
//                } else element
//                val newType = when (val result = typeMapper(type, element)) {
//                    RemoveElement -> return RemoveElement
//                    ElementUnchanged -> type
//                    is ReplaceWith -> {
//                        needsMutation = true
//                        result.replacement
//                    }
//                }
//                return if (needsMutation) ReplaceWith(newType as T) else ElementUnchanged
//            }
//            is WrappedModifiedType -> {
//                val baseType =
//                    when (val result = map(element.baseType, element, typeMapper)) {
//                        RemoveElement -> return RemoveElement
//                        ElementUnchanged -> element.baseType
//                        is ReplaceWith -> {
//                            needsMutation = true
//                            result.replacement
//                        }
//                    }
//                val type = if (needsMutation) {
//                    WrappedModifiedType(baseType, element.modifier)
//                } else element
//                val newType = when (val result = typeMapper(type, element)) {
//                    RemoveElement -> return RemoveElement
//                    ElementUnchanged -> type
//                    is ReplaceWith -> {
//                        needsMutation = true
//                        result.replacement
//                    }
//                }
//                return if (needsMutation) ReplaceWith(newType as T) else ElementUnchanged
//            }
//            is WrappedType -> {
//                return typeMapper(
//                    element,
//                    parent ?: error("WrappedType cannot be mapped directly without a parent")
//                ) as MapResult<out T>
//            }
//            is WrappedMethod -> {
//                val resolvedChildrenCount =
//                    resolvedChildren.filterIsInstance<WrappedArgument>().size
//                if (element.args.size != resolvedChildrenCount) {
//                    return RemoveElement
//                }
//                val returnType =
//                    when (val result = map(element.returnType, element, typeMapper)) {
//                        RemoveElement -> return RemoveElement
//                        ElementUnchanged -> element.returnType
//                        is ReplaceWith -> {
//                            needsMutation = true
//                            result.replacement
//                        }
//                    }
//                return if (needsMutation) ReplaceWith(
//                    element.copy(returnType = returnType).also {
//                        it.clearChildren()
//                        it.addAllChildren(resolvedChildren)
//                        it.parent = element.parent
//                    }.also {
//                        if (element.name == "swap") {
//                            println("Mapping swap $element $it inside $parent")
//                        }
//                    } as T
//                ) else ElementUnchanged
//            }
//            is WrappedBase -> {
//                val mappedType =
//                    when (
//                        val result =
//                            map(element.type ?: return RemoveElement, element, typeMapper)
//                    ) {
//                        RemoveElement -> return RemoveElement
//                        ElementUnchanged -> element.type
//                        is ReplaceWith -> {
//                            needsMutation = true
//                            result.replacement
//                        }
//                    }
//                return if (needsMutation) ReplaceWith(
//                    WrappedBase(mappedType).also {
//                        it.clearChildren()
//                        it.addAllChildren(resolvedChildren)
//                        it.parent = element.parent
//                    } as T
//                ) else ElementUnchanged
//            }
//            is WrappedTypedef -> {
//                val mappedType =
//                    when (val result = map(element.targetType, element, typeMapper)) {
//                        RemoveElement -> return RemoveElement
//                        ElementUnchanged -> element.targetType
//                        is ReplaceWith -> {
//                            needsMutation = true
//                            result.replacement
//                        }
//                    }
//                return if (needsMutation) ReplaceWith(
//                    WrappedTypedef(element.name, mappedType).also {
//                        it.clearChildren()
//                        it.addAllChildren(resolvedChildren)
//                        it.parent = element.parent
//                    } as T
//                ) else ElementUnchanged
//            }
//            is WrappedField -> {
//                val mappedType = when (val result = map(element.type, element, typeMapper)) {
//                    RemoveElement -> return RemoveElement
//                    ElementUnchanged -> element.type
//                    is ReplaceWith -> {
//                        needsMutation = true
//                        result.replacement
//                    }
//                }
//                return if (needsMutation) ReplaceWith(
//                    WrappedField(element.name, mappedType).also {
//                        it.clearChildren()
//                        it.addAllChildren(resolvedChildren)
//                        it.parent = element.parent
//                    } as T
//                ) else ElementUnchanged
//            }
//            is WrappedArgument -> {
//                val mappedType =
//                    when (val result = map(element.type, element, typeMapper)) {
//                        RemoveElement -> return RemoveElement
//                        ElementUnchanged -> element.type
//                        is ReplaceWith -> {
//                            needsMutation = true
//                            result.replacement
//                        }
//                    }
//                return if (needsMutation) ReplaceWith(
//                    WrappedArgument(
//                        element.name,
//                        mappedType
//                    ).also {
//                        it.clearChildren()
//                        it.addAllChildren(resolvedChildren)
//                        it.parent = element.parent
//                    } as T
//                ) else ElementUnchanged
//            }
//            is WrappedClass -> {
//                val specifiedType = element.specifiedType?.let { type ->
//                    when (val result = map(type, element, typeMapper)) {
//                        RemoveElement -> return RemoveElement
//                        ElementUnchanged -> type
//                        is ReplaceWith -> {
//                            needsMutation = true
//                            result.replacement
//                        }
//                    }
//                }
//                return if (needsMutation) ReplaceWith(
//                    element.clone(specifiedType).also {
//                        it.clearChildren()
//                        it.addAllChildren(resolvedChildren)
//                        it.parent = element.parent
//                    } as T
//                ) else ElementUnchanged
//            }
//        }
//        if (!needsMutation) return ElementUnchanged
//
//        return ReplaceWith(
//            element.clone().also {
//                it.clearChildren()
//                it.addAllChildren(resolvedChildren)
//                it.parent = element.parent
//            } as T
//        )
//    } catch (t: Throwable) {
//        throw IllegalArgumentException("Failed mapping $element", t)
//    }
//}
