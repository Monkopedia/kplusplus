/*
 * Copyright 2026 Jason Monk
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
import com.monkopedia.krapper.generator.model.SerializedElement
import kotlin.system.exitProcess
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.Json
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.fread

// #44 brick 7 — THE GOLDEN TEST. Both front-ends parse the SAME fixture and project their
// WrappedTU through the canonical SerializedElement DTO (:krapper_model ModelSerializer):
//  * cpp.json      — this front-end (CppFrontend.kt --golden-emit),
//  * libclang.json — krapper_gen's libclang-C path (--dumpParsedModel, Parsing.kt).
// goldenCompare() diffs the two trees STRUCTURALLY under the normalizer ledger below and
// exits non-zero on any unexplained divergence — the automated regression lock for the
// front-end rewrite (Phase B exit / Phase C entry; the spirit of issue #8).
//
// ============================ NORMALIZER / MASK LEDGER =============================
// The honest record of remaining representational divergence between the two front-ends.
// Every entry cites BOTH sides' behavior; anything NOT explained here fails the compare.
//
// N1 IDENTITY MASK (usr fields). The libclang path keys identity on clang USR strings
//    ("c:@ST>1#T@Holder@T", "" for params); this front-end on canonical-Decl ids
//    ("cpp:<id>", ModelBuilder.cppUsr). The literal strings can never agree. Template-
//    param usrs are CROSS-REFERENCED by WrappedTemplateRef targets, so they are rewritten
//    to POSITIONAL tokens ("tp:0", document order) on both sides — preserving the
//    structural keying the resolver depends on — while arg usrs (referenced by nothing)
//    are masked to null.
// N2 TEMPLATE-REF KEYING IN SPELLINGS (WrappedTemplateRef.toString = "template<target>"
//    embeds the ref's key in type/returnType spellings, e.g. Holder's `T get()` return).
//    The libclang path keys a dependent-T ref EITHER on the param's USR (TypeFactories'
//    CXCursor_TemplateTypeParameter branch) OR on its bare NAME ("template<T>", the
//    CXType_Unexposed/NoDeclFound fallback — which is what every dependent use inside a
//    ClassTemplate's members actually hits, libclang not resolving the dependent decl);
//    this front-end always keys on identity (TypeBuilder's TemplateTypeParmType branch).
//    The resolver accepts BOTH keys (Parsing.typedAs maps the param's name AND usr to the
//    same substitution), so within a template's subtree, "template<usr>" and
//    "template<name>" of each of its params rewrite to the same positional token.
// N3 CURSOR-WALK TYPE RESIDUE (dropped child elements). ModelFactories.mapAll attaches
//    elements for CXCursor_TypeRef (-> WrappedTemplateRef) and CXCursor_TemplateRef
//    (-> WrappedType) cursors when their parent cursor maps to a real element — stray
//    type-reference residue riding on methods/bases/typedefs (load-bearing ONLY under a
//    WrappedTemplateParam, where defaultType reads them; the fixture has no defaulted
//    template params — Phase D item on #46). This front-end constructs types only where
//    the model references them and emits no such children. Children whose kind is not a
//    named model kind are dropped from both sides.
// N4 DEFAULT-VALUE WHITESPACE. libclang's defaultValue is a token-spelling JOIN with no
//    separators (ModelFactories.defaultValue: joinToString("")); this front-end reads the
//    raw SOURCE text (kppbridge::defaultArgText over the default-arg range), which keeps
//    the author's spacing. Whitespace is stripped from defaultValue on both sides.
// N5 USING-ALIAS COLLAPSE ORDER-DEPENDENCE (the documented brick-5/6 divergence). A
//    `using` alias produces no typedef ELEMENT on either path, but as a type USE the
//    libclang path's reducer collapse (ResolverBuilderImpl.visit) is ORDER-DEPENDENT —
//    for the fixture's `using HandlerFn = int (*)(double)` the leaf survives as the
//    alias NAME ("HandlerFn"), while this front-end deterministically collapses to the
//    canonical spelling ("int (*)(double)", TypeBuilder's typedef branch). Normalized by
//    the fixture's alias table below; durable convergence (deterministic collapse on the
//    libclang path, or alias preservation here) is a Phase D tail item (#46).
// ===================================================================================

// N5's fixture-scoped `using`-alias spellings: alias-name leaf -> canonical collapse.
private val USING_ALIAS_SPELLINGS = mapOf(
    "HandlerFn" to "int (*)(double)"
)

// The element kinds ModelSerializer names explicitly — everything both front-ends MODEL.
// Any other kind (the serializer's `else` branch spells the class name, e.g.
// "WrappedTemplateRef") is libclang cursor-walk residue, dropped under N3.
private val MODEL_KINDS = setOf(
    "tu", "class", "template", "templateParam", "typedef",
    "namespace", "base", "method", "arg", "field"
)

fun goldenCompare(cppPath: String, libclangPath: String): Nothing {
    val json = Json
    val cpp = json.decodeFromString(
        SerializedElement.serializer(),
        readFile(cppPath)
    ).normalized()
    val libclang = json.decodeFromString(
        SerializedElement.serializer(),
        readFile(libclangPath)
    ).normalized()

    val divergences = mutableListOf<String>()
    diff(cpp, libclang, "tu", divergences)

    if (divergences.isEmpty()) {
        println(
            "GOLDEN COMPARE: PASS — cpp front-end and libclang front-end parse output " +
                "match structurally under the documented normalizer ledger (N1-N5)."
        )
        exitProcess(0)
    }
    println("GOLDEN COMPARE: FAIL — ${divergences.size} unexplained divergence(s):")
    for (d in divergences) println("  $d")
    exitProcess(1)
}

// ---- Normalization (the ledger, executable) ----

private fun SerializedElement.normalized(): SerializedElement = normalize(this, emptyList(), Counter())

private class Counter {
    var next = 0
}

private fun normalize(
    element: SerializedElement,
    inherited: List<Pair<String, String>>,
    counter: Counter
): SerializedElement {
    // N1+N2: entering a template scope assigns each of its params a positional token
    // (document order — identical on both sides, both walks being source-ordered) and
    // registers the rewrites: the param's usr (either side's identity string), and the
    // exact "template<name>" spelling (the libclang name-keyed ref form). The bare name
    // is never rewritten outside the template<...> wrapper — "T" alone is too short to
    // substring-replace safely.
    val rewrites = if (element.kind == "template") {
        inherited + element.children.filter { it.kind == "templateParam" }.flatMap { param ->
            val token = "tp:${counter.next++}"
            listOfNotNull(
                param.usr?.takeIf { it.isNotEmpty() }?.let { it to token },
                param.name?.let { "template<$it>" to "template<$token>" }
            )
        }
    } else {
        inherited
    }
    fun String.rewriteKeys(): String {
        // Longest-first so one key being a prefix of another (e.g. "cpp:7"/"cpp:75")
        // can't corrupt the rewrite.
        var result = this
        for ((key, token) in rewrites.sortedByDescending { it.first.length }) {
            result = result.replace(key, token)
        }
        return result
    }
    // N5: a whole-leaf `using`-alias spelling normalizes to its canonical collapse.
    fun String.collapseAliases(): String = USING_ALIAS_SPELLINGS[this] ?: this
    return element.copy(
        type = element.type?.collapseAliases()?.rewriteKeys(),
        returnType = element.returnType?.collapseAliases()?.rewriteKeys(),
        // N4: whitespace-insensitive default values.
        defaultValue = element.defaultValue?.filterNot { it.isWhitespace() },
        // N1: positional template-param identity; all other identity masked entirely.
        usr = when (element.kind) {
            "templateParam" -> element.usr?.rewriteKeys()
            else -> null
        },
        // N3: drop cursor-walk type residue, recurse into the real model children.
        children = element.children
            .filter { it.kind in MODEL_KINDS }
            .map { normalize(it, rewrites, counter) }
    )
}

// ---- Structural diff ----

private fun diff(
    cpp: SerializedElement,
    libclang: SerializedElement,
    path: String,
    out: MutableList<String>
) {
    fun field(name: String, c: Any?, l: Any?) {
        if (c != l) out += "$path.$name: cpp=$c libclang=$l"
    }
    field("kind", cpp.kind, libclang.kind)
    field("name", cpp.name, libclang.name)
    field("type", cpp.type, libclang.type)
    field("returnType", cpp.returnType, libclang.returnType)
    field("methodType", cpp.methodType, libclang.methodType)
    field("isConst", cpp.isConst, libclang.isConst)
    field("isVirtual", cpp.isVirtual, libclang.isVirtual)
    field("isAbstract", cpp.isAbstract, libclang.isAbstract)
    field("isPublic", cpp.isPublic, libclang.isPublic)
    field("isVirtualBase", cpp.isVirtualBase, libclang.isVirtualBase)
    field("isCopyConstructor", cpp.isCopyConstructor, libclang.isCopyConstructor)
    field("isDefaultConstructor", cpp.isDefaultConstructor, libclang.isDefaultConstructor)
    field("hasDefault", cpp.hasDefault, libclang.hasDefault)
    field("defaultValue", cpp.defaultValue, libclang.defaultValue)
    field("metadata", cpp.metadata, libclang.metadata)
    field("usr", cpp.usr, libclang.usr)
    if (cpp.children.size != libclang.children.size) {
        out += "$path.children: cpp has ${cpp.children.size} ${cpp.children.label()} " +
            "vs libclang ${libclang.children.size} ${libclang.children.label()}"
    }
    // Positional compare over the common prefix: both walks emit SOURCE order (the
    // libclang cursor walk; decls()/bases() on this side — brick 3 switched to decls()
    // for exactly this), so index i is the same declaration on both sides.
    for (i in 0 until minOf(cpp.children.size, libclang.children.size)) {
        val c = cpp.children[i]
        diff(c, libclang.children[i], "$path/${c.kind}[${c.name ?: i}]", out)
    }
}

private fun List<SerializedElement>.label(): String =
    joinToString(",", "[", "]") { "${it.kind}:${it.name ?: it.type ?: "?"}" }

// ---- Minimal posix file IO (this module has no dependency on krapper_gen's File) ----

internal fun writeFile(path: String, content: String) {
    val file = fopen(path, "wb") ?: fail("cannot open for write: $path")
    try {
        fputs(content, file)
    } finally {
        fclose(file)
    }
}

internal fun readFile(path: String): String = memScoped {
    val file = fopen(path, "rb") ?: fail("cannot open for read: $path")
    try {
        val builder = StringBuilder()
        val bufferSize = 65536
        val buffer = allocArray<ByteVar>(bufferSize + 1)
        while (true) {
            val read = fread(buffer, 1u, bufferSize.toULong(), file)
            if (read == 0uL) break
            buffer[read.toInt()] = 0.toByte()
            builder.append(buffer.toKString())
        }
        builder.toString()
    } finally {
        fclose(file)
    }
}
