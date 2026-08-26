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
package com.monkopedia.kplusplus.compiler.fir

import java.io.File
import java.io.IOException

/**
 * Reads krapper's `binding-index.json` — the file it writes alongside the generated tree
 * naming, for every C++ spec it resolved, the Kotlin class it emitted (#186 brick B1, shipped
 * in v0.3.6; see `krapper/src/commonMain/kotlin/BindingIndex.kt` for the producer side).
 *
 * **Why this is hand-written instead of `kotlinx.serialization`.** This module is the *kotlinc*
 * plugin. It declares exactly one dependency — `compileOnly kotlin-compiler-embeddable` — and
 * everything else it touches at runtime has to come from the JDK or the Kotlin compiler's own
 * classloader. Putting a serialization (or ktor/coroutines) runtime on that classpath
 * reproduces #194's failure *inside* the Kotlin/Native compiler's plugin classloader, where
 * shading the Gradle plugin does not help. `docs/design/live-service.md` §4.4 calls that "the
 * one hard rule in this section"; the scanner below is the cost of obeying it.
 *
 * **It reads two fields and skips the rest.** The plugin needs `bindings[].spec` and
 * `bindings[].classId`; it never looks at `drops` (which is 95% of the file — featuregen's
 * index is 2MB, krapper's 4.8MB, and both carry ~4k drop diagnostics). [skipValue] walks past
 * those structurally without materialising them, so the cost is one pass over the bytes and
 * one small map, not a DOM.
 */
internal object BindingIndexReader {

    /**
     * Schema versions this reader understands.
     *
     * The two fields it consumes — `bindings[].spec` and `bindings[].classId` — have existed
     * since schema 1 and did not change in 2 (which only made `templates`/`templateArgs`
     * nullable, #213). A version ABOVE [MAX_SCHEMA] is refused rather than read optimistically:
     * `BindingIndex.SCHEMA_VERSION` is bumped precisely when the shape changes incompatibly, so
     * guessing that the change missed these two fields is exactly the kind of assumption #206
     * exists to stop. Since the tool ships bundled inside the plugin jar there is no
     * plugin/tool skew, so this can only fire on an index from a foreign build.
     */
    const val MIN_SCHEMA: Int = 1

    /** @see MIN_SCHEMA */
    const val MAX_SCHEMA: Int = 2

    /**
     * Load the index at [path], or explain why it cannot be used.
     *
     * Never throws: every failure — no path configured, no file, unparseable bytes, a schema
     * this reader does not know — comes back as [BindingIndexLoad.Unavailable] carrying text
     * meant for a compiler diagnostic, because the caller's only alternative to reporting is
     * to guess a class name, which is the defect this whole brick removes.
     */
    fun load(path: String?): BindingIndexLoad {
        if (path.isNullOrBlank()) {
            return BindingIndexLoad.Unavailable(
                "no `bindingIndexPath` plugin option was passed to the kplusplus compiler " +
                    "plugin, so it has no krapper-emitted binding index to resolve against"
            )
        }
        val file = File(path)
        if (!file.isFile) {
            return BindingIndexLoad.Unavailable(
                "krapper's binding index is not at $path (run the module's `kplusplusSync` " +
                    "task, which writes it alongside the generated bindings)"
            )
        }
        return try {
            parse(file.readText(), path)
        } catch (e: IOException) {
            BindingIndexLoad.Unavailable("$path could not be read: ${e.message}")
        }
    }

    /**
     * Parse [text] as an index document. Like [load], never throws: malformed bytes come back
     * as [BindingIndexLoad.Unavailable] carrying the scanner's complaint.
     */
    fun parse(text: String, path: String): BindingIndexLoad = try {
        parseOrThrow(text, path)
    } catch (e: BindingIndexFormatException) {
        BindingIndexLoad.Unavailable("$path is not a readable binding index: ${e.message}")
    }

