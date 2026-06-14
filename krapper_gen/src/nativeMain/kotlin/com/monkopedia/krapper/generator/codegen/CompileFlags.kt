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
package com.monkopedia.krapper.generator.codegen

class CompileFlags(
    private val headers: List<String>,
    libraries: List<String>,
    linkStatics: Boolean = false,
    // Extra include dirs from the module's `kplusplus { headerDirectory(...) }` (threaded by
    // the gradle plugin via krapper_gen's --include-dir). The header files' own parents are
    // always on -I; these add cross-directory roots so quote-includes in the wrapper compile
    // resolve the same way the cpp front-end's parse did.
    private val extraIncludeDirs: List<String> = emptyList()
) {

    init {
        libraries.forEach {
            if (!it.endsWith(".a") && !it.endsWith(".so")) {
                error("Unable to handle file type $it")
            }
        }
    }

    val headerFiles = (headers.map(::File)).toSet()
    private val staticLibs = libraries.filter { it.endsWith(".a") && !linkStatics }.map(::File)
    private val dynamicLibs = libraries.filter { linkStatics || it.endsWith(".so") }.map(::File)

    val linkerOpts: String?
        get() {
            if (dynamicLibs.isEmpty()) return null

            val dynamicPaths = dynamicLibs.map { it.parent }.toSet().joinToString(" ") {
                "-L${it.path}"
            }
            val dynamicLinks = dynamicLibs.joinToString(" ") {
                var name = it.name
                if (name.startsWith("lib")) {
                    name = name.substring("lib".length)
                }
                if (name.endsWith(".so")) {
                    name = name.substring(0, name.length - ".so".length)
                }
                if (name.endsWith(".a")) {
                    name = name.substring(0, name.length - ".a".length)
                }
                "-l$name"
            }
            return "$dynamicPaths $dynamicLinks"
        }

    val libraryOpts: String?
        get() {
            if (staticLibs.isEmpty()) return null
            return staticLibs.joinToString(" ") { it.name }
        }

    val libraryPaths: String?
        get() {
            if (staticLibs.isEmpty()) return null
            return staticLibs.map { it.parent }.toSet().joinToString(" ") { it.path }
        }

    val includeDirs: String?
        get() {
            if (headerFiles.isEmpty()) return null
            val headerParents = headerFiles.map { it.parent.path }
            return (headerParents + extraIncludeDirs).toSet().joinToString(" ") { "-I$it" } +
                " -DV8_COMPRESS_POINTERS"
        }
}
