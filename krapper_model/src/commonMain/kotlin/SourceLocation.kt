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
package com.monkopedia.krapper

import kotlinx.serialization.Serializable

/**
 * A C++ source position — the presumed file/line/column Clang reports for a declaration or
 * a compiler diagnostic.
 *
 * Lives in the model module (rather than with [Diagnostic]) because the parse-output
 * [com.monkopedia.krapper.generator.model.WrappedElement] carries one, and the model module
 * cannot depend on the service protocol above it.
 */
@Serializable
data class SourceLocation(val file: String, val line: Int, val column: Int) {
    override fun toString(): String = "$file:$line:$column"

    companion object {
        /**
         * Parse a `path:line:col` spelling — the shape the `kppbridge::declLocation` bridge
         * emits and the prefix Clang puts on every diagnostic line. Returns null for anything
         * that isn't that shape (an empty/invalid location, a path with no position).
         *
         * Splits from the RIGHT so a path containing a colon can't be mis-split.
         */
        fun parse(spelling: String): SourceLocation? {
            val col = spelling.substringAfterLast(':', "").toIntOrNull() ?: return null
            val head = spelling.substringBeforeLast(':')
            val line = head.substringAfterLast(':', "").toIntOrNull() ?: return null
            val file = head.substringBeforeLast(':')
            return if (file.isEmpty()) null else SourceLocation(file, line, col)
        }
    }
}
