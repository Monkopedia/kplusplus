/*
 * Copyright 2022 Jason Monk
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
package com.monkopedia.krapper.generator.model

import com.monkopedia.krapper.generator.DropPhase
import com.monkopedia.krapper.generator.GenerationContext
import com.monkopedia.krapper.generator.dropLedger
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_FUNCTION
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_OPAQUE_POINTER
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_POINTER
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_POINTER_VAR
import com.monkopedia.krapper.generator.builders.KotlinFactory.Companion.C_VALUES_REF
import com.monkopedia.krapper.generator.model.type.WrappedEnumConstant
import com.monkopedia.krapper.generator.model.type.WrappedEnumType
import com.monkopedia.krapper.generator.model.type.WrappedFunctionPointer
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType

interface WrappedKotlinType {
    val isWrapper: Boolean
    val fullyQualified: List<String>
    val name: String
    val pkg: String
}

/**
 * The Kotlin type of a C/C++ enum surfaced as a generated `enum class`. It
 * delegates its qualification (name/pkg/fullyQualified) to the [base] type
 * derived from the enum's C++ spelling, but additionally carries the enum
 * [constants] so the resolver can emit the enum-class declaration and the
 * boundary conversions.
 */
class EnumKotlinType(
    private val base: WrappedKotlinType,
    val constants: List<WrappedEnumConstant>,
    val underlying: WrappedKotlinType
) : WrappedKotlinType by base

private val typeMap = mapOf(
    "size_t" to "platform.posix.size_t",
    "ssize_t" to "platform.posix.ssize_t",
    "ptrdiff_t" to "platform.posix.ptrdiff_t",
    "intptr_t" to "platform.posix.intptr_t",
    "wchar_t" to "platform.posix.wchar_t",
    "void" to "kotlin.Unit",
    "bool" to "kotlin.Boolean",
    "char" to "kotlin.Byte",
    "signed char" to "kotlin.Byte",
    "unsigned char" to "kotlin.UByte",
    "uint8_t" to "kotlin.UShort",
    "short" to "kotlin.Short",
    "signed short" to "kotlin.Short",
    "unsigned short" to "kotlin.UShort",
    "uint16_t" to "kotlin.UShort",
    "int" to "kotlin.Int",
    "signed int" to "kotlin.Int",
    "unsigned int" to "kotlin.UInt",
    "uint32_t" to "kotlin.UInt",
    "long" to "kotlin.Long",
    "signed long" to "kotlin.Long",
    "unsigned long" to "kotlin.ULong",
    "uint64_t" to "kotlin.ULong",
    "long long" to "kotlin.Long",
    "signed long long" to "kotlin.Long",
    "unsigned long long" to "kotlin.ULong",
    "float" to "kotlin.Float",
    "double" to "kotlin.Double",
    "long double" to "kotlin.Double",
    "uintptr_t" to C_OPAQUE_POINTER
)

private val pointerTypeMap = mapOf(
    "bool" to "kotlinx.cinterop.BooleanVar",
    "char" to "kotlinx.cinterop.ByteVar",
    "signed char" to "kotlinx.cinterop.ByteVar",
    "unsigned char" to "kotlinx.cinterop.UByteVar",
    "uint8_t" to "kotlinx.cinterop.UByteVar",
    "short" to "kotlinx.cinterop.ShortVar",
    "signed short" to "kotlinx.cinterop.ShortVar",
    "unsigned short" to "kotlinx.cinterop.UShortVar",
    "uint16_t" to "kotlinx.cinterop.UShortVar",
    "int" to "kotlinx.cinterop.IntVar",
    "signed int" to "kotlinx.cinterop.IntVar",
    "unsigned int" to "kotlinx.cinterop.UIntVar",
    "uint32_t" to "kotlinx.cinterop.UIntVar",
    "long" to "kotlinx.cinterop.LongVar",
    "signed long" to "kotlinx.cinterop.LongVar",
    "unsigned long" to "kotlinx.cinterop.ULongVar",
    "uint64_t" to "kotlinx.cinterop.ULongVar",
    "long long" to "kotlinx.cinterop.LongVar",
    "signed long long" to "kotlinx.cinterop.LongVar",
    "unsigned long long" to "kotlinx.cinterop.ULongVar",
    "float" to "kotlinx.cinterop.FloatVar",
    "double" to "kotlinx.cinterop.DoubleVar",
    // C posix-alias typedefs added to NATIVE — pointers to them need the matching
    // cinterop *Var (else a `wchar_t*` etc. collapses to the bare scalar).
    "wchar_t" to "kotlinx.cinterop.IntVar",
    "ptrdiff_t" to "kotlinx.cinterop.LongVar",
    "ssize_t" to "kotlinx.cinterop.LongVar",
    "intptr_t" to "kotlinx.cinterop.LongVar",
    // size_t is native (so its wrapper C signature is `size_t*`/`size_t&`), but it had no
    // *Var entry, so a `size_t*`/`size_t&` out-param collapsed through the `?:` fallback
    // below to a BARE `size_t` scalar — the Kotlin facade then passed a value where the
    // extern's `CValuesRef<...>` pointer is expected and did NOT compile (issue #103,
    // Family 1: v8 Isolate::GetCodeRange(..., size_t*), TraceBufferChunk::AddTraceEvent(
    // size_t&), ValueSerializer::Delegate::ReallocateBufferMemory(..., size_t*)). size_t
    // is unsigned long on LP64 (its by-value form is `platform.posix.size_t` = ULong), so
    // its pointer rides ULongVar — identical to `uint64_t*` and to cinterop's own
    // `CValuesRef<size_tVar>` (size_tVar aliases ULongVar), matching the `size_t*` extern.
    "size_t" to "kotlinx.cinterop.ULongVar"
)

