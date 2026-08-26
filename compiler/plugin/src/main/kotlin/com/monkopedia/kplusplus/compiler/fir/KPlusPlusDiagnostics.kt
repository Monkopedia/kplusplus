package com.monkopedia.kplusplus.compiler.fir

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.psi.KtElement

@Suppress("detekt:ObjectPropertyNaming")
object KPlusPlusDiagnostics : KtDiagnosticsContainer() {

    // A template instantiation used in Kotlin has no generated binding yet.
    // Payload is the structured request, e.g. "kplusplus-sync-required: std::vector<int>".
    val SYNC_REQUIRED: KtDiagnosticFactory1<String> by error1<KtElement, String>()

    // The plugin could not read krapper's `binding-index.json`, so it cannot say which class a
    // container facade refines to — or whether one exists at all (#206/B2). Payload is the
    // reason, naming the path and what to do. This is deliberately NOT a sync request: a sync
    // request asserts the binding is missing, and that is precisely the claim an unreadable
    // index leaves the plugin unable to make. Before B2 this situation produced a name derived
    // by a second, independently-drifting copy of krapper's mangling and a SYNC_REQUIRED for a
    // class that may well have existed.
    val BINDING_INDEX_UNAVAILABLE: KtDiagnosticFactory1<String> by error1<KtElement, String>()

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = KPlusPlusDiagnosticRenderers
}

@Suppress("ktlint:standard:property-naming")
private object KPlusPlusDiagnosticRenderers : BaseDiagnosticRendererFactory() {
    override val MAP: KtDiagnosticFactoryToRendererMap by KtDiagnosticFactoryToRendererMap(
        "kplusplus"
    ) { map ->
        with(KPlusPlusDiagnostics) {
            map.put(SYNC_REQUIRED, "{0}", CommonRenderers.STRING)
            map.put(BINDING_INDEX_UNAVAILABLE, "{0}", CommonRenderers.STRING)
        }
    }
}
