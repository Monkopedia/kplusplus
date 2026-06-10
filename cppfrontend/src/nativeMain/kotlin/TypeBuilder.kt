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
import clang.CXXRecordDecl
import clang.Decl
import clang.EnumConstantDecl
import clang.EnumDecl
import clang.EnumType
import clang.NamedDecl
import clang.QualType
import clang.TagDecl
import clang.TemplateTypeParmType
import clang.Type
import clang.TypedefNameDecl
import clang.TypedefType
import clang.decl.Kind
import clang.templateArgument.ArgKind
import clang.type.TypeClass
import com.monkopedia.krapper.generator.model.type.WrappedEnumConstant
import com.monkopedia.krapper.generator.model.type.WrappedEnumType
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.UNRESOLVABLE
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.const
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.referenceTo
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference

// STRUCTURAL type construction (#44 brick 4): decode a clang::QualType's type tree into the
// same WrappedType shapes the libclang-C front-end produces, rule-for-rule against
// krapper_gen's TypeFactories.kt (`WrappedType.Companion.invoke(CValue<CXType>)` +
// `createForType`). Each branch cites the TypeFactories rule it mirrors; every deliberate
// divergence is documented in place as a Phase C normalizer entry.

// QualType::getTypePtrOrNull() is declared to return the TypeApi *interface* (the generated
// related-object-getter convention); re-wrap to the concrete Type so the shape predicates
// (which live on the concrete class) resolve. Same pattern as the type-decode probe.
private fun QualType.typePtr(): Type? = getTypePtrOrNull()?.let { Type(it.ptr, memScope) }

// Type::getAsCXXRecordDecl() likewise returns the Api interface; re-wrap so the
// dyn-cast helpers + NamedDecl upcasts resolve.
private fun Type.asRecord(): CXXRecordDecl? =
    getAsCXXRecordDecl()?.let { CXXRecordDecl(it.ptr, memScope) }

// BRIDGE: the generated down-cast helper `Type.asTypedefType()` is missing — TypedefType's
// base list doesn't survive resolution (TypeBase.h: `class TypedefType final : public
// TypeWithKeyword, private llvm::TrailingObjects<...>`; the chain is TypedefType ->
// TypeWithKeyword -> KeywordWrapper<Type> -> Type), so CastTargets never records Type as a
// base and emits neither TypedefType.asType() nor the inverse dyn-cast (binding gap to fix
// in krapper_gen). Bridge with the exact mechanism the generated `B_dyncast_D` helper uses:
// TypedefType's static llvm `classof` (bound on its companion), then re-view the SAME
// pointer as the derived class — safe because Type is the address-identical primary base
// through that whole chain (KeywordHelpers contributes no state, so EBO applies) —
// precisely what the generated llvm::dyn_cast helper compiles to.
private fun Type.asTypedefTypeOrNull(): TypedefType? = with(TypedefType.Companion) {
    if (memScope.classof(this@asTypedefTypeOrNull)) TypedefType(ptr, memScope) else null
}

// BRIDGE: the same generated-helper gap as TypedefType above, for the brick-5 type
// classes — EnumType (TypeBase.h: `class EnumType final : public TagType`, the same
// TypeWithKeyword chain CastTargets can't resolve) and TemplateTypeParmType (TypeBase.h:
// `: public Type, public llvm::FoldingSetNode`, the unbound second base). Each check is
// EXACTLY what the class's static `classof` compiles to (`getTypeClass() == Enum` /
// `== TemplateTypeParm`), and Type is the address-identical primary base of both, so the
// kind check + same-pointer re-view is precisely the generated dyn-cast's semantics.
private fun Type.asEnumTypeOrNull(): EnumType? =
    if (getTypeClass() == TypeClass.Enum) EnumType(ptr, memScope) else null

private fun Type.asTemplateTypeParmTypeOrNull(): TemplateTypeParmType? =
    if (getTypeClass() == TypeClass.TemplateTypeParm) {
        TemplateTypeParmType(ptr, memScope)
    } else {
        null
    }

// TypeFactories.maybeConst: every shape gets the type's OWN const qualifier re-applied
// after construction (a pointee's qualifier instead nests inside, via the recursion).
private fun WrappedType.maybeConst(isConst: Boolean): WrappedType =
    if (isConst) const(this) else this

// cAliasTypedefElement's alias set (TypeFactories.C_ALIAS_TYPEDEFS): C typedefs whose
// Kotlin form is the platform-correct `platform.posix.<name>`, preserved by NAME instead
// of being collapsed to their underlying integer.
private val C_ALIAS_TYPEDEFS = setOf("size_t", "ssize_t", "ptrdiff_t", "intptr_t", "wchar_t")

