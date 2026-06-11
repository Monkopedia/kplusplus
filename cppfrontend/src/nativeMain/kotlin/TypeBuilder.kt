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
import clang.EnumDecl
import clang.QualType
import clang.Type
import clang.TypedefNameDecl
import clang.TypedefType
import clang.decl.Kind
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
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import kppbridge.numTemplateArgs
import kppbridge.templateArgAsType
import kppbridge.templateBaseName

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
            // The rich WrappedFunctionPointer only exists for a `typedef` over a
            // fn-pointer — functionPointerTypedef below (brick 6), reached through the
            // alias branch; a `using` fn-ptr alias collapses to THIS leaf. The same
            // PrintingPolicy bridge as spellingOf applies (a `bool` anywhere in the proto
            // prints as C's `_Bool` under the default policy; `_Bool` is not otherwise a
            // valid token in a C++ type spelling, so the blanket replace is safe).
            val spelling = type.getAsString()?.trim()?.replace("_Bool", "bool")
                ?: return UNRESOLVABLE
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
    // #45 brick 3: decoded through the CIndex GetTemplateArguments MIRROR (kppbridge):
    // the sugared TemplateSpecializationType's WRITTEN args are preferred — exactly what
    // clang_Type_getNumTemplateArguments reads, so `std::vector<Item*>` carries ONE arg
    // (the defaulted allocator is NOT part of the libclang type, and the forcing key /
    // tracker key depends on that spelling) — with the canonical specialization decl's
    // full list as the fallback. The TST half also catches DEPENDENT specializations
    // (`vector<_Tp, _Alloc>`, `allocator<_Tp>` inside a template's own member
    // signatures), which have no canonical record and previously fell through to a
    // canonical-spelling leaf ("vector<type-parameter-0-0,...>"). Checked BEFORE the
    // typedef handling for the same reason invoke checks it before createForType: an
    // alias over a template type still collapses to the template shape. A non-type
    // argument decodes to a null QualType and is dropped (TypeFactories' CXType_Invalid
    // filterNotNull).
    val canonical = type.getCanonicalType()
    val canonTy = canonical.typePtr()
    val templateArgCount = with(type.memScope) { numTemplateArgs(type) }
    if (templateArgCount > 0) {
        val baseName = with(type.memScope) { templateBaseName(type) }
            ?.takeIf { it.isNotEmpty() } ?: return UNRESOLVABLE
        // std::initializer_list is compiler magic the C boundary can never marshal (a
        // Kotlin caller cannot construct one), so it is UNRESOLVABLE by construction —
        // members taking one drop in resolution (or have the defaulted arg omitted),
        // matching the libclang path, where the same members drop because that
        // front-end's `initializer_list<value_type>` spelling keeps the alias-name leaf
        // its order-dependent visit() collapse can't resolve. This front-end's faithful
        // decode (value_type -> the substituted element type) would otherwise
        // MATERIALIZE initializer_list<Item*> as a useless extra binding.
        if (baseName == "std::initializer_list") return UNRESOLVABLE
        val args = (0u until templateArgCount.toUInt()).mapNotNull { i ->
            val arg = with(type.memScope) { templateArgAsType(type, i) }
            if (arg.typePtr() == null) null else buildWrappedType(arg)
        }
        return WrappedTemplateType(WrappedTypeReference(baseName), args).maybeConst(isConst)
    }

    if (typedef != null) {
        return buildTypedefTargetType(typedef).maybeConst(isConst)
    }

    // Dependent template-parameter reference -> WrappedTemplateRef keyed by the param's
    // NAME. #45 brick 3 revision (was cpp:<id> identity): on the libclang path, every
    // dependent use inside a ClassTemplate's member signatures hits TypeFactories'
    // CXType_Unexposed/NoDeclFound fallback — WrappedTemplateRef(spelling), i.e. the
    // BARE NAME ("_Tp") — because libclang does not resolve the dependent decl (the
    // USR-keyed CXCursor_TemplateTypeParameter branch effectively never fires there).
    // The name key is LOAD-BEARING for output parity: a member's pre-substitution
    // spelling is baked into its uniqueCName (`...new__size_t_const_template__Tp_and`),
    // so an identity key would leak a nondeterministic cpp:<id> into generated symbol
    // names. Substitution accepts either key (Parsing.typedAs maps each param's name AND
    // usr), so resolution is unaffected. Read off the SUGARED type: the canonical
    // TemplateTypeParmType carries no decl (TypeBase.h: `assert(!TTPDecl == Canon.isNull())`).
    ty.asTemplateTypeParmType()?.let { parm ->
        val decl = parm.getDecl() ?: return UNRESOLVABLE
        val name = decl.asNamedDecl().getNameAsString() ?: return UNRESOLVABLE
        return WrappedTemplateRef(name).maybeConst(isConst)
    }

    // ---- createForType leaf dispatch (post-visit, i.e. against the canonical type) ----

    if (canonTy != null && canonTy.isConstantArrayType()) {
        // A constant array falls through createForType to `else -> WrappedType(spelling)`,
        // and LLVM-22's TypePrinter spells it "elem[N]" with NO space — proven by the
        // golden compare against the live libclang dump ("int[4]"; the krapper_gen
        // TestData golden "int [5]" predates the printer change). WrappedTypeReference's
        // array parsing (indexOf('[') + trim) accepts either spelling.
        // Reconstruct the same spelling structurally from element + extent.
        val array = canonTy.asConstantArrayType() ?: return UNRESOLVABLE
        val element = buildWrappedType(array.getElementType())
        return WrappedTypeReference("$element[${array.getZExtSize()}]").maybeConst(isConst)
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
        // The decl is reached through Type::getAsTagDecl() + the generated asEnumDecl
        // dyn-cast (clang::EnumType itself is unbindable — see build.gradle.kts); same
        // decl clang_getTypeDeclaration reports.
        val enumDecl = canonTy.getAsTagDecl()
            ?.let { Decl(it.ptr, canonTy.memScope) }
            ?.asEnumDecl() ?: return UNRESOLVABLE
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
 *  - functionPointerTypedefElement (brick 6): a `typedef` over a pointer-to-function-proto
 *    becomes a WrappedFunctionPointer (see [functionPointerTypedef]) — first in the stack,
 *    `typedef`-only.
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
    // functionPointerTypedefElement runs FIRST in createForType's originalDecl reducer
    // stack, and is keyed on CXCursor_TypedefDecl ONLY — a `using` fn-ptr alias
    // (TypeAliasDecl) has no branch on the libclang path, so the Kind.Typedef gate
    // mirrors that split (it collapses to the bare fn-ptr leaf below instead).
    if (typedef.asDecl().getKind() == Kind.Typedef) {
        functionPointerTypedef(typedef)?.let { return it }
    }
    referenceTypedef(typedef)?.let { return it }
    val name = typedef.getNameAsString() ?: ""
    when (name) {
        "size_type" -> return WrappedTypeReference("size_t")
        "difference_type" -> return WrappedTypeReference("ptrdiff_t")
    }
    if (name in C_ALIAS_TYPEDEFS) return WrappedTypeReference(name)
    return buildWrappedType(typedef.getUnderlyingType())
}

