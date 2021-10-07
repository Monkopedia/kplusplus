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
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTypeReference
import com.monkopedia.krapper.generator.model.WrappedTypeReference.Companion.arrayOf
import com.monkopedia.krapper.generator.model.WrappedTypeReference.Companion.pointerTo
import com.monkopedia.krapper.generator.model.WrappedTypeReference.Companion.referenceTo
import kotlinx.cinterop.CValue

interface Resolver {
    fun resolve(fullyQualified: String): WrappedClass
    fun findClasses(filter: ClassFilter): List<WrappedClass>
}

interface ResolverBuilder {
    fun visit(type: CValue<CXType>)
    fun visit(type: WrappedTypeReference)
}

fun List<WrappedClass>.resolveAll(
    resolver: Resolver,
    referencePolicy: ReferencePolicy
): List<WrappedClass> {
    val classMap = associate { it.fullyQualified to it }.toMutableMap()
    for (cls in this) {
        resolveAll(cls, classMap, resolver, referencePolicy)
    }
    return classMap.values.toList()
}

private fun resolveAll(
    cls: WrappedClass,
    classMap: MutableMap<String, WrappedClass>,
    resolver: Resolver,
    policy: ReferencePolicy
) {
    var needsMutation = false
    val methods = cls.methods.map {
        resolve(it, classMap, resolver, policy)?.also {
            needsMutation = true
        } ?: it
    }
    val fields = cls.fields.map {
        resolve(it, classMap, resolver, policy)?.also {
            needsMutation = true
        } ?: it
    }
    if (needsMutation) {
        classMap[cls.fullyQualified] = cls.copy(
            methods = methods,
            fields = fields
        )
    }
}

fun resolve(
    method: WrappedMethod,
    classMap: MutableMap<String, WrappedClass>,
    resolver: Resolver,
    policy: ReferencePolicy
): WrappedMethod? {
    val returnTypeResolves = classMap.canResolve(method.returnType)
    val argsResolving = method.args.map {
        it to classMap.canResolve(it.type)
    }
    if (returnTypeResolves && argsResolving.all { it.second }) return null

    return method.copy(
        returnType =
            if (returnTypeResolves) method.returnType
            else handleUnresolved(method.returnType, classMap, resolver, policy) ?: return null,
        args = argsResolving.map {
            if (it.second) it.first
            else it.first.copy(
                type = handleUnresolved(it.first.type, classMap, resolver, policy) ?: return null
            )
        }
    )
}

fun resolve(
    field: WrappedField,
    classMap: MutableMap<String, WrappedClass>,
    resolver: Resolver,
    policy: ReferencePolicy
): WrappedField? {
    if (classMap.canResolve(field.type)) return null
    return field.copy(
        type = handleUnresolved(field.type, classMap, resolver, policy) ?: return null
    )
}

fun handleUnresolved(
    missingType: WrappedTypeReference,
    classMap: MutableMap<String, WrappedClass>,
    resolver: Resolver,
    policy: ReferencePolicy
): WrappedTypeReference? {
    if (missingType.isArray) return arrayOf(
        handleUnresolved(missingType.arrayType, classMap, resolver, policy) ?: return null
    )
    if (missingType.isPointer) return pointerTo(
        handleUnresolved(missingType.pointed, classMap, resolver, policy) ?: return null
    )
    if (missingType.isReference) return referenceTo(
        handleUnresolved(missingType.referenced, classMap, resolver, policy) ?: return null
    )
    return when (policy) {
        ReferencePolicy.IGNORE_MISSING -> {
            null
        }
        ReferencePolicy.OPAQUE_MISSING -> {
            pointerTo(WrappedTypeReference.VOID)
        }
        ReferencePolicy.THROW_MISSING -> {
            throw IllegalStateException("Cannot resolve ${missingType.name}")
        }
        ReferencePolicy.INCLUDE_MISSING -> {
            if (!classMap.containsKey(missingType.name)) {
                val cls = resolver.resolve(missingType.name)
                classMap[cls.fullyQualified] = cls
                resolveAll(cls, classMap, resolver, policy)
            }
            missingType
        }
    }
}

private fun MutableMap<String, WrappedClass>.canResolve(type: WrappedTypeReference): Boolean {
    if (type.isVoid || type.isNative || type.isReturnable) return true
    if (type.isArray) return canResolve(type.arrayType)
    if (type.isPointer) return canResolve(type.pointed)
    if (type.isReference) return canResolve(type.referenced)
    return containsKey(type.unconst.name)
}
