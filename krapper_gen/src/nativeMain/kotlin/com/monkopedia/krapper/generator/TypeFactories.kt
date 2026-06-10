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

import clang.CXCursor
import clang.CXCursorKind.CXCursor_ClassDecl
import clang.CXCursorKind.CXCursor_ClassTemplate
import clang.CXCursorKind.CXCursor_EnumConstantDecl
import clang.CXCursorKind.CXCursor_EnumDecl
import clang.CXCursorKind.CXCursor_NoDeclFound
import clang.CXCursorKind.CXCursor_StructDecl
import clang.CXCursorKind.CXCursor_TemplateTypeParameter
import clang.CXCursorKind.CXCursor_TypeAliasDecl
import clang.CXCursorKind.CXCursor_TypedefDecl
import clang.CXType
import clang.CXTypeKind.CXType_FunctionProto
import clang.CXTypeKind.CXType_Invalid
import clang.CXTypeKind.CXType_Pointer
import clang.CXTypeKind.CXType_RValueReference
import clang.CXTypeKind.CXType_Unexposed
import com.monkopedia.krapper.generator.model.type.WrappedEnumConstant
import com.monkopedia.krapper.generator.model.type.WrappedEnumType
import com.monkopedia.krapper.generator.model.type.WrappedFunctionPointer
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.UNRESOLVABLE
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.const
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.referenceTo
import com.monkopedia.krapper.generator.model.type.WrappedTypedefRef
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents

// FRONT-END SEAM (issue #44): construction of the pure WrappedType model from libclang-C
// CXTypes. Lives outside model/type/ so the type model itself carries no clang/cinterop
// dependency; an alternative front-end builds the same types through the string/companion
// factories instead.

operator fun WrappedType.Companion.invoke(
    type: CValue<CXType>,
    resolverBuilder: ResolverBuilder
): WrappedType = WrappedType(type, resolverBuilder, throwOnError = false)

operator fun WrappedType.Companion.invoke(
    type: CValue<CXType>,
    resolverBuilder: ResolverBuilder,
    throwOnError: Boolean
): WrappedType {
    val kind = type.useContents { kind }
    try {
        if (kind == CXType_Invalid) {
            throw IllegalArgumentException("Invalid type")
        } else if (kind == CXType_RValueReference) {
            throw IllegalArgumentException("RValues unsupported at the moment")
        }
        val spelling = type.spelling.toKString()
        if (spelling?.endsWith("*") == true) {
            return pointerTo(WrappedType(type.pointeeType, resolverBuilder))
                .maybeConst(type.isConstQualifiedType)
        }
        if (spelling?.endsWith("&") == true) {
            return referenceTo(WrappedType(type.pointeeType, resolverBuilder))
                .maybeConst(type.isConstQualifiedType)
        }
        // Check both the type and its canonical form for template arguments,
        // since some type representations (e.g. elaborated types) may not
        // report template arguments even when the canonical type has them.
        val templateType =
            if (type.numTemplateArguments > 0) {
                type
            } else {
                val canonical = type.canonicalType
                if (canonical.numTemplateArguments > 0) canonical else null
            }
        if (templateType != null) {
            val templateReference =
                createForType(type, resolverBuilder, forTemplateBase = true)
            return WrappedTemplateType(
                templateReference,
                List(templateType.numTemplateArguments) {
                    val tempType =
                        templateType.getTemplateArgumentType(it.toUInt())
                    if (tempType.useContents { kind } == CXType_Invalid) {
                        null
                    } else {
                        WrappedType(tempType, resolverBuilder)
                    }
                }.filterNotNull()
            ).maybeConst(type.isConstQualifiedType)
        }
        return createForType(type, resolverBuilder).maybeConst(type.isConstQualifiedType)
    } catch (t: RuntimeException) {
        // Skip-not-crash: createForType() reaches several error() calls
        // (IllegalStateException) for a missing spelling / usr / declaration —
        // not only IllegalArgumentException. Treat any of these as "this one
        // type is unmodelable": drop it (UNRESOLVABLE) and let the element that
        // referenced it fall out during resolution, rather than aborting the run.
        if (throwOnError) {
            throw IllegalArgumentException(
                "Failed to create type for ${type.spelling.toKString()}",
                t
            )
        } else {
            return UNRESOLVABLE
        }
    }
}

