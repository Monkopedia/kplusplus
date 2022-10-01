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
package com.monkopedia.kplusplus

import java.io.File

object KrapperGenExecutable {
    private var libsFile: File? = null

    fun getExeFile(exeHome: File): File {
        return libsFile ?: createExeFile(exeHome).also {
            libsFile = it
        }
    }

    private fun createExeFile(exeHome: File): File {
        return File(exeHome, "krapper_gen.kexe").also { exeFile ->
            if (!exeFile.parentFile.exists()) {
                exeFile.parentFile.mkdirs()
            }
            exeFile.outputStream().use { output ->
                this::class.java.getResourceAsStream("/krapper_gen.kexe").use {
                    it.copyTo(output)
                }
            }
            exeFile.setExecutable(true)
        }
    }
}
