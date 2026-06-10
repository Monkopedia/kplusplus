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
package com.monkopedia.krapper.generator

import clang.CXType
import com.monkopedia.krapper.generator.codegen.BasicAssignmentOperator
import com.monkopedia.krapper.generator.codegen.NameHandler
import com.monkopedia.krapper.generator.codegen.Namer
import com.monkopedia.krapper.generator.codegen.Operator
import com.monkopedia.krapper.generator.model.EnumKotlinType
import com.monkopedia.krapper.generator.model.NullableKotlinType
import com.monkopedia.krapper.generator.model.TemplatedKotlinType
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedKotlinType
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.kotlinType
import com.monkopedia.krapper.generator.model.type.WrappedFunctionPointer
import com.monkopedia.krapper.generator.model.type.WrappedModifiedType
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.LONG_DOUBLE
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.arrayOf
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.referenceTo
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.type.CastMethod.CAST
import com.monkopedia.krapper.generator.resolvedmodel.type.CastMethod.NATIVE
import com.monkopedia.krapper.generator.resolvedmodel.type.CastMethod.POINTED_STRING_CAST
import com.monkopedia.krapper.generator.resolvedmodel.type.CastMethod.STRING_CAST
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedEnumEntry
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedFunctionPointer
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolvedmodel.type.nullable
import kotlinx.cinterop.CValue

interface Resolver {
    suspend fun resolve(
        type: WrappedType,
        context: ResolveContext
    ): Pair<ResolvedClass, WrappedClass>?

    fun resolveTemplate(type: WrappedType, context: ResolveContext): WrappedTemplate
    suspend fun findClasses(filter: ElementFilter): List<WrappedElement>
}

class ResolveTracker(val classes: MutableMap<String, WrappedClass>) {
    val resolvedClasses = mutableMapOf<String, ResolvedClass>()
    suspend fun canResolve(type: WrappedType, context: ResolveContext): Boolean {
        if (type.isArray) return false
        if (type == WrappedType.UNRESOLVABLE) return false
        if (otherResolved.contains(type.toString())) return true
        if (type is WrappedModifiedType) {
            return canResolve(type.baseType, context)
        }
        // Covers function-pointer typedefs too (WrappedFunctionPointer.isNative == true):
        // they cross the boundary as an opaque native pointer named by their typedef.
        if (type.isNative || type.isVoid || type == LONG_DOUBLE) return true
        // Enums reduce to their underlying integer at the boundary, so they always
        // resolve (no model element / tracker entry needed).
        if (type.isEnum) return true
        if (type !is WrappedTypeReference) {
            return canResolve(type.toString(), context)
        }
        return canResolve(type.unconst.name, context)
    }

