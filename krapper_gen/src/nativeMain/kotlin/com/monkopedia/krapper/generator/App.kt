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
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.monkopedia.krapper.AddToChild
import com.monkopedia.krapper.DefaultFilter
import com.monkopedia.krapper.ErrorPolicy.FAIL
import com.monkopedia.krapper.ErrorPolicy.LOG
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.KrapperService
import com.monkopedia.krapper.ReplaceChild
import com.monkopedia.krapper.addMapping
import com.monkopedia.krapper.addTypedMapping
import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.LogPolicy
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.getcwd
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.REINT_CAST
import com.monkopedia.krapper.generator.resolved_model.MethodType.METHOD
import com.monkopedia.krapper.generator.resolved_model.ResolvedArgument
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.COPY_CONSTRUCTOR
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.VOIDP
import com.monkopedia.krapper.generator.resolved_model.resolvedSerializerModule
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCType
import com.monkopedia.ksrpc.ErrorListener
import com.monkopedia.ksrpc.channels.asConnection
import com.monkopedia.ksrpc.channels.registerDefault
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.posixFileReadChannel
import com.monkopedia.ksrpc.posixFileWriteChannel
import kotlinx.cinterop.CValue
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import kotlin.system.exitProcess

val ErrorPolicy.policy: CodeGenerationPolicy
    get() = when (this) {
        FAIL -> ThrowPolicy
        LOG -> LogPolicy
    }

