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
import clang.AccessSpecifier
import clang.CXXBaseSpecifier
import clang.CXXConstructorDecl
import clang.CXXDestructorDecl
import clang.CXXMethodDecl
import clang.CXXRecordDecl
import clang.ClassTemplateDecl
import clang.Decl
import clang.DeclContext
import clang.FieldDecl
import clang.FunctionDecl
import clang.NamedDecl
import clang.NamespaceDecl
import clang.ParmVarDecl
import clang.QualType
import clang.RecordDecl
import clang.TemplateDecl
import clang.TemplateParameterList
import clang.TemplateTypeParmDecl
import clang.TranslationUnitDecl
import clang.TypedefNameDecl
import clang.decl.Kind
import com.monkopedia.krapper.generator.model.ClassMetadata
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedBase
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
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import kotlinx.cinterop.MemScope
import kppbridge.defaultArgText
import kppbridge.defaultArgType

// The C++-AST front-end's model construction (#44 bricks 2-5): walk a parsed Clang AST on
// the kplusplus-generated libclang-cpp bindings and build :krapper_model's parse-output
// model — the same WrappedTU shape the libclang-C front-end (ModelFactories.kt) produces.
// Every construction rule cites the ModelFactories source it mirrors; types are decoded
// STRUCTURALLY from the QualType tree (TypeBuilder.kt, mirroring TypeFactories.kt).

// IDENTITY CONVENTION: the libclang front-end keys identity fields (WrappedArgument.usr and
// the reducer's lookup maps) on libclang's USR string. This front-end's equivalent is the
// canonical-Decl id proven on clangwalk/decl-identity-probe (b9e19dc): normalize any decl to
// its canonical clang::Decl* (redeclaration-collapse) and read Decl::getID(), spelled
// "cpp:<id>". The two front-ends therefore NEVER agree on the literal identity string —
// Phase C's tree-diff treats identity fields as maskable and compares structure only.
private fun Decl.canonical(): Decl = (getCanonicalDecl() as? Decl) ?: this

// The DEFINING redeclaration of a record (or null when it is never defined). A base/field
// type may be spelled through a forward declaration whose own `decls()` carries no members;
// the #10 gates must introspect the definition. Re-view the RecordDecl-Api result as a
// CXXRecordDecl (address-identical), returning the record itself when it is already the
// definition.
private fun CXXRecordDecl.definitionOrNull(): CXXRecordDecl? = when {
    isThisDeclarationADefinition() -> this
    else -> recordDeclGetDefinition()?.let { CXXRecordDecl(it.ptr) }
}

// Shared with TypeBuilder (the dependent-T WrappedTemplateRef must key on the SAME
// identity string as the WrappedTemplateParam it refers to).
internal fun Decl.cppUsr(): String = "cpp:${canonical().getID()}"

// BRIDGE: the generated `Decl.asClassTemplateDecl()` dyn-cast is missing — the
// ClassTemplateDecl -> RedeclarableTemplateDecl -> TemplateDecl chain doesn't survive
// resolution (RedeclarableTemplateDecl is unbound), the same CastTargets gap as
// TypedefType (TypeBuilder.asTypedefTypeOrNull; probe-confirmed on the brick-5
// generation run). The gate is EXACTLY the class's static `classof`
// (`getKind() == ClassTemplate`), and Decl is ClassTemplateDecl's address-identical
// primary base, so the same-pointer re-view is the generated dyn-cast's semantics.
private fun Decl.asClassTemplateDeclOrNull(): ClassTemplateDecl? =
    if (getKind() == Kind.ClassTemplate) ClassTemplateDecl(ptr) else null

// Load-bearing mirroring, not a gap workaround: ModelFactories.map only has a
// CXCursor_TypedefDecl branch — a C++ `using` alias (CXCursor_TypeAliasDecl, here
// Kind.TypeAlias) produces NO element on the libclang path, so the kind gate must
// exclude it (the generated asTypedefNameDecl() alone would catch both alias kinds).
private fun Decl.asTypedefDeclOrNull(): TypedefNameDecl? =
    if (getKind() == Kind.Typedef) asTypedefNameDecl() else null

// operator new/delete are never bound — neither as members nor as free functions
// (ModelFactories.map's CXXMethod/FunctionDecl branch filters these names).
private val NEW_DELETE_OPERATORS = listOf(
    "operator new",
    "operator new[]",
    "operator delete",
    "operator delete[]"
)

/**
 * Construct a WrappedTU from the parsed translation unit. Top-level shape matches the
 * libclang front-end (ModelFactories.mapAll): a WrappedNamespace per (named) namespace, a
 * WrappedClass per record, and a MethodType.STATIC WrappedMethod per free function.
 */
context(scope: MemScope)
fun buildWrappedTU(tuDecl: TranslationUnitDecl): WrappedTU =
    WrappedTU().also { ModelBuilder(scope).addContextDecls(tuDecl.asDeclContext(), it) }

