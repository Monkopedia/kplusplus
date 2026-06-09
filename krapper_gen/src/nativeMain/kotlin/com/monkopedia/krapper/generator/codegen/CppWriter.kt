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

import com.monkopedia.krapper.OperatorType.ASSIGN
import com.monkopedia.krapper.ResolvedOperator
import com.monkopedia.krapper.generator.builders.Call
import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.CodeGenerator
import com.monkopedia.krapper.generator.builders.CodeStringBuilder
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.builders.ExternCClose
import com.monkopedia.krapper.generator.builders.ExternCOpen
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.builders.New
import com.monkopedia.krapper.generator.builders.Raw
import com.monkopedia.krapper.generator.builders.RawCast
import com.monkopedia.krapper.generator.builders.Return
import com.monkopedia.krapper.generator.builders.Symbol
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.builders.addressOf
import com.monkopedia.krapper.generator.builders.appendLine
import com.monkopedia.krapper.generator.builders.arrow
import com.monkopedia.krapper.generator.builders.assign
import com.monkopedia.krapper.generator.builders.buildCode
import com.monkopedia.krapper.generator.builders.coloncolon
import com.monkopedia.krapper.generator.builders.comment
import com.monkopedia.krapper.generator.builders.define
import com.monkopedia.krapper.generator.builders.dereference
import com.monkopedia.krapper.generator.builders.dot
import com.monkopedia.krapper.generator.builders.function
import com.monkopedia.krapper.generator.builders.include
import com.monkopedia.krapper.generator.builders.includeSys
import com.monkopedia.krapper.generator.builders.op
import com.monkopedia.krapper.generator.builders.reference
import com.monkopedia.krapper.generator.builders.type
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.DIRECT
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.STACK
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.NATIVE
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.RAW_CAST
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.REINT_CAST
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode.STD_MOVE
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedConstructor
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedField
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.ARG_CAST
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.COPY_CONSTRUCTOR
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.ENUM_RETURN
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.RETURN
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.RETURN_REFERENCE
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.STRING
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.STRING_POINTER
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.VOID
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.VOIDP
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.VOIDP_REFERENCE
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedType

