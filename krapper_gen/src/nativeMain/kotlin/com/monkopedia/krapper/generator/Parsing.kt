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
import com.monkopedia.krapper.AndFilter
import com.monkopedia.krapper.DefaultFilter
import com.monkopedia.krapper.FilterDefinition
import com.monkopedia.krapper.FilterableTypes
import com.monkopedia.krapper.FilterableTypes.CLASS
import com.monkopedia.krapper.FilterableTypes.FIELD
import com.monkopedia.krapper.FilterableTypes.METHOD
import com.monkopedia.krapper.FilterableTypes.TYPE
import com.monkopedia.krapper.HierarchyFilter
import com.monkopedia.krapper.HierarchyTarget.ALL_CHILDREN
import com.monkopedia.krapper.HierarchyTarget.ANY_CHILD
import com.monkopedia.krapper.HierarchyTarget.BASE
import com.monkopedia.krapper.HierarchyTarget.PARENT
import com.monkopedia.krapper.NotFilter
import com.monkopedia.krapper.OrFilter
import com.monkopedia.krapper.StringFilter
import com.monkopedia.krapper.StringMatcher
import com.monkopedia.krapper.StringMatcherType.CONTAINS
import com.monkopedia.krapper.StringMatcherType.ENDS_WITH
import com.monkopedia.krapper.StringMatcherType.EQUALS
import com.monkopedia.krapper.StringMatcherType.REGEX
import com.monkopedia.krapper.StringMatcherType.STARTS_WITH
import com.monkopedia.krapper.StringSelector
import com.monkopedia.krapper.StringSelector.CLASS_NAME
import com.monkopedia.krapper.StringSelector.CLASS_QUALIFIED
import com.monkopedia.krapper.StringSelector.METHOD_NAME
import com.monkopedia.krapper.StringSelector.METHOD_RETURN_TYPE
import com.monkopedia.krapper.StringSelector.METHOD_TYPE
import com.monkopedia.krapper.StringSelector.NAMESPACE
import com.monkopedia.krapper.StringSelector.STRINGIFY
import com.monkopedia.krapper.TypeFilter
import com.monkopedia.krapper.filter
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.WrappedTemplateParam
import com.monkopedia.krapper.generator.model.baseParent
import com.monkopedia.krapper.generator.model.cloneRecursive
import com.monkopedia.krapper.generator.model.filterRecursive
import com.monkopedia.krapper.generator.model.forEachRecursive
import com.monkopedia.krapper.generator.model.parentClass
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolved_model.MethodType.STATIC
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.ResolvedField
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import com.monkopedia.krapper.generator.resolved_model.ResolvedNamespace
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedType
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValue
import kotlinx.cinterop.DeferScope
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
import kotlin.math.min

typealias ElementFilter = WrappedElement.() -> Boolean

fun FilterDefinition.wrapperFilter(): (WrappedElement) -> Boolean {
    return when (this) {
        is AndFilter -> {
            val each = this.elements.map { it.wrapperFilter() }
            return { element ->
                each.all { it(element) }
            }
        }
        is OrFilter -> {
            val each = this.elements.map { it.wrapperFilter() }
            return { element ->
                each.any { it(element) }
            }
        }
        is NotFilter -> {
            val base = this.base.wrapperFilter()
            return { element ->
                !base(element)
            }
        }
        DefaultFilter -> {
            return defaultFilter().wrapperFilter()
        }
        is HierarchyFilter -> {
            val base = this.filter.wrapperFilter()
            return when (this.target) {
                PARENT -> { element ->
                    element.parent?.let { base(it) } ?: false
                }
                BASE -> { element ->
                    base(element.baseParent)
                }
                ANY_CHILD -> { element ->
                    element.children.any(base)
                }
                ALL_CHILDREN -> { element ->
                    element.children.all(base)
                }
            }
        }
        is StringFilter -> {
            return { element ->
                val str = this.selector.select(element)
                this.matcher.matches(str)
            }
        }
        is TypeFilter -> {
            return { element ->
                types.any {
                    when (it) {
                        CLASS -> element is WrappedClass
                        METHOD -> element is WrappedMethod
                        FIELD -> element is WrappedField
                        TYPE -> element is WrappedType
                        FilterableTypes.NAMESPACE -> element is WrappedNamespace
                    }
                }
            }
        }
    }
}