typealias ErrorPolicy = com.monkopedia.krapper.ErrorPolicy
typealias ReferencePolicy = com.monkopedia.krapper.ReferencePolicy

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
    val moduleName by argument(help = "Name of the wrapper module created").optional()
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
    val serviceMode by option(
        "-s",
        help = "Tells Krapper to host a ksrpc service on std in/out, and ignores all other options"
    ).flag()

    override fun run() {
        if (serviceMode) {
            return runService()
        }
        runBlocking {
            val service = KrapperServiceImpl()
            service.setConfig(
                KrapperConfig(
                    pkg = pkg ?: "krapper.$moduleName",
                    compiler = compiler,
                    moduleName = moduleName ?: File(header.first()).name,
                    errorPolicy = errorPolicy,
                    referencePolicy = referencePolicy,
                    debug = debug
                )
            )
            val indexService = service.index(IndexRequest(header, library))
            indexService.filterAndResolve(DefaultFilter)
//            for (file in header) {
//                val tu =
//                    index.parseTranslationUnit(file, args, null) ?: error("Failed to parse $file")
//                tu.printDiagnostics()
//                defer {
//                    tu.dispose()
//                }
//                        val info = Utils.CursorTreeInfo(tu.cursor)
//                        Log.i("Writing ${tu.cursor.usr.toKString()}")
//                        File("/tmp/full_tree.json").writeText(Json.encodeToString(info))
//                tu.cursor.filterChildrenRecursive {
//                    if (it.kind == CXCursorKind.CXCursor_ClassDecl && it.fullyQualified.contains("OtherClass")) {
//                        val info = Utils.CursorTreeInfo(it)
//                        Log.i("Writing ${it.usr.toKString()}")
//                        File("/tmp/testlib_otherclass.json").writeText(Json.encodeToString(info))
//                    }
//                    if (it.kind == CXCursorKind.CXCursor_ClassDecl && it.fullyQualified.contains("TestClass")) {
//                        val info = Utils.CursorTreeInfo(it)
//                        Log.i("Writing ${it.usr.toKString()}")
//                        File("/tmp/testlib_testClass.json").writeText(Json.encodeToString(info))
//                    }
//                    if (it.kind == CXCursorKind.CXCursor_ClassTemplate && it.fullyQualified.contains("basic_string") && !it.fullyQualified.contains("basic_stringbuf")) {
//                        val info = Utils.CursorTreeInfo(it)
//                        Log.i("Writing ${it.usr.toKString()}")
//                        File("/tmp/basic_string.json").writeText(Json.encodeToString(info))
//                    }
//                    false
//                }
// //                tu.cursor.filterChildren { true }.forEach {
// //                    val cursor = KXCursor.generate(tu.cursor)
// //                    Log.i("Cursor $cursor ${cursor?.children?.size} ${tu.cursor.kind}")
// //                }
//            }
            indexService.addTypedMapping(
                ResolvedMethod,
                filter = {
                    parent(qualified eq "v8::ScriptOrigin") and
                        (methodName eq "options")
                },
                handler = { element ->
                    Log.i("Setting return type on $element")
                    element.replaceWith(
                        element.copy(
                            returnStyle = COPY_CONSTRUCTOR,
                            returnType = element.returnType.copy(
                                typeString =
                                element.returnType.typeString.removePrefix("const ")
                                    .trimEnd('*')
                            )
                        )
                    )
                }
            )
            indexService.addTypedMapping(
                ResolvedMethod,
                filter = {
                    (methodReturnType startsWith "const v8::Local<") or
                        (methodReturnType startsWith "const v8::Maybe<") or
                        (methodReturnType startsWith "const v8::MaybeLocal<") or
                        (methodReturnType startsWith "const v8::ScriptOrigin<") or
                        (methodReturnType startsWith "const v8::Location<")
                },
                handler = { element ->
                    Log.i("Clearing const return type on $element")
                    listOf(
                        ReplaceChild(
                            element.copy(
                                returnType = element.returnType.copy(
                                    typeString = element.returnType.typeString.removePrefix(
                                        "const "
                                    )
                                )
                            )
                        )
                    )
                }
            )
            indexService.addTypedMapping(
                ResolvedMethod,
                filter = {
                    parent(qualified eq "v8::Persistent<v8::Value>")
                },
                handler = { element ->
                    if (element.uniqueCName == "_v8_Persistent_v8_Value_new" ||
                        element.uniqueCName == "v8_Persistent_v8_Value_op_assign" ||
                        element.uniqueCName == "v8_platform_tracing_TraceWriter_create_system_instrumentation_trace_writer"
                    ) {
                        Log.i("Removing $element")
                        element.remove()
                    }
                }
            )
//            indexService.addTypedMapping(ResolvedClass)
            indexService.addMapping(
                filter = {
                    (thiz isType ResolvedClass) and
                        (qualified startsWith "std::unique_ptr") and
                        (className eq "unique_ptr")
                },
                handler = { request ->
                    val parent = request.child
                    parent as ResolvedClass
                    val wrappedType = WrappedType(
                        parent.type.typeString.replace(
                            "std::unique_ptr<",
                            ""
                        ).removeSuffix(">")
                    )
                    listOf(
                        AddToChild(
                            ResolvedMethod(
                                "get",
                                parent.type.copy(
                                    typeString = wrappedType.toString(),
                                    kotlinType = toResolvedKotlinType(
                                        wrappedType.kotlinType
                                    ),
                                    cType = toResolvedCType(wrappedType.cType)
                                ),
                                METHOD,
                                "_custom_unique_ptr_get_${
                                parent.type.typeString.replace("<", "_").replace(">", "_")
                                    .replace("::", "_")
                                }",
                                null,
                                listOf(
                                    ResolvedArgument(
                                        "thiz",
                                        parent.type.copy(
                                            typeString = parent.type.type + "*",
                                            cType = ResolvedCType("void*", false)
                                        ),
                                        parent.type.copy(
                                            typeString = parent.type.type + "*",
                                            cType = ResolvedCType("void*", false)
                                        ),
                                        "",
                                        REINT_CAST,
                                        needsDereference = true,
                                        hasDefault = false
                                    )
                                ),
                                VOIDP,
                                false,
                                parent.type.typeString
                            ).also {
                                Log.i("Adding $it to $parent")
                            }
                        )
                    )
                }
            )
//            debug?.let {
//                val clsStr = Json.encodeToString(resolver.tu)
//                File(it).writeText(clsStr)
//            }
            indexService.writeTo(output ?: getcwd())
        }
    }

    private fun runService() {
        val input = posixFileReadChannel(STDIN_FILENO)
        val output = posixFileWriteChannel(STDOUT_FILENO)
        withoutIcanon {
            runBlocking {
                val connection = (input to output).asConnection(
                    ksrpcEnvironment {
                        serialization = Json {
                            serializersModule = resolvedSerializerModule
                        }
                        errorListener = ErrorListener { t ->
                            launch {
                                Log.e("Exception: " + t.message + "\n" + t.stackTraceToString())
                            }
                        }
                    }
                )
                connection.registerDefault<KrapperService>(KrapperServiceImpl())
                val deferred = CompletableDeferred<Unit>()
                connection.onClose {
                    exitProcess(0)
                }
            }
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

val CValue<CXCursor>.allChildren: Collection<CValue<CXCursor>>
    get() {
        return mutableListOf<CValue<CXCursor>>().also { list ->
            forEachRecursive { child, _ ->
                list.add(child)
            }
        }
    }

val CValue<CXCursor>.children: Collection<CValue<CXCursor>>
    get() {
        return mutableListOf<CValue<CXCursor>>().also { list ->
            forEach(list::add)
        }
    }
