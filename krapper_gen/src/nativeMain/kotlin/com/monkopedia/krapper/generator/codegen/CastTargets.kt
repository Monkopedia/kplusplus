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
package com.monkopedia.krapper.generator.codegen

import com.monkopedia.krapper.generator.GenerationContext
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCType
import com.monkopedia.krapper.generator.resolvedmodel.type.plainName

// The opaque-pointer C type the upcast helpers take and return (`void* D_as_B(void*)`).
// Shared by CppWriter (definition) and HeaderWriter (declaration).
internal val VOID_PTR = ResolvedCType("void*")

// One generated upcast: derived class [cls] -> a (transitive) public base [base].
// Backs `D.asB()` in Kotlin and the `D_as_B` static_cast helper in C.
data class CastTarget(val cls: ResolvedClass, val base: ResolvedClass) {
    // The base's plain Kotlin name (no nullable suffix), e.g. "Drawable2". Both the
    // Kotlin method name (`as<KotlinName>`) and the C helper name are derived from it,
    // so the name is stable across the Kotlin / C / header writers.
    val baseKotlinName: String
        get() = base.type.kotlinType.plainName

    // The Kotlin upcast member name, e.g. `asDrawable2`.
    val kotlinMethodName: String
        get() = "as$baseKotlinName"

    // The synthetic C helper name, e.g. `Widget2_as_Drawable2`. Built identically in
    // CppWriter (emits the body), HeaderWriter (emits the declaration) and KotlinWriter
    // (calls it) — like a SIZE_OF uniqueCName, it must be derivable the same way in each.
    val cHelperName: String
        get() = "${cName(cls)}_as_${cName(base)}"

    companion object {
        // Same transform as NameHandler.NamerImpl.cName (type.toString().cleanupName()).
        // No global-dedup step needed here: two distinct classes can't share a type
        // string, so D_as_B is collision-free by construction.
        private fun cName(cls: ResolvedClass): String = cls.type.toString().cleanupName()
    }
}

// Every (D, B) upcast helper to generate for [cls]: each public, non-virtual base in the
// FULL transitive superclass set (primary + secondary). A virtual base (diamond) is
// skipped (no vbase-offset support); a base whose binding wasn't generated is skipped
// (no wrapper type to return). [lookup] resolves a base's C++ type string to its binding.
fun castTargetsFor(cls: ResolvedClass, lookup: (String) -> ResolvedClass?): List<CastTarget> {
    val result = mutableListOf<CastTarget>()
    val seen = mutableSetOf<String>()
    // BFS over direct bases, then their bases, etc. A virtual base on this path is
    // skipped (no vbase-offset support); we stop exploring that path's sub-tree. If
    // those same ancestors are reachable via another non-virtual path, `seen` lets
    // them through normally.
    val queue = ArrayDeque(cls.allBaseClasses)
    while (queue.isNotEmpty()) {
        val multiBase = queue.removeFirst()
        if (multiBase.isVirtualBase || !multiBase.isPublic) continue
        val baseTypeStr = multiBase.type.toString()
        if (!seen.add(baseTypeStr)) continue
        val baseCls = lookup(baseTypeStr) ?: continue
        result.add(CastTarget(cls, baseCls))
        queue.addAll(baseCls.allBaseClasses)
    }
    return result
}

// One generated DOWN-cast: base class [base] -> a (transitive) bound derived [derived].
// Backs the nullable `B.asD(): D?` in Kotlin and the `B_dyncast_D` C helper, a checked
// cast that returns the `D*` only when the runtime object really is a `D` (else null).
//
// [useLlvmDynCast] picks the runtime-check MECHANISM (see downCastTargetsFor): true ->
// LLVM-style `llvm::dyn_cast_or_null<D>` via the derived's static `classof` (the only
// thing that works when the library is compiled `-fno-rtti`, e.g. Clang/LLVM); false ->
// the generic `dynamic_cast<D*>` (standard C++ with RTTI on).
data class DownCastTarget(
    val base: ResolvedClass,
    val derived: ResolvedClass,
    val useLlvmDynCast: Boolean
) {
    // The derived's plain Kotlin name (no nullable suffix), e.g. "Derived1".
    val derivedKotlinName: String
        get() = derived.type.kotlinType.plainName

    // The Kotlin down-cast member name, e.g. `asDerived1` (returns a nullable `Derived1?`).
    val kotlinMethodName: String
        get() = "as$derivedKotlinName"

    // The synthetic C helper name, e.g. `Base_dyncast_Derived1`. Distinct `_dyncast_`
    // infix (vs the up-cast's `_as_`) so a bidirectionally-cast B/D pair can't collide.
    // Derived identically in CppWriter / HeaderWriter / KotlinWriter (like cName above).
    val cHelperName: String
        get() = "${cName(base)}_dyncast_${cName(derived)}"

    companion object {
        private fun cName(cls: ResolvedClass): String = cls.type.toString().cleanupName()
    }
}