/**
 * TypeFactories.referenceTypedefElement (#45 brick 3): a std-container
 * `reference`/`const_reference` member alias whose underlying is a DEPENDENT trait
 * expression (libstdc++ routes it through `_Alloc_traits::reference`, which libclang
 * reports as CXType_Unexposed and can't reduce) is rewritten to `value_type&` /
 * `const value_type&` from the sibling `value_type` typedef's underlying — this is what
 * keeps vector's at/operator[]/front/back accessors alive. Gates mirrored:
 *  - only when the underlying is dependent AND not itself a concrete reference/pointer
 *    shape (libclang's Unexposed-kind test: a dependent `_Tp&` spells as
 *    CXType_LValueReference, NOT Unexposed, and must fall through);
 *  - only when the sibling element type is NOT a template specialization (associative
 *    containers' `value_type = pair<const Key, T>` stays opaque — const members).
 */
private fun referenceTypedef(typedef: TypedefNameDecl): WrappedType? {
    val name = typedef.getNameAsString()
    val isConstRef = name == "const_reference"
    if (name != "reference" && !isConstRef) return null
    val underlying = typedef.getUnderlyingType().typePtr() ?: return null
    if (!underlying.isDependentType() ||
        underlying.isReferenceType() || underlying.isPointerType()
    ) {
        return null
    }
    val valueTypedef = siblingTypedef(typedef, "value_type") ?: return null
    val elementType = buildWrappedType(valueTypedef.getUnderlyingType())
    if (elementType == UNRESOLVABLE) return null
    if (elementType is WrappedTemplateType) return null
    return referenceTo(if (isConstRef) const(elementType) else elementType)
}