// The Kotlin-facing spelling of a model type. An extension (not a member) because the
// model module is front-end/back-end agnostic — the Kotlin naming tables live with the
// generator (issue #44 brick 1b).
val WrappedType.kotlinType: WrappedKotlinType
    get() = typeToKotlinType(this)

fun typeToKotlinType(type: WrappedType): WrappedKotlinType = WrappedKotlinType(type)

fun WrappedKotlinType(type: WrappedType): WrappedKotlinType {
    // A C function-pointer typedef crosses the boundary as a raw cinterop function
    // pointer: `CPointer<CFunction<(<args>) -> <ret>>>?`. The arg/return types are
    // rendered through the normal per-type Kotlin mapping (so `int` -> `kotlin.Int`,
    // etc.). The inner `(<args>) -> <ret>` Kotlin function type is emitted as a single
    // literal leaf (it has no fully-qualified form to import); CPointer/CFunction import
    // normally. No lambda/trampoline bridge is built here — this is type mapping only.
    if (type is WrappedFunctionPointer) {
        val args = type.argTypes.joinToString(", ") { WrappedKotlinType(it).name }
        val ret = WrappedKotlinType(type.returnType).name
        return nullable(
            fullyQualifiedType(C_POINTER).typedWith(
                listOf(
                    fullyQualifiedType(C_FUNCTION).typedWith(
                        listOf(fullyQualifiedType("($args) -> $ret"))
                    )
                )
            )
        )
    }
    if (type is WrappedTemplateType) {
        return WrappedKotlinType(
            WrappedKotlinType(type.baseType).pkg + "." +
                (listOf(type.baseType) + type.templateArgs).joinToString("__") {
                    it.toString().split("::").last()
                        .capitalize()
                        .mangleToIdentifier()
                }
        )
    }
    if (type is WrappedTemplateRef) throw IllegalArgumentException("Can't convert $type to kotlin")
    // An enum with recoverable constants surfaces as a generated Kotlin `enum
    // class` (named after the enum's C++ spelling, in the same package a wrapper
    // class of that name would get). Without constants it falls back to exposing
    // its underlying integer (e.g. Int/UInt), the legacy behaviour.
    if (type is WrappedEnumType) {
        if (type.constants.isEmpty()) return WrappedKotlinType(type.underlying)
        return EnumKotlinType(
            WrappedKotlinType(type.cppName),
            type.constants,
            WrappedKotlinType(type.underlying)
        )
    }
    if (type.isString) return fullyQualifiedType("kotlin.String?")
    if (type.toString() == "const char*") return fullyQualifiedType("kotlin.String?")
    if (type.isConst) {
        return WrappedKotlinType(type.unconst)
    }
    if (type.isPointer || type.isReference) {
        val baseType = if (type.isReference) type.unreferenced else type.pointed
        if (baseType == WrappedType.VOID) {
            return fullyQualifiedType(C_OPAQUE_POINTER)
        }
        if (baseType.isPointer && baseType.pointed.isNative) {
            if (baseType.pointed == WrappedType.VOID) {
                return nullable(
                    fullyQualifiedType(C_VALUES_REF).typedWith(
                        listOf(fullyQualifiedType("kotlinx.cinterop.COpaquePointerVar"))
                    )
                )
            }

            val pointerType = pointerTypeMap[baseType.pointed.toString()]
                ?: return WrappedKotlinType(baseType.pointed)
            return nullable(
                fullyQualifiedType(C_VALUES_REF).typedWith(
                    listOf(
                        fullyQualifiedType(C_POINTER_VAR).typedWith(
                            listOf(fullyQualifiedType(pointerType))
                        )
                    )
                )
            )
        }
        if (baseType.isNative) {
            val pointerType = pointerTypeMap[baseType.toString()]
                ?: return WrappedKotlinType(baseType)
            return nullable(
                fullyQualifiedType(C_VALUES_REF).typedWith(
                    listOf(fullyQualifiedType(pointerType))
                )
            )
        }
        // A POINTER to a value-reduced enum (issue #103, Family 1: v8 Maybe::To(
        // PropertyAttribute*), String::GetExternalStringResourceBase(Encoding*)). The
        // enum collapses to its underlying integer at the boundary and is NOT a
        // `.ptr`-backed wrapper, so the wrapper's cType is `void*` (see
        // WrappedModifiedType.cType: a non-native pointee yields `void*`). Surfacing the
        // generated `enum class` here would make KotlinWriter.reference() emit `arg.value`
        // (an Int) where the extern expects a pointer — uncompilable. Expose it as the
        // opaque `void*` the caller fills/reads, exactly as a by-value `void*` arg above.
        // Guarded on `isPointer`: an enum REFERENCE (`&`) is deliberately a by-value enum
        // at the boundary (WrappedModifiedType.isEnum), so it must keep the enum class.
        if (type.isPointer && baseType.isEnum) {
            return fullyQualifiedType(C_OPAQUE_POINTER)
        }
        return nullable(WrappedKotlinType(baseType))
    }
    if (type.isNative || type == WrappedType.LONG_DOUBLE) {
        return fullyQualifiedType(typeMap[type.toString()] ?: type.toString())
    }
    val name = type.toString()
    if (name.contains("<")) {
        // Qualified-template spelling: split into qualifiers and parse each
        // template arg list (skip-not-crash on failure — see parseOrOpaqueLeaf).
        return parseOrOpaqueLeaf(name) {
            val templateTypes = mutableListOf<WrappedKotlinType>()
            WrappedKotlinType(
                findQualifiers(name).joinToString("::") {
                    val section = name.substring(it).trimStart(':')
                    val start = section.indexOf('<')
                    if (start < 0) {
                        section
                    } else {
                        val base = section.substring(0, start)
                        templateTypes += parseTypes(
                            section.substring(
                                start + 1,
                                section.length - 1
                            )
                        )
                        base
                    }
                } + "__" + templateTypes.joinToString("__") { it.name }
            )
        }
    }
    return WrappedKotlinType(name)
}

