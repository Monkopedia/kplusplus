package com.monkopedia.kplusplus.compiler

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
        // Tell the FIR mapping which package the generated container bindings live
        // under, so its bindingClassId lookup matches the generator's rootPackage.
        CppVectorMapping.configureRootPackage(configuration.get(ROOT_PACKAGE_KEY))
        FirExtensionRegistrarAdapter.registerExtension(FirKPlusPlusRegistrar())
        IrGenerationExtension.registerExtension(CppVectorIrExtension())
    }
}