    private suspend fun canResolve(str: String, context: ResolveContext): Boolean {
        resolvedClasses[str]?.let {
            return if (it.isNotEmpty()) {
                true
            } else {
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

suspend fun List<WrappedElement>.resolveAll(
    resolver: Resolver,
    policy: ReferencePolicy
): List<ResolvedElement> {
    val classes = filterIsInstance<WrappedClass>()
    val resolveContext = ResolveContext.Empty
        .copy(
            resolver = resolver,
            // Surface dropped/failed resolutions when KRAPPER_DEBUG_RESOLVE is set.
            // Previously this filter was hardcoded to a single ("CreateParams") case,
            // which silently hid all other resolution failures (e.g. dropped member
            // methods whose return type couldn't be resolved).
            debugFilter = { _, _, _ ->
                platform.posix.getenv("KRAPPER_DEBUG_RESOLVE") != null
            }
        )
        .withClasses(classes)
        .withPolicy(policy)
    classes.forEach {
        if (resolveContext.resolve(it.type) == null) {
            DropLedger.record(
                it.type.toString(),
                "Filtered class did not resolve",
                DropPhase.RESOLVE
            )
            Log.w("Warning: can't resolve filtered class ${it.type}")
        }
    }
    val methods = filterIsInstance<WrappedMethod>().mapNotNull { method ->
        (method.parent as? WrappedNamespace)?.let { nm ->
            method.resolve(resolveContext + nm)
        }
    }
    return resolveContext.tracker.resolvedClasses.values.toList() + methods
}

/**
 * Scoped resolution for a forced template instantiation (`--instantiate "Base<Args>"`).
 *
 * The forcing path re-parses a synthetic header that `#include`s the consumer's own
 * headers, so a blanket `findClasses { defaultFilter() }.resolveAll(...)` resolves EVERY
 * class in that TU — against a large library (e.g. Clang) that is the whole transitive
 * surface (~1269 classes), not just the container specialization we asked for. [this]
 * is therefore scoped by the caller to the `KrapperForce_*` forcing struct (whose `value`
 * member's type IS the specialization — resolving it materializes it as a side effect)
 * PLUS the classes that were ALREADY bound by `filterAndResolve` (re-parsed here so they
 * can be re-resolved now that the specialization exists: a method whose return type is the
 * just-forced container — e.g. `RangeHolder::items() -> std::vector<Thing*>` — was dropped
 * the first time round because the container wasn't bound yet, and is recovered here). The
 * UNRELATED TU classes (the ~1269) are NOT in [this], so they are neither re-resolved nor,
 * being referenced by nothing in the scoped set, pulled in under INCLUDE_MISSING.
 *
 * Resolution runs in THREE passes over ONE shared tracker (see the inline comments at
 * each pass for the full reasoning). In short: pass 1 binds the already-bound classes
 * under the main policy; pass 2 forces the container under INCLUDE_MISSING (the std spec
 * is excluded by `defaultFilter` and so is never in the tracker's class map — only
 * INCLUDE materializes it on-demand from the parsed TU); pass 3 re-resolves the bound
 * classes (fresh `mappingCache`) to recover methods that were dropped in pass 1 because
 * their return type was the then-unbound container. The shared tracker is what keeps
 * pass 2 bounded: classes resolved in pass 1 are already in `resolvedClasses`, so
 * INCLUDE will not re-expand them (re-expanding them is exactly the ~1269 explosion).
 *
 * [alreadyBoundKeys] is the type-string set of the pre-existing bindings, used only to
 * report the genuinely-NEW class count (the forcing struct and re-resolved-but-unchanged
 * bound classes are excluded from the count; the writer's last-wins dedup still picks up
 * the re-resolved bound classes, which is how `items()` is restored). The full
 * resolved set (minus the transient forcing struct) is returned for appending.
 *
 * [forcedContainerKeys] is a CUMULATIVE, in/out set of the container specs forced by every
 * EARLIER instantiation request, shared across the whole run. It exists because each request
 * gets its OWN `resolveForcing` with a FRESH tracker holding only THAT request's container,
 * yet a single owning class can have SEVERAL range accessors over DIFFERENT containers (e.g.
 * Clang's `CXXRecordDecl` with `methods() -> vector<CXXMethodDecl*>`, `bases() ->
 * vector<CXXBaseSpecifier*>` and `decls() -> vector<Decl*>`). With only the current request's
 * container in the tracker, pass 3 recovers THAT one range method and re-drops the others
 * (their containers aren't visible), and the writer's last-wins dedup keeps whichever request
 * ran last — silently losing the rest (`decls()` survived only by sorting last). Seeding the
 * prior containers here (IGNORE_MISSING only — see pass 3) makes `canResolve` true for ALL of
 * them, so pass 3 recovers EVERY range accessor of the re-resolved class regardless of the
 * order requests are processed in (the set is cumulative, so whichever request runs last sees
 * every container). This function also ADDS the container(s) it forces to the set, for the
 * next request.
 */
suspend fun List<WrappedElement>.resolveForcing(
    resolver: Resolver,
    policy: ReferencePolicy,
    alreadyBoundKeys: Set<String>,
    forcedContainerKeys: MutableSet<String> = mutableSetOf()
): List<ResolvedElement> {
    val classes = filterIsInstance<WrappedClass>()
    // The `KrapperForce_*` struct's `value` member type IS the forced container
    // specialization (e.g. `std::vector<clang::Decl*>`); resolving it is what
    // materializes the container. Everything else in the scoped set is a class that
    // was ALREADY bound by filterAndResolve, re-parsed here so it can be re-resolved
    // once the container exists.
    val forcingStructs = classes.filter { it.type.toString().contains("KrapperForce_") }
    val boundClasses = classes.filter { it.type.toString().contains("KrapperForce_").not() }

    // ONE context, ONE tracker, shared across all passes. `withClasses` seeds the
    // tracker's class map with every scoped class (incl. the forcing struct) and is
    // called EXACTLY ONCE so the namer (reset by both withClasses and withPolicy) is
    // established once and stays stable — re-running withClasses/withPolicy would
    // reset the NameHandler mid-stream and risk inconsistent generated names. We then
    // only swap `typeMapping` (the policy) between passes via `withPolicyKeepingNamer`,
    // which preserves the namer + tracker and changes nothing else but the policy.
    //
    // The SHARED tracker is the load-bearing invariant: once pass 1 puts `clang::Decl`
    // (etc.) into `tracker.resolvedClasses`, every later pass — including the
    // INCLUDE_MISSING pass — sees `canResolve == true` for it and will NOT re-expand
    // it. Re-expanding already-bound library classes under INCLUDE_MISSING is exactly
    // what produced the ~1269-class explosion; sharing the tracker prevents it.
    val baseContext = ResolveContext.Empty
        .copy(
            resolver = resolver,
            debugFilter = { _, _, _ ->
                platform.posix.getenv("KRAPPER_DEBUG_RESOLVE") != null
            }
        )
        .withClasses(classes)

    // ---- Pass 1: incoming `policy` — bind the already-bound library classes. ----
    // Uses the MAIN policy (NOT a hardcoded one) so this pass reproduces exactly what
    // the original single-pass forcing did to the bound set:
    //   * Under the real-Clang IGNORE_MISSING run: no on-demand expansion, so no
    //     ~1269-class explosion. Methods whose return type is the (still unbound)
    //     container are DROPPED here and recovered in pass 3.
    //   * Under featuregen's INCLUDE_MISSING run: types referenced by a bound class but
    //     bound by a DIFFERENT instantiation request (e.g. `StringFeature::produce()`
    //     returning `std::string`, where `std::string` is bound by a separate
    //     std::vector<string> request, not present in THIS forcing tracker) are
    //     materialized on-demand and survive — matching baseline. Hardcoding IGNORE
    //     here instead would silently drop those cross-instantiation methods.
    // The shared-tracker invariant still prevents the explosion: see pass 2.
    val pass1 = baseContext.withPolicyKeepingNamer(policy)
    boundClasses.forEach {
        if (pass1.resolve(it.type) == null) {
            DropLedger.record(
                it.type.toString(),
                "Filtered class did not resolve",
                DropPhase.RESOLVE
            )
            Log.w("Warning: can't resolve filtered class ${it.type}")
        }
    }

    // ---- Pass 2: INCLUDE_MISSING — bind the forcing struct (the container). ----
    // Same tracker. The struct's `value` member (`std::vector<clang::Decl*>`) is
    // materialized ON-DEMAND from the parsed TU (this is the semantics IGNORE_MISSING
    // lacks: under IGNORE the std spec, which is excluded by defaultFilter and so
    // never collected into tracker.classes, would simply be dropped). Because
    // `clang::Decl` is ALREADY in tracker.resolvedClasses from pass 1, INCLUDE sees it
    // as resolved and does NOT re-expand it — only the std container machinery is
    // pulled in. Result: the container specialization is now bound, bounded.
    val pass2 = baseContext.withPolicyKeepingNamer(ReferencePolicy.INCLUDE_MISSING)
    forcingStructs.forEach {
        if (pass2.resolve(it.type) == null) {
            DropLedger.record(
                it.type.toString(),
                "Forcing struct did not resolve",
                DropPhase.RESOLVE
            )
            Log.w("Warning: can't resolve forcing struct ${it.type}")
        }
    }

    // ---- Pass 3: incoming `policy` (FRESH mappingCache) — recover dropped methods. ----
    // Re-resolve the already-bound classes now that the container exists. A method
    // dropped in pass 1 because its return type was the then-unbound container (e.g.
    // `RangeHolder::items() -> std::vector<Thing*>`, the featuregen analog of
    // `TranslationUnitDecl::decls()`) is recovered here. A FRESH mappingCache is
    // mandatory: `ResolveContext.map` memoizes per-type results in mappingCache, so a
    // type that mapped to RemoveElement in pass 1 (container then unbound) would return
    // that STALE drop from the cache and the method would stay dropped. Wiping the cache
    // forces re-evaluation against the now-populated tracker. Uses the incoming `policy`
    // (same reasoning as pass 1: preserve cross-instantiation methods under
    // INCLUDE_MISSING; don't expand the world under IGNORE_MISSING — the container is
    // already in the tracker from pass 2, so it resolves regardless of policy here). The
    // writer's last-wins dedup adopts these improved versions.
    // EVICT the bound classes from the tracker's resolved cache first. Without this,
    // `resolve(it.type)` sees the class is already in `tracker.resolvedClasses` (from
    // pass 1) and short-circuits to that STALE version — the one whose container-returning
    // method (e.g. `DeclContext::decls() -> std::vector<clang::Decl*>`) was dropped because
    // the container wasn't bound yet. Removing the cache entry forces a genuine rebuild
    // against the now-populated tracker, so the dropped range method is recovered. (Under
    // INCLUDE_MISSING the method never dropped in pass 1 — it materialized on-demand — so
    // this path is exercised mainly by the IGNORE_MISSING real-Clang run; the fresh
    // mappingCache alone is not enough because the per-CLASS resolvedClasses cache also
    // memoizes.) The container spec resolved in pass 2 is NOT a bound class, so it stays.
    // Evict the bound classes from `resolvedClasses` so pass-3's `resolve(it.type)` rebuilds
    // them (against the now-bound container) instead of short-circuiting to the stale pass-1
    // version whose container-returning range method was dropped.
    //
    // The eviction BREADTH is policy-gated, because the two runs need different things:
    //
    //  * IGNORE_MISSING (real-Clang): a method WAS dropped in pass 1, and at a large header
    //    set the bound class can sit in `resolvedClasses` under a DIFFERENT spelling than its
    //    forcing-parse key (the forcing re-parses into a fresh TU; canonical/include-order
    //    drift makes the MAIN-parse resolved key — in `alreadyBoundKeys` — diverge from the
    //    forcing key). So evict the UNION of both key forms or pass-3 finds the stale entry
    //    under the un-evicted spelling and never recovers (the entry-point-header harness
    //    exposed this; small slices key-matched and worked with the forcing key alone).
    //    Library std types (`std::string`) are filtered out under IGNORE, so evicting them is
    //    harmless — they are never re-pulled or emitted.
    //
    //  * INCLUDE_MISSING (featuregen): NOTHING dropped in pass 1 (methods materialized
    //    on-demand), so the wide union-evict buys no recovery — but it DOES evict still-needed
    //    library classes like `std::string` that a sibling instantiation request bound. Pass-3's
    //    boundClass re-resolution then re-pulls the evicted `std::string` as a fresh
    //    INCLUDE_MISSING reference the tracker no longer recognizes, emitting a DUPLICATE,
    //    degraded copy (param casts lose their `<char>` template arg → uncompilable C++). Evict
    //    only the forcing keys here; leaving the sibling-bound std types in place keeps them
    //    recognized, so they are neither re-pulled nor degraded. (This is what featuregen ran
    //    green on before the union-evict landed.)
    val evictKeys = if (policy == ReferencePolicy.IGNORE_MISSING) {
        alreadyBoundKeys + boundClasses.map { it.type.toString() }
    } else {
        boundClasses.map { it.type.toString() }.toSet()
    }
    evictKeys.forEach { baseContext.tracker.resolvedClasses.remove(it) }
    // CUMULATIVE forcing recovery: make every PRIOR request's container visible to this
    // pass-3 re-resolve, not just the one this request forced. `otherResolved` is the set
    // `canResolve` short-circuits on (it returns true before reaching any on-demand class
    // resolution), so seeding it keeps a range method whose return container was forced by an
    // EARLIER request — e.g. when this request forced `vector<Decl*>`, the owning
    // `CXXRecordDecl`'s `methods()`/`bases()` (forced by sibling requests) are recovered here
    // too instead of being re-dropped and overwriting the earlier recovery via last-wins.
    // Seeds are query-only: they don't enter `resolvedClasses`, so they're neither emitted nor
    // returned (the container is emitted by the request that actually forced it). IGNORE_MISSING
    // only: under INCLUDE_MISSING nothing dropped in pass 1 (methods materialize on-demand), so
    // there's nothing to recover and seeding would needlessly perturb the featuregen-stable path.
    if (policy == ReferencePolicy.IGNORE_MISSING) {
        baseContext.tracker.otherResolved.addAll(forcedContainerKeys)
    }
    val pass3 = baseContext
        .withPolicyKeepingNamer(policy)
        .copy(mappingCache = mutableMapOf())
    boundClasses.forEach {
        pass3.resolve(it.type)
    }

    // Re-resolve any namespace-scoped static methods in the scoped set (parity with
    // resolveAll, which resolves these after the classes). Use the fresh-cache pass.
    val methods = filterIsInstance<WrappedMethod>().mapNotNull { method ->
        (method.parent as? WrappedNamespace)?.let { nm ->
            method.resolve(pass3 + nm)
        }
    }

    // The tracker accumulated every resolved class across all three passes. Drop the
    // transient forcing struct (it's scaffolding, never emitted) and return the rest.
    val resolved = baseContext.tracker.resolvedClasses.values.toList()
        .filter { it.type.toString().contains("KrapperForce_").not() } + methods
    // Record the container(s) THIS request forced (the genuinely-new resolved classes pass 2
    // materialized — i.e. those not already bound) so the NEXT request's pass 3 can see them.
    // This is the cumulative half of the order-independent recovery seeded above. Captured for
    // every policy (cheap, and never read back under INCLUDE_MISSING where seeding is gated off).
    val newClassKeys = resolved.mapNotNull { element ->
        (element as? ResolvedClass)?.type?.toString()?.takeIf { it !in alreadyBoundKeys }
    }
    forcedContainerKeys.addAll(newClassKeys)
    Log.i(
        "Forcing introduced ${newClassKeys.size} new class(es) " +
            "(re-resolved ${resolved.size} total)"
    )
    return resolved
}

data class ResolveContext(
    val tracker: ResolveTracker,
    val resolver: Resolver,
    val typeMapping: TypeMapping,
    val namer: NameHandler,
    val currentNamer: Namer,
    val mappingCache: MutableMap<WrappedType, MapResult> = mutableMapOf(),
    var debugFilter: ((WrappedElement?, WrappedType?, String) -> Boolean)? = null,
    // T1.0b: when a class's PRIMARY base can't resolve, a top-level (listed/found)
    // class drops it to a borrowed/non-modeled base and still binds (don't require
    // the full base-closure in every allowlist). But a class pulled ONLY as an
    // INCLUDE_MISSING *reference* keeps the legacy hard-fail: resurrecting such a
    // transitively-referenced subclass would also drag in its incomplete-at-emit
    // bases/members (e.g. `std::ctype<char>` → `std::ctype_base`), emitting broken
    // C++. False only while expanding an INCLUDE_MISSING reference. See
    // `WrappedClass.resolve`.
    val dropUnresolvablePrimaryBase: Boolean = true
) {

    suspend fun map(type: WrappedType): WrappedType? {
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

    suspend fun mapAndResolve(type: WrappedType): Pair<WrappedType, ResolvedCppType>? {
        val type = map(type) ?: return null
        return type to toResolvedCppType(type)
    }

    suspend fun resolve(type: WrappedType): ResolvedCppType? {
        val type = map(type) ?: return null
        return toResolvedCppType(type)
    }

    suspend fun findBases(cls: WrappedClass): List<WrappedClass> {
        // Walk EVERY direct base (primary + secondary), each up its own primary chain,
        // so callers (e.g. constructor synthesis) account for ALL inherited members.
        // For a single-inheritance class this is exactly the old single chain.
        val list = mutableListOf<WrappedClass>()
        val seen = mutableSetOf<String>()
        for (base in cls.baseClasses) {
            val baseType = base.type ?: continue
            resolve(baseType)
            var baseCls = tracker.classes[baseType.toString()]
            while (baseCls != null && seen.add(baseCls.type.toString())) {
                list.add(baseCls)
                baseCls = tracker.classes[baseCls.baseClass?.toString()]
            }
        }
        return list
    }

    operator fun plus(wrappedClass: WrappedClass): ResolveContext =
        copy(currentNamer = namer.namerFor(wrappedClass), mappingCache = mappingCache)

    operator fun plus(wrappedClass: WrappedNamespace): ResolveContext =
        copy(currentNamer = namer.namerFor(wrappedClass), mappingCache = mappingCache)

    fun withClasses(classes: List<WrappedClass>) = copy(
        tracker = ResolveTracker(classes.associateBy { it.type.toString() }.toMutableMap()),
        namer = NameHandler()
    )

    fun withPolicy(policy: ReferencePolicy) =
        copy(typeMapping = typeMapper(policy), namer = NameHandler())

    // Swap ONLY the policy (typeMapping), preserving the namer, tracker and
    // mappingCache. Used by the multi-pass forcing resolve, which switches policy
    // across passes (main policy -> INCLUDE_MISSING for the container -> main policy)
    // while accumulating into a single shared tracker. Unlike `withPolicy`, it must
    // NOT reset the NameHandler:
    // resetting it mid-stream (as withPolicy does, since it normally pairs with a
    // fresh withClasses) would re-number already-established classes and risk
    // inconsistent generated names across the passes.
    fun withPolicyKeepingNamer(policy: ReferencePolicy) = copy(typeMapping = typeMapper(policy))

    suspend fun canAssign(type: WrappedType): Boolean {
        resolve(type) ?: return true
        if (type.isConst) {
            return false
        }
        if (type.isReturnable) {
            return true
        }
        val resolvedClass = tracker.classes[type.toString()] ?: return true
        if (resolvedClass.metadata.hasPrivateConstField) {
            return false
        }
        // A `= delete`d/non-public copy assignment is filtered out before it becomes a
        // child, but it was recorded in metadata at parse time: the field's `field = *value`
        // setter won't compile, so the type is unassignable (T-skip drops the setter).
        if (resolvedClass.metadata.hasDeletedCopyAssignment) {
            return false
        }
        resolvedClass.children.filterIsInstance<WrappedMethod>()
            .find { Operator.from(it) == BasicAssignmentOperator.ASSIGN }
            ?.let {
                return true
            }
        return resolvedClass.children.filterIsInstance<WrappedField>().all { f ->
            canAssign(f.type)
        }
    }

    // True unless [type] resolves to a class whose copy constructor is `= delete`d or
    // non-public. A by-value return/field is materialized by copy-constructing into a
    // Holder buffer (placement-new), so a non-copy-constructible element type is
    // unmodelable and the method/field must be dropped (T-skip). Reads the metadata flag
    // recorded at parse time: the offending constructor cursor is filtered out (non-public/
    // NotAvailable) before it ever becomes a resolved child, so it can't be re-derived from
    // children here. Conservative: any type we can't find, or one with no deleted-copy
    // record, is treated as copyable so ordinary implicitly-copyable classes (which declare
    // no copy ctor at all) are unaffected.
    suspend fun canCopyConstruct(type: WrappedType): Boolean {
        resolve(type) ?: return true
        val key = if (type.isConst) type.unconst else type
        val resolvedClass = tracker.classes[key.toString()]
            ?: tracker.classes[type.toString()] ?: return true
        return !resolvedClass.metadata.hasDeletedCopyConstructor
    }

    suspend fun <T> notifyFailed(
        element: WrappedElement?,
        type: WrappedType?,
        message: String
    ): T? {
        // Ledger every resolve-phase drop so the end-of-run report can diff it against
        // the requested allowlist. This is the central choke point: every model element
        // that fails to resolve returns through here, so recording once covers the
        // method/field/class drop sites uniformly. The (debug-gated) log line stays for
        // the live KRAPPER_DEBUG_RESOLVE trace.
        DropLedger.record(
            symbol = element?.toString() ?: type?.toString() ?: "<unknown>",
            reason = message,
            phase = DropPhase.RESOLVE
        )
        if (debugFilter?.invoke(element, type, message) == true) {
            Log.w("$element failed resolving $type: $message")
        }
        return null
    }

    companion object {
        val Empty: ResolveContext
            get() = ResolveContext(
                ResolveTracker(mutableMapOf()),
                object : Resolver {
                    override suspend fun resolve(
                        type: WrappedType,
                        context: ResolveContext
                    ): Pair<ResolvedClass, WrappedClass>? = null

                    override fun resolveTemplate(
                        type: WrappedType,
                        context: ResolveContext
                    ): WrappedTemplate = error("Not found")

                    override suspend fun findClasses(filter: ElementFilter): List<WrappedClass> =
                        emptyList()
                },
                { _, _ -> RemoveElement },
                NameHandler(),
                NameHandler.Empty
            )
    }
}

private fun typeMapper(policy: ReferencePolicy): TypeMapping {
    val mapper: TypeMapping = when (policy) {
        ReferencePolicy.IGNORE_MISSING -> { t, context ->
            t.operateOn {
                if (context.tracker.canResolve(it, context)) {
                    ElementUnchanged
                } else {
                    RemoveElement
                }
            }
        }

        ReferencePolicy.OPAQUE_MISSING -> { t, context ->
            t.operateOn {
                if (context.tracker.canResolve(it, context)) {
                    ElementUnchanged
                } else {
                    ReplaceWith(pointerTo(WrappedType.VOID))
                }
            }
        }

        ReferencePolicy.THROW_MISSING -> { t, context ->
            t.operateOn {
                if (context.tracker.canResolve(it, context)) {
                    ElementUnchanged
                } else {
                    throw IllegalStateException("Cannot resolve $it")
                }
            }
        }

        ReferencePolicy.INCLUDE_MISSING -> { t, context ->
            t.operateOn {
                if (it.isArray) return@operateOn RemoveElement
                if (!context.tracker.canResolve(it, context)) {
                    try {
                        if (context.tracker.resolvedClasses[it.toString()] != null) {
                            return@operateOn RemoveElement
                        }
                        // Cycle guard: an outer frame may already be expanding this very
                        // leaf (Clang's AST is densely cyclic — mutual Decl refs, CRTP
                        // bases, pointer/ref members back into the hierarchy). If so, don't
                        // recurse; the outer frame will store it. Key on the leaf `it` —
                        // exactly what canResolve queries — NOT the wrapped carrier `t`
                        // (e.g. `const clang::X &`). When a class is reached only through a
                        // wrapped carrier, a `t`-keyed marker never matches the leaf the
                        // guard checks, so a base/member edge re-entering the in-flight
                        // class isn't deduped and resolution descends until the native
                        // stack overflows.
                        if (context.tracker.otherResolved.contains(it.toString())) {
                            return@operateOn ElementUnchanged
                        }
                        context.tracker.otherResolved.add(it.toString())
                        try {
                            // Reference expansion: keep the legacy hard-fail when a
                            // pulled class's primary base can't resolve (don't
                            // resurrect transitively-referenced subclasses + their
                            // incomplete-at-emit bases). Listed/found classes still
                            // drop-to-borrowed via their own resolve context.
                            val (resolved, wrapper) =
                                context.resolver.resolve(
                                    it,
                                    context.copy(dropUnresolvablePrimaryBase = false)
                                ) ?: error("Couldn't include $it, resolve failed")
                            if (!resolved.isNotEmpty()) {
                                return@operateOn RemoveElement
                            }
                            context.tracker.resolvedClasses[resolved.type.toString()] = resolved
                            context.tracker.classes[wrapper.type.toString()] = wrapper
                        } finally {
                            context.tracker.otherResolved.remove(it.toString())
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
    // INCLUDE_MISSING binds the basic_string class itself (so std::string returns surface
    // as the wrapped Basic_string__Char) — leave it untouched so its output stays
    // byte-identical. Under the missing-tolerant policies, first normalize an unbound
    // canonical `basic_string<char>` to `std::string` so it survives as a STRING boundary
    // instead of being dropped/opaqued. See [normalizeMissingCharString].
    if (policy == ReferencePolicy.INCLUDE_MISSING) return mapper
    return { t, context ->
        val normalized = t.normalizeMissingCharString(context)
        val result = mapper(normalized, context)
        // When normalization rewrote `t` (basic_string<char> -> std::string) but the mapper
        // then left the result unchanged (std::string resolves cleanly), `map()` would read
        // ElementUnchanged as "keep the ORIGINAL `t`" and discard the rewrite. Surface the
        // normalized type as a ReplaceWith so the std::string actually flows through.
        if (normalized.toString() == t.toString()) {
            result
        } else {
            when (result) {
                ElementUnchanged -> ReplaceWith(normalized)
                else -> result
            }
        }
    }
}

// libstdc++ spells std::string's canonical type as the ABI-tagged template
// `std::__cxx11::basic_string<char,...>` (or the plain `std::basic_string<char,...>`),
// NOT the literal `std::string`. A method returning one by value (e.g. Clang's
// `QualType::getAsString` / `NamedDecl::getNameAsString`) thus carries a template type
// that — under a scoped (`only(...)`) import whose allowlist doesn't bind basic_string —
// matches neither the STRING fast-path nor any tracked class, so a missing-tolerant
// policy silently DROPS the whole method (self-bootstrap GAP B).
private val CHAR_BASIC_STRING_BASES = setOf("std::basic_string", "std::__cxx11::basic_string")

/**
 * Normalize an unbound canonical `basic_string<char,...>` anywhere in [this] to the
 * always-available `std::string`, so it rides the existing STRING -> `const char*` ->
 * Kotlin String marshalling (the same destination [Parsing.rewriteViewReturns] routes
 * `llvm::StringRef` to) instead of being dropped/opaqued as a missing reference. char-only:
 * `wstring`/`u16string`/`u32string` (`basic_string<wchar_t>`/`<char16_t>`/`<char32_t>`) are
 * NOT `char*` strings, so they are left untouched and follow the normal policy behavior.
 *
 * Descends only carriers the STRING boundary can actually marshal: by-value, `const`, and
 * reference (`&`), re-wrapping the basic_string node through them. A POINTER (`*`) or array
 * (`[]`) carrier is a HARD STOP — a `std::string*` out-param (e.g. `clang::Decl::getAvailability(
 * std::string*)`) can't ride the `const char*` boundary: the cast-var is declared with the
 * pointer-typed `targetType`, so normalizing it would emit `std::string* x = std::string(arg)`,
 * which does not compile and aborts the whole wrapper build. Leaving such a type unbound makes
 * the arg/method DROP cleanly (the pre-GAP-B behavior) instead of producing broken codegen.
 * Matches the basic_string node even for the multi-arg `<char, char_traits<char>,
 * allocator<char>>` spelling. Gated on the basic_string class being UNRESOLVABLE: when it IS
 * bound (an explicit allowlist entry) the type is left as the wrapped class.
 */
private suspend fun WrappedType.normalizeMissingCharString(context: ResolveContext): WrappedType =
    when {
        // Pointer / array carrier: cannot marshal through the const char* boundary — stop.
        this is WrappedModifiedType && (modifier == "*" || modifier == "[]") -> this

        this is WrappedModifiedType ->
            WrappedModifiedType(baseType.normalizeMissingCharString(context), modifier)

        this is WrappedPrefixedType ->
            WrappedPrefixedType(baseType.normalizeMissingCharString(context), modifier)

        this is WrappedTemplateType &&
            baseType.toString() in CHAR_BASIC_STRING_BASES &&
            templateArgs.firstOrNull()?.toString() == "char" &&
            !context.tracker.canResolve(this, context) ->
            WrappedType("std::string")

        // A non-string template still carries value-position args (e.g. a by-value
        // `pair<std::string,int>`); descend into them so a nested marshallable string survives.
        this is WrappedTemplateType ->
            WrappedTemplateType(
                baseType,
                templateArgs.map {
                    it.normalizeMissingCharString(context)
                }
            )

        else -> this
    }

suspend fun WrappedType.operateOn(typeHandler: suspend (WrappedType) -> MapResult): MapResult {
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

private suspend fun WrappedTemplateType.handleTemplate(
    typeHandler: suspend (WrappedType) -> MapResult
): MapResult {
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

inline fun MapResult.wrapOnReplace(typeWrapping: (WrappedType) -> WrappedType): MapResult =
    when (this) {
        is ReplaceWith -> ReplaceWith(typeWrapping(replacement))
        RemoveElement -> this
        ElementUnchanged -> this
    }

fun toResolvedCppType(type: WrappedType) = ResolvedCppType(
    type.toString(),
    if (type.isPointer) {
        nullable(toResolvedKotlinType(type.kotlinType))
    } else {
        toResolvedKotlinType(type.kotlinType)
    },
    toResolvedCType(type.cType),
    when {
        type.isString -> STRING_CAST
        type.isPointer && type.pointed.isString -> POINTED_STRING_CAST
        type.isNative || (type.isPointer && type.pointed.isNative) -> NATIVE
        else -> CAST
    },
    funcPointer = (type as? WrappedFunctionPointer)?.let {
        // Use the original C++ element types (not their reduced cType forms) so the
        // re-declared typedef is identical to the one in the user's header. A
        // typedef redefinition with the same type is legal C++; a reduced form
        // (e.g. an enum arg collapsed to `unsigned int`, or a reference to `void*`)
        // would instead be a conflicting redefinition and fail to compile.
        ResolvedFunctionPointer(
            it.cName,
            it.returnType.toString(),
            it.argTypes.map { arg -> arg.toString() },
            it.cppName
        )
    }
)

fun toResolvedCType(type: WrappedType) = ResolvedCType(
    type.toString(),
    type.isVoid,
    cppName = (type as? WrappedFunctionPointer)?.cppName?.takeIf { it != type.cName }
)

fun toResolvedKotlinType(kotlinType: WrappedKotlinType): ResolvedKotlinType {
    if (kotlinType is EnumKotlinType) {
        // A generated `enum class`: not a `.ptr`-backed wrapper, so isWrapper=false.
        // The entries drive the enum-class declaration + boundary conversions; see
        // KotlinWriter.
        return ResolvedKotlinType(
            qualifyList = kotlinType.fullyQualified.first().trimEnd('?').split('.'),
            isWrapper = false,
            enumEntries = kotlinType.constants.map { ResolvedEnumEntry(it.name, it.value) },
            enumUnderlying = toResolvedKotlinType(kotlinType.underlying)
        )
    }
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
