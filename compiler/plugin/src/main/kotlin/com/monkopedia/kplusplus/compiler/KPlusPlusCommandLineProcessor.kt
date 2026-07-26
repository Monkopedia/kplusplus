package com.monkopedia.kplusplus.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

/**
 * Routes per-compilation kplusplus subplugin options from the Gradle subplugin
 * into the FIR/IR pipeline.
 *
 * v0 hardcoded the manifest path to the in-build slice consumer; v1 plumbs it
 * here so any external consumer can use the compiler plugin.
 */
@OptIn(ExperimentalCompilerApi::class)
class KPlusPlusCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        REQUEST_MANIFEST_PATH_OPTION,
        ROOT_PACKAGE_OPTION
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        when (option.optionName) {
            REQUEST_MANIFEST_PATH_OPTION.optionName ->
                configuration.put(REQUEST_MANIFEST_PATH_KEY, value)
            ROOT_PACKAGE_OPTION.optionName ->
                configuration.put(ROOT_PACKAGE_KEY, value)
            else -> error("Unexpected kplusplus compiler plugin option: ${option.optionName}")
        }
    }

    companion object {
        const val PLUGIN_ID = "com.monkopedia.kplusplus.compiler"

        val REQUEST_MANIFEST_PATH_OPTION = CliOption(
            optionName = "requestManifestPath",
            valueDescription = "<path>",
            description = "Absolute path of the per-compilation requested-instantiations " +
                "manifest the FIR SYNC_REQUIRED checker appends to.",
            required = false,
            allowMultipleOccurrences = false
        )

        val REQUEST_MANIFEST_PATH_KEY: CompilerConfigurationKey<String> =
            CompilerConfigurationKey.create("kplusplus requested-instantiations manifest path")

        val ROOT_PACKAGE_OPTION = CliOption(
            optionName = "rootPackage",
            valueDescription = "<package>",
            description = "Root Kotlin package the generated bindings are nested under " +
                "(e.g. container bindings land in <rootPackage>.std). Must match the " +
                "krapper --root-package used to generate them. Omit for the historical " +
                "layout (std containers in `std`).",
            required = false,
            allowMultipleOccurrences = false
        )

        val ROOT_PACKAGE_KEY: CompilerConfigurationKey<String> =
            CompilerConfigurationKey.create("kplusplus generated-binding root package")
    }
}