internal class NullableKotlinType(internal val base: WrappedKotlinType) : WrappedKotlinType {
    override val isWrapper: Boolean
        get() = base.isWrapper
    override val fullyQualified: List<String>
        get() = base.fullyQualified
    override val name: String
        get() = base.name + "?"
    override val pkg: String
        get() = base.pkg

    override fun toString(): String = "$base?"
}

fun nullable(base: WrappedKotlinType): WrappedKotlinType = NullableKotlinType(base)

fun WrappedKotlinType(nameIn: String): WrappedKotlinType {
    val name = nameIn.trim()
    if (name.contains("<")) {
        // Template spelling: parse the base and its arg list (skip-not-crash on
        // failure — see parseOrOpaqueLeaf).
        return parseOrOpaqueLeaf(name) {
            val start = name.indexOf('<')
            val base = name.substring(0, start)
            WrappedKotlinType(base).typedWith(
                parseTypes(
                    name.substring(
                        start + 1,
                        name.length - 1
                    )
                )
            )
        }
    }
    return fullyQualifiedType(name.replace("::", ".").replace("*", "_P"), isWrapper = true)
}

// Skip-not-crash wrapper shared by the two template-parsing entry points. The
// hand-rolled index math (parseTypes/findQualifiers/findTemplates/findEnd) can
// overflow or fail on a deeply-nested or malformed spelling; on any failure we
// record the drop and degrade the one element to a single opaque leaf instead
// of aborting the whole generation run.
private inline fun parseOrOpaqueLeaf(
    name: String,
    parse: () -> WrappedKotlinType
): WrappedKotlinType = runCatching(parse).getOrElse {
    dropLedger.record(
        name,
        "Failed to parse template spelling (${it.message}); degraded to opaque leaf",
        DropPhase.PARSE
    )
    println(
        "WARN skip-not-crash: failed to parse template spelling '$name' " +
            "(${it.message}); surfacing as opaque type"
    )
    opaqueLeaf(name)
}

// An opaque single-leaf Kotlin type built from an arbitrary (possibly malformed)
// type spelling, with the template/qualifier characters that would re-trigger the
// structured parser stripped to safe identifier characters. Used as the
// skip-not-crash fallback when the structured template parse fails.
private fun opaqueLeaf(spelling: String): WrappedKotlinType = fullyQualifiedType(
    spelling
        .replace("::", ".")
        .mangleToIdentifier(),
    isWrapper = true
)

// Collapse template/pointer punctuation that can't appear in a Kotlin
// identifier: '< > , space' -> '_', and pointer '*' -> '_P'. Callers handle any
// '::' themselves (map to '.' or split away) before calling.
private fun String.mangleToIdentifier(): String = this
    .replace("*", "_P")
    .replace("<", "_")
    .replace(">", "_")
    .replace(",", "_")
    .replace(" ", "_")

