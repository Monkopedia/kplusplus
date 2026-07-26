package com.monkopedia.kplusplus.compiler.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.resolve.calls.candidate.CallInfo
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.types.FirTypeProjectionWithVariance
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Single source of truth for the kplusplus container facades (`cppVector<T>()`,
 * `cppMap<K,V>()`, …): which functions exist, how a Kotlin type argument maps to
 * a C++ element type, the C++ instantiation spec, and the generated binding's
 * ClassId. Shared by the FIR call refinement, the SYNC_REQUIRED call checker,
 * and (indirectly) IR lowering.
 *
 * Binding-name mangling mirrors what krapper's KotlinWriter emits:
 *   std::vector<int>      -> std.Vector__Int
 *   std::map<int, int>    -> std.Map__Int__Int
 *
 * The C++ element type table is hardcoded for primitives; a later step
 * derives it from the generator (and per-binding `@CppBinding` annotations so
 * Kotlin classes that are themselves bindings can appear as element types).
 */
internal object CppVectorMapping {

    data class Container(
        val callableId: CallableId,
        val cppBase: String,
        val bindingPrefix: String,
        val arity: Int,
        // Package the generated binding lives in, or null for the std-container
        // default (`std`, rooted under rootPackage when set). User templates
        // derived from a `@CppTemplate` facade carry their concrete binding's
        // package here (e.g. "root" for a top-level `Box<int>`).
        val bindingPackage: String? = null
    )

    // Neutral facade package (the eventual kplusplus runtime-lib package). The
    // cppVector/cppMap entry-point functions must be declared here for the plugin
    // to recognize them — previously hardcoded to the slice demo's package.
    private val FACADE_PACKAGE = FqName("com.monkopedia.kplusplus")

