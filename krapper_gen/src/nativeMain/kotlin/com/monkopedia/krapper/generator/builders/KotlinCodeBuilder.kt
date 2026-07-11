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
package com.monkopedia.krapper.generator.builders

import com.monkopedia.krapper.generator.resolvedmodel.type.FqSymbol
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedType.Companion.VOID
import com.monkopedia.krapper.generator.resolvedmodel.type.fullyQualifiedType

typealias KotlinCodeBuilder = CodeBuilder<KotlinFactory>

fun KotlinCodeBuilder(rootScope: Scope<KotlinFactory> = Scope()): KotlinCodeBuilder =
    CodeBuilderBase(KotlinFactory(), rootScope, addSemis = false)

// Kotlin HARD keywords — illegal as a bare identifier, so a C++ name that collides with one
// (e.g. a parameter literally named `val`, as on clang::ASTUnit::setOwnsRemappedFileBuffers)
// must be back-tick-escaped or the generated Kotlin won't parse. Soft/modifier keywords
// (value, data, in-other-positions) are fine bare and are left alone.
private val KOTLIN_HARD_KEYWORDS = setOf(
    "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in",
    "interface", "is", "null", "object", "package", "return", "super", "this", "throw",
    "true", "try", "typealias", "typeof", "val", "var", "when", "while"
)

internal fun String.escapeKotlinKeyword(): String =
    if (this in KOTLIN_HARD_KEYWORDS) "`$this`" else this

class KotlinFactory : LangFactory {
    override fun define(
        name: String,
        type: ResolvedType,
        initializer: Symbol?,
        constructorArgs: List<Symbol>?
    ): LocalVar {
        require(constructorArgs == null) {
            "Constructor args not supported for kotlin"
        }
        return KotlinLocalVar(
            name.escapeKotlinKeyword(),
            (type as? ResolvedKotlinType) ?: (type as? ResolvedCppType)?.kotlinType
                ?: error("Cannot define $type in kotlin"),
            initializer
        )
    }

    override fun funSig(name: String, retType: Symbol?, args: List<LocalVar>): Symbol =
        KotlinFunctionSig(name, retType ?: KotlinType(VOID), args)

    override fun createType(type: ResolvedType): Symbol = KotlinType(type)

    fun define(name: String, type: ResolvedKotlinType, initializer: Symbol?): LocalVar =
        KotlinLocalVar(name.escapeKotlinKeyword(), type, initializer)

    companion object {
        const val C_FUNCTION = "kotlinx.cinterop.CFunction"
        const val C_OPAQUE_POINTER = "kotlinx.cinterop.COpaquePointer"
        const val C_POINTER = "kotlinx.cinterop.CPointer"
        const val C_POINTER_VAR = "kotlinx.cinterop.CPointerVar"
        const val C_VALUES_REF = "kotlinx.cinterop.CValuesRef"
        const val MEM_SCOPE = "kotlinx.cinterop.MemScope"
        const val PAIR = "kotlin.Pair"
        const val STABLE_REF = "kotlinx.cinterop.asStableRef"
        const val STATIC_C_FUNCTION = "kotlinx.cinterop.staticCFunction"
        const val STABLE_REF_CREATE = "kotlinx.cinterop.StableRef.Companion.create"
    }
}

class QDot(private val first: Symbol, private val second: Symbol) :
    Symbol,
    SymbolContainer {
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

    override fun toString(): String = "private"
}

object Public : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("public")
    }

    override fun toString(): String = "public"
}

object Internal : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("internal")
    }

    override fun toString(): String = "internal"
}

object Defer : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("defer {\n")
    }

    override fun toString(): String = "defer"
}

object CompanionStart : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("companion object {\n")
    }

    override fun toString(): String = "companion"
}

object EndClass : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append('}')
    }

    override fun toString(): String = "end block"
}

object Getter : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("get() {\n")
    }

    override fun toString(): String = "getter"
}

object Setter : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("set(value) {\n")
    }

    override fun toString(): String = "setter"
}

inline fun KotlinCodeBuilder.getter(builder: KotlinCodeBuilder.() -> Unit) =
    block(this, Getter, EndClass, builder)

