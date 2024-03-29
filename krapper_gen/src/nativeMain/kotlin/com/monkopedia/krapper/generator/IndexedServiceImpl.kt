/*
 * Copyright 2022 Jason Monk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.AddToChild
import com.monkopedia.krapper.AddToParent
import com.monkopedia.krapper.FilterDefinition
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.IndexedService
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.MapRequest
import com.monkopedia.krapper.MapResult
import com.monkopedia.krapper.MappingService
import com.monkopedia.krapper.NoChange
import com.monkopedia.krapper.RemoveChild
import com.monkopedia.krapper.RemoveParent
import com.monkopedia.krapper.ReplaceChild
import com.monkopedia.krapper.ReplaceParent
import com.monkopedia.krapper.ResolverService
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.codegen.CppCompiler
import com.monkopedia.krapper.generator.codegen.CppWriter
import com.monkopedia.krapper.generator.codegen.DefWriter
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.HeaderWriter
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import com.monkopedia.krapper.generator.codegen.NameHandler
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.recursiveSequence
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedType
import kotlinx.cinterop.Arena
import kotlinx.coroutines.runBlocking

class IndexedServiceImpl(private val config: KrapperConfig, private val request: IndexRequest) :
    IndexedService {
    private val scope = Arena()
    private val index = createIndex(0, 0) ?: error("Failed to create Index")
    private var classes: List<ResolvedElement> = emptyList()
    private val mappings = mutableListOf<MappingService>()

    init {
        scope.defer {
            index.dispose()
        }
    }

    private val includePaths = generateIncludes(config.compiler)
    private val args: Array<String> = arrayOf("-xc++", "--std=c++14") +
        includePaths.map { "-I$it" }.toTypedArray()

    init {
        if (config.debug) {
            runBlocking {
                Log.i("Args: ${args.toList()}")
            }
        }
    }

    override suspend fun filterAndResolve(filter: FilterDefinition) {
        val resolver = scope.parseHeader(
            index,
            request.headers,
            includePaths + request.headerDirectories,
            debug = config.debug
        )
        val initialClasses = resolver.findClasses(filter.wrapperFilter())
        if (config.debug) {
            val resolvingStr = initialClasses
                .map { (it as? WrappedClass)?.type?.toString() ?: it.toString() }
                .sorted()
                .joinToString(",\n    ")
            Log.i("Resolving: [\n    $resolvingStr\n]")
        }
        classes = initialClasses.resolveAll(resolver, config.referencePolicy)
    }

    override suspend fun addMapping(mappingService: MappingService) {
        mappings.add(mappingService)
    }

    override suspend fun writeTo(output: String) {
        if (config.debug) {
            Log.i("Running mapping")
        }
        if (mappings.isNotEmpty()) {
            executeMappings()
        }
        if (config.debug) {
            val resolvedClasses = classes
                .map { (it as? ResolvedClass)?.type?.toString() ?: it.toString() }
                .sorted()
                .joinToString(",\n    ")
            Log.i("Generating for [\n    $resolvedClasses\n]")
        }
        val outputBase = File(output)
        outputBase.mkdirs()
        val namer = NameHandler()
        File(outputBase, "${config.moduleName}.h").writeText(
            CppCodeBuilder().also {
                HeaderWriter(
                    it,
                    policy = config.errorPolicy.policy
                ).generate(config.moduleName!!, request.headers, classes)
            }.toString()
        )
        val cppFile = File(outputBase, "${config.moduleName}.cc")
        cppFile.writeText(
            CppCodeBuilder().also {
                CppWriter(cppFile, it, policy = config.errorPolicy.policy).generate(
                    config.moduleName!!,
                    request.headers,
                    classes
                )
            }.toString()
        )
        val pkg = config.pkg
        File(outputBase, "${config.moduleName}.def").writeText(
            DefWriter(namer).generateDef(
                outputBase,
                "$pkg.internal",
                config.moduleName!!,
                request.headers,
                request.libraries
            )
        )
        CppCompiler(File(outputBase, "lib${config.moduleName}.a"), config.compiler).compile(
            cppFile,
            request.headers,
            request.libraries
        )
        KotlinWriter(
            "$pkg.internal",
            policy = config.errorPolicy.policy
        ).generate(
            File(outputBase, "src"),
            classes
        )
    }

    private suspend fun executeMappings() {
        val resolver = object : ResolverService {
            override suspend fun resolvedType(typeStr: String): ResolvedType {
                return toResolvedCppType(WrappedType(typeStr))
            }

            override suspend fun resolvedKotlinType(typeStr: String): ResolvedKotlinType {
                return toResolvedKotlinType(WrappedType(typeStr).kotlinType)
            }

            override suspend fun resolvedCType(typeStr: String): ResolvedCType {
                return toResolvedCType(WrappedType(typeStr).cType)
            }
        }
        val mappingsAndFilters = mappings.map {
            it.getFilter(resolver).resolveFilter() to it
        }

        classes.recursiveSequence().forEach { element ->
            for ((filter, mapper) in mappingsAndFilters) {
                if (!filter(element)) continue
                if (config.debug) {
                    Log.i("Executing mapping ($mapper) on $element")
                }

                try {
                    val results = mapper.mapElement(
                        MapRequest(
                            element.parent ?: element,
                            element.parent?.children?.indexOf(element) ?: -1
                        )
                    )
                    if (config.debug) {
                        Log.i("     --> $results")
                    }
                    for (result in results) {
                        applyResult(result, element)
                    }
                } catch (t: Throwable) {
                    Log.w(t.message + "\n" + t.stackTraceToString())
                    throw RuntimeException("Mapping failed for $element", t)
                }
            }
        }
    }

    private fun applyResult(
        result: MapResult,
        element: ResolvedElement
    ) = when (result) {
        RemoveChild -> {
            element.parent?.removeChild(element)
        }
        RemoveParent -> {
            element.parent?.let { parent ->
                parent.parent?.removeChild(parent)
            }
        }
        is AddToChild -> {
            element.addChild(result.newChild)
        }
        is AddToParent -> {
            element.parent?.addChild(result.newChild)
        }
        is ReplaceChild -> {
            element.parent?.let { parent ->
                parent.removeChild(element)
                parent.addChild(result.newChild)
            }
        }
        is ReplaceParent -> {
            element.parent?.let { parent ->
                parent.parent?.let { parentParent ->
                    parentParent.removeChild(parent)
                    parentParent.addChild(result.newChild)
                }
            }
        }
        NoChange -> {
            // Nothing to do.
        }
    }

    override suspend fun close() {
        super.close()
        index.dispose()
        scope.clear()
    }
}