internal class TemplatedKotlinType(
    internal val baseType: WrappedKotlinType,
    internal val templateTypes: List<WrappedKotlinType>
) : WrappedKotlinType {
    override val isWrapper: Boolean
        get() = baseType.isWrapper
    override val fullyQualified: List<String>
        get() = templateTypes.flatMap { it.fullyQualified } + baseType.fullyQualified
    override val name: String
        get() = "${baseType.name}<${templateTypes.joinToString(", ") { it.name }}>"
    override val pkg: String
        get() = baseType.pkg

    override fun toString(): String = "$baseType<${templateTypes.joinToString(", ")}>"
}

fun WrappedKotlinType.typedWith(parseTypes: List<WrappedKotlinType>): WrappedKotlinType =
    TemplatedKotlinType(this, parseTypes)

fun parseTypes(argList: String): List<WrappedKotlinType> = findTemplates(argList).map {
    argList.substring(it).trimStart(',')
}.map(::WrappedKotlinType)

fun findQualifiers(argList: String): List<IntRange> = sequence {
    var last = -1
    var current = 1
    var lastColon = false
    while (current < argList.length) {
        val c = argList[current]
        if (c == '<') {
            current = argList.findEnd(start = current)
        } else if (c == ':' && lastColon) {
            yield(IntRange(last + 1, current - 2))
            last = current
        }
        lastColon = c == ':'
        current++
    }
    yield(IntRange(last + 1, current - 1))
}.toList()

fun findTemplates(argList: String): List<IntRange> = sequence {
    var last = 0
    var current = 1
    while (current < argList.length) {
        if (argList[current] == '<') {
            current = argList.findEnd(start = current)
        } else if (argList[current] == ',') {
            yield(IntRange(last, current - 1))
            last = current
        }
        current++
    }
    yield(IntRange(last, current - 1))
}.toList()

private fun String.findEnd(start: Int): Int {
    var startSearch = start
    var openIndex = indexOf('<', startSearch + 1)
    while (openIndex >= 0) {
        startSearch = findEnd(openIndex)
        openIndex = indexOf('<', startSearch + 1)
    }
    val end = indexOf('>', startSearch + 1)
    if (end < 0) {
        throw IllegalStateException("Cannot find end of $this[$start]")
    }
    return end
}

fun fullyQualifiedType(name: String, isWrapper: Boolean = false): WrappedKotlinType {
    val parts = name.split(".")
    // Only wrapper class names are force-capitalized to Kotlin convention
    // (e.g. `max_align_t` -> `Max_align_t`). Non-wrapper names are already correctly
    // cased fully-qualified types — capitalizing their last segment would corrupt
    // lowercase platform typealiases (`platform.posix.size_t` -> undefined `Size_t`).
    val cased = if (isWrapper) parts.dropLast(1) + parts.last().capitalize() else parts
    // Root-package handling (wrapper types only — never platform/native names):
    //   - rootPackage null: top-level (size 1) wrappers get the legacy `root` prefix,
    //     namespaced wrappers are left as-is. (Provably identical to historical.)
    //   - rootPackage set: prepend the configured segments to every wrapper package,
    //     replacing the legacy `root` for top-level and prefixing namespaced ones.
    // Fixed for the run by its GenerationContext (via config.rootPackage), installed before any
    // resolution — a type's Kotlin package is baked into its ResolvedKotlinType then.
    val rootSegments = GenerationContext.current.rootPackage?.split(".")
    val nameList = when {
        !isWrapper -> cased

        // Override set: root every wrapper under the configured segments — but
        // IDEMPOTENTLY. The template path derives a binding's package by re-wrapping
        // `baseType.pkg + mangledName` (an already-rooted string) back through here, so
        // skip the prefix when `cased` already starts with the root segments. Otherwise
        // `std::vector<int>` would double to `<root>.<root>.std.Vector__Int`. Safe because
        // first-pass inputs are raw C++ names (`std.Vector`), never dotted-package style,
        // so "starts with root segments" reliably means "already rooted".
        rootSegments != null ->
            if (cased.take(rootSegments.size) == rootSegments) {
                cased
            } else {
                rootSegments + cased
            }

        cased.size == 1 -> listOf("root") + cased

        else -> cased
    }
    val capitalizedName = nameList.joinToString(".")
    return object : WrappedKotlinType {
        override val isWrapper: Boolean
            get() = isWrapper
        override val fullyQualified: List<String>
            get() = listOf(capitalizedName)
        override val name: String
            get() = nameList.last()
        override val pkg: String
            get() = nameList.dropLast(1).joinToString(".")

        override fun toString(): String = capitalizedName
    }
}
