package com.monkopedia.kplusplus.compiler

import com.monkopedia.kplusplus.compiler.KPlusPlusCommandLineProcessor.Companion.BINDING_INDEX_PATH_KEY
import com.monkopedia.kplusplus.compiler.KPlusPlusCommandLineProcessor.Companion.REQUEST_MANIFEST_PATH_KEY
import com.monkopedia.kplusplus.compiler.KPlusPlusCommandLineProcessor.Companion.ROOT_PACKAGE_KEY
import com.monkopedia.kplusplus.compiler.fir.CppVectorMapping
import com.monkopedia.kplusplus.compiler.fir.RequestedManifest
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
class KPlusPlusComponentRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String
        get() = "com.monkopedia.kplusplus.compiler"

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        // Stash the manifest path so RequestedManifest (called from the FIR checker
        // without a CheckerContext path to the config) can look it up. Per-compiler-
        // daemon-lifetime state is fine because each daemon serves one project's
        // compilations and the path is per-project.
        configuration.get(REQUEST_MANIFEST_PATH_KEY)?.let { path ->
            RequestedManifest.configure(path)
        }
        // Tell the FIR mapping which root package this compilation was generated under. Since
        // #206/B2 it does not position bindings with it — it cross-checks it against the
        // rootPackage the index records, so a krapped tree from a different configuration is
        // reported rather than resolved against.
        CppVectorMapping.configureRootPackage(configuration.get(ROOT_PACKAGE_KEY))
        // Point the FIR mapping at krapper's emitted binding index (#186 B2). It is the ONLY
        // source of generated binding names since #206 — absent it, the plugin reports rather
        // than guesses.
        CppVectorMapping.configureBindingIndex(configuration.get(BINDING_INDEX_PATH_KEY))
        FirExtensionRegistrarAdapter.registerExtension(FirKPlusPlusRegistrar())
        IrGenerationExtension.registerExtension(CppVectorIrExtension())
    }
}
