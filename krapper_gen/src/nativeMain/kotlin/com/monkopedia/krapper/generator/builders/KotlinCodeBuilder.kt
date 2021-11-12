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

import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_OPAQUE_POINTER
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.MEM_SCOPE
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.PAIR
import com.monkopedia.krapper.generator.model.WrappedKotlinType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.VOID
import com.monkopedia.krapper.generator.model.fullyQualifiedType
import com.monkopedia.krapper.generator.model.type.kotlinType
import com.monkopedia.krapper.generator.model.typedWith

typealias KotlinCodeBuilder = CodeBuilder<KotlinFactory>

fun KotlinCodeBuilder(): KotlinCodeBuilder {
    return CodeBuilderBase(KotlinFactory(), addSemis = false)
}

class KotlinFactory : LangFactory {
    override fun define(name: String, type: WrappedType, initializer: Symbol?): LocalVar {
        return KotlinLocalVar(name, type.kotlinType, initializer)
    }

    override fun funSig(name: String, retType: Symbol?, args: List<LocalVar>): Symbol {
        return KotlinFunctionSig(name, retType ?: KotlinType(VOID), args)
    }

    override fun createType(type: WrappedType): Symbol = KotlinType(type)

    fun define(name: String, type: WrappedKotlinType, initializer: Symbol?): LocalVar {
        return KotlinLocalVar(name, type, initializer)
    }

    companion object {
        const val C_OPAQUE_POINTER = "kotlinx.cinterop.COpaquePointer"
        const val C_POINTER = "kotlinx.cinterop.CPointer"
        const val C_VALUES_REF = "kotlinx.cinterop.CValuesRef"
        const val MEM_SCOPE = "kotlinx.cinterop.MemScope"
        const val PAIR = "kotlin.Pair"
    }
}

class QDot(private val first: Symbol, private val second: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(first, second)
    override fun build(builder: CodeStringBuilder) {
        first.build(builder)
        builder.append("?.")
        second.build(builder)
    }
}

inline infix fun Symbol.qdot(other: Symbol): Symbol = QDot(this, other)

object Private : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("private")
    }

    override fun toString(): String {
        return "private"
    }
}

object Public : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("public")
    }

    override fun toString(): String {
        return "public"
    }
}

object Internal : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("internal")
    }

    override fun toString(): String {
        return "internal"
    }
}

object Defer : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("defer {\n")
    }

    override fun toString(): String {
        return "defer"
    }
}

object CompanionStart : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("companion object {\n")
    }

    override fun toString(): String {
        return "companion"
    }
}

object EndClass : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append('}')
    }

    override fun toString(): String {
        return "end block"
    }
}

object Getter : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("get() {\n")
    }

    override fun toString(): String {
        return "getter"
    }
}

object Setter : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("set(value) {\n")
    }

    override fun toString(): String {
        return "setter"
    }
}

inline fun KotlinCodeBuilder.getter(
    builder: KotlinCodeBuilder.() -> Unit
) = block(this, Getter, EndClass, builder)

inline fun KotlinCodeBuilder.setter(
    builder: KotlinCodeBuilder.(LocalVar) -> Unit
) = block(this, Setter, EndClass) {
    builder(object : LocalVar {
        override val name: String
            get() = "value"

        override fun build(builder: CodeStringBuilder) {
        }
    })
}

inline fun KotlinCodeBuilder.companion(
    builder: KotlinCodeBuilder.() -> Unit
) = block(CompanionStart, EndClass, builder)

inline fun KotlinCodeBuilder.defer(
    builder: KotlinCodeBuilder.() -> Unit
) = block(Defer, EndClass, builder)

fun KotlinCodeBuilder.define(
    desiredName: String,
    type: WrappedKotlinType,
    initializer: Symbol? = null
): LocalVar {
    val name = scope.allocateName(desiredName)
    return factory.define(name, type, initializer)
}