const val STACK_CONSTRUCTOR_CALLBACK = "StackConstructorCallback"
class CppWriter(
    private val cppFile: File,
    codeBuilder: CppCodeBuilder,
    policy: CodeGenerationPolicy = ThrowPolicy
) : CodeGenerator<CppCodeBuilder>(codeBuilder, policy) {

    private var lookup: ClassLookup = ClassLookup(emptyList())

    // True when any emitted down-cast helper uses LLVM-style `llvm::dyn_cast` (a derived
    // with a static `classof`). Gates the `<llvm/Support/Casting.h>` include, which must
    // appear in the module preamble (emitted before per-class bodies) and must NOT appear
    // for the generic `dynamic_cast` path (no LLVM headers present there).
    private var needsLlvmCasting: Boolean = false

    // True when any method materializes an `iterator_range` into a `std::vector<Elem*>`
    // (T1.3). Gates the one-time `kpp_to_elem_ptr<E>` overload pair emitted in the module
    // preamble (the range loops call it to normalize `*it` to `Elem*`).
    private var needsRangeHelper: Boolean = false

    // Per-method-body counter giving the range loop's locals unique names, so two range
    // methods (or a future two-ranges-in-one-body) never collide on `__vec`/`__range`.
    private var rangeCounter: Int = 0

    override fun generate(
        moduleName: String,
        headers: List<String>,
        classes: List<ResolvedElement>
    ) {
        lookup = ClassLookup(classes.filterIsInstance<ResolvedClass>())
        needsLlvmCasting = lookup.allClasses.any { base ->
            downCastTargetsFor(base, lookup.allClasses) { lookup[it] }.any { it.useLlvmDynCast }
        }
        needsRangeHelper = lookup.allClasses.any { cls ->
            cls.children.filterIsInstance<ResolvedMethod>().any { it.rangeElementType != null }
        }
        super.generate(moduleName, headers, classes)
    }

    override fun CppCodeBuilder.onGenerate(
        cls: ResolvedClass,
        handleChildren: CppCodeBuilder.() -> Unit
    ) {
        comment("BEGIN KRAPPER GEN for ${cls.type}")
        appendLine()
        handleChildren()
        generateCastHelpers(cls)
        generateDownCastHelpers(cls)
        appendLine()
        comment("END KRAPPER GEN for ${cls.type}")
        appendLine()
        appendLine()
    }

    // For every (transitive) public, non-virtual base B of D, emit the upcast helper
    //   void* D_as_B(void* p) { return static_cast<B*>(reinterpret_cast<D*>(p)); }
    // The reinterpret_cast recovers the real `D*` from the opaque pointer; the
    // static_cast applies the base subobject's (possibly non-zero) offset. The plain
    // reinterpret_cast the wrappers normally use is only correct when that offset is 0,
    // which fails for a 2nd base and even for a single non-polymorphic base under a
    // vtable-adding derived. (HeaderWriter emits the matching declaration; KotlinWriter
    // calls these as `D.asB()`.)
    private fun CppCodeBuilder.generateCastHelpers(cls: ResolvedClass) {
        for (target in castTargetsFor(cls) { lookup[it] }) {
            val derivedPtr = "${target.cls.type}*"
            val basePtr = "${target.base.type}*"
            function {
                retType = type(VOID_PTR)
                name = target.cHelperName
                val p = define("p", VOID_PTR)
                body {
                    +Return(
                        staticCast(
                            basePtr,
                            reinterpretRaw(p, derivedPtr)
                        )
                    )
                }
            }
            appendLine()
        }
    }

    // For base B (= [cls]), every bound derived D reachable through B emits the CHECKED
    // down-cast helper
    //   void* B_dyncast_D(void* p) { return dyn-cast<D>(reinterpret_cast<B*>(p)); }
    // returning the `D*` only when the object really is a D, else null. The cast form is
    //   * `llvm::dyn_cast_or_null<D>(b)` when D has a static `classof` (LLVM-style RTTI,
    //     the only form that works under `-fno-rtti`, e.g. Clang); needs Casting.h.
    //   * `dynamic_cast<D*>(b)`          otherwise (standard C++ RTTI; the generic default).
    // (HeaderWriter declares the match; KotlinWriter calls these as nullable `B.asD(): D?`.)
    private fun CppCodeBuilder.generateDownCastHelpers(cls: ResolvedClass) {
        for (target in downCastTargetsFor(cls, lookup.allClasses) { lookup[it] }) {
            val basePtr = "${target.base.type}*"
            val derivedType = "${target.derived.type}"
            function {
                retType = type(VOID_PTR)
                name = target.cHelperName
                val p = define("p", VOID_PTR)
                body {
                    +Return(
                        dynOrLlvmCast(
                            derivedType,
                            reinterpretRaw(p, basePtr),
                            target.useLlvmDynCast
                        )
                    )
                }
            }
            appendLine()
        }
    }

    // The checked-cast expression for a down-cast helper. [useLlvmDynCast] picks the form:
    //   true  -> `llvm::dyn_cast_or_null<D>(value)`  (LLVM `classof`-based; `_or_null`
    //            tolerates a null input, mirroring dynamic_cast on null).
    //   false -> `dynamic_cast<D*>(value)`           (standard C++ RTTI).
    // Both yield `D*`-or-null, which the shim returns as the opaque `void*`.
    private fun dynOrLlvmCast(derivedType: String, value: Symbol, useLlvmDynCast: Boolean): Symbol =
        if (useLlvmDynCast) {
            castSymbol("llvm::dyn_cast_or_null<$derivedType>(", value)
        } else {
            castSymbol("dynamic_cast<$derivedType*>(", value)
        }

    // `static_cast<type>(value)` — used to apply a base subobject offset in the upcast
    // helpers (the wrappers' usual RawCast/reinterpret can't shift the pointer).
    private fun staticCast(type: String, value: Symbol): Symbol =
        castSymbol("static_cast<$type>(", value)

    // `reinterpret_cast<type>(arg)` over a RAW C++ type string (not a ResolvedType) — used
    // only by the cast helpers, where the type is built by hand. Named distinctly from the
    // ResolvedType-typed `reinterpret` below to avoid confusing the two at the call site.
    private fun reinterpretRaw(arg: LocalVar, type: String): Symbol =
        castSymbol("reinterpret_cast<$type>(", arg.reference)

    // A C++ cast expression `prefix value )` — every cast helper above is just a fixed
    // `cast<...>(` prefix wrapped around a value and closed with `)`. [prefix] must include
    // the trailing `(`.
    private fun castSymbol(prefix: String, value: Symbol): Symbol = object : Symbol {
        override fun build(builder: CodeStringBuilder) {
            builder.append(prefix)
            value.build(builder)
            builder.append(")")
        }
    }

    override fun CppCodeBuilder.onGenerate(
        moduleName: String,
        headers: List<String>,
        handleChildren: CppCodeBuilder.() -> Unit
    ) {
        val splitCamelcase = moduleName.splitCamelcase()
        val headerLabel = splitCamelcase.joinToString("_") { it.lowercase() }
        include("$headerLabel.h")
        for (header in headers) {
            include("${File(header).relativeTo(cppFile)}")
        }
        includeSys("vector")
        includeSys("string")
        includeSys("iterator")
        // Only when a down-cast uses LLVM-style RTTI (a derived with a static `classof`):
        // pull in `llvm::dyn_cast`. Guarded so the generic `dynamic_cast` path (no LLVM in
        // the include set) never references this header.
        if (needsLlvmCasting) {
            includeSys("llvm/Support/Casting.h")
        }
        appendLine()
        // T1.3: `kpp_to_elem_ptr<E>` normalizes a range iterator's `*it` to `E*`,
        // covering both already-pointer derefs (`Decl*`, the `push_back(*it)` shape) and
        // lvalue derefs (`CXXBaseSpecifier&`, needing `&*it`). E is supplied explicitly by
        // each loop, so the overload is unambiguous. The `const E*`/`const E&` overloads
        // cover iterators whose `*it` yields a const pointer/reference (e.g.
        // `CXXMethodDecl::overridden_methods()` dereferences to `const CXXMethodDecl* const`):
        // the materialized `std::vector<E*>` is non-const (matching k++'s non-const-pointer
        // binding model), so const_cast the element to `E*`. Templates can't live inside
        // `extern "C"`, so emit it before the block.
        if (needsRangeHelper) {
            +Raw("template <class E> static E* kpp_to_elem_ptr(E* p) { return p; }")
            +Raw("template <class E> static E* kpp_to_elem_ptr(E& r) { return &r; }")
            +Raw("template <class E> static E* kpp_to_elem_ptr(const E* p) { return (E*)p; }")
            +Raw("template <class E> static E* kpp_to_elem_ptr(const E& r) { return (E*)&r; }")
            appendLine()
        }
        +ExternCOpen
        appendLine()

        +Raw("typedef void (*$STACK_CONSTRUCTOR_CALLBACK)(void*, void*)")
        appendLine()

        handleChildren()

        appendLine()
        +ExternCClose
        appendLine()
    }

    override fun CppCodeBuilder.onGenerate(cls: ResolvedClass, method: ResolvedMethod) {
        function {
            generateMethodSignature(method)
            val args = addArgs(method)
            body {
                generateMethodBody(cls, method, args)
            }
        }
        appendLine()
    }

    override fun CppCodeBuilder.onGenerate(cls: ResolvedClass, field: ResolvedField) {
        function {
            val args = generateFieldGet(field)
            body {
                generateFieldGetBody(cls, field, args)
            }
        }
        appendLine()
        // A const (or reference) data member can't be assigned, so emitting a `_set`
        // wrapper produces non-compiling C ("cannot assign to const-qualified").
        // Skip-not-crash: emit only the getter for such members (KotlinWriter already
        // suppresses the matching `var` setter on field.isConst). HeaderWriter mirrors
        // this so the declaration isn't left dangling.
        if (!field.isConst) {
            function {
                val args = generateFieldSet(field)
                body {
                    generateFieldSetBody(cls, field, args)
                }
            }
            appendLine()
        }
    }

    override fun CppCodeBuilder.onGenerate(method: ResolvedMethod) {
        function {
            generateMethodSignature(method)
            val args = addArgs(method)
            body {
                val argCasts = args.map { a ->
                    generateArgumentCast(a)
                }.toMutableList()
                val returnCast = if (method.returnStyle == ARG_CAST) argCasts.removeLast() else null
                val call = Raw(method.qualified) coloncolon Call(
                    method.name,
                    *(argCasts.map { it.reference }.toTypedArray())
                )
                generateReturn(
                    method.rangeElementType?.let { emitRangeVector(call, it) }
                        ?: method.applyReturnRewrites(call),
                    method.returnStyle,
                    method.returnType,
                    returnCast
                )
            }
        }
    }

    private fun CppCodeBuilder.generateMethodBody(
        cls: ResolvedClass,
        method: ResolvedMethod,
        args: List<SignatureArgument>
    ) {
        val argCasts = args.map { a ->
            generateArgumentCast(a)
        }.toMutableList()
        val returnCast = if (method.returnStyle == ARG_CAST) argCasts.removeLast() else null
        when (method.methodType) {
            MethodType.CONSTRUCTOR -> {
                method as ResolvedConstructor
                when (method.allocationStyle) {
                    DIRECT -> {
                        val locationCast = argCasts.removeFirst()
                        +Return(
                            New(
                                Call(
                                    cls.type.toString(),
                                    *(argCasts.map { it.reference }.toTypedArray())
                                ),
                                locationCast.reference
                            )
                        )
                    }

                    STACK -> {
                        val locationCast = argCasts.removeFirst()
                        val callbackCast = argCasts.removeLast()
                        val item = +define(
                            "item",
                            cls.type,
                            constructorArgs = argCasts.map { it.reference }
                        )
                        +Call(callbackCast.reference, item.addressOf, locationCast.reference)
                    }
                }
            }

            MethodType.SIZE_OF -> {
                +Return(
                    Call(
                        "sizeof",
                        Raw(cls.type.toString())
                    )
                )
            }

            MethodType.ALIGN_OF -> {
                +Return(
                    Call(
                        "alignof",
                        Raw(cls.type.toString())
                    )
                )
            }

            MethodType.DESTRUCTOR -> {
                val thizCast = argCasts.removeFirst()
                +(thizCast.pointerReference arrow Call(method.name.removeTemplate()))
            }

            MethodType.STATIC_OP -> {
                val thizCast = argCasts.removeFirst()
                val call = (thizCast.reference).op(
                    method.name.substring("operator".length),
                    argCasts.map { it.reference }.single()
                )
                generateReturn(call, method.returnStyle, method.returnType, returnCast)
            }

            MethodType.STATIC -> {
                val call = Raw(cls.type.toString()) coloncolon Call(
                    method.name,
                    *(argCasts.map { it.reference }.toTypedArray())
                )
                generateReturn(
                    method.rangeElementType?.let { emitRangeVector(call, it) }
                        ?: method.applyReturnRewrites(call),
                    method.returnStyle,
                    method.returnType,
                    returnCast
                )
            }

            MethodType.METHOD -> {
                val thizCast = argCasts.removeFirst()
                val thizRef = thizCast.pointerReference
                val operator = method.operator
                val call =
                    when {
                        // A conversion operator (`operator double`, ...) takes no args:
                        // cast the dereferenced receiver to the (scalar) destination —
                        // `(double)*self` — and let the RETURN style hand it back.
                        operator == ResolvedOperator.CONVERSION -> {
                            RawCast(method.returnType.cType.typeString, thizRef.dereference)
                        }

                        operator?.supportsDirectCall == true -> {
                            thizRef.dereference.op(operator.cppOp, argCasts.first().reference)
                        }

                        operator?.operatorType == ASSIGN -> {
                            thizRef.dereference.assign(
                                argCasts.first().reference,
                                operator == ResolvedOperator.PLUS_EQUALS
                            )
                        }

                        else -> {
                            thizRef arrow Call(
                                conversionCallName(method),
                                *(argCasts.map { it.reference }.toTypedArray())
                            )
                        }
                    }
                // T1.3: an `iterator_range<It>` return was rewritten to
                // `std::vector<Elem*>`; materialize it with a begin()/end() loop and
                // hand back the populated vector (placement-new'd into the Holder by the
                // ARG_CAST return below). Other methods: `insert`'s `.second` /
                // StringRef's `.str()` rewrites apply via applyReturnRewrites.
                val returnExpr = method.rangeElementType?.let { elem ->
                    emitRangeVector(call, elem)
                } ?: method.applyReturnRewrites(call)
                generateReturn(returnExpr, method.returnStyle, method.returnType, returnCast)
            }
        }
    }

    private fun CppCodeBuilder.generateArgumentCast(a: SignatureArgument) = when (a.arg?.castMode) {
        NATIVE -> a
        ArgumentCastMode.STRING -> createStringCast(a)
        ArgumentCastMode.STRING_VIEW -> createViewCast(a)
        RAW_CAST -> createRawCast(a)
        STD_MOVE, REINT_CAST, null -> createCast(a)
    }

    // Apply the post-call return rewrites recorded during resolution, in order:
    //   * returnsPairSecond: `(call).second` — hand back the pair's bool half.
    //   * returnViaMemberCall: `(call).<name>()` — either materialize a non-owning view
    //     into an owned value (T1.10, e.g. StringRef -> std::string via `.str()`) or
    //     release ownership of a move-only return (T1.7e, unique_ptr<T> -> T* via
    //     `.release()`).
    private fun ResolvedMethod.applyReturnRewrites(call: Symbol): Symbol {
        var expr = if (returnsPairSecond) call dot Raw("second") else call
        returnViaMemberCall?.let { expr = expr dot Call(it) }
        return expr
    }

    // T1.3: materialize an `iterator_range<It>` ([call] is e.g. `thiz->decls()`) into a
    // local `std::vector<Elem*>` and return that vector as the value to hand back. Emits:
    //   auto __range_N = <call>;
    //   std::vector<Elem*> __vec_N;
    //   for (auto __it = __range_N.begin(); __it != __range_N.end(); ++__it)
    //       __vec_N.push_back(kpp_to_elem_ptr<Elem>(*__it));
    // The `kpp_to_elem_ptr<Elem>` overload normalizes `*__it` to `Elem*` whether the
    // iterator dereferences to an already-pointer (`Decl*`, from decl_iterator/
    // specific_decl_iterator) or to an lvalue (`CXXBaseSpecifier&`, from a pointer
    // iterator). Elem is fixed (recovered at parse time), so the overload is unambiguous.
    // The returned vector flows into the ARG_CAST/Holder path (placement-new), exactly
    // like any other by-value `std::vector` return — so the Kotlin side gets a
    // `for`-iterable CppVector<Elem>.
    private fun CppCodeBuilder.emitRangeVector(call: Symbol, elementType: String): Symbol {
        val id = rangeCounter++
        val rangeVar = "__range_$id"
        val vecVar = "__vec_$id"
        val itVar = "__it_$id"
        val callStr = buildCode { call.build(this) }
        +Raw("auto $rangeVar = $callStr")
        +Raw("std::vector<$elementType*> $vecVar")
        +Raw(
            "for (auto $itVar = $rangeVar.begin(); $itVar != $rangeVar.end(); ++$itVar) " +
                "$vecVar.push_back(kpp_to_elem_ptr<$elementType>(*$itVar))"
        )
        return Raw(vecVar)
    }

    private fun CppCodeBuilder.generateReturn(
        call: Symbol,
        returnStyle: ReturnStyle,
        returnType: ResolvedType,
        returnCast: SignatureArgument?
    ) {
        when (returnStyle) {
            VOID -> +call

            VOIDP_REFERENCE -> +Return(RawCast("void*", call.addressOf))

            VOIDP -> +Return(RawCast("void*", call))

            // Placement-new, not assignment: the caller hands back raw,
            // unconstructed memory (from *_Holder(), which allocs size/align but
            // runs no constructor). `*ret = call` would copy/move-assign into a
            // non-object — UB that crashes at runtime for non-trivial types like
            // std::string. `new (ret) T(call)` constructs in place instead.
            ARG_CAST -> +New(
                Call(returnType.toConstructor(), call),
                returnCast!!.pointerReference
            )

            STRING -> createStringReturn(call)

            STRING_POINTER -> createPointedStringReturn(call)

            COPY_CONSTRUCTOR -> +Return(New(Call(returnType.toConstructor(), call)))

            RETURN_REFERENCE -> +Return(call.addressOf)

            RETURN -> +Return(call)

            // The wrapper returns the underlying integer; cast the enum result to
            // it (`(int)(Cls::method(...))`) since a scoped enum won't convert
            // implicitly.
            ENUM_RETURN -> +Return(
                RawCast((returnType as ResolvedCppType).cType.typeString, call)
            )
        }
    }

    // A user-defined conversion operator to a CLASS/STRUCT target (`operator nn::Tok`) is
    // emitted as a plain method call `thiz->operator <Target>()`. The method's spelling
    // (`method.name`) carries the target type UNqualified (`operator Tok`), which fails to
    // compile outside the type's namespace ("unknown type name 'Tok'; did you mean
    // 'nn::Tok'?"). Re-spell the call's target with the fully-qualified type taken from the
    // method's resolved return type (the same qualified spelling the Holder placement-new
    // uses), so the call is `thiz->operator nn::Tok()`. Non-conversion methods (and scalar
    // conversions, which take the `(target)*self` cast path above) are returned unchanged.
    private fun conversionCallName(method: ResolvedMethod): String {
        val prefix = "operator "
        if (!method.name.startsWith(prefix)) return method.name
        // Distinguish a conversion operator (`operator <Type>`, spelled WITH a trailing
        // space then a type identifier) from symbol operators (`operator==`, `operator[]`,
        // `operator()` — no space). `operator new`/`operator delete` are filtered earlier.
        // Only a target that begins with an identifier char is a type to re-qualify.
        val target = method.name.substring(prefix.length)
        if (target.firstOrNull()?.let { it.isLetter() || it == '_' || it == ':' } != true) {
            return method.name
        }
        return prefix + method.returnType.toConstructor()
    }

    private fun ResolvedType.toConstructor(): String = toString().trimEnd('*').let {
        if (it.startsWith("const ")) {
            it.substring("const ".length)
        } else {
            it
        }
    }

    private fun CppCodeBuilder.createPointedStringReturn(call: Symbol) =
        createStringReturn(call, byPointer = true)

    // Build the `const char*` boundary return from a (by-value or by-pointer) std::string.
    // [byPointer] selects the member-access form: `->`/`->length()` for a `std::string*`
    // ([ResolvedType.PSTRING]) source, `.`/`.length()` for a by-value `std::string`
    // ([ResolvedType.STRING]). The emitted C++ must stay byte-identical for either form.
    private fun CppCodeBuilder.createStringReturn(call: Symbol, byPointer: Boolean = false) {
        val sourceType = if (byPointer) ResolvedType.PSTRING else ResolvedType.STRING
        // The raw `.length()` / `->length()` access on the source local, as a C++ string.
        val lengthAccess = "${if (byPointer) "->" else "."}length()"
        val access: Symbol.(Symbol) -> Symbol = if (byPointer) Symbol::arrow else Symbol::dot
        val returnStr = +define(
            "ret_value",
            sourceType,
            initializer = call
        )
        val returnArray = +define(
            "ret_value_cast",
            ResolvedType.CSTRING,
            initializer = New(Raw("char[${returnStr.name}$lengthAccess + 1]"))
        )
        +(
            returnStr.reference.access(
                Call(
                    "copy",
                    returnArray.reference,
                    returnStr.reference.access(Call("length")),
                    Raw("0")
                )
            )
            )
        // std::basic_string::copy does NOT NUL-terminate — the `+ 1` slot is allocated but
        // never written, so toKString() (which scans to the first '\0') reads a garbage byte
        // past the string (the `Point` -> `PointU` corruption). Terminate it explicitly.
        +Raw("${returnArray.name}[${returnStr.name}$lengthAccess] = '\\0'")
        +Return(returnArray.reference)
    }

    private fun CppCodeBuilder.createStringCast(arg: SignatureArgument): SignatureArgument =
        arg.copy(
            localVar = +define(
                arg.localVar.name + "_cast",
                arg.targetType,
                initializer = Call("std::string", arg.localVar.reference)
            )
        )

    // T1.10p: construct an inbound non-owning string view from the `const char*` boundary
    // argument — `llvm::StringRef arg_cast = llvm::StringRef(arg)`. targetType is the view
    // type (its C boundary is a `char*`, its Kotlin type a String), so the local var is the
    // view and is passed through to the call. The inbound inverse of the STRING return path.
    private fun CppCodeBuilder.createViewCast(arg: SignatureArgument): SignatureArgument = arg.copy(
        localVar = +define(
            arg.localVar.name + "_cast",
            arg.targetType,
            initializer = Call(arg.targetType.typeString, arg.localVar.reference)
        )
    )

    private fun CppCodeBuilder.createCast(arg: SignatureArgument): SignatureArgument = arg.copy(
        localVar = +define(
            arg.localVar.name + "_cast",
            arg.targetType,
            reinterpret(arg.localVar, arg.targetType)
        )
    )

    private fun CppCodeBuilder.createRawCast(arg: SignatureArgument): SignatureArgument = arg.copy(
        localVar = +define(
            arg.localVar.name + "_cast",
            arg.targetType,
            RawCast(arg.targetType.toString(), arg.localVar.reference)
        )
    )

    private fun CppCodeBuilder.reinterpret(arg: LocalVar, type: ResolvedType): Symbol {
        val type = type(type)
        val reference = arg.reference
        return object : Symbol {
            override fun build(builder: CodeStringBuilder) {
                builder.append("reinterpret_cast<")
                type.build(builder)
                builder.append(">(")
                reference.build(builder)
                builder.append(")")
            }
        }
    }

    private fun CppCodeBuilder.generateFieldGetBody(
        cls: ResolvedClass,
        field: ResolvedField,
        args: List<SignatureArgument>
    ) {
        val thizCast = generateArgumentCast(args[0])
        val fetch = thizCast.pointerReference arrow Raw(field.name)
        generateReturn(
            fetch,
            field.getter.returnStyle,
            field.getter.returnType,
            args.getOrNull(1)?.let { generateArgumentCast(it) }
        )
    }

    private fun CppCodeBuilder.generateFieldSetBody(
        cls: ResolvedClass,
        field: ResolvedField,
        args: List<SignatureArgument>
    ) {
        val thizCast = generateArgumentCast(args[0])
        val fetch = thizCast.pointerReference arrow Raw(field.name)
        val argumentCast = generateArgumentCast(args[1])
        +(fetch assign argumentCast.reference)
    }
}

private fun String.removeTemplate(): String {
    if (!contains("<")) return this
    return substring(0, indexOf('<'))
}