// The former per-wrapper `memScope` field is gone from the generated bindings; the ONE
// ambient MemScope for this parse (from the enclosing `memScoped { }`) is threaded in here
// and used wherever an allocating/`context(scope: MemScope)` binding member is called.
private class ModelBuilder(private val memScope: MemScope) {
    // Mirrors ModelFactories' USR-keyed elementLookup memo for namespaces: every block of
    // `namespace geo { }` shares one libclang USR, so they all collapse into ONE
    // WrappedNamespace that accumulates all the blocks' members. The canonical-Decl
    // identity (cppUsr) collapses namespace redeclarations the same way.
    private val namespaces = mutableMapOf<String, WrappedNamespace>()

    // Redeclaration collapse for classes and class templates (#45 brick 3 — the std
    // headers the forcing parse pulls in are full of forward declarations): the same
    // canonical decl re-encountered maps to the SAME element, exactly like ModelFactories'
    // USR-keyed elementLookup memo. Members attach only from the DEFINING redeclaration
    // (a forward decl's pattern record SHARES DefinitionData with the definition, so
    // reading bases()/members there would double-add — the cursor walk only ever sees a
    // redecl's own children).
    private val classes = mutableMapOf<String, WrappedClass>()
    private val templates = mutableMapOf<String, WrappedTemplate>()

    // Walk one DeclContext (the TU, a namespace, or a linkage-spec block) and add its
    // children to [parent]. [attach] mirrors libclang's LinkageSpecDecl semantics (D2,
    // #46): mapAll's `map(parentCursor) ?: return` skips every edge whose parent cursor
    // is a linkage spec (no map branch), so an `extern "C"/"C++" { ... }` block's DIRECT
    // children are never attached as model children — but the walk still DESCENDS, and
    // anything one level deeper attaches to its own mapped parent (members of `extern
    // "C++" { namespace std { ... } }` land on the memoized nm(std); a record defined
    // inside `extern "C" { ... }` gets its members on the USR-memoized class element,
    // which is visible only if some NON-linkage-spec redeclaration attached it — exactly
    // how glibc's `struct _IO_FILE` binds: forward decl at TU scope, definition inside
    // extern "C"). With attach=false: namespaces/classes/templates are created DETACHED
    // (memo only) and their members walked; functions and typedefs — which have no
    // deeper attachment — produce nothing.
    fun addContextDecls(context: DeclContext, parent: WrappedElement, attach: Boolean = true) {
        for (decl in with(memScope) { context.decls() }) {
            if (decl == null) continue
            // Skip implicit decls (the builtin TU members like __int128_t,
            // __builtin_va_list) — libclang's cursor walk never surfaces them.
            if (decl.isImplicit()) continue
            if (decl.getKind() == Kind.LinkageSpec) {
                val specContext = with(Decl.Companion) {
                    castToDeclContext(decl)
                } ?: continue
                addContextDecls(specContext, parent, attach = false)
                continue
            }
            val namespace = decl.asNamespaceDecl()
            if (namespace != null) {
                addNamespace(namespace, parent, attach)
                continue
            }
            // Brick 5: a `template <typename T> class` is a ClassTemplateDecl (NOT a
            // CXXRecordDecl — the pattern record hangs off it), mirrored to
            // WrappedTemplate (ModelFactories.map's CXCursor_ClassTemplate branch).
            val classTemplate = decl.asClassTemplateDeclOrNull()
            if (classTemplate != null) {
                addTemplate(classTemplate, decl, parent, attach)
                continue
            }
            // A ClassTemplateSpecializationDecl IS-A CXXRecordDecl, but libclang's cursor
            // walk never surfaces IMPLICIT instantiations (they hang off the template's
            // specialization set, not the TU) — guard so one is never mistaken for a
            // plain record. Explicit specializations are brick-6+.
            // #101 — this skip is also the NONCOPYABLE DETERMINISM guarantee. Clang
            // instantiates a specialization's member FUNCTIONS LAZILY (on first odr-use), so
            // an implicitly-instantiated spec's decls() carries only what THIS parse happened
            // to trigger — a PARSE-DEPENDENT set (the report: Persistent<v8::Value> surfacing
            // its copy ctor on one parse, its copy assignment on another). By never walking
            // the specialization and instead materializing it from the template PATTERN's
            // fixed source-order decls() (the resolver's deterministic param substitution,
            // Parsing.typedAs), the special-member set is identical every parse. Locked by
            // the krapper_parse `--noncopyable-determinism` guard.
            if (decl.getKind() == Kind.ClassTemplateSpecialization ||
                decl.getKind() == Kind.ClassTemplatePartialSpecialization
            ) {
                continue
            }
            val record = decl.asCXXRecordDecl()
            if (record != null) {
                addClass(record, decl, parent, attach)
                continue
            }
            // Brick 5: a `typedef` is a WrappedTypedef element at ANY level — TU,
            // namespace, class, template (ModelFactories.map's CXCursor_TypedefDecl
            // branch is level-agnostic); a `using` alias produces no element (no
            // CXCursor_TypeAliasDecl branch) — the Kind.Typedef gate mirrors that split.
            // A typedef or free function DIRECTLY inside a linkage spec has no deeper
            // structure that could attach via the memo, so under attach=false both
            // produce nothing — exactly libclang's skipped LinkageSpec->child edge.
            val typedef = decl.asTypedefDeclOrNull()
            if (typedef != null) {
                if (attach) buildTypedef(typedef)?.let { parent.addChild(it) }
                continue
            }
            val function = decl.asFunctionDecl()
            if (function != null && decl.asCXXMethodDecl() == null && attach) {
                // A deleted free function: libclang surfaces `= delete` as
                // CXAvailability_NotAvailable and filters the decl out
                // (ModelFactories.map's availability check).
                if (function.isDeleted()) continue
                if (function.asNamedDecl().getNameAsString() in NEW_DELETE_OPERATORS) continue
                // A free function is a STATIC WrappedMethod (ModelFactories.WrappedMethod:
                // STATIC when the semantic parent is not a class).
                parent.addChild(buildFunction(function))
            }
            // An EnumDecl deliberately produces NO element: ModelFactories.map has no
            // CXCursor_EnumDecl branch (`else -> return null`); the enum's payload —
            // underlying type + constants — rides on every WrappedEnumType LEAF that
            // references it (TypeBuilder.buildEnumType).
            // brick-6+: global variables and forward-declaration redecl-collapse for
            // classes (elementLookup's other use).
        }
    }

