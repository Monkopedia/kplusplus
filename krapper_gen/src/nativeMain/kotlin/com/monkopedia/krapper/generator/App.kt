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
import clang.clang_visitChildren
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.builders.LogPolicy
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.codegen.CppCompiler
import com.monkopedia.krapper.generator.codegen.CppWriter
import com.monkopedia.krapper.generator.codegen.DefWriter
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.HeaderWriter
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import com.monkopedia.krapper.generator.codegen.NameHandler
import com.monkopedia.krapper.generator.codegen.getcwd
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.COPY_CONSTRUCTOR
import com.monkopedia.krapper.generator.resolved_model.recursiveSequence
import kotlinx.cinterop.CValue
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.staticCFunction

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
    val debug by option(
        "-d",
        "--debugOutput",
        help = "Specify file to output debug dump of state"
    ).flag()

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
            val args: Array<String> = arrayOf("-xc++", "--std=c++14") +
                INCLUDE_PATHS.map { "-I$it" }.toTypedArray()
            println("Args: ${args.toList()}")
//            for (file in header) {
//                val tu =
//                    index.parseTranslationUnit(file, args, null) ?: error("Failed to parse $file")
//                tu.printDiagnostics()
//                defer {
//                    tu.dispose()
//                }
//                        val info = Utils.CursorTreeInfo(tu.cursor)
//                        println("Writing ${tu.cursor.usr.toKString()}")
//                        File("/tmp/full_tree.json").writeText(Json.encodeToString(info))
//                tu.cursor.filterChildrenRecursive {
//                    if (it.kind == CXCursorKind.CXCursor_ClassDecl && it.fullyQualified.contains("OtherClass")) {
//                        val info = Utils.CursorTreeInfo(it)
//                        println("Writing ${it.usr.toKString()}")
//                        File("/tmp/testlib_otherclass.json").writeText(Json.encodeToString(info))
//                    }
//                    if (it.kind == CXCursorKind.CXCursor_ClassDecl && it.fullyQualified.contains("TestClass")) {
//                        val info = Utils.CursorTreeInfo(it)
//                        println("Writing ${it.usr.toKString()}")
//                        File("/tmp/testlib_testClass.json").writeText(Json.encodeToString(info))
//                    }
//                    if (it.kind == CXCursorKind.CXCursor_ClassTemplate && it.fullyQualified.contains("basic_string") && !it.fullyQualified.contains("basic_stringbuf")) {
//                        val info = Utils.CursorTreeInfo(it)
//                        println("Writing ${it.usr.toKString()}")
//                        File("/tmp/basic_string.json").writeText(Json.encodeToString(info))
//                    }
//                    false
//                }
// //                tu.cursor.filterChildren { true }.forEach {
// //                    val cursor = KXCursor.generate(tu.cursor)
// //                    println("Cursor $cursor ${cursor?.children?.size} ${tu.cursor.kind}")
// //                }
//            }
            val resolver = parseHeader(index, header, debug = debug)
            val initialClasses = resolver.findClasses(WrappedClass::defaultFilter)
            println(
                "Resolving: [\n    ${initialClasses.joinToString(",\n    ") { it.type.toString() }}\n]"
            )
            val classes = initialClasses.resolveAll(resolver, referencePolicy)
            println("Running mapping")
            classes.recursiveSequence().filterIsInstance<ResolvedMethod>().filter {
                it.uniqueCName == "v8_ScriptOrigin_options"
//                (it.parent as? ResolvedClass)?.type.toString() == "v8::ScriptOrigin" && it.name == "Options"
            }.forEach {
                it.returnStyle = COPY_CONSTRUCTOR
                it.returnType.typeString =
                    it.returnType.typeString.removePrefix("const ").removeSuffix("*")
                println("Setting return type on $it")
            }
            classes.recursiveSequence().filterIsInstance<ResolvedMethod>().filter {
                it.returnType.toString().startsWith("const v8::Local<") ||
                    it.returnType.toString().startsWith("const v8::Maybe<") ||
                    it.returnType.toString().startsWith("const v8::MaybeLocal<") ||
                    it.returnType.toString() == "const v8::ScriptOrigin*" ||
                    it.returnType.toString() == "const v8::Location*"
            }.forEach {
                it.returnType.typeString = it.returnType.typeString.removePrefix("const ")
                println("Clearing const return type on $it")
            }
            classes.recursiveSequence().filterIsInstance<ResolvedMethod>().filter {
                it.uniqueCName == "___v8_Persistent_v8_Value_new" || it.uniqueCName == "_v8_Persistent_v8_Value_op_assign"
            }.forEach {
                it.parent?.removeChild(it)
                println("Removing $it")
            }
//            debug?.let {
//                val clsStr = Json.encodeToString(resolver.tu)
//                File(it).writeText(clsStr)
//            }
            println(
                "Generating for [\n    ${classes.joinToString(",\n    ") { it.type.toString() }}\n]"
            )
            val outputBase = File(output ?: getcwd())
            outputBase.mkdirs()
            val namer = NameHandler()
            File(outputBase, "$moduleName.h").writeText(
                CppCodeBuilder().also {
                    HeaderWriter(
                        namer,
                        it,
                        policy = errorPolicy.policy
                    ).generate(moduleName, header, classes)
                }.toString()
            )
            val cppFile = File(outputBase, "$moduleName.cc")
            cppFile.writeText(
                CppCodeBuilder().also {
                    CppWriter(namer, cppFile, it, policy = errorPolicy.policy).generate(
                        moduleName,
                        header,
                        classes
                    )
                }.toString()
            )
            val pkg = pkg ?: "krapper.$moduleName"
            File(outputBase, "$moduleName.def").writeText(
                DefWriter(namer).generateDef(
                    outputBase,
                    "$pkg.internal",
                    moduleName,
                    header,
                    library
                )
            )
            CppCompiler(File(outputBase, "lib$moduleName.a"), compiler).compile(
                cppFile,
                header,
                library
            )
            KotlinWriter(
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

typealias ChildVisitor = (child: CValue<CXCursor>, parent: CValue<CXCursor>) -> Unit

val recurseVisitor =
    staticCFunction { child: CValue<CXCursor>,
        parent: CValue<CXCursor>,
        children: clang.CXClientData? ->
        children!!.asStableRef<ChildVisitor>().get().invoke(child, parent)
        CXChildVisitResult.CXChildVisit_Recurse
    }

val visitor =
    staticCFunction { child: CValue<CXCursor>,
        _: CValue<CXCursor>,
        children: clang.CXClientData? ->
        children!!.asStableRef<(CValue<CXCursor>) -> Unit>().get()
            .invoke(child)
        CXChildVisitResult.CXChildVisit_Continue
    }

inline fun CValue<CXCursor>.forEachRecursive(noinline childHandler: ChildVisitor) {
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
        forEachRecursive { child, _ ->
            if (filter(child)) {
                list.add(child)
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

inline fun <T> CValue<CXCursor>.mapChildren(
    crossinline filter: (CValue<CXCursor>) -> T
): List<T> {
    return mutableListOf<T>().also { list ->
        forEach {
            list.add(filter(it))
        }
    }
}

private val CValue<CXCursor>.allChildren: Collection<CValue<CXCursor>>
    get() {
        return mutableListOf<CValue<CXCursor>>().also { list ->
            forEachRecursive { child, _ ->
                list.add(child)
            }
        }
    }

private val CValue<CXCursor>.children: Collection<CValue<CXCursor>>
    get() {
        return mutableListOf<CValue<CXCursor>>().also { list ->
            forEach(list::add)
        }
    }
