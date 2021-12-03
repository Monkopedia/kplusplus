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

import clang.CXType
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.type.WrappedModifiedType
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.arrayOf
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.referenceTo
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import kotlinx.cinterop.CValue

interface Resolver {
    fun resolve(type: WrappedType): WrappedClass
    fun resolveTemplate(type: WrappedType): WrappedTemplate
    fun findClasses(filter: ClassFilter): List<WrappedClass>
}

private class ResolveTracker(val classes: MutableMap<String, WrappedClass>) {
    fun canResolve(type: WrappedType): Boolean {
        if (type is WrappedModifiedType) {
            return canResolve(type.baseType)
        }
        if (type.isNative || type.isVoid) return true
        if (type !is WrappedTypeReference) {
            return canResolve(type.toString())
        }
        return canResolve(type.unconst.name)
    }

    private fun canResolve(str: String): Boolean {
        return classes.containsKey(str) || otherResolved.contains(str)
    }

    val otherResolved = mutableSetOf<String>()
}

interface ResolverBuilder {
    fun visit(type: CValue<CXType>): CValue<CXType>
}

fun List<WrappedClass>.resolveAll(resolver: Resolver, policy: ReferencePolicy): List<WrappedClass> {
    val baseList = associate { it.type.toString() to it }
    println("Seeding with ${baseList.keys}")
    val classMap = ResolveTracker(baseList.toMutableMap())
    val mapper = typeMapper(classMap, resolver, policy)
    val results = mapAll(mapper)
    for (key in baseList.keys) {
        classMap.classes.remove(key)
    }
    for (cls in results) {
        classMap.classes[cls.type.toString()] = cls
    }
    return classMap.classes.values.map {
        it.also { it.generateConstructorIfNeeded() }
    }
}

private fun resolveAll(
    cls: WrappedClass,
    classMap: ResolveTracker,
    resolver: Resolver,
    policy: ReferencePolicy
): WrappedClass? {
    val mapper = typeMapper(classMap, resolver, policy)
    return when (val result = map(cls, null, mapper)) {
        RemoveElement -> null
        ElementUnchanged -> cls
        is ReplaceWith -> result.replacement
    }
}

private fun typeMapper(
    tracker: ResolveTracker,
    resolver: Resolver,
    policy: ReferencePolicy
): TypeMapping {
    return when (policy) {
        ReferencePolicy.IGNORE_MISSING -> return { t, _ ->
            t.operateOn {
                if (tracker.canResolve(it)) {
                    ElementUnchanged
                } else {
                    RemoveElement
                }
            }
        }
        ReferencePolicy.OPAQUE_MISSING -> return { t, _ ->
            t.operateOn {
                if (tracker.canResolve(it)) {
                    ElementUnchanged
                } else {
                    ReplaceWith(pointerTo(WrappedType.VOID))
                }
            }
        }
        ReferencePolicy.THROW_MISSING -> return { t, _ ->
            t.operateOn {
                if (tracker.canResolve(it)) {
                    ElementUnchanged
                } else {
                    throw IllegalStateException("Cannot resolve $it")
                }
            }
        }
        ReferencePolicy.INCLUDE_MISSING -> return { t, _ ->
            t.operateOn {
                if (!tracker.canResolve(it)) {
                    try {
                        val cls = resolver.resolve(it)
                        tracker.otherResolved.add(cls.type.toString())
                        try {
                            val resolved = resolveAll(cls, tracker, resolver, policy)
                                ?: error("Couldn't include ${cls.type}, resolve failed")
                            removeDuplicateMethods(resolved)
                            tracker.classes[cls.type.toString()] = resolved
                        } finally {
                            tracker.otherResolved.remove(cls.type.toString())
                        }
                    } catch (original: Throwable) {
                        try {
                            // Its ok to not have a class if this reference points at a template.
                            resolver.resolveTemplate(it)
                            tracker.otherResolved.add(it.toString())
                        } catch (template: Throwable) {
                            return@operateOn RemoveElement
                        }
                    }
                }
                ElementUnchanged
            }
        }
    }
}

fun WrappedType.operateOn(typeHandler: (WrappedType) -> MapResult<out WrappedType>): MapResult<out WrappedType> {
    when {
        this is WrappedModifiedType -> {
            return (baseType.operateOn(typeHandler)).wrapOnReplace {
                WrappedModifiedType(it, modifier)
            }
        }
        this is WrappedPrefixedType -> {
            return (baseType.operateOn(typeHandler)).wrapOnReplace {
                WrappedPrefixedType(it, modifier)
            }
        }
        this is WrappedTypeReference && this.isArray -> {
            return (arrayType.operateOn(typeHandler)).wrapOnReplace {
                arrayOf(it)
            }
        }
        this.isPointer -> return (pointed.operateOn(typeHandler)).wrapOnReplace {
            pointerTo(it)
        }
        this.isReference -> return (unreferenced.operateOn(typeHandler)).wrapOnReplace {
            referenceTo(it)
        }
        else -> return typeHandler(this)
    }
}

inline fun MapResult<out WrappedType>.wrapOnReplace(
    typeWrapping: (WrappedType) -> WrappedType
): MapResult<out WrappedType> {
    return when (this) {
        is ReplaceWith -> ReplaceWith(typeWrapping(replacement))
        RemoveElement -> this
        ElementUnchanged -> this
    }
}
