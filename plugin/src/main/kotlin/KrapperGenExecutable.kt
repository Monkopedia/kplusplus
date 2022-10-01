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
