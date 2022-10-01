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
package com.monkopedia.krapper.generator.codegen

import kotlin.native.internal.NativePtr
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import platform.posix.EOF
import platform.posix.F_OK
import platform.posix.F_SETFL
import platform.posix.O_NONBLOCK
import platform.posix.S_IFDIR
import platform.posix.S_IFMT
import platform.posix.access
import platform.posix.closedir
import platform.posix.fclose
import platform.posix.fcntl
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.fread
import platform.posix.mkdir
import platform.posix.opendir
import platform.posix.readdir
import platform.posix.remove
import platform.posix.stat

data class File(val pathSegments: List<String>) {
    constructor(path: String) : this(
        (if (path.startsWith("/")) path else "${getcwd()}/$path").substring(1).split("/")
    )

    constructor(parent: File, name: String) : this(parent.pathSegments + name)

    val path: String
        get() = "/${pathSegments.joinToString("/")}"
    val name: String
        get() = pathSegments.lastOrNull() ?: "/"
    val parent: File
        get() = File(pathSegments.subList(0, pathSegments.size - 1))

    fun rmR() {
        if (isDir()) {
            listFiles().forEach(File::rmR)
            rmdir()
        } else {
            delete()
        }
    }

    fun isDir(): Boolean = memScoped {
        val stat = alloc<stat>()
        if (stat(path, stat.ptr) != 0) {
            return false
        }
        return (stat.st_mode and S_IFMT.toUInt()) == S_IFDIR.toUInt()
    }

    fun listFiles(): List<File> = memScoped {
        val d = opendir(path) ?: return emptyList()
        defer { closedir(d) }

        return sequence {
            var dir = readdir(d)
            while (dir != null && dir.rawValue != NativePtr.NULL) {
                val fileName = dir[0].d_name.toKString()
                if (fileName != "." && fileName != "..") {
                    yield(fileName)
                }
                dir = readdir(d)
            }
        }.map { File(this@File, it) }.toList()
    }

    fun rmdir() {
        platform.posix.rmdir(path)
    }

    fun delete() {
        remove(path)
    }

    fun writeText(text: String) {
        memScoped {
            val file = fopen(path, "w") ?: error("Can't open $path")
            defer { fclose(file) }
            if (fputs(text, file) == EOF) throw Error("File write error")
        }
    }

    fun readText(): String =
        memScoped {
            val file = fopen(path, "r") ?: error("Can't open $path")
            defer { fclose(file) }
            fcntl(file[0]._fileno, F_SETFL, O_NONBLOCK)
            return buildString {
                val buffer = allocArray<ByteVar>(256)
                var amount = fread(buffer, 1, 255, file)
                while (amount > 0UL) {
                    buffer[amount.toInt()] = 0
                    append(buffer.toKString())
                    amount = fread(buffer, 1, 255, file)
                }
            }
        }

    fun relativeTo(file: File): String {
        var maxCommon = pathSegments.zip(file.pathSegments).indexOfFirst {
            it.first != it.second
        }
        if (maxCommon < 0) {
            return path
        }
        return (
            List(file.pathSegments.size - maxCommon - 1) { ".." } + pathSegments.subList(
                maxCommon,
                pathSegments.size
            )
            ).joinToString("/")
    }

    fun mkdirs() {
        if (pathSegments.isNotEmpty()) {
            parent.mkdirs()
        }
        if (access(path, F_OK) != 0) {
            mkdir(path, 0b111111111U)
        }
    }

    fun exists(): Boolean {
        return access(path, F_OK) == 0
    }

    companion object {
        val CWD: File
            get() = File(getcwd())
    }
}
