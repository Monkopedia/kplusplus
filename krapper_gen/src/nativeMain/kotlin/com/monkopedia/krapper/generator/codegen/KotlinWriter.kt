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

import com.monkopedia.krapper.BasicMethod
import com.monkopedia.krapper.BasicWithDummyMethod
import com.monkopedia.krapper.InfixMethod
import com.monkopedia.krapper.KotlinOperator
import com.monkopedia.krapper.ResolvedOperator
import com.monkopedia.krapper.generator.builders.Call
import com.monkopedia.krapper.generator.builders.CodeBuilder
import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.CodeGeneratorBase
import com.monkopedia.krapper.generator.builders.KotlinCodeBuilder
import com.monkopedia.krapper.generator.builders.KotlinFactory
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_OPAQUE_POINTER
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_POINTER
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.MEM_SCOPE
import com.monkopedia.krapper.generator.builders.KotlinLocalVar
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.builders.Raw
import com.monkopedia.krapper.generator.builders.Return
import com.monkopedia.krapper.generator.builders.Symbol
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.builders.appendLine
import com.monkopedia.krapper.generator.builders.cls
import com.monkopedia.krapper.generator.builders.comment
import com.monkopedia.krapper.generator.builders.companion
import com.monkopedia.krapper.generator.builders.defer
import com.monkopedia.krapper.generator.builders.define
import com.monkopedia.krapper.generator.builders.dot
import com.monkopedia.krapper.generator.builders.elvis
import com.monkopedia.krapper.generator.builders.extensionFunction
import com.monkopedia.krapper.generator.builders.extensionMethod
import com.monkopedia.krapper.generator.builders.fqType
import com.monkopedia.krapper.generator.builders.function
import com.monkopedia.krapper.generator.builders.getter
import com.monkopedia.krapper.generator.builders.importBlock
import com.monkopedia.krapper.generator.builders.infix
import com.monkopedia.krapper.generator.builders.inline
import com.monkopedia.krapper.generator.builders.isVal
import com.monkopedia.krapper.generator.builders.operator
import com.monkopedia.krapper.generator.builders.pairedTo
import com.monkopedia.krapper.generator.builders.pkg
import com.monkopedia.krapper.generator.builders.property
import com.monkopedia.krapper.generator.builders.qdot
import com.monkopedia.krapper.generator.builders.reference
import com.monkopedia.krapper.generator.builders.setter
import com.monkopedia.krapper.generator.builders.symbol
import com.monkopedia.krapper.generator.builders.type
import com.monkopedia.krapper.generator.resolved_model.MethodType
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedDestructor
import com.monkopedia.krapper.generator.resolved_model.ResolvedField
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolved_model.type.fullyQualifiedType
import com.monkopedia.krapper.generator.resolved_model.type.nullable
import com.monkopedia.krapper.generator.resolved_model.type.typedWith

