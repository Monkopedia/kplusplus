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

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = KPlusPlusDiagnosticRenderers
}

@Suppress("ktlint:standard:property-naming")
private object KPlusPlusDiagnosticRenderers : BaseDiagnosticRendererFactory() {
    override val MAP: KtDiagnosticFactoryToRendererMap by KtDiagnosticFactoryToRendererMap(
        "kplusplus"
    ) { map ->
        with(KPlusPlusDiagnostics) {
            map.put(SYNC_REQUIRED, "{0}", CommonRenderers.STRING)
        }
    }
}