/**
 * Decode [type]'s structure into a WrappedType, mirroring the libclang front-end's
 * TypeFactories dispatch (`WrappedType(CValue<CXType>, resolverBuilder)`):
 * pointer/reference recursion, template-specialization detection, the typedef reducers
 * and canonical collapse, then the record/builtin leaf — with const re-applied per level.
 */
fun buildWrappedType(type: QualType): WrappedType {
    // invoke: CXType_Invalid throws; the model-construction call sites all use
    // throwOnError=false, so an unmodelable type lands as UNRESOLVABLE (skip-not-crash).
    val ty = type.typePtr() ?: return UNRESOLVABLE
    val isConst = type.isConstQualified()

    // Alias sugar detection must come FIRST: clang's shape predicates (isPointerType & co)
    // classify the CANONICAL type, while TypeFactories' pointer/reference dispatch keys on
    // the SUGARED spelling (`endsWith("*")`/`endsWith("&")`) — and an alias spells its own
    // name, never taking those branches. TypedefType covers both `typedef` and `using`
    // (both are TypedefNameDecls).
    val typedef = ty.asTypedefTypeOrNull()?.getDecl()
    if (typedef == null) {
        // invoke: CXType_RValueReference throws "RValues unsupported" -> UNRESOLVABLE
        // under the call sites' throwOnError=false.
        if (ty.isRValueReferenceType()) return UNRESOLVABLE
        if (ty.isFunctionPointerType()) {
            // A BARE function-pointer (`int (*)(int)`) spells with a trailing ')' on the
            // libclang path, so it falls through every special case to createForType's
            // `else -> WrappedType(spelling)` — an opaque named leaf. Mirror that shape.
            // brick-6: the rich WrappedFunctionPointer only exists for a TYPEDEF over a
            // fn-pointer (functionPointerTypedefElement) — that reducer needs the
            // underlying-proto decode + the isCFunctionPointerCompatible recursion, so it
            // (and a fn-pointer fixture case) is deferred to the callbacks brick.
            val spelling = type.getAsString()?.trim() ?: return UNRESOLVABLE
            return WrappedTypeReference(spelling).maybeConst(isConst)
        }
        if (ty.isPointerType()) {
            // invoke: spelling.endsWith("*") -> pointerTo(invoke(pointeeType)).maybeConst.
            // A `const Shape*`'s const belongs to the POINTEE QualType, so it nests inside
            // through the recursion — `isConst` here is only a `T* const` top-level const,
            // exactly libclang's clang_isConstQualifiedType semantics.
            return pointerTo(buildWrappedType(ty.getPointeeType())).maybeConst(isConst)
        }
        if (ty.isReferenceType()) {
            // invoke: spelling.endsWith("&") -> referenceTo(invoke(pointeeType)).maybeConst.
            // Only lvalue refs reach here (rvalue refs returned UNRESOLVABLE above), so a
            // `const Shape&` decodes as &(const(Shape)) — const INSIDE the reference.
            return referenceTo(buildWrappedType(ty.getPointeeType())).maybeConst(isConst)
        }
    }

    // invoke lines 80-104: template arguments detected on the type itself OR its canonical
    // form (alias/elaborated sugar hides them) -> WrappedTemplateType(baseRef, args).
    // Structurally, every template-specialization TYPE's canonical form is a record whose
    // decl dyn-casts to ClassTemplateSpecializationDecl (type-decode probe, P5). Checked
    // BEFORE the typedef handling for the same reason invoke checks it before
    // createForType: an alias over a template type still collapses to the template shape.
    val canonical = type.getCanonicalType()
    val canonTy = canonical.typePtr()
    val spec = canonTy?.asRecord()?.asClassTemplateSpecializationDecl()
    if (spec != null) {
        // Base ref: createForType(forTemplateBase=true) resolves the specialization decl
        // (surfaced as a ClassDecl/StructDecl cursor) -> WrappedType(referencedDecl
        // .fullyQualified); NamedDecl::getQualifiedNameAsString() is the Decl-side spelling
        // of that same semantic-parent walk (template args are not part of either).
        val baseName = spec.getQualifiedNameAsString() ?: return UNRESOLVABLE
        val argList = spec.getTemplateArgs() ?: return UNRESOLVABLE
        val args = (0u until argList.size()).mapNotNull { i ->
            // TypeFactories keeps only TYPE arguments: getTemplateArgumentType yields
            // CXType_Invalid for a value arg, and the invalid slot is dropped
            // (filterNotNull) — the ArgKind.Type filter is that same rule decl-side.
            val arg = argList.get(i) ?: return@mapNotNull null
            if (arg.getKind() != ArgKind.Type) return@mapNotNull null
            buildWrappedType(arg.getAsType())
        }
        return WrappedTemplateType(WrappedTypeReference(baseName), args).maybeConst(isConst)
    }

    if (typedef != null) {
        return buildTypedefTargetType(typedef).maybeConst(isConst)
    }

    // Dependent template-parameter reference: createForType's CXCursor_TemplateTypeParameter
    // branch (TypeFactories) -> WrappedTemplateRef(the param DECL's USR) — NOT the canonical
    // `type-parameter-0-0` spelling and NOT the bare name `T`: the identity string ties the
    // use back to the matching WrappedTemplateParam.usr so substitution can key on it. This
    // front-end's identity is the same cpp:<canonical-id> convention (ModelBuilder.kt), so
    // the ref and the param agree. Read off the SUGARED type: the canonical
    // TemplateTypeParmType carries no decl (TypeBase.h: `assert(!TTPDecl == Canon.isNull())`).
    ty.asTemplateTypeParmTypeOrNull()?.let { parm ->
        val decl = parm.getDecl() ?: return UNRESOLVABLE
        return WrappedTemplateRef(Decl(decl.ptr, ty.memScope).cppUsr()).maybeConst(isConst)
    }

    // ---- createForType leaf dispatch (post-visit, i.e. against the canonical type) ----

    if (canonTy != null && canonTy.isConstantArrayType()) {
        // A constant array falls through createForType to `else -> WrappedType(spelling)`,
        // and libclang's TypePrinter spells it "elem [N]" (krapper_gen TestData golden:
        // "int [5]") — WrappedTypeReference models the array parsing on that exact name.
        // Reconstruct the same spelling structurally from element + extent.
        val array = canonTy.asConstantArrayType() ?: return UNRESOLVABLE
        val element = buildWrappedType(array.getElementType())
        return WrappedTypeReference("$element [${array.getZExtSize()}]").maybeConst(isConst)
    }
    canonTy?.asRecord()?.let { record ->
        // Record leaf: createForType's CXCursor_ClassDecl/StructDecl branches ->
        // WrappedType(referencedDecl.fullyQualified) — the "::"-joined semantic-parent
        // chain, which is exactly NamedDecl::getQualifiedNameAsString().
        val qualified = record.getQualifiedNameAsString() ?: return UNRESOLVABLE
        return WrappedTypeReference(qualified).maybeConst(isConst)
    }
    if (canonTy != null && canonTy.isEnumeralType()) {
        // Enum leaf: createForType's CXCursor_EnumDecl branch (TypeFactories) — the
        // dual-identity WrappedEnumType: cppName = the decl's fullyQualified (the
        // "::"-joined semantic-parent chain ≡ getQualifiedNameAsString, so a nested enum
        // spells `Palette::Flavor`), underlying = the decl's integer type
        // (clang_getEnumDeclIntegerType ≡ EnumDecl::getIntegerType — the EXPLICIT
        // underlying for a fixed enum, clang's COMPUTED one otherwise), constants = the
        // EnumConstantDecl children's (name, value) pairs. Scoped vs unscoped needs no
        // flag here: both spell their qualified name and both carry their underlying type
        // (the libclang path never consults clang_EnumDecl_isScoped in construction).
        val enumDecl = canonTy.asEnumTypeOrNull()?.getDecl()
            ?.let { EnumDecl(it.ptr, canonTy.memScope) } ?: return UNRESOLVABLE
        return buildEnumType(enumDecl).maybeConst(isConst)
    }
    // Builtin (and any remaining) leaf: createForType's `else -> WrappedType(spelling)`,
    // spelled from the canonical type — the structural equivalent of visit()'s collapse.
    val spelling = spellingOf(canonical) ?: return UNRESOLVABLE
    return WrappedTypeReference(spelling).maybeConst(isConst)
}

