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
        val fqNames = findFqSymbols(target.base.symbols).flatMap { it.fqNames }.toSet().sorted()
        for (fqName in fqNames) {
            // Root pkg, native types, skip
            if (!fqName.contains(".")) continue
            // Same pkg as file, skip import
            if (fqName.startsWith(prefixedPkg) && !fqName.substring(prefixLength).contains(".")) {
                continue
            }
            Import(fqName).build(builder)
            builder.append('\n')
        }
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

inline fun extensionMethod(pkg: String, method: String): Symbol {
    return object : Symbol, FqSymbol {
        override fun build(builder: CodeStringBuilder) {
            builder.append(method)
        }

        override val fqNames: List<String>
            get() = listOf("$pkg.$method")
    }
}

interface SymbolContainer {
    val symbols: List<Symbol>
}