fun FilterDefinition.resolveFilter(): (ResolvedElement) -> Boolean {
    return when (this) {
        is AndFilter -> {
            val each = this.elements.map { it.resolveFilter() }
            return { element ->
                each.all { it(element) }
            }
        }
        is OrFilter -> {
            val each = this.elements.map { it.resolveFilter() }
            return { element ->
                each.any { it(element) }
            }
        }
        is NotFilter -> {
            val base = this.base.resolveFilter()
            return { element ->
                !base(element)
            }
        }
        DefaultFilter -> {
            return defaultFilter().resolveFilter()
        }
        is HierarchyFilter -> {
            val base = this.filter.resolveFilter()
            return when (this.target) {
                PARENT -> { element ->
                    element.parent?.let { base(it) } ?: false
                }
                BASE -> { element ->
                    base(element.baseParent)
                }
                ANY_CHILD -> { element ->
                    element.children.any(base)
                }
                ALL_CHILDREN -> { element ->
                    element.children.all(base)
                }
            }
        }
        is StringFilter -> {
            return { element ->
                val str = this.selector.select(element)
                this.matcher.matches(str)
            }
        }
        is TypeFilter -> {
            return { element ->
                types.any {
                    when (it) {
                        CLASS -> element is ResolvedClass
                        METHOD -> element is ResolvedMethod
                        FIELD -> element is ResolvedField
                        TYPE -> element is ResolvedType
                        FilterableTypes.NAMESPACE -> element is ResolvedNamespace
                    }
                }
            }
        }
    }
}

private fun StringMatcher.matches(input: String?): Boolean {
    val target = input ?: return false
    return when (this.type) {
        STARTS_WITH -> {
            target.startsWith(this.str)
        }
        CONTAINS -> {
            target.contains(this.str)
        }
        EQUALS -> {
            target == this.str
        }
        ENDS_WITH -> {
            target.endsWith(this.str)
        }
        REGEX -> {
            Regex(this.str).matches(target)
        }
    }
}

private fun StringSelector.select(element: ResolvedElement): String? {
    return when (this) {
        STRINGIFY -> element.toString()
        CLASS_NAME -> (element as? ResolvedClass)?.name
        CLASS_QUALIFIED -> (element as? ResolvedClass)?.type?.type
        METHOD_NAME -> (element as? ResolvedMethod)?.name
        METHOD_TYPE -> (element as? ResolvedMethod)?.methodType?.toString()
        METHOD_RETURN_TYPE -> (element as? ResolvedMethod)?.returnType?.type
        NAMESPACE -> (element as? ResolvedNamespace)?.namespace
    }
}

private fun StringSelector.select(element: WrappedElement): String? {
    return when (this) {
        STRINGIFY -> element.toString()
        CLASS_NAME -> (element as? WrappedClass)?.name
        CLASS_QUALIFIED -> (element as? WrappedClass)?.type?.toString()
        METHOD_NAME -> (element as? WrappedMethod)?.name
        METHOD_TYPE -> (element as? WrappedMethod)?.methodType?.toString()
        METHOD_RETURN_TYPE -> (element as? WrappedMethod)?.returnType?.toString()
        NAMESPACE -> (element as? WrappedNamespace)?.namespace
    }
}

fun defaultFilter(): FilterDefinition {
    return filter {
        (
            (thiz isType ResolvedClass) and
                (!(qualified startsWith "std::")) and
                (!(qualified.startsWith("__")))
            ) or (
            (thiz isType ResolvedMethod) and
                (methodType eq STATIC.name) and
                !(parent isType ResolvedClass) and
                base(!(stringified startsWith "std")) and
                parent(!((thiz isType ResolvedNamespace) and (namespace startsWith "_")))
            )
    }
}

