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
import com.monkopedia.krapper.generator.model.type.isNative
import com.monkopedia.krapper.generator.model.type.isPointer
import com.monkopedia.krapper.generator.model.type.isReference
import com.monkopedia.krapper.generator.model.type.isReturnable
import com.monkopedia.krapper.generator.model.type.isVoid
import com.monkopedia.krapper.generator.model.type.pointed
import com.monkopedia.krapper.generator.model.type.unreferenced
import kotlinx.cinterop.CValue

interface Resolver {
    fun resolve(type: WrappedType): WrappedClass
    fun resolveTemplate(type: WrappedType): WrappedTemplate
    fun findClasses(filter: ClassFilter): List<WrappedClass>
}

interface ResolverBuilder {
    fun visit(type: CValue<CXType>): CValue<CXType>
    fun visit(type: WrappedType)
}

fun List<WrappedClass>.resolveAll(resolver: Resolver, policy: ReferencePolicy): List<WrappedClass> {
    val classMap = associate { it.type.toString() to it }.toMutableMap()
    val mapper = typeMapper(classMap, resolver, policy)
    return mapAll(mapper)
}

fun resolveAll(
    cls: WrappedClass,
    classMap: MutableMap<String, WrappedClass>,
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

fun typeMapper(
    classMap: MutableMap<String, WrappedClass>,
    resolver: Resolver,
    policy: ReferencePolicy
): TypeMapping {
    return when (policy) {
        ReferencePolicy.IGNORE_MISSING -> return { t ->
            t.operateOn {
                if (classMap.canResolve(it)) {
                    ElementUnchanged
                } else {
                    RemoveElement
                }
            }
        }
        ReferencePolicy.OPAQUE_MISSING -> return { t ->
            t.operateOn {
                if (classMap.canResolve(it)) {
                    ElementUnchanged
                } else {
                    ReplaceWith(pointerTo(WrappedType.VOID))
                }
            }
        }
        ReferencePolicy.THROW_MISSING -> return { t ->
            t.operateOn {
                if (classMap.canResolve(it)) {
                    ElementUnchanged
                } else {
                    throw IllegalStateException("Cannot resolve $it")
                }
            }
        }
        ReferencePolicy.INCLUDE_MISSING -> return { t ->
            t.operateOn {
                if (!classMap.canResolve(it)) {
                    try {
                        val cls = resolver.resolve(it)
                        classMap[cls.name] = cls
                        classMap[cls.name] = resolveAll(cls, classMap, resolver, policy)
                            ?: error("Couldn't include ${cls.name}, resolve failed")
                    } catch (_: Throwable) {
                        // Its ok to not have a class if this reference points at a template.
                        resolver.resolveTemplate(it)
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

private inline fun MapResult<out WrappedType>.wrapOnReplace(typeWrapping: (WrappedType) -> WrappedType): MapResult<out WrappedType> {
    return when (this) {
        is ReplaceWith -> ReplaceWith(typeWrapping(replacement))
        RemoveElement -> this
        ElementUnchanged -> this
    }
}

private fun MutableMap<String, WrappedClass>.canResolve(type: WrappedType): Boolean {
    if (type is WrappedModifiedType) {
        return canResolve(type.baseType)
    }
    if (type.isNative || type.isVoid || type.isReturnable) return true
    if (type !is WrappedTypeReference) {
        return containsKey(type.toString())
    }
    return containsKey(type.unconst.name)
}