    // ModelFactories.map's CXCursor_TypedefDecl branch: WrappedTypedef(spelling, the
    // reducer-stack target type). The libclang branch drops a typedef whose type can't be
    // built (catch -> null); buildTypedefTargetType never throws (UNRESOLVABLE instead),
    // so only a missing name drops here.
    private fun buildTypedef(typedef: TypedefNameDecl): WrappedTypedef? = with(memScope) {
        val name = typedef.getNameAsString() ?: return@with null
        WrappedTypedef(name, buildTypedefTargetType(typedef))
    }

    // ModelFactories.map's CXCursor_ClassTemplate branch + the WrappedTemplate factory
    // (WrappedTemplate(spelling)): the element is the template's bare name; its children
    // are the WrappedTemplateParams followed by the templated record's bases + members
    // built through the SAME paths as a plain class (mapAll's recursion reuses every
    // member branch on template children; the metadata side-effects land on the
    // WrappedTemplate via the shared `(parent as? WrappedTemplate)?.metadata` mirrors).
    // Redeclarations collapse to ONE element (the memo); EVERY redeclaration's params
    // re-walk through WrappedTemplate's merge machinery — mapAll resets templateArgCounter
    // per template-cursor encounter, so repeat params land in the first params'
    // `otherParams`, letting substitution key on ANY redecl's param name/usr — and the
    // bases/members attach from the DEFINING redeclaration only.
    private fun addTemplate(
        templateDecl: ClassTemplateDecl,
        decl: Decl,
        parent: WrappedElement,
        attach: Boolean
    ) {
        val record = templateDecl.getTemplatedDecl()
            ?.let { CXXRecordDecl(it.ptr) } ?: return
        val name = record.asNamedDecl().getNameAsString() ?: return
        val template = templates.getOrPut(decl.cppUsr()) { WrappedTemplate(name) }
        attachIfNew(template, parent, attach)
        template.templateArgCounter = 0
        // getTemplateParameters() lives on TemplateDecl — ClassTemplateDecl's
        // address-identical primary base chain, so the same-pointer re-view is exact.
        // Each TYPE parameter mirrors ModelFactories' CXCursor_TemplateTypeParameter ->
        // WrappedTemplateParam(spelling, usr); a non-type/template-template parameter has
        // no map branch on the libclang path (`else -> null`) and is skipped the same way.
        val params = TemplateDecl(templateDecl.ptr)
            .getTemplateParameters()
            ?.let { TemplateParameterList(it.ptr) }
            ?: return
        var typeParamIndex = 0
        for (i in 0u until params.size()) {
            val param = params.getParam(i)
                ?.let { NamedDecl(it.ptr) } ?: continue
            if (param.getKind() != Kind.TemplateTypeParm) continue
            template.addChild(
                WrappedTemplateParam(
                    param.getNameAsString() ?: continue,
                    // The identity the dependent-T WrappedTemplateRef keys back to
                    // (TypeBuilder's TemplateTypeParmType branch reads the same decl).
                    Decl(param.ptr).cppUsr()
                )
            )
            // defaultType capture (#46 Phase D): the model derives defaultType from the
            // param element's children (TypeBuilder.defaultTypeResidue — the libclang
            // cursor-walk residue, reproduced structurally). Attached to the CANONICAL
            // instance (a re-walked redeclaration's params route into otherParams), so a
            // default declared on a LATER redeclaration (basic_string is forward-declared
            // default-less in <iosfwd> first) still lands — clang reports the inherited
            // default on every subsequent redecl's param, mirroring how the libclang
            // memo accumulates the residue cursors onto its one USR-keyed instance.
            val canonical = template.templateArgs.getOrNull(typeParamIndex)
            typeParamIndex++
            if (canonical == null || canonical.children.isNotEmpty()) continue
            val parmDecl = TemplateTypeParmDecl(param.ptr)
            if (!parmDecl.hasDefaultArgument()) continue
            val default = with(memScope) { defaultArgType(parmDecl) }
            with(memScope) {
                for (residue in defaultTypeResidue(default)) canonical.addChild(residue)
            }
        }
        if (templateDecl.isThisDeclarationADefinition()) {
            addBasesAndMembers(record, template, template.metadata)
        }
    }

