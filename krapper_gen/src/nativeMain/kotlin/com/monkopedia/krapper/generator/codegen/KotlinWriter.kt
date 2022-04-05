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
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.STABLE_REF
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.STABLE_REF_CREATE
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.STATIC_C_FUNCTION
import com.monkopedia.krapper.generator.builders.KotlinLocalVar
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.builders.Raw
import com.monkopedia.krapper.generator.builders.Return
import com.monkopedia.krapper.generator.builders.Symbol
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.builders.appendLine
import com.monkopedia.krapper.generator.builders.asserting
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
import com.monkopedia.krapper.generator.builders.lambda
import com.monkopedia.krapper.generator.builders.operator
import com.monkopedia.krapper.generator.builders.pkg
import com.monkopedia.krapper.generator.builders.property
import com.monkopedia.krapper.generator.builders.qdot
import com.monkopedia.krapper.generator.builders.reference
import com.monkopedia.krapper.generator.builders.setter
import com.monkopedia.krapper.generator.builders.symbol
import com.monkopedia.krapper.generator.builders.type
import com.monkopedia.krapper.generator.resolved_model.AllocationStyle.DIRECT
import com.monkopedia.krapper.generator.resolved_model.AllocationStyle.STACK
import com.monkopedia.krapper.generator.resolved_model.MethodType
import com.monkopedia.krapper.generator.resolved_model.MethodType.SIZE_OF
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedConstructor
import com.monkopedia.krapper.generator.resolved_model.ResolvedDestructor
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.ResolvedField
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.ARG_CAST
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedType
import com.monkopedia.krapper.generator.resolved_model.type.fullyQualifiedType
import com.monkopedia.krapper.generator.resolved_model.type.nullable
import com.monkopedia.krapper.generator.resolved_model.type.typedWith

