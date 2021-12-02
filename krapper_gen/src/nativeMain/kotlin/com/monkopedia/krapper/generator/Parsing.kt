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

import clang.CXCursorKind
import clang.CXCursorKind.CXCursor_TypedefDecl
import clang.CXIndex
import clang.CXTranslationUnit
import clang.CXType
import clang.clang_defaultDiagnosticDisplayOptions
import clang.clang_disposeString
import clang.clang_formatDiagnostic
import clang.clang_getCString
import clang.clang_getDiagnostic
import clang.clang_getNumDiagnostics
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.WrappedTemplateParam
import com.monkopedia.krapper.generator.model.cloneRecursive
import com.monkopedia.krapper.generator.model.filterRecursive
import com.monkopedia.krapper.generator.model.forEachRecursive
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValue
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.posix.EOF
import platform.posix.close
import platform.posix.getenv
import platform.posix.read
import platform.posix.system
import platform.posix.write

typealias ClassFilter = WrappedClass.() -> Boolean

fun WrappedClass.defaultFilter(): Boolean {
    return !type.toString().startsWith("std::") && !type.toString().startsWith("__")
}

/**
 *
#include <...> search starts here:
/usr/lib/gcc/x86_64-pc-linux-gnu/11.1.0/../../../../include/c++/11.1.0
/usr/lib/gcc/x86_64-pc-linux-gnu/11.1.0/../../../../include/c++/11.1.0/x86_64-pc-linux-gnu
/usr/lib/gcc/x86_64-pc-linux-gnu/11.1.0/../../../../include/c++/11.1.0/backward
/usr/lib/gcc/x86_64-pc-linux-gnu/11.1.0/include
/usr/local/include
/usr/lib/gcc/x86_64-pc-linux-gnu/11.1.0/include-fixed
/usr/include
End of search list.
 */

fun generateIncludes() = memScoped {
    val emptyFile = File("/tmp/clang_includes.c")
    emptyFile.writeText("")
    val process = Process {
        system("clang++ -E -x c++ -v ${emptyFile.path}")
    }
    process.start()
    defer {
        process.kill()
    }
    val buffer = alloc<ByteVar> {
        EOF
    }
    write(process.stdIn(), buffer.ptr, 1)
    close(process.stdIn())
    process.wait()
    val readBuffer = allocArray<ByteVar>(256)
    var fullString = StringBuilder()
    var amount = read(process.stdOut(), readBuffer, 255)
    while (amount > 0) {
        readBuffer[amount.toInt()] = 0.toByte()
        fullString.append(readBuffer.toKStringFromUtf8())
        amount = read(process.stdOut(), readBuffer, 255)
    }
    val lines = fullString.split("\n")
    val start = lines.indexOf("#include <...> search starts here:")
    val end = lines.indexOf("End of search list.")
    if (start < 0 || end < 0) {
        throw IllegalStateException("Can't find includes for:\n$fullString")
    }
    return@memScoped lines.subList(start + 1, end).toList().map { it.trim() }.also {
        println("Found include locations $it")
    } + "."
}

fun find(s: String): String? {
    val paths = getenv("PATH")?.toKStringFromUtf8().orEmpty().split(":")
    for (path in paths) {
        val parent = File(path)
        val file = File(parent, s)
        if (file.exists()) {
            return file.path
        }
    }
    return error("Can't find $s in $paths")
}

// Obtained from 'g++ -E -x c++ - -v < /dev/null'
val INCLUDE_PATHS = generateIncludes()

class ParsedResolver(val tu: WrappedTU) : Resolver {
    private val classMap = mutableMapOf<String, WrappedClass>()
    private val templateMap = mutableMapOf<String, WrappedTemplate>()

    override fun resolveTemplate(type: WrappedType): WrappedTemplate {
        return templateMap.getOrPut(type.toString()) {
            val templateCandidates = tu.filterRecursive {
                ((it as? WrappedTemplate)?.qualified == type.toString())
            }
            templateCandidates.singleOrNull() as? WrappedTemplate
                ?: error("Can't resolve template $type (${type::class.simpleName})")
        }
    }

    override fun resolve(type: WrappedType): WrappedClass {
        return classMap.getOrPut(type.toString()) {
            when (type) {
                is WrappedTemplateType -> {
                    val template = resolveTemplate(type.baseType)
                    template.typedAs(type)
                }
                is WrappedTemplateRef -> {
                    throw IllegalArgumentException("Can't resolve $type since it is templated")
                }
                else -> {
                    tu.filterRecursive {
                        (it as? WrappedClass)?.type?.toString() == type.toString()
                    }.singleOrNull() as? WrappedClass
                        ?: error("Can't resolve $type (${type::class.simpleName})")
                }
            }
        }
    }

    override fun findClasses(filter: ClassFilter): List<WrappedClass> {
        println("Finding classes")
        return mutableListOf<WrappedClass>().also { ret ->
            tu.forEachRecursive {
                if ((it as? WrappedClass)?.filter() == true) {
                    ret.add(it)
                }
            }
        }.also {
            println("Found ${it.size} classes")
        }
    }
}

private class ResolverBuilderImpl : ResolverBuilder {
    private val seenNames = mutableMapOf<String, CValue<CXType>>()
    val classes = mutableListOf<WrappedClass>()
    val desiredTemplates = mutableListOf<CValue<CXType>>()