    private fun parseOrThrow(text: String, path: String): BindingIndexLoad {
        val scanner = JsonScanner(text)
        var schemaVersion: Int? = null
        var rootPackage: String? = null
        var bindings: Map<String, String>? = null
        scanner.readObject { key ->
            when (key) {
                "schemaVersion" -> schemaVersion = scanner.readNumberAsInt()
                "rootPackage" -> rootPackage = scanner.readStringOrNull()
                "bindings" -> bindings = scanner.readBindings()
                else -> scanner.skipValue()
            }
        }
        val version = schemaVersion
            ?: return BindingIndexLoad.Unavailable(
                "$path has no `schemaVersion`, so it is not a krapper binding index"
            )
        if (version < MIN_SCHEMA || version > MAX_SCHEMA) {
            return BindingIndexLoad.Unavailable(
                "$path declares schemaVersion $version; this kplusplus compiler plugin reads " +
                    "$MIN_SCHEMA..$MAX_SCHEMA. Upgrade the plugin (or regenerate the index " +
                    "with the matching krapper)."
            )
        }
        // An index with `bindings: []` is a real and fine state (a module that generated
        // nothing). A `bindings` key that is ABSENT is not: it means the document is not
        // shaped like an index at all, and treating it as "zero bindings" would turn every
        // lookup into a spurious sync request.
        val bySpec = bindings
            ?: return BindingIndexLoad.Unavailable(
                "$path has no `bindings` array, so it is not a krapper binding index"
            )
        return BindingIndexLoad.Loaded(
            LoadedBindingIndex(
                path = path,
                schemaVersion = version,
                rootPackage = rootPackage,
                classIdBySpec = bySpec
            )
        )
    }
}

/** Outcome of [BindingIndexReader.load]. */
internal sealed interface BindingIndexLoad {
    data class Loaded(val index: LoadedBindingIndex) : BindingIndexLoad

    /** [reason] is user-facing diagnostic text: it names the path and what is wrong with it. */
    data class Unavailable(val reason: String) : BindingIndexLoad
}

/** The subset of krapper's `binding-index.json` this plugin consumes. */
internal class LoadedBindingIndex(
    val path: String,
    val schemaVersion: Int,
    /** The `rootPackage` the bindings were generated under, or null when unset. */
    val rootPackage: String?,
    private val classIdBySpec: Map<String, String>
) {
    val bindingCount: Int get() = classIdBySpec.size

    /**
     * The fully-qualified Kotlin name krapper emitted for [spec], or null if it emitted none.
     *
     * Null is an ANSWER, not a failure: the index is krapper's own account of what it wrote,
     * so "not listed" means "not generated" — which is a sync request, not a naming problem.
     */
    fun classIdFor(spec: String): String? = classIdBySpec[spec]
}

/** Raised by [JsonScanner] on malformed input; never escapes [BindingIndexReader.load]. */
internal class BindingIndexFormatException(message: String) : Exception(message)

/**
 * A single-pass JSON scanner over the subset krapper's index actually uses: objects, arrays,
 * strings, numbers, `true`/`false`/`null`. No DOM, no reflection, no third-party classes.
 */
private class JsonScanner(private val text: String) {
    private var pos = 0

    fun readObject(onMember: (String) -> Unit) {
        skipWhitespace()
        expect('{')
        skipWhitespace()
        if (consumeIf('}')) return
        while (true) {
            skipWhitespace()
            val key = readString()
            skipWhitespace()
            expect(':')
            onMember(key)
            skipWhitespace()
            if (consumeIf(',')) continue
            expect('}')
            return
        }
    }

