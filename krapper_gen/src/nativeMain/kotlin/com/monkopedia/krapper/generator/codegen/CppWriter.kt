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
package com.monkopedia.krapper.generator.codegen

import com.monkopedia.krapper.OperatorType.ASSIGN
import com.monkopedia.krapper.ResolvedOperator
import com.monkopedia.krapper.generator.builders.Call
import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.CodeGenerator
import com.monkopedia.krapper.generator.builders.CodeStringBuilder
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.builders.ExternCClose
import com.monkopedia.krapper.generator.builders.ExternCOpen
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.builders.New
import com.monkopedia.krapper.generator.builders.Raw
import com.monkopedia.krapper.generator.builders.RawCast
import com.monkopedia.krapper.generator.builders.Return
import com.monkopedia.krapper.generator.builders.Symbol
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.builders.addressOf
import com.monkopedia.krapper.generator.builders.appendLine
import com.monkopedia.krapper.generator.builders.arrow
import com.monkopedia.krapper.generator.builders.assign
import com.monkopedia.krapper.generator.builders.coloncolon
import com.monkopedia.krapper.generator.builders.comment
import com.monkopedia.krapper.generator.builders.define
import com.monkopedia.krapper.generator.builders.dereference
import com.monkopedia.krapper.generator.builders.dot
import com.monkopedia.krapper.generator.builders.function
import com.monkopedia.krapper.generator.builders.include
import com.monkopedia.krapper.generator.builders.includeSys
import com.monkopedia.krapper.generator.builders.op
import com.monkopedia.krapper.generator.builders.reference
import com.monkopedia.krapper.generator.builders.type
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.NATIVE
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.RAW_CAST
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.REINT_CAST
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.STD_MOVE
import com.monkopedia.krapper.generator.resolved_model.MethodType
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedField
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.ARG_CAST
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.COPY_CONSTRUCTOR
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.RETURN
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.STRING
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.STRING_POINTER
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.VOID
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.VOIDP
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.VOIDP_REFERENCE
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedType

