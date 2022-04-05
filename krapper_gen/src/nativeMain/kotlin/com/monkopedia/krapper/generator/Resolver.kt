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
import com.monkopedia.krapper.generator.codegen.BasicAssignmentOperator
import com.monkopedia.krapper.generator.codegen.NameHandler
import com.monkopedia.krapper.generator.codegen.Namer
import com.monkopedia.krapper.generator.codegen.Operator
import com.monkopedia.krapper.generator.model.NullableKotlinType
import com.monkopedia.krapper.generator.model.TemplatedKotlinType
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedKotlinType
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.parentClass
import com.monkopedia.krapper.generator.model.qualified
import com.monkopedia.krapper.generator.model.type.WrappedModifiedType
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.LONG_DOUBLE
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.arrayOf
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.referenceTo
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.type.CastMethod.CAST
import com.monkopedia.krapper.generator.resolved_model.type.CastMethod.NATIVE
import com.monkopedia.krapper.generator.resolved_model.type.CastMethod.POINTED_STRING_CAST
import com.monkopedia.krapper.generator.resolved_model.type.CastMethod.STRING_CAST
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolved_model.type.nullable
import kotlinx.cinterop.CValue

interface Resolver {
    fun resolve(type: WrappedType, context: ResolveContext): Pair<ResolvedClass, WrappedClass>?
    fun resolveTemplate(type: WrappedType, context: ResolveContext): WrappedTemplate
    fun findClasses(filter: ElementFilter): List<WrappedElement>
}

class ResolveTracker(val classes: MutableMap<String, WrappedClass>) {
    val resolvedClasses = mutableMapOf<String, ResolvedClass>()
    fun canResolve(type: WrappedType, context: ResolveContext): Boolean {
        if (type == WrappedType.UNRESOLVABLE) return false
        if (otherResolved.contains(type.toString())) return true
        if (type is WrappedModifiedType) {
            return canResolve(type.baseType, context)
        }
        if (type.isNative || type.isVoid || type == LONG_DOUBLE) return true
        if (type !is WrappedTypeReference) {
            return canResolve(type.toString(), context)
        }
        return canResolve(type.unconst.name, context)
    }

    private fun canResolve(str: String, context: ResolveContext): Boolean {
        resolvedClasses[str]?.let {
            return if (it.isNotEmpty()) true else {
                context.notifyFailed<Any>(null, null, "Empty resolve for $str")
                false
            }
        }
        if (otherResolved.contains(str)) return true
        val cls = classes[str] ?: return false
        if (cls.isNotEmpty()) {
            try {
                otherResolved.add(str)
                resolvedClasses[str] = classes[str]?.resolve(context) ?: return false
                return resolvedClasses[str]?.isNotEmpty() == true
            } finally {
                otherResolved.remove(str)
            }
        } else {
            context.notifyFailed<Any>(cls, cls.type, "Empty class")
            return false
        }
    }

    val otherResolved = mutableSetOf<String>()
}

interface ResolverBuilder {
    fun visit(type: CValue<CXType>): CValue<CXType>
}

fun List<WrappedElement>.resolveAll(
    resolver: Resolver,
    policy: ReferencePolicy
): List<ResolvedElement> {
    val classes = filterIsInstance<WrappedClass>()
    val resolveContext = ResolveContext.Empty
        .copy(resolver = resolver, debugFilter = { element, type, message ->
            element?.parentClass?.toString()?.contains("HandleScope") ?: false ||
                (element == null && message.contains("HandleScope"))
        })
        .withClasses(classes)
        .withPolicy(policy)
    classes.forEach {
        if (resolveContext.resolve(it.type) == null) {
            println("Warning: can't resolve filtered class ${it.type}")
        }
    }
    val methods = filterIsInstance<WrappedMethod>().mapNotNull { method ->
        (method.parent as? WrappedNamespace)?.let { nm ->
            method.resolve(resolveContext + nm)
        }
    }
    return resolveContext.tracker.resolvedClasses.values.toList() + methods
}