    val cppVector = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppVector")),
        cppBase = "std::vector",
        bindingPrefix = "Vector",
        arity = 1
    )

    val cppMap = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppMap")),
        cppBase = "std::map",
        bindingPrefix = "Map",
        arity = 2
    )

    val cppPair = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppPair")),
        cppBase = "std::pair",
        bindingPrefix = "Pair",
        arity = 2
    )

    // bindingPrefix mirrors the generator's class-name mangling, which only
    // capitalizes the first character of the C++ base's last segment — so
    // `std::unique_ptr` -> `Unique_ptr`, `std::unordered_map` -> `Unordered_map`
    // (not `UnorderedMap`; cf. cppMap's `map` -> `Map`).
    val cppUniquePtr = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppUniquePtr")),
        cppBase = "std::unique_ptr",
        bindingPrefix = "Unique_ptr",
        arity = 1
    )

    val cppUnorderedMap = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppUnorderedMap")),
        cppBase = "std::unordered_map",
        bindingPrefix = "Unordered_map",
        arity = 2
    )

    val cppUnorderedSet = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppUnorderedSet")),
        cppBase = "std::unordered_set",
        bindingPrefix = "Unordered_set",
        arity = 1
    )

    val cppSet = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppSet")),
        cppBase = "std::set",
        bindingPrefix = "Set",
        arity = 1
    )

    private val containers =
        listOf(cppVector, cppMap, cppPair, cppUniquePtr, cppUnorderedMap, cppUnorderedSet, cppSet)

    // The configured root package (from the `rootPackage` SubpluginOption), or null
    // for the historical layout. Set once per compilation by the component registrar;
    // container bindings then live in `<rootPackage>.std` — matching the generator,
    // which roots every binding under the same configured package. Compiler-daemon-
    // lifetime state is fine (each daemon serves one project's compilations).
    private var rootPackage: String? = null

    fun configureRootPackage(pkg: String?) {
        rootPackage = pkg
    }

    /** Compatibility alias for the original single-container call sites. */
    val cppVectorId: CallableId get() = cppVector.callableId

    fun containerForCallable(callableId: CallableId): Container? =
        containers.firstOrNull { it.callableId == callableId }

    /**
     * Resolve the [Container] for a facade call. First the hardcoded std
     * containers (cppVector/cppMap/cppPair) by callableId; failing that, derive
     * one from a `@krapper.CppTemplate` annotation on the resolved facade
     * function — the generic path for USER templates (`fun <T> MemScope.Box()`).
     */
    fun containerForCallable(
        symbol: FirNamedFunctionSymbol,
        session: FirSession
    ): Container? =
        containerForCallable(symbol.callableId)
            ?: cppTemplateContainer(symbol, session)

    private fun cppTemplateContainer(
        symbol: FirNamedFunctionSymbol,
        session: FirSession
    ): Container? {
        val annotation = symbol.getAnnotationByClassId(CPP_TEMPLATE_ANNOTATION, session)
            ?: return null
        val base = annotation.getStringArgument(CPP_TEMPLATE_BASE_ARG) ?: return null
        val prefix = annotation.getStringArgument(CPP_TEMPLATE_PREFIX_ARG) ?: return null
        val pkg = annotation.getStringArgument(CPP_TEMPLATE_PKG_ARG) ?: return null
        // Arity comes from the facade function's own type parameters
        // (`fun <T> MemScope.Box()` -> 1), so the annotation needn't carry an
        // Int argument (no getIntArgument exists on this FIR API anyway).
        val arity = symbol.typeParameterSymbols.size
        return Container(
            callableId = symbol.callableId,
            cppBase = base,
            bindingPrefix = prefix,
            arity = arity,
            bindingPackage = pkg
        )
    }

    /** True if this callable is one of the hardcoded kplusplus container facades. */
    fun isContainerFacade(callableId: CallableId): Boolean =
        containerForCallable(callableId) != null

    /** ClassId of the `@krapper.CppBinding` annotation the generator emits. */
    private val CPP_BINDING_ANNOTATION = ClassId(FqName("krapper"), Name.identifier("CppBinding"))
    private val CPP_BINDING_SPEC_ARG = Name.identifier("spec")

    /** ClassId + arg names of the `@krapper.CppTemplate` facade annotation. */
    private val CPP_TEMPLATE_ANNOTATION = ClassId(FqName("krapper"), Name.identifier("CppTemplate"))
    private val CPP_TEMPLATE_BASE_ARG = Name.identifier("base")
    private val CPP_TEMPLATE_PREFIX_ARG = Name.identifier("prefix")
    private val CPP_TEMPLATE_PKG_ARG = Name.identifier("pkg")

    /** Map a Kotlin type's classId fqname to its C++ element type, or null. */
    private fun cppElementType(kotlinClassFq: String?): String? = when (kotlinClassFq) {
        "kotlin.Int" -> "int"
        "kotlin.Long" -> "long"
        "kotlin.Double" -> "double"
        "kotlin.Float" -> "float"
        else -> null
    }

    /**
     * Resolve all Kotlin type args to C++ element strings; null if any is
     * unsupported. Primitives map via the hardcoded table; any other class
     * carrying `@krapper.CppBinding("<spec>")` (emitted by the generator on
     * every generated binding) contributes its annotation spec — so a class
     * that is itself a binding (e.g. `std.Vector__Int` with spec
     * `std::vector<int>`) can appear as an element type.
     */
    private fun elementsOf(
        typeArguments: List<FirTypeProjection>,
        session: FirSession?
    ): List<String>? {
        return typeArguments.map { proj ->
            val withVariance = proj as? FirTypeProjectionWithVariance ?: return null
            val coneType = withVariance.typeRef.coneType
            val primitive = cppElementType(coneType.classId?.asFqNameString())
            if (primitive != null) return@map primitive
            val annotated = session?.let { cppBindingSpec(coneType, it) }
            annotated ?: return null
        }
    }

    /**
     * Read `@krapper.CppBinding("<spec>")` off a type's regular-class symbol.
     * Returns the spec string, or null if the annotation is missing / the
     * type doesn't resolve to a regular class.
     */
    private fun cppBindingSpec(
        coneType: org.jetbrains.kotlin.fir.types.ConeKotlinType,
        session: FirSession
    ): String? {
        val classSymbol = coneType.toRegularClassSymbol(session) ?: return null
        val annotation = classSymbol.getAnnotationByClassId(CPP_BINDING_ANNOTATION, session)
            ?: return null
        return annotation.getStringArgument(CPP_BINDING_SPEC_ARG)
    }

    fun elementsOf(callInfo: CallInfo, session: FirSession? = null): List<String>? =
        elementsOf(callInfo.typeArguments, session)

    fun elementsOf(call: FirFunctionCall, session: FirSession? = null): List<String>? =
        elementsOf(call.typeArguments, session)

    /** Element compatibility shim for the single-arg cppVector callers. */
    fun elementOf(callInfo: CallInfo, session: FirSession? = null): String? =
        elementsOf(callInfo, session)?.singleOrNull()

    /** Element compatibility shim for the single-arg cppVector callers. */
    fun elementOf(call: FirFunctionCall, session: FirSession? = null): String? =
        elementsOf(call, session)?.singleOrNull()

    /** The C++ instantiation spec, e.g. "std::vector<int>" / "std::map<int, int>". */
    fun cppSpec(container: Container, args: List<String>): String =
        "${container.cppBase}<${args.joinToString(", ")}>"

    /** Single-arg compat: defaults to vector. */
    fun cppSpec(element: String): String = cppSpec(cppVector, listOf(element))

    /**
     * Generated Kotlin binding ClassId, e.g. std.Vector__Int / std.Map__Int__Int /
     * std.Vector__Vector_int_ (for std::vector<std::vector<int>>). Mirrors the
     * mangling krapper's WrappedKotlinType applies to each template arg:
     * split on "::" take last, capitalize first char, replace ` `<`,`>`*` with `_`.
     */
    fun bindingClassId(container: Container, args: List<String>): ClassId = ClassId(
        // std containers live in `std` (their C++ namespace), rooted under the
        // configured rootPackage when set — exactly mirroring the generator's package.
        // A user template's `@CppTemplate` facade instead carries the concrete
        // binding's package directly (already rooted by the generator).
        FqName(container.bindingPackage ?: rootPackage?.let { "$it.std" } ?: "std"),
        Name.identifier(
            container.bindingPrefix +
                args.joinToString("") { "__${mangleTemplateArg(it)}" }
        )
    )

    private fun mangleTemplateArg(spec: String): String {
        val last = spec.substringAfterLast("::")
        return last.replaceFirstChar { it.uppercase() }
            .replace(' ', '_')
            .replace('<', '_')
            .replace(',', '_')
            .replace('>', '_')
            .replace('*', '_')
    }

    /** Single-arg compat: defaults to vector. */
    fun bindingClassId(element: String): ClassId = bindingClassId(cppVector, listOf(element))
}
