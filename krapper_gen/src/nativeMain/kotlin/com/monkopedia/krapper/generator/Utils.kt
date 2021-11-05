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

import clang.CXCursor
import clang.CXCursorKind
import kotlinx.cinterop.CValue
import kotlinx.serialization.Serializable
import platform.posix.fflush
import platform.posix.fprintf

object Utils {
    val STDERR = platform.posix.fdopen(2, "w")

    fun printerrln(message: String) {
        fprintf(STDERR, message + "\n")
        fflush(STDERR)
    }

    @Serializable
    data class CursorTreeInfo(
        val spelling: String?,
        val type: String?,
        val kind: CXCursorKind,
        val prettyPrint: String,
        val children: List<CursorTreeInfo>,
    ) {
        constructor(cursor: CValue<CXCursor>): this(
            cursor.spelling.toKString() ?: "UKN",
            cursor.type.spelling.toKString() ?: "UKN",
            cursor.kind,
            cursor.prettyPrinted.toKString() ?: "NOINFO",
            cursor.filterChildren { true }.map { CursorTreeInfo(it) }
        )
    }
}
