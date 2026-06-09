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

import com.monkopedia.krapper.BasicMethod
import com.monkopedia.krapper.BasicWithDummyMethod
import com.monkopedia.krapper.InfixMethod
import com.monkopedia.krapper.KotlinOperator
import com.monkopedia.krapper.ResolvedOperator
import com.monkopedia.krapper.generator.builders.Call
import com.monkopedia.krapper.generator.builders.CodeBuilder
import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.CodeGeneratorBase
import com.monkopedia.krapper.generator.builders.Concat
import com.monkopedia.krapper.generator.builders.Defer
import com.monkopedia.krapper.generator.builders.Dot
import com.monkopedia.krapper.generator.builders.EndClass
import com.monkopedia.krapper.generator.builders.FunctionBuilder
import com.monkopedia.krapper.generator.builders.KotlinCodeBuilder
import com.monkopedia.krapper.generator.builders.KotlinFactory
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_OPAQUE_POINTER
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_POINTER
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.MEM_SCOPE
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.STABLE_REF
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.STABLE_REF_CREATE
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.STATIC_C_FUNCTION
import com.monkopedia.krapper.generator.builders.KotlinLocalVar
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.builders.Raw
import com.monkopedia.krapper.generator.builders.Return
import com.monkopedia.krapper.generator.builders.Symbol
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.builders.appendLine
import com.monkopedia.krapper.generator.builders.asserting
import com.monkopedia.krapper.generator.builders.block
import com.monkopedia.krapper.generator.builders.cls
import com.monkopedia.krapper.generator.builders.comment
import com.monkopedia.krapper.generator.builders.companion
import com.monkopedia.krapper.generator.builders.defer
import com.monkopedia.krapper.generator.builders.define
import com.monkopedia.krapper.generator.builders.dot
import com.monkopedia.krapper.generator.builders.elvis
import com.monkopedia.krapper.generator.builders.escapeKotlinKeyword
import com.monkopedia.krapper.generator.builders.extensionFunction
import com.monkopedia.krapper.generator.builders.extensionMethod
import com.monkopedia.krapper.generator.builders.fqType
import com.monkopedia.krapper.generator.builders.function
import com.monkopedia.krapper.generator.builders.getter
import com.monkopedia.krapper.generator.builders.importBlock
import com.monkopedia.krapper.generator.builders.infix
import com.monkopedia.krapper.generator.builders.inline
import com.monkopedia.krapper.generator.builders.isVal
import com.monkopedia.krapper.generator.builders.lambda
import com.monkopedia.krapper.generator.builders.operator
import com.monkopedia.krapper.generator.builders.override
import com.monkopedia.krapper.generator.builders.pkg
import com.monkopedia.krapper.generator.builders.property
import com.monkopedia.krapper.generator.builders.qdot
import com.monkopedia.krapper.generator.builders.reference
import com.monkopedia.krapper.generator.builders.setter
import com.monkopedia.krapper.generator.builders.symbol
import com.monkopedia.krapper.generator.builders.type
import com.monkopedia.krapper.generator.model.rootPackageOverride
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.DIRECT
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.STACK
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.ALIGN_OF
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.CONSTRUCTOR
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.DESTRUCTOR
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.METHOD
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.SIZE_OF
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.STATIC
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.STATIC_OP
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedArgument
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedConstructor
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedDestructor
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedField
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.ARG_CAST
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.STRING
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle.STRING_POINTER
import com.monkopedia.krapper.generator.resolvedmodel.recursiveSequence
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedType
import com.monkopedia.krapper.generator.resolvedmodel.type.fullyQualifiedType
import com.monkopedia.krapper.generator.resolvedmodel.type.nullable
import com.monkopedia.krapper.generator.resolvedmodel.type.typedWith

// Recognizers for the renderable subset of C++ default-argument literals (see
// KotlinWriter.renderKotlinDefault). Suffixes (u/U/l/L/f/F) are stripped before matching.
private val INT_LITERAL = Regex("[+-]?(0[xX][0-9a-fA-F]+|[0-9]+)")
private val FLOAT_LITERAL = Regex("[+-]?([0-9]*\\.[0-9]+|[0-9]+\\.?[0-9]*)([eE][+-]?[0-9]+)?")

// A strictly Kotlin-valid float/double literal (digit on both sides of any dot) — used to
// final-check a normalized C++ float default before emitting it as a Kotlin default.
private val KOTLIN_FLOAT_LITERAL = Regex("[+-]?[0-9]+(\\.[0-9]+)?([eE][+-]?[0-9]+)?")