inline fun KotlinCodeBuilder.setter(builder: KotlinCodeBuilder.(LocalVar) -> Unit) =
    block(this, Setter, EndClass) {
        builder(object : LocalVar {
            override val name: String
                get() = "value"

            override fun build(builder: CodeStringBuilder) {
            }
        })
    }

inline fun KotlinCodeBuilder.companion(builder: KotlinCodeBuilder.() -> Unit) =
    block(CompanionStart, EndClass, builder)

inline fun KotlinCodeBuilder.defer(builder: KotlinCodeBuilder.() -> Unit) =
    block(Defer, EndClass, builder)

fun KotlinCodeBuilder.define(
    desiredName: String,
    type: ResolvedKotlinType? = null,
    initializer: Symbol? = null
): LocalVar {
    val name = scope.allocateName(desiredName)
    return KotlinLocalVar(name, type, initializer)
}

inline fun KotlinCodeBuilder.cls(
    name: Symbol,
    constructorArgs: List<Symbol>,
    superType: Symbol? = null,
    builder: KotlinCodeBuilder.() -> Unit
) {
    functionScope {
        block(ClassStartSymbol(name, constructorArgs, superType), EndClass) {
            builder()
        }
    }
}

class ClassStartSymbol(
    val clsName: Symbol,
    val constructorArgs: List<Symbol>,
    // Optional single supertype rendered as `class Name(...) : Super {`. Used to make
    // a generated template-instantiation binding implement its generic interface
    // (e.g. `class Box__Int(...) : Box<Int>`).
    val superType: Symbol? = null
) : Symbol,
    SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(clsName) + constructorArgs + listOfNotNull(superType)

    override fun build(builder: CodeStringBuilder) {
        builder.append("class ")
        clsName.build(builder)
        builder.append("(\n")
        builder.startBlock()
        constructorArgs.forEach { symbol ->
            symbol.build(builder)
            builder.removeLast()
            builder.append(",\n")
        }
        builder.endBlock()
        builder.append(")")
        superType?.let {
            builder.append(" : ")
            it.build(builder)
        }
        builder.append(" {\n")
    }

    override fun toString(): String = buildString {
        append("class@${this@ClassStartSymbol.hashCode()} ")
        append(clsName)
        append("(")
        constructorArgs.forEachIndexed { index, symbol ->
            if (index != 0) {
                append(", ")
            }
            append(symbol)
        }
        append(")")
    }
}

/** A Kotlin context parameter (`name: Type`) for `context(...)` on a generated function. */
class ContextParam(val name: String, val type: Symbol)

