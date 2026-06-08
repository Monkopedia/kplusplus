package com.monkopedia.kplusplus.compiler.fir

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
 * generated binding is not yet on the compile classpath, emitting a structured
 * `kplusplus-sync-required: <cpp-spec>` diagnostic and recording the spec to a
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
        val present = context.session.symbolProvider
            .getClassLikeSymbolByClassId(
                CppVectorMapping.bindingClassId(container, elements)
            ) != null
        if (!present) {
            val spec = CppVectorMapping.cppSpec(container, elements)
            RequestedManifest.append(spec)
            reporter.reportOn(
                source,
                KPlusPlusDiagnostics.SYNC_REQUIRED,
                "kplusplus-sync-required: $spec",
                context
            )
        }
    }
}