    private fun addNamespace(decl: NamespaceDecl, parent: WrappedElement, attach: Boolean) {
        val name = decl.asNamedDecl().getNameAsString() ?: error("namespace without a name")
        // An anonymous namespace has an empty spelling; its members have TU-internal
        // linkage and can't cross the C ABI, so it is skipped transitively
        // (ModelFactories.map's CXCursor_Namespace branch).
        if (name.isEmpty()) return
        val namespace = namespaces.getOrPut(decl.asDecl().cppUsr()) { WrappedNamespace(name) }
        // Attach at the first sighting outside a linkage spec — including an element
        // created DETACHED inside one earlier (libclang's lazy `parent.addChild` on the
        // first edge whose parent cursor maps).
        attachIfNew(namespace, parent, attach)
        // Members of a linkage-spec-wrapped namespace block DO attach: they are one
        // level below the skipped edge (their parent is the namespace element).
        addContextDecls(decl.asDeclContext(), namespace)
    }

    private fun attachIfNew(element: WrappedElement, parent: WrappedElement, attach: Boolean) {
        if (attach && element.parent == null) parent.addChild(element)
    }

    private fun addClass(
        record: CXXRecordDecl,
        decl: Decl,
        parent: WrappedElement,
        attach: Boolean
    ) {
        // An ANONYMOUS record is spelled by its typedef name when one exists (`typedef
        // struct { ... } max_align_t;` — libclang's cursor-spelling rule, which is how
        // max_align_t binds on that path); a truly anonymous record (libclang spells
        // "(unnamed struct at <file:line>)", never bindable) is skipped — an empty name
        // would crash WrappedClass.type ("Empty type").
        val name = record.asNamedDecl().getNameAsString()?.takeIf { it.isNotEmpty() }
            ?: RecordDecl(record.ptr).getTypedefNameForAnonDecl()
                ?.getNameAsString()?.takeIf { it.isNotEmpty() }
            ?: return
        // ModelFactories' WrappedClass factory: name via wrapName (= the bare name for a
        // non-template class; template-args spelling is brick-4+) + the isAbstract flag.
        // hasDefinition() guards a never-defined record, whose definition-data accessors
        // may not be read (DefinitionData is SHARED across redeclarations, so it holds
        // even when the walk meets a forward declaration first). Redeclarations collapse
        // to one element via the memo; members attach from the defining redecl only.
        val cls = classes.getOrPut(decl.cppUsr()) {
            WrappedClass(name, isAbstract = record.hasDefinition() && record.isAbstract())
        }
        attachIfNew(cls, parent, attach)
        if (record.isThisDeclarationADefinition()) {
            addBasesAndMembers(record, cls, cls.metadata)
        }
    }

