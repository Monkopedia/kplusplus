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
import clang.Decl
import clang.DeclContext
import clang.FieldDecl
import clang.FunctionDecl
import clang.NamespaceDecl
import clang.ParmVarDecl
import clang.TranslationUnitDecl
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
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.MethodType

// The C++-AST front-end's model construction (#44 bricks 2-4): walk a parsed Clang AST on
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

private fun Decl.cppUsr(): String = "cpp:${canonical().getID()}"

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
            val record = decl.asCXXRecordDecl()
            if (record != null) {
                parent.addChild(buildClass(record))
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
            // brick-5+: typedef ELEMENTS, enums, global variables, class templates, and
            // forward-declaration redecl-collapse for classes (elementLookup's other use).
            // (Typedefs/aliases used as TYPES already resolve — TypeBuilder.kt.)
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

    private fun buildClass(record: CXXRecordDecl): WrappedClass {
        val name = record.asNamedDecl().getNameAsString() ?: error("record without a name")
        // ModelFactories' WrappedClass factory: name via wrapName (= the bare name for a
        // non-template class; template-args spelling is brick-4+) + the isAbstract flag.
        // hasDefinition() guards a forward-declared record, whose definition-data
        // accessors may not be read (libclang's isAbstract reads the definition too).
        val cls = WrappedClass(name, isAbstract = record.hasDefinition() && record.isAbstract())
        for (base in record.bases()) {
            if (base == null) continue
            // ModelFactories.map's CXCursor_CXXBaseSpecifier branch: the base's type built
            // through the CXType factory (structural buildWrappedType here, brick 4),
            // isPublic from the access specifier, isVirtualBase from `virtual` inheritance.
            cls.addChild(
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
            addMember(decl, cls)
        }
        return cls
    }

    private fun addMember(decl: Decl, cls: WrappedClass) {
        val access = decl.getAccess()
        // ModelFactories.map's top filter: a private/protected member — or one libclang
        // marks CXAvailability_NotAvailable, which is how `= delete` surfaces (bridged
        // here as FunctionDecl::isDeleted()) — is dropped, after recording the metadata
        // the resolver needs about the member's absence.
        if (access == AccessSpecifier.AS_private ||
            access == AccessSpecifier.AS_protected ||
            decl.asFunctionDecl()?.isDeleted() == true
        ) {
            recordFilteredMember(decl, cls)
            return
        }
        val constructor = decl.asCXXConstructorDecl()
        if (constructor != null) {
            cls.addChild(buildConstructor(constructor))
            return
        }
        val destructor = decl.asCXXDestructorDecl()
        if (destructor != null) {
            cls.addChild(buildDestructor(destructor))
            return
        }
        val method = decl.asCXXMethodDecl()
        if (method != null) {
            if (method.asNamedDecl().getNameAsString() in NEW_DELETE_OPERATORS) return
            cls.addChild(buildMethod(method))
            return
        }
        val field = decl.asFieldDecl()
        if (field != null) {
            addField(field, cls)
        }
        // Anything else (static data members, member typedefs, nested enums) falls
        // through ModelFactories.map's `else -> return null` and is dropped; nested
        // record decls are brick-5+ (together with the nested-in-class-template skip).
    }

    // Mirror of the metadata side-effects ModelFactories.map records for a member it
    // filters out (private/protected/deleted), keyed by the member's kind and name.
    private fun recordFilteredMember(decl: Decl, cls: WrappedClass) {
        val metadata = cls.metadata
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

    private fun addField(field: FieldDecl, cls: WrappedClass) {
        // Anonymous data members (bitfield padding `int :3;`, anon union/struct members)
        // have a blank spelling and are dropped (ModelFactories' FieldDecl branch).
        val name = field.asNamedDecl().getNameAsString()?.takeIf { it.isNotBlank() } ?: return
        val type = buildWrappedType(field.getType())
        // A public REFERENCE or CONST member implicitly deletes the enclosing class's
        // copy assignment, and a reference member also deletes the implicit default
        // constructor — recorded structurally because the implicit special members are
        // never emitted as decls (ModelFactories' FieldDecl branch, T-skip residuals).
        if (type.isReference || type.isConst) {
            cls.metadata.hasDeletedCopyAssignment = true
        }
        if (type.isReference) {
            cls.metadata.hasDeletedDefaultConstructor = true
        }
        cls.addChild(WrappedField(name, type))
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
            // brick-5+: hasDefault/defaultValue (ParmVarDecl::hasDefaultArg + source text).
            addChild(
                WrappedArgument(
                    name,
                    buildWrappedType(param.asValueDecl().getType()),
                    param.asDecl().cppUsr()
                )
            )
        }
    }
}
