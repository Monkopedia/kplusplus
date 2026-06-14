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
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.fread

// Minimal posix file IO for the cpp front-end's emit modes (--golden-emit / --handoff-emit
// / --parity-emit). This module has no dependency on krapper_gen's File.
//
// The former goldenCompare() lived here: it structurally diffed this front-end's parse
// output against krapper_gen's libclang-C reducer (--dumpParsedModel) under a normalizer
// ledger — the front-end-rewrite regression lock. That reducer was DELETED in the self-
// hosting flip (B5, #88), so there is no second front-end to compare against; the compare
// (and its task/CLI mode) was removed in #92. The standing cpp-side gates are
// handoffGenerate / handoffInstGenerate (cpp e2e, wrapper compiled) + :featuregen:nativeTest.

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