    // The shared class-body construction: bases + every member through the same branches,
    // whether [parent] is a WrappedClass or a WrappedTemplate (mapAll's recursion makes no
    // distinction either; ModelFactories' metadata side-effects mirror to both).
    private fun addBasesAndMembers(
        record: CXXRecordDecl,
        parent: WrappedElement,
        metadata: ClassMetadata
    ) = with(memScope) {
        for (base in record.bases()) {
            if (base == null) continue
            // ModelFactories.map's TOP access filter applies to base specifiers too: a
            // protected/private base (std::vector : protected _Vector_base) never becomes
            // a WrappedBase on the libclang path. Load-bearing for forcing (#45 brick 3):
            // keeping the dependent _Vector_base would hard-fail the specialization's
            // INCLUDE reference-expansion (the unresolvable-primary-base rule).
            if (base.getAccessSpecifier() != AccessSpecifier.AS_public) continue
            // ModelFactories.map's CXCursor_CXXBaseSpecifier branch: the base's type built
            // through the CXType factory (structural buildWrappedType here, brick 4),
            // isPublic from the access specifier, isVirtualBase from `virtual` inheritance.
            parent.addChild(
                WrappedBase(
                    buildWrappedType(base.getType()),
                    isPublic = true,
                    isVirtualBase = base.isVirtual()
                )
            )
        }
        for (decl in record.asDeclContext().decls()) {
            if (decl == null) continue
            // Implicit members (the injected class name, Sema-injected copy ctor/
            // assignment) are never visited by libclang's cursor walk; mirror that.
            if (decl.isImplicit()) continue
            addMember(decl, parent, metadata)
        }
        // #10 GATE 2 (deleted default ctor): generalize the reference-field-only fact
        // addField already records so the resolver's `_new` guard covers ALL deletion
        // reasons, not just a reference member. The direct decl scan catches an already-
        // materialized deleted default ctor (Module::Header family), the structural check
        // the still-lazy implicit case (TargetInfo::GCCRegAlias const-array family). Runs
        // AFTER the member loop so it sees the same decls addMember walked.
        if (defaultCtorIsDeleted(record)) metadata.hasDeletedDefaultConstructor = true
        // #10 GATE 1 (value-equality): record whether this record has an accessible
        // `operator==` (member / public base / enclosing-namespace free function). The
        // consumer (krapper_gen typedAs) drops a container `equals`/`operator==` whose
        // element is a record proven to lack `==`, so the container body never fails to
        // instantiate `element == element` deep in <stl_algobase.h>.
        if (recordHasEqualityOperator(record)) metadata.hasEqualityOperator = true
    }

    // #10 GATE 2 predicate — is [record]'s DEFAULT constructor deleted? Two-step hybrid
    // (`hasDefaultConstructor()` is useless: it returns true even for an implicitly-deleted
    // default ctor, and clang 22 has no `defaultedDefaultConstructorIsDeleted()`):
    //  1. DIRECT — a materialized `CXXConstructorDecl` that isDefaultConstructor() AND
    //     isDeleted() (clang already built the decl because it was odr-used in the parse);
    //  2. STRUCTURAL — when the implicit default ctor is still lazy
    //     (needsImplicitDefaultConstructor), apply `[class.default.ctor]` deletion rules
    //     over bases + non-in-class-initialized fields.
    // Keep-on-doubt: returns true ONLY on a positive proof of deletion; every ambiguous
    // case leaves the existing behaviour (a false KEEP is at worst an unchanged error, a
    // false DROP would lose a valid `_new`).
    private fun defaultCtorIsDeleted(record: CXXRecordDecl): Boolean = with(memScope) {
        for (decl in record.asDeclContext().decls()) {
            if (decl == null) continue
            val ctor = decl.asCXXConstructorDecl() ?: continue
            if (ctor.isDefaultConstructor() &&
                ctor.asFunctionDecl().isDeleted()
            ) {
                return true
            }
        }
        // Only reach the structural rules when the implicit default ctor hasn't been
        // materialized (otherwise the direct scan above is authoritative). If a usable
        // default ctor decl exists, needsImplicitDefaultConstructor() is false and we
        // correctly keep the `_new`.
        if (!record.needsImplicitDefaultConstructor()) return false
        structuralDefaultCtorDeleted(record)
    }

    // `[class.default.ctor]` structural deletion over the AST: the implicit default ctor is
    // deleted if any base or non-in-class-initialized field can't be default-constructed —
    // a record base/field whose OWN default ctor is unusable (recurse), a reference field,
    // or a const-qualified field. Recursion is depth-bounded by [seen] (a self-referential
    // record type can't complete-construct anyway). Anything not positively proven deleting
    // is treated as constructible (keep-on-doubt).
    private fun structuralDefaultCtorDeleted(
        record: CXXRecordDecl,
        seen: MutableSet<String> = mutableSetOf()
    ): Boolean = with(memScope) {
        if (!seen.add(record.asNamedDecl().getNameAsString().orEmpty().ifEmpty { return false })) {
            return false
        }
        for (base in record.bases()) {
            if (base == null) continue
            if (baseOrFieldTypeDeletesDefaultCtor(base.getType(), seen)) return true
        }
        for (decl in record.asDeclContext().decls()) {
            if (decl == null) continue
            val field = decl.asFieldDecl() ?: continue
            // A field with an in-class initializer (`int n = 0;`) is default-initialized by
            // that initializer, so it never deletes the default ctor.
            if (field.hasInClassInitializer()) continue
            if (baseOrFieldTypeDeletesDefaultCtor(field.getType(), seen)) return true
        }
        false
    }

