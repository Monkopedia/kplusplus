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
package com.monkopedia.krapper.generator

import clang.CXChildVisitResult
import clang.CXCursor
import clang.CXCursorKind
import clang.CXTemplateArgumentKind.CXTemplateArgumentKind_Declaration
import clang.CXTemplateArgumentKind.CXTemplateArgumentKind_Expression
import clang.CXTemplateArgumentKind.CXTemplateArgumentKind_Integral
import clang.CXTemplateArgumentKind.CXTemplateArgumentKind_Invalid
import clang.CXTemplateArgumentKind.CXTemplateArgumentKind_Null
import clang.CXTemplateArgumentKind.CXTemplateArgumentKind_NullPtr
import clang.CXTemplateArgumentKind.CXTemplateArgumentKind_Pack
import clang.CXTemplateArgumentKind.CXTemplateArgumentKind_Template
import clang.CXTemplateArgumentKind.CXTemplateArgumentKind_TemplateExpansion
import clang.CXTemplateArgumentKind.CXTemplateArgumentKind_Type
import clang.clang_visitChildren
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.builders.LogPolicy
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.NameHandler
import com.monkopedia.krapper.generator.codegen.WrappedCppCompiler
import com.monkopedia.krapper.generator.codegen.WrappedCppWriter
import com.monkopedia.krapper.generator.codegen.WrappedDefWriter
import com.monkopedia.krapper.generator.codegen.WrappedHeaderWriter
import com.monkopedia.krapper.generator.codegen.WrappedKotlinWriter
import com.monkopedia.krapper.generator.codegen.getcwd
import com.monkopedia.krapper.generator.model.WrappedClass
import kotlinx.cinterop.CValue
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.staticCFunction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class ErrorPolicy(val policy: CodeGenerationPolicy) {
    FAIL(ThrowPolicy),
    LOG(LogPolicy)
}

enum class ReferencePolicy {
    IGNORE_MISSING,
    OPAQUE_MISSING,
    THROW_MISSING,
    INCLUDE_MISSING
}

class KrapperGen : CliktCommand() {
    val header by option(
        "-h",
        "--header",
        help = "Specify a header of classes to import"
    ).multiple()
    val library by option(
        "-l",
        "--lib",
        help = "Specify a library to that contains the header specified"
    ).multiple()
    val pkg by option("-p", "--package", help = "Desired package for wrappers to be placed")
    val compiler by option(
        "-c",
        "--compiler",
        help = "Compiler to use for creating wrapper module"
    ).default("clang++")
    val moduleName by argument(help = "Name of the wrapper module created")
    val output by option("-o", "--outdir", help = "Directory to place generated files")
    val errorPolicy by option("--policy", help = "How to handle errors")
        .enum<ErrorPolicy>()
        .default(ErrorPolicy.LOG)
    val debug by option("-d", "--debugOutput", help = "Specify file to output debug dump of state")

    val referencePolicy by option(
        "-r",
        "--referencePolicy",
        help = "Sets policy of how to handle non-included classes"
    )
        .enum<ReferencePolicy>()
        .default(ReferencePolicy.IGNORE_MISSING)

    override fun run() {
        memScoped {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val resolver = parseHeader(index, header)
            var classes = resolver.findClasses(WrappedClass::defaultFilter)
            classes = classes.resolveAll(resolver, referencePolicy)
            debug?.let {
                val clsStr = Json.encodeToString(classes)
                File(it).writeText(clsStr)
            }
            println(
                "Generating for [\n    ${classes.joinToString(",\n    ") { it.fullyQualified }}\n]"
            )
            val outputBase = File(output ?: getcwd())
            outputBase.mkdirs()
            val namer = NameHandler()
            File(outputBase, "$moduleName.h").writeText(
                CppCodeBuilder().also {
                    WrappedHeaderWriter(
                        namer,
                        it,
                        policy = errorPolicy.policy
                    ).generate(moduleName, header, classes)
                }.toString()
            )
            val cppFile = File(outputBase, "$moduleName.cc")
            cppFile.writeText(
                CppCodeBuilder().also {
                    WrappedCppWriter(namer, cppFile, it, policy = errorPolicy.policy).generate(
                        moduleName,
                        header,
                        classes
                    )
                }.toString()
            )
            val pkg = pkg ?: "krapper.$moduleName"
            File(outputBase, "$moduleName.def").writeText(
                WrappedDefWriter(namer).generateDef(
                    outputBase,
                    "$pkg.internal",
                    moduleName,
                    header,
                    library
                )
            )
            WrappedCppCompiler(File(outputBase, "lib$moduleName.a"), compiler).compile(
                cppFile,
                header,
                library
            )
            WrappedKotlinWriter(
                namer,
                "$pkg.internal",
                policy = errorPolicy.policy
            ).generate(
                File(outputBase, "src"),
                classes
            )
        }
    }
}