class CppWriter(
    private val nameHandler: NameHandler,
    private val cppFile: File,
    codeBuilder: CppCodeBuilder,
    policy: CodeGenerationPolicy = ThrowPolicy
) : CodeGenerator<CppCodeBuilder>(codeBuilder, policy) {

    private var lookup: ClassLookup = ClassLookup(emptyList())

    override fun generate(moduleName: String, headers: List<String>, classes: List<ResolvedClass>) {
        lookup = ClassLookup(classes)
        super.generate(moduleName, headers, classes)
    }

    override fun CppCodeBuilder.onGenerate(
        cls: ResolvedClass,
        handleChildren: CppCodeBuilder.() -> Unit
    ) {
        comment("BEGIN KRAPPER GEN for ${cls.type}")
        appendLine()
        handleChildren()
        appendLine()
        comment("END KRAPPER GEN for ${cls.type}")
        appendLine()
        appendLine()
    }

    override fun CppCodeBuilder.onGenerate(
        moduleName: String,
        headers: List<String>,
        handleChildren: CppCodeBuilder.() -> Unit
    ) {
        val splitCamelcase = moduleName.splitCamelcase()
        val headerLabel = splitCamelcase.joinToString("_") { it.toLowerCase() }
        include("$headerLabel.h")
        for (header in headers) {
            include("${File(header).relativeTo(cppFile)}")
        }
        includeSys("vector")
        includeSys("string")
        includeSys("iterator")
        appendLine()
        +ExternCOpen
        appendLine()

        handleChildren()

        appendLine()
        +ExternCClose
        appendLine()
    }

    override fun CppCodeBuilder.onGenerate(cls: ResolvedClass, method: ResolvedMethod) {
        function {
            val type = cls.type
            generateMethodSignature(method)
            val args = addArgs(lookup, type, method)
            body {
                generateMethodBody(cls, method, args)
            }
        }
        appendLine()
    }

    override fun CppCodeBuilder.onGenerate(cls: ResolvedClass, field: ResolvedField) {
        function {
            val type = cls.type
            val args = generateFieldGet(field)
            body {
                generateFieldGetBody(cls, field, args)
            }
        }
        appendLine()
        function {
            val type = cls.type
            val args = generateFieldSet(field)
            body {
                generateFieldSetBody(cls, field, args)
            }
        }
        appendLine()
    }

    private fun CppCodeBuilder.generateMethodBody(
        cls: ResolvedClass,
        method: ResolvedMethod,
        args: List<SignatureArgument>
    ) {
        val argCasts = args.map { a ->
            generateArgumentCast(a)
        }.toMutableList()
        val returnCast = if (method.returnStyle == ARG_CAST) argCasts.removeLast() else null
        when (method.methodType) {
            MethodType.CONSTRUCTOR -> {
                val locationCast = argCasts.removeFirst()
                +Return(
                    New(
                        Call(
                            cls.type.toString(),
                            *(argCasts.map { it.reference }.toTypedArray())
                        ),
                        locationCast.reference
                    )
                )
            }
            MethodType.SIZE_OF -> {
                +Return(
                    Call(
                        "sizeof",
                        Raw(cls.type.toString())
                    )
                )
            }
            MethodType.DESTRUCTOR -> {
                val thizCast = argCasts.removeFirst()
                +(thizCast.pointerReference arrow Call(method.name.removeTemplate()))
            }
            MethodType.STATIC_OP -> {
                val thizCast = argCasts.removeFirst()
                val call = (thizCast.reference).op(
                    method.name.substring("operator".length),
                    argCasts.map { it.reference }.single()
                )
                generateReturn(call, method.returnStyle, method.returnType, returnCast)
            }
            MethodType.STATIC -> {
                val call = Raw(cls.type.toString()) coloncolon Call(
                    method.name,
                    *(argCasts.map { it.reference }.toTypedArray())
                )
                generateReturn(call, method.returnStyle, method.returnType, returnCast)
            }
            MethodType.METHOD -> {
                val thizCast = argCasts.removeFirst()
                val thizRef = thizCast.pointerReference
                val operator = method.operator
                val call =
                    when {
                        operator?.supportsDirectCall == true -> {
                            thizRef.dereference.op(operator.cppOp, argCasts.first().reference)
                        }
                        operator?.operatorType == ASSIGN -> {
                            thizRef.dereference.assign(
                                argCasts.first().reference,
                                operator == ResolvedOperator.PLUS_EQUALS
                            )
                        }
                        else -> {
                            thizRef arrow Call(
                                method.name,
                                *(argCasts.map { it.reference }.toTypedArray())
                            )
                        }
                    }
                generateReturn(call, method.returnStyle, method.returnType, returnCast)
            }
        }
    }

    private fun CppCodeBuilder.generateArgumentCast(a: SignatureArgument) =
        when (a.arg?.castMode) {
            NATIVE -> a
            ArgumentCastMode.STRING -> createStringCast(a)
            RAW_CAST -> createRawCast(a)
            STD_MOVE, REINT_CAST, null -> createCast(a)
        }

    private fun CppCodeBuilder.generateReturn(
        call: Symbol,
        returnStyle: ReturnStyle,
        returnType: ResolvedType,
        returnCast: SignatureArgument?
    ) {
        when (returnStyle) {
            VOID -> +call
            VOIDP_REFERENCE -> +Return(RawCast("void*", call.addressOf))
            VOIDP -> +Return(RawCast("void*", call))
            ARG_CAST -> +(returnCast!!.reference assign call)
            STRING -> createStringReturn(call)
            STRING_POINTER -> createPointedStringReturn(call)
            COPY_CONSTRUCTOR -> +Return(New(Call(returnType.toConstructor(), call)))
            RETURN -> +Return(call)
        }
    }

    private fun ResolvedType.toConstructor(): String {
        return toString().trimEnd('*').let {
            if (it.startsWith("const ")) it.substring("const ".length)
            else it
        }
    }

    private fun CppCodeBuilder.createPointedStringReturn(call: Symbol) {
        val returnStr = +define(
            "ret_value",
            ResolvedType.PSTRING,
            initializer = call
        )
        val returnArray = +define(
            "ret_value_cast",
            ResolvedType.CSTRING,
            initializer = New(Raw("char[${returnStr.name}->length() + 1]"))
        )
        +(
            returnStr.reference arrow Call(
                "copy",
                returnArray.reference,
                returnStr.reference arrow Call("length"),
                Raw("0")
            )
            )
        +Return(returnArray.reference)
    }

    private fun CppCodeBuilder.createStringReturn(call: Symbol) {
        val returnStr =
            +define("ret_value", ResolvedType.STRING, initializer = call)
        val returnArray = +define(
            "ret_value_cast",
            ResolvedType.CSTRING,
            initializer = New(Raw("char[${returnStr.name}.length() + 1]"))
        )
        +(
            returnStr.reference dot Call(
                "copy",
                returnArray.reference,
                returnStr.reference dot Call("length"),
                Raw("0")
            )
            )
        +Return(returnArray.reference)
    }

    private fun CppCodeBuilder.createStringCast(
        arg: SignatureArgument
    ): SignatureArgument {
        return arg.copy(
            localVar = +define(
                arg.localVar.name + "_cast",
                arg.targetType,
                initializer = Call("std::string", arg.localVar.reference)
            )
        )
    }

    private fun CppCodeBuilder.createCast(
        arg: SignatureArgument
    ): SignatureArgument {
        return arg.copy(
            localVar = +define(
                arg.localVar.name + "_cast",
                arg.targetType,
                reinterpret(arg.localVar, arg.targetType)
            )
        )
    }

    private fun CppCodeBuilder.createRawCast(
        arg: SignatureArgument
    ): SignatureArgument {
        return arg.copy(
            localVar = +define(
                arg.localVar.name + "_cast",
                arg.targetType,
                RawCast(arg.targetType.toString(), arg.localVar.reference)
            )
        )
    }

    private fun CppCodeBuilder.reinterpret(arg: LocalVar, type: ResolvedType): Symbol {
        val type = type(type)
        val reference = arg.reference
        return object : Symbol {
            override fun build(builder: CodeStringBuilder) {
                builder.append("reinterpret_cast<")
                type.build(builder)
                builder.append(">(")
                reference.build(builder)
                builder.append(")")
            }
        }
    }

    private fun CppCodeBuilder.generateFieldGetBody(
        cls: ResolvedClass,
        field: ResolvedField,
        args: List<SignatureArgument>
    ) {
        val thizCast = generateArgumentCast(args[0])
        val fetch = thizCast.pointerReference arrow Raw(field.name)
        generateReturn(
            fetch,
            field.getter.returnStyle,
            field.getter.returnType,
            args.getOrNull(1)?.let { generateArgumentCast(it) }
        )
    }

    private fun CppCodeBuilder.generateFieldSetBody(
        cls: ResolvedClass,
        field: ResolvedField,
        args: List<SignatureArgument>
    ) {
        val thizCast = generateArgumentCast(args[0])
        val fetch = thizCast.pointerReference arrow Raw(field.name)
        val argumentCast = generateArgumentCast(args[1])
        +(fetch assign argumentCast.reference)
    }
}

private fun String.removeTemplate(): String {
    if (!contains("<")) return this
    return substring(0, indexOf('<'))
}
