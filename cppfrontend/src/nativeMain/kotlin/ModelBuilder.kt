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
import clang.RecordDecl
import clang.TemplateDecl
import clang.TemplateParameterList
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
import kppbridge.defaultArgText

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
    if (getKind() == Kind.ClassTemplate) ClassTemplateDecl(ptr, memScope) else null

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
fun buildWrappedTU(tuDecl: TranslationUnitDecl): WrappedTU =
    WrappedTU().also { ModelBuilder().addContextDecls(tuDecl.asDeclContext(), it) }

private class ModelBuilder {
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

    // Walk one DeclContext (the TU or a namespace) and add its children to [parent].
    fun addContextDecls(context: DeclContext, parent: WrappedElement) {
        for (decl in context.decls()) {
            if (decl == null) continue
            // Skip implicit decls (the builtin TU members like __int128_t,
            // __builtin_va_list) — libclang's cursor walk never surfaces them.
            if (decl.isImplicit()) continue
            val namespace = decl.asNamespaceDecl()
            if (namespace != null) {
                addNamespace(namespace, parent)
                continue
            }
            // Brick 5: a `template <typename T> class` is a ClassTemplateDecl (NOT a
            // CXXRecordDecl — the pattern record hangs off it), mirrored to
            // WrappedTemplate (ModelFactories.map's CXCursor_ClassTemplate branch).
            val classTemplate = decl.asClassTemplateDeclOrNull()
            if (classTemplate != null) {
                addTemplate(classTemplate, decl, parent)
                continue
            }
            // A ClassTemplateSpecializationDecl IS-A CXXRecordDecl, but libclang's cursor
            // walk never surfaces IMPLICIT instantiations (they hang off the template's
            // specialization set, not the TU) — guard so one is never mistaken for a
            // plain record. Explicit specializations are brick-6+.
            if (decl.getKind() == Kind.ClassTemplateSpecialization ||
                decl.getKind() == Kind.ClassTemplatePartialSpecialization
            ) {
                continue
            }
            val record = decl.asCXXRecordDecl()
            if (record != null) {
                addClass(record, decl, parent)
                continue
            }
            // Brick 5: a `typedef` is a WrappedTypedef element at ANY level — TU,
            // namespace, class, template (ModelFactories.map's CXCursor_TypedefDecl
            // branch is level-agnostic); a `using` alias produces no element (no
            // CXCursor_TypeAliasDecl branch) — the Kind.Typedef gate mirrors that split.
            val typedef = decl.asTypedefDeclOrNull()
            if (typedef != null) {
                buildTypedef(typedef)?.let { parent.addChild(it) }
                continue
            }
            val function = decl.asFunctionDecl()
            if (function != null && decl.asCXXMethodDecl() == null) {
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
    private fun buildTypedef(typedef: TypedefNameDecl): WrappedTypedef? {
        val name = typedef.getNameAsString() ?: return null
        return WrappedTypedef(name, buildTypedefTargetType(typedef))
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
        parent: WrappedElement
    ) {
        val record = templateDecl.getTemplatedDecl()
            ?.let { CXXRecordDecl(it.ptr, templateDecl.memScope) } ?: return
        val name = record.asNamedDecl().getNameAsString() ?: return
        val template = templates.getOrPut(decl.cppUsr()) {
            WrappedTemplate(name).also { parent.addChild(it) }
        }
        template.templateArgCounter = 0
        // getTemplateParameters() lives on TemplateDecl — ClassTemplateDecl's
        // address-identical primary base chain, so the same-pointer re-view is exact.
        // Each TYPE parameter mirrors ModelFactories' CXCursor_TemplateTypeParameter ->
        // WrappedTemplateParam(spelling, usr); a non-type/template-template parameter has
        // no map branch on the libclang path (`else -> null`) and is skipped the same way.
        val params = TemplateDecl(templateDecl.ptr, templateDecl.memScope)
            .getTemplateParameters()
            ?.let { TemplateParameterList(it.ptr, templateDecl.memScope) }
            ?: return
        for (i in 0u until params.size()) {
            val param = params.getParam(i)
                ?.let { NamedDecl(it.ptr, templateDecl.memScope) } ?: continue
            if (param.getKind() != Kind.TemplateTypeParm) continue
            template.addChild(
                WrappedTemplateParam(
                    param.getNameAsString() ?: continue,
                    // The identity the dependent-T WrappedTemplateRef keys back to
                    // (TypeBuilder's TemplateTypeParmType branch reads the same decl).
                    Decl(param.ptr, templateDecl.memScope).cppUsr()
                )
            )
        }
        if (templateDecl.isThisDeclarationADefinition()) {
            addBasesAndMembers(record, template, template.metadata)
        }
    }

    private fun addNamespace(decl: NamespaceDecl, parent: WrappedElement) {
        val name = decl.asNamedDecl().getNameAsString() ?: error("namespace without a name")
        // An anonymous namespace has an empty spelling; its members have TU-internal
        // linkage and can't cross the C ABI, so it is skipped transitively
        // (ModelFactories.map's CXCursor_Namespace branch).
        if (name.isEmpty()) return
        val namespace = namespaces.getOrPut(decl.asDecl().cppUsr()) {
            WrappedNamespace(name).also { parent.addChild(it) }
        }
        addContextDecls(decl.asDeclContext(), namespace)
    }

    private fun addClass(record: CXXRecordDecl, decl: Decl, parent: WrappedElement) {
        // An ANONYMOUS record is spelled by its typedef name when one exists (`typedef
        // struct { ... } max_align_t;` — libclang's cursor-spelling rule, which is how
        // max_align_t binds on that path); a truly anonymous record (libclang spells
        // "(unnamed struct at <file:line>)", never bindable) is skipped — an empty name
        // would crash WrappedClass.type ("Empty type").
        val name = record.asNamedDecl().getNameAsString()?.takeIf { it.isNotEmpty() }
            ?: RecordDecl(record.ptr, record.memScope).getTypedefNameForAnonDecl()
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
                .also { parent.addChild(it) }
        }
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
    ) {
        for (base in record.bases()) {
            if (base == null) continue
            // ModelFactories.map's CXCursor_CXXBaseSpecifier branch: the base's type built
            // through the CXType factory (structural buildWrappedType here, brick 4),
            // isPublic from the access specifier, isVirtualBase from `virtual` inheritance.
            parent.addChild(
                WrappedBase(
                    buildWrappedType(base.getType()),
                    isPublic = base.getAccessSpecifier() == AccessSpecifier.AS_public,
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
        // Anything else (static data members, nested enums) falls through
        // ModelFactories.map's `else -> return null` and is dropped; nested record decls
        // are brick-6+ (together with the nested-in-class-template skip).
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
            if (buildWrappedType(field.getType()).isConst) {
                metadata.hasPrivateConstField = true
            }
        }
    }

    private fun addField(field: FieldDecl, parent: WrappedElement, metadata: ClassMetadata) {
        // Anonymous data members (bitfield padding `int :3;`, anon union/struct members)
        // have a blank spelling and are dropped (ModelFactories' FieldDecl branch).
        val name = field.asNamedDecl().getNameAsString()?.takeIf { it.isNotBlank() } ?: return
        val type = buildWrappedType(field.getType())
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

    private fun buildMethod(method: CXXMethodDecl): WrappedMethod {
        val name = method.asNamedDecl().getNameAsString() ?: error("method without a name")
        val isConst = method.isConst()
        // Mirror the libclang front-end's return-const rule (ModelFactories.WrappedMethod):
        // a `const` method's constness only carries to a pointer/reference return; const on
        // a by-value return is meaningless for the temporary and breaks the Holder path (G8).
        val returnType = buildWrappedType(method.getReturnType()).let {
            if (isConst && (it.isPointer || it.isReference)) WrappedType.const(it) else it
        }
        // ModelFactories.WrappedMethod: STATIC for a static member (or a non-class-parent
        // function — the buildFunction path here).
        val methodType = if (method.isStatic()) MethodType.STATIC else MethodType.METHOD
        return WrappedMethod(name, returnType, methodType).also {
            it.isConst = isConst
            it.isVirtual = method.isVirtual()
            it.addArgs(method.asFunctionDecl())
        }
    }

    private fun buildFunction(function: FunctionDecl): WrappedMethod {
        val name = function.asNamedDecl().getNameAsString() ?: error("function without a name")
        val returnType = buildWrappedType(function.getReturnType())
        return WrappedMethod(name, returnType, MethodType.STATIC).also {
            it.addArgs(function)
        }
    }

    private fun WrappedMethod.addArgs(function: FunctionDecl) {
        for (i in 0u until function.getNumParams()) {
            // getParamDecl returns the Api interface (the generated related-object-getter
            // convention); re-wrap to the concrete ParmVarDecl so the upcast chain resolves.
            val param = function.getParamDecl(i)?.let { ParmVarDecl(it.ptr, function.memScope) }
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
                with(param.memScope) { defaultArgText(param) }?.takeIf { it.isNotBlank() }
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
