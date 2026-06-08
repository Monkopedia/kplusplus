/*
 * Copyright 2022 Jason Monk
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
package com.monkopedia.krapper.generator.model

import clang.CXCursor
import clang.CXFile
import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.includedFile
import com.monkopedia.krapper.generator.isAbstract
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.STACK
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.ALIGN_OF
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.SIZE_OF
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClassMetadata
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMultiBase
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import kotlinx.cinterop.CValue

private val CValue<CXCursor>.fileParent: CXFile?
    get() {
        return includedFile
    }

class WrappedBase(
    val type: WrappedType?,
    val isPublic: Boolean = true,
    val isVirtualBase: Boolean = false
) : WrappedElement() {
    override fun clone(): WrappedElement = WrappedBase(type, isPublic, isVirtualBase).also {
        it.addAllChildren(children.map { it.clone() })
    }

    override suspend fun resolve(resolverContext: ResolveContext): ResolvedElement? = null
}

data class ClassMetadata(
    var hasHiddenNew: Boolean = false,
    var hasHiddenDelete: Boolean = false,
    var hasConstructor: Boolean = false,
    var hasPrivateConstField: Boolean = false,
    var hasDefaultConstructor: Boolean = false,
    var hasCopyConstructor: Boolean = false,
    // Set when the class's copy constructor is inaccessible for the generated C wrapper:
    // either explicitly `= delete`d or private/protected. Such a type can't be copied into
    // a by-value Holder (placement-new copy), so a method/field that returns it by value is
    // unmodelable and is dropped (T-skip). Recorded at parse time because the offending
    // constructor cursor is filtered out (NotAvailable/non-public) before it ever becomes a
    // child, so it can't be re-derived from the resolved children later.
    var hasDeletedCopyConstructor: Boolean = false,
    // Same idea for copy ASSIGNMENT (`operator=`): when it's deleted/inaccessible the
    // generated field setter's `field = *value` won't compile, so the setter is dropped.
    var hasDeletedCopyAssignment: Boolean = false,
    // Set when this class can't be DEFAULT-constructed because it has a (public) reference
    // data member and declares no usable constructor: its implicit default constructor is
    // deleted (a reference must be initialized), so krapper must NOT synthesize a
    // default-construct path (`new T()`) for it — that would emit a call to the
    // implicitly-deleted default ctor (T-skip residual, e.g. FunctionEffectSet::Conflict).
    var hasDeletedDefaultConstructor: Boolean = false
) {
    fun toResolved(): ResolvedClassMetadata = ResolvedClassMetadata(
        hasHiddenNew = hasHiddenNew,
        hasHiddenDelete = hasHiddenDelete,
        hasConstructor = hasConstructor,
        hasPrivateConstField = hasPrivateConstField,
        hasDefaultConstructor = hasDefaultConstructor,
        hasCopyConstructor = hasCopyConstructor
    )
}

class WrappedClass(
    val name: String,
    var isAbstract: Boolean = false,
    val specifiedType: WrappedType? = null
) : WrappedElement() {
    var metadata: ClassMetadata = ClassMetadata()
    val baseClass: WrappedType?
        get() = children.filterIsInstance<WrappedBase>().firstOrNull()?.type

    // Every base specifier of this class (primary + secondary), in declaration order.
    // `baseClass` above is the FIRST (offset-0) base used by the existing single-
    // inheritance paths; multi-inheritance codegen uses this full list so 2nd+ bases
    // (which are NOT at offset 0) can be flattened with an offset-aware C wrapper.
    val baseClasses: List<WrappedBase>
        get() = children.filterIsInstance<WrappedBase>()

    val type: WrappedType
        get() = specifiedType ?: WrappedType(qualified)

    constructor(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        wrapName(value, value.spelling.toKString() ?: error("Missing name")),
        value.isAbstract
    )

    override fun clone(): WrappedClass = clone(this.specifiedType)

    fun clone(specifiedType: WrappedType? = this.specifiedType): WrappedClass =
        WrappedClass(name, isAbstract, specifiedType).also {
            it.parent = parent
            it.addAllChildren(children)
            it.metadata = metadata.copy()
        }

    override suspend fun resolve(resolverContext: ResolveContext): ResolvedClass? {
        val baseClasses = resolverContext.findBases(this)
        modifyMethodsIfNeeded(baseClasses)
        return ResolvedClass(
            name,
            isAbstract,
            specifiedType?.let {
                // If no type, thats fine, but if type exists, it needs to resolve.
                resolverContext.resolve(specifiedType) ?: return resolverContext.notifyFailed(
                    this,
                    specifiedType,
                    "Specified class type resolve"
                )
            },
            metadata.toResolved(),

            // If the primary base can't resolve (e.g. an unlisted/unbindable
            // intermediate base), a top-level (listed/found) class drops it to a
            // non-modeled/borrowed base rather than failing the whole class —
            // exactly like the `allBaseClasses` path below silently drops
            // unresolvable secondary bases. The class loses that base's flattened
            // members + `.asBase()`, but its own members and any resolvable bases
            // still bind. (Don't require the full base-closure in every allowlist.)
            //
            // A class pulled ONLY as an INCLUDE_MISSING reference keeps the legacy
            // hard-fail (dropUnresolvablePrimaryBase == false): resurrecting such a
            // transitively-referenced subclass would also drag in its
            // incomplete-at-emit bases/members and emit broken C++.
            baseClass?.let {
                resolverContext.resolve(it)
                    ?: if (resolverContext.dropUnresolvablePrimaryBase) {
                        null
                    } else {
                        return resolverContext.notifyFailed(this, it, "Base class resolve")
                    }
            },
            resolverContext.resolve(type) ?: return resolverContext.notifyFailed(
                this,
                type,
                "Default class type resolve"
            ),
            // Resolve EVERY direct base (primary + secondary). A base whose type can't
            // be resolved is dropped from the list rather than failing the whole class
            // (it can't be flattened anyway); the first base mirrors `baseClass` above.
            allBaseClasses = this@WrappedClass.baseClasses.mapNotNull { base ->
                val baseType = base.type ?: return@mapNotNull null
                val resolved = resolverContext.resolve(baseType) ?: return@mapNotNull null
                ResolvedMultiBase(
                    type = resolved,
                    isPublic = base.isPublic,
                    isVirtualBase = base.isVirtualBase
                )
            }
        ).also {
            it.addAllChildren(
                children.toList().mapNotNull { child ->
                    if (isAbstract && child is WrappedConstructor) {
                        null
                    } else {
                        child.resolve(resolverContext + this)
                    }
                }
            )
        }
    }

    // T1.5: a class that declares BOTH a `const` and a non-`const` overload of the
    // same accessor (e.g. `T& at(int)` and `const T& at(int) const`) produces two
    // wrappers differing only in this-constness. They share the same name and the same
    // non-`this` parameter signature, so they are the read-only/mutable halves of ONE
    // logical accessor — not genuinely distinct overloads. Binding both yields a
    // redundant second wrapper (uniquified to `_at`) over the same call. Drop the
    // non-`const` one and keep the `const` overload: read-only is the safe choice (it
    // can't mutate the receiver, and `const`-qualifying the `this` call always
    // compiles), and a single binding avoids the duplicate. Only fires when the names
    // AND the full parameter-type lists match AND exactly one of the pair is `const`;
    // overloads that differ in parameters (genuinely distinct) are untouched.
    private fun dropConstOverloadDuplicates() {
        val methods = children.filterIsInstance<WrappedMethod>()
            .filter { it !is WrappedConstructor && it !is WrappedDestructor }
        val signature = { m: WrappedMethod -> m.name to m.args.map { it.type.toString() } }
        methods.groupBy(signature).values.forEach { overloads ->
            if (overloads.size != 2) return@forEach
            val constOverload = overloads.singleOrNull { it.isConst } ?: return@forEach
            val nonConst = overloads.single { it !== constOverload }
            if (nonConst.isConst) return@forEach
            removeChild(nonConst)
        }
    }

    private fun modifyMethodsIfNeeded(baseClasses: List<WrappedClass>) {
        dropConstOverloadDuplicates()
        if (!metadata.hasConstructor && children.any { it is WrappedConstructor }) {
            metadata.hasConstructor = true
        }
        if (!metadata.hasDefaultConstructor) {
            metadata.hasDefaultConstructor =
                children.any { (it as? WrappedConstructor)?.isDefaultConstructor == true }
        }
        if (!metadata.hasCopyConstructor) {
            metadata.hasCopyConstructor =
                children.any { (it as? WrappedConstructor)?.isCopyConstructor == true }
        }
        if (!isAbstract &&
            !metadata.hasConstructor &&
            !metadata.hasDeletedDefaultConstructor &&
            !baseClasses.any { it.metadata.hasConstructor }
        ) {
            addChild(
                WrappedConstructor(
                    "new",
                    type,
                    isCopyConstructor = false,
                    isDefaultConstructor = true
                )
            )
        }
        children.filterIsInstance<WrappedConstructor>().forEach {
            it.checkCopyConstructor(type)
        }
        // Assignment operators (`operator=`, `+=`, ...) return `T&` by convention;
        // we keep that return type so the binding surfaces a non-owning `Vec2?`
        // (the same OB-return-ref path), enabling chaining. Genuinely void-returning
        // assignment operators still resolve to Unit via determineReturnStyle.
        if (metadata.hasHiddenNew) {
            children.filterIsInstance<WrappedConstructor>().forEach {
                it.allocationStyle = STACK
            }
        }
        if (metadata.hasHiddenDelete) {
            children.filterIsInstance<WrappedDestructor>().forEach {
                removeChild(it)
            }
        }
        addChild(
            WrappedMethod(
                "sizeOf",
                WrappedTypeReference("int"),
                MethodType.SIZE_OF
            )
        )
        addChild(
            WrappedMethod(
                "alignOf",
                WrappedTypeReference("int"),
                MethodType.ALIGN_OF
            )
        )
    }

    override fun toString(): String = qualified

    private var isNotEmptyCache: Boolean? = null

    override fun addChild(child: WrappedElement) {
        isNotEmptyCache = null
        super.addChild(child)
    }

    override fun addAllChildren(list: List<WrappedElement>) {
        isNotEmptyCache = null
        super.addAllChildren(list)
    }

    fun isNotEmpty(): Boolean = isNotEmptyCache ?: calculateNotEmpty().also {
        isNotEmptyCache = it
    }

    private fun calculateNotEmpty() = baseClass != null || children.any {
        (it !is WrappedBase) &&
            ((it as? WrappedMethod)?.methodType != SIZE_OF) &&
            ((it as? WrappedMethod)?.methodType != ALIGN_OF) &&
            ((it as? WrappedConstructor)?.children?.isNotEmpty() != false)
    }
}

private fun wrapName(value: CValue<CXCursor>, name: String): String {
    val type = value.type.spelling.toKString() ?: return name
    val index = type.indexOf(name)
    if (index < 0) return name
    return type.substring(index)
}

val WrappedElement.qualified: String
    get() = withParents.mapNotNull { it.named }.joinToString("::")

// T1.0c: fully-qualified spelling of a FREE FUNCTION (a STATIC method with no parent
// class). `qualified` yields only the enclosing namespace path because a method name is
// not a `named` segment, so append the method name to get `<namespace>::<name>` (or just
// `<name>` for a function at file scope). Used to match free functions against an
// AllowListFilter the same way classes are matched by `type`.
val WrappedMethod.freeFunctionQualifiedName: String
    get() = qualified.let { if (it.isEmpty()) name else "$it::$name" }
private val WrappedElement.withParents: List<WrappedElement>
    get() = this@withParents.parent?.withParents?.plus(listOf(this@withParents))
        ?: listOf(this@withParents)
private val WrappedElement.named: String?
    get() = when (this) {
        is WrappedClass -> this@named.name
        is WrappedNamespace -> this@named.namespace
        else -> null
    }
