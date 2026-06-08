package com.monkopedia.kplusplus.compiler

import com.monkopedia.kplusplus.compiler.fir.CppVectorCallRefinement
import com.monkopedia.kplusplus.compiler.fir.KPlusPlusFirCheckersComponent
import org.jetbrains.kotlin.fir.extensions.FirExtensionApiInternals
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class FirKPlusPlusRegistrar : FirExtensionRegistrar() {
    @OptIn(FirExtensionApiInternals::class)
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::KPlusPlusFirCheckersComponent
        +::CppVectorCallRefinement
    }
}