fun WrappedElement.defaultFilter(): Boolean {
    if (this is WrappedClass) {
        return !type.toString().startsWith("std::") && !type.toString().startsWith("__")
    }
    if (this is WrappedMethod && this.methodType == STATIC) {
        return this.parentClass == null && !this.baseParent.toString()
            .startsWith("std") && !this.name.startsWith("_") &&
            (this.parent as? WrappedNamespace)?.namespace?.startsWith("_") != true
    }
    return false
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

fun generateIncludes(compiler: String) = memScoped {
    val emptyFile = File("/tmp/clang_includes.c")
    emptyFile.writeText("")
    val process = Process {
        system("$compiler -E -x c++ -v ${emptyFile.path}")
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
    return@memScoped (
        lines.subList(start + 1, end).toList().map { it.trim() } + "."
        ).toTypedArray()
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

class ParsedResolver(val tu: WrappedTU) : Resolver {
    private val classMap = mutableMapOf<String, Pair<ResolvedClass, WrappedClass>?>()
    private val templateMap = mutableMapOf<String, WrappedTemplate>()

    override fun resolveTemplate(type: WrappedType, context: ResolveContext): WrappedTemplate {
        return templateMap.getOrPut(type.toString()) {
            val templateCandidates = tu.filterRecursive {
                ((it as? WrappedTemplate)?.qualified == type.toString())
            }
            templateCandidates.singleOrNull() as? WrappedTemplate
                ?: error("Can't resolve template $type (${type::class.simpleName})")
        }
    }

    override suspend fun resolve(
        type: WrappedType,
        context: ResolveContext
    ): Pair<ResolvedClass, WrappedClass>? {
        return classMap.getOrPut(type.toString()) {
            val existingClass = tu.filterRecursive {
                (it as? WrappedClass)?.type?.toString() == type.toString() &&
                    it.isNotEmpty()
            }.singleOrNull() as? WrappedClass
            existingClass?.let { cls ->
                return@getOrPut cls.resolve(context)?.let { it to cls }
            }
            when (type) {
                is WrappedTemplateType -> {
                    val template = resolveTemplate(type.baseType, context)
                    template.typedAs(type, context)
                }
                is WrappedTemplateRef -> {
                    throw IllegalArgumentException("Can't resolve $type since it is templated")
                }
                else -> {
                    error("Can't resolve $type (${type::class.simpleName})")
                }
            }
        }
    }

    override suspend fun findClasses(filter: ElementFilter): List<WrappedElement> {
        Log.i("Finding classes")
        return mutableListOf<WrappedElement>().also { ret ->
            tu.forEachRecursive {
                if (it.filter() == true) {
                    ret.add(it)
                }
            }
        }.also {
            Log.i("Found ${it.size} classes")
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
                CXCursorKind.CXCursor_ClassDecl,
                CXCursorKind.CXCursor_StructDecl -> {
                    seenNames[strType] = type
                    if (type.numTemplateArguments <= 0) {
                        classes.add(WrappedClass(declaration, this))
                    } else {
                        desiredTemplates.add(type)
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

suspend fun DeferScope.parseHeader(
    index: CXIndex,
    file: List<String>,
    includePaths: Array<String>,
    args: Array<String> = arrayOf("-xc++", "--std=c++14") + includePaths.map { "-I$it" }
        .toTypedArray(),
    debug: Boolean = false
): Resolver {
    val builder = ResolverBuilderImpl()
    val tu = file.map { parseHeader(index, it, builder, includePaths, args, debug) }
        .reduceRight { tu1, tu2 ->
            tu1.also {
                it.addAllChildren(
                    tu2.children.map {
                        it.also { it.parent = tu1 }
                    }
                )
            }
        }
    Log.i("Reduced ${tu.children.size}")
    return ParsedResolver(tu)
}

private fun DeferScope.parseHeader(
    index: CXIndex,
    file: String,
    resolverBuilder: ResolverBuilder,
    includePaths: Array<String>,
    args: Array<String> = arrayOf("-xc++", "--std=c++14") + includePaths.map { "-I$it" }
        .toTypedArray(),
    debug: Boolean = false
): WrappedTU {
    val tu = index.parseTranslationUnit(file, args, null) ?: error("Failed to parse $file")
    tu.printDiagnostics()?.let {
        throw RuntimeException("Parse failure: $it")
    }
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
    val element = WrappedElement.mapAll(tu.cursor, resolverBuilder)
    return element as? WrappedTU ?: error("$element is not a WrappedTU, ${tu.cursor.kind}")
}

fun CXTranslationUnit.printDiagnostics(): String? {
    val nbDiag = clang_getNumDiagnostics(this)
    var foundError = false
    val errorString = buildString {
        append("There are $nbDiag diagnostics:")
        append('\n')

        for (currentDiag in 0 until nbDiag.toInt()) {
            val diagnotic = clang_getDiagnostic(this@printDiagnostics, currentDiag.toUInt())
            val errorString =
                clang_formatDiagnostic(diagnotic, clang_defaultDiagnosticDisplayOptions())
            val str = clang_getCString(errorString)?.toKString()
            clang_disposeString(errorString)
            if (str?.contains("error:") == true) {
                foundError = true
            }
            append("$str")
            append('\n')
        }
    }
    return if (foundError) {
        errorString
    } else {
        null
    }
}

suspend fun WrappedTemplate.typedAs(
    templateSpec: WrappedTemplateType,
    baseContext: ResolveContext
): Pair<ResolvedClass, WrappedClass>? {
    val fullyQualified = templateSpec.baseType.toString()
    val templates =
        filterRecursive { it is WrappedTemplateParam }.filterIsInstance<WrappedTemplateParam>()
    val mapping = mutableMapOf<String, WrappedType?>()
    for (i in 0 until min(templates.size, templateSpec.templateArgs.size)) {
        val mappedType = baseContext.map(templateSpec.templateArgs[i])
            ?: throw IllegalArgumentException(
                "Can't resolve ${templateSpec.templateArgs[i]} in $templateSpec"
            )
        mapping[templates[i].name] = mappedType
        mapping[templates[i].usr] = mappedType
        for (extra in templates[i].otherParams) {
            mapping[extra.name] = mappedType
            mapping[extra.usr] = mappedType
        }
    }
    val mapper: TypeMapping = { type, context ->
        if (type.toString() == fullyQualified) ReplaceWith(templateSpec)
        else type.operateOn { type ->
            if (type is WrappedTemplateRef) {
                val mapping = mapping[type.target]
                if (mapping != null) {
                    val result = baseContext.typeMapping(mapping, context)
                    return@operateOn if (result == ElementUnchanged) {
                        ReplaceWith(mapping)
                    } else result
                }
            }
            if (type.toString() == fullyQualified) {
                val result = baseContext.typeMapping(templateSpec, context)
                if (result == ElementUnchanged) {
                    ReplaceWith(templateSpec)
                } else result
            } else {
                baseContext.typeMapping(type, context)
            }
        }
    }
    val localContext = baseContext.copy(typeMapping = mapper, mappingCache = mutableMapOf())
    for (i in min(templates.size, templateSpec.templateArgs.size) until templates.size) {
        val defaultType = templates[i].defaultType ?: continue
        val mappedType = mapper(defaultType, localContext)
        if (mappedType is RemoveElement) continue
        val type = if (mappedType is ReplaceWith) mappedType.replacement else defaultType
        mapping[templates[i].name] = type
        mapping[templates[i].usr] = type
    }
    val outputClass = WrappedClass(name, false, templateSpec)
    outputClass.metadata = metadata.copy()
    outputClass.parent = parent
    outputClass.addAllChildren(children.map { it.cloneRecursive() })
    removeDuplicateMethods(outputClass)
    return outputClass.resolve(localContext)?.let { it to outputClass }
}

fun removeDuplicateMethods(element: WrappedElement) {
    if (element is WrappedClass) {
        val signaturesSeen = mutableSetOf<String>()
        for (child in element.children.filterIsInstance<WrappedMethod>()) {
            val signature = child.generateSignatureString()
            if (!signaturesSeen.add(signature)) {
                element.removeChild(child)
            }
        }
    }
    for (child in element.children) {
        removeDuplicateMethods(child)
    }
}

private fun WrappedMethod.generateSignatureString(): String {
    return buildString {
        append(methodType.ordinal)
        append('#')
        append(name)
        append(',')
        for (argument in args) {
            append(argument.type.maybeUnconst.maybeUnreferenced.toString())
            append(',')
        }
    }
}

private val WrappedType.maybeUnconst: WrappedType
    get() = if (isConst) unconst else this
private val WrappedType.maybeUnreferenced: WrappedType
    get() = if (isReference) unreferenced else this