data class ResolveContext(
    val tracker: ResolveTracker,
    val resolver: Resolver,
    val typeMapping: TypeMapping,
    val namer: NameHandler,
    val currentNamer: Namer,
    val mappingCache: MutableMap<WrappedType, MapResult> = mutableMapOf(),
    var debugFilter: ((WrappedElement?, WrappedType?, String) -> Boolean)? = null
) {

    fun map(type: WrappedType): WrappedType? {
        if (type.isArray) return null
        return when (
            val mapResult = mappingCache.getOrPut(type) {
                typeMapping(type, this)
            }
        ) {
            RemoveElement -> return null
            ElementUnchanged -> type
            is ReplaceWith -> mapResult.replacement
        }
    }

    fun mapAndResolve(type: WrappedType): Pair<WrappedType, ResolvedCppType>? {
        val type = map(type) ?: return null
        return type to toResolvedCppType(type)
    }

    fun resolve(type: WrappedType): ResolvedCppType? {
        val type = map(type) ?: return null
        return toResolvedCppType(type)
    }

    fun findBases(cls: WrappedClass): List<WrappedClass> {
        cls.baseClass?.let(::resolve)
        var baseCls = tracker.classes[cls.baseClass?.toString()]
        val list = mutableListOf<WrappedClass>()
        while (baseCls != null) {
            list.add(baseCls)
            baseCls = tracker.classes[baseCls.baseClass?.toString()]
        }
        return list
    }

    operator fun plus(wrappedClass: WrappedClass): ResolveContext {
        return copy(currentNamer = namer.namerFor(wrappedClass), mappingCache = mappingCache)
    }

    operator fun plus(wrappedClass: WrappedNamespace): ResolveContext {
        return copy(currentNamer = namer.namerFor(wrappedClass), mappingCache = mappingCache)
    }

    fun withClasses(
        classes: List<WrappedClass>
    ) = copy(
        tracker = ResolveTracker(classes.associateBy { it.type.toString() }.toMutableMap()),
        namer = NameHandler()
    )

    fun withPolicy(
        policy: ReferencePolicy
    ) = copy(typeMapping = typeMapper(policy), namer = NameHandler())

    fun canAssign(type: WrappedType): Boolean {
        resolve(type) ?: return true
        if (type.isConst) {
            return false
        }
        if (type.isReturnable) {
            return true
        }
        val resolvedClass = tracker.classes[type.toString()] ?: return true
        resolvedClass.children.filterIsInstance<WrappedMethod>()
            .find { Operator.from(it) == BasicAssignmentOperator.ASSIGN }
            ?.let {
                return true
            }
        return resolvedClass.children.filterIsInstance<WrappedField>().all {
            canAssign(it.type)
        }
    }

    fun <T> notifyFailed(
        element: WrappedElement?,
        type: WrappedType?,
        message: String
    ): T? {
        if (debugFilter?.invoke(element, type, message) == true) {
            println("$element failed resolving $type: $message")
        }
        return null
    }

    companion object {
        val Empty: ResolveContext
            get() = ResolveContext(
                ResolveTracker(mutableMapOf()),
                object : Resolver {
                    override fun resolve(
                        type: WrappedType,
                        context: ResolveContext
                    ): Pair<ResolvedClass, WrappedClass>? = null

                    override fun resolveTemplate(
                        type: WrappedType,
                        context: ResolveContext
                    ): WrappedTemplate = error("Not found")

                    override fun findClasses(filter: ElementFilter): List<WrappedClass> =
                        emptyList()
                },
                { _, _ -> RemoveElement },
                NameHandler(),
                NameHandler.Empty
            )
    }
}

