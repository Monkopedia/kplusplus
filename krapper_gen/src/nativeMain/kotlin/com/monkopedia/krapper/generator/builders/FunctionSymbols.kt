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

import com.monkopedia.krapper.generator.model.WrappedTypeReference

inline fun <T : LangFactory> CodeBuilder<T>.funSig(
    name: String,
    retType: Symbol?,
    args: List<LocalVar>
): Symbol =
    factory.funSig(name, retType, args)

sealed class FunctionBuilder<T : LangFactory>(
    var name: String? = null,
    var retType: Symbol? = null,
    val functionBuilder: CodeBuilder<T>
) {
    protected val args = mutableListOf<LocalVar>()
    abstract val body: CodeBuilder<T>?

    inline fun body(block: BodyBuilder<T>) {
        body!!.apply(block)
    }

    fun define(name: String, type: WrappedTypeReference): LocalVar {
        return functionBuilder.define(name, type).also(args::add).also {
            (it as? KotlinLocalVar)?.isVal = null
        }
    }
}

private object EndFunction : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append('}')
    }
}

open class FunctionSymbol<T : LangFactory>(functionBuilder: CodeBuilder<T>) :
    FunctionBuilder<T>(functionBuilder = functionBuilder), Symbol, SymbolContainer {
    private val block = BlockSymbol(functionBuilder, this, EndFunction)
    lateinit var signature: Symbol
    val symbol: Symbol
        get() = block
    override val body: CodeBuilder<T>
        get() = block

    override val symbols: List<Symbol>
        get() = listOf(signature)

    open fun init() {
        signature = functionBuilder.funSig(name ?: error("Name was not specified"), retType, args)
    }

    override fun build(builder: CodeStringBuilder) {
        signature.build(builder)
        builder.append(" {\n")
    }

    override fun toString(): String {
        return signature.toString()
    }
}

inline fun <T : LangFactory> CodeBuilder<T>.function(
    functionBuilder: FunctionBuilder<T>.() -> Unit
): Symbol {
    functionScope {
        val builder = FunctionSymbol(this)
        builder.functionBuilder()
        builder.init()
        return@function +builder.symbol
    }
}

class FunctionDeclarationSymbol<T : LangFactory>(functionBuilder: CodeBuilder<T>) :
    FunctionBuilder<T>(functionBuilder = functionBuilder) {
    override val body: CodeBuilder<T>?
        get() = null

    fun init(): Symbol {
        return functionBuilder.funSig(name ?: error("Name was not specified"), retType, args)
    }
}

inline fun <T : LangFactory> CodeBuilder<T>.functionDeclaration(
    functionBuilder: FunctionBuilder<T>.() -> Unit
): Symbol {
    functionScope {
        val builder = FunctionDeclarationSymbol(this)
        builder.functionBuilder()
        return@functionDeclaration +builder.init()
    }
}
