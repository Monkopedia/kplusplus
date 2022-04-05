/*
 * Copyright 2021 Jason Monk
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
package com.monkopedia.krapper.generator.builders

import com.monkopedia.krapper.generator.resolved_model.type.FqSymbol

inline fun KotlinCodeBuilder.importBlock(pkg: String, target: KotlinCodeBuilder) {
    addSymbol(ImportBlock(pkg, target))
}

class ImportBlock(pkg: String, private val target: CodeBuilder<KotlinFactory>) : Symbol {
    private val prefixedPkg = "$pkg."
    private val prefixLength = prefixedPkg.length

    override fun build(builder: CodeStringBuilder) {
        val fqSymbols = findFqSymbols(target.base.symbols)
        val fqNames = handleDuplicates(
            fqSymbols.flatMap { it.fqNames }.toSet()
        ).sortedBy { it.first }
        for ((fqName, importedName) in fqNames) {
            if (importedName != null) {
                ImportAs(fqName, importedName).build(builder)
            } else {
                // Root pkg, native types, skip
                if (!fqName.contains(".")) continue
                // Same pkg as file, skip import
                if (fqName.startsWith(prefixedPkg) && !fqName.substring(prefixLength)
                    .contains(".")
                ) {
                    continue
                }
                Import(fqName).build(builder)
            }
            builder.append('\n')
        }
        val mappings = fqNames.mapNotNull { item -> item.second?.let { item.first to it } }
            .toMap()
        if (mappings.isNotEmpty()) {
            fqSymbols.forEach {
                it.setNameRemap(mappings)
            }
        }
    }

    private fun handleDuplicates(fqNames: Set<String>): Collection<kotlin.Pair<String, String?>> {
        val usedNames = mutableSetOf<String>()
        val mappings = mutableListOf<kotlin.Pair<String, String?>>()
        for (fqName in fqNames) {
            val desiredName = fqName.split(".").last()
            val actualName = selectName(usedNames, fqName, desiredName)
            mappings.add(
                if (actualName != desiredName) {
                    fqName to actualName
                } else {
                    fqName to null
                }
            )
        }
        return mappings
    }

    private fun selectName(
        usedNames: MutableSet<String>,
        fqName: String,
        desiredName: String
    ): String {
        if (usedNames.add(desiredName)) {
            return desiredName
        }
        val pkgOptions = fqName.split(".").toMutableList().apply {
            removeLast()
        }
        var modifiedName = desiredName
        for (pkg in pkgOptions.reversed()) {
            modifiedName = pkg.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            } + modifiedName
            if (usedNames.add(modifiedName)) {
                return modifiedName
            }
        }
        for (i in 0 until 5) {
            modifiedName = desiredName + i
            if (usedNames.add(modifiedName)) {
                return modifiedName
            }
        }
        return error("Cannot name $fqName, too many conflicts.")
    }

    override fun toString(): String {
        return "Imports@${hashCode()}"
    }
}

private fun findFqSymbols(
    symbols: List<Symbol>,
    list: MutableList<FqSymbol> = mutableListOf(),
    processed: MutableSet<Symbol> = mutableSetOf()
): List<FqSymbol> {
    for (symbol in symbols) {
        if (symbol is FqSymbol) {
            list.add(symbol)
        }
        if (symbol is SymbolContainer) {
            if (!processed.add(symbol)) {
                throw IllegalStateException(
                    "Already processed $symbol within $processed,\n\nFound $list so far"
                )
            }
            findFqSymbols(symbol.symbols, list, processed)
        }
    }
    return list
}

inline fun extensionMethod(fullyQualified: String): Symbol {
    val list = fullyQualified.split(".")
    return extensionMethod(list.subList(0, list.size - 1).joinToString("."), list.last())
}

inline fun extensionMethod(pkg: String, method: String): Symbol {
    return object : Symbol, FqSymbol {
        private val fullyQualified = "$pkg.$method"
        private var remap: Map<String, String>? = null

        override fun setNameRemap(map: Map<String, String>) {
            remap = map
        }

        override fun build(builder: CodeStringBuilder) {
            builder.append(remap?.get(fullyQualified) ?: method)
        }

        override val fqNames: List<String>
            get() = listOf(fullyQualified)
    }
}

interface SymbolContainer {
    val symbols: List<Symbol>
}