private inline fun WrappedType.maybeConst(isConst: Boolean): WrappedType =
    if (isConst) const(this) else this

private fun createForType(
    type: CValue<CXType>,
    resolverBuilder: ResolverBuilder,
    forTemplateBase: Boolean = false
): WrappedType {
    // Capture the original declaration before visit() follows typedefs to their
    // underlying type. Standard container `reference`/`const_reference` aliases must
    // be reduced to `value_type&`/`const value_type&` here, because their underlying
    // type is a dependent trait expression libclang can't resolve (see
    // referenceTypedefElement). Without this, accessors returning them are dropped.
    val originalDecl = type.typeDeclaration
    if (originalDecl.kind == CXCursor_TypedefDecl) {
        functionPointerTypedefElement(originalDecl, resolverBuilder)?.let { return it }
        referenceTypedefElement(originalDecl, resolverBuilder)?.let { return it }
        pointerTypedefElement(originalDecl, resolverBuilder)?.let { return it }
        assocTypedefElement(originalDecl)?.let { return it }
        sizeTypedefElement(originalDecl)?.let { return it }
        cAliasTypedefElement(originalDecl)?.let { return it }
    }
    // C++11 `using` aliases (e.g. unique_ptr's `pointer`) are TypeAliasDecl, not
    // TypedefDecl; pointerTypedefElement self-guards (alias name + dependent underlying).
    if (originalDecl.kind == CXCursor_TypeAliasDecl) {
        pointerTypedefElement(originalDecl, resolverBuilder)?.let { return it }
    }
    val type = resolverBuilder.visit(type)
    var spelling =
        type.spelling.toKString()?.trim() ?: error("Missing spelling for type $type")
    if (spelling.startsWith("const ")) {
        spelling = spelling.substring("const ".length)
    }
    val fullSpelling = spelling
    if (spelling.contains('<')) {
        spelling = spelling.substring(0, spelling.indexOf('<'))
    }
    val referencedDecl = type.typeDeclaration
    return when {
        referencedDecl.kind == CXCursor_TypedefDecl -> {
            referenceTypedefElement(referencedDecl, resolverBuilder)
                ?: pointerTypedefElement(referencedDecl, resolverBuilder)
                ?: assocTypedefElement(referencedDecl)
                ?: sizeTypedefElement(referencedDecl)
                ?: WrappedTypedefRef(
                    referencedDecl.usr.toKString() ?: error("Declaration missing usr")
                )
        }

        referencedDecl.kind == CXCursor_TemplateTypeParameter -> {
            WrappedTemplateRef(
                referencedDecl.usr.toKString() ?: error("Declaration missing usr")
            )
        }

        referencedDecl.kind == CXCursor_ClassTemplate -> {
            val baseType = WrappedType(referencedDecl.fullyQualified)
            if (!forTemplateBase && fullSpelling.contains('<')) {
                // Template args exist in spelling but the caller didn't detect them
                // (clang doesn't report numTemplateArguments for some type kinds).
                // Reconstruct the full type with template args from the spelling.
                val templateArgsPart =
                    fullSpelling.substring(fullSpelling.indexOf('<'))
                WrappedType(referencedDecl.fullyQualified + templateArgsPart)
            } else {
                baseType
            }
        }

        referencedDecl.kind == CXCursor_ClassDecl -> {
            WrappedType(referencedDecl.fullyQualified)
        }

        referencedDecl.kind == CXCursor_StructDecl -> {
            WrappedType(referencedDecl.fullyQualified)
        }

        referencedDecl.kind == CXCursor_EnumDecl -> {
            // Enum: dual identity (real spelling + underlying integer) so the
            // method survives resolution and casts at the C++ boundary. Also
            // recover the constants (name + value) so the Kotlin binding can
            // surface a real `enum class`. See WrappedEnumType.
            val constants = referencedDecl.children
                .filter { it.kind == CXCursor_EnumConstantDecl }
                .mapNotNull {
                    val name = it.spelling.toKString() ?: return@mapNotNull null
                    WrappedEnumConstant(name, it.enumValue)
                }
            WrappedEnumType(
                referencedDecl.fullyQualified,
                WrappedType(referencedDecl.integerType, resolverBuilder),
                constants
            )
        }

        type.useContents { kind } == CXType_Unexposed &&
            referencedDecl.kind == CXCursor_NoDeclFound &&
            !spelling.startsWith("typename ") -> {
            WrappedTemplateRef(spelling)
        }

        else -> {
            WrappedType(spelling)
        }
    }
}