    override fun visit(type: CValue<CXType>): CValue<CXType> {
        val strType = type.spelling.toKString()?.trim() ?: return type
        return seenNames.getOrPut(strType) {
            val declaration = type.typeDeclaration
            when (declaration.kind) {
                CXCursorKind.CXCursor_ClassDecl -> {
                    val fullyQualified = declaration.fullyQualified
                    seenNames[strType] = type
                    val std = StdPopulator.maybeCreate(fullyQualified, type, this)
                    if (std != null) {
                        classes.add(std)
                    } else {
                        if (type.numTemplateArguments <= 0) {
                            classes.add(WrappedClass(declaration, this))
                        } else {
                            desiredTemplates.add(type)
                        }
                    }
                    type
                }
                CXCursor_TypedefDecl -> {
                    visit(type.typeDeclaration.typedefDeclUnderlyingType)
                }
                else -> {
                    type
                }
            }
        }
    }
}

fun MemScope.parseHeader(
    index: CXIndex,
    file: List<String>,
    args: Array<String> = arrayOf("-xc++", "--std=c++14") + INCLUDE_PATHS.map { "-I$it" }
        .toTypedArray(),
    debug: Boolean = false
): Resolver {
    val builder = ResolverBuilderImpl()
    val tu = file.map { parseHeader(index, it, builder, args, debug) }
        .reduceRight { tu1, tu2 ->
            tu1.also {
                it.addAllChildren(
                    tu2.children.map {
                        it.also { it.parent = tu1 }
                    }
                )
            }
        }
    println("Reduced ${tu.children.size}")
    return ParsedResolver(tu)
}

private fun MemScope.parseHeader(
    index: CXIndex,
    file: String,
    resolverBuilder: ResolverBuilder,
    args: Array<String> = arrayOf("-xc++", "--std=c++14") + INCLUDE_PATHS.map { "-I$it" }
        .toTypedArray(),
    debug: Boolean = false
): WrappedTU {
    val tu = index.parseTranslationUnit(file, args, null) ?: error("Failed to parse $file")
    tu.printDiagnostics()
    defer {
        tu.dispose()
    }
    val cursor = tu.cursor
    if (debug) {
        File(
            File("/tmp"),
            "cursor_${File(file).name}.json"
        ).writeText(Json.encodeToString(Utils.CursorTreeInfo(cursor)))
    }
//    val classes = cursor.filterChildrenRecursive {
//        it.kind == CXCursorKind.CXCursor_ClassDecl
//    }.map { WrappedClass(it, resolverBuilder) }
//    val templates = cursor.filterChildrenRecursive {
//        it.kind == CXCursorKind.CXCursor_ClassTemplate
//    }.map {
// //        if (it.fullyQualified.startsWith("std::_Vector_base")) {
// //            val info = CursorTreeInfo(it)
// //            File("/tmp/std_vector_base.json").writeText(Json.encodeToString(info))
// //        }
//        WrappedTemplate(it, resolverBuilder).also {
// //            println("Created template $it")
//        }
//    }
    val element = WrappedElement.mapAll(tu.cursor, resolverBuilder)
    return element as? WrappedTU ?: error("$element is not a WrappedTU, ${tu.cursor.kind}")
}

fun CXTranslationUnit.printDiagnostics() {
    val nbDiag = clang_getNumDiagnostics(this)
    println("There are $nbDiag diagnostics:")

    var foundError = false
    for (currentDiag in 0 until nbDiag.toInt()) {
        val diagnotic = clang_getDiagnostic(this, currentDiag.toUInt())
        val errorString =
            clang_formatDiagnostic(diagnotic, clang_defaultDiagnosticDisplayOptions())
        val str = clang_getCString(errorString)?.toKString()
        clang_disposeString(errorString)
        if (str?.contains("error:") == true) {
            foundError = true
        }
        println("$str")
    }
    if (foundError) {
        throw RuntimeException("Found errors parsing")
    }
}

fun WrappedTemplate.typedAs(templateSpec: WrappedTemplateType): WrappedClass {
    val fullyQualified = templateSpec.baseType.toString()
    val templates =
        filterRecursive { it is WrappedTemplateParam }.filterIsInstance<WrappedTemplateParam>()
    val mapping = (
        templates.mapIndexedNotNull { index, t ->
            when {
                index < templateSpec.templateArgs.size -> t.name to templateSpec.templateArgs[index]
                t.defaultType != null -> t.name to t.defaultType
                else -> null
            }
        } + templates.mapIndexedNotNull { index, t ->
            when {
                index < templateSpec.templateArgs.size -> t.usr to templateSpec.templateArgs[index]
                t.defaultType != null -> t.usr to t.defaultType
                else -> null
            }
        }
        ).toMap()
    val outputClass =
        WrappedClass(name, templateSpec)
    outputClass.parent = parent
    outputClass.addAllChildren(children.map { it.cloneRecursive() })
    return when (
        val result = map(outputClass, null) { type, parent ->
            type.operateOn { type ->
                if (type is WrappedTemplateRef) {
                    val mapping = mapping[type.target]
                    if (mapping != null) {
                        ReplaceWith(mapping)
                    } else {
                        RemoveElement
                    }
                } else if (parent !is WrappedTemplateType && type.toString() == fullyQualified) {
                    ReplaceWith(templateSpec)
                } else {
                    ElementUnchanged
                }
            }
        }
    ) {
        RemoveElement -> throw IllegalArgumentException("Can't map $outputClass")
        ElementUnchanged -> outputClass
        is ReplaceWith -> result.replacement
    }.also {
    }
}