    /** `[ {…}, {…} ]` where each element contributes `spec -> classId`. */
    fun readBindings(): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        skipWhitespace()
        if (peekIsNull()) {
            skipValue()
            return result
        }
        expect('[')
        skipWhitespace()
        if (consumeIf(']')) return result
        while (true) {
            var spec: String? = null
            var classId: String? = null
            readObject { key ->
                when (key) {
                    "spec" -> spec = readStringOrNull()
                    "classId" -> classId = readStringOrNull()
                    else -> skipValue()
                }
            }
            val s = spec
            val c = classId
            if (s == null || c == null) {
                fail("a `bindings` entry is missing `spec` and/or `classId`")
            }
            result[s] = c
            skipWhitespace()
            if (consumeIf(',')) continue
            expect(']')
            return result
        }
    }

    fun readStringOrNull(): String? {
        skipWhitespace()
        if (peekIsNull()) {
            skipValue()
            return null
        }
        return readString()
    }

    fun readNumberAsInt(): Int? {
        skipWhitespace()
        if (peekIsNull()) {
            skipValue()
            return null
        }
        val start = pos
        while (pos < text.length && (text[pos].isDigit() || text[pos] in "+-.eE")) pos++
        if (start == pos) fail("expected a number at offset $start")
        return text.substring(start, pos).toIntOrNull()
    }

    /** Walk past one value of any shape without building it. */
    fun skipValue() {
        skipWhitespace()
        when (val c = peek()) {
            '{', '[' -> skipContainer()
            '"' -> readString()
            else -> {
                val start = pos
                while (pos < text.length && text[pos] !in ",}] \t\r\n") pos++
                if (start == pos) fail("unexpected character '$c' at offset $pos")
            }
        }
    }

    private fun skipContainer() {
        var depth = 0
        do {
            when (peek()) {
                '{', '[' -> {
                    depth++
                    pos++
                }
                '}', ']' -> {
                    depth--
                    pos++
                }
                '"' -> readString()
                else -> pos++
            }
        } while (depth > 0 && pos < text.length)
        if (depth > 0) fail("unterminated object/array")
    }

    private fun readString(): String {
        expect('"')
        val plainStart = pos
        // Fast path: the overwhelming majority of strings in an index carry no escape, so scan
        // for the closing quote first and only fall back to the char-by-char decoder if an
        // escape turns up.
        while (pos < text.length) {
            when (text[pos]) {
                '"' -> return text.substring(plainStart, pos).also { pos++ }
                '\\' -> return readEscapedString(plainStart)
                else -> pos++
            }
        }
        fail("unterminated string starting at offset $plainStart")
    }

    private fun readEscapedString(plainStart: Int): String {
        val out = StringBuilder().append(text, plainStart, pos)
        while (pos < text.length) {
            when (val c = text[pos++]) {
                '"' -> return out.toString()
                '\\' -> out.append(readEscape())
                else -> out.append(c)
            }
        }
        fail("unterminated string starting at offset $plainStart")
    }

    private fun readEscape(): Char {
        if (pos >= text.length) fail("truncated escape")
        return when (val c = text[pos++]) {
            '"', '\\', '/' -> c
            'b' -> '\b'
            'f' -> ''
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'u' -> {
                if (pos + 4 > text.length) fail("truncated \\u escape at offset $pos")
                val hex = text.substring(pos, pos + 4)
                pos += 4
                hex.toIntOrNull(16)?.toChar() ?: fail("bad \\u escape '$hex'")
            }
            else -> fail("unknown escape '\\$c' at offset ${pos - 1}")
        }
    }

    private fun peekIsNull(): Boolean = text.startsWith("null", pos)

    private fun peek(): Char {
        if (pos >= text.length) fail("unexpected end of input")
        return text[pos]
    }

    private fun consumeIf(c: Char): Boolean {
        skipWhitespace()
        if (pos < text.length && text[pos] == c) {
            pos++
            return true
        }
        return false
    }

    private fun expect(c: Char) {
        skipWhitespace()
        if (pos >= text.length || text[pos] != c) {
            fail("expected '$c' at offset $pos but found '${text.getOrNull(pos) ?: "<eof>"}'")
        }
        pos++
    }

    private fun skipWhitespace() {
        while (pos < text.length && text[pos].isWhitespace()) pos++
    }

    private fun fail(message: String): Nothing = throw BindingIndexFormatException(message)
}