// Only plain native scalars, void, and `void*` can be re-declared verbatim in
// the C interop header; enums/refs/class types name C++ types not in scope
// there. `void*` is allowed because it re-declares verbatim as `void*` (valid
// C) and is the context slot used by Mode-1 callback APIs (`int(*)(void*,int)`).
private val WrappedType.isCFunctionPointerCompatible: Boolean
    get() = isVoid || (isNative && !isString) || (isPointer && pointed.isVoid)

/**
 * A typedef over a pointer-to-function-proto (e.g. `typedef int (*IntTransform)(int);`)
 * is the C function-pointer shape used by callback-style APIs. libclang gives the
 * param/return as a typedef whose underlying declarator can't be followed through the
 * normal paths, so methods using it would be silently dropped. Capture it as a
 * [WrappedFunctionPointer] — an opaque NATIVE pointer named by its typedef spelling —
 * so the method survives resolution and emits a valid `CPointer<CFunction<...>>?` on
 * the Kotlin side. Returns null for any other typedef (normal handling applies); the
 * map/set/vector typedef paths depend on that fall-through.
 */
private fun functionPointerTypedefElement(
    typedefDecl: CValue<CXCursor>,
    resolverBuilder: ResolverBuilder
): WrappedType? {
    val underlying = typedefDecl.typedefDeclUnderlyingType
    if (underlying.useContents { kind } != CXType_Pointer) return null
    val proto = underlying.pointeeType
    if (proto.useContents { kind } != CXType_FunctionProto) return null
    val returnType = WrappedType(proto.result, resolverBuilder)
    val argTypes = (0 until proto.numArgTypes).map {
        WrappedType(proto.getArgType(it.toUInt()), resolverBuilder)
    }
    // Stage 1 only handles function pointers whose return + every argument is a
    // plain native scalar or void — the typedef can then be re-declared verbatim
    // in the generated C interop header. Signatures touching C++ types (enums,
    // references, class types, e.g. libstdc++'s `event_callback`) can't be
    // re-declared in the C header without the user header in scope, so we fall
    // through to the prior behavior (the method drops) rather than emit a typedef
    // that conflicts with the real one. Richer signatures arrive in a later stage.
    if (!returnType.isCFunctionPointerCompatible) return null
    if (argTypes.any { !it.isCFunctionPointerCompatible }) return null
    val cName = typedefDecl.spelling.toKString()
        ?: error("Function pointer typedef missing spelling")
    // A namespace-scoped typedef (`nn::handler_t`) must be spelled qualified in
    // the generated C++ wrapper; `fullyQualified` prepends the enclosing
    // namespace path (and equals the bare `cName` for a global typedef).
    val cppName = typedefDecl.fullyQualified.takeIf { it.isNotEmpty() } ?: cName
    return WrappedFunctionPointer(cName, returnType, argTypes, cppName)
}

/**
 * Standard containers expose `reference`/`const_reference` member typedefs that are,
 * by contract, `value_type&` / `const value_type&`. In practice (e.g. libstdc++)
 * these are defined through dependent trait expressions such as
 * `__gnu_cxx::__alloc_traits<...>::reference`, which libclang leaves as an
 * unresolvable dependent type. Without reducing them, accessors returning them
 * (operator[], at, front, back, ...) get silently dropped during resolution.
 *
 * When we encounter such a typedef, reconstruct it from the sibling `value_type`
 * typedef in the same class so the existing element-reference return path applies.
 * Returns null when the typedef isn't one of these aliases, so the caller falls
 * back to the normal (opaque) typedef handling.
 */