class KotlinFunctionSig(
    private val name: String,
    private val retType: Symbol,
    private val args: List<LocalVar>
) : Symbol,
    SymbolContainer {
    var receiver: Symbol? = null

    // Kotlin context parameters (`context(name: Type, …)`), emitted before `fun`. Used to
    // supply the ambient MemScope to allocating/cleanup-registering members instead of
    // carrying it as a per-object field. Empty => a plain `fun`.
    var contextParams: List<ContextParam> = emptyList()

    override val symbols: List<Symbol>
        get() = listOfNotNull(receiver) + contextParams.map { it.type } + retType + args

    private fun CodeStringBuilder.appendContext() {
        if (contextParams.isEmpty()) return
        append("context(")
        for ((index, cp) in contextParams.withIndex()) {
            if (index != 0) append(", ")
            append(cp.name)
            append(": ")
            cp.type.build(this)
        }
        append(") ")
    }

    override fun build(builder: CodeStringBuilder) {
        builder.appendContext()
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

    override fun toString(): String = buildString {
        if (contextParams.isNotEmpty()) {
            append("context(")
            for ((index, cp) in contextParams.withIndex()) {
                if (index != 0) append(", ")
                append(cp.name)
                append(": ")
                append(cp.type)
            }
            append(") ")
        }
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

class KotlinLocalVar(
    override val name: String,
    val type: ResolvedKotlinType?,
    private val initializer: Symbol?
) : LocalVar,
    SymbolContainer {
    var isVal: Boolean? = false

    // When this var is a function parameter of a lambda/function type that the body
    // stores (rather than inline-invokes), it must be `noinline` in an inline function.
    var noinline: Boolean = false
    private val typeSymbol = type?.let { KotlinType(it) }
    override val symbols: List<Symbol>
        get() = listOfNotNull(typeSymbol, initializer)

    override fun build(builder: CodeStringBuilder) {
        if (noinline) {
            builder.append("noinline ")
        }
        isVal?.let { isVal ->
            builder.append(if (isVal) "val " else "var ")
        }
        builder.append(name)
        typeSymbol?.let {
            builder.append(": ")
            it.build(builder)
        }
        initializer?.let {
            builder.append(" = ")
            it.build(builder)
        }
    }

    override fun toString(): String = buildString {
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

class KotlinType(private val type: ResolvedKotlinType) :
    Symbol,
    FqSymbol by type {
    private val typeStr: String
        get() = type.name

    constructor(type: ResolvedType) : this(
        (type as? ResolvedKotlinType) ?: (type as? ResolvedCppType)?.kotlinType
            ?: error("Cannot create KotlinType with $type")
    )

    override fun build(builder: CodeStringBuilder) {
        builder.append(typeStr)
    }

    override fun toString(): String = "Type@${hashCode()}:[$typeStr]"
}

inline fun KotlinCodeBuilder.import(target: String) {
    addSymbol(Import(target))
}

inline fun KotlinCodeBuilder.pkg(target: String) {
    addSymbol(Package(target))
}

inline fun KotlinCodeBuilder.inline(build: KotlinCodeBuilder.() -> Unit) {
    val builder = KotlinCodeBuilder(scope).also(build)
    (builder as? CodeBuilderBase<KotlinFactory>)?.symbols?.forEach {
        +inline(it)
    }
}

inline fun inline(target: Symbol): Symbol = Inline(target)

class Inline(private val target: Symbol) :
    Symbol,
    SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(target)

    override fun build(builder: CodeStringBuilder) {
        builder.append("inline ")
        target.build(builder)
    }

    override fun toString(): String = "inline[$target]"
}

inline fun KotlinCodeBuilder.operator(build: KotlinCodeBuilder.() -> Unit) {
    val builder = KotlinCodeBuilder(scope).also(build)
    (builder as? CodeBuilderBase<KotlinFactory>)?.symbols?.forEach {
        +operator(it)
    }
}

inline fun operator(target: Symbol): Symbol = Operator(target)

class Operator(private val target: Symbol) :
    Symbol,
    SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(target)

    override fun build(builder: CodeStringBuilder) {
        builder.append("operator ")
        target.build(builder)
    }
}

inline fun KotlinCodeBuilder.infix(build: KotlinCodeBuilder.() -> Unit) {
    val builder = KotlinCodeBuilder(scope).also(build)
    (builder as? CodeBuilderBase<KotlinFactory>)?.symbols?.forEach {
        +infix(it)
    }
}

inline fun infix(target: Symbol): Symbol = Infix(target)

inline fun KotlinCodeBuilder.override(build: KotlinCodeBuilder.() -> Unit) {
    val builder = KotlinCodeBuilder(scope).also(build)
    (builder as? CodeBuilderBase<KotlinFactory>)?.symbols?.forEach {
        +override(it)
    }
}

inline fun override(target: Symbol): Symbol = Override(target)

class Override(private val target: Symbol) :
    Symbol,
    SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(target)

    override fun build(builder: CodeStringBuilder) {
        builder.append("override ")
        target.build(builder)
    }
}

class Infix(private val target: Symbol) :
    Symbol,
    SymbolContainer {
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

class ImportAs(private val target: String, private val name: String) : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("import ")
        builder.append(target)
        builder.append(" as ")
        builder.append(name)
    }
}

class Package(private val target: String) : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append("package ")
        builder.append(target)
        builder.append('\n')
    }

    override fun toString(): String = "pkg:$target"
}

