package com.monkopedia.kplusplus.compiler.fir

import java.io.File
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
 * Binding names are NOT computed here. `resolveBinding` looks the C++ spec up in krapper's
 * emitted `binding-index.json` and uses the ClassId krapper itself recorded
 * (`std::vector<int>` -> `std.Vector__Int`) — see [BindingIndexReader] and #206.
 *
 * The C++ element type table is hardcoded for primitives; a later step
 * derives it from the generator (and per-binding `@CppBinding` annotations so
 * Kotlin classes that are themselves bindings can appear as element types).
 */
internal object CppVectorMapping {

    // #206/B2 removed `bindingPrefix` and `bindingPackage`. They existed only to REBUILD the
    // generated class's name and package, which the plugin no longer does — `resolveBinding`
    // reads both out of krapper's own `binding-index.json`. What is left is what the plugin
    // genuinely knows: which callable is the facade, how to spell the C++ instantiation, and
    // how many type arguments it takes.
    data class Container(
        val callableId: CallableId,
        val cppBase: String,
        val arity: Int
    )

    // Neutral facade package (the eventual kplusplus runtime-lib package). The
    // cppVector/cppMap entry-point functions must be declared here for the plugin
    // to recognize them — previously hardcoded to the slice demo's package.
    private val FACADE_PACKAGE = FqName("com.monkopedia.kplusplus")