inline fun KotlinCodeBuilder.cls(
    name: Symbol,
    constructorVisibility: Symbol = Private,
    builder: KotlinCodeBuilder.(LocalVar) -> Unit
) {
    functionScope {
        val args = listOf(
            define(
                "source",
                fullyQualifiedType(PAIR).typedWith(
                    listOf(
                        fullyQualifiedType(C_OPAQUE_POINTER),
                        fullyQualifiedType(MEM_SCOPE)
                    )
                )
            ).also { (it as? KotlinLocalVar)?.isVal = true }
        )
        block(ClassStartSymbol(name, constructorVisibility, args), EndClass) {
            builder(args[0])
        }
    }
}

class ClassStartSymbol(
    val clsName: Symbol,
    val constructorVisibility: Symbol,
    val constructorArgs: List<Symbol>
) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(clsName, constructorVisibility) + constructorArgs
    override fun build(builder: CodeStringBuilder) {
        builder.append("value class ")
        clsName.build(builder)
        builder.append(' ')
        constructorVisibility.build(builder)
        builder.append(" constructor(")
        constructorArgs.forEachIndexed { index, symbol ->
            if (index != 0) {
                builder.append(", ")
            }
            symbol.build(builder)
        }
        builder.append(") {\n")
    }

    override fun toString(): String {
        return buildString {
            append("class@${this@ClassStartSymbol.hashCode()} ")
            append(clsName)
            append(' ')
            append(constructorVisibility)
            append(" constructor(")
            constructorArgs.forEachIndexed { index, symbol ->
                if (index != 0) {
                    append(", ")
                }
                append(symbol)
            }
            append(")")
        }
    }
}

class KotlinFunctionSig(
    private val name: String,
    private val retType: Symbol,
    private val args: List<LocalVar>
) : Symbol, SymbolContainer {
    var receiver: Symbol? = null

    override val symbols: List<Symbol>
        get() = listOfNotNull(receiver) + retType + args

    override fun build(builder: CodeStringBuilder) {
        builder.append("fun ")
        receiver?.let {
            it.build(builder)
            builder.append(".")
        }
        builder.append(name)
        builder.append('(')
        for ((index, arg) in args.withIndex()) {
            if (index != 0) {
                builder.append(", ")
            }
            arg.build(builder)
        }
        builder.append("): ")
        retType.build(builder)
    }

    override fun toString(): String {
        return buildString {
            append("fun ")
            receiver?.let {
                append(it)
                append(".")
            }
            append(name)
            append('(')
            for ((index, arg) in args.withIndex()) {
                if (index != 0) {
                    append(", ")
                }
                append(arg)
            }
            append("): ")
            append(retType)
        }
    }
}

class KotlinLocalVar(
    override val name: String,
    val type: WrappedKotlinType,
    private val initializer: Symbol?
) : LocalVar, SymbolContainer {
    var isVal: Boolean? = false
    private val typeSymbol = KotlinType(type)
    override val symbols: List<Symbol>
        get() = listOfNotNull(typeSymbol, initializer)
    override fun build(builder: CodeStringBuilder) {
        isVal?.let { isVal ->
            builder.append(if (isVal) "val " else "var ")
        }
        builder.append(name)
        builder.append(": ")
        typeSymbol.build(builder)
        initializer?.let {
            builder.append(" = ")
            it.build(builder)
        }
    }

    override fun toString(): String {
        return buildString {
            isVal?.let { isVal ->
                append(if (isVal) "val@${hashCode()} " else "var@${hashCode()} ")
            }
            append(name)
            append(": ")
            append(typeSymbol)
            initializer?.let {
                append(" = ")
                append(it)
            }
        }
    }
}

class KotlinType(
    private val typeStr: String,
    override val fqNames: List<String>
) : Symbol, FqSymbol {
    constructor(type: WrappedType) : this(type.kotlinType)
    constructor(type: WrappedKotlinType) : this(type.name, type.fullyQualified)

    override fun build(builder: CodeStringBuilder) {
        builder.append(typeStr)
    }

    override fun toString(): String {
        return "Type@${hashCode()}:[$typeStr]"
    }
}

inline fun KotlinCodeBuilder.import(target: String) {
    addSymbol(Import(target))
}

inline fun KotlinCodeBuilder.pkg(target: String) {
    addSymbol(Package(target))
}