/**
 * The target type a typedef/alias DECL reduces to — shared between an alias used as a TYPE
 * (buildWrappedType's TypedefType branch) and the WrappedTypedef ELEMENT
 * (ModelBuilder.buildTypedef), both of which run the same TypeFactories reducer stack
 * against the ORIGINAL declaration (createForType's originalDecl block):
 *  - sizeTypedefElement: `size_type`/`difference_type` ALWAYS normalize to the
 *    `size_t`/`ptrdiff_t` aliases (so they surface as platform.posix.<name>).
 *  - cAliasTypedefElement: the C platform aliases are preserved as the alias NAME itself
 *    instead of collapsing to the underlying integer. (TypeFactories keys these on a
 *    CXCursor_TypedefDecl only; here they apply to either alias kind — a `using size_t =
 *    ...` is vanishingly rare and the name is the contract either way.)
 *  - referenceTypedefElement / pointerTypedefElement / assocTypedefElement reduce
 *    std-container member aliases whose underlyings are DEPENDENT trait expressions; they
 *    only fire on the std headers, so they ride with the std-scale brick (brick-6+).
 *  - Everything else collapses to the underlying type, mirroring ResolverBuilderImpl
 *    .visit()'s CXCursor_TypedefDecl branch (recursive typedefDeclUnderlyingType walk) —
 *    which is also how an in-template `typedef T value_type` lands on the dependent
 *    WrappedTemplateRef (the underlying TemplateTypeParmType branch above).
 *    DIVERGENCE (Phase C normalizer entry): for a C++ `using` alias (TypeAliasDecl) the
 *    libclang path is ORDER-DEPENDENT — visit() leaves the alias un-collapsed
 *    (createForType's `else` then emits the alias NAME as a leaf) unless the underlying's
 *    canonical spelling was already in visit()'s seenNames cache, in which case the cached
 *    record type collapses it anyway. This front-end always collapses both alias kinds —
 *    deterministic, never the order-dependent name leaf.
 */