    val cppVector = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppVector")),
        cppBase = "std::vector",
        arity = 1
    )

    val cppMap = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppMap")),
        cppBase = "std::map",
        arity = 2
    )

    val cppPair = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppPair")),
        cppBase = "std::pair",
        arity = 2
    )

    val cppUniquePtr = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppUniquePtr")),
        cppBase = "std::unique_ptr",
        arity = 1
    )

    val cppUnorderedMap = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppUnorderedMap")),
        cppBase = "std::unordered_map",
        arity = 2
    )

    val cppUnorderedSet = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppUnorderedSet")),
        cppBase = "std::unordered_set",
        arity = 1
    )

    val cppSet = Container(
        callableId = CallableId(FACADE_PACKAGE, Name.identifier("cppSet")),
        cppBase = "std::set",
        arity = 1
    )

    private val containers =
        listOf(cppVector, cppMap, cppPair, cppUniquePtr, cppUnorderedMap, cppUnorderedSet, cppSet)

    // The configured root package (from the `rootPackage` SubpluginOption), or null for the
    // historical layout. Since #206/B2 this NO LONGER positions the binding — the classId comes
    // verbatim out of krapper's index — it is a CROSS-CHECK: an index generated under a
    // different rootPackage does not describe this compilation, and saying so beats resolving
    // against it. Set once per compilation by the component registrar; compiler-daemon-lifetime
    // state is fine (each daemon serves one project's compilations).
    private var rootPackage: String? = null

    // Path of krapper's `binding-index.json` (the `bindingIndexPath` SubpluginOption), and the
    // parsed index cached against the file's identity so a sync inside the same daemon session
    // is picked up rather than served stale.
    private var bindingIndexPath: String? = null
    private var cachedLoad: BindingIndexLoad? = null
    private var cachedKey: String? = null

    fun configureRootPackage(pkg: String?) {
        rootPackage = pkg
        cachedLoad = null
    }

    fun configureBindingIndex(path: String?) {
        bindingIndexPath = path
        cachedLoad = null
    }

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
        // Only `base` is read: it is what spells the C++ instantiation the index is keyed on.
        // The annotation's `prefix`/`pkg` describe the NAME krapper gave the generated class,
        // and since #206/B2 that name is taken from the index rather than reassembled here.
        val base = annotation.getStringArgument(CPP_TEMPLATE_BASE_ARG) ?: return null
        // Arity comes from the facade function's own type parameters
        // (`fun <T> MemScope.Box()` -> 1), so the annotation needn't carry an
        // Int argument (no getIntArgument exists on this FIR API anyway).
        val arity = symbol.typeParameterSymbols.size
        return Container(
            callableId = symbol.callableId,
            cppBase = base,
            arity = arity
        )
    }

    /** True if this callable is one of the hardcoded kplusplus container facades. */
    fun isContainerFacade(callableId: CallableId): Boolean =
        containerForCallable(callableId) != null

    /** ClassId of the `@krapper.CppBinding` annotation the generator emits. */
    private val CPP_BINDING_ANNOTATION = ClassId(FqName("krapper"), Name.identifier("CppBinding"))
    private val CPP_BINDING_SPEC_ARG = Name.identifier("spec")

    /** ClassId + arg name of the `@krapper.CppTemplate` facade annotation. */
    private val CPP_TEMPLATE_ANNOTATION = ClassId(FqName("krapper"), Name.identifier("CppTemplate"))
    private val CPP_TEMPLATE_BASE_ARG = Name.identifier("base")

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
     * Which generated binding a facade call refines to — **read out of krapper's
     * `binding-index.json`, never recomputed** (#186 brick B2, closing #206).
     *
     * There used to be a `mangleTemplateArg` here that reimplemented krapper's
     * `String.mangleToIdentifier()` so the plugin could guess the class name krapper would
     * emit. Two implementations of one rule, in two builds that cannot share code — and they
     * had already drifted on `*` (#206: krapper emits `std.Vector__CXXBaseSpecifier_P`, the
     * copy looked up `std.Vector__CXXBaseSpecifier_`, which is nothing). B1 shipped the index
     * in v0.3.6, so the guess is gone: a spec the index lists resolves to the exact name
     * krapper wrote, and a spec it does not list is *definitively* not generated — that is an
     * answer, not a naming failure, and it is the SYNC_REQUIRED the checker already reports.
     *
     * Deliberately there is NO derivation fallback. A fallback that can be wrong is worse than
     * one that reports it cannot answer; the whole substance of #206 is that the wrong answer
     * was silent. When no index can be read the result is [BindingResolution.NoIndex], which
     * the checker turns into a diagnostic naming the path and what to do about it.
     */
    fun resolveBinding(container: Container, args: List<String>): BindingResolution =
        resolveSpec(cppSpec(container, args))

    /**
     * [resolveBinding] keyed directly on the C++ spec, which is how the index is keyed.
     *
     * krapper spells the specs it indexes exactly as [cppSpec] builds them — verified against
     * the emitted artifact: `std::map<int, int>` (with the space), `std::vector<Thing*>`,
     * `Box<Box<int>>`, `std::vector<std::__cxx11::basic_string<char>>`.
     */
    fun resolveSpec(spec: String): BindingResolution {
        val load = bindingIndex()
        if (load is BindingIndexLoad.Unavailable) {
            return BindingResolution.NoIndex(spec, load.reason)
        }
        val index = (load as BindingIndexLoad.Loaded).index
        val classId = index.classIdFor(spec) ?: return BindingResolution.NotGenerated(spec)
        return BindingResolution.Resolved(spec, classId.toClassId())
    }

    /**
     * The index, loaded at most once per (path, size, mtime).
     *
     * Re-stat rather than cache-forever: the sync task rewrites the index and the SAME compiler
     * daemon then compiles against it, so a load pinned for the daemon's lifetime would serve
     * the previous generation's answers — the exact staleness class §5 D3 calls a determinism
     * bug.
     */
    private fun bindingIndex(): BindingIndexLoad {
        val path = bindingIndexPath
        val file = path?.let(::File)
        // Path AND identity: keying on (mtime, size) alone would let two different index files
        // that happen to agree on both share a cache entry.
        val key = "$path|${file?.lastModified()}|${file?.length()}"
        cachedLoad?.let { if (key == cachedKey) return it }
        val load = BindingIndexReader.load(path).let(::rejectIfDifferentlyRooted)
        cachedLoad = load
        cachedKey = key
        return load
    }

    /**
     * An index generated under a different `rootPackage` describes a different generation.
     *
     * This is the drift detector G5 was reaching for, in the only form that survives deleting
     * the second implementation: plugin and tool are configured from ONE value (the Gradle
     * plugin passes `KPlusPlusExtension.rootPackage` to both), so a disagreement means the
     * krapped tree on disk predates a config change. Resolving against it would hand back
     * classIds for packages this compilation does not have.
     */
    private fun rejectIfDifferentlyRooted(load: BindingIndexLoad): BindingIndexLoad {
        if (load !is BindingIndexLoad.Loaded) return load
        val indexRoot = load.index.rootPackage
        if (indexRoot == rootPackage) return load
        return BindingIndexLoad.Unavailable(
            "${load.index.path} was generated with rootPackage=${indexRoot ?: "<unset>"} but " +
                "this compilation is configured with rootPackage=${rootPackage ?: "<unset>"}; " +
                "re-run the module's `kplusplusSync` task so the generated tree and the " +
                "compile agree"
        )
    }

    /** `std.Vector__Int` -> ClassId(std, Vector__Int). Bindings krapper emits are top-level. */
    private fun String.toClassId(): ClassId = ClassId(
        FqName(substringBeforeLast('.', missingDelimiterValue = "")),
        Name.identifier(substringAfterLast('.'))
    )
}

/** The answer [CppVectorMapping.resolveSpec] gives about one C++ instantiation. */
internal sealed interface BindingResolution {

    /** The C++ spec that was looked up, in krapper's spelling. */
    val spec: String

    /** krapper emitted [classId] for [spec]; the name is its own, not a reconstruction. */
    data class Resolved(override val spec: String, val classId: ClassId) : BindingResolution

    /** The index is readable and does not list [spec]: krapper has generated no binding yet. */
    data class NotGenerated(override val spec: String) : BindingResolution

    /**
     * No usable index, so the plugin cannot say whether a binding exists. [reason] is
     * user-facing: it names the path and what to do.
     */
    data class NoIndex(override val spec: String, val reason: String) : BindingResolution
}