private fun typeMapper(
    policy: ReferencePolicy
): TypeMapping {
    return when (policy) {
        ReferencePolicy.IGNORE_MISSING -> return { t, context ->
            t.operateOn {
                if (context.tracker.canResolve(it, context)) {
                    ElementUnchanged
                } else {
                    RemoveElement
                }
            }
        }
        ReferencePolicy.OPAQUE_MISSING -> return { t, context ->
            t.operateOn {
                if (context.tracker.canResolve(it, context)) {
                    ElementUnchanged
                } else {
                    ReplaceWith(pointerTo(WrappedType.VOID))
                }
            }
        }
        ReferencePolicy.THROW_MISSING -> return { t, context ->
            t.operateOn {
                if (context.tracker.canResolve(it, context)) {
                    ElementUnchanged
                } else {
                    throw IllegalStateException("Cannot resolve $it")
                }
            }
        }
        ReferencePolicy.INCLUDE_MISSING -> return { t, context ->
            t.operateOn {
                if (it.toString().contains("WasmStreamingImpl") || it.toString()
                    .contains("CompiledWasmModule")
                ) {
                    return@operateOn RemoveElement
                }
                if (!context.tracker.canResolve(it, context)) {
                    try {
                        if (context.tracker.resolvedClasses[it.toString()] != null) {
                            return@operateOn RemoveElement
                        }
                        context.tracker.otherResolved.add(t.toString())
                        try {
                            val (resolved, wrapper) = context.resolver.resolve(it, context)
                                ?: error("Couldn't include $it, resolve failed")
                            if (!resolved.isNotEmpty()) {
                                return@operateOn RemoveElement
                            }
                            context.tracker.resolvedClasses[resolved.type.toString()] = resolved
                            context.tracker.classes[wrapper.type.toString()] = wrapper
                        } finally {
                            context.tracker.otherResolved.remove(t.toString())
                        }
                    } catch (original: Throwable) {
                        try {
                            // Its ok to not have a class if this reference points at a template.
                            context.resolver.resolveTemplate(it, context)
                            context.tracker.otherResolved.add(it.toString())
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

fun WrappedType.operateOn(
    typeHandler: (WrappedType) -> MapResult
): MapResult {
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
        this is WrappedTemplateType -> return handleTemplate(typeHandler)
        this.isPointer -> return (pointed.operateOn(typeHandler)).wrapOnReplace {
            pointerTo(it)
        }
        this.isReference -> return (unreferenced.operateOn(typeHandler)).wrapOnReplace {
            referenceTo(it)
        }
        else -> return typeHandler(this)
    }
}

private fun WrappedTemplateType.handleTemplate(typeHandler: (WrappedType) -> MapResult): MapResult {
    val templates = templateArgs.map {
        when (val result = it.operateOn(typeHandler)) {
            RemoveElement -> return RemoveElement
            ElementUnchanged -> it
            is ReplaceWith -> result.replacement
        }
    }
    val mappedTemplates = WrappedTemplateType(baseType, templates)
    return when (val result = typeHandler(mappedTemplates)) {
        RemoveElement -> RemoveElement
        ElementUnchanged -> ReplaceWith(mappedTemplates)
        is ReplaceWith -> result
    }
}

inline fun MapResult.wrapOnReplace(
    typeWrapping: (WrappedType) -> WrappedType
): MapResult {
    return when (this) {
        is ReplaceWith -> ReplaceWith(typeWrapping(replacement))
        RemoveElement -> this
        ElementUnchanged -> this
    }
}

private fun toResolvedCppType(type: WrappedType) =
    ResolvedCppType(
        type.toString(),
        if (type.isPointer) nullable(toResolvedKotlinType(type.kotlinType))
        else toResolvedKotlinType(type.kotlinType),
        toResolvedCType(type.cType),
        when {
            type.isString -> STRING_CAST
            type.isPointer && type.pointed.isString -> POINTED_STRING_CAST
            type.isNative || (type.isPointer && type.pointed.isNative) -> NATIVE
            else -> CAST
        }
    )

fun toResolvedCType(type: WrappedType) =
    ResolvedCType(type.toString(), type.isVoid)

fun toResolvedKotlinType(kotlinType: WrappedKotlinType): ResolvedKotlinType {
    if (kotlinType is NullableKotlinType) {
        return toResolvedKotlinType(kotlinType.base).copy(
            isWrapper = kotlinType.isWrapper,
            isNullable = true
        )
    }
    if (kotlinType is TemplatedKotlinType) {
        return toResolvedKotlinType(kotlinType.baseType).copy(
            isWrapper = kotlinType.isWrapper,
            templates = kotlinType.templateTypes.map {
                toResolvedKotlinType(it)
            }
        )
    }
    return ResolvedKotlinType(
        kotlinType.fullyQualified.first().trimEnd('?'),
        kotlinType.isWrapper,
        kotlinType.name.endsWith('?')
    )
}