class KotlinWriter(
    private val pkg: String,
    policy: CodeGenerationPolicy = ThrowPolicy
) : CodeGeneratorBase<KotlinCodeBuilder>(policy) {
    private var currentClasses = mapOf<String, ResolvedClass>()
    private var needsCCaller = false
    private val staticRouterPkg = "krapper.static"
    private val staticRouterName = "router"
    private val staticRouter = "$staticRouterPkg.$staticRouterName"

    fun generate(outputDir: File, classes: List<ResolvedElement>) {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        for (file in outputDir.listFiles()) {
            file.delete()
        }
        needsCCaller = false
        currentClasses =
            classes.filterIsInstance<ResolvedClass>().associateBy { it.type.toString() }
        for (cls in currentClasses.values) {
            val clsFile =
                File(outputDir, cls.type.kotlinType.fullyQualified.replace(".", "_") + ".kt")
            val builder = KotlinCodeBuilder()
            builder.generate(cls)
            clsFile.writeText(builder.toString())
        }
        val methodsByPkg = classes.filterIsInstance<ResolvedMethod>().groupBy { it.qualified }
        for ((qualified, methods) in methodsByPkg) {
            val clsFile = File(outputDir, "${qualified.replace("::", "_")}_Functions.kt")
            val builder = KotlinCodeBuilder()
            val pkg = qualified.split("::").joinToString(".") { it.decapitalize() }
            builder.pkg(pkg)
            builder.importBlock(pkg, builder)
            builder.comment("BEGIN KRAPPER GEN for $pkg Functions")
            for (method in methods) {
                builder.onGenerate(method)
            }
            builder.comment("END KRAPPER GEN for $pkg Functions")
            clsFile.writeText(builder.toString())
        }
        if (needsCCaller) {
            // kotlinx.cinterop.asStableRef
            // kotlinx.cinterop.staticCFunction
            // val method = staticCFunction { arg1: COpaquePointer, arg2: COpaquePointer ->
            //    val callback = arg1.asStableRef<(COpaquePointer) -> Unit>().get()
            //    callback(arg2)
            // }
            val clsFile = File(outputDir, "_Krapper_Static_Router.kt")
            val builder = KotlinCodeBuilder().apply {
                pkg(staticRouterPkg)
                importBlock(staticRouterPkg, this)
                comment("BEGIN KRAPPER GEN for static C function Router")

                +define(
                    staticRouterName,
                    initializer = lambda {
                        type = type(fullyQualifiedType(STATIC_C_FUNCTION))
                        val arg1 = define("arg1", nullable(fullyQualifiedType(C_OPAQUE_POINTER)))
                        val arg2 = define("arg2", fullyQualifiedType(C_OPAQUE_POINTER))
                        body {
                            val callback = +define(
                                "callback",
                                initializer = arg2.reference dot Call(
                                    extensionMethod(STABLE_REF),
                                    templateArgs = listOf(Raw("(COpaquePointer?) -> Unit"))
                                ) dot Call(Raw("get"))
                            )
                            +Call(callback.reference, arg1.reference)
                        }
                    }
                )

                comment("END KRAPPER GEN for static C function Router")
            }

            clsFile.writeText(builder.toString())
        }
    }

    override fun KotlinCodeBuilder.onGenerate(
        cls: ResolvedClass,
        handleChildren: KotlinCodeBuilder.() -> Unit
    ) {
        val type = cls.type.kotlinType
        val pkg = type.pkg
        pkg(pkg)

        importBlock(pkg, this)
        comment("BEGIN KRAPPER GEN for ${cls.type}")
        appendLine()
        val ptr = define(ptr.content, fullyQualifiedType(C_OPAQUE_POINTER))
        val memScope = define(memScope.content, fullyQualifiedType(MEM_SCOPE))
        cls(named(type), listOf(property(ptr), property(memScope))) {
            handleSuperClassesRecursive(cls)
            handleChildren()
        }
        appendLine()
        comment("END KRAPPER GEN for ${cls.type}")
        appendLine()
        appendLine()
    }

    private fun KotlinCodeBuilder.handleSuperClassesRecursive(cls: ResolvedClass) {
        if (cls.baseClass == null) return
        val superClass = currentClasses[cls.baseClass.toString()]
            ?: error("Can't find specified base class ${cls.baseClass}")
        val methods = superClass.children.filterIsInstance<ResolvedMethod>()
        methods.filter {
            it.methodType == MethodType.STATIC_OP || it.methodType == MethodType.METHOD
        }.forEach { method ->
            try {
                onGenerate(superClass, method)
            } catch (t: Throwable) {
                codeGenerationPolicy.onGenerateMethodFailed(cls, method, t)
            }
        }
        for (field in cls.children.filterIsInstance<ResolvedField>()) {
            try {
                onGenerate(cls, field)
            } catch (t: Throwable) {
                codeGenerationPolicy.onGenerateFieldFailed(cls, field, t)
            }
        }
        handleSuperClassesRecursive(superClass)
    }

    override fun KotlinCodeBuilder.onGenerateMethods(cls: ResolvedClass) {
        val methods = cls.children.filterIsInstance<ResolvedMethod>()
        methods.filter {
            it.methodType == MethodType.STATIC_OP || it.methodType == MethodType.METHOD
        }.forEach { method ->
            try {
                onGenerate(cls, method, null)
            } catch (t: Throwable) {
                codeGenerationPolicy.onGenerateMethodFailed(cls, method, t)
            }
        }
        companion {
            val sizeOf = methods.find { (it as? ResolvedMethod)?.methodType == SIZE_OF }!!
            val size = define(
                "size",
                sizeOf.returnType,
            )
            +property(size) {
                getter = inline(
                    getter {
                        +Return(Call(extensionMethod(pkg, sizeOf.uniqueCName!!)))
                    }
                )
            }
            for (
                method in methods.filter {
                    it.methodType == MethodType.CONSTRUCTOR || it.methodType == MethodType.STATIC
                }
            ) {
                try {
                    onGenerate(cls, method, size)
                } catch (t: Throwable) {
                    codeGenerationPolicy.onGenerateMethodFailed(cls, method, t)
                }
            }
            val defaultConstructor =
                cls.children.filterIsInstance<ResolvedConstructor>()
                    .firstOrNull { it.args.isEmpty() }
            val destructor =
                cls.children.filterIsInstance<ResolvedDestructor>().firstOrNull()
            extensionFunction {
                receiver = fqType(MEM_SCOPE)
                name = cls.type.kotlinType.name.trimEnd('?') + "_Holder"
                retType = type(cls.type)
                body {
                    if (defaultConstructor != null) {
                        +Return(Call(constructorMethod(cls.type.kotlinType)))
                        return@body
                    }
                    val obj = +define(
                        "memory",
                        fullyQualifiedType(C_OPAQUE_POINTER),
                        initializer = (
                            Call(
                                extensionMethod("kotlinx.cinterop", "interpretCPointer"),
                                Call(
                                    "alloc",
                                    size.reference,
                                    size.reference
                                ) dot Raw("rawPtr")
                            ) elvis Call("error", "Allocation failed".symbol)
                            )
                    )
                    obj.isVal = true
                    if (destructor != null) {
                        defer {
                            +Call(
                                extensionMethod(
                                    pkg,
                                    destructor.uniqueCName
                                        ?: error("Unnamed destructor in $cls")
                                ),
                                obj.reference
                            )
                        }
                    }
                    +Return(
                        generateConstructorCall(
                            cls.type.kotlinType,
                            obj.reference,
                            thiz.reference
                        )
                    )
                }
            }
        }
    }

    private fun named(name: ResolvedKotlinType) = Raw(name.name)

    private val ptr = Raw("ptr")
    private val memScope = Raw("memScope")

    override fun KotlinCodeBuilder.onGenerate(cls: ResolvedClass, method: ResolvedMethod) {
        onGenerate(cls, method, null)
    }

    override fun KotlinCodeBuilder.onGenerate(method: ResolvedMethod) {
        val uniqueCName =
            extensionMethod(pkg, method.uniqueCName ?: error("Unnamed method $method"))
        require(method.methodType == MethodType.STATIC) {
            "Non-static method being generated at top level $method"
        }
        inline {
            extensionFunction {
                receiver = fqType(MEM_SCOPE)
                name = method.name.kotlinMethodName()
                val returnType = method.returnType
                val returnStyle = method.returnStyle
                retType = type(returnType)
                var args = if (method.args.isNotEmpty()) method.args.map {
                    define(it.name, it.type)
                } else emptyList()
                body {
                    generateMethodBody(
                        args.map { reference(it) },
                        returnStyle,
                        method.returnType,
                        uniqueCName,
                    )
                }
            }
        }
    }

    fun KotlinCodeBuilder.onGenerate(cls: ResolvedClass, method: ResolvedMethod, size: LocalVar?) {
        val uniqueCName =
            extensionMethod(pkg, method.uniqueCName ?: error("Unnamed method $method"))
        when (method.methodType) {
            MethodType.CONSTRUCTOR -> {
                generateConstructor(cls, method as ResolvedConstructor, size, uniqueCName)
            }
            MethodType.DESTRUCTOR -> {
                // Do nothing
            }
            MethodType.STATIC -> {
                onGenerate(method)
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

    private fun KotlinCodeBuilder.generateConstructor(
        cls: ResolvedClass,
        method: ResolvedConstructor,
        size: LocalVar?,
        uniqueCName: Symbol
    ) {
        when (method.allocationStyle) {
            DIRECT -> generateDirectConstructor(cls, method, size, uniqueCName)
            STACK -> generateStackConstructor(cls, method, uniqueCName)
        }
    }

    private fun KotlinCodeBuilder.generateStackConstructor(
        cls: ResolvedClass,
        method: ResolvedConstructor,
        uniqueCName: Symbol
    ) {
        needsCCaller = true
        extensionFunction {
            receiver = fqType(MEM_SCOPE)
            name = cls.type.kotlinType.name
            retType = type(ResolvedType.UNIT.copy())
            val args = method.args.subList(1, method.args.size).map {
                define(it.name, it.type)
            }.toMutableList()
            val callbackArg = args.removeLast()
            body {
                val lambda = +define(
                    "callback",
                    initializer = lambda {
                        val opaquePointer =
                            define("ptr", nullable(fullyQualifiedType(C_OPAQUE_POINTER)))
                        body {
                            val ptr = +define(
                                "ptr",
                                fullyQualifiedType(C_OPAQUE_POINTER),
                                opaquePointer.reference elvis Call(
                                    "error",
                                    "Creation failed".symbol
                                )
                            )
                            +Call(
                                callbackArg.reference,
                                generateConstructorCall(
                                    cls.type.kotlinType,
                                    ptr.reference,
                                    thiz.reference,
                                )
                            )
                        }
                    }
                )
                val stableRef = +define(
                    "withObjStable",
                    initializer = Call(
                        extensionMethod(STABLE_REF_CREATE),
                        lambda.reference
                    )
                )
                +Call(
                    uniqueCName,
                    *(
                        listOf(stableRef.reference dot Call("asCPointer")) +
                            args.map { reference(it) } +
                            extensionMethod(staticRouter)
                        ).toTypedArray()
                )
                +(stableRef.reference dot Call("dispose"))
            }
        }
    }

    private fun KotlinCodeBuilder.generateDirectConstructor(
        cls: ResolvedClass,
        method: ResolvedConstructor,
        size: LocalVar?,
        uniqueCName: Symbol
    ) {
        val destructor =
            cls.children.filterIsInstance<ResolvedDestructor>().firstOrNull()
        extensionFunction {
            receiver = fqType(MEM_SCOPE)
            name = cls.type.kotlinType.name
            retType = type(cls.type)
            val args = method.args.subList(1, method.args.size).map {
                define(it.name, it.type)
            }
            body {
                val memory = +define(
                    "memory",
                    fullyQualifiedType(C_OPAQUE_POINTER),
                    initializer = (
                        Call(
                            extensionMethod("kotlinx.cinterop", "interpretCPointer"),
                            Call(
                                "alloc",
                                size!!.reference,
                                size.reference
                            ) dot Raw("rawPtr")
                        ) elvis Call("error", "Allocation failed".symbol)
                        )
                )
                memory.isVal = true
                val obj = +define(
                    "obj",
                    fullyQualifiedType(C_OPAQUE_POINTER),
                    initializer = (
                        Call(
                            uniqueCName,
                            *(listOf(memory.reference) + args.map { reference(it) })
                                .toTypedArray()
                        ) elvis Call("error", "Creation failed".symbol)
                        )
                )
                obj.isVal = true
                if (destructor != null) {
                    defer {
                        +Call(
                            extensionMethod(
                                pkg,
                                destructor.uniqueCName
                                    ?: error("Unnamed destructor in $cls")
                            ),
                            obj.reference
                        )
                    }
                }
                +Return(
                    generateConstructorCall(cls.type.kotlinType, obj.reference, thiz.reference)
                )
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

    private fun String.kotlinMethodName() = replace("<", "_lt")
        .replace(">", "_gt")
        .replace("\"\"", "_qts")
        .replace("\"", "_qt")
        .replace("==", "_cmd")
        .replace("=", "_eq")
        .replace("+", "_plus")
        .replace("-", "_minus")
        .replace("/", "_div")
        .replace("*", "_star")
        .replace("%", "_mod")
        .replace("!", "_not")

    private fun KotlinCodeBuilder.generateBasicMethod(
        method: ResolvedMethod,
        uniqueCName: Symbol,
        methodName: String = method.name,
        startArgs: List<Symbol> = listOf(ptr),
        skipFirstArg: Boolean = true
    ) {
        function {
            name = methodName.kotlinMethodName()
            val returnType = method.returnType
            val returnStyle = method.returnStyle
            retType = type(returnType)
            var args = if (method.args.isNotEmpty()) method.args.subList(
                if (skipFirstArg) 1 else 0,
                method.args.size
            ).map {
                define(it.name, it.type)
            } else emptyList()
            body {
                generateMethodBody(
                    startArgs + args.map { reference(it) },
                    returnStyle,
                    method.returnType,
                    uniqueCName,
                )
            }
        }
    }

    private fun CodeBuilder<KotlinFactory>.generateMethodBody(
        args: List<Symbol>,
        returnStyle: ReturnStyle,
        returnType: ResolvedCppType,
        uniqueCName: Symbol,
    ) {
        val kotlinType = returnType.kotlinType
        if (returnStyle == ARG_CAST && kotlinType.isWrapper) {
            val ret = define(
                "retValue",
                returnType,
                initializer = memScope dot Call(
                    extensionMethod(
                        kotlinType.fullyQualified + ".Companion",
                        kotlinType.name.trimEnd('?') + "_Holder"
                    )
                ),
            ).also {
                it.isVal = true
            }
            +ret
            +Call(
                uniqueCName,
                *(args + reference(ret)).toTypedArray()
            )
            +Return(ret.reference)
        } else {
            generateReturn(
                kotlinType,
                Call(
                    uniqueCName,
                    *args.toTypedArray()
                )
            )
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

    private fun reference(type: ResolvedKotlinType?, v: LocalVar): Symbol {
        return if (type != null && type.isWrapper) {
            if (type.toString().endsWith("?")) {
                v.reference qdot ptr
            } else {
                v.reference dot ptr
            }
        } else v.reference
    }

    override fun KotlinCodeBuilder.onGenerate(cls: ResolvedClass, field: ResolvedField) {
        +property(define(field.name, field.kotlinType)) {
            getter = inline(
                getter {
                    generateMethodBody(
                        listOf(ptr),
                        field.getter.returnStyle,
                        field.getter.returnType,
                        extensionMethod(pkg, field.getter.uniqueCName!!)
                    )
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
                    generateConstructorCall(
                        returnType,
                        if (returnType.isNullable) call elvis Return(Raw("null"))
                        else asserting(call),
                        memScope
                    )
                )
            }
            returnType.fullyQualified == "kotlin.String" -> {
                generateStringReturn(call)
            }
            else -> {
                +Return(call)
            }
        }
    }

    private fun generateConstructorCall(
        type: ResolvedKotlinType,
        ptr: Symbol,
        memScope: Symbol
    ): Symbol {
        return Call(constructorMethod(type), ptr, memScope)
    }

    private fun constructorMethod(type: ResolvedKotlinType) = extensionMethod(
        type.pkg,
        type.name.trimEnd('?')
    )

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
            fullyQualifiedType("kotlin.String?"),
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
