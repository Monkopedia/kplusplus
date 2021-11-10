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
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedDestructor
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedKotlinType
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedType
import com.monkopedia.krapper.generator.model.fullyQualifiedType
import com.monkopedia.krapper.generator.model.isArray
import com.monkopedia.krapper.generator.model.isConst
import com.monkopedia.krapper.generator.model.isPointer
import com.monkopedia.krapper.generator.model.isReference
import com.monkopedia.krapper.generator.model.isString
import com.monkopedia.krapper.generator.model.kotlinType
import com.monkopedia.krapper.generator.model.nullable
import com.monkopedia.krapper.generator.model.pointed
import com.monkopedia.krapper.generator.model.typedWith


class WrappedKotlinWriter(
    private val nameHandler: NameHandler,
    private val pkg: String,
    policy: CodeGenerationPolicy = ThrowPolicy
) : CodeGeneratorBase<KotlinCodeBuilder>(policy) {

    fun generate(outputDir: File, classes: List<WrappedClass>) {
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
        cls: WrappedClass,
        handleChildren: KotlinCodeBuilder.() -> Unit
    ) {
        val pkg = cls.type.kotlinType.pkg
        pkg(pkg)

        importBlock(pkg, this)
        comment("BEGIN KRAPPER GEN for ${cls.type}")
        appendLine()
        cls(named(cls.type.kotlinType)) { source ->
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

    override fun KotlinCodeBuilder.onGenerateMethods(cls: WrappedClass) {
        val methods = cls.children.filterIsInstance<WrappedMethod>()
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

    private fun named(name: WrappedKotlinType) = Raw(name.name)

    private val ptr = Raw("ptr")
    private val memScope = Raw("memScope")

    override fun KotlinCodeBuilder.onGenerate(cls: WrappedClass, method: WrappedMethod) =
        nameHandler.withNamer(cls) {
            val uniqueCName = extensionMethod(pkg, method.uniqueCName)
            when (method.methodType) {
                MethodType.CONSTRUCTOR -> {
                    val destructor =
                        cls.children.filterIsInstance<WrappedDestructor>().firstOrNull()
                    extensionFunction {
                        receiver = fqType(MEM_SCOPE)
                        name = cls.type.kotlinType.name
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
                                        extensionMethod(pkg, destructor.uniqueCName),
                                        obj.reference
                                    )
                                } else {
                                    +Call(extensionMethod("platform.linux", "free"), obj.reference)
                                }
                            }
                            +Return(
                                Call(
                                    cls.type.kotlinType.name,
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
                    val operator = Operator.from(method)
                    if (operator != null) {
                        generateOperator(operator, cls, method)
                    } else {
                        inline {
                            generateBasicMethod(method, uniqueCName)
                        }
                    }
                }
            }
        }

    private fun KotlinCodeBuilder.generateBasicMethod(
        method: WrappedMethod,
        uniqueCName: Symbol,
        methodName: String = method.name,
        startArgs: List<Symbol> = listOf(ptr)
    ) {
        function {
            name = methodName
            val returnType = method.returnType
            var isGeneratingReturn = false
            retType = type(returnType)
            var args = method.args.map {
                define(it.name, it.type)
            }
            if (!(returnType.isPointer || returnType.isReference) && returnType.kotlinType.isWrapper) {
                args += this@generateBasicMethod.define(
                    "retValue",
                    returnType,
                    initializer = memScope dot Call(
                        extensionMethod(
                            returnType.kotlinType.fullyQualified.last() + ".Companion",
                            returnType.kotlinType.name
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
                    generateReturn(returnType, call)
                }
            }
        }
    }

    private fun KotlinCodeBuilder.generateOperator(
        operator: Operator,
        cls: WrappedClass,
        method: WrappedMethod
    ) = nameHandler.withNamer(cls) {
        when (val kotlinType = operator.kotlinOperatorType) {
            is KotlinOperator -> {
                inline {
                    operator {
                        generateBasicMethod(
                            method,
                            extensionMethod(pkg, method.uniqueCName),
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
                            extensionMethod(pkg, method.uniqueCName),
                            kotlinType.name
                        )
                    }
                }
            }
            is BasicWithDummyMethod -> {
                inline {
                    generateBasicMethod(
                        method.copy(children = emptyList()),
                        extensionMethod(pkg, method.uniqueCName),
                        kotlinType.name,
                        startArgs = listOf(ptr, Raw("0"))
                    )
                }
            }
            is BasicMethod -> {
                inline {
                    generateBasicMethod(
                        method,
                        extensionMethod(pkg, method.uniqueCName),
                        kotlinType.name
                    )
                }
            }
        }
    }

    private fun reference(v: LocalVar): Symbol {
        (v as? KotlinLocalVar) ?: error("Non-kotlin local var $v")
        return if (v.type.isWrapper) v.reference dot ptr else v.reference
    }

    override fun KotlinCodeBuilder.onGenerate(cls: WrappedClass, field: WrappedField) =
        nameHandler.withNamer(cls) {
            if (field.type.isArray) {
                throw UnsupportedOperationException("Arrays not supported yet")
            }
            property(define(field.name, field.type)) {
                getter = inline(
                    getter {
                        val call = Call(extensionMethod(pkg, field.uniqueCGetter), ptr)
                        generateReturn(field.type, call)
                    }
                )
                if (!field.type.isConst) {
                    setter = inline(
                        setter { value ->
                            +Call(extensionMethod(pkg, field.uniqueCSetter), ptr, value.reference)
                        }
                    )
                }
            }
        }

    private fun CodeBuilder<KotlinFactory>.generateReturn(
        returnType: WrappedType,
        call: Call
    ) {
        when {
            returnType.kotlinType.isWrapper -> {
                +Return(Call(returnType.kotlinType.name, call pairedTo memScope))
            }
            returnType.isString ||
                (returnType.isPointer && returnType.pointed.isString) -> {
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
