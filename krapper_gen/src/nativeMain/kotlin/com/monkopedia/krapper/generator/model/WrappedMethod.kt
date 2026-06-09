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

import clang.CXCursor
import clang.CXCursorKind
import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.STRING_VIEW_TYPES
import com.monkopedia.krapper.generator.children
import com.monkopedia.krapper.generator.codegen.Operator
import com.monkopedia.krapper.generator.codegen.STACK_CONSTRUCTOR_CALLBACK
import com.monkopedia.krapper.generator.isConst
import com.monkopedia.krapper.generator.isStatic
import com.monkopedia.krapper.generator.isVirtual
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.lexicalParent
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.LONG_DOUBLE
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.UNRESOLVABLE
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.VOID
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.const
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.DIRECT
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.STACK
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.NATIVE
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.RAW_CAST
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.REINT_CAST
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.STD_MOVE
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.ALIGN_OF
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.SIZE_OF
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.STATIC
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedArgument
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedConstructor
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedDestructor
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
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
import com.monkopedia.krapper.generator.result
import com.monkopedia.krapper.generator.semanticParent
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.tokenSpellings
import com.monkopedia.krapper.generator.type
import com.monkopedia.krapper.generator.usr
import kotlinx.cinterop.CValue

val WrappedElement.parentClass: WrappedClass?
    get() = (parent as? WrappedClass) ?: parent?.parentClass
val WrappedElement.baseParent: WrappedElement
    get() = parent?.baseParent ?: parent ?: this
val ResolvedElement.parentClass: ResolvedClass?
    get() = (parent as? ResolvedClass) ?: parent?.parentClass
val ResolvedElement.baseParent: ResolvedElement
    get() = parent?.baseParent ?: parent ?: this

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

typealias MethodType = com.monkopedia.krapper.generator.resolvedmodel.MethodType