// True when [cls] declares a static `classof` member — the LLVM-style-RTTI signal. LLVM's
// `dyn_cast<T>` routes the runtime check through `T::classof(const Base*)` (a discriminator
// /Kind-enum check), so such a type is castable WITHOUT C++ RTTI and, being compiled
// `-fno-rtti`, can NOT use `dynamic_cast`. `classof` survives resolution as a STATIC method
// (confirmed against the real Clang AST: `clang::TypeDecl::classof` et al. bind cleanly).
private fun hasLlvmClassof(cls: ResolvedClass): Boolean =
    cls.children.filterIsInstance<ResolvedMethod>()
        .any { it.methodType == MethodType.STATIC && it.name == "classof" }

// True when [cls] itself declares a virtual member (method or destructor) — i.e. it has
// its own vtable. `dynamic_cast<D*>(b)` requires the OPERAND type (the base) to be
// polymorphic; a non-polymorphic base has no RTTI to check, so no checked down-cast from
// it is possible (the only safe view would be an unchecked reinterpret, which defeats the
// "null when not a D" contract). We therefore skip the generic-RTTI down-cast for such a
// base. NOTE (backlog): this checks the class's OWN virtuals, not virtuals inherited from
// a polymorphic base; a class made polymorphic only by an unbound/transitive base would be
// missed. Good enough for the demo + featuregen shapes (a polymorphic base declares — or,
// via its dtor, inherits a declared — virtual directly); revisit if a real case needs the
// transitive walk.
private fun declaresVirtual(cls: ResolvedClass): Boolean =
    cls.children.any { (it as? ResolvedMethod)?.isVirtual == true }

// Every (B, D) DOWN-cast helper to generate for base [base]: the inverse of castTargetsFor.
// We scan ALL bound classes [allClasses] and, for each, reuse castTargetsFor to ask "which
// (transitive, non-virtual, public) bases does it reach"; whenever that set contains [base],
// `base.as<Derived>()` is a valid down-cast. Mechanism per pair: LLVM `dyn_cast` if the
// derived has a static `classof`, else generic `dynamic_cast`. [lookup] resolves a base's
// C++ type string to its binding (shared with castTargetsFor's BFS).
fun downCastTargetsFor(
    base: ResolvedClass,
    allClasses: List<ResolvedClass>,
    lookup: (String) -> ResolvedClass?
): List<DownCastTarget> {
    val baseTypeStr = base.type.toString()
    val basePolymorphic = declaresVirtual(base)
    val result = mutableListOf<DownCastTarget>()
    for (derived in allClasses) {
        if (derived.type.toString() == baseTypeStr) continue
        val reachesBase = castTargetsFor(derived, lookup).any {
            it.base.type.toString() == baseTypeStr
        }
        if (!reachesBase) continue
        val useLlvmDynCast = hasLlvmClassof(derived)
        // A `-fno-rtti` target library (config.noRtti) exports no `typeinfo` symbols, so the
        // generic `dynamic_cast<D*>` path below would reference a `typeinfo for D` the library
        // doesn't provide and fail to LINK (e.g. v8's TracingController / ExternalStringResource
        // against the -fno-rtti monolith). The LLVM `classof` path needs no RTTI, so keep it;
        // drop only the generic-dynamic_cast pairs.
        if (GenerationContext.noRtti && !useLlvmDynCast) continue
        // The generic `dynamic_cast` path needs a POLYMORPHIC base (RTTI to check). A
        // non-polymorphic base with no LLVM `classof` on the derived has no safe checked
        // down-cast at all — skip it (drop, don't emit non-compiling C). The LLVM path
        // does not need a polymorphic base (it's a Kind-enum check), so it's exempt.
        if (!useLlvmDynCast && !basePolymorphic) continue
        result.add(DownCastTarget(base, derived, useLlvmDynCast))
    }
    return result
}