    // Whether a base/field of [type] makes the enclosing class's implicit default ctor
    // deleted: a reference member (must be initialized), a const-qualified member (a const
    // scalar/array/pointer has no default initializer), or a record member whose own
    // default ctor is deleted (recurse). A non-const scalar/pointer/enum default-initializes
    // fine and does not delete.
    private fun baseOrFieldTypeDeletesDefaultCtor(
        type: QualType,
        seen: MutableSet<String>
    ): Boolean = with(memScope) {
        val ty = type.typePtr() ?: return false
        if (ty.isReferenceType()) return true
        if (type.isConstQualified()) return true
        val nested = ty.asRecord() ?: return false
        // Only a DEFINED record can be introspected; reach the definition when the leaf
        // itself is a forward declaration, and leave a never-defined record as
        // constructible (keep-on-doubt).
        val defn = nested.definitionOrNull() ?: return false
        defaultCtorIsDeletedForNested(defn, seen)
    }

    // The nested-record arm of the structural recursion: a direct deleted default-ctor decl
    // OR (when still lazy) the structural rules again.
    private fun defaultCtorIsDeletedForNested(
        record: CXXRecordDecl,
        seen: MutableSet<String>
    ): Boolean = with(memScope) {
        for (decl in record.asDeclContext().decls()) {
            if (decl == null) continue
            val ctor = decl.asCXXConstructorDecl() ?: continue
            if (ctor.isDefaultConstructor() && ctor.asFunctionDecl().isDeleted()) return true
        }
        if (!record.needsImplicitDefaultConstructor()) return false
        structuralDefaultCtorDeleted(record, seen)
    }

    // #10 GATE 1 predicate — does [record] have an accessible `operator==` usable for value
    // comparison? Mirrors the design's probe (§4.1): a member/base `operator==` OR a free
    // `operator==` in the enclosing namespace. The free scan is LOAD-BEARING — SourceLocation
    // / StringRef have no member `==` but a namespace-level one, and dropping their container
    // value-equality would be a false DROP. Keep-on-doubt everywhere else: an unresolvable or
    // undefined record returns... nothing here (the caller only ACTS on a positive hit at the
    // element side, so a missing flag simply keeps the container member).
    private fun recordHasEqualityOperator(record: CXXRecordDecl): Boolean = with(memScope) {
        if (recordDeclaresEqualityMember(record, mutableSetOf())) return true
        // Enclosing-namespace free `operator==`. getEnclosingNamespaceContext walks past the
        // record's own context to the nearest namespace (`clang` for `clang::Module::Header`).
        val enclosing = record.asDecl().getDeclContext()?.getEnclosingNamespaceContext()
            ?: return false
        if (!enclosing.isNamespace()) return false
        for (decl in enclosing.decls()) {
            if (decl == null) continue
            if (decl.asCXXMethodDecl() != null) continue
            val fn = decl.asFunctionDecl() ?: continue
            if (fn.asNamedDecl().getNameAsString() == "operator==") return true
        }
        false
    }

    // A member `operator==` on [record] or any of its public bases (recurse, depth-bounded).
    private fun recordDeclaresEqualityMember(
        record: CXXRecordDecl,
        seen: MutableSet<String>
    ): Boolean = with(memScope) {
        if (!seen.add(record.asNamedDecl().getNameAsString().orEmpty().ifEmpty { return false })) {
            return false
        }
        for (decl in record.asDeclContext().decls()) {
            if (decl == null) continue
            val method = decl.asCXXMethodDecl() ?: continue
            if (method.asNamedDecl().getNameAsString() == "operator==") return true
        }
        for (base in record.bases()) {
            if (base == null) continue
            if (base.getAccessSpecifier() != AccessSpecifier.AS_public) continue
            val baseRecord = base.getType().typePtr()?.asRecord() ?: continue
            val defn = baseRecord.definitionOrNull() ?: continue
            if (recordDeclaresEqualityMember(defn, seen)) return true
        }
        false
    }

