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
package com.monkopedia.kplusplus

import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.ErrorPolicy.LOG
import com.monkopedia.krapper.FilterDefinition
import com.monkopedia.krapper.FilterDsl
import com.monkopedia.krapper.MappingScope
import com.monkopedia.krapper.MappingService
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.ReferencePolicy.INCLUDE_MISSING
import com.monkopedia.krapper.TypeTarget
import com.monkopedia.krapper.filter
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.typedMapping
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation

open class KPlusPlusExtension(
    @Nested
    open val config: KPlusPlusConfig,
    @Inject
    open val objectFactory: ObjectFactory,
    @Nested
    open val imports: MutableList<ImportConfig> = mutableListOf()
) {
    fun config(action: Action<KPlusPlusConfig>) {
        action.execute(config)
    }

    fun import(name: String? = null, configure: Action<ImportConfig>) {
        // TODO: Migrate to named container
        val name = name ?: "import${imports.size}"
        val config = imports.find { it.name == name }
            ?: ImportConfig(
                name,
                headers = objectFactory.sourceDirectorySet(
                    "${name}Headers",
                    "Header files for kplusplus $name import"
                ),
                library = objectFactory.sourceDirectorySet(
                    "${name}Library",
                    "Library files for kplusplus $name import"
                ),
            ).also { imports.add(it) }
        configure.execute(config)
    }
}

open class ImportConfig(
    @Optional
    @Input
    open var name: String? = null,
    @Optional
    @Input
    open var compilations: MutableList<KotlinNativeCompilation>? = mutableListOf(),
    @Optional
    @Input
    open var classFilter: FilterDefinition? = null,
    @Inject
    @Input
    open val headers: SourceDirectorySet,
    @Inject
    @Input
    open val library: SourceDirectorySet,
    @Internal
    open val mappings: MutableList<MappingService> = mutableListOf()
) {

    inline fun <reified T : ResolvedElement> map(
        type: TypeTarget<T>,
        builder: MappingBuilder<T>.() -> Unit
    ) {
        mappings.add(MappingBuilder(type).also(builder).toMappingService())
    }

    inline fun classFilter(crossinline filter: FilterDsl.() -> FilterDefinition) {
        classFilter = filter(filter)
    }
}

open class MappingBuilder<T : ResolvedElement>(
    open val type: TypeTarget<T>,
    open var filterMethod: (FilterDsl.() -> FilterDefinition)? = null,
    open var mappingMethod: (suspend MappingScope.(T) -> Unit)? = null
) {
    fun toMappingService(): MappingService {
        val filterMethod = filterMethod ?: error("No filter defined for mapping")
        val mappingMethod = mappingMethod ?: error("No handler defined for mapping")
        return typedMapping(type, { filterMethod() }, { mappingMethod(it) })
    }
}

inline fun <reified T : ResolvedElement> MappingBuilder<T>.find(
    noinline filter: FilterDsl.() -> FilterDefinition
) {
    require(filterMethod == null) {
        "Cannot call find multiple times"
    }
    filterMethod = filter
}

inline fun <reified T : ResolvedElement> MappingBuilder<T>.onEach(
    noinline handler: suspend MappingScope.(T) -> Unit
) {
    require(mappingMethod == null) {
        "Cannot call onEach multiple times"
    }
    mappingMethod = handler
}

open class KPlusPlusConfig(
    @Optional
    @Input
    open var pkg: String? = null,
    @Optional
    @Input
    open var compiler: String? = null,
    @Input
    open var moduleName: String,
    @Optional
    @Input
    open var errorPolicy: ErrorPolicy = LOG,
    @Optional
    @Input
    open var referencePolicy: ReferencePolicy = INCLUDE_MISSING,
    @Optional
    @Input
    open var debug: Boolean = false
)
