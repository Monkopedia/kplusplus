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

import clang.CXAvailabilityKind
import clang.CXCursor
import clang.CXCursorKind
import clang.CXCursorKind.CXCursor_ClassDecl
import clang.CXCursorKind.CXCursor_ClassTemplate
import clang.CXCursorKind.CXCursor_StructDecl
import clang.CX_CXXAccessSpecifier
import com.monkopedia.krapper.generator.DropLedger
import com.monkopedia.krapper.generator.DropPhase
import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.accessSpecifier
import com.monkopedia.krapper.generator.availability
import com.monkopedia.krapper.generator.equals
import com.monkopedia.krapper.generator.forEachRecursive
import com.monkopedia.krapper.generator.getArgument
import com.monkopedia.krapper.generator.isAnonymous
import com.monkopedia.krapper.generator.isCopyConstructor
import com.monkopedia.krapper.generator.isDefaultConstructor
import com.monkopedia.krapper.generator.isVirtual
import com.monkopedia.krapper.generator.isVirtualBase
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.numArguments
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.semanticParent
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import com.monkopedia.krapper.generator.usr
import kotlinx.cinterop.CValue

private val elementLookup = mutableMapOf<String, WrappedElement>()

abstract class WrappedElement(
    private val mutableChildren: MutableList<WrappedElement> = mutableListOf()
) {
    val children: List<WrappedElement>
        get() = mutableChildren
    var parent: WrappedElement? = null

    fun clearChildren() {
        mutableChildren.clear()
    }

    open fun addAllChildren(list: List<WrappedElement>) {
        list.forEach {
            require(!children.contains(it)) {
                "$this already contains $it"
            }
        }
        mutableChildren.addAll(list)
        list.forEach { it.parent = this }
    }

    open fun addChild(child: WrappedElement) {
        require(!children.contains(child)) {
            "$this already contain a $child"
        }
        mutableChildren.add(child)
        child.parent = this
    }

    fun removeChild(child: WrappedElement) {
        mutableChildren.remove(child)
    }

    abstract fun clone(): WrappedElement
    abstract suspend fun resolve(resolverContext: ResolveContext): ResolvedElement?

    companion object {
        // USRs of declarations the diagnostic policy chose to drop at parse time (issue
        // #9: an `error:` diagnostic attributable to a single declaration skips THAT
        // declaration and lets the rest of the header bind). A dropped USR is excised here
        // in [map] — returning null removes the declaration AND, via the
        // `map(parentCursor) ?: return@forEachRecursive` guard in [mapAll], all of its
        // members — so the broken declaration is never carried into resolution.
        // Parse-scoped: [setDroppedUsrs] is called with each TU's set right before its
        // [mapAll]. Mirrors the process-scoped [elementLookup] memo above.
        private var droppedUsrs: Set<String> = emptySet()

        /** Register the USRs of declarations dropped by the diagnostic policy for the next
         * [mapAll]. */
        fun setDroppedUsrs(usrs: Set<String>) {
            droppedUsrs = usrs
        }

        fun mapAll(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder): WrappedElement? {
            val element = map(value, null, null, resolverBuilder) ?: return null
            value.forEachRecursive { childCursor, parentCursor ->
                val parentUsr = parentCursor.usr.toKString()

                val parent =
                    map(parentCursor, null, null, resolverBuilder) ?: return@forEachRecursive
                val child =
                    map(childCursor, parent, parentUsr, resolverBuilder) ?: return@forEachRecursive
                if (child is WrappedTemplate) {
                    child.templateArgCounter = 0
                }
                if (child == element) return@forEachRecursive
                if (child.parent == parent) return@forEachRecursive
                if (parent.children.contains(child)) {
                    if (child is WrappedTemplateParam ||
                        parent is WrappedTemplateParam ||
                        child is WrappedTemplateRef ||
                        child is WrappedNamespace ||
                        child == WrappedType.UNRESOLVABLE
                    ) {
                        return@forEachRecursive
                    }
//                    throw IllegalArgumentException(
//                        "Parent ($parent) already contains child ($child)"
//                    )
                    return@forEachRecursive
                }
                if (child is WrappedMethod && parent is WrappedNamespace) {
                    // Don't add a method to a namespace when its already been added to a class.
                    if (child.parent is WrappedClass) {
                        return@forEachRecursive
                    }
                    val parentKind = childCursor.semanticParent.kind
                    if (parentKind == CXCursor_ClassDecl ||
                        parentKind == CXCursor_ClassTemplate ||
                        parentKind == CXCursor_StructDecl
                    ) {
                        return@forEachRecursive
                    }
                }
                parent.addChild(child)
            }
            return element
        }

        private fun map(
            value: CValue<CXCursor>,
            parent: WrappedElement?,
            parentUsr: String?,
            resolverBuilder: ResolverBuilder
        ): WrappedElement? {
            val usr = value.usr.toKString().orEmpty()
            // Excise a declaration the diagnostic policy dropped at parse time (issue #9).
            // Its members are excised transitively: when this cursor is a member's parent,
            // mapAll's `map(parentCursor) ?: return@forEachRecursive` guard skips the member.
            if (usr.isNotEmpty() && usr in droppedUsrs) {
                return null
            }
            val strTag = usr.ifEmpty { "$parentUsr:${value.spelling.toKString()}" }

            elementLookup[strTag]?.let { return it }

            if (value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPrivate ||
                value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXProtected ||
                value.availability == CXAvailabilityKind.CXAvailability_NotAvailable
            ) {
                if (value.kind == CXCursorKind.CXCursor_Constructor) {
                    (parent as? WrappedClass)?.metadata?.hasConstructor = true
                    (parent as? WrappedTemplate)?.metadata?.hasConstructor = true
                    // A `= delete`d or non-public COPY constructor is filtered out here, but
                    // its absence makes the type non-copyable for the C wrapper's by-value
                    // Holder placement-new. Record it so canCopyConstruct can drop by-value
                    // returns/fields of this type (T-skip).
                    if (value.isCopyConstructor) {
                        (parent as? WrappedClass)?.metadata?.hasDeletedCopyConstructor = true
                        (parent as? WrappedTemplate)?.metadata?.hasDeletedCopyConstructor = true
                    }
                }
                if (value.kind == CXCursorKind.CXCursor_CXXMethod) {
                    val opName = value.referenced.spelling.toKString()
                    if (opName == "operator new") {
                        (parent as? WrappedClass)?.metadata?.hasHiddenNew = true
                        (parent as? WrappedTemplate)?.metadata?.hasHiddenNew = true
                    } else if (opName == "operator delete") {
                        (parent as? WrappedClass)?.metadata?.hasHiddenDelete = true
                        (parent as? WrappedTemplate)?.metadata?.hasHiddenDelete = true
                    } else if (opName == "operator=" &&
                        value.isCopyAssignmentOf(parent, resolverBuilder)
                    ) {
                        // A `= delete`d/non-public COPY ASSIGNMENT is filtered out here; record
                        // it so a field of this type drops its `field = *value` setter (T-skip).
                        (parent as? WrappedClass)?.metadata?.hasDeletedCopyAssignment = true
                        (parent as? WrappedTemplate)?.metadata?.hasDeletedCopyAssignment = true
                    }
                }
                if (value.kind == CXCursorKind.CXCursor_FieldDecl) {
                    if (WrappedType(value.type, resolverBuilder).isConst) {
                        (parent as? WrappedClass)?.metadata?.hasPrivateConstField = true
                        (parent as? WrappedTemplate)?.metadata?.hasPrivateConstField = true
                    }
                }
                return null
            }
            val element = try {
                when (value.kind) {
//                CXCursorKind.CXCursor_UnexposedDecl -> TODO()
//                CXCursorKind.CXCursor_UnionDecl -> TODO()
                    CXCursorKind.CXCursor_StructDecl,
                    CXCursorKind.CXCursor_ClassDecl -> {
                        // An anonymous record (the unnamed `struct { ... }` in
                        // `struct { int a; } anon;`) has no addressable name — libclang
                        // spells it `(unnamed struct at <file>:<line>)`, which is not a
                        // valid C++ type to wrap. Binding it emits broken C
                        // (`G2_(unnamed struct at ...)_new`). Skip-not-crash: drop the
                        // anonymous record; its enclosing class still binds (the anon
                        // member itself is skipped by the FieldDecl blank-spelling guard).
                        if (value.isAnonymous) {
                            return null
                        }
                        // A class/struct lexically NESTED inside a class TEMPLATE (e.g.
                        // `template<class T> struct W { struct Inner { ... }; };`) has a
                        // qualified name that can't be spelled standalone — naming
                        // `W<...>::Inner` needs the enclosing template's arguments, which the
                        // generator doesn't have. krapper flattens it to a bare
                        // `clang::Inner` and emits `sizeof`/`reinterpret_cast`/field accessors
                        // for a type the compiler can't find ("no type named 'Inner'"). Such a
                        // type is unbindable; skip-not-crash: drop the whole class (and log).
                        // A nested class inside a NON-template class (`Outer::Inner`) IS
                        // nameable and is NOT dropped.
                        if (value.isNestedInClassTemplate) {
                            DropLedger.record(
                                value.spelling.toKString() ?: "<unnamed class>",
                                "Class nested inside a class template " +
                                    "(unnameable without template args)",
                                DropPhase.PARSE
                            )
                            println(
                                "WARN skip-not-crash: dropping class " +
                                    "'${value.spelling.toKString()}' nested inside a class " +
                                    "template (unnameable without template args)"
                            )
                            return null
                        }
                        WrappedClass(value, resolverBuilder)
                    }

                    //                CXCursorKind.CXCursor_EnumDecl -> TODO()
//                CXCursorKind.CXCursor_EnumConstantDecl -> TODO()
                    CXCursorKind.CXCursor_FieldDecl -> {
                        // Anonymous data members (anon bitfield padding `int :3;`, anon
                        // union/struct members) have a blank spelling. They have no name to
                        // address, and emitting an accessor for one produces broken C
                        // (`thiz_cast->;` + an empty accessor name). Skip-not-crash: drop the
                        // member; the rest of the class still binds.
                        if (value.referenced.spelling.toKString().isNullOrBlank()) {
                            return null
                        }
                        // A (public) data member that is a REFERENCE or CONST makes the
                        // enclosing class's implicit copy ASSIGNMENT deleted (you can't
                        // rebind a reference or reassign a const), and a reference member
                        // additionally deletes the implicit DEFAULT constructor (a reference
                        // must be initialized). These special members are never emitted as
                        // cursors (they're implicit), so the existing parse-time signal —
                        // which only fires when an EXPLICIT `= delete`/non-public member is
                        // filtered out — misses them. Record the structural facts here so a
                        // field setter (`field = *value`) of this type is dropped (canAssign)
                        // and no bogus default-construct (`new T()`) is synthesized for it
                        // (T-skip residuals: implicitly-deleted copy-assign / default ctor).
                        run {
                            val fieldType = WrappedType(value.type, resolverBuilder)
                            if (fieldType.isReference || fieldType.isConst) {
                                (parent as? WrappedClass)?.metadata
                                    ?.hasDeletedCopyAssignment = true
                                (parent as? WrappedTemplate)?.metadata
                                    ?.hasDeletedCopyAssignment = true
                            }
                            if (fieldType.isReference) {
                                (parent as? WrappedClass)?.metadata
                                    ?.hasDeletedDefaultConstructor = true
                                (parent as? WrappedTemplate)?.metadata
                                    ?.hasDeletedDefaultConstructor = true
                            }
                        }
                        WrappedField(value, resolverBuilder)
                    }

                    CXCursorKind.CXCursor_ParmDecl -> return null

                    // WrappedArgument(value, resolverBuilder)
                    CXCursorKind.CXCursor_TypedefDecl ->
                        try {
                            WrappedTypedef(value, resolverBuilder)
                        } catch (t: IllegalArgumentException) {
                            // Don't mind when parsing everything, if this reference is needed,
                            // it'll come up in resolution
                            return null
                        }

                    // A user-defined conversion operator (`operator double() const`,
                    // `operator bool()`, ...) is, at the C ABI, a zero-arg method whose
                    // return is the conversion target. clang exposes it as its own cursor
                    // kind; route it through the same WrappedMethod path as a plain method
                    // (its `referenced.spelling` is `operator double`, `type.result` is the
                    // target type). Operator.from then recognizes it via ConversionOperator
                    // and CppWriter/KotlinWriter emit the `(target)*self` cast + a `toX()`.
                    CXCursorKind.CXCursor_ConversionFunction,
                    CXCursorKind.CXCursor_FunctionDecl,
                    CXCursorKind.CXCursor_CXXMethod -> {
                        if (value.referenced.spelling.toKString() in listOf(
                                "operator new",
                                "operator new[]",
                                "operator delete",
                                "operator delete[]"
                            )
                        ) {
                            return null
                        }
                        WrappedMethod(value, resolverBuilder).also {
                            for (i in 0 until value.numArguments) {
                                it.addChild(
                                    WrappedArgument(
                                        value.getArgument(i.toUInt()),
                                        resolverBuilder,
                                        i
                                    )
                                )
                            }
                        }
                    }

                    CXCursorKind.CXCursor_Namespace -> {
                        val name = value.spelling.toKString() ?: error("Namespace without name")
                        // Anonymous namespaces have an empty spelling. Their members have
                        // TU-internal linkage and cannot be referenced across the C ABI, so
                        // they aren't bindable. Skip them (and, transitively, everything they
                        // contain) rather than emitting a binding with an empty package name.
                        if (name.isEmpty()) return null
                        WrappedNamespace(name)
                    }

                    CXCursorKind.CXCursor_Constructor ->
                        WrappedConstructor(
                            value.spelling.toKString() ?: "constructor",
                            WrappedType.VOID,
                            value.isCopyConstructor,
                            value.isDefaultConstructor
                        ).also {
                            for (i in 0 until value.numArguments) {
                                it.addChild(
                                    WrappedArgument(
                                        value.getArgument(i.toUInt()),
                                        resolverBuilder,
                                        i
                                    )
                                )
                            }
                        }

                    CXCursorKind.CXCursor_Destructor ->
                        WrappedDestructor(
                            value.spelling.toKString() ?: "destructor",
                            WrappedType.VOID
                        ).also {
                            // Thread destructor virtuality (same plumbing as method
                            // isVirtual) so the non-virtual-destructor diagnostic can fire.
                            it.isVirtual = value.isVirtual
                            for (i in 0 until value.numArguments) {
                                it.addChild(
                                    WrappedArgument(
                                        value.getArgument(i.toUInt()),
                                        resolverBuilder,
                                        i
                                    )
                                )
                            }
                        }

                    //                CXCursorKind.CXCursor_NamespaceAlias -> TODO()
                    CXCursorKind.CXCursor_TemplateTypeParameter -> WrappedTemplateParam(
                        value,
                        resolverBuilder
                    )

                    //                CXCursorKind.CXCursor_NonTypeTemplateParameter -> TODO()
//                CXCursorKind.CXCursor_TemplateTemplateParameter -> TODO()
                    CXCursorKind.CXCursor_ClassTemplate -> WrappedTemplate(value, resolverBuilder)

                    //                CXCursorKind.CXCursor_ClassTemplatePartialSpecialization -> TODO()
//                CXCursorKind.CXCursor_TypeAliasDecl -> TODO()
                    CXCursorKind.CXCursor_TypeRef -> WrappedTemplateRef(
                        value.spelling.toKString() ?: error("TypeRef without a name")
                    )

                    CXCursorKind.CXCursor_CXXBaseSpecifier -> WrappedBase(
                        try {
                            WrappedType(value.type, resolverBuilder)
                        } catch (t: IllegalArgumentException) {
                            // Don't mind when parsing everything, if this reference is needed,
                            // it'll come up in resolution
                            return null
                        },
                        isPublic = value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPublic,
                        isVirtualBase = value.isVirtualBase
                    )

                    CXCursorKind.CXCursor_TemplateRef ->
                        try {
                            WrappedType(value.type, resolverBuilder)
                        } catch (t: IllegalArgumentException) {
                            // Don't mind when parsing everything, if this reference is needed,
                            // it'll come up in resolution
                            return null
                        }

                    CXCursorKind.CXCursor_TranslationUnit -> WrappedTU()

                    else -> return null
                }
            } catch (t: RuntimeException) {
                // Skip-not-crash: building an element reaches several error() calls
                // (IllegalStateException) — a missing spelling/usr/name — that the
                // narrower per-branch catches (IllegalArgumentException only) let
                // escape as a fatal crash. When parsing everything, a single
                // unmodelable cursor must drop-and-log: if this reference is genuinely
                // needed it resurfaces during resolution, where it fails gracefully.
                DropLedger.record(
                    value.spelling.toKString()?.takeIf { it.isNotEmpty() }
                        ?: value.kind.toString(),
                    "Unmodelable ${value.kind} cursor (${t.message})",
                    DropPhase.PARSE
                )
                println(
                    "WARN skip-not-crash: dropping ${value.kind} cursor " +
                        "'${value.spelling.toKString()}' (${t.message})"
                )
                return null
            }
            elementLookup[strTag] = element
            return element
        }
    }
}

