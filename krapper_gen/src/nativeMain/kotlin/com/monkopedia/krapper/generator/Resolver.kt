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
import com.monkopedia.krapper.generator.model.WrappedModifiedType
import com.monkopedia.krapper.generator.model.WrappedType
import com.monkopedia.krapper.generator.model.WrappedType.Companion.arrayOf
import com.monkopedia.krapper.generator.model.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.model.WrappedType.Companion.referenceTo
import com.monkopedia.krapper.generator.model.WrappedTypeReference
import kotlinx.cinterop.CValue

interface Resolver {
    fun resolve(fullyQualified: String): WrappedClass
    fun findClasses(filter: ClassFilter): List<WrappedClass>
}

interface ResolverBuilder {
    fun visit(type: CValue<CXType>): CValue<CXType>
    fun visit(type: WrappedTypeReference)
}

fun List<WrappedClass>.resolveAll(resolver: Resolver, policy: ReferencePolicy): List<WrappedClass> {
    val classMap = associate { it.type.toString() to it }.toMutableMap()
    val mapper = handleUnresolved(classMap, resolver, policy)
    return mapAll(mapper)
}

fun resolveAll(
    cls: WrappedClass,
    classMap: MutableMap<String, WrappedClass>,
    resolver: Resolver,
    policy: ReferencePolicy
): WrappedClass? {
    val mapper = handleUnresolved(classMap, resolver, policy)
    return when (val result = map(cls, mapper)) {
        RemoveElement -> null
        ElementUnchanged -> cls
        is ReplaceWith -> result.replacement
    }
}

fun handleUnresolved(
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
                    ReplaceWith(pointerTo(WrappedTypeReference.VOID))
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
                if (!classMap.containsKey(it.toString())) {
                    val cls = resolver.resolve(it.toString())
                    classMap[cls.name] = cls
                    classMap[cls.name] = resolveAll(cls, classMap, resolver, policy)
                        ?: error("Couldn't include ${cls.name}, resolve failed")
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
        if (isPointer) return typeHandler(pointed).wrapOnReplace {
            pointerTo(it)
        }
        if (isReference) return typeHandler(referenced).wrapOnReplace {
            referenceTo(it)
        }
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
    if (type !is WrappedTypeReference) {
        return false
    }
    if (type.isArray) return canResolve(type.arrayType)
    if (type.isPointer) return canResolve(type.pointed)
    if (type.isReference) return canResolve(type.referenced)
    if (type.isVoid || type.isNative || type.isReturnable) return true
    return containsKey(type.unconst.name)
}
