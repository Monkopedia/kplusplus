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

import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.UNRESOLVABLE
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.DIRECT

val WrappedElement.parentClass: WrappedClass?
    get() = (parent as? WrappedClass) ?: parent?.parentClass
val WrappedElement.baseParent: WrappedElement
    get() = parent?.baseParent ?: parent ?: this

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

    // True for a C++20 `= default`-ed method (`CXXMethodDecl::isDefaulted()`). Only
    // meaningful here for a defaulted `operator==`, which the compiler guarantees is a
    // memberwise comparison over ALL members. KotlinWriter gates the equals/hashCode
    // contract on it: a defaulted `==` keeps the delegating `equals` + field-fold
    // `hashCode` (provably sound); a hand-written `==` — not statically classifiable as
    // memberwise — falls back to an identity contract keyed on the backing pointer.
    // A settable var so copy()/clone() preserve it, like isVirtual/isConst.
    var isDefaulted: Boolean = false

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
        it.isDefaulted = isDefaulted
        it.returnsPairSecond = returnsPairSecond
        it.returnViaMemberCall = returnViaMemberCall
        it.rangeElementType = rangeElementType
    }

    override fun clone(): WrappedMethod = WrappedMethod(name, returnType, methodType).also {
        it.parent = parent
        it.addAllChildren(children)
        it.isVirtual = isVirtual
        it.isConst = isConst
        it.isDefaulted = isDefaulted
        it.returnsPairSecond = returnsPairSecond
        it.returnViaMemberCall = returnViaMemberCall
        it.rangeElementType = rangeElementType
    }

    override fun toString(): String = "fun $name(${args.joinToString(", ")}): $returnType"
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
}
