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
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.WrappedTypeReference
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
import platform.posix.EOF
import platform.posix.close
import platform.posix.getenv
import platform.posix.read
import platform.posix.system
import platform.posix.write

typealias ClassFilter = WrappedClass.() -> Boolean

fun WrappedClass.defaultFilter(): Boolean {
    return !fullyQualified.startsWith("std::") && !fullyQualified.startsWith("__")
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

private class ParsedResolver(classes: List<WrappedClass>, templates: List<WrappedTemplate>) :
    Resolver {
    private val classMap = classes.associateBy { it.fullyQualified }

    override fun resolve(fullyQualified: String): WrappedClass {
        return classMap[fullyQualified] ?: error("Can't resolve $fullyQualified")
    }

    override fun findClasses(filter: ClassFilter): List<WrappedClass> {
        return classMap.values.filter { it.filter() }
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
                    println("Typedef ${declaration.prettyPrinted.toKString()}")
                    visit(type.typeDeclaration.typedefDeclUnderlyingType)
                }
                else -> {
                    println("Not sure how to handle $strType with kind ${declaration.kind}")
                    type
                }
            }
        }
    }

    override fun visit(type: WrappedTypeReference) {
        val strType = type.name
//        if (!seenNames.add(strType)) return

        classes.add(
            StdPopulator.maybePopulate(type, this) ?: return
        )
    }
}

fun MemScope.parseHeader(
    index: CXIndex,
    file: List<String>,
    args: Array<String> = arrayOf("-xc++", "--std=c++14") + INCLUDE_PATHS.map { "-I$it" }
        .toTypedArray()
): Resolver {
    val builder = ResolverBuilderImpl()
    val (expected, templates) = file.map { parseHeader(index, it, builder, args) }
        .reduceRight { (f1, s1), (f2, s2) ->
            (f1 + f2) to (s1 + s2)
        }
    return ParsedResolver(expected + builder.classes, templates)
}

private fun MemScope.parseHeader(
    index: CXIndex,
    file: String,
    resolverBuilder: ResolverBuilder,
    args: Array<String> = arrayOf("-xc++", "--std=c++14") + INCLUDE_PATHS.map { "-I$it" }
        .toTypedArray(),
): Pair<List<WrappedClass>, List<WrappedTemplate>> {
    val tu = index.parseTranslationUnit(file, args, null) ?: error("Failed to parse $file")
    tu.printDiagnostics()
    defer {
        tu.dispose()
    }
    val cursor = tu.cursor
    val classes = cursor.filterChildrenRecursive {
        it.kind == CXCursorKind.CXCursor_ClassDecl
    }.map { WrappedClass(it, resolverBuilder) }
    val templates = cursor.filterChildrenRecursive {
        it.kind == CXCursorKind.CXCursor_ClassTemplate
    }.map {
//        if (it.fullyQualified.startsWith("std::vector")) {
//            val info = CursorTreeInfo(it)
// //            println(Json.encodeToString(info))
//            File("/tmp/std_vector.json").writeText(Json.encodeToString(info))
//        }
//        if (it.fullyQualified.startsWith("std::_Vector_base")) {
//            val info = CursorTreeInfo(it)
//            File("/tmp/std_vector_base.json").writeText(Json.encodeToString(info))
//        }
        WrappedTemplate(it, resolverBuilder).also {
//            println("Created template $it")
        }
    }
    return classes to templates
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