private fun referenceTypedefElement(
    typedefDecl: CValue<CXCursor>,
    resolverBuilder: ResolverBuilder
): WrappedType? {
    val name = typedefDecl.spelling.toKString()
    val isConstRef = name == "const_reference"
    if (name != "reference" && !isConstRef) return null
    // Only intervene when the typedef's underlying type is dependent/unexposed,
    // i.e. it didn't resolve to a concrete reference on its own.
    val underlyingKind = typedefDecl.typedefDeclUnderlyingType.useContents { kind }
    if (underlyingKind != CXType_Unexposed) return null
    val valueTypedef = typedefDecl.semanticParent.children.firstOrNull {
        it.kind == CXCursor_TypedefDecl && it.spelling.toKString() == "value_type"
    } ?: return null
    val elementType = WrappedType(valueTypedef.typedefDeclUnderlyingType, resolverBuilder)
    if (elementType == UNRESOLVABLE) return null
    // Restrict to sequence-style containers whose value_type is a simple element
    // (e.g. vector<T>'s `_Tp`, or a plain named/native type). Associative containers
    // such as std::map have value_type = std::pair<const Key, T>; reducing those
    // would surface a reference to a type with const members that cannot be wrapped,
    // so we leave them with their prior (opaque) handling.
    if (elementType is WrappedTemplateType) return null
    return referenceTo(if (isConstRef) const(elementType) else elementType)
}

/**
 * Smart pointers (e.g. `std::unique_ptr<T>`) expose a `pointer` member alias that, by
 * contract, is `T*`. libclang leaves it as a dependent/unexposed type, so accessors
 * returning it (`get()`, `release()`, `operator->()`) get silently dropped during
 * resolution — the same failure mode as container `reference`/`const_reference`.
 *
 * When we encounter such a typedef, reconstruct it as a pointer to the sibling
 * `element_type` typedef in the same class (whose underlying type is the template
 * param `_Tp`). The existing `_Tp -> concrete` substitution engine then maps
 * `_Tp*` to e.g. `int*`. Returns null when the typedef isn't one of these aliases,
 * or when its underlying type is already a concrete pointer (a real `T*` typedef
 * must be left untouched), so the caller falls back to normal typedef handling.
 */
private fun pointerTypedefElement(
    typedefDecl: CValue<CXCursor>,
    resolverBuilder: ResolverBuilder
): WrappedType? {
    val name = typedefDecl.spelling.toKString()
    val isConstPtr = name == "const_pointer"
    if (name != "pointer" && !isConstPtr) return null
    // Only intervene when the alias's underlying type is dependent/unexposed —
    // a concrete resolved `T*` pointer typedef must fall through to normal handling.
    val underlyingKind = typedefDecl.typedefDeclUnderlyingType.useContents { kind }
    if (underlyingKind != CXType_Unexposed) return null
    val elementTypedef = typedefDecl.semanticParent.children.firstOrNull {
        (it.kind == CXCursor_TypedefDecl || it.kind == CXCursor_TypeAliasDecl) &&
            it.spelling.toKString() == "element_type"
    } ?: return null
    val elementType = WrappedType(elementTypedef.typedefDeclUnderlyingType, resolverBuilder)
    if (elementType == UNRESOLVABLE) return null
    return pointerTo(if (isConstPtr) const(elementType) else elementType)
}

/**
 * Key→value associative containers expose `key_type` / `mapped_type` member aliases
 * that, by contract, are the container's first / second template type parameters.
 * `std::map` defines them directly (libclang resolves them cleanly), but
 * `std::unordered_map` defines them through its `_Hashtable` base
 * (`typename _Hashtable::key_type` / `::mapped_type`), which libclang leaves as an
 * unresolvable dependent self-reference — so accessors touching them (`operator[]`/
 * `at`/`count`/`find`/`erase`) silently drop. Same failure mode as `reference`/`pointer`.
 *
 * Reconstruct the alias as a [WrappedTemplateRef] to the enclosing template's
 * corresponding type parameter, so the existing `_Key`/`_Tp -> concrete` engine maps
 * it. Returns null unless it's one of the recognized aliases on a class exposing the
 * required sibling typedefs with an unexposed/collapsed underlying — so std::map's
 * already-clean aliases and plain vectors are untouched.
 *
 * Two container shapes are recognized, distinguished by sibling typedefs:
 *  - key→value map (`key_type` AND `mapped_type`): `key_type`→param 0,
 *    `mapped_type`→param 1.
 *  - key-only set (`key_type` but NO `mapped_type`): both `key_type` and the
 *    self-referential `value_type` (set defines `value_type = key_type`, which
 *    libclang collapses to an unexposed self-reference) → param 0 (`_Key`). This
 *    resolves `std::set`'s `insert(const value_type&)`/`find` args to `const T&`.
 */