class WrappedConstructor(
    name: String,
    returnType: WrappedType,
    var isCopyConstructor: Boolean,
    val isDefaultConstructor: Boolean,
    var allocationStyle: AllocationStyle = DIRECT
) : WrappedMethod(name, returnType, MethodType.CONSTRUCTOR) {
    override fun copy(
        name: String,
        returnType: WrappedType,
        methodType: MethodType,
        children: List<WrappedElement>
    ): WrappedMethod =
        WrappedConstructor(name, returnType, isCopyConstructor, isDefaultConstructor).also {
            it.addAllChildren(children)
            it.parent = parent
        }

    override fun clone(): WrappedConstructor =
        WrappedConstructor(name, returnType, isCopyConstructor, isDefaultConstructor).also {
            it.parent = parent
            it.addAllChildren(children)
        }

    fun checkCopyConstructor(type: WrappedType) {
        if (args.size == 1 && args.first().type == type) {
            isCopyConstructor = true
        }
    }

    override suspend fun thizArg(resolverContext: ResolveContext): List<ResolvedArgument> {
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

    private suspend fun postArgs(resolverContext: ResolveContext): List<ResolvedArgument>? {
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

    override suspend fun resolve(resolverContext: ResolveContext): ResolvedConstructor? =
        with(resolverContext.currentNamer) {
            val pointedType = (
                if (allocationStyle == DIRECT) {
                    parentClass?.type?.let(::pointerTo)
                } else {
                    VOID
                }
                ) ?: return resolverContext.notifyFailed(
                this@WrappedConstructor,
                null,
                "Constructor missing parent ${parentClass?.type}"
            )
            return ResolvedConstructor(
                name,
                resolverContext.resolve(pointedType) ?: return resolverContext.notifyFailed(
                    this@WrappedConstructor,
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
}

class WrappedDestructor(name: String, returnType: WrappedType) :
    WrappedMethod(name, returnType, MethodType.DESTRUCTOR) {
    override fun copy(
        name: String,
        returnType: WrappedType,
        methodType: MethodType,
        children: List<WrappedElement>
    ): WrappedMethod = WrappedDestructor(name, returnType).also {
        it.addAllChildren(children)
        it.parent = parent
        it.isVirtual = isVirtual
    }

    override fun clone(): WrappedDestructor = WrappedDestructor(name, returnType).also {
        it.parent = parent
        it.addAllChildren(children)
        it.isVirtual = isVirtual
    }

    override suspend fun resolve(resolverContext: ResolveContext): ResolvedDestructor? =
        with(resolverContext.currentNamer) {
            return ResolvedDestructor(
                name,
                resolverContext.resolve(returnType) ?: return resolverContext.notifyFailed(
                    this@WrappedDestructor,
                    returnType,
                    "Destructor return type"
                ),
                uniqueCName,
                listOf(createThisArg(resolverContext) ?: return null)
            ).also {
                it.isVirtual = isVirtual
            }
        }
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

open class WrappedMethod(
    val name: String,
    val returnType: WrappedType,
    val methodType: MethodType = MethodType.METHOD
) : WrappedElement() {
    val args: List<WrappedArgument>
        get() = children.filterIsInstance<WrappedArgument>()

    // True for a C++ `virtual` method (including overrides). Drives the
    // inheritance-flatten naming rule (decision A): a virtual override is one
    // logical method that keeps the bare name, while a non-virtual method that
    // shadows an inherited same-name one yields the name and is class-prefixed.
    // Carried as a settable var so copy()/clone() (which keep the primary
    // constructor signature stable for subclasses) can preserve it.
    var isVirtual: Boolean = false

    // True for a C++ `const` member method (`T foo() const`). Carried so the
    // const/non-const overload dedup (T1.5, WrappedClass.modifyMethodsIfNeeded) can
    // tell the two halves of a read-only accessor pair apart and keep the const one.
    // A settable var so copy()/clone() preserve it, like isVirtual.
    var isConst: Boolean = false

    // Set by rewritePairSecondReturns (Parsing.kt): the C++ method returns
    // `std::pair<iterator, bool>`; its return is rewritten to `bool` and CppWriter emits
    // `.second` on the call. Threaded through copy()/clone()/resolve, like isVirtual.
    var returnsPairSecond: Boolean = false

    // Set by rewriteViewReturns (Parsing.kt): a non-owning view return (T1.10, e.g.
    // `llvm::StringRef`) whose value type can't cross the boundary directly. The
    // return is rewritten to an owned, bindable type (e.g. `std::string`) and CppWriter
    // emits this member call on the result (e.g. `.str()`) to materialize it. Threaded
    // through copy()/clone()/resolve, like returnsPairSecond.
    var returnViaMemberCall: String? = null

    // Set when a `llvm::iterator_range<It>`-returning method is materialized into a bound
    // `std::vector<Elem*>` (T1.3). Holds the element class's fully-qualified C++ spelling
    // (e.g. `clang::Decl`). When non-null, the return type has been rewritten to
    // `std::vector<Elem*>` and CppWriter emits a materializing loop
    // (`for (it = r.begin(); it != r.end(); ++it) v.push_back(kpp_to_elem_ptr<Elem>(*it))`)
    // instead of a direct call. Threaded through copy()/clone()/resolve, like
    // returnViaMemberCall.
    var rangeElementType: String? = null

    constructor(method: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        method.referenced.spelling.toKString() ?: error("Can't find name of $method"),
        rangeVectorReturn(method) ?: WrappedType(method.type.result, resolverBuilder).let {
            // A `const` method says nothing about its RETURN type's constness. Const on
            // a by-value (non-pointer/reference) return is meaningless for the returned
            // temporary (G8) and actively breaks the by-value Holder path — placement-new
            // into a `const T*` is ill-formed. Only carry method-derived const for a
            // pointer/reference return, where const-of-pointee can matter; strip it on a
            // by-value return.
            if (method.isConst && (it.isPointer || it.isReference)) const(it) else it
        },
        if (method.isStatic ||
            (method.semanticParent.kind !in clsParents && method.lexicalParent.kind !in clsParents)
        ) {
            MethodType.STATIC
        } else {
            MethodType.METHOD
        }
    ) {
        isVirtual = method.isVirtual
        isConst = method.isConst
        // T1.3: an `iterator_range<It>` return is materialized into the
        // `std::vector<Elem*>` produced above; record Elem so CppWriter emits the loop.
        rangeElementType = extractRangeReturn(method.type.result)?.elementSpelling
    }

    open fun copy(
        name: String = this.name,
        returnType: WrappedType = this.returnType,
        methodType: MethodType = this.methodType,
        children: List<WrappedElement> = this.children.toList()
    ): WrappedMethod = WrappedMethod(name, returnType, methodType).also {
        it.addAllChildren(children)
        it.parent = parent
        it.isVirtual = isVirtual
        it.isConst = isConst
        it.returnsPairSecond = returnsPairSecond
        it.returnViaMemberCall = returnViaMemberCall
        it.rangeElementType = rangeElementType
    }

    override fun clone(): WrappedMethod = WrappedMethod(name, returnType, methodType).also {
        it.parent = parent
        it.addAllChildren(children)
        it.isVirtual = isVirtual
        it.isConst = isConst
        it.returnsPairSecond = returnsPairSecond
        it.returnViaMemberCall = returnViaMemberCall
        it.rangeElementType = rangeElementType
    }

    protected open suspend fun thizArg(resolverContext: ResolveContext): List<ResolvedArgument>? {
        if (methodType == SIZE_OF || methodType == ALIGN_OF ||
            methodType == STATIC
        ) {
            return emptyList()
        }
        return listOf(createThisArg(resolverContext) ?: return null)
    }

    override suspend fun resolve(resolverContext: ResolveContext): ResolvedMethod? =
        with(resolverContext.currentNamer) {
            val (rawMapping, rawResolved) = resolverContext.mapAndResolve(returnType)
                ?: return resolverContext.notifyFailed(
                    this@WrappedMethod,
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
                    this@WrappedMethod,
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
                    this@WrappedMethod,
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
                                this@WrappedMethod,
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
                Operator.from(this@WrappedMethod)?.resolvedOperator,
                (thizArg(resolverContext) ?: return null) +
                    (resolveArguments(resolverContext) ?: return null),
                returnStyle,
                argCastNeedsPointer,
                qualified
            ).also {
                it.isVirtual = isVirtual
                it.returnsPairSecond = returnsPairSecond
                it.returnViaMemberCall = returnViaMemberCall
                it.rangeElementType = rangeElementType
                it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
            }
        }

    protected suspend fun resolveArguments(
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

    override fun toString(): String = "fun $name(${args.joinToString(", ")}): $returnType"

    companion object {
        private val clsParents = listOf(
            CXCursorKind.CXCursor_ClassTemplate,
            CXCursorKind.CXCursor_ClassDecl,
            CXCursorKind.CXCursor_StructDecl
        )

        // T1.3: if [method] returns `llvm::iterator_range<It>`, build the materialized
        // `std::vector<Elem*>` return type (a real template type, so it resolves through
        // the existing vector-instantiation/CppVector path — yielding a `for`-iterable on
        // the Kotlin side). Returns null for any non-range return, so the caller falls
        // back to the normal return-type construction.
        private fun rangeVectorReturn(method: CValue<CXCursor>): WrappedType? {
            val range = extractRangeReturn(method.type.result) ?: return null
            return WrappedTemplateType(
                WrappedType("std::vector"),
                listOf(pointerTo(WrappedType(range.elementSpelling)))
            )
        }
    }
}

class WrappedArgument(
    val name: String,
    val type: WrappedType,
    val usr: String = "",
    val hasDefault: Boolean = false,
    // The C++ source text of the parameter's default-value expression (e.g. "10",
    // "' '", "true", "nullptr"), or null when the parameter has no default. The
    // KotlinWriter maps a renderable subset of these to Kotlin default values.
    val defaultValue: String? = null
) : WrappedElement() {
    constructor(arg: CValue<CXCursor>, resolverBuilder: ResolverBuilder, index: Int = 0) : this(
        arg.spelling.toKString()?.takeIf { it.isNotBlank() } ?: "_arg_$index",
        WrappedType(arg.type, resolverBuilder),
        arg.usr.toKString() ?: "",
        hasDefault(arg),
        defaultValue(arg)
    )

    // True only when this param can be SAFELY left off the generated C++ call because
    // the C++ method genuinely has a trailing C++ default for it. Stricter than the raw
    // [hasDefault]: that flag is derived from a cursor-children heuristic that ALSO fires
    // for an INTEGER token belonging to the parameter's TYPE rather than to a default
    // value — e.g. an array bound (`int (&)[4]`) or a non-type template argument
    // (`Mask<4>`). Such a param has NO real default, so omitting it from the call emits a
    // short, non-compiling `m()`. Treat a param as omittable only when its default token
    // is a genuine value expression: it must NOT be the array bound or a template
    // argument. We approximate this structurally — array types and template-instantiation
    // types are never C++-default-omittable here — so a failed param of those shapes drops
    // the whole method (skip-not-crash) instead of producing a short call.
    val isOmittableDefault: Boolean
        get() = hasDefault && !typeCarriesFalseDefault

    // True when [hasDefault] is a FALSE positive — the cursor heuristic misread a token in
    // the param's type as a C++ default, so the param is not legitimately omittable. Four
    // shapes carry a false default: a non-const output reference (the heuristic can misfire
    // on the `<...>` in `SmallPtrSetImpl<X>&`), an array bound (`int(&)[4]`), an unmodelable
    // callback (`function_ref<R(Args)>` / `std::function<R(Args)>`, whose parenthesized
    // signature the heuristic reads as a default), and a by-value template over a NON-TYPE
    // arg (`Mask<4>`'s `4`). A by-value template over only TYPE args
    // (`shared_ptr<PCHContainerOperations>`) and a CONST reference with a genuine default
    // (`const Alloc& = Alloc()`) keep their real default and stay omittable.
    private val typeCarriesFalseDefault: Boolean
        get() {
            // A NON-CONST lvalue reference is an output/in-out param (e.g.
            // CollectInheritedProtocols's `SmallPtrSetImpl<ObjCProtocolDecl*>&`), never a
            // genuinely-omittable default — a misread hasDefault on it must NOT trim it (that
            // yields a short, non-compiling call). Keying on NON-CONST-ness catches it even
            // when the referent parsed as a bare string rather than a structured
            // WrappedTemplateType (which is how it slips past the template check below at
            // Clang scale — the regression a blanket `isReference -> true` was added for).
            //
            // A CONST reference, by contrast, CAN carry a real, omittable default —
            // `std::basic_string(const CharT*, const Alloc& = Alloc())`'s trailing allocator
            // param is `const Alloc&`. A blanket `isReference -> true` wrongly made that
            // REQUIRED, so the const-char*/char/initializer_list string ctors all dropped
            // (their genuine default became un-modelable). So fall through for a const ref and
            // judge it by its referent, exactly like a by-value param.
            if (type.isReference && !type.unreferenced.isConst) return true
            // Strip a leading reference so the array/template-token tests below see the
            // referent (`const Alloc&` -> `const Alloc`); guarded by isReference so
            // `unreferenced` never throws on a non-reference.
            val referent = if (type.isReference) type.unreferenced else type
            if (referent.isArray) return true
            // A by-value template carries a false default only when it has a NON-TYPE
            // (value) template arg (Mask<4>'s `4`, Flags<true>'s `true`, Foo<'x'>'s `'x'`)
            // — the token the hasDefault cursor heuristic misreads as a default value. A
            // by-value template with only TYPE args (shared_ptr<X>) has a real default and
            // stays omittable.
            //
            // STRUCTURAL signal: a non-type template arg is NOT a type, so libclang's
            // clang_Type_getTemplateArgumentType reports it as CXType_Invalid, which the
            // type builder maps to UNRESOLVABLE (`WrappedTypeReference("unresolveable")`).
            // So `Mask<4>` / `Flags<true>` surface as `Mask<unresolveable>` regardless of
            // whether the value is a number, bool, or char — that's what fires here. A
            // resolvable type arg (shared_ptr<PCHContainerOperations>) is never
            // UNRESOLVABLE, so the genuine default stays omittable.
            //
            // Secondary path: when the value instead survives as a literal STRING token
            // (the spelling-reconstruction path can yield `Mask<4>` with arg `"4"`),
            // [isNonTypeTemplateArg] classifies that token. Either path catches it.
            //
            // Third path — an unmodelable CALLBACK param (`llvm::function_ref<R(Args)>`,
            // `std::function<R(Args)>`): its instantiation carries a FUNCTION TYPE (`int
            // (Thing *)`) the type model can't bind, so the param never resolves. But the
            // hasDefault cursor heuristic misreads the callback signature's parenthesized
            // param list as a default value (e.g. `cb`'s "default" surfaces as `Thing*`), so
            // a callback param with NO real C++ default looks omittable. Trimming it then
            // emits a SHORT call (`m(a)` for a 2-arg method) — an arity mismatch that fails
            // to compile and aborts the whole build (the clangwalk self-host hit this on
            // `clang::ASTContext::adjustType` / `forEachMultiversionedFunctionVersion`). The
            // callback signature's parens ride on the referent's OWN spelling whether the
            // instantiation surfaced as a structured WrappedTemplateType (`FnRef<int (Thing
            // *)>`, a function-type template arg) OR — at Clang scale, when libclang doesn't
            // report the template args — collapsed to a bare type-reference string (the same
            // fallback CollectInheritedProtocols's `SmallPtrSetImpl<...>&` hits above, which
            // is why the structured-template check alone missed `adjustType`). A function-type
            // spelling is never a genuine, trimmable default, so a `(` in the referent
            // spelling is the false-default signal: drop the whole method instead.
            if (referent.isFunctionType) return true
            // A by-value template over a NON-TYPE (value) arg also carries a false default
            // (Mask<4>'s `4`, etc.) — see [isNonTypeTemplateArg] and the UNRESOLVABLE note.
            return referent is WrappedTemplateType &&
                referent.templateArgs.any { it == UNRESOLVABLE || it.isNonTypeTemplateArg }
        }

    // A WrappedType whose spelling carries a FUNCTION TYPE — the `R(Args...)` callback
    // signature of `function_ref<R(Args...)>` / `std::function<R(Args...)>`, whether it rides
    // as a structured template arg (`FnRef<int (Thing *)>`) or, at Clang scale, as the whole
    // collapsed type-reference string. Plain/pointer/reference/template type spellings never
    // contain `(`, so a `(` is the clean structural signal of a (function-)callable type the
    // model can't bind by value.
    private val WrappedType.isFunctionType: Boolean
        get() = '(' in toString()

    // A template-argument WrappedType whose spelling is a VALUE literal rather than a
    // type name — i.e. a non-type template parameter's argument that survived as a string
    // token rather than collapsing to UNRESOLVABLE. Recognizes the literal forms: a
    // decimal/hex/octal/float number (with an optional C++ integer/float suffix like `u`,
    // `L`, `ULL`, `f`), a `true`/`false` bool, or a single-quoted character literal
    // (`'x'`, `'\n'`). A plain enum-constant arg (`Foo<SomeEnum::Flag>`) is spelling-
    // indistinguishable from a qualified type name and so is NOT claimed here.
    private val WrappedType.isNonTypeTemplateArg: Boolean
        get() {
            val token = toString().trim()
            if (token.isEmpty()) return false
            if (token == "true" || token == "false") return true
            if (token.startsWith("'") && token.endsWith("'") && token.length >= 3) return true
            // Strip a trailing C++ numeric literal suffix (u/l/f combinations) before the
            // numeric test so `4u`, `0x4ULL`, `1.5f` are recognized as values.
            val numeric = token.trimEnd('u', 'U', 'l', 'L', 'f', 'F')
            if (numeric.isEmpty()) return false
            if (numeric.startsWith("0x") || numeric.startsWith("0X")) {
                return numeric.length > 2 &&
                    numeric.substring(2).all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
            }
            return numeric.toDoubleOrNull() != null
        }

    override fun clone(): WrappedArgument =
        WrappedArgument(name, type, usr, hasDefault, defaultValue).also {
            it.parent = parent
            it.addAllChildren(children)
        }

    override fun toString(): String = "$name: $type"

    override fun equals(other: Any?): Boolean =
        (other as? WrappedArgument)?.name == name && other.type == type

    override suspend fun resolve(resolverContext: ResolveContext): ResolvedElement? = null

    suspend fun resolveArgument(resolverContext: ResolveContext): ResolvedArgument? {
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

    companion object {
        private val nonDefaultChildKinds = listOf(
            CXCursorKind.CXCursor_TypeRef,
            CXCursorKind.CXCursor_TemplateRef,
            CXCursorKind.CXCursor_NamespaceRef
        )

        private fun defaultExpr(value: CValue<CXCursor>): CValue<CXCursor>? {
            val last = value.children.lastOrNull() ?: return null
            return last.takeIf { it.kind !in nonDefaultChildKinds }
        }

        fun hasDefault(value: CValue<CXCursor>): Boolean = defaultExpr(value) != null

        // Recovers the C++ source text of the parameter's default-value expression by
        // tokenizing the default sub-expression cursor's extent (libclang exposes no
        // direct accessor). Returns null when there is no default.
        fun defaultValue(value: CValue<CXCursor>): String? {
            val expr = defaultExpr(value) ?: return null
            return expr.tokenSpellings().joinToString("").takeIf { it.isNotBlank() }
        }
    }
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