class KotlinWriter(
    private val nameHandler: NameHandler,
    private val pkg: String,
    policy: CodeGenerationPolicy = ThrowPolicy
) : CodeGeneratorBase<KotlinCodeBuilder>(policy) {

    fun generate(outputDir: File, classes: List<ResolvedClass>) {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        for (file in outputDir.listFiles()) {
            file.delete()
        }
        for (cls in classes) {
            val clsFile = File(outputDir, cls.type.kotlinType.name + ".kt")
            val builder = KotlinCodeBuilder()
            builder.generate(cls)
            clsFile.writeText(builder.toString())
        }
    }

    override fun KotlinCodeBuilder.onGenerate(
        cls: ResolvedClass,
        handleChildren: KotlinCodeBuilder.() -> Unit
    ) {
        val type = cls.type.kotlinType ?: error("Cannot generate code with no kotlin type")
        val pkg = type.pkg
        pkg(pkg)

        importBlock(pkg, this)
        comment("BEGIN KRAPPER GEN for ${cls.type}")
        appendLine()
        cls(named(type)) { source ->
            val ptr = define(ptr.content, fullyQualifiedType(C_OPAQUE_POINTER))
            val memScope = define(memScope.content, fullyQualifiedType(MEM_SCOPE))
            property(ptr) {
                getter = inline(
                    getter {
                        +Return(source.reference dot Raw("first"))
                    }
                )
            }
            property(memScope) {
                getter = inline(
                    getter {
                        +Return(source.reference dot Raw("second"))
                    }
                )
            }
            handleChildren()
        }
        appendLine()
        comment("END KRAPPER GEN for ${cls.type}")
        appendLine()
        appendLine()
    }

    override fun KotlinCodeBuilder.onGenerateMethods(cls: ResolvedClass) {
        val methods = cls.children.filterIsInstance<ResolvedMethod>()
        methods.filter {
            it.methodType == MethodType.STATIC_OP || it.methodType == MethodType.METHOD
        }.forEach { method ->
            try {
                onGenerate(cls, method)
            } catch (t: Throwable) {
                codeGenerationPolicy.onGenerateMethodFailed(cls, method, t)
            }
        }
        companion {
            for (method in methods.filter { it.methodType == MethodType.CONSTRUCTOR }) {
                try {
                    onGenerate(cls, method)
                } catch (t: Throwable) {
                    codeGenerationPolicy.onGenerateMethodFailed(cls, method, t)
                }
            }
        }
    }

    private fun named(name: ResolvedKotlinType) = Raw(name.name)

    private val ptr = Raw("ptr")
    private val memScope = Raw("memScope")

    override fun KotlinCodeBuilder.onGenerate(cls: ResolvedClass, method: ResolvedMethod) {
        val uniqueCName =
            extensionMethod(pkg, method.uniqueCName ?: error("Unnamed method $method"))
        when (method.methodType) {
            MethodType.CONSTRUCTOR -> {
                val destructor =
                    cls.children.filterIsInstance<ResolvedDestructor>().firstOrNull()
                extensionFunction {
                    receiver = fqType(MEM_SCOPE)
                    name = cls.type.kotlinType!!.name
                    retType = type(cls.type)
                    val args = method.args.map {
                        define(it.name, it.type)
                    }
                    body {
                        val obj = +define(
                            "obj",
                            fullyQualifiedType(C_OPAQUE_POINTER),
                            initializer = (
                                Call(
                                    uniqueCName,
                                    *args.map { reference(it) }.toTypedArray()
                                ) elvis Call("error", "Creation failed".symbol)
                                )
                        )
                        obj.isVal = true
                        defer {
                            if (destructor != null) {
                                +Call(
                                    extensionMethod(
                                        pkg,
                                        destructor.uniqueCName
                                            ?: error("Unnamed destructor in $cls")
                                    ),
                                    obj.reference
                                )
                            } else {
                                +Call(extensionMethod("platform.linux", "free"), obj.reference)
                            }
                        }
                        +Return(
                            Call(
                                cls.type.kotlinType!!.name.trimEnd('?'),
                                obj.reference pairedTo thiz.reference
                            )
                        )
                    }
                }
            }
            MethodType.DESTRUCTOR -> {
                // Do nothing
            }
            MethodType.METHOD,
            MethodType.STATIC_OP -> {
                val operator = method.operator
                if (operator != null) {
                    generateOperator(operator, cls, method)
                } else {
                    inline {
                        generateBasicMethod(fixNaming(method), uniqueCName)
                    }
                }
            }
        }
    }

    private val INFIX_LIST = setOf(
        "assign",
        "plusEquals",
        "eq",
        "neq",
        "lt",
        "gt",
        "lteq",
        "gteq",
        "binAnd",
        "binOr",
        "and",
        "or",
        "xor",
        "shl",
        "shr"
    )

    private fun fixNaming(method: ResolvedMethod): ResolvedMethod {
        return if (method.name in INFIX_LIST) {
            method.copy(name = method.name + "_method")
        } else {
            method
        }
    }

    private fun KotlinCodeBuilder.generateBasicMethod(
        method: ResolvedMethod,
        uniqueCName: Symbol,
        methodName: String = method.name,
        startArgs: List<Symbol> = listOf(ptr)
    ) {
        function {
            name = methodName
            val returnType = method.returnType
            println("Generating ${returnType.kotlinType} for ${returnType} in $method")
            val kotlinType = returnType.kotlinType
            var isGeneratingReturn = false
            retType = type(returnType)
            var args = if (method.args.isNotEmpty()) method.args.subList(1, method.args.size).map {
                define(it.name, it.type)
            } else emptyList()
            if (method.returnStyle == ReturnStyle.ARG_CAST && kotlinType.isWrapper) {
                args += this@generateBasicMethod.define(
                    "retValue",
                    returnType,
                    initializer = memScope dot Call(
                        extensionMethod(
                            kotlinType.fullyQualified.last() + ".Companion",
                            kotlinType.name
                        )
                    )
                ).also {
                    it.isVal = true
                }
                isGeneratingReturn = true
            }
            body {
                val call = Call(
                    uniqueCName,
                    *(startArgs + args.map { reference(it) }).toTypedArray()
                )
                if (isGeneratingReturn) {
                    +args.last()
                    +call
                    +Return(args.last().reference)
                } else {
                    generateReturn(kotlinType, call)
                }
            }
        }
    }

    private fun KotlinCodeBuilder.generateOperator(
        operator: ResolvedOperator,
        cls: ResolvedClass,
        method: ResolvedMethod
    ) {
        when (val kotlinType = operator.kotlinOperatorType) {
            is KotlinOperator -> {
                inline {
                    operator {
                        generateBasicMethod(
                            method,
                            extensionMethod(pkg, method.uniqueCName!!),
                            kotlinType.name
                        )
                    }
                }
            }
            is InfixMethod -> {
                inline {
                    infix {
                        generateBasicMethod(
                            method,
                            extensionMethod(pkg, method.uniqueCName!!),
                            kotlinType.name
                        )
                    }
                }
            }
            is BasicWithDummyMethod -> {
                inline {
                    generateBasicMethod(
                        method.copy(args = emptyList()),
                        extensionMethod(pkg, method.uniqueCName!!),
                        kotlinType.name,
                        startArgs = listOf(ptr, Raw("0"))
                    )
                }
            }
            is BasicMethod -> {
                inline {
                    generateBasicMethod(
                        method,
                        extensionMethod(pkg, method.uniqueCName!!),
                        kotlinType.name
                    )
                }
            }
        }
    }

    private fun reference(v: LocalVar): Symbol {
        (v as? KotlinLocalVar) ?: error("Non-kotlin local var $v")
        val type = v.type
        return reference(type, v)
    }

    private fun reference(type: ResolvedKotlinType, v: LocalVar): Symbol {
        return if (type.isWrapper) {
            if (type.toString().endsWith("?")) {
                v.reference qdot ptr
            } else {
                v.reference dot ptr
            }
        } else v.reference
    }

    override fun KotlinCodeBuilder.onGenerate(cls: ResolvedClass, field: ResolvedField) {
        property(define(field.name, field.kotlinType)) {
            getter = inline(
                getter {
                    val call = Call(extensionMethod(pkg, field.getter.uniqueCName!!), ptr)
                    generateReturn(field.kotlinType, call)
                }
            )
            if (!field.isConst) {
                setter = inline(
                    setter { value ->
                        +Call(
                            extensionMethod(pkg, field.setter.uniqueCName!!),
                            ptr,
                            reference(field.kotlinType, value)
                        )
                    }
                )
            }
        }
    }

    private fun CodeBuilder<KotlinFactory>.generateReturn(
        returnType: ResolvedKotlinType,
        call: Call
    ) {
        when {
            returnType.isWrapper -> {
                +Return(
                    Call(
                        returnType.name.trimEnd('?'),
                        (
                            if (returnType.isNullable) call elvis Return(Raw("null"))
                            else call
                            ) pairedTo memScope
                    )
                )
            }
            returnType.toString() == "String?" -> {
                generateStringReturn(call)
            }
            else -> {
                +Return(call)
            }
        }
    }

    private fun KotlinCodeBuilder.generateStringReturn(call: Call) {
        val strDecl = +define(
            "str",
            nullable(
                fullyQualifiedType(C_POINTER)
                    .typedWith(listOf(fullyQualifiedType("kotlinx.cinterop.ByteVar")))
            ),
            initializer = call
        )
        strDecl.isVal = true
        val retValue = +define(
            "ret",
            fullyQualifiedType("String?"),
            initializer = strDecl.reference qdot Call(
                extensionMethod(
                    "kotlinx.cinterop",
                    "toKString"
                )
            )
        )
        retValue.isVal = true
        +Call(extensionMethod("platform.linux", "free"), strDecl.reference)
        +Return(retValue.reference)
    }
}