    private fun addMember(decl: Decl, parent: WrappedElement, metadata: ClassMetadata) {
        val access = decl.getAccess()
        // ModelFactories.map's top filter: a private/protected member — or one libclang
        // marks CXAvailability_NotAvailable, which is how `= delete` surfaces (bridged
        // here as FunctionDecl::isDeleted()) — is dropped, after recording the metadata
        // the resolver needs about the member's absence.
        if (access == AccessSpecifier.AS_private ||
            access == AccessSpecifier.AS_protected ||
            decl.asFunctionDecl()?.isDeleted() == true
        ) {
            recordFilteredMember(decl, metadata)
            return
        }
        val constructor = decl.asCXXConstructorDecl()
        if (constructor != null) {
            parent.addChild(buildConstructor(constructor))
            return
        }
        val destructor = decl.asCXXDestructorDecl()
        if (destructor != null) {
            parent.addChild(buildDestructor(destructor))
            return
        }
        val method = decl.asCXXMethodDecl()
        if (method != null) {
            if (method.asNamedDecl().getNameAsString() in NEW_DELETE_OPERATORS) return
            parent.addChild(buildMethod(method))
            return
        }
        val field = decl.asFieldDecl()
        if (field != null) {
            addField(field, parent, metadata)
            return
        }
        // Brick 5: a member `typedef` (incl. an in-template `typedef T value_type`) is a
        // WrappedTypedef element, same map branch as the TU level; `using` aliases and
        // member EnumDecls produce no element (see addContextDecls).
        val typedef = decl.asTypedefDeclOrNull()
        if (typedef != null) {
            buildTypedef(typedef)?.let { parent.addChild(it) }
            return
        }
        // NESTED record (#46 Phase D): a class nested in a PLAIN class binds as a
        // WrappedClass child (`Outer::Inner` — ModelFactories.map's ClassDecl/StructDecl
        // branch applies at any level); a class nested in a class TEMPLATE produces NO
        // element (featuregen's T-typename rows: `Wrapper<T>::Inner` must NOT bind —
        // matching the libclang tree, which carries no class under a template).
        if (parent is WrappedClass &&
            decl.getKind() != Kind.ClassTemplateSpecialization &&
            decl.getKind() != Kind.ClassTemplatePartialSpecialization
        ) {
            decl.asCXXRecordDecl()?.let { record ->
                addClass(record, decl, parent, attach = true)
                return
            }
        }
        // Anything else (static data members, nested enums) falls through
        // ModelFactories.map's `else -> return null` and is dropped.
    }

    // Mirror of the metadata side-effects ModelFactories.map records for a member it
    // filters out (private/protected/deleted), keyed by the member's kind and name.
    private fun recordFilteredMember(decl: Decl, metadata: ClassMetadata) {
        val constructor = decl.asCXXConstructorDecl()
        if (constructor != null) {
            metadata.hasConstructor = true
            // A deleted or non-public COPY constructor makes the type non-copyable for
            // the by-value Holder placement-new (T-skip); isCopyConstructor is the
            // C++-AST equivalent of clang_CXXConstructor_isCopyConstructor.
            if (constructor.isCopyConstructor()) {
                metadata.hasDeletedCopyConstructor = true
            }
            return
        }
        // ModelFactories' filter only inspects CXCursor_CXXMethod here — a filtered
        // destructor records nothing — so exclude destructors from this branch.
        val method = decl.asCXXMethodDecl()
        if (method != null && decl.asCXXDestructorDecl() == null) {
            when (method.asNamedDecl().getNameAsString()) {
                "operator new" -> metadata.hasHiddenNew = true

                "operator delete" -> metadata.hasHiddenDelete = true

                // ModelFactories' isCopyAssignmentOf (single non-move lvalue-ref param of
                // the same class) is exactly CXXMethodDecl::isCopyAssignmentOperator().
                "operator=" -> if (method.isCopyAssignmentOperator()) {
                    metadata.hasDeletedCopyAssignment = true
                }
            }
            return
        }
        val field = decl.asFieldDecl()
        if (field != null) {
            // A private/protected CONST field (ModelFactories' CXCursor_FieldDecl filter
            // branch) blocks the generated default-assignment paths.
            if (with(memScope) { buildWrappedType(field.getType()) }.isConst) {
                metadata.hasPrivateConstField = true
            }
        }
    }

    private fun addField(field: FieldDecl, parent: WrappedElement, metadata: ClassMetadata) {
        // Anonymous data members (bitfield padding `int :3;`, anon union/struct members)
        // have a blank spelling and are dropped (ModelFactories' FieldDecl branch).
        val name = field.asNamedDecl().getNameAsString()?.takeIf { it.isNotBlank() } ?: return
        val type = with(memScope) { buildWrappedType(field.getType()) }
        // A public REFERENCE or CONST member implicitly deletes the enclosing class's
        // copy assignment, and a reference member also deletes the implicit default
        // constructor — recorded structurally because the implicit special members are
        // never emitted as decls (ModelFactories' FieldDecl branch, T-skip residuals).
        if (type.isReference || type.isConst) {
            metadata.hasDeletedCopyAssignment = true
        }
        if (type.isReference) {
            metadata.hasDeletedDefaultConstructor = true
        }
        parent.addChild(WrappedField(name, type))
    }

    // ModelFactories.map's CXCursor_Constructor branch: name from the spelling (the class
    // name), VOID return, copy/default flags from the dedicated predicates
    // (clang_CXXConstructor_is{Copy,Default}Constructor ⇒
    // CXXConstructorDecl::is{Copy,Default}Constructor()); allocationStyle stays DIRECT.
    private fun buildConstructor(constructor: CXXConstructorDecl): WrappedConstructor =
        WrappedConstructor(
            constructor.asNamedDecl().getNameAsString() ?: "constructor",
            WrappedType.VOID,
            constructor.isCopyConstructor(),
            constructor.isDefaultConstructor()
        ).also { it.addArgs(constructor.asFunctionDecl()) }

