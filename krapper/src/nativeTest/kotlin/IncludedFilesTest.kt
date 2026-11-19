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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.DefaultFilter
import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.canonicalPath
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import platform.posix.symlink

/**
 * #297 — krapper reports the files its parses opened, so the sync can re-run when a header the
 * module reaches only transitively changes. The include dir is a symlink, so only the canonical
 * spelling of the included header matches. The root header itself is parsed as the in-memory
 * main buffer, not opened, so the plugin declares it directly.
 */
class IncludedFilesTest {

    @Test
    fun the_include_set_names_a_transitively_included_header() = runBlocking {
        val dir = File.createTempDir("krapper_included_files")
        try {
            val real = File(dir, "real").also { it.mkdirs() }
            File(real, "outer.h").writeText("#include \"inner.h\"\nstruct Outer {};\n")
            File(real, "inner.h").writeText("struct Inner {};\n")
            check(symlink(real.path, File(dir, "link").path) == 0)

            val service = KrapperServiceImpl()
            service.setConfig(
                KrapperConfig(
                    pkg = "p",
                    compiler = "clang++",
                    moduleName = "P",
                    errorPolicy = ErrorPolicy.LOG,
                    referencePolicy = ReferencePolicy.IGNORE_MISSING,
                    debug = false
                )
            )
            val indexed = service.index(
                IndexRequest(
                    headers = listOf(File(real, "outer.h").path),
                    libraries = emptyList(),
                    includeDirs = listOf(File(dir, "link").path)
                )
            )
            indexed.filterAndResolve(DefaultFilter)
            val included = indexed.includedFiles(Unit)

            val inner = canonicalPath(File(real, "inner.h").path)
            assertTrue(inner in included, "$inner must be in the include set: $included")
        } finally {
            dir.rmR()
        }
    }
}
