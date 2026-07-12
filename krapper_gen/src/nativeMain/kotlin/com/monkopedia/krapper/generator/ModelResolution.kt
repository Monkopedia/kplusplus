/*
 * Copyright 2026 Jason Monk
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

import com.monkopedia.krapper.generator.codegen.Operator
import com.monkopedia.krapper.generator.codegen.STACK_CONSTRUCTOR_CALLBACK
import com.monkopedia.krapper.generator.model.ClassMetadata
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedDestructor
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.WrappedTemplateParam
import com.monkopedia.krapper.generator.model.WrappedTypedef
import com.monkopedia.krapper.generator.model.parentClass
import com.monkopedia.krapper.generator.model.qualified
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.LONG_DOUBLE
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.VOID
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.DIRECT
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.STACK
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.NATIVE
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.RAW_CAST
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.REINT_CAST
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.STD_MOVE
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.ALIGN_OF
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.SIZE_OF
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.STATIC
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedArgument
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClassMetadata
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedConstructor
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedDestructor
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedField
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedFieldGetter
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedFieldSetter
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMultiBase
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedNamespace
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedTU
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedTemplate
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedTemplateParam
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedTypedef
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.ARG_CAST
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.ENUM_RETURN
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.RETURN
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.RETURN_REFERENCE
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.STRING
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.STRING_POINTER
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.VOIDP
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.VOIDP_REFERENCE
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedKotlinType

// Resolution of the parse-output model against the type graph (issue #44 brick 1b).
//
// The model module (:krapper_model) carries pure data — no ResolveContext, no
// resolvedmodel beyond the two data-carried enums, no codegen. The per-element
// resolve() bodies that used to be polymorphic overrides on WrappedElement live here
// as extensions, with [resolve] (on WrappedElement) as the when-dispatch replacing
// the old virtual dispatch. Call sites keep their `element.resolve(context)` spelling.

val ResolvedElement.parentClass: ResolvedClass?
    get() = (parent as? ResolvedClass) ?: parent?.parentClass
val ResolvedElement.baseParent: ResolvedElement
    get() = parent?.baseParent ?: parent ?: this

// The else branch covers WrappedBase / WrappedArgument / WrappedTemplateParam, which
// have no standalone resolution (they resolve as part of their owning class/method).
suspend fun WrappedElement.resolve(resolverContext: ResolveContext): ResolvedElement? =
    when (this) {
        is WrappedTU -> resolve(resolverContext)
        is WrappedNamespace -> resolve(resolverContext)
        is WrappedClass -> resolve(resolverContext)
        is WrappedTemplate -> resolve(resolverContext)
        is WrappedTypedef -> resolve(resolverContext)
        is WrappedMethod -> resolve(resolverContext)
        is WrappedField -> resolve(resolverContext)
        is WrappedType -> resolverContext.resolve(this)
        else -> null
    }

suspend fun WrappedTU.resolve(resolverContext: ResolveContext): ResolvedTU = ResolvedTU().also {
    it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
}

suspend fun WrappedNamespace.resolve(resolverContext: ResolveContext): ResolvedNamespace =
    ResolvedNamespace(namespace).also {
        it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
    }

suspend fun WrappedTypedef.resolve(resolverContext: ResolveContext): ResolvedElement? {
    return ResolvedTypedef(
        name,
        resolverContext.resolve(targetType) ?: return resolverContext.notifyFailed(
            this,
            targetType,
            "Failed to resolve typedef"
        )
    )
}

private fun ClassMetadata.toResolved(): ResolvedClassMetadata = ResolvedClassMetadata(
    hasHiddenNew = hasHiddenNew,
    hasHiddenDelete = hasHiddenDelete,
    hasConstructor = hasConstructor,
    hasPrivateConstField = hasPrivateConstField,
    hasDefaultConstructor = hasDefaultConstructor,
    hasCopyConstructor = hasCopyConstructor
)

suspend fun WrappedTemplate.resolve(resolverContext: ResolveContext): ResolvedTemplate? {
    return ResolvedTemplate(
        name,
        baseClass?.let {
            resolverContext.resolve(it)
                ?: return resolverContext.notifyFailed(this, it, "Base class")
        },
        metadata.toResolved(),
        qualified,
        templateArgs.mapNotNull { it.resolveTemplateParam(resolverContext) }
    )
}

suspend fun WrappedTemplateParam.resolveTemplateParam(
    resolverContext: ResolveContext
): ResolvedTemplateParam? {
    return ResolvedTemplateParam(
        name,
        usr,
        defaultType?.let {
            resolverContext.resolve(it)
                ?: return resolverContext.notifyFailed(this, it, "Default type")
        }
    )
}

suspend fun WrappedClass.resolve(resolverContext: ResolveContext): ResolvedClass? {
    val baseClasses = resolverContext.findBases(this)
    modifyMethodsIfNeeded(baseClasses)
    return ResolvedClass(
        name,
        isAbstract,
        specifiedType?.let {
            // If no type, thats fine, but if type exists, it needs to resolve.
            resolverContext.resolve(it) ?: return resolverContext.notifyFailed(
                this,
                it,
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
        allBaseClasses = this.baseClasses.mapNotNull { base ->
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
private fun WrappedClass.dropConstOverloadDuplicates() {
    val methods = children.filterIsInstance<WrappedMethod>()
        .filter { it !is WrappedConstructor && it !is WrappedDestructor }
    val signature = { m: WrappedMethod -> m.name to m.args.map { it.type.toString() } }
    methods.groupBy(signature).values.forEach { overloads ->
        if (overloads.size != 2) return@forEach
        val constOverload = overloads.singleOrNull { it.isConst } ?: return@forEach
        val nonConst = overloads.single { it !== constOverload }
        if (nonConst.isConst) return@forEach
        DropLedger.record(
            "$name::${nonConst.name}",
            "Non-const overload duplicates a const overload (kept the const half)",
            DropPhase.DEDUP
        )
        removeChild(nonConst)
    }
}

private fun WrappedClass.modifyMethodsIfNeeded(baseClasses: List<WrappedClass>) {
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
    // Idempotent: this runs on the SHARED WrappedClass on EVERY resolve, and a class is
    // re-resolved once per requestInstantiation pass (resolveForcing pass 1 + pass 3).
    // Unconditionally appending here accumulated a fresh sizeOf/alignOf pair per pass,
    // which the per-pass NameHandler then `_`-uniquified — e.g. `timespec_size_of`
    // emitted 324 times with up to 34 leading underscores after featuregen's 17
    // instantiation requests (#57). Add the pair only once per class.
    if (children.none { (it as? WrappedMethod)?.methodType == MethodType.SIZE_OF }) {
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
}

suspend fun WrappedElement.createThisArg(resolverContext: ResolveContext): ResolvedArgument? {
    val pointerParent = pointerTo(
        parentClass?.type ?: return resolverContext.notifyFailed(
            this,
            parentClass?.type,
            "Can't find parent type"
        )
    )
    val type = resolverContext.resolve(pointerParent)
        ?: return resolverContext.notifyFailed(this, pointerParent, "Parent type not resolving")
    return ResolvedArgument(
        "thiz",
        type,
        type,
        castMode = REINT_CAST,
        needsDereference = true,
        hasDefault = false
    )
}

suspend fun determineReturnStyle(
    returnType: WrappedType,
    resolverContext: ResolveContext
): ReturnStyle = when {
    returnType.isVoid -> ReturnStyle.VOID

    // A non-returnable (by-value class) return is materialized into a scope-bound
    // Holder via placement-new (ARG_CAST), which `defer`s the dispose so the heap
    // copy is freed when the MemScope exits — no leak (T1.6). Placement-new only
    // needs a copy constructor, never assignment, so `canAssign` is irrelevant
    // here: the old `else COPY_CONSTRUCTOR` branch (`return new T(call)`) handed
    // back a borrowed pointer to an UNMANAGED heap copy and leaked. COPY_CONSTRUCTOR
    // stays reachable as an explicit opt-in (see FixupApplier scriptOriginOptionsFix).
    !returnType.isReturnable -> ARG_CAST

    returnType.isString -> STRING

    returnType.isPointer && returnType.pointed.isString -> STRING_POINTER

    returnType.isNative || returnType == LONG_DOUBLE ->
        if (returnType.isReference) RETURN_REFERENCE else RETURN

    returnType.isEnum -> ENUM_RETURN

    // A reference-to-POINTER (`T*&`, e.g. `std::vector<Thing*>::operator[]`/`front`/`back`,
    // whose `reference` is `value_type& = Thing*&`) carries the stored pointer itself. The
    // generic VOIDP_REFERENCE path would return `&call` (`Thing**`) — one level too many,
    // so the Kotlin wrapper would point at the vector slot, not the element. Return the
    // pointer value directly (VOIDP: `(void*)call`), the same as a plain pointer return.
    returnType.isReference && returnType.unreferenced.isPointer -> VOIDP

    returnType.isReference -> VOIDP_REFERENCE

    else -> VOIDP
}

suspend fun WrappedMethod.resolve(resolverContext: ResolveContext): ResolvedMethod? = when (this) {
    is WrappedConstructor -> resolveConstructor(resolverContext)
    is WrappedDestructor -> resolveDestructor(resolverContext)
    else -> resolvePlainMethod(resolverContext)
}

private suspend fun WrappedConstructor.thizArg(
    resolverContext: ResolveContext
): List<ResolvedArgument> {
    val type = resolverContext.resolve(pointerTo(VOID))!!
    return listOf(
        ResolvedArgument(
            "location",
            type,
            type,
            "",
            NATIVE,
            needsDereference = false,
            hasDefault = false
        )
    )
}

private suspend fun WrappedConstructor.postArgs(
    resolverContext: ResolveContext
): List<ResolvedArgument>? {
    if (allocationStyle != STACK) return emptyList()
    val clsType = parentClass?.type?.let { resolverContext.resolve(it) }
        ?: return resolverContext.notifyFailed(
            this,
            parentClass?.type,
            "constructor missing class"
        )
    val type = resolverContext.resolve(pointerTo(VOID))!!.copy(
        typeString = STACK_CONSTRUCTOR_CALLBACK,
        kotlinType = ResolvedKotlinType(
            listOf("(${clsType.kotlinType.name}) -> Unit"),
            false,
            emptyList(),
            false
        )
    )
    return listOf(
        ResolvedArgument(
            "callback",
            type,
            type,
            "",
            REINT_CAST,
            needsDereference = false,
            hasDefault = false
        )
    )
}

private suspend fun WrappedConstructor.resolveConstructor(
    resolverContext: ResolveContext
): ResolvedConstructor? = with(resolverContext.currentNamer) {
    val pointedType = (
        if (allocationStyle == DIRECT) {
            parentClass?.type?.let(::pointerTo)
        } else {
            VOID
        }
        ) ?: return resolverContext.notifyFailed(
        this@resolveConstructor,
        null,
        "Constructor missing parent ${parentClass?.type}"
    )
    return ResolvedConstructor(
        name,
        resolverContext.resolve(pointedType) ?: return resolverContext.notifyFailed(
            this@resolveConstructor,
            pointedType,
            "Parent class resolve"
        ),
        isCopyConstructor,
        isDefaultConstructor,
        uniqueCName,
        thizArg(resolverContext) +
            (resolveArguments(resolverContext) ?: return null) +
            (postArgs(resolverContext) ?: return null),
        allocationStyle
    ).also {
        it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
    }
}

private suspend fun WrappedDestructor.resolveDestructor(
    resolverContext: ResolveContext
): ResolvedDestructor? = with(resolverContext.currentNamer) {
    return ResolvedDestructor(
        name,
        resolverContext.resolve(returnType) ?: return resolverContext.notifyFailed(
            this@resolveDestructor,
            returnType,
            "Destructor return type"
        ),
        uniqueCName,
        listOf(createThisArg(resolverContext) ?: return null)
    ).also {
        it.isVirtual = isVirtual
    }
}

private suspend fun WrappedMethod.thizArg(
    resolverContext: ResolveContext
): List<ResolvedArgument>? {
    if (methodType == SIZE_OF || methodType == ALIGN_OF ||
        methodType == STATIC
    ) {
        return emptyList()
    }
    return listOf(createThisArg(resolverContext) ?: return null)
}

private suspend fun WrappedMethod.resolvePlainMethod(
    resolverContext: ResolveContext
): ResolvedMethod? = with(resolverContext.currentNamer) {
    val (rawMapping, rawResolved) = resolverContext.mapAndResolve(returnType)
        ?: return resolverContext.notifyFailed(
            this@resolvePlainMethod,
            returnType,
            "Couldn't resolve return"
        )
    val returnStyle = determineReturnStyle(rawMapping, resolverContext)
    // A by-value class return is copy-constructed into a scope-bound Holder via
    // placement-new (ARG_CAST). Two element-type problems make that emission
    // ill-formed; handle them here, at the point the Holder's target type/style
    // is decided:
    //  * T-skip: if the element type `= delete`s its copy constructor, the
    //    placement-new `new (buf) T(expr)` calls a deleted ctor — drop the whole
    //    method (skip-not-crash), its siblings still bind.
    //  * T-constholder: a `const`-qualified by-value return makes the Holder
    //    target buffer `const T*`, and placement-new into a const pointer is
    //    ill-formed. The const is meaningless on a fresh Holder buffer, so strip
    //    top-level const from the type used to build the Holder pointer (the
    //    Kotlin-facing kotlinType, carried separately via rawResolved, is
    //    untouched). A copy-construct from a const lvalue into a non-const buffer
    //    is well-formed.
    if (returnStyle == ARG_CAST && !resolverContext.canCopyConstruct(rawMapping)) {
        return resolverContext.notifyFailed(
            this@resolvePlainMethod,
            rawMapping,
            "By-value return of a non-copy-constructible type (deleted copy ctor)"
        )
    }
    val holderMapping =
        if (returnStyle == ARG_CAST &&
            rawMapping.isConst
        ) {
            rawMapping.unconst
        } else {
            rawMapping
        }
    val type =
        if (!holderMapping.isPointer && !holderMapping.isReturnable) {
            pointerTo(holderMapping)
        } else {
            holderMapping
        }
    var resolvedReturnType = resolverContext.resolve(type)
        ?: return resolverContext.notifyFailed(
            this@resolvePlainMethod,
            type,
            "Couldn't resolve pointed return"
        )
    resolvedReturnType = resolvedReturnType.copy(
        kotlinType = rawResolved.kotlinType
    )
    val argCastNeedsPointer = if (returnStyle == ARG_CAST) {
        val type =
            if (holderMapping.isReference) {
                holderMapping.unreferenced
            } else {
                holderMapping
            }
        !type.isPointer
    } else {
        false
    }
    if (argCastNeedsPointer) {
        resolvedReturnType = resolvedReturnType.copy(
            cType =
                resolverContext.resolve(pointerTo(holderMapping))?.cType
                    ?: return resolverContext.notifyFailed(
                        this@resolvePlainMethod,
                        pointerTo(holderMapping),
                        "Couldn't resolve argCast"
                    )
        )
    }
    return ResolvedMethod(
        name,
        resolvedReturnType,
        methodType,
        uniqueCName,
        Operator.from(this@resolvePlainMethod)?.resolvedOperator,
        (thizArg(resolverContext) ?: return null) +
            (resolveArguments(resolverContext) ?: return null),
        returnStyle,
        argCastNeedsPointer,
        qualified
    ).also {
        it.isVirtual = isVirtual
        it.isDefaulted = isDefaulted
        it.returnsPairSecond = returnsPairSecond
        it.returnViaMemberCall = returnViaMemberCall
        it.rangeElementType = rangeElementType
        it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
    }
}

private suspend fun WrappedMethod.resolveArguments(
    resolverContext: ResolveContext
): List<ResolvedArgument>? {
    val retArgs = mutableListOf<ResolvedArgument>()

    args.forEachIndexed { index, wrappedArgument ->
        val resolved = wrappedArgument.resolveArgument(resolverContext)
        if (resolved != null) {
            retArgs.add(resolved)
        } else {
            // A param that fails to resolve can only be SAFELY omitted from the
            // generated C++ call when the C++ method itself supplies a default for it
            // (and for every trailing param) — then `m(a)` compiles because the
            // compiler fills in the rest. If instead a REQUIRED param is dropped, the
            // emitted call `m()` has too few arguments and won't compile. Skip-not-
            // crash: drop the WHOLE method rather than emit a short, non-compiling
            // call (the project's unmodelable-shapes-drop-and-log principle; the
            // sibling methods of the class still bind).
            //
            // The omittable check uses isOmittableDefault, NOT a bare hasDefault: the
            // cursor-children heuristic behind hasDefault also fires for an integer
            // token that is part of the param's TYPE (an array bound `int (&)[4]` or a
            // non-type template arg `Mask<4>`), wrongly flagging a required param as
            // "defaulted". Such params are never C++-default-omittable, so they drop
            // the method instead of trimming to a short call.
            if (args.subList(index, args.size).all { it.isOmittableDefault }) {
                return retArgs
            }
            return resolverContext.notifyFailed(
                this,
                null,
                "Method failed from argument $wrappedArgument"
            )
        }
    }
    return retArgs
}

suspend fun WrappedArgument.resolveArgument(resolverContext: ResolveContext): ResolvedArgument? {
    val unreferencedType = if (type.isReference) type.unreferenced else type
    // T1.10p: an inbound non-owning string view (e.g. `const llvm::StringRef&`). Marshal it
    // like a by-value std::string — the C boundary takes a `const char*`, Kotlin sees a
    // String — but tag it STRING_VIEW so CppWriter constructs the VIEW (not a std::string)
    // from the char* at the call site. The view's C++ spelling rides on signatureType so
    // the wrapper's local var is declared as that view type. The inbound inverse of
    // T1.10's outbound StringRef->std::string return (rewriteViewReturns).
    val viewName = unreferencedType.let { if (it.isConst) it.unconst else it }.toString()
    if (viewName in STRING_VIEW_TYPES) {
        val stringType = resolverContext.resolve(WrappedType("std::string"))
            ?: return resolverContext.notifyFailed(this, type, "std::string for view param")
        val viewSignature = (stringType as ResolvedCppType).copy(typeString = viewName)
        return ResolvedArgument(
            name,
            viewSignature,
            viewSignature,
            usr,
            ArgumentCastMode.STRING_VIEW,
            needsDereference = false,
            hasDefault,
            defaultValue
        )
    }
    val mapped =
        resolverContext.mapAndResolve(unreferencedType)
            ?: return resolverContext.notifyFailed(this, unreferencedType, "Missing type $type")
    var type = mapped.first
    var resolved = mapped.second
    // A TOP-LEVEL `const` on a POINTER passed by value is meaningless (the wrapper
    // takes a `void*` and reinterprets it) and renders wrong: a const-PREFIX over a
    // pointer prints as `const Thing *` — which C++ reads as pointer-to-const-Thing,
    // NOT the intended const-pointer `Thing* const`. So `push_back(const value_type&)`
    // on a `std::vector<Thing*>` (value_type = `Thing*`) would emit
    // `reinterpret_cast<const Thing*>(...)` and fail to match. Strip it — the same
    // reasoning as the G8 by-value-return const fix. Done AFTER mapAndResolve because
    // the const usually wraps an as-yet-unresolved typedef (`const value_type`) whose
    // pointer-ness only shows once resolved; detected structurally as a const-PREFIX
    // (WrappedPrefixedType) wrapping a pointer, so a genuine pointer-to-const
    // (`const Thing*`, a WrappedModifiedType) is left untouched.
    (type as? WrappedPrefixedType)?.takeIf { it.isPointer }?.let {
        resolverContext.mapAndResolve(it.unconst)?.let { (t, r) ->
            type = t
            resolved = r
        }
    }
    val needsDereference =
        !type.isPointer && !type.isNative && type != LONG_DOUBLE && !type.isEnum
    val resolvedArgType =
        if (needsDereference) {
            val pointerType = pointerTo(type)
            resolverContext.resolve(pointerType) ?: return resolverContext.notifyFailed(
                this,
                pointerType,
                "Argument type"
            )
        } else {
            resolved
        }
    return ResolvedArgument(
        name,
        resolved,
        resolvedArgType,
        usr,
        determineArgumentCastMode(type, this.type.isReference, resolverContext),
        needsDereference,
        hasDefault,
        defaultValue
    )
}

fun determineArgumentCastMode(
    type: WrappedType,
    // Resolve ditches the reference, so pass it in manually.
    isReference: Boolean,
    resolverContext: ResolveContext
) = when {
    type.isString -> ArgumentCastMode.STRING

    type.isNative -> NATIVE

    type == LONG_DOUBLE -> RAW_CAST

    // Cast the exposed integer back to the enum: `(Color)(x)`. C++ does not
    // implicitly convert integer -> enum for an argument (scoped or unscoped).
    type.isEnum -> RAW_CAST

    // TODO: Real type check rather than prefix.
    !isReference && !type.isPointer && type.toString().startsWith("std::unique_ptr") -> STD_MOVE

    else -> REINT_CAST
}

suspend fun WrappedField.resolve(resolverContext: ResolveContext): ResolvedField? =
    with(resolverContext.currentNamer) {
        val (mappedType, resolvedType) = resolverContext.mapAndResolve(type)
            ?: return resolverContext.notifyFailed(this@resolve, type, "Field type")
        val type =
            if (mappedType.isReference) {
                val unreferenced = mappedType.unreferenced
                resolverContext.map(unreferenced)
                    ?: return resolverContext.notifyFailed(
                        this@resolve,
                        unreferenced,
                        "Field unreferenced type"
                    )
            } else {
                mappedType
            }
        val needsDereference =
            !type.isPointer && !type.isNative && type != WrappedType.LONG_DOUBLE &&
                // An enum crosses the boundary as its underlying integer (by value), not a
                // dereferenced pointer — matching the method-return path. Without this an
                // enum FIELD getter resolved `pointerTo(enum)`, losing the enum identity +
                // package, so its `fromValue` call rendered as a mashed reference
                // (`TranslationUnitKindCompanionfromValue`) and the type emitted as a wrapper.
                !type.isEnum
        val getterStyle = determineReturnStyle(type, resolverContext)
        // T-skip: a by-value (ARG_CAST/Holder) field getter copy-constructs the member
        // into a Holder buffer (`new (buf) T(thiz->field)`). If T `= delete`s its copy
        // constructor that call won't compile, so the getter is unmodelable. A field
        // with no usable getter is useless, so DROP the whole field (skip-not-crash);
        // sibling fields/methods still bind.
        if (getterStyle == ReturnStyle.ARG_CAST && !resolverContext.canCopyConstruct(type)) {
            return resolverContext.notifyFailed(
                this@resolve,
                type,
                "By-value field getter of a non-copy-constructible type (deleted copy ctor)"
            )
        }
        // T-skip: a field whose type `= delete`s its copy assignment can't be written
        // through the generated `field = *value` setter. Mark it unassignable (drop the
        // setter); the getter is unaffected. canAssign returns true for pointer/native/
        // implicitly-assignable types, so only genuinely non-assignable members drop.
        val setterUnassignable = !resolverContext.canAssign(type)
        val wrappedArgType = if (needsDereference) WrappedType.pointerTo(type) else type
        val argType = resolverContext.resolve(wrappedArgType)
            ?: return resolverContext.notifyFailed(
                this@resolve,
                wrappedArgType,
                "Arg type"
            )
        return ResolvedField(
            name,
            // Mark the field unassignable (no `_set`) when it is const OR a
            // reference member: a reference can't be rebound, so assigning through
            // it would be a compile error just like a const member. `type` here has
            // already been unreferenced, so test the original field type for the
            // reference case. Also drop the setter when the field's type can't be
            // copy-assigned (deleted copy assignment — T-skip).
            type.isConst || this@resolve.type.isReference || setterUnassignable,
            ResolvedFieldGetter(
                uniqueCGetter,
                getterStyle,
                argType,
                listOf(
                    createThisArg(resolverContext) ?: return null
                ),
                needsDereference = needsDereference
            ),
            ResolvedFieldSetter(
                uniqueCSetter,
                listOf(
                    createThisArg(resolverContext) ?: return null,
                    ResolvedArgument(
                        "value",
                        resolvedType,
                        argType,
                        "",
                        determineArgumentCastMode(
                            type,
                            mappedType.isReference,
                            resolverContext
                        ),
                        needsDereference,
                        false
                    )
                )
            )
        )
    }