private fun assocTypedefElement(typedefDecl: CValue<CXCursor>): WrappedType? {
    val name = typedefDecl.spelling.toKString()
    // Only intervene when the alias collapsed to a dependent/unexposed self-reference;
    // a concretely-resolved alias (e.g. std::map's) should skip this reducer.
    val underlyingKind = typedefDecl.typedefDeclUnderlyingType.useContents { kind }
    if (underlyingKind != CXType_Unexposed) return null
    val siblings = typedefDecl.semanticParent.children
    fun hasTypedef(n: String) =
        siblings.any { it.kind == CXCursor_TypedefDecl && it.spelling.toKString() == n }
    val isMap = hasTypedef("key_type") && hasTypedef("mapped_type")
    // set/multiset/unordered_set: key_type present, no mapped_type.
    val isKeyOnly = hasTypedef("key_type") && !hasTypedef("mapped_type")
    val paramIndex = when {
        // key→value map: key_type→param 0, mapped_type→param 1.
        isMap && name == "key_type" -> 0

        isMap && name == "mapped_type" -> 1

        // key-only set: key_type/value_type both reduce to the key param (_Key).
        isKeyOnly && (name == "key_type" || name == "value_type") -> 0

        else -> return null
    }
    val typeParams = siblings.filter { it.kind == CXCursor_TemplateTypeParameter }
    val param = typeParams.getOrNull(paramIndex) ?: return null
    return WrappedTemplateRef(
        param.usr.toKString() ?: error("Template param missing usr")
    )
}

/**
 * Standard containers/strings expose integral member typedefs `size_type`
 * (= `std::size_t`) and `difference_type` (= `std::ptrdiff_t`) through dependent
 * trait expressions (e.g. `_Rep_type::size_type`, `__alloc_traits<...>::size_type`).
 * Without intervention, methods returning/taking them (`size()`, `count()`,
 * `length()`, `max_size()`, `erase(key)`, `resize(n)`, ...) either drop (when
 * libclang leaves the typedef unexposed) or leak the bare integer.
 *
 * `size_type` and `difference_type` are always normalized to the `size_t` /
 * `ptrdiff_t` aliases — so they surface as `platform.posix.<name>` (matching
 * cinterop) consistently across containers, and so methods survive when the
 * typedef was left unexposed.
 */
private fun sizeTypedefElement(typedefDecl: CValue<CXCursor>): WrappedType? =
    when (typedefDecl.spelling.toKString()) {
        "size_type" -> WrappedType("size_t")
        "difference_type" -> WrappedType("ptrdiff_t")
        else -> null
    }

// C typedef aliases whose Kotlin form is the platform-correct
// `platform.posix.<name>` (maintained per-target by cinterop). They are real
// typedefs over a plain integer, so without intervention libclang's visit()
// follows them to that integer and the alias is lost — the same mismatch as
// size_type, but for direct uses of the alias. (uintptr_t is intentionally
// excluded: it maps to COpaquePointer, not a platform.posix alias.)
private val C_ALIAS_TYPEDEFS =
    setOf("size_t", "ssize_t", "ptrdiff_t", "intptr_t", "wchar_t")

/**
 * Preserve a C typedef alias ([C_ALIAS_TYPEDEFS]) by representing it as the
 * alias name itself rather than letting visit() resolve it to the underlying
 * integer, so the Kotlin binding surfaces `platform.posix.<name>` consistently
 * with cinterop. Returns null for any other typedef (normal handling applies).
 */
private fun cAliasTypedefElement(typedefDecl: CValue<CXCursor>): WrappedType? {
    val name = typedefDecl.spelling.toKString()
    return if (name in C_ALIAS_TYPEDEFS) WrappedType(name!!) else null
}
