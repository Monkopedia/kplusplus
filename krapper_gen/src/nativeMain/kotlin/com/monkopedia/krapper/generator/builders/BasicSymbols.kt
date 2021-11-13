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

import com.monkopedia.krapper.generator.model.type.WrappedType

class Reference(private val arg: LocalVar) : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append(arg.name)
    }
}

inline val LocalVar.reference: Symbol
    get() = Reference(this)

class Dereference(private val arg: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(arg)
    override fun build(builder: CodeStringBuilder) {
        builder.append("*(")
        arg.build(builder)
        builder.append(")")
    }
}

inline val LocalVar.dereference: Symbol
    get() = Dereference(reference)

inline val Symbol.dereference: Symbol
    get() = Dereference(this)

class Return(private val s: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(s)
    override fun build(builder: CodeStringBuilder) {
        builder.append("return ")
        s.build(builder)
    }
}

class Delete(private val s: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(s)
    override fun build(builder: CodeStringBuilder) {
        builder.append("delete ")
        s.build(builder)
    }
}

class New(private val s: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(s)
    override fun build(builder: CodeStringBuilder) {
        builder.append("new ")
        s.build(builder)
    }
}

class Call(private val name: Symbol, private vararg val args: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(name) + args

    constructor(name: String, vararg args: Symbol) : this(Raw(name), *args)

    override fun build(builder: CodeStringBuilder) {
        name.build(builder)
        builder.append('(')
        args.forEachIndexed { index, arg ->
            if (index != 0) {
                builder.append(", ")
            }
            arg.build(builder)
        }
        builder.append(')')
    }
}

class Dot(private val first: Symbol, private val second: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(first, second)
    override fun build(builder: CodeStringBuilder) {
        first.build(builder)
        builder.append('.')
        second.build(builder)
    }
}

class Arrow(private val first: Symbol, private val second: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(first, second)
    override fun build(builder: CodeStringBuilder) {
        first.build(builder)
        builder.append("->")
        second.build(builder)
    }
}

class Assign(private val first: Symbol, private val second: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(first, second)
    override fun build(builder: CodeStringBuilder) {
        first.build(builder)
        builder.append(" = ")
        second.build(builder)
    }
}

class Op(private val operand: String, private val first: Symbol, private val second: Symbol) :
    Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(first, second)
    override fun build(builder: CodeStringBuilder) {
        first.build(builder)
        builder.append(" $operand ")
        second.build(builder)
    }
}

inline fun Symbol.op(operand: String, other: Symbol): Symbol = Op(operand, this, other)

inline infix fun Symbol.dot(other: Symbol): Symbol = Dot(this, other)
inline infix fun Symbol.arrow(other: Symbol): Symbol = Arrow(this, other)
inline infix fun Symbol.assign(other: Symbol): Symbol = Assign(this, other)

class Raw(val content: String) : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append(content)
    }
}

class RawCast(val content: String, val target: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(target)

    override fun build(builder: CodeStringBuilder) {
        builder.append('(')
        builder.append(content)
        builder.append(')')
        target.build(builder)
    }
}