fun buildTypedefTargetType(typedef: TypedefNameDecl): WrappedType {
    val name = typedef.getNameAsString() ?: ""
    when (name) {
        "size_type" -> return WrappedTypeReference("size_t")
        "difference_type" -> return WrappedTypeReference("ptrdiff_t")
    }
    if (name in C_ALIAS_TYPEDEFS) return WrappedTypeReference(name)
    return buildWrappedType(typedef.getUnderlyingType())
}

// The WrappedEnumType payload for [enumDecl] (TypeFactories' CXCursor_EnumDecl branch).
private fun buildEnumType(enumDecl: EnumDecl): WrappedType {
    val qualified = NamedDecl(enumDecl.ptr, enumDecl.memScope).getQualifiedNameAsString()
        ?: return UNRESOLVABLE
    // Constants: the decl's EnumConstantDecl children as (spelling, value) pairs, a
    // missing name dropping just that constant (TypeFactories' mapNotNull). The decl-side
    // walk is the enum's DeclContext; TagDecl is EnumDecl's address-identical primary
    // base, so the re-view + TagDecl's GENERATED asDeclContext() (a real pointer
    // adjustment — DeclContext is a secondary base) reach it without new cast surface.
    val constants = TagDecl(enumDecl.ptr, enumDecl.memScope).asDeclContext().decls()
        .filterNotNull()
        .filter { it.getKind() == Kind.EnumConstant }
        .mapNotNull { constant ->
            val name = constant.asNamedDecl()?.getNameAsString() ?: return@mapNotNull null
            // VALUE BRIDGE (the documented extraction choice): libclang's
            // clang_getEnumConstantDeclValue is `getInitVal().getSExtValue()` (CIndex.cpp)
            // — a sign-extension through the constant's bit WIDTH even for an UNSIGNED
            // enum. Here the value reads through llvm::APSInt::getExtValue() — the
            // smallest bindable surface (APSInt's OWN method; getSExtValue lives on the
            // unbound llvm::APInt base) and sign-CORRECT: it extends by the constant's
            // real signedness. The two agree for every signed constant and every unsigned
            // constant below the top bit; for an unsigned constant WITH the top bit set
            // (e.g. `enum X : unsigned { M = 0x80000000 }`) libclang reports the
            // sign-mangled negative — a Phase C normalizer entry (mask: sign-extend the
            // libclang value by the underlying width). EnumConstantDecl's Decl is the
            // address-identical primary base (ValueDecl chain; Decl.h: `: public
            // ValueDecl, public Mergeable<>, public APIntStorage`), so the kind-gated
            // re-view mirrors its static classof (`getKind() == EnumConstant`).
            val value = EnumConstantDecl(constant.ptr, constant.memScope)
                .getInitVal()
                .getExtValue()
            WrappedEnumConstant(name, value)
        }
    return WrappedEnumType(
        qualified,
        buildWrappedType(enumDecl.getIntegerType()),
        constants
    )
}

// Leaf spelling, mirroring createForType's const-stripped spelling read (the constness is
// re-applied by maybeConst — here the unqualified type is read instead of string-stripping).
// PrintingPolicy bridge (brick-3 finding): QualType::getAsString()'s no-policy overload
// prints with PrintingPolicy(LangOptions()) — C, not the TU's C++ LangOpts that libclang
// uses — whose one divergent builtin spelling on this slice is `_Bool` for `bool`. Binding
// ASTContext::getPrintingPolicy() + the policy-taking getAsString overload is a whole extra
// class surface for one token, so the spelling is normalized instead — documented bridge.
private fun spellingOf(type: QualType): String? {
    val spelling = type.getUnqualifiedType().getAsString()?.trim() ?: return null
    return if (spelling == "_Bool") "bool" else spelling
}