class KotlinWriter(private val pkg: String, policy: CodeGenerationPolicy = ThrowPolicy) :
    CodeGeneratorBase<KotlinCodeBuilder>(policy) {
    private var currentClasses = mapOf<String, ResolvedClass>()

    // Fully-qualified Kotlin names of every generated binding, for O(1) "is this a binding
    // we emitted?" checks (currentClasses is keyed by C++ type string, not the Kotlin name).
    private var currentKotlinFqTypes = setOf<String>()

    // Per user-template-base, the generic method surface to put on `interface Base<T..>`
    // (and which concrete methods must therefore carry `override`). Computed once in
    // generate() from the concrete instantiations; see computeTemplateInterfaces.
    private var templateInterfaces = mapOf<String, List<TemplateInterfaceMethod>>()

    // Single-public-inheritance interface-per-base (IH-upcast / IH-virtual-dispatch):
    // every generated class that is used as a base gets an `interface <Name>Api`
    // carrying its `ptr` + virtual method surface, which the base's own binding and
    // every derived binding implement. Base-class-typed params/returns then map to the
    // interface so a derived can be passed/returned where the base is wanted (upcast),
    // and a virtual call through the interface vtable-dispatches to the real override.
    // Both maps are keyed by the base's C++ type string (matching currentClasses keys).
    private var baseInterfaces = mapOf<String, BaseInterface>()

    // FQ Kotlin name (e.g. "root.Shape") of every base-class binding -> its interface
    // FQ name ("root.ShapeApi"), for remapping a base-typed method position to the
    // interface. Derived from baseInterfaces; built once in generate().
    private var baseInterfaceByFqType = mapOf<String, BaseInterface>()
    private var needsCCaller = false
    private val staticRouterPkg = "krapper.static"
    private val staticRouterName = "router"
    private val staticRouter = "$staticRouterPkg.$staticRouterName"

    fun generate(outputDir: File, classes: List<ResolvedElement>) {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        for (file in outputDir.listFiles()) {
            file.delete()
        }
        needsCCaller = false
        currentClasses =
            classes.filterIsInstance<ResolvedClass>().associateBy { it.type.toString() }
        currentKotlinFqTypes =
            currentClasses.values.mapTo(mutableSetOf()) { it.type.kotlinType.fullyQualified }
        templateInterfaces = computeTemplateInterfaces()
        baseInterfaces = computeBaseInterfaces()
        baseInterfaceByFqType = baseInterfaces.values.associateBy { it.baseFqType }
        for (cls in currentClasses.values) {
            val clsFile =
                File(outputDir, cls.type.kotlinType.fullyQualified.replace(".", "_") + ".kt")
            val builder = KotlinCodeBuilder()
            builder.generate(cls)
            clsFile.writeText(builder.toString())
        }
        val methodsByPkg = classes.filterIsInstance<ResolvedMethod>().groupBy { it.qualified }
        for ((qualified, methods) in methodsByPkg) {
            val clsFile = File(outputDir, "${qualified.replace("::", "_")}_Functions.kt")
            val builder = KotlinCodeBuilder()
            val rawPkg = qualified.split("::").joinToString(".") { it.decapitalize() }
            // Root the free-functions container's own package the same way the type
            // bindings are rooted (it bypasses fullyQualifiedType, so apply the override
            // here). Override null -> rawPkg as-is; empty namespace under a root -> the root.
            val pkg = rootPackageOverride?.let { if (rawPkg.isEmpty()) it else "$it.$rawPkg" }
                ?: rawPkg
            builder.pkg(pkg)
            builder.importBlock(pkg, builder)
            builder.comment("BEGIN KRAPPER GEN for $pkg Functions")
            for (method in methods) {
                builder.onGenerate(method)
            }
            builder.comment("END KRAPPER GEN for $pkg Functions")
            clsFile.writeText(builder.toString())
        }
        generateEnums(outputDir, classes)
        generateTemplateFacades(outputDir, classes)
        generateBaseInterfaces(outputDir)
        if (needsCCaller) {
            // kotlinx.cinterop.asStableRef
            // kotlinx.cinterop.staticCFunction
            // val method = staticCFunction { arg1: COpaquePointer, arg2: COpaquePointer ->
            //    val callback = arg1.asStableRef<(COpaquePointer) -> Unit>().get()
            //    callback(arg2)
            // }
            val clsFile = File(outputDir, "_Krapper_Static_Router.kt")
            val builder = KotlinCodeBuilder().apply {
                pkg(staticRouterPkg)
                importBlock(staticRouterPkg, this)
                comment("BEGIN KRAPPER GEN for static C function Router")

                +define(
                    staticRouterName,
                    initializer = lambda {
                        type = type(fullyQualifiedType(STATIC_C_FUNCTION))
                        val arg1 = define("arg1", nullable(fullyQualifiedType(C_OPAQUE_POINTER)))
                        val arg2 = define("arg2", fullyQualifiedType(C_OPAQUE_POINTER))
                        body {
                            val callback = +define(
                                "callback",
                                initializer = arg2.reference dot Call(
                                    extensionMethod(STABLE_REF),
                                    templateArgs = listOf(Raw("(COpaquePointer?) -> Unit"))
                                ) dot Call(Raw("get"))
                            )
                            +Call(callback.reference, arg1.reference)
                        }
                    }
                )

                comment("END KRAPPER GEN for static C function Router")
            }

            clsFile.writeText(builder.toString())
        }
    }

    /**
     * Emit a Kotlin `enum class` file for every distinct enum surfaced by the
     * generated bindings. Enums aren't lifted into the model as elements; they
     * only appear as the [ResolvedKotlinType] of a method/field/arg, so we walk
     * those, dedupe by fully-qualified name, and synthesize the declaration.
     */
    private fun generateEnums(outputDir: File, classes: List<ResolvedElement>) {
        val enums = LinkedHashMap<String, ResolvedKotlinType>()
        fun consider(type: ResolvedKotlinType?) {
            if (type != null && type.isEnum) {
                enums.getOrPut(type.fullyQualified) { type }
            }
        }
        classes.recursiveSequence().forEach { element ->
            when (element) {
                is ResolvedMethod -> {
                    consider(element.returnType.kotlinType)
                    element.args.forEach { consider(it.type.kotlinType) }
                }

                is ResolvedField -> consider(element.kotlinType)

                else -> {}
            }
        }
        for (enum in enums.values) {
            val clsFile = File(outputDir, enum.fullyQualified.replace(".", "_") + ".kt")
            clsFile.writeText(renderEnum(enum))
        }
    }

    // A plain string builder rather than KotlinCodeBuilder: the enum-class body is
    // fixed boilerplate with no AST nodes to model.
    private fun renderEnum(enum: ResolvedKotlinType): String {
        val underlying = enum.enumUnderlying?.name ?: "Int"
        // The enum's DECLARATION name must be the bare class name — `enum.name` carries the
        // nullable `?` of the type's default use-position (it's the same ResolvedKotlinType
        // a nullable field/return uses), which is illegal in a class declaration
        // (`enum class TranslationUnitKind?`). Type USAGES keep the `?`; the declaration trims.
        val name = enum.name.trimEnd('?')
        val entries = enum.enumEntries.joinToString(", ") { entry ->
            "${entry.name}(${literalForUnderlying(entry.value, underlying)})"
        }
        return buildString {
            if (enum.pkg.isNotEmpty()) {
                appendLine("package ${enum.pkg}")
                appendLine()
            }
            appendLine("// BEGIN KRAPPER GEN for enum $name")
            appendLine("enum class $name(val value: $underlying) {")
            appendLine("    $entries;")
            appendLine()
            appendLine("    companion object {")
            // `firstOrNull`, not `first`: an out-of-range integer (a value from a newer
            // library ABI, or an OR-combined flag/bitmask value that no single entry equals)
            // must NOT throw `NoSuchElementException` at runtime — it falls back to the first
            // entry (typically the 0/default). This does not faithfully represent a bitmask
            // combination; flag-enum modelling is a separate concern. (A nullable `fromValue`
            // returning null on unknown would be the alternative, but only once enum return
            // positions can be made nullable uniformly.)
            appendLine(
                "        fun fromValue(v: $underlying): $name = " +
                    "entries.firstOrNull { it.value == v } ?: entries.first()"
            )
            appendLine("    }")
            appendLine("}")
            appendLine("// END KRAPPER GEN for enum $name")
        }
    }

    // Render an enum constant's integer literal (stored as a signed Long) in its
    // underlying Kotlin type. UInt/ULong have literal suffixes; UShort/UByte have
    // none (a `u` suffix would be a UInt, which won't match the property type), so
    // build them via a cast.
    private fun literalForUnderlying(value: Long, underlying: String): String = when (underlying) {
        "UInt" -> "${value.toUInt()}u"
        "ULong" -> "${value.toULong()}uL"
        "Long" -> "${value}L"
        "UShort" -> "${value.toInt() and 0xFFFF}.toUShort()"
        "UByte" -> "${value.toInt() and 0xFF}.toUByte()"
        else -> value.toString()
    }

    override fun KotlinCodeBuilder.onGenerate(
        cls: ResolvedClass,
        handleChildren: KotlinCodeBuilder.() -> Unit
    ) {
        val type = cls.type.kotlinType
        val pkg = type.pkg
        pkg(pkg)
        // The base interface(s) this class implements (own + ancestors); computed once.
        val implementedInterfaces = baseInterfacesFor(cls)

        importBlock(pkg, this)
        comment("BEGIN KRAPPER GEN for ${cls.type}")
        // Abstract class (a pure-virtual member): no construction factory is emitted
        // (its constructors are dropped in WrappedClass.resolve), so the wrapper can
        // only be obtained by upcast from a concrete subtype, never constructed here.
        // The comment makes that absence discoverable to a reader of the binding.
        if (cls.isAbstract) {
            val plainName = type.name.trimEnd('?')
            comment(
                "Abstract C++ class: no constructor factory is generated. Obtain a " +
                    "$plainName via a concrete subclass (upcast to ${plainName}Api)."
            )
        }
        // Decision B diagnostic: a polymorphic class (it is used as a base and/or has
        // any virtual member) whose destructor is non-virtual is unsafe to delete
        // through a base pointer in C++ (slicing/leak — the derived `~Derived` won't
        // run). The binding's dispose runs an explicit `~Static-type()`, so this is
        // only a hazard when a derived is held via a base wrapper. Surface it as a
        // generated-code comment; non-fatal.
        if (hasNonVirtualDestructorHazard(cls)) {
            val plainName = type.name.trimEnd('?')
            comment(
                "WARNING: polymorphic class with a non-virtual destructor — deleting a " +
                    "$plainName through a base pointer is undefined in C++ (the derived " +
                    "destructor will not run). Give the base a `virtual ~...()` to fix."
            )
        }
        appendLine()
        val ptr = define(ptr.content, fullyQualifiedType(C_OPAQUE_POINTER))
        val memScope = define(memScope.content, fullyQualifiedType(MEM_SCOPE))
        // Emit @krapper.CppBinding("<cppSpec>") on every generated class so the
        // kplusplus compiler plugin can recover the C++ instantiation spec from
        // a Kotlin element type. The spec is the qualified C++ type of the class.
        +Raw("@krapper.CppBinding(\"${cls.type}\")")
        // If this class is a template instantiation (e.g. `Box<int>` -> `Box__Int`),
        // make it implement the generated generic interface (`: Box<Int>`) so the
        // same-named scoped factory `MemScope.Box(): Box<T>` can return it. The
        // interface + facade are emitted once per template base in generate().
        val superTypes = buildList {
            templateInstantiationFacade(cls)?.let { facade ->
                add("${facade.base}<${facade.kotlinArgs.joinToString(", ")}>")
            }
            // Comparable<Self> is needed for sorted()/maxOrNull() etc. — a bare
            // compareTo only gives `<`/`>`. (Composes with a template supertype above.)
            if (cls.children.any { it is ResolvedMethod && it.operator == ResolvedOperator.LT }) {
                add("kotlin.Comparable<${type.name.trimEnd('?')}>")
            }
            // Iterable<Elem> for an index-accessible container (std::vector): `for (x in
            // v)` needs only the synthesized `iterator()` (emitted in onGenerateMethods),
            // but the stdlib `map`/`toList`/`sumOf`/… extensions are defined on Iterable,
            // so this supertype is what unlocks them. Elem = the index get's return type.
            detectIndexIterable(cls)?.let { iterable ->
                add("kotlin.collections.Iterable<${iterable.elemFq}>")
            }
            // Single-public-inheritance interface(s): a class used as a base, and every
            // derived, implements `<Base>Api`. This is the Kotlin subtype edge that lets a
            // derived be passed where the base is wanted (the base-typed params/returns are
            // remapped to the interface) and dispatches virtual calls through the base.
            addAll(implementedInterfaces)
        }
        // When the class implements a base interface, its `ptr` must `override` the
        // interface's `val ptr`. (`memScope` is not on the interface, so it stays plain.)
        val ptrProp =
            if (implementedInterfaces.isNotEmpty()) override(property(ptr)) else property(ptr)
        val superType = superTypes.ifEmpty { null }?.let { Raw(it.joinToString(", ")) }
        cls(named(type), listOf(ptrProp, property(memScope)), superType) {
            handleSuperClassesRecursive(cls)
            handleChildren()
            generateDisposeMethods(cls)
            generateCastMethods(cls)
            generateDownCastMethods(cls)
        }
        appendLine()
        comment("END KRAPPER GEN for ${cls.type}")
        appendLine()
        appendLine()
    }

    // Kotlin names of the class's own member methods — used to detect when a synthesized member
    // (dispose/owned, up-/down-cast) would clash with a real one.
    private fun memberMethodNames(cls: ResolvedClass): Set<String> =
        cls.children.filterIsInstance<ResolvedMethod>()
            .filter { it.methodType == MethodType.METHOD }
            .mapTo(mutableSetOf()) { fixNaming(it).name }

    // Cast-generator name-clash guard: if `name` is already a member, emit the NOTE and skip.
    private fun KotlinCodeBuilder.skipIfNameClash(name: String, existing: Set<String>): Boolean {
        if (name !in existing) return false
        comment(
            "NOTE: $name() not generated — this class already declares a member with that name."
        )
        return true
    }

    // Caller-controlled destruction (matrix: OB-return-ptr-owned / IH-destruction). Every
    // `T*`/`T&` return is a NON-owning wrapper, so the caller decides if/when to delete it.
    // For any class with a destructor we surface the dispose C wrapper (`Type_dispose`, the
    // same one the `_Holder` factory's `defer` calls) as two members:
    //   * `dispose()` — delete now, explicitly.
    //   * `owned()`   — register `memScope.defer { Type_dispose(ptr) }` so the object is
    //                   deleted at scope end, and return `this` for chaining
    //                   (`makeFoo().owned()`).
    // Emitted for abstract classes too: `Type_dispose` runs `~Type()`, which is virtual-aware,
    // so disposing a base wrapper over a derived correctly runs the derived destructor.
    private fun KotlinCodeBuilder.generateDisposeMethods(cls: ResolvedClass) {
        val destructor = cls.children.filterIsInstance<ResolvedDestructor>().firstOrNull() ?: return
        // Rare collision: a C++ class that already declares a `dispose`/`owned` method would
        // clash with these synthesized members. Skip emission in that case (the existing
        // method keeps the name); the dispose C wrapper is still reachable via the Holder.
        val existingNames = memberMethodNames(cls)
        val hasNameClash = "dispose" in existingNames || "owned" in existingNames
        if (hasNameClash) {
            comment(
                "NOTE: dispose()/owned() not generated — this class already declares a " +
                    "member named `dispose` or `owned`."
            )
            return
        }
        // No dispose C wrapper was emitted for this destructor (no uniqueCName) — there's
        // nothing to call, so don't surface dispose()/owned(). (Mirrors "skip if no dispose
        // C fn exists".)
        val disposeName = destructor.uniqueCName ?: return

        // A fresh Call each time: a Symbol instance may appear only once in the emitted
        // symbol tree (the FQ-import walker rejects a re-used node), so dispose() and owned()
        // each get their own `Type_dispose(ptr)` call.
        fun disposeCall() = Call(extensionMethod(pkg, disposeName), ptr)
        inline {
            function {
                name = "dispose"
                retType = type(ResolvedType.UNIT.copy())
                body {
                    +disposeCall()
                }
            }
        }
        inline {
            function {
                name = "owned"
                retType = type(cls.type)
                body {
                    // `memScope.defer { Type_dispose(ptr) }` — same mechanism as the _Holder
                    // factory, but deferred onto the wrapper's own `memScope` at the caller's
                    // request rather than at construction.
                    block(Dot(memScope, Defer), EndClass) {
                        +disposeCall()
                    }
                    +Return(Raw("this"))
                }
            }
        }
    }

    // Uniform offset-correct upcast methods (IH-multi-inherit). For every (transitive)
    // public, non-virtual base B of D, emit
    //   fun asB(): B = B(D_as_B(ptr), memScope)
    // returning a NON-owning wrapper over the same object viewed as its base subobject.
    // The `D_as_B` C helper does `static_cast<B*>(reinterpret_cast<D*>(p))`, which applies
    // B's (possibly non-zero) subobject offset — correct for a 2nd base and for a single
    // base pushed off offset 0 by a vtable-adding derived, where the implicit interface
    // upcast's plain reinterpret_cast would read garbage. Complements (does not replace)
    // the offset-0 interface-per-base upcast in handleSuperClassesRecursive.
    private fun KotlinCodeBuilder.generateCastMethods(cls: ResolvedClass) {
        val existingNames = memberMethodNames(cls)
        for (target in castTargetsFor(cls) { currentClasses[it] }) {
            // Name-clash guard (mirrors generateDisposeMethods): if D already declares a
            // member named `as<Base>`, skip it — the existing method keeps the name.
            if (skipIfNameClash(target.kotlinMethodName, existingNames)) continue
            inline {
                function {
                    name = target.kotlinMethodName
                    retType = type(target.base.type)
                    body {
                        +Return(
                            generateConstructorCall(
                                target.base.type.kotlinType,
                                asserting(Call(extensionMethod(pkg, target.cHelperName), ptr)),
                                memScope
                            )
                        )
                    }
                }
            }
        }
    }

    // Checked DOWN-cast methods (T1.2 dyn_cast). For base D's binding, every bound class
    // reaching it as a (transitive) base B gets the NULLABLE
    //   fun asB(): B? {
    //       val raw = D_dyncast_B(ptr) ?: return null   // C shim: dyn_cast / dynamic_cast
    //       return B(raw, memScope)                      // BORROWED, non-owning view
    //   }
    // The result is the SAME object viewed as the derived type, so (like the up-cast) it is
    // a non-owning wrapper — no dispose. `?: return null` propagates the shim's null when the
    // runtime object isn't actually a B. The C shim's mechanism (LLVM `dyn_cast` for a
    // `classof`-bearing type, else `dynamic_cast`) is invisible here. Name-clash-guarded
    // identically to the up-cast and dispose methods.
    private fun KotlinCodeBuilder.generateDownCastMethods(cls: ResolvedClass) {
        val existingNames = memberMethodNames(cls)
        val allClasses = currentClasses.values.toList()
        for (target in downCastTargetsFor(cls, allClasses) { currentClasses[it] }) {
            if (skipIfNameClash(target.kotlinMethodName, existingNames)) continue
            inline {
                function {
                    name = target.kotlinMethodName
                    retType = type(nullable(target.derived.type.kotlinType))
                    body {
                        // `val raw = shim(ptr) ?: return null` — the elvis-return narrows the
                        // shim's nullable `COpaquePointer?` to a non-null `COpaquePointer`, so
                        // the local is declared non-nullable and feeds the constructor (which
                        // wants a non-null pointer) directly.
                        val raw = +define(
                            "raw",
                            fullyQualifiedType(C_OPAQUE_POINTER),
                            initializer = Call(extensionMethod(pkg, target.cHelperName), ptr)
                                elvis Return(Raw("null"))
                        )
                        raw.isVal = true
                        +Return(
                            generateConstructorCall(
                                target.derived.type.kotlinType,
                                raw.reference,
                                memScope
                            )
                        )
                    }
                }
            }
        }
    }

    // Single (one-base) public-inheritance flattening (decision A): re-emit a derived
    // class's inherited public methods + fields onto its binding so `derived.move(...)`
    // / base fields work with no downcast (derived IS-A base at offset 0, so the base
    // member's wrapper runs on the derived `ptr` unchanged). Naming details — bare name
    // owned by the base-most decl, virtual overrides emitted once, non-virtual shadows
    // class-prefixed — are inline below and in `shadowsInheritedNonVirtual`. See the
    // matrix "Inheritance & virtual dispatch" section.
    private fun KotlinCodeBuilder.handleSuperClassesRecursive(cls: ResolvedClass) {
        if (cls.baseClass == null) return
        // Methods of the leaf class only — used to detect an inherited method that the
        // leaf virtually overrides. NOTE: this is just `cls`'s own methods; a deeper
        // chain whose *intermediate* class overrides would need walking intermediates
        // (not currently needed — flatten is single-base depth).
        val descendantMethods = descendantInstanceMethods(cls)
        var superClass: ResolvedClass = currentClasses[cls.baseClass.toString()]
            ?: error("Can't find specified base class ${cls.baseClass}")
        // Method signatures already emitted from a nearer ancestor; a method
        // re-declared higher up the chain is skipped so it isn't double-emitted.
        val claimedSignatures = mutableSetOf<String>()
        while (true) {
            superClass.children.filterIsInstance<ResolvedMethod>().filter {
                it.methodType == MethodType.STATIC_OP || it.methodType == MethodType.METHOD
            }.forEach { method ->
                val sig = methodSignature(method)
                if (!claimedSignatures.add(sig)) return@forEach
                // A virtual method overridden by the leaf is one logical method:
                // emit only the leaf's own override (keeps the bare name).
                val overridden = method.isVirtual && descendantMethods.any {
                    it.isVirtual && methodSignature(it) == sig
                }
                if (overridden) return@forEach
                try {
                    onGenerate(superClass, method)
                } catch (t: Throwable) {
                    codeGenerationPolicy.onGenerateMethodFailed(cls, method, t)
                }
            }
            for (field in superClass.children.filterIsInstance<ResolvedField>()) {
                try {
                    onGenerate(superClass, field)
                } catch (t: Throwable) {
                    codeGenerationPolicy.onGenerateFieldFailed(cls, field, t)
                }
            }
            superClass = superClass.baseClass?.let { currentClasses[it.toString()] } ?: break
        }
    }

    // The leaf class's own instance (METHOD/STATIC_OP) methods — the candidates for
    // overriding/shadowing an inherited base method. (Leaf only; see the note in
    // handleSuperClassesRecursive about deeper chains.)
    private fun descendantInstanceMethods(cls: ResolvedClass): List<ResolvedMethod> =
        cls.children.filterIsInstance<ResolvedMethod>().filter {
            it.methodType == MethodType.METHOD || it.methodType == MethodType.STATIC_OP
        }

    // True when [cls] is polymorphic (it is used as a base OR declares a virtual
    // member) yet no destructor in its inheritance chain is virtual — making it
    // unsafe to `delete` through a base pointer in C++ (the derived destructor won't
    // run). Drives a non-fatal generated-code diagnostic.
    //
    // Destructor virtuality is inherited, so a class that relies on a base's
    // `virtual ~Base()` is safe — hence the walk up baseClass. A class with no
    // explicit destructor has an implicit non-virtual one, so the per-class check is
    // `none virtual`, not `any present`.
    //
    // Abstract classes are treated as a safe link (and never flagged themselves):
    // they are designated polymorphic bases, are never `delete`d directly through the
    // binding (no factory is emitted for them), and their destructor is frequently
    // declared out-of-line — which the model doesn't currently surface — so flagging
    // them would be noise. The hazard we reliably catch is a CONCRETE polymorphic
    // class whose chain has no virtual destructor (e.g. a `virtual` method + an
    // implicit dtor, or a non-virtual base).
    private fun hasNonVirtualDestructorHazard(cls: ResolvedClass): Boolean {
        if (cls.isAbstract) return false
        val isUsedAsBase = baseInterfaces.containsKey(cls.type.toString())
        val hasVirtualMember = cls.children.filterIsInstance<ResolvedMethod>().any { it.isVirtual }
        if (!isUsedAsBase && !hasVirtualMember) return false
        // Safe if the class or any (bound) ancestor declares a virtual destructor, or
        // if any ancestor is abstract (a designated polymorphic base, presumed to own
        // a virtual dtor). Destructor virtuality propagates down the hierarchy.
        var base: ResolvedClass? = cls
        while (base != null) {
            if (base.isAbstract) return false
            val hasVirtualDtor = base.children.filterIsInstance<ResolvedMethod>().any {
                it.methodType == MethodType.DESTRUCTOR && it.isVirtual
            }
            if (hasVirtualDtor) return false
            base = base.baseClass?.let { currentClasses[it.toString()] }
        }
        return true
    }

    // Name + real-parameter-type identity of a method (receiver pointer dropped),
    // used to match an inherited method against a derived override/shadow.
    private fun methodSignature(method: ResolvedMethod): String =
        "${method.name}(${method.args.drop(1).joinToString(",") { it.type.typeString }})"

    // True when [method], declared directly on [cls], non-virtually shadows a method
    // of the same signature inherited from a (bound) base class. Such a method yields
    // the bare Kotlin name to the inherited member and is class-prefixed instead.
    // Intrinsic to cls + its bases (libclang sees base members regardless of binding).
    private fun shadowsInheritedNonVirtual(cls: ResolvedClass?, method: ResolvedMethod): Boolean {
        if (cls == null || method.isVirtual) return false
        if (method.operator != null) return false
        val sig = methodSignature(method)
        var base = cls.baseClass?.let { currentClasses[it.toString()] }
        while (base != null) {
            val match = base.children.filterIsInstance<ResolvedMethod>().any {
                (it.methodType == MethodType.METHOD || it.methodType == MethodType.STATIC_OP) &&
                    methodSignature(it) == sig
            }
            if (match) return true
            base = base.baseClass?.let { currentClasses[it.toString()] }
        }
        return false
    }

    // CamelCase `<class><Method>` form for a shadowing non-virtual method, e.g.
    // Derived::label -> derivedLabel. Lowercases the class initial so the result is
    // a conventional Kotlin member name.
    private fun classPrefixedName(cls: ResolvedClass, method: ResolvedMethod): String {
        // kotlinType.name can carry a trailing '?' for a nullable wrapper type; strip
        // it so the prefix is a plain identifier.
        val clsName = cls.type.kotlinType.name.trimEnd('?')
        val prefix = clsName.replaceFirstChar { it.lowercase() }
        val suffix = method.name.replaceFirstChar { it.uppercase() }
        return prefix + suffix
    }

    override fun KotlinCodeBuilder.onGenerateMethods(cls: ResolvedClass) {
        val methods = cls.children.filterIsInstance<ResolvedMethod>()
        methods.filter {
            it.methodType == MethodType.STATIC_OP || it.methodType == MethodType.METHOD
        }.forEach { method ->
            try {
                onGenerate(cls, method, null, null)
            } catch (t: Throwable) {
                codeGenerationPolicy.onGenerateMethodFailed(cls, method, t)
            }
        }
        // A class with `operator==` got an `equals` override above; emit the paired
        // `hashCode` override here so the equals/hashCode contract is always honoured.
        if (methods.any { it.operator == ResolvedOperator.EQ }) {
            generateHashCode(cls)
        }
        // Index-accessible containers (detected by size() + index get) get a synthesized
        // `operator fun iterator()` so `for (x in v)` and the full Iterable surface work.
        generateIndexIterator(cls)
        companion {
            val sizeOf = methods.find { (it as? ResolvedMethod)?.methodType == SIZE_OF }!!
            val size = define(
                "size",
                sizeOf.returnType
            )
            +property(size) {
                getter = inline(
                    getter {
                        +Return(Call(extensionMethod(pkg, sizeOf.uniqueCName!!)))
                    }
                )
            }
            val alignOf = methods.find { (it as? ResolvedMethod)?.methodType == ALIGN_OF }!!
            val align = define(
                "align",
                alignOf.returnType
            )
            +property(align) {
                getter = inline(
                    getter {
                        +Return(Call(extensionMethod(pkg, alignOf.uniqueCName!!)))
                    }
                )
            }
            for (
            method in methods.filter {
                it.methodType == MethodType.CONSTRUCTOR || it.methodType == MethodType.STATIC
            }
            ) {
                try {
                    onGenerate(cls, method, size, align)
                } catch (t: Throwable) {
                    codeGenerationPolicy.onGenerateMethodFailed(cls, method, t)
                }
            }
            val defaultConstructor =
                cls.children.filterIsInstance<ResolvedConstructor>()
                    .firstOrNull { it.args.isEmpty() }
            val destructor =
                cls.children.filterIsInstance<ResolvedDestructor>().firstOrNull()
            extensionFunction {
                receiver = fqType(MEM_SCOPE)
                name = cls.type.kotlinType.name.trimEnd('?') + "_Holder"
                retType = type(cls.type)
                body {
                    if (defaultConstructor != null) {
                        +Return(Call(constructorMethod(cls.type.kotlinType)))
                        return@body
                    }
                    val obj = +define(
                        "memory",
                        fullyQualifiedType(C_OPAQUE_POINTER),
                        initializer = (
                            Call(
                                extensionMethod("kotlinx.cinterop", "interpretCPointer"),
                                Call(
                                    "alloc",
                                    size.reference,
                                    align.reference
                                ) dot Raw("rawPtr")
                            ) elvis Call("error", "Allocation failed".symbol)
                            )
                    )
                    obj.isVal = true
                    if (destructor != null) {
                        defer {
                            +Call(
                                extensionMethod(
                                    pkg,
                                    destructor.uniqueCName
                                        ?: error("Unnamed destructor in $cls")
                                ),
                                obj.reference
                            )
                        }
                    }
                    +Return(
                        generateConstructorCall(
                            cls.type.kotlinType,
                            obj.reference,
                            thiz.reference
                        )
                    )
                }
            }
        }
    }

    private fun named(name: ResolvedKotlinType) = Raw(name.name)

    /**
     * Describes the generic interface + scoped factory ("facade") generated for a
     * user template instantiation, mirroring the stdlib `List(n){}` pattern:
     * an `interface Box<T>` (no constructor) plus a same-named scoped factory
     * `fun <T> MemScope.Box(): Box<T>`. `Box<Int>()` then resolves to the FUNCTION,
     * which the kplusplus compiler plugin refines to the concrete `Box__Int`.
     *
     * @param base the C++ template base name, also the Kotlin interface/facade name (e.g. "Box")
     * @param pkg the package the concrete binding (and so the interface) lives in
     * @param arity number of template parameters
     * @param kotlinArgs the Kotlin types for THIS instantiation's args (e.g. ["Int"])
     */
    private data class TemplateFacade(
        val base: String,
        val pkg: String,
        val arity: Int,
        val kotlinArgs: List<String>
    )

    // C++ primitive arg -> Kotlin type; non-primitives handled by cppArgToKotlinType.
    private val cppPrimitiveArgToKotlin = mapOf(
        "int" to "Int",
        "long" to "Long",
        "double" to "Double",
        "float" to "Float"
    )

    /**
     * Returns the Kotlin type name for C++ template arg [cppArg] in a facade
     * supertype (`: Box<Point>`), or null if the arg is neither a supported
     * primitive nor a generated binding. Primitives use the hardcoded table; any
     * other arg must be a class we generated a binding for — we reuse that binding's
     * own Kotlin name (`Point`, or the mangled `Box__Int` for a nested instantiation)
     * rather than re-deriving the mangling here.
     */
    private fun cppArgToKotlinType(cppArg: String): String? {
        val arg = cppArg.trim()
        cppPrimitiveArgToKotlin[arg]?.let { return it }
        // The arg names a user/nested binding only if we actually generated it.
        // currentClasses is keyed by the binding's C++ type string (e.g. "Point",
        // "Box<int>"); [arg] comes from splitting the same normalized type string, so
        // an exact match is reliable (no interior-space variants to reconcile).
        return currentClasses[arg]?.type?.kotlinType?.name
    }

    /**
     * If [cls] is a USER template instantiation (its C++ type is `Base<args...>`
     * with an un-namespaced base — namespaced std:: templates use the hardcoded
     * cppVector/cppMap/cppPair facades instead), return its [TemplateFacade];
     * else null. Returns null if any arg is neither a supported primitive nor a
     * generated binding.
     */
    private fun templateInstantiationFacade(cls: ResolvedClass): TemplateFacade? {
        val cppType = cls.type.toString()
        if (!cppType.contains("<")) return null
        val base = cppType.substringBefore("<")
        if (base.contains("::")) return null
        val argString = cppType.substringAfter("<").substringBeforeLast(">")
        val cppArgs = splitTopLevelArgs(argString)
        val kotlinArgs = cppArgs.map { cppArgToKotlinType(it) ?: return null }
        return TemplateFacade(
            base = base,
            pkg = cls.type.kotlinType.pkg,
            arity = cppArgs.size,
            kotlinArgs = kotlinArgs
        )
    }

    // Split a comma-separated template-arg list at top level only (so a nested
    // `Inner<a, b>` arg stays intact).
    private fun splitTopLevelArgs(args: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        val current = StringBuilder()
        for (c in args) {
            when (c) {
                '<' -> {
                    depth++
                    current.append(c)
                }

                '>' -> {
                    depth--
                    current.append(c)
                }

                ',' -> if (depth == 0) {
                    result.add(current.toString())
                    current.clear()
                } else {
                    current.append(c)
                }

                else -> current.append(c)
            }
        }
        if (current.isNotBlank()) result.add(current.toString())
        return result
    }

    /**
     * One method of a generated `interface Base<T..>`. [renderedParams]/[renderedReturn]
     * are already-rendered Kotlin type strings: a type-param token (`T`/`T1`/`T2`, with a
     * trailing `?` if the position is nullable) where the C++ template param appears, or a
     * fully-qualified concrete Kotlin type elsewhere. [name] + [renderedParams].size (the
     * user-arg count) match a concrete binding's method to this one (so it can be emitted
     * with `override`); [name] already carries overload disambiguation from overloadMethodName.
     */
    private data class TemplateInterfaceMethod(
        val name: String,
        val renderedParams: List<Pair<String, String>>,
        val renderedReturn: String
    )

    /**
     * The fully-qualified Kotlin type (with a trailing `?` when nullable) each template
     * parameter resolves to for THIS instantiation, in param order — e.g. `Box<Point>` ->
     * `["root.Point"]`, `Pair2<int, double>` -> `["kotlin.Int", "kotlin.Double"]`. Returns
     * null if any arg isn't a supported primitive or a generated binding (so the base is
     * skipped). Mirrors templateInstantiationFacade's arg handling but yields FQ names so a
     * concrete method position can be matched against a param by value.
     */
    private fun instantiationParamFqTypes(cls: ResolvedClass): List<String>? {
        val cppType = cls.type.toString()
        if (!cppType.contains("<")) return null
        val argString = cppType.substringAfter("<").substringBeforeLast(">")
        return splitTopLevelArgs(argString).map { cppArg ->
            val arg = cppArg.trim()
            cppPrimitiveArgToKotlin[arg]?.let { return@map "kotlin.$it" }
            currentClasses[arg]?.type?.kotlinType?.fullyQualified ?: return null
        }
    }

    // Fully-qualified Kotlin type of a method position, with a trailing `?` when nullable,
    // so it can be compared by value against a param's FQ type / across instantiations.
    private fun fqOf(type: ResolvedKotlinType): String =
        type.fullyQualified + if (type.isNullable) "?" else ""

    /**
     * Build the generic interface method surface for each user template base, derived from
     * its concrete instantiations (the only place fully-resolved method types exist — the
     * raw template's `T`-typed methods don't survive resolution). A method appears on the
     * interface only when its shape is consistent across EVERY instantiation of the base:
     * for each position (params + return) either it is template-param k in every
     * instantiation (its FQ type equals that instantiation's param-k FQ type), or it is the
     * same concrete FQ type in every instantiation. Anything inconsistent or unmappable
     * omits the whole method — correctness over completeness (a partial interface still
     * lets callers abstract over the methods that are sound).
     */
    private fun computeTemplateInterfaces(): Map<String, List<TemplateInterfaceMethod>> {
        // base -> instantiations (concrete class + its per-param FQ types).
        val byBase = LinkedHashMap<String, MutableList<Pair<ResolvedClass, List<String>>>>()
        for (cls in currentClasses.values) {
            val facade = templateInstantiationFacade(cls) ?: continue
            val paramFqs = instantiationParamFqTypes(cls) ?: continue
            byBase.getOrPut(facade.base) { mutableListOf() }.add(cls to paramFqs)
        }
        val result = LinkedHashMap<String, List<TemplateInterfaceMethod>>()
        for ((base, instantiations) in byBase) {
            val arity = instantiations.first().second.size
            val typeParamToken = { k: Int -> if (arity == 1) "T" else "T${k + 1}" }
            // Plain, non-operator instance methods that exist (by emitted Kotlin name +
            // user-arg count) on EVERY instantiation are the candidates.
            val methodByKey = instantiations.map { (cls, _) ->
                interfaceCandidateMethods(cls).associateBy {
                    overloadMethodName(cls, fixNaming(it)) to (it.args.size - 1)
                }
            }
            val sharedKeys = methodByKey
                .map { it.keys }
                .reduce { acc, keys -> acc.intersect(keys) }
            val methods = mutableListOf<TemplateInterfaceMethod>()
            method@ for (key in sharedKeys) {
                val perInst = instantiations.indices.map { i ->
                    instantiations[i].first to methodByKey[i].getValue(key)
                }
                val argCount = key.second
                // Resolve each position (each user arg, then the return) to a rendered type
                // string, or bail on the whole method if a position isn't consistent.
                val params = mutableListOf<Pair<String, String>>()
                for (argIdx in 1..argCount) {
                    val rendered = renderPosition(instantiations, perInst, typeParamToken) {
                        it.args[argIdx].type.kotlinType
                    } ?: continue@method
                    params += perInst.first().second.args[argIdx].name to rendered
                }
                val returnRendered = renderPosition(instantiations, perInst, typeParamToken) {
                    it.returnType.kotlinType
                } ?: continue@method
                methods += TemplateInterfaceMethod(
                    name = key.first,
                    renderedParams = params,
                    renderedReturn = returnRendered
                )
            }
            result[base] = methods
        }
        return result
    }

    // Candidate methods for the generic interface: plain instance methods (METHOD), no
    // operator, not a constructor. (STATIC_OP and operators are excluded — their Kotlin
    // surface isn't a normal nameable method.)
    private fun interfaceCandidateMethods(cls: ResolvedClass): List<ResolvedMethod> =
        cls.children.filterIsInstance<ResolvedMethod>().filter {
            it !is ResolvedConstructor &&
                it.methodType == MethodType.METHOD &&
                it.operator == null
        }

    /**
     * Render one method position (an arg or the return) for the interface, or null to omit
     * the method. [extract] pulls the position's Kotlin type from a concrete method. The
     * position becomes type-param k if, in every instantiation, its FQ type equals that
     * instantiation's param-k FQ type; otherwise it must be the same concrete FQ type across
     * all instantiations and that type must be renderable (primitive or a generated binding),
     * in which case it renders as that FQ type.
     *
     * Nullability is conservative: instantiationParamFqTypes records each arg's bare FQ name,
     * so a NULLABLE position (e.g. a `T*` arg surfacing as `Point?`) won't match the bare
     * param FQ and is treated as concrete — and if it differs across instantiations, the
     * method is omitted. So by-value template positions become `T`; pointer-typed ones drop
     * out rather than risk a wrong `T` vs `T?`. (`nullable` below is thus only ever set for a
     * concrete-but-nullable param that matched, which doesn't arise for the supported types.)
     */
    private fun renderPosition(
        instantiations: List<Pair<ResolvedClass, List<String>>>,
        perInst: List<Pair<ResolvedClass, ResolvedMethod>>,
        typeParamToken: (Int) -> String,
        extract: (ResolvedMethod) -> ResolvedKotlinType
    ): String? {
        val arity = instantiations.first().second.size
        val fqs = perInst.map { fqOf(extract(it.second)) }
        val nullable = fqs.first().endsWith("?")
        // Try each template-param index: is this position param k in every instantiation?
        for (k in 0 until arity) {
            if (perInst.indices.all { i ->
                    fqs[i] == instantiations[i].second[k]
                }
            ) {
                return typeParamToken(k) + if (nullable) "?" else ""
            }
        }
        // Not a param: must be one identical, renderable concrete type everywhere.
        if (fqs.toSet().size != 1) return null
        val kt = extract(perInst.first().second)
        return if (isRenderableConcreteType(kt)) fqs.first() else null
    }

    // A non-param interface position renders only if its type is a plain value we can name
    // by FQ: a generated binding (isWrapper) or a Kotlin primitive/Unit. Enums, strings, and
    // anything else are omitted (the method is dropped) rather than risk a wrong signature.
    private fun isRenderableConcreteType(type: ResolvedKotlinType): Boolean = when {
        type.isEnum -> false
        type.isWrapper -> type.fullyQualified in currentKotlinFqTypes
        type.fullyQualified.startsWith("kotlin.") -> true
        else -> false
    }

    // True when [method] on [cls] is one the generated `interface <base><T..>` declares, so
    // its concrete emission must carry `override` (and drop `inline`, illegal on an override).
    private fun overridesTemplateInterface(cls: ResolvedClass, method: ResolvedMethod): Boolean {
        val base = templateInstantiationFacade(cls)?.base ?: return false
        val name = overloadMethodName(cls, fixNaming(method))
        val arity = method.args.size - 1
        return templateInterfaces[base]?.any {
            it.name == name && it.renderedParams.size == arity
        } == true
    }

    // Emit one `interface Base<T..> { <methods> }` + `@krapper.CppTemplate(...) fun <T..>
    // MemScope.Base(): Base<T..>` file per distinct user template base present in the
    // generated classes. The interface declares the template's generic method surface
    // (T-preserving; see computeTemplateInterfaces) so callers can abstract over `Base<X>`;
    // the concrete bindings `override` those methods. `Base<Int>()` resolves to the factory
    // and the plugin refines the call to the concrete binding.
    private fun generateTemplateFacades(outputDir: File, classes: List<ResolvedElement>) {
        val facadesByBase = LinkedHashMap<String, TemplateFacade>()
        classes.filterIsInstance<ResolvedClass>().forEach { cls ->
            templateInstantiationFacade(cls)?.let { facade ->
                facadesByBase.getOrPut(facade.base) { facade }
            }
        }
        for (facade in facadesByBase.values) {
            // Single param -> the conventional `T`; multi-param -> `T1, T2, …` (safe
            // and unambiguous for the general case, unlike char-arithmetic `T,U,V`).
            val typeParams = if (facade.arity == 1) {
                "T"
            } else {
                (1..facade.arity).joinToString(", ") { "T$it" }
            }
            val clsFile = File(
                outputDir,
                (if (facade.pkg.isEmpty()) "" else facade.pkg.replace(".", "_") + "_") +
                    facade.base + "_Facade.kt"
            )
            clsFile.writeText(
                buildString {
                    if (facade.pkg.isNotEmpty()) {
                        appendLine("package ${facade.pkg}")
                        appendLine()
                    }
                    appendLine("import kotlinx.cinterop.MemScope")
                    appendLine()
                    appendLine("// BEGIN KRAPPER GEN for template ${facade.base}")
                    // The generic surface: the template's methods with their param/return
                    // types T-preserved (FQ types render inline, so no imports needed). The
                    // concrete bindings `override` these. Has no constructor, so
                    // `${facade.base}<...>()` can only be the same-named factory below.
                    val methods = templateInterfaces[facade.base].orEmpty()
                    if (methods.isEmpty()) {
                        appendLine("interface ${facade.base}<$typeParams>")
                    } else {
                        appendLine("interface ${facade.base}<$typeParams> {")
                        for (m in methods) {
                            val params = m.renderedParams.joinToString(", ") { (n, t) ->
                                "$n: $t"
                            }
                            appendLine("    fun ${m.name}($params): ${m.renderedReturn}")
                        }
                        appendLine("}")
                    }
                    appendLine()
                    appendLine(
                        "@krapper.CppTemplate(base = \"${facade.base}\", " +
                            "prefix = \"${facade.base}\", pkg = \"${facade.pkg}\")"
                    )
                    appendLine(
                        "fun <$typeParams> MemScope.${facade.base}(): " +
                            "${facade.base}<$typeParams> ="
                    )
                    appendLine(
                        "    throw NotImplementedError(" +
                            "\"lowered by the kplusplus compiler plugin\")"
                    )
                    appendLine("// END KRAPPER GEN for template ${facade.base}")
                }
            )
        }
    }

    /**
     * The generated `interface <Name>Api` for a class used as a base in single public
     * inheritance. It carries the base's `ptr` (so a base-typed value can reach the C
     * boundary) plus its virtual instance-method surface; the base's own binding and
     * every derived binding implement it. [methods] are the virtual methods that go on
     * the interface (by their EMITTED Kotlin name, so a concrete method matches by
     * name + user-arg count and is emitted with `override`).
     *
     * @param baseClass the base's ResolvedClass
     * @param interfaceName the Kotlin interface name (`<KotlinName>Api`)
     * @param interfaceFqType the interface's fully-qualified Kotlin name (for remapping)
     * @param baseFqType the base binding's own fully-qualified Kotlin name
     * @param pkg the package the base (and so its interface) lives in
     */
    private data class BaseInterface(
        val baseClass: ResolvedClass,
        val interfaceName: String,
        val interfaceFqType: String,
        val baseFqType: String,
        val pkg: String,
        val methods: List<BaseInterfaceMethod>
    )

    // One method on a base interface: its emitted Kotlin name + the (user) arg count and
    // already-rendered FQ param/return type strings. name + params.size match a concrete
    // method to this one so the concrete emission carries `override`.
    private data class BaseInterfaceMethod(
        val name: String,
        val params: List<Pair<String, String>>,
        val renderedReturn: String
    )

    /**
     * Build the interface-per-base map (single public inheritance). A class is a "base"
     * when some OTHER generated class names it as its (transitive) `baseClass`. For each
     * such base we synthesize `interface <Name>Api` exposing `ptr` + the base's public
     * virtual instance methods. Operators, statics, constructors and non-virtual methods
     * are excluded from the interface surface (the design calls for the virtual surface;
     * non-virtual base members still flatten onto derived bindings as before). A method
     * whose signature can't be rendered as plain FQ types (e.g. a base-typed position we
     * can't yet map, or an enum/string) is dropped from the interface rather than risk a
     * wrong signature — the upcast/dispatch for the renderable methods still holds.
     */
    private fun computeBaseInterfaces(): Map<String, BaseInterface> {
        val baseTypeStrings = LinkedHashSet<String>()
        for (cls in currentClasses.values) {
            var base = cls.baseClass?.toString()
            while (base != null) {
                val baseCls = currentClasses[base] ?: break
                baseTypeStrings.add(base)
                base = baseCls.baseClass?.toString()
            }
        }
        val result = LinkedHashMap<String, BaseInterface>()
        for (typeString in baseTypeStrings) {
            val baseCls = currentClasses[typeString] ?: continue
            val kotlinName = baseCls.type.kotlinType.name.trimEnd('?')
            val interfaceName = kotlinName + "Api"
            val pkg = baseCls.type.kotlinType.pkg
            val interfaceFqType = if (pkg.isEmpty()) interfaceName else "$pkg.$interfaceName"
            val methods = baseInterfaceMethods(baseCls)
            result[typeString] = BaseInterface(
                baseClass = baseCls,
                interfaceName = interfaceName,
                interfaceFqType = interfaceFqType,
                baseFqType = baseCls.type.kotlinType.fullyQualified,
                pkg = pkg,
                methods = methods
            )
        }
        return result
    }

    // The base's virtual instance methods that go on its interface, rendered. A method is
    // included only when every position (params + return) renders as a plain FQ type
    // string (primitive/Unit, a generated binding, or a base-typed position mapped to its
    // interface). Anything else (enum/string/unrenderable) drops the method.
    private fun baseInterfaceMethods(baseCls: ResolvedClass): List<BaseInterfaceMethod> {
        val methods = mutableListOf<BaseInterfaceMethod>()
        val candidates = baseCls.children.filterIsInstance<ResolvedMethod>().filter {
            it !is ResolvedConstructor &&
                it.methodType == MethodType.METHOD &&
                it.operator == null &&
                it.isVirtual
        }
        for (method in candidates) {
            val named = fixNaming(method)
            val ret = renderInterfaceType(method.returnType.kotlinType) ?: continue
            val params = mutableListOf<Pair<String, String>>()
            var ok = true
            for (arg in method.args.drop(1)) {
                val t = renderInterfaceType(arg.type.kotlinType)
                if (t == null) {
                    ok = false
                    break
                }
                params += arg.name to t
            }
            if (!ok) continue
            methods += BaseInterfaceMethod(
                name = overloadMethodName(baseCls, named),
                params = params,
                renderedReturn = ret
            )
        }
        return methods
    }

    // Render a method-position Kotlin type as a plain FQ string for a base interface, or
    // null to drop the method. A base-typed position maps to that base's interface
    // (the upcast enabler); other generated bindings / Kotlin primitives render as their
    // FQ name; enums/strings/unrenderables return null.
    private fun renderInterfaceType(type: ResolvedKotlinType): String? {
        remapBaseToInterfaceFq(type)?.let { return it }
        return when {
            type.isEnum -> null

            type.fullyQualified == "kotlin.String" -> null

            type.isWrapper ->
                if (type.fullyQualified in currentKotlinFqTypes) fqOf(type) else null

            type.fullyQualified.startsWith("kotlin.") -> fqOf(type)

            else -> null
        }
    }

    // If [type] is a wrapper naming a base class, the base interface's FQ name with the
    // position's nullability preserved (`root.Shape?` -> `root.ShapeApi?`); else null.
    private fun remapBaseToInterfaceFq(type: ResolvedKotlinType): String? {
        if (!type.isWrapper) return null
        val bi = baseInterfaceByFqType[type.fullyQualified] ?: return null
        return bi.interfaceFqType + if (type.isNullable) "?" else ""
    }

    // The base interfaces [cls] implements: its own interface (when [cls] is itself a base)
    // AND every (transitive) base class of [cls] that has a generated interface. Walks the
    // single-base chain once; callers take the FQ names (supertypes) or the method surface.
    private fun baseInterfacesImplementedBy(cls: ResolvedClass): List<BaseInterface> {
        val result = LinkedHashMap<String, BaseInterface>()
        baseInterfaces[cls.type.toString()]?.let { result[it.interfaceFqType] = it }
        var base = cls.baseClass?.toString()
        while (base != null) {
            val baseCls = currentClasses[base] ?: break
            baseInterfaces[base]?.let { result[it.interfaceFqType] = it }
            base = baseCls.baseClass?.toString()
        }
        return result.values.toList()
    }

    // The FQ interface names [cls] must implement, to add as supertypes.
    private fun baseInterfacesFor(cls: ResolvedClass): List<String> =
        baseInterfacesImplementedBy(cls).map { it.interfaceFqType }

    // True when [method] on [cls] (own or flattened from a base) matches a method declared
    // on one of the base interfaces [cls] implements, so its concrete emission must carry
    // `override` (and drop `inline`, illegal on an override). Matched by emitted name +
    // user-arg count, mirroring overridesTemplateInterface.
    private fun overridesBaseInterface(cls: ResolvedClass, method: ResolvedMethod): Boolean {
        if (method.operator != null) return false
        val name = overloadMethodName(cls, fixNaming(method))
        val arity = method.args.size - 1
        return baseInterfacesImplementedBy(cls).any { bi ->
            bi.methods.any { it.name == name && it.params.size == arity }
        }
    }

    // Remap a method's return type for emission: a base-typed return becomes the base's
    // interface type (so a derived can be returned where the base is wanted). Non-base
    // types are returned unchanged.
    private fun remapReturnType(returnType: ResolvedCppType): ResolvedCppType {
        val remapped = interfaceKotlinType(returnType.kotlinType) ?: return returnType
        return returnType.copy(kotlinType = remapped)
    }

    // Remap a single argument's type for emission: a base-typed param becomes the base's
    // interface type (the upcast enabler). Non-base args are returned unchanged.
    private fun remapArgType(arg: ResolvedArgument): ResolvedArgument {
        val remapped = interfaceKotlinType(arg.type.kotlinType) ?: return arg
        return arg.copy(type = arg.type.copy(kotlinType = remapped))
    }

    // If [type] is a wrapper naming a base class, a ResolvedKotlinType that RENDERS as the
    // base's interface (same package, name `<Base>Api`, nullability preserved) while
    // remaining `isWrapper` so the `.ptr` boundary handling is unchanged; else null.
    private fun interfaceKotlinType(type: ResolvedKotlinType): ResolvedKotlinType? {
        if (!type.isWrapper) return null
        val bi = baseInterfaceByFqType[type.fullyQualified] ?: return null
        return ResolvedKotlinType(
            fullyQualified = bi.interfaceFqType,
            isWrapper = true,
            isNullable = type.isNullable
        )
    }

    // Emit one `interface <Name>Api { val ptr: COpaquePointer; <virtual methods> }` file
    // per base class. The base's own binding and every derived binding implement it; the
    // interface is what lets a derived be passed/returned where the base is wanted, and a
    // virtual call through it vtable-dispatches to the real override at the C boundary.
    private fun generateBaseInterfaces(outputDir: File) {
        for (bi in baseInterfaces.values) {
            val clsFile = File(
                outputDir,
                (if (bi.pkg.isEmpty()) "" else bi.pkg.replace(".", "_") + "_") +
                    bi.interfaceName + ".kt"
            )
            clsFile.writeText(
                buildString {
                    if (bi.pkg.isNotEmpty()) {
                        appendLine("package ${bi.pkg}")
                        appendLine()
                    }
                    appendLine("import kotlinx.cinterop.COpaquePointer")
                    appendLine()
                    appendLine("// BEGIN KRAPPER GEN for base interface ${bi.interfaceName}")
                    appendLine("interface ${bi.interfaceName} {")
                    // The backing pointer: every implementor exposes it (as `override val
                    // ptr`), so a base-interface-typed value can reach the C boundary.
                    appendLine("    val ptr: COpaquePointer")
                    for (m in bi.methods) {
                        val params = m.params.joinToString(", ") { (n, t) -> "$n: $t" }
                        appendLine("    fun ${m.name}($params): ${m.renderedReturn}")
                    }
                    appendLine("}")
                    appendLine("// END KRAPPER GEN for base interface ${bi.interfaceName}")
                }
            )
        }
    }

    private val ptr = Raw("ptr")
    private val memScope = Raw("memScope")

    override fun KotlinCodeBuilder.onGenerate(cls: ResolvedClass, method: ResolvedMethod) {
        onGenerate(cls, method, null, null)
    }

    override fun KotlinCodeBuilder.onGenerate(method: ResolvedMethod) {
        val uniqueCName =
            extensionMethod(pkg, method.uniqueCName ?: error("Unnamed method $method"))
        require(method.methodType == MethodType.STATIC) {
            "Non-static method being generated at top level $method"
        }
        inline {
            extensionFunction {
                receiver = fqType(MEM_SCOPE)
                name = method.name.kotlinMethodName()
                // Declared return type is upcast to a base interface when the method
                // returns a base class (so a derived can be returned where the base is
                // wanted); the BODY keeps the concrete returnType so it still constructs
                // the concrete wrapper, which IS-A the interface.
                retType = type(remapReturnType(method.returnType))
                defineArgsAndBody(
                    // Base-typed params upcast to the base interface (the upcast enabler):
                    // the body forwards `arg.ptr`, which the interface also exposes.
                    userArgs = method.args.map { remapArgType(it) },
                    startArgs = emptyList(),
                    returnStyle = method.returnStyle,
                    returnType = method.returnType,
                    uniqueCName = uniqueCName
                )
            }
        }
    }

    fun KotlinCodeBuilder.onGenerate(
        cls: ResolvedClass,
        method: ResolvedMethod,
        size: LocalVar?,
        align: LocalVar?
    ) {
        val uniqueCName =
            extensionMethod(pkg, method.uniqueCName ?: error("Unnamed method $method"))
        when (method.methodType) {
            CONSTRUCTOR -> {
                generateConstructor(cls, method as ResolvedConstructor, size, align, uniqueCName)
            }

            SIZE_OF,
            ALIGN_OF,
            DESTRUCTOR -> {
                // Do nothing
            }

            STATIC -> {
                onGenerate(method)
            }

            METHOD,
            STATIC_OP -> {
                val operator = method.operator
                if (operator != null) {
                    generateOperator(operator, cls, method)
                } else {
                    val named = fixNaming(method)
                    val emit: KotlinCodeBuilder.() -> Unit = {
                        generateBasicMethod(
                            named,
                            uniqueCName,
                            methodName = overloadMethodName(cls, named)
                        )
                    }
                    // A method declared on the generic template interface OR on a base
                    // interface (single-inheritance upcast) must `override` it — and
                    // `override` can't combine with `inline` (illegal on a virtual member),
                    // so the override drops `inline`. Others stay `inline`.
                    if (overridesTemplateInterface(cls, method) ||
                        overridesBaseInterface(cls, method)
                    ) {
                        override(emit)
                    } else {
                        inline(emit)
                    }
                }
            }
        }
    }

    // The MemScope.<Class>(...) factory name. A single-constructor class uses the
    // bare class name; overloaded constructors get an order-independent
    // `__<argtypes>` suffix from their own parameter types (same convention as
    // NameHandler.uniqueOverloadName). No `_`-prefix fallback here: two constructors
    // with identical arg-type suffixes would be a generator bug, not a user error.
    private fun constructorFactoryName(
        cls: ResolvedClass,
        paramArgs: List<ResolvedArgument>
    ): String {
        val base = cls.type.kotlinType.name
        val ctorCount = cls.children.count { it is ResolvedConstructor }
        if (ctorCount <= 1) return base
        val suffix = paramArgs.joinToString("_") { it.type.typeString }.cleanupName()
        return if (suffix.isEmpty()) base else "${base}__$suffix"
    }

    // The Kotlin function name for a (non-constructor, non-operator) method. A
    // method with a unique name on its class keeps the bare name; overloaded
    // methods (same name, distinct arg types) get an order-independent
    // `__<argtypes>` suffix from their own parameter types — mirroring
    // constructorFactoryName. The first/only overload to claim the bare name keeps
    // it (suffix dropped when the first sibling, so a method's identity here is its
    // arg types). const/non-const overloads share identical arg types, so their
    // suffixes collide and they fall back to the downstream `_`-prefix uniquifier.
    private fun overloadMethodName(cls: ResolvedClass?, method: ResolvedMethod): String {
        val base = method.name
        if (cls == null) return base
        // Decision A: a non-virtual method that shadows an inherited same-name one
        // yields the bare name and takes the class-prefixed `<class><Method>` form.
        if (shadowsInheritedNonVirtual(cls, method)) {
            return classPrefixedName(cls, method)
        }
        val siblings = cls.children.filterIsInstance<ResolvedMethod>().filter {
            it !is ResolvedConstructor &&
                it.operator == null &&
                (it.methodType == MethodType.METHOD || it.methodType == MethodType.STATIC_OP) &&
                fixNaming(it).name == base
        }
        if (siblings.size <= 1) return base
        // Real parameter types only; arg[0] is the receiver pointer.
        val suffix = method.args.drop(1).joinToString("_") { it.type.typeString }.cleanupName()
        // The first overload (by arg-type identity) keeps the bare name.
        val first = siblings.minByOrNull {
            it.args.drop(1).joinToString("_") { a -> a.type.typeString }
        }
        val isFirst = first != null &&
            first.args.drop(1).joinToString("_") { it.type.typeString } ==
            method.args.drop(1).joinToString("_") { it.type.typeString }
        return if (isFirst || suffix.isEmpty()) base else "${base}__$suffix"
    }

    private fun KotlinCodeBuilder.generateConstructor(
        cls: ResolvedClass,
        method: ResolvedConstructor,
        size: LocalVar?,
        align: LocalVar?,
        uniqueCName: Symbol
    ) {
        when (method.allocationStyle) {
            DIRECT -> generateDirectConstructor(cls, method, size, align, uniqueCName)
            STACK -> generateStackConstructor(cls, method, uniqueCName)
        }
    }

    private fun KotlinCodeBuilder.generateStackConstructor(
        cls: ResolvedClass,
        method: ResolvedConstructor,
        uniqueCName: Symbol
    ) {
        needsCCaller = true
        extensionFunction {
            receiver = fqType(MEM_SCOPE)
            name = cls.type.kotlinType.name
            retType = type(ResolvedType.UNIT.copy())
            val args = method.args.subList(1, method.args.size).map {
                define(it.name, it.type)
            }.toMutableList()
            val callbackArg = args.removeLast()
            body {
                val lambda = +define(
                    "callback",
                    initializer = lambda {
                        val opaquePointer =
                            define("ptr", nullable(fullyQualifiedType(C_OPAQUE_POINTER)))
                        body {
                            val ptr = +define(
                                "ptr",
                                fullyQualifiedType(C_OPAQUE_POINTER),
                                opaquePointer.reference elvis Call(
                                    "error",
                                    "Creation failed".symbol
                                )
                            )
                            +Call(
                                callbackArg.reference,
                                generateConstructorCall(
                                    cls.type.kotlinType,
                                    ptr.reference,
                                    thiz.reference
                                )
                            )
                        }
                    }
                )
                val stableRef = +define(
                    "withObjStable",
                    initializer = Call(
                        extensionMethod(STABLE_REF_CREATE),
                        lambda.reference
                    )
                )
                +Call(
                    uniqueCName,
                    *(
                        listOf(stableRef.reference dot Call("asCPointer")) +
                            args.map { reference(it) } +
                            extensionMethod(staticRouter)
                        ).toTypedArray()
                )
                +(stableRef.reference dot Call("dispose"))
            }
        }
    }

    private fun KotlinCodeBuilder.generateDirectConstructor(
        cls: ResolvedClass,
        method: ResolvedConstructor,
        size: LocalVar?,
        align: LocalVar?,
        uniqueCName: Symbol
    ) {
        val destructor =
            cls.children.filterIsInstance<ResolvedDestructor>().firstOrNull()
        extensionFunction {
            receiver = fqType(MEM_SCOPE)
            name = constructorFactoryName(cls, method.args.drop(1))
            retType = type(cls.type)
            val args = method.args.subList(1, method.args.size).map {
                define(it.name, it.type)
            }
            body {
                val memory = +define(
                    "memory",
                    fullyQualifiedType(C_OPAQUE_POINTER),
                    initializer = (
                        Call(
                            extensionMethod("kotlinx.cinterop", "interpretCPointer"),
                            Call(
                                "alloc",
                                size!!.reference,
                                align!!.reference
                            ) dot Raw("rawPtr")
                        ) elvis Call("error", "Allocation failed".symbol)
                        )
                )
                memory.isVal = true
                val obj = +define(
                    "obj",
                    fullyQualifiedType(C_OPAQUE_POINTER),
                    initializer = (
                        Call(
                            uniqueCName,
                            *(listOf(memory.reference) + args.map { reference(it) })
                                .toTypedArray()
                        ) elvis Call("error", "Creation failed".symbol)
                        )
                )
                obj.isVal = true
                if (destructor != null) {
                    defer {
                        +Call(
                            extensionMethod(
                                pkg,
                                destructor.uniqueCName
                                    ?: error("Unnamed destructor in $cls")
                            ),
                            obj.reference
                        )
                    }
                }
                +Return(
                    generateConstructorCall(cls.type.kotlinType, obj.reference, thiz.reference)
                )
            }
        }
    }

    private val infixList = setOf(
        "assign",
        "plusEquals",
        "eq",
        "neq",
        "lt",
        "gt",
        "lteq",
        "gteq",
        "binAnd",
        "binOr",
        "and",
        "or",
        "xor",
        "shl",
        "shr"
    )

    private fun fixNaming(method: ResolvedMethod): ResolvedMethod = when {
        method.name in infixList -> method.copy(name = method.name + "_method")

        // A user-defined conversion operator to a CLASS/STRUCT target (`operator nn::Tok`)
        // isn't matched by the scalar-only ConversionOperator, so it reaches the plain-method
        // path with libclang's spelling `operator Tok` — not a valid Kotlin identifier. Give
        // it the same idiomatic `to<Target>` name a scalar conversion gets (`toTok`), off the
        // resolved return type's Kotlin name. (The C++ side already qualifies the call.)
        method.name.startsWith("operator ") &&
            method.name.substring("operator ".length)
                .firstOrNull()?.let { it.isLetter() || it == '_' } == true ->
            method.copy(name = "to" + method.returnType.kotlinType.name.trimEnd('?'))

        else -> method
    }

    // NB: this is the KOTLIN-name operator-symbol table. There are sibling tables on the C
    // side (NameHandler.cleanupName, NameHandler.cleanupOperatorName) — keep them in mind if
    // adding a symbol here; they intentionally differ (Kotlin vs C identifier rules).
    private fun String.kotlinMethodName() = replace("<", "_lt")
        .replace(">", "_gt")
        .replace("\"\"", "_qts")
        .replace("\"", "_qt")
        .replace("()", "_call") // operator() → a plain `_call` method (not idiomatic invoke yet)
        .replace("==", "_cmp")
        .replace("=", "_eq")
        .replace("+", "_plus")
        .replace("-", "_minus")
        .replace("/", "_div")
        .replace("*", "_star")
        .replace("%", "_mod")
        .replace("!", "_not")
        // Bitwise/logic operators from free functions (e.g. std::operator& from <memory>),
        // which fall through the member-operator path to a plainly-named function.
        .replace("&", "_and")
        .replace("|", "_or")
        .replace("^", "_xor")
        .replace("~", "_inv")
        // A C++ method whose name IS a Kotlin hard keyword (`in`/`is`/`when`/`object`/…)
        // has no operator symbols to replace, so it reaches here unchanged and would emit
        // `fun in(...)` (won't parse). Back-tick-escape it (mirrors the param/field path).
        .escapeKotlinKeyword()

    private fun KotlinCodeBuilder.generateBasicMethod(
        method: ResolvedMethod,
        uniqueCName: Symbol,
        methodName: String = method.name,
        startArgs: List<Symbol> = listOf(ptr),
        skipFirstArg: Boolean = true
    ) {
        function {
            name = methodName.kotlinMethodName()
            // Declared return upcast to a base interface when applicable; body keeps the
            // concrete returnType (constructs the concrete wrapper). See onGenerate(method).
            // EXCEPT the index `[]` operator: it returns a CONTAINER ELEMENT, which must stay
            // CONCRETE — the synthesized `Iterable<Elem>`/`iterator()` (detectIndexIterable)
            // declares the raw element type, and the element's own methods (e.g. a walked
            // `clang::Decl`'s getDeclKindName/asNamedDecl) live on the concrete class, not the
            // (member-less) base interface. Remapping `get` to `DeclApi` both mismatched
            // `next()` and made the elements useless.
            retType = type(
                if (method.operator == ResolvedOperator.IND) {
                    method.returnType
                } else {
                    remapReturnType(method.returnType)
                }
            )
            val userArgs = if (method.args.isNotEmpty()) {
                method.args.subList(if (skipFirstArg) 1 else 0, method.args.size)
            } else {
                emptyList()
            }
            defineArgsAndBody(
                userArgs = userArgs.map { remapArgType(it) },
                startArgs = startArgs,
                returnStyle = method.returnStyle,
                returnType = method.returnType,
                uniqueCName = uniqueCName
            )
        }
    }

    /**
     * Define the caller-facing parameters and emit the method body that forwards
     * to the C wrapper [uniqueCName]. [startArgs] are fixed leading call arguments
     * (e.g. the receiver `ptr`); [userArgs] are the resolved parameters the caller
     * supplies. The common path defines one param per [userArgs] entry and calls
     * [generateMethodBody]; the Mode-1 callback path ([detectMode1Callback]) rewrites
     * a `(funcPointer, void* userData)` arg pair into a single Kotlin lambda param.
     */
    private fun FunctionBuilder<KotlinFactory>.defineArgsAndBody(
        userArgs: List<ResolvedArgument>,
        startArgs: List<Symbol>,
        returnStyle: ReturnStyle,
        returnType: ResolvedCppType,
        uniqueCName: Symbol
    ) {
        // Only take the Mode-1 path for a plain-value return: the body captures the C
        // result in a local and returns it directly, so it doesn't apply the
        // wrapper/enum/string boundary conversions generateReturn would. Those returns
        // fall through to the (already-working) raw Mode-2 surface rather than risk a
        // miscompiled conversion. (The synchronous Mode-1 cases in scope return scalars.)
        val mode1 = detectMode1Callback(userArgs)?.takeIf { isPlainValueReturn(returnType) }
        if (mode1 != null) {
            generateMode1CallbackArgsAndBody(
                userArgs,
                startArgs,
                returnStyle,
                returnType,
                uniqueCName,
                mode1
            )
            return
        }
        val args = userArgs.map { arg ->
            val default = renderKotlinDefault(arg)
            if (default != null) {
                define(arg.name, arg.type, Raw(default))
            } else {
                define(arg.name, arg.type)
            }
        }
        body {
            generateMethodBody(
                startArgs + args.map { reference(it) },
                returnStyle,
                returnType,
                uniqueCName
            )
        }
    }

    /**
     * Maps a C++ default-argument expression (the source text in
     * [ResolvedArgument.defaultValue]) to an equivalent Kotlin default-value literal,
     * or null when the default isn't a simple renderable literal (a constructor call,
     * named constant, unhandled enum, ...). In the null case the parameter stays
     * mandatory — correctness over coverage; an uncompilable default is never emitted.
     *
     * The Kotlin default is filled in at the caller's call site and forwarded to the
     * fixed-arity C wrapper unchanged, so no C-side change is required.
     */
    private fun renderKotlinDefault(arg: ResolvedArgument): String? {
        val raw = arg.defaultValue?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val kotlinType = arg.type.kotlinType
        val typeName = kotlinType.name.trimEnd('?')

        // nullptr / NULL / 0 for a pointer/wrapper param -> Kotlin null.
        if (kotlinType.isWrapper || kotlinType.isNullable) {
            return if (raw == "nullptr" || raw == "NULL" || raw == "0") "null" else null
        }

        // Boolean literal.
        if (typeName == "Boolean") {
            return raw.takeIf { it == "true" || it == "false" }
        }

        // Character literal: char maps to Kotlin Byte, so render `'x'` as `'x'.code.toByte()`.
        // Only a plain char or a Kotlin-recognized escape renders; an escape Kotlin doesn't
        // know (`'\a'`, `'\xNN'`, `'\NNN'`) falls back to a mandatory param.
        if (typeName == "Byte" && raw.length >= 3 && raw.first() == '\'' && raw.last() == '\'') {
            val inner = raw.substring(1, raw.length - 1)
            val kotlinEscapes = setOf('t', 'b', 'n', 'r', '\'', '"', '\\', '$', '0')
            val renderable = inner.length == 1 ||
                (inner.length == 2 && inner[0] == '\\' && inner[1] in kotlinEscapes)
            return if (renderable) "$raw.code.toByte()" else null
        }

        // Floating-point literal (strip a trailing `f`/`F`/`l`/`L`).
        if (typeName == "Float" || typeName == "Double") {
            var number = raw.trimEnd('f', 'F', 'l', 'L')
            if (!number.matches(FLOAT_LITERAL)) return null
            // C++ permits a bare leading/trailing dot (`.5`, `1.`); Kotlin needs a digit on
            // both sides. Normalize, then accept only a strictly Kotlin-valid form — else
            // fall back to a mandatory param rather than emit uncompilable Kotlin.
            val sign = number.takeWhile { it == '+' || it == '-' }
            number = number.removePrefix(sign)
            if (number.startsWith('.')) number = "0$number"
            if (number.endsWith('.')) number = "${number}0"
            number = sign + number
            if (!number.matches(KOTLIN_FLOAT_LITERAL)) return null
            return when (typeName) {
                "Float" -> "${number}f"

                else -> if (number.contains('.') || number.contains('e') || number.contains('E')) {
                    number
                } else {
                    "$number.0"
                }
            }
        }

        // Integer literal (strip C integer suffixes); render with the type's converter
        // so it type-checks against the param's exact Kotlin integer type.
        val intText = raw.trimEnd('u', 'U', 'l', 'L')
        if (!intText.matches(INT_LITERAL)) return null
        // Kotlin has no octal / leading-zero integer literals (C++ `0777`, `010`); fall back
        // to a mandatory param. (`0` itself, and hex `0x..`, are fine.)
        val intBody = intText.trimStart('+', '-')
        if (intBody.length > 1 && intBody[0] == '0' && intBody[1] != 'x' && intBody[1] != 'X') {
            return null
        }
        return when (typeName) {
            "Int" -> intText
            "Long" -> "${intText}L"
            "UInt" -> "${intText}u"
            "ULong" -> "${intText}uL"
            "Short" -> "$intText.toShort()"
            "Byte" -> "$intText.toByte()"
            "UByte" -> "$intText.toUByte()"
            "UShort" -> "$intText.toUShort()"
            else -> null
        }
    }

    /**
     * A detected Mode-1 callback pair within a method's argument list: a C
     * function-pointer arg whose first proto parameter is a `void*` context slot,
     * paired with an adjacent plain `void*` userData arg. Both are consumed and
     * replaced by a single Kotlin lambda param.
     *
     * @param fnIndex index (into the method's user args) of the function-pointer arg
     * @param userDataIndex index of the plain `void*` userData arg
     * @param lambdaType the Kotlin lambda type the caller passes, e.g. `(Int) -> Int`
     *   (the callback proto with its leading `void*` ctx dropped)
     * @param protoArgTypes the (short-name) Kotlin types of every callback proto param
     *   including the leading ctx, e.g. `[COpaquePointer, Int]` — used to type the
     *   trampoline params
     */
    private data class Mode1Callback(
        val fnIndex: Int,
        val userDataIndex: Int,
        val lambdaType: String,
        val protoArgTypes: List<String>
    )

    /**
     * Detect a Mode-1 callback pair in [args]: a function-pointer arg whose callback
     * proto takes a leading `void*` context slot, alongside an adjacent plain `void*`
     * userData arg. This is the C idiom where the caller registers a callback plus an
     * opaque context the callback receives back. We bridge it to a plain (capturing)
     * Kotlin lambda by boxing the lambda in a StableRef passed through that `void*`.
     *
     * A wrong guess crashes at runtime, so this is deliberately strict: it fires only
     * when there is EXACTLY ONE qualifying function-pointer arg, EXACTLY ONE plain
     * `void*` arg, and the two are adjacent. Anything ambiguous returns null, leaving
     * the raw `CPointer<CFunction<...>>?` (Mode-2) surface untouched.
     */
    private fun detectMode1Callback(args: List<ResolvedArgument>): Mode1Callback? {
        val fnIndices = args.indices.filter { i ->
            val fp = args[i].type.funcPointer
            fp != null && fp.argCTypes.firstOrNull()?.let { isVoidPointer(it) } == true
        }
        val voidPtrIndices = args.indices.filter { i ->
            args[i].type.funcPointer == null && isVoidPointer(args[i].type.typeString)
        }
        if (fnIndices.size != 1 || voidPtrIndices.size != 1) return null
        val fnIndex = fnIndices.single()
        val userDataIndex = voidPtrIndices.single()
        if (fnIndex - userDataIndex !in -1..1) return null // must be adjacent

        // Recover the callback proto's Kotlin types from the function-pointer arg's
        // already-resolved Kotlin type: `CPointer<CFunction<(<proto>) -> <ret>>>?`.
        // templates[0] is the CFunction<...>, whose templates[0] is the `(...) -> ...`
        // literal leaf. The leading proto arg is the `void*` ctx; dropping it yields
        // the lambda type the caller passes.
        val funcType = args[fnIndex].type.kotlinType
            .templates.firstOrNull()
            ?.templates?.firstOrNull()?.name
            ?: return null
        val (protoArgs, protoReturn) = parseFunctionType(funcType) ?: return null
        if (protoArgs.isEmpty()) return null
        // Short names (e.g. `Int`, `COpaquePointer`) for the `(...) -> ...` literal: it
        // renders verbatim as an import-skipped leaf, like the Mode-2 surface.
        val lambdaType = "(${protoArgs.drop(1).joinToString(", ")}) -> $protoReturn"
        return Mode1Callback(fnIndex, userDataIndex, lambdaType, protoArgs)
    }

    // Re-qualify a short Kotlin type name to its fully-qualified form so a generated
    // trampoline param shares the SAME fqName as the type's other uses (no import alias).
    // Mode-1 proto types are restricted to `void`/`void*`/native scalars, so the only
    // forms are `COpaquePointer` and `kotlin.*` primitives.
    private fun qualifyKotlinTypeName(name: String): String = when {
        '.' in name -> name
        name.trimEnd('?') == "COpaquePointer" -> "kotlinx.cinterop.$name"
        else -> "kotlin.$name"
    }

    // A C/C++ `void*` (the userData/context slot). Spacing is normalized by the
    // type renderer to `void*`, but accept `void *` defensively.
    private fun isVoidPointer(typeString: String): Boolean = typeString.replace(" ", "") == "void*"

    // A return whose value crosses the boundary as-is (no wrapper/enum/string
    // conversion in generateReturn). The Mode-1 body returns such a value directly
    // after disposing the StableRef; richer returns stay on the Mode-2 surface.
    private fun isPlainValueReturn(returnType: ResolvedCppType): Boolean {
        val kt = returnType.kotlinType
        return !kt.isWrapper && !kt.isEnum && kt.fullyQualified != "kotlin.String"
    }

    /**
     * Split a Kotlin function-type literal `(<arg>, <arg>, ...) -> <ret>` into its
     * argument-type list and return type. Args are separated on top-level commas
     * (none of the Mode-1 proto types — native scalars / `COpaquePointer?` — contain
     * commas, but bracket-depth tracking keeps this robust). Returns null if the
     * string isn't of that shape.
     */
    private fun parseFunctionType(literal: String): Pair<List<String>, String>? {
        val arrow = literal.lastIndexOf("->")
        if (!literal.startsWith("(") || arrow < 0) return null
        val close = literal.lastIndexOf(')', arrow)
        if (close <= 0) return null
        val inner = literal.substring(1, close).trim()
        val ret = literal.substring(arrow + 2).trim()
        if (inner.isEmpty()) return emptyList<String>() to ret
        val args = mutableListOf<String>()
        var depth = 0
        var start = 0
        inner.forEachIndexed { i, c ->
            when (c) {
                '(', '<' -> depth++

                ')', '>' -> depth--

                ',' -> if (depth == 0) {
                    args.add(inner.substring(start, i).trim())
                    start = i + 1
                }
            }
        }
        args.add(inner.substring(start).trim())
        return args to ret
    }

    /**
     * Emit the Mode-1 callback method: a single lambda param replaces the consumed
     * function-pointer + `void*` userData pair, and the body boxes the lambda in a
     * StableRef, forwards a non-capturing `staticCFunction` trampoline (which recovers
     * the lambda from the `void*` ctx) plus the StableRef pointer into the C call,
     * captures the result, disposes the StableRef, then returns. Synchronous only:
     * the StableRef lifetime is exactly this call.
     */
    private fun FunctionBuilder<KotlinFactory>.generateMode1CallbackArgsAndBody(
        userArgs: List<ResolvedArgument>,
        startArgs: List<Symbol>,
        returnStyle: ReturnStyle,
        returnType: ResolvedCppType,
        uniqueCName: Symbol,
        mode1: Mode1Callback
    ) {
        needsCCaller = true
        // Define params in source order, substituting one lambda param for the
        // function-pointer arg and dropping the consumed userData arg entirely.
        val paramSymbols = LinkedHashMap<Int, Symbol>()
        var lambdaParam: LocalVar? = null
        for ((i, arg) in userArgs.withIndex()) {
            when (i) {
                mode1.fnIndex -> {
                    // `noinline`: the lambda is stored in a StableRef (not inline-invoked),
                    // which an `inline` wrapper would otherwise reject.
                    lambdaParam = define(arg.name, fullyQualifiedType(mode1.lambdaType)).also {
                        (it as KotlinLocalVar).noinline = true
                    }
                    paramSymbols[i] = lambdaParam.reference
                }

                mode1.userDataIndex -> {
                    // consumed: filled by ref.asCPointer() in callArgs below
                }

                else -> paramSymbols[i] = reference(define(arg.name, arg.type))
            }
        }
        val cb = lambdaParam ?: error("Mode-1 detection produced no lambda param")
        body {
            val ref = +define(
                "ref",
                initializer = Call(extensionMethod(STABLE_REF_CREATE), cb.reference)
            ).also { it.isVal = true }
            // Non-capturing trampoline: recover the lambda from the void* ctx (first
            // proto arg) and forward the remaining proto args to it.
            val trampoline = lambda {
                type = type(fullyQualifiedType(STATIC_C_FUNCTION))
                // Type each trampoline param as the callback proto (qualified so imports
                // resolve) so the inferred `staticCFunction` type matches the C wrapper's
                // parameter. cinterop types the pointer params of a CFunction as nullable,
                // so the ctx (void*) slot is `COpaquePointer?` — force-unwrapped below.
                val ctx = define(
                    "ctx",
                    nullable(fullyQualifiedType(qualifyKotlinTypeName(mode1.protoArgTypes.first())))
                )
                val forwarded = mode1.protoArgTypes.drop(1).mapIndexed { j, t ->
                    define("a$j", fullyQualifiedType(qualifyKotlinTypeName(t)))
                }
                body {
                    // ctx is the StableRef pointer we passed as userData (never null by
                    // construction, hence `!!`); recover the boxed lambda and forward the
                    // remaining proto args to it. A bare expression (not `return`) so the
                    // lambda yields this value.
                    +(
                        Concat(ctx.reference, Raw("!!")) dot Call(
                            extensionMethod(STABLE_REF),
                            templateArgs = listOf(Raw(mode1.lambdaType))
                        ) dot Call("get") dot Call(
                            "invoke",
                            *forwarded.map { it.reference }.toTypedArray()
                        )
                        )
                }
            }
            // Assemble the C call args: start args, then per-index user args with the
            // function-pointer slot -> trampoline and the userData slot -> ref pointer.
            val callArgs = startArgs.toMutableList()
            for (i in userArgs.indices) {
                callArgs += when (i) {
                    mode1.fnIndex -> trampoline
                    mode1.userDataIndex -> ref.reference dot Call("asCPointer")
                    else -> paramSymbols[i] ?: error("Missing param for arg $i")
                }
            }
            val result = +define(
                "result",
                returnType,
                initializer = Call(uniqueCName, *callArgs.toTypedArray())
            ).also { it.isVal = true }
            +(ref.reference dot Call("dispose"))
            +Return(result.reference)
        }
    }

    private fun CodeBuilder<KotlinFactory>.generateMethodBody(
        args: List<Symbol>,
        returnStyle: ReturnStyle,
        returnType: ResolvedCppType,
        uniqueCName: Symbol
    ) {
        val kotlinType = returnType.kotlinType
        if (returnStyle == ARG_CAST && kotlinType.isWrapper) {
            val ret = define(
                "retValue",
                returnType,
                initializer = memScope dot Call(
                    extensionMethod(
                        kotlinType.fullyQualified + ".Companion",
                        kotlinType.name.trimEnd('?') + "_Holder"
                    )
                )
            ).also {
                it.isVal = true
            }
            +ret
            +Call(
                uniqueCName,
                *(args + reference(ret)).toTypedArray()
            )
            +Return(ret.reference)
        } else {
            val call = Call(
                uniqueCName,
                *args.toTypedArray()
            )
            // cinterop types a C function returning a function-pointer typedef as the
            // generic `CPointer<out CPointed>?`, which doesn't match our precise
            // `CPointer<CFunction<...>>?` return type. Reinterpret it back (the target
            // type is inferred from the declared return type).
            val returnCall = if (returnType.funcPointer != null) {
                call qdot Call(extensionMethod("kotlinx.cinterop", "reinterpret"))
            } else {
                call
            }
            generateReturn(
                kotlinType,
                returnCall,
                returnStyle
            )
        }
    }

    private fun KotlinCodeBuilder.generateOperator(
        operator: ResolvedOperator,
        cls: ResolvedClass,
        method: ResolvedMethod
    ) {
        // C++ operator== maps to an idiomatic Kotlin `equals` override (so `a == b`
        // works) rather than an `infix fun eq`. The matching `hashCode` override is
        // emitted alongside in onGenerateMethods to keep the equals/hashCode
        // contract.
        if (operator == ResolvedOperator.EQ) {
            generateEquals(cls, method)
            return
        }
        // C++ `operator<` maps to an idiomatic Kotlin `operator fun compareTo`
        // synthesized from the single `<` (calling the `_op_lt` C wrapper twice with
        // swapped operands) rather than an `infix fun lt`, so `a < b`, `a > b`,
        // `compareTo`, and `sorted()` all work.
        if (operator == ResolvedOperator.LT) {
            generateCompareTo(cls, method)
            return
        }
        // A C++ conversion operator (`operator double`, `operator bool`, ...) becomes
        // an idiomatic Kotlin `toDouble()`/`toBoolean()`/... named after the (scalar)
        // destination type rather than a symbolic operator.
        if (operator == ResolvedOperator.CONVERSION) {
            generateConversion(method)
            return
        }
        when (val kotlinType = operator.kotlinOperatorType) {
            is KotlinOperator -> {
                inline {
                    operator {
                        generateBasicMethod(
                            method,
                            extensionMethod(pkg, method.uniqueCName!!),
                            kotlinType.name
                        )
                    }
                }
            }

            is InfixMethod -> {
                inline {
                    infix {
                        generateBasicMethod(
                            method,
                            extensionMethod(pkg, method.uniqueCName!!),
                            kotlinType.name
                        )
                    }
                }
            }

            is BasicWithDummyMethod -> {
                inline {
                    generateBasicMethod(
                        method.copy(args = emptyList()),
                        extensionMethod(pkg, method.uniqueCName!!),
                        kotlinType.name,
                        startArgs = listOf(ptr, Raw("0"))
                    )
                }
            }

            is BasicMethod -> {
                inline {
                    generateBasicMethod(
                        method,
                        extensionMethod(pkg, method.uniqueCName!!),
                        kotlinType.name
                    )
                }
            }
        }
    }

    // C++ conversion operator → a Kotlin `to<Target>()` (the scalar destination
    // names the method: `operator double` → `toDouble`, `operator bool` →
    // `toBoolean`). The destination is a primitive scalar, so the C wrapper returns
    // it by value and this is just a plain forwarding method named after the type.
    private fun KotlinCodeBuilder.generateConversion(method: ResolvedMethod) {
        inline {
            generateBasicMethod(
                method,
                extensionMethod(pkg, method.uniqueCName!!),
                "to" + method.returnType.kotlinType.name
            )
        }
    }

    // C++ `operator==` → an idiomatic Kotlin `equals` override. The body narrows
    // `other` to the wrapper type, then delegates the actual comparison to the
    // generated `_op_eq` C function over the two backing pointers. Using the
    // extensionMethod Call (rather than a Raw) keeps the C symbol's import wired up.
    private fun KotlinCodeBuilder.generateEquals(cls: ResolvedClass, method: ResolvedMethod) {
        val typeName = cls.type.kotlinType.name.trimEnd('?')
        override {
            function {
                name = "equals"
                retType = type(ResolvedKotlinType("kotlin.Boolean", false))
                define("other", nullable(fullyQualifiedType("kotlin.Any")))
                body {
                    +Return(
                        Concat(
                            Raw("other is $typeName && "),
                            Call(
                                extensionMethod(pkg, method.uniqueCName!!),
                                ptr,
                                Raw("(other as $typeName).ptr")
                            )
                        )
                    )
                }
            }
        }
    }

    // C++ `operator<` → an idiomatic Kotlin `operator fun compareTo`. There is no
    // single C "compare" wrapper, so a total order is synthesized from the one `<`:
    // call the generated `_op_lt` C function twice over the backing pointers with
    // swapped operands — `this < other` → -1, else `other < this` → 1, else equal → 0.
    // Using the extensionMethod Call (rather than a Raw) keeps the C symbol imported.
    private fun KotlinCodeBuilder.generateCompareTo(cls: ResolvedClass, method: ResolvedMethod) {
        override {
            operator {
                function {
                    name = "compareTo"
                    retType = type(ResolvedKotlinType("kotlin.Int", false))
                    define("other", cls.type.kotlinType.copy(isNullable = false))
                    body {
                        +Return(
                            Concat(
                                Raw("if ("),
                                Call(
                                    extensionMethod(pkg, method.uniqueCName!!),
                                    ptr,
                                    Raw("other.ptr")
                                ),
                                Raw(") -1 else if ("),
                                Call(
                                    extensionMethod(pkg, method.uniqueCName!!),
                                    Raw("other.ptr"),
                                    ptr
                                ),
                                Raw(") 1 else 0")
                            )
                        )
                    }
                }
            }
        }
    }

    // Always paired with `equals` so the equals/hashCode contract holds: equal
    // objects (same `operator==`) must hash equal. Best-effort and allowed to be
    // poorly-distributed — it folds the public fields' hashCodes (the same state
    // `operator==` compares), or returns a fixed constant when the class exposes
    // no usable fields.
    private fun KotlinCodeBuilder.generateHashCode(cls: ResolvedClass) {
        val fields = cls.children.filterIsInstance<ResolvedField>()
        override {
            function {
                name = "hashCode"
                retType = type(ResolvedKotlinType("kotlin.Int", false))
                body {
                    if (fields.isEmpty()) {
                        +Return(Raw("0"))
                        return@body
                    }
                    +Raw("var result = ${fieldHash(fields.first())}")
                    for (field in fields.drop(1)) {
                        +Raw("result = 31 * result + ${fieldHash(field)}")
                    }
                    +Return(Raw("result"))
                }
            }
        }
    }

    private fun fieldHash(field: ResolvedField): String = "${field.name}.hashCode()"

    /**
     * The element type + index-get name for an index-accessible container, or null
     * when the class isn't one. Used both to add the `: Iterable<Elem>` supertype and
     * to emit the matching `override operator fun iterator()`.
     *
     * @param elemFq the FQ Kotlin element type (`get`'s return) — `Point?`, or a
     *   `CValuesRef<IntVar>?` element pointer for primitive vectors
     * @param getName the Kotlin name the index get is emitted under (`operator[]` -> `get`)
     */
    private data class IndexIterable(val elemFq: String, val getName: String)

    /**
     * Detect an index-accessible container (std::vector — exposes a `size()` plus an
     * index `get(<size_t>)`). Detection is deliberately narrow so it ONLY fires on real
     * index containers: the index `get`'s single argument must have the SAME C++ type as
     * `size()`'s return (`size_t` for vector). That excludes `std::map`, whose `get`
     * takes the KEY type (e.g. `int`) — map iteration needs real key/value pairs and is
     * out of scope — and excludes user templates like `Box`, whose `get()` takes no index.
     */
    private fun detectIndexIterable(cls: ResolvedClass): IndexIterable? {
        val instanceMethods = cls.children.filterIsInstance<ResolvedMethod>().filter {
            it.methodType == MethodType.METHOD || it.methodType == MethodType.STATIC_OP
        }
        // size(): a no-user-arg method named `size` returning a non-Unit value.
        val sizeMethod = instanceMethods.firstOrNull {
            it.operator == null &&
                it.name == "size" &&
                it.args.size == 1 &&
                it.returnType.kotlinType.fullyQualified != "kotlin.Unit"
        } ?: return null
        // Compare the C++ index type ignoring const/whitespace: `size()` surfaces as
        // `const size_t` but the index arg is a bare `size_t`; the size-vs-key
        // distinction (vector `size_t` vs map `int`) survives this normalization.
        val sizeType = normalizeIndexType(sizeMethod.returnType.typeString)

        // The index `get`: prefer the `[]` operator (emits as Kotlin `get`); fall back
        // to a plain `get`/`at`. Must take exactly one user arg whose C++ type matches
        // size()'s return type (so map's key-typed get is rejected), returning non-Unit.
        fun ResolvedMethod.isIndexGet(): Boolean = args.size == 2 &&
            normalizeIndexType(args[1].type.typeString) == sizeType &&
            returnType.kotlinType.fullyQualified != "kotlin.Unit"
        val indexGet = instanceMethods.firstOrNull {
            it.operator == ResolvedOperator.IND && it.isIndexGet()
        } ?: instanceMethods.firstOrNull {
            it.operator == null && (it.name == "get" || it.name == "at") && it.isIndexGet()
        } ?: return null
        val getName = indexGet.operator?.kotlinOperatorType?.let {
            (it as? KotlinOperator)?.name
        } ?: overloadMethodName(cls, fixNaming(indexGet))
        return IndexIterable(renderFqKotlinType(indexGet.returnType.kotlinType), getName)
    }

    /**
     * Emit the synthesized `override operator fun iterator()` for an index-accessible
     * container so `for (x in v)` AND the whole `Iterable` surface (`map`/`toList`/
     * `sumOf`/…) work. It just loops `0 until size()` over the existing index `get`; no
     * C++-iterator wrapping. The class also carries `: Iterable<Elem>` (added in
     * onGenerate) — `for` needs only `iterator()`, but the stdlib extensions are defined
     * on `Iterable`, so the supertype is what unlocks `map`/`toList`/`sumOf`.
     */
    private fun KotlinCodeBuilder.generateIndexIterator(cls: ResolvedClass) {
        val iterable = detectIndexIterable(cls) ?: return
        val elem = iterable.elemFq
        // The index cursor matches `size()`'s type so the `<` comparison and the
        // `get(...)` arg type both line up with no conversion. `size() - size()` is a
        // typed zero of exactly the size type (`size_t`, a ULong typealias) — robust to
        // whatever integral that aliases, without guessing a literal suffix.
        val zero = "(size() - size())"
        // Emitted verbatim (FQ types, no imports), mirroring the Raw-string approach in
        // generateCompareTo/generateHashCode. `size()`/`get(...)` are this class's own members.
        +Raw(
            buildString {
                appendLine(
                    "override operator fun iterator(): kotlin.collections.Iterator<$elem> ="
                )
                appendLine("    object : kotlin.collections.Iterator<$elem> {")
                appendLine("        private var __i = $zero")
                appendLine("        override fun hasNext(): Boolean = __i < size()")
                appendLine("        override fun next(): $elem = ${iterable.getName}(__i++)")
                append("    }")
            }
        )
    }

    // Normalize a C++ index type for the size-vs-key comparison: drop a leading/trailing
    // `const` and all whitespace so `const size_t` and `size_t` match, while `int` (a map
    // key) stays distinct from `size_t`.
    private fun normalizeIndexType(typeString: String): String =
        typeString.replace("const", " ").replace(Regex("\\s+"), "")

    // Render a Kotlin type fully-qualified (recursing through template args, preserving
    // nullability) so it can be emitted inline without an import — used by the
    // synthesized iterator's `Iterator<T>` element type. `fullyQualified` drops template
    // args and the trailing `?`, so reassemble both here.
    private fun renderFqKotlinType(type: ResolvedKotlinType): String {
        val base = type.fullyQualified
        val args = if (type.templates.isEmpty()) {
            ""
        } else {
            "<" + type.templates.joinToString(", ") { renderFqKotlinType(it) } + ">"
        }
        return base + args + if (type.isNullable) "?" else ""
    }

    private fun reference(v: LocalVar): Symbol {
        (v as? KotlinLocalVar) ?: error("Non-kotlin local var $v")
        val type = v.type
        return reference(type, v)
    }

    private fun reference(type: ResolvedKotlinType?, v: LocalVar): Symbol =
        if (type != null && type.isEnum) {
            // An enum arg surfaces as the Kotlin `enum class`; the C boundary wants
            // the underlying integer, so pass `<arg>.value`.
            v.reference dot Raw("value")
        } else if (type != null && type.isWrapper) {
            if (type.toString().endsWith("?")) {
                v.reference qdot ptr
            } else {
                v.reference dot ptr
            }
        } else {
            v.reference
        }

    override fun KotlinCodeBuilder.onGenerate(cls: ResolvedClass, field: ResolvedField) {
        +property(define(field.name, field.kotlinType)) {
            getter = inline(
                getter {
                    generateMethodBody(
                        listOf(ptr),
                        field.getter.returnStyle,
                        field.getter.returnType,
                        extensionMethod(pkg, field.getter.uniqueCName!!)
                    )
                }
            )
            if (!field.isConst) {
                setter = inline(
                    setter { value ->
                        +Call(
                            extensionMethod(pkg, field.setter.uniqueCName!!),
                            ptr,
                            reference(field.kotlinType, value)
                        )
                    }
                )
            }
        }
    }

    private fun CodeBuilder<KotlinFactory>.generateReturn(
        returnType: ResolvedKotlinType,
        call: Symbol,
        returnStyle: ReturnStyle
    ) {
        when {
            // Check enum BEFORE wrapper: an enum-typed FIELD getter (e.g. ASTContext::TUKind:
            // TranslationUnitKind) resolves its type with isWrapper ALSO set, so the wrapper
            // branch would emit an illegal `Enum(ptr, memScope)` construction ("enum cannot be
            // instantiated"). An enum is never wrapper-constructed; its `fromValue` path always
            // wins. (Method-return enums have isWrapper=false, so the old ordering didn't break
            // them — this only corrects the field-getter path.)
            returnType.isEnum -> {
                // The C call returns the underlying integer; wrap it back into the
                // generated Kotlin `enum class` via its `fromValue` companion fn.
                +Return(
                    Call(
                        extensionMethod(
                            returnType.fullyQualified.trimEnd('?') + ".Companion",
                            "fromValue"
                        ),
                        call
                    )
                )
            }

            returnType.isWrapper -> {
                +Return(
                    generateConstructorCall(
                        returnType,
                        if (returnType.isNullable) {
                            call elvis Return(Raw("null"))
                        } else {
                            asserting(call)
                        },
                        memScope
                    )
                )
            }

            returnType.fullyQualified == "kotlin.String" -> {
                generateStringReturn(
                    call,
                    free = returnStyle == STRING || returnStyle == STRING_POINTER
                )
            }

            else -> {
                +Return(call)
            }
        }
    }

    private fun generateConstructorCall(
        type: ResolvedKotlinType,
        ptr: Symbol,
        memScope: Symbol
    ): Symbol = Call(constructorMethod(type), ptr, memScope)

    private fun constructorMethod(type: ResolvedKotlinType) = extensionMethod(
        type.pkg,
        type.name.trimEnd('?')
    )

    private fun KotlinCodeBuilder.generateStringReturn(call: Symbol, free: Boolean = true) {
        val strDecl = +define(
            "str",
            nullable(
                fullyQualifiedType(C_POINTER)
                    .typedWith(listOf(fullyQualifiedType("kotlinx.cinterop.ByteVar")))
            ),
            initializer = call
        )
        strDecl.isVal = true
        val retValue = +define(
            "ret",
            fullyQualifiedType("kotlin.String?"),
            initializer = strDecl.reference qdot Call(
                extensionMethod(
                    "kotlinx.cinterop",
                    "toKString"
                )
            )
        )
        retValue.isVal = true
        if (free) {
            +Call(extensionMethod("platform.linux", "free"), strDecl.reference)
        }
        +Return(retValue.reference)
    }
}