// The sibling typedef of [typedef]'s parent context with the given [name] (the
// `semanticParent.children` walk of the libclang reducers). `typedef`-kind only,
// mirroring the CXCursor_TypedefDecl filter.
private fun siblingTypedef(typedef: TypedefNameDecl, name: String): TypedefNameDecl? {
    val context = typedef.asDecl().getDeclContext() ?: return null
    for (decl in context.decls()) {
        if (decl == null || decl.getKind() != Kind.Typedef) continue
        val sibling = decl.asTypedefNameDecl() ?: continue
        if (sibling.getNameAsString() == name) return sibling
    }
    return null
}

// TypeFactories.isCFunctionPointerCompatible, verbatim: only plain native scalars, void,
// and `void*` (the Mode-1 context slot) can be re-declared verbatim in the C interop
// header; enums/refs/class types name C++ types not in scope there.
private val WrappedType.isCFunctionPointerCompatible: Boolean
    get() = isVoid || (isNative && !isString) || (isPointer && pointed.isVoid)

/**
 * TypeFactories.functionPointerTypedefElement (#44 brick 6): a `typedef` over a
 * pointer-to-function-proto (`typedef int (*IntTransform)(int);` — the CB-cfnptr shape) is
 * captured as a [WrappedFunctionPointer]: cName = the UNqualified typedef spelling (the
 * extern-"C" redeclaration name), cppName = the FULLY-QUALIFIED spelling
 * (`getQualifiedNameAsString` ≡ the cursor `fullyQualified` walk; identical to cName for a
 * global typedef), return + every param decoded structurally through [buildWrappedType].
 * The STAGE-1 GATE is mirrored verbatim: only signatures whose return AND every param is
 * [isCFunctionPointerCompatible] qualify — a richer proto (CB-cfnptr-richsig, e.g. a
 * class-pointer param) returns null and falls through to the normal alias collapse, same
 * as the libclang path. The pointer/proto shape checks mirror the CXType_Pointer +
 * CXType_FunctionProto kind tests on typedefDeclUnderlyingType.
 */
private fun functionPointerTypedef(typedef: TypedefNameDecl): WrappedType? {
    val underlying = typedef.getUnderlyingType()
    val ty = underlying.typePtr() ?: return null
    if (!ty.isPointerType()) return null
    // The pointee is read CANONICALLY: the declarator `void (*Callback)(int)` wraps its
    // proto in ParenType sugar (Pointer -> Paren -> FunctionProto), which libclang's
    // clang_getPointeeType desugars before the CXType_FunctionProto kind test fires —
    // getCanonicalType is the decl-side desugar. Unlike TypedefType (the bridge above),
    // FunctionProtoType's chain survives resolution — the generated dyn-cast and the
    // inherited getReturnType() are both real.
    val proto = ty.getPointeeType().getCanonicalType().typePtr()
        ?.asFunctionProtoType() ?: return null
    val returnType = buildWrappedType(proto.getReturnType())
    val argTypes = (0u until proto.getNumParams()).map { buildWrappedType(proto.getParamType(it)) }
    if (!returnType.isCFunctionPointerCompatible) return null
    if (argTypes.any { !it.isCFunctionPointerCompatible }) return null
    val cName = typedef.getNameAsString() ?: return null
    val cppName = typedef.getQualifiedNameAsString()?.takeIf { it.isNotEmpty() } ?: cName
    return WrappedFunctionPointer(cName, returnType, argTypes, cppName)
}

// The WrappedEnumType payload for [enumDecl] (TypeFactories' CXCursor_EnumDecl branch).
private fun buildEnumType(enumDecl: EnumDecl): WrappedType {
    val qualified = enumDecl.getQualifiedNameAsString() ?: return UNRESOLVABLE
    // Constants: the decl's EnumConstantDecl children as (spelling, value) pairs, a
    // missing name dropping just that constant (TypeFactories' mapNotNull).
    val constants = enumDecl.asDeclContext().decls()
        .mapNotNull { it?.asEnumConstantDecl() }
        .mapNotNull { constant ->
            val name = constant.getNameAsString() ?: return@mapNotNull null
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
            // libclang value by the underlying width).
            WrappedEnumConstant(name, constant.getInitVal().getExtValue())
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
