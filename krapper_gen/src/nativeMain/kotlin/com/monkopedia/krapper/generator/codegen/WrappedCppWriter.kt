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

import com.monkopedia.krapper.generator.builders.Call
import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.CodeGenerator
import com.monkopedia.krapper.generator.builders.CodeStringBuilder
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.builders.Delete
import com.monkopedia.krapper.generator.builders.ExternCClose
import com.monkopedia.krapper.generator.builders.ExternCOpen
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.builders.New
import com.monkopedia.krapper.generator.builders.Raw
import com.monkopedia.krapper.generator.builders.Return
import com.monkopedia.krapper.generator.builders.Symbol
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.builders.appendLine
import com.monkopedia.krapper.generator.builders.arrow
import com.monkopedia.krapper.generator.builders.assign
import com.monkopedia.krapper.generator.builders.comment
import com.monkopedia.krapper.generator.builders.define
import com.monkopedia.krapper.generator.builders.dot
import com.monkopedia.krapper.generator.builders.function
import com.monkopedia.krapper.generator.builders.include
import com.monkopedia.krapper.generator.builders.includeSys
import com.monkopedia.krapper.generator.builders.op
import com.monkopedia.krapper.generator.builders.reference
import com.monkopedia.krapper.generator.builders.type
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTypeReference
import com.monkopedia.krapper.generator.model.WrappedTypeReference.Companion.pointerTo

class WrappedCppWriter(
    private val nameHandler: NameHandler,
    private val cppFile: File,
    codeBuilder: CppCodeBuilder,
    policy: CodeGenerationPolicy = ThrowPolicy
) : CodeGenerator<CppCodeBuilder>(codeBuilder, policy) {

    override fun CppCodeBuilder.onGenerate(
        cls: WrappedClass,
        handleChildren: CppCodeBuilder.() -> Unit
    ) {
        comment("BEGIN KRAPPER GEN for ${cls.fullyQualified}")
        appendLine()
        handleChildren()
        appendLine()
        comment("END KRAPPER GEN for ${cls.fullyQualified}")
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

    override fun CppCodeBuilder.onGenerate(cls: WrappedClass, method: WrappedMethod) {
        nameHandler.withNamer(cls) {
            function {
                val type = cls.type
                generateMethodSignature(type, method, this@withNamer)
                val args = addArgs(type, method)
                body {
                    generateMethodBody(cls, method, args)
                }
            }
            appendLine()
        }
    }

    override fun CppCodeBuilder.onGenerate(cls: WrappedClass, field: WrappedField) {
        nameHandler.withNamer(cls) {
            function {
                val type = cls.type
                val args = generateFieldGet(type, field, this@withNamer)
                body {
                    generateFieldGetBody(cls, field, args)
                }
            }
            appendLine()
            function {
                val type = cls.type
                val args = generateFieldSet(type, field, this@withNamer)
                body {
                    generateFieldSetBody(cls, field, args)
                }
            }
            appendLine()
        }
    }

    private fun CppCodeBuilder.generateMethodBody(
        cls: WrappedClass,
        method: WrappedMethod,
        args: List<WrapperArgument>
    ) {
        var thiz = if (method.methodType != MethodType.CONSTRUCTOR) args[0] else null
        val returnArg =
            if ((
                method.methodType == MethodType.METHOD ||
                    method.methodType == MethodType.STATIC_OP
                ) &&
                !method.returnType.isReturnable
            ) args.last()
            else null
        val thizCast = thiz?.let { createCast(it) }
        val returnCast = returnArg?.let { createCast(it) }
        val argCasts = args.filter { it.arg != null }.map { a ->
            if (a.targetType.isString) createStringCast(a)
            else if (a.targetType.isNative) a
            else createCast(a)
        }
        when (method.methodType) {
            MethodType.CONSTRUCTOR -> {
                +Return(
                    New(
                        Call(
                            cls.fullyQualified,
                            *(argCasts.map { it.reference }.toTypedArray())
                        )
                    )
                )
            }
            MethodType.DESTRUCTOR -> {
                +Delete(thizCast?.pointerReference ?: error("Missing this in delete"))
            }
            MethodType.STATIC_OP -> {
                val call = (thizCast?.reference ?: error("Missing this in method")).op(
                    method.name.substring("operator".length),
                    argCasts.map { it.reference }.single()
                )
                if (returnCast != null) {
                    +(returnCast.reference assign call)
                } else if (method.returnType.isString) {
                    createStringReturn(call)
                } else if (method.returnType.isPointer && method.returnType.pointed.isString) {
                    createPointedStringReturn(call)
                } else if (!method.returnType.isVoid) {
                    +Return(call)
                } else {
                    +call
                }
            }
            MethodType.METHOD -> {
                val call =
                    (thizCast?.pointerReference ?: error("Missing this in method")) arrow Call(
                        method.name,
                        *(argCasts.map { it.reference }.toTypedArray())
                    )
                if (returnCast != null) {
                    +(returnCast.reference assign call)
                } else if (method.returnType.isString) {
                    createStringReturn(call)
                } else if (method.returnType.isPointer && method.returnType.pointed.isString) {
                    createPointedStringReturn(call)
                } else if (!method.returnType.isVoid) {
                    +Return(call)
                } else {
                    +call
                }
            }
        }
    }

    private fun CppCodeBuilder.createPointedStringReturn(call: Symbol) {
        val returnStr = +define(
            "ret_value",
            pointerTo(WrappedTypeReference("std::string")),
            initializer = call
        )
        val returnArray = +define(
            "ret_value_cast",
            WrappedTypeReference("char *"),
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
            +define("ret_value", WrappedTypeReference("std::string"), initializer = call)
        val returnArray = +define(
            "ret_value_cast",
            WrappedTypeReference("char *"),
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
        arg: WrapperArgument
    ): WrapperArgument {
        return arg.copy(
            localVar = +define(
                arg.localVar.name + "_cast",
                arg.targetType,
                initializer = Call("std::string", arg.localVar.reference)
            )
        )
    }

    private fun CppCodeBuilder.createCast(
        arg: WrapperArgument
    ): WrapperArgument {
        return arg.copy(
            localVar = +define(
                arg.localVar.name + "_cast",
                arg.targetType,
                reinterpret(arg.localVar, arg.targetType)
            )
        )
    }

    private fun CppCodeBuilder.reinterpret(arg: LocalVar, type: WrappedTypeReference): Symbol {
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
        cls: WrappedClass,
        field: WrappedField,
        args: List<WrapperArgument>
    ) {
        val thizCast = createCast(args[0])
        val fetch = thizCast.pointerReference arrow Raw(field.name)
        if (field.type.isString) {
            createStringReturn(fetch)
        } else if (field.type.isPointer && field.type.pointed.isString) {
            createPointedStringReturn(fetch)
        } else if (field.type.isReturnable) {
            +Return(fetch)
        } else {
            val valueCast = createCast(args[1])
            +(valueCast.reference assign fetch)
        }
    }

    private fun CppCodeBuilder.generateFieldSetBody(
        cls: WrappedClass,
        field: WrappedField,
        args: List<WrapperArgument>
    ) {
        val thizCast = createCast(args[0])
        val fetch = thizCast.pointerReference arrow Raw(field.name)
        if (field.type.isReturnable) {
            +(fetch assign args[1].reference)
        } else {
            val valueCast = createCast(args[1])
            +(fetch assign valueCast.reference)
        }
    }
}