// True when [this] class/struct cursor is lexically nested inside a class TEMPLATE: any
// of its enclosing semantic-parent contexts is a `CXCursor_ClassTemplate` (or a partial
// specialization of one). Walks up the semantic-parent chain until it reaches the
// translation unit (or stops making progress). The enclosing template makes the nested
// type's qualified name unspellable without template arguments, so it can't be bound.
private val CValue<CXCursor>.isNestedInClassTemplate: Boolean
    get() {
        var current = semanticParent
        while (true) {
            val kind = current.kind
            if (kind == CXCursorKind.CXCursor_ClassTemplate ||
                kind == CXCursorKind.CXCursor_ClassTemplatePartialSpecialization
            ) {
                return true
            }
            if (kind == CXCursorKind.CXCursor_TranslationUnit ||
                kind == CXCursorKind.CXCursor_InvalidFile ||
                kind == CXCursorKind.CXCursor_NoDeclFound
            ) {
                return false
            }
            val next = current.semanticParent
            if (next.equals(current)) return false
            current = next
        }
    }

// True when [this] `operator=` cursor is a COPY assignment of [parent]'s class — i.e. it
// takes a single (const) LVALUE-reference parameter to the same class. Distinguishes copy
// assignment (whose deletion blocks the generated `field = *value` setter) from a deleted
// MOVE assignment (`operator=(T&&)`), which leaves copy assignment available, so the field
// is still assignable.
private fun CValue<CXCursor>.isCopyAssignmentOf(
    parent: WrappedElement?,
    resolverBuilder: ResolverBuilder
): Boolean {
    if (numArguments != 1) return false
    val argType = WrappedType(getArgument(0u).type, resolverBuilder)
    if (!argType.isReference) return false
    // Exclude move assignment (`operator=(T&&)`): a deleted move leaves copy available.
    if (argType.toString().contains("&&")) return false
    val className = when (parent) {
        is WrappedClass -> parent.type.toString()
        is WrappedTemplate -> parent.name
        else -> return false
    }
    val unref = argType.unreferenced
    val bare = if (unref.isConst) unref.unconst else unref
    return bare.toString() == className
}

fun WrappedElement.forEachRecursive(onEach: (WrappedElement) -> Unit) {
    for (child in children.toList()) {
        onEach(child)
        child.forEachRecursive(onEach)
    }
}

fun WrappedElement.filterRecursive(
    ret: MutableList<WrappedElement> = mutableListOf(),
    onEach: (WrappedElement) -> Boolean
): List<WrappedElement> {
    for (child in children.toList()) {
        if (onEach(child)) {
            ret.add(child)
        }
        child.filterRecursive(ret, onEach)
    }
    return ret
}

fun <T : WrappedElement> T.cloneRecursive(): T = clone().also {
    val newChildren = it.children.map { it.cloneRecursive() }
    it.clearChildren()
    it.addAllChildren(newChildren)
} as T