inline fun KotlinCodeBuilder.inline(build: KotlinCodeBuilder.() -> Unit) {
    val builder = KotlinCodeBuilder().also(build)
    (builder as? CodeBuilderBase<KotlinFactory>)?.symbols?.forEach {
        +inline(it)
    }
}
inline fun inline(target: Symbol): Symbol =
    Inline(target)

class Inline(private val target: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(target)
    override fun build(builder: CodeStringBuilder) {
        builder.append("inline ")
        target.build(builder)
    }

    override fun toString(): String {
        return "inline[$target]"
    }
}

inline fun KotlinCodeBuilder.operator(build: KotlinCodeBuilder.() -> Unit) {
    val builder = KotlinCodeBuilder().also(build)
    (builder as? CodeBuilderBase<KotlinFactory>)?.symbols?.forEach {
        +operator(it)
    }
}

inline fun operator(target: Symbol): Symbol =
    Operator(target)

class Operator(private val target: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(target)
    override fun build(builder: CodeStringBuilder) {
        builder.append("operator ")
        target.build(builder)
    }
}

inline fun KotlinCodeBuilder.infix(build: KotlinCodeBuilder.() -> Unit) {
    val builder = KotlinCodeBuilder().also(build)
    (builder as? CodeBuilderBase<KotlinFactory>)?.symbols?.forEach {
        +infix(it)
    }
}

inline fun infix(target: Symbol): Symbol =
    Infix(target)

class Infix(private val target: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(target)
    override fun build(builder: CodeStringBuilder) {
        builder.append("infix ")
        target.build(builder)
    }
}

class Import(private val target: String) : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("import ")
        builder.append(target)
    }
}

class Package(private val target: String) : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("package ")
        builder.append(target)
        builder.append('\n')
    }

    override fun toString(): String {
        return "pkg:$target"
    }
}

class KotlinFunctionSymbol(functionBuilder: KotlinCodeBuilder) :
    FunctionSymbol<KotlinFactory>(functionBuilder) {
    var receiver: Symbol? = null
    val thiz: LocalVar
        get() = object : LocalVar {
            override val name: String
                get() = "this"

            override fun build(builder: CodeStringBuilder) {
                // Not actually in symbol tree.
            }
        }

    override fun init() {
        super.init()
        (signature as? KotlinFunctionSig)?.receiver = receiver
    }

    override fun toString(): String {
        return "Func@${hashCode()}"
    }
}

fun KotlinCodeBuilder.fqType(fq: String): Symbol = fqType(WrappedKotlinType(fq))
fun KotlinCodeBuilder.fqType(kotlinType: WrappedKotlinType): Symbol = KotlinType(kotlinType)

inline fun KotlinCodeBuilder.extensionFunction(
    functionBuilder: KotlinFunctionSymbol.() -> Unit
): Symbol {
    functionScope {
        val builder = KotlinFunctionSymbol(this)
        builder.functionBuilder()
        builder.init()
        return@extensionFunction +builder.symbol
    }
}

class Elvis(private val first: Symbol, private val second: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(first, second)
    override fun build(builder: CodeStringBuilder) {
        builder.append('(')
        first.build(builder)
        builder.append(" ?: ")
        second.build(builder)
        builder.append(')')
    }
}

inline infix fun Symbol.elvis(other: Symbol): Symbol = Elvis(this, other)

class Pair(private val first: Symbol, private val second: Symbol) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(first, second)
    override fun build(builder: CodeStringBuilder) {
        builder.append('(')
        first.build(builder)
        builder.append(" to ")
        second.build(builder)
        builder.append(')')
    }
}

inline infix fun Symbol.pairedTo(other: Symbol): Symbol = Pair(this, other)

class StringConstant(private val str: String) : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append('"')
        builder.append(str)
        builder.append('"')
    }
}

inline val String.symbol: Symbol
    get() = StringConstant(this)

var LocalVar.isVal: Boolean
    get() =
        (this as? KotlinLocalVar)?.isVal ?: error("Not a kotlin var, mixing source builders?")
    set(value) {
        val thiz = (this as? KotlinLocalVar) ?: error("Not a kotlin var, mixing source builders?")
        thiz.isVal = value
    }