    // ModelFactories.map's CXCursor_Destructor branch: name from the spelling ("~Shape"),
    // VOID return, virtuality threaded for the non-virtual-destructor diagnostic.
    private fun buildDestructor(destructor: CXXDestructorDecl): WrappedDestructor =
        WrappedDestructor(
            destructor.asNamedDecl().getNameAsString() ?: "destructor",
            WrappedType.VOID
        ).also {
            it.isVirtual = destructor.isVirtual()
            it.addArgs(destructor.asFunctionDecl())
        }

    private fun buildMethod(method: CXXMethodDecl): WrappedMethod = with(memScope) {
        val name = method.asNamedDecl().getNameAsString() ?: error("method without a name")
        val isConst = method.isConst()
        // T1.3 mirror (#46): an `iterator_range<It>` return is materialized into
        // `std::vector<Elem*>` at construction, exactly like ModelFactories.WrappedMethod
        // (see TypeBuilder.rangeVectorReturn). The G8 const rule never applies to it
        // (the materialized vector is a by-value template type).
        val range = rangeVectorReturn(method.getReturnType())
        // Mirror the libclang front-end's return-const rule (ModelFactories.WrappedMethod):
        // a `const` method's constness only carries to a pointer/reference return; const on
        // a by-value return is meaningless for the temporary and breaks the Holder path (G8).
        val returnType = range?.first ?: buildWrappedType(method.getReturnType()).let {
            if (isConst && (it.isPointer || it.isReference)) WrappedType.const(it) else it
        }
        // ModelFactories.WrappedMethod: STATIC for a static member (or a non-class-parent
        // function — the buildFunction path here).
        val methodType = if (method.isStatic()) MethodType.STATIC else MethodType.METHOD
        return WrappedMethod(name, returnType, methodType).also {
            it.isConst = isConst
            it.isVirtual = method.isVirtual()
            // Use isExplicitlyDefaulted (user wrote `= default`), NOT isDefaulted: the latter
            // also covers compiler-implicit defaulting, which never applies to a user-declared
            // operator==. Only an explicit `= default`-ed operator== should take the delegating
            // equals + field-fold hashCode path; hand-written == stays on the identity path.
            it.isDefaulted = method.isExplicitlyDefaulted()
            it.rangeElementType = range?.second
            it.addArgs(method.asFunctionDecl())
        }
    }

    private fun buildFunction(function: FunctionDecl): WrappedMethod = with(memScope) {
        val name = function.asNamedDecl().getNameAsString() ?: error("function without a name")
        val returnType = buildWrappedType(function.getReturnType())
        WrappedMethod(name, returnType, MethodType.STATIC).also {
            it.addArgs(function)
        }
    }

    private fun WrappedMethod.addArgs(function: FunctionDecl) = with(memScope) {
        for (i in 0u until function.getNumParams()) {
            // getParamDecl returns the Api interface (the generated related-object-getter
            // convention); re-wrap to the concrete ParmVarDecl so the upcast chain resolves.
            val param = function.getParamDecl(i)?.let { ParmVarDecl(it.ptr) }
                ?: continue
            // Unnamed parameter (e.g. `void freeFunction(int)`) falls back to the same
            // positional name the libclang front-end synthesizes.
            val name = param.asNamedDecl().getNameAsString()?.takeIf { it.isNotBlank() }
                ?: "_arg_$i"
            // Brick 6: hasDefault on the libclang path is the cursor-children HEURISTIC
            // (ModelFactories.defaultExpr: the param's last child cursor that isn't a
            // Type/Template/NamespaceRef) — the token-scrape the self-bootstrap deletes.
            // ParmVarDecl::hasDefaultArg() is the first-class Sema-computed fact: it can
            // never fire for an integer token inside the param's TYPE (an array bound, a
            // non-type template arg, a function_ref signature), so the model-side
            // typeCarriesFalseDefault repairs (#36/#41) have nothing to repair on this
            // path — the deletion payoff. defaultValue mirrors ModelFactories.defaultValue
            // (the default sub-expression's token spellings joined with "", blank -> null)
            // through the kppbridge source-text read (see clang_slice.h for the contract
            // + the whitespace normalizer entry).
            val hasDefault = param.hasDefaultArg()
            val defaultValue = if (hasDefault) {
                defaultArgText(param)?.takeIf { it.isNotBlank() }
            } else {
                null
            }
            addChild(
                WrappedArgument(
                    name,
                    buildWrappedType(param.asValueDecl().getType()),
                    param.asDecl().cppUsr(),
                    hasDefault,
                    defaultValue
                )
            )
        }
    }
}
