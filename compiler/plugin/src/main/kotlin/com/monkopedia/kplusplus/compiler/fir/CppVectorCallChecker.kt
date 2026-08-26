package com.monkopedia.kplusplus.compiler.fir

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.references.toResolvedFunctionSymbol
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol

/**
 * Fires on container-facade calls (`cppVector<T>()`, `cppMap<K,V>()`, …) whose
 * generated binding krapper has not emitted, according to the `binding-index.json` krapper
 * wrote alongside the generated tree (#206). Emits a structured
 * `kplusplus-sync-required: <cpp-spec>` diagnostic and records the spec to a
 * manifest the sync task reads. The transparent call site is itself the sync
 * trigger — no annotation needed. Once the binding exists,
 * [CppVectorCallRefinement] types the call and this checker stays silent.
 */
internal object CppVectorCallChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val symbol = expression.calleeReference.toResolvedFunctionSymbol()
            as? FirNamedFunctionSymbol ?: return
        val container = CppVectorMapping.containerForCallable(symbol, context.session) ?: return
        val source = expression.source ?: return
        val elements = CppVectorMapping.elementsOf(expression, context.session)
        if (elements == null || elements.size != container.arity) {
            reporter.reportOn(
                source,
                KPlusPlusDiagnostics.SYNC_REQUIRED,
                "${symbol.callableId.callableName} type arguments are not supported " +
                    "C++ element types",
                context
            )
            return
        }
        when (val resolution = CppVectorMapping.resolveBinding(container, elements)) {
            is BindingResolution.Resolved -> {
                // krapper says it emitted this binding, but the class is not on the compile
                // classpath — a krapped tree that generated it and a compilation that cannot
                // see it. Still a sync request: re-running the generator is what reconciles
                // the two.
                val present = context.session.symbolProvider
                    .getClassLikeSymbolByClassId(resolution.classId) != null
                if (!present) requestSync(resolution.spec, source)
            }
            // The index is krapper's own account of what it wrote, so "not listed" is a
            // definitive "not generated" — exactly the case SYNC_REQUIRED exists for.
            is BindingResolution.NotGenerated -> requestSync(resolution.spec, source)
            // No index: the plugin cannot tell whether the binding exists, so it must not
            // claim one is missing. Deliberately does NOT append to the manifest — recording a
            // sync request asserts a fact the plugin does not have.
            is BindingResolution.NoIndex -> reporter.reportOn(
                source,
                KPlusPlusDiagnostics.BINDING_INDEX_UNAVAILABLE,
                "kplusplus cannot resolve ${resolution.spec}: ${resolution.reason}",
                context
            )
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun requestSync(spec: String, source: KtSourceElement) {
        RequestedManifest.append(spec)
        reporter.reportOn(
            source,
            KPlusPlusDiagnostics.SYNC_REQUIRED,
            "kplusplus-sync-required: $spec",
            context
        )
    }
}