private val CValue<CXCursor>.templatedName: String
    get() {
//        if (numTemplateArguments != 0) {
//            return spelling.toKString() + "<" + (0 until numTemplateArguments).joinToString(",") {
//                when (getTemplateArgumentKind(it.toUInt())) {
//                    CXTemplateArgumentKind_Null -> "__NULL__"
//                    CXTemplateArgumentKind_Type -> getTemplateArgumentType(it.toUInt()).spelling.toKString() ?: ""
//                    CXTemplateArgumentKind_Declaration -> "__DECL__"
//                    CXTemplateArgumentKind_NullPtr -> "__NULL_PTR__"
//                    CXTemplateArgumentKind_Integral -> "__INTEGRAL__"
//                    CXTemplateArgumentKind_Template -> "__TYPE__"
//                    CXTemplateArgumentKind_TemplateExpansion -> "__TEMPLATE_EXPANSION__"
//                    CXTemplateArgumentKind_Expression -> "__EXPRESSION__"
//                    CXTemplateArgumentKind_Pack -> "__PACK__"
//                    CXTemplateArgumentKind_Invalid -> "__INVALID__"
//                }
//            } + ">"
//        }
        return spelling.toKString() ?: ""
    }
val CValue<CXCursor>?.fullyQualified: String
    get() =
        if (this == null || this == CXCursor.NULL) ""
        else if (kind == CXCursorKind.CXCursor_TranslationUnit) ""
        else {
            val res = semanticParent.fullyQualified
            if (res.isNotEmpty()) "$res::$templatedName"
            else templatedName ?: ""
        }

val recurseVisitor =
    staticCFunction {
        child: CValue<CXCursor>,
        _: CValue<CXCursor>,
        children: clang.CXClientData? ->
        children!!.asStableRef<(CValue<CXCursor>) -> Unit>()!!.get()!!
            .invoke(child)
        CXChildVisitResult.CXChildVisit_Recurse
    }

val visitor =
    staticCFunction {
        child: CValue<CXCursor>,
        _: CValue<CXCursor>,
        children: clang.CXClientData? ->
        children!!.asStableRef<(CValue<CXCursor>) -> Unit>()!!.get()!!
            .invoke(child)
        CXChildVisitResult.CXChildVisit_Continue
    }

inline fun CValue<CXCursor>.forEachRecursive(noinline childHandler: (CValue<CXCursor>) -> Unit) {
    val ptr = StableRef.create(childHandler)
    clang_visitChildren(this, recurseVisitor, ptr.asCPointer())
}

inline fun CValue<CXCursor>.forEach(noinline childHandler: (CValue<CXCursor>) -> Unit) {
    val ptr = StableRef.create(childHandler)
    clang_visitChildren(this, visitor, ptr.asCPointer())
}

inline fun CValue<CXCursor>.filterChildrenRecursive(
    crossinline filter: (CValue<CXCursor>) -> Boolean
): List<CValue<CXCursor>> {
    return mutableListOf<CValue<CXCursor>>().also { list ->
        forEachRecursive {
            if (filter(it)) {
                list.add(it)
            }
        }
    }
}

inline fun CValue<CXCursor>.filterChildren(
    crossinline filter: (CValue<CXCursor>) -> Boolean
): List<CValue<CXCursor>> {
    return mutableListOf<CValue<CXCursor>>().also { list ->
        forEach {
            if (filter(it)) {
                list.add(it)
            }
        }
    }
}

private val CValue<CXCursor>.allChildren: Collection<CValue<CXCursor>>
    get() {
        return mutableListOf<CValue<CXCursor>>().also { list ->
            forEachRecursive(list::add)
        }
    }

private val CValue<CXCursor>.children: Collection<CValue<CXCursor>>
    get() {
        return mutableListOf<CValue<CXCursor>>().also { list ->
            forEach(list::add)
        }
    }
