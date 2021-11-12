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

typealias CppCodeBuilder = CodeBuilder<CppFactory>

fun CppCodeBuilder(): CppCodeBuilder {
    return CodeBuilderBase(CppFactory(), addSemis = true)
}

class CppFactory : LangFactory {
    override fun define(name: String, type: WrappedType, initializer: Symbol?): LocalVar {
        return CppLocalVar(name, type, initializer)
    }

    override fun funSig(name: String, retType: Symbol?, args: List<LocalVar>): Symbol {
        return CppFunctionSig(name, retType ?: CppType("void"), args)
    }

    override fun createType(type: WrappedType): Symbol = CppType(type)
}

class CppFunctionSig(
    private val name: String,
    private val retType: Symbol,
    private val args: List<LocalVar>
) : Symbol {

    override fun build(builder: CodeStringBuilder) {
        retType.build(builder)
        builder.append(' ')
        builder.append(name)
        builder.append('(')
        for ((index, arg) in args.withIndex()) {
            if (index != 0) {
                builder.append(", ")
            }
            arg.build(builder)
        }
        builder.append(')')
    }
}

class CppLocalVar(
    override val name: String,
    val type: WrappedType,
    private val initializer: Symbol?
) : LocalVar, SymbolContainer {
    private val typeSymbol = CppType(type)
    override val symbols: List<Symbol>
        get() = listOfNotNull(typeSymbol, initializer)
    override fun build(builder: CodeStringBuilder) {
        typeSymbol.build(builder)
        builder.append(' ')
        builder.append(name)
        initializer?.let {
            builder.append(" = ")
            it.build(builder)
        }
    }
}

class CppType(private val typeStr: String) : Symbol {
    constructor(type: WrappedType) : this(type.toString())

    override fun build(builder: CodeStringBuilder) {
        builder.append(typeStr)
    }
}

inline fun CppCodeBuilder.include(target: String) {
    addSymbol(PreprocessorSymbol("include \"$target\""))
}
inline fun CppCodeBuilder.includeSys(target: String) {
    addSymbol(PreprocessorSymbol("include <$target>"))
}

inline fun CppCodeBuilder.define(condition: String) = +PreprocessorSymbol("define $condition")

inline fun CppCodeBuilder.ifdef(condition: String, builder: CppCodeBuilder.() -> Unit) = block(
    PreprocessorSymbol("ifdef $condition\n"),
    PreprocessorSymbol("endif //$condition"),
    builder
)

inline fun CppCodeBuilder.ifndef(condition: String, builder: CppCodeBuilder.() -> Unit) = block(
    PreprocessorSymbol("ifndef $condition\n"),
    PreprocessorSymbol("endif //$condition"),
    builder
)

class PreprocessorSymbol(private val target: String) : Symbol {
    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) {
        builder.append("#$target")
    }
}

object ExternCOpen : Symbol {
    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) {
        builder.append("extern \"C\" {")
    }
}

object ExternCClose : Symbol {
    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) {
        builder.append("}")
    }
}