class KotlinFunctionSymbol(functionBuilder: KotlinCodeBuilder) :
    FunctionSymbol<KotlinFactory>(functionBuilder) {
    var receiver: Symbol? = null

    // Context parameters emitted as `context(name: Type, …)` before `fun` (Kotlin 2.4
    // -Xcontext-parameters). Set on the builder; propagated to the signature in init().
    var contextParams: List<ContextParam> = emptyList()

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
        (signature as? KotlinFunctionSig)?.let {
            it.receiver = receiver
            it.contextParams = contextParams
        }
    }

    override fun toString(): String = "Func@${hashCode()}"
}

fun KotlinCodeBuilder.fqType(fq: String): Symbol = fqType(fullyQualifiedType(fq))
fun KotlinCodeBuilder.fqType(kotlinType: ResolvedKotlinType): Symbol = KotlinType(kotlinType)

inline fun KotlinCodeBuilder.extensionFunction(
    functionBuilder: KotlinFunctionSymbol.() -> Unit
): Symbol {
    val builder = functionScope { KotlinFunctionSymbol(this).also(functionBuilder) }
    builder.init()
    return +builder.symbol
}

class Elvis(private val first: Symbol, private val second: Symbol) :
    Symbol,
    SymbolContainer {
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

inline fun asserting(s: Symbol): Symbol = Asserting(s)

class Asserting(private val s: Symbol) :
    Symbol,
    SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOf(s)

    override fun build(builder: CodeStringBuilder) {
        s.build(builder)
        builder.append("!!")
    }
}

inline infix fun Symbol.elvis(other: Symbol): Symbol = Elvis(this, other)

class Pair(private val first: Symbol, private val second: Symbol) :
    Symbol,
    SymbolContainer {
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

sealed class LambdaBuilder<T : LangFactory>(
    var type: Symbol? = null,
    val lambdaBuilder: CodeBuilder<T>
) {
    protected val args = mutableListOf<LocalVar>()
    abstract val body: CodeBuilder<T>?

    inline fun body(block: BodyBuilder<T>) {
        body!!.apply(block)
    }

    fun define(name: String, type: ResolvedType): LocalVar = lambdaBuilder.define(
        name,
        type
    ).also(args::add).also {
        (it as? KotlinLocalVar)?.isVal = null
    }
}

private object EndLambda : Symbol {
    override fun build(builder: CodeStringBuilder) {
        builder.append('}')
    }
}

open class LambdaSymbol<T : LangFactory>(lambdaBuilder: CodeBuilder<T>) :
    LambdaBuilder<T>(lambdaBuilder = lambdaBuilder),
    Symbol,
    SymbolContainer {
    private val block = BlockSymbol(lambdaBuilder, this, EndLambda)
    lateinit var signature: Symbol
    val symbol: Symbol
        get() = block
    override val body: CodeBuilder<T>
        get() = block

    override val symbols: List<Symbol>
        get() = listOf(signature)

    open fun init() {
        signature = LambdaFunctionSig(
            type,
            args
        )
    }

    override fun build(builder: CodeStringBuilder) {
        signature.build(builder)
        builder.append("\n")
    }

    override fun toString(): String = signature.toString()
}

private class LambdaFunctionSig(private val type: Symbol?, private val args: List<LocalVar>) :
    Symbol,
    SymbolContainer {

    override val symbols: List<Symbol>
        get() = listOfNotNull(type) + args

    override fun build(builder: CodeStringBuilder) {
        type?.build(builder)
        builder.append(" { ")
        for ((index, arg) in args.withIndex()) {
            if (index != 0) {
                builder.append(", ")
            }
            arg.build(builder)
        }
        builder.append(" ->")
    }

    override fun toString(): String = buildString {
        append(type)
        append(" { ")
        for ((index, arg) in args.withIndex()) {
            if (index != 0) {
                append(", ")
            }
            append(arg)
        }
        append(" -> ... }")
    }
}

inline fun <T : LangFactory> CodeBuilder<T>.lambda(
    lambdaBuilder: LambdaBuilder<T>.() -> Unit
): Symbol {
    val builder = functionScope {
        LambdaSymbol(this).also(lambdaBuilder)
    }
    builder.init()
    return builder.symbol
}
