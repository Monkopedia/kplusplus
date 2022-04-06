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
package com.monkopedia.krapper.generator.builders

class CodeStringBuilder {
    private var indent = 0
    private val strBuilder = StringBuilder()

    fun startBlock() {
        indent++
    }

    fun endBlock() {
        indent--
    }

    inline fun block(b: CodeStringBuilder.() -> Unit) {
        startBlock()
        b()
        endBlock()
    }

    fun append(c: Char) {
        if (c != '\n') {
            checkNewLine()
        }
        strBuilder.append(c)
    }

    fun append(str: String) {
        str.split('\n').forEachIndexed { index, s ->
            if (index != 0) {
                append('\n')
            }
            appendInternal(s)
        }
    }

    fun removeLast() {
        strBuilder.deleteAt(strBuilder.length - 1)
    }

    private fun appendInternal(s: String) {
        if (s.isEmpty()) return
        checkNewLine()
        strBuilder.append(s)
    }

    private fun checkNewLine() {
        if (strBuilder.endsWith('\n')) {
            startLine()
        }
    }

    private fun startLine() {
        for (i in 0 until indent) {
            strBuilder.append("    ")
        }
    }

    fun build(): String {
        return strBuilder.toString()
    }
}

inline fun buildCode(build: CodeStringBuilder.() -> Unit): String {
    return CodeStringBuilder().apply(build).build()
}
