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
    fun startCapture()
    fun endCapture(): List<WrappedClass>
}

private class ResolveTracker(val classes: MutableMap<String, WrappedClass>) {
    fun canResolve(type: WrappedType): Boolean {
        if (type is WrappedModifiedType) {
            return canResolve(type.baseType)
        }
        if (type.isNative || type.isVoid || type.isReturnable) return true
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
    val classMap = ResolveTracker(associate { it.type.toString() to it }.toMutableMap())
    val mapper = typeMapper(classMap, resolver, policy)
    resolver.startCapture()
    val list = mapAll(mapper)
    val extras = resolver.endCapture()
    return list + extras
}

private fun resolveAll(
    cls: WrappedClass,
    classMap: ResolveTracker,
    resolver: Resolver,
    policy: ReferencePolicy
): WrappedClass? {
    val mapper = typeMapper(classMap, resolver, policy)
    return when (val result = map(cls, mapper)) {
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
        ReferencePolicy.IGNORE_MISSING -> return { t ->
            t.operateOn {
                if (tracker.canResolve(it)) {
                    ElementUnchanged
                } else {
                    RemoveElement
                }
            }
        }
        ReferencePolicy.OPAQUE_MISSING -> return { t ->
            t.operateOn {
                if (tracker.canResolve(it)) {
                    ElementUnchanged
                } else {
                    ReplaceWith(pointerTo(WrappedType.VOID))
                }
            }
        }
        ReferencePolicy.THROW_MISSING -> return { t ->
            t.operateOn {
                if (tracker.canResolve(it)) {
                    ElementUnchanged
                } else {
                    throw IllegalStateException("Cannot resolve $it")
                }
            }
        }
        ReferencePolicy.INCLUDE_MISSING -> return { t ->
            t.operateOn {
                if (!tracker.canResolve(it)) {
                    try {
                        val cls = resolver.resolve(it)
                        tracker.classes[cls.type.toString()] = cls
                        val resolved = resolveAll(cls, tracker, resolver, policy)
                        tracker.classes[cls.type.toString()] = resolved
                            ?: error("Couldn't include ${cls.type}, resolve failed")
                    } catch (original: Throwable) {
                        try {
                            // Its ok to not have a class if this reference points at a template.
                            resolver.resolveTemplate(it)
                            tracker.otherResolved.add(it.toString())
                        } catch (template: Throwable) {
                            template.printStackTrace()
                            return@operateOn RemoveElement
                        }
                    }
                }
                ElementUnchanged
            }
        }
    }
}

private inline fun WrappedType.operateOn(typeHandler: (WrappedType) -> MapResult<out WrappedType>): MapResult<out WrappedType> {
    if (this is WrappedModifiedType) {
        return typeHandler(baseType).wrapOnReplace {
            WrappedModifiedType(it, modifier)
        }
    }
    if (this is WrappedTypeReference) {
        if (isArray) return typeHandler(arrayType).wrapOnReplace {
            arrayOf(it)
        }
    }
    if (isPointer) return typeHandler(pointed).wrapOnReplace {
        pointerTo(it)
    }
    if (isReference) return typeHandler(unreferenced).wrapOnReplace {
        referenceTo(it)
    }
    return typeHandler(this)
}

private inline fun MapResult<out WrappedType>.wrapOnReplace(
    typeWrapping: (WrappedType) -> WrappedType
): MapResult<out WrappedType> {
    return when (this) {
        is ReplaceWith -> ReplaceWith(typeWrapping(replacement))
        RemoveElement -> this
        ElementUnchanged -> this
    }
}
