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
package com.monkopedia.krapper

import kotlinx.serialization.Serializable

enum class ErrorPolicy {
    FAIL,
    LOG
}

enum class ReferencePolicy {
    IGNORE_MISSING,
    OPAQUE_MISSING,
    THROW_MISSING,
    INCLUDE_MISSING
}

@Serializable
data class KrapperConfig(
    val pkg: String,
    val compiler: String,
    val moduleName: String,
    val errorPolicy: ErrorPolicy,
    val referencePolicy: ReferencePolicy,
    val debug: Boolean,
    // C++ standard libclang parses headers under (e.g. "c++14", "c++17",
    // "c++20"). Becomes the `--std=<value>` parse arg. Defaults to c++14 to
    // preserve historical behavior; bump to reach C++17 types like
    // std::string_view or C++20 char8_t.
    val cppStandard: String = "c++14",
    // Root package for generated bindings. Null (default) preserves historical
    // behavior: top-level user types -> package `root`, C++ namespaces -> their
    // bare path (`std`, `geo`). When set (e.g. "com.acme.app"), every generated
    // binding's package becomes <rootPackage> + the C++ namespace path.
    val rootPackage: String? = null,
    // When true, a run that dropped ANY unmodelable symbol (skip-not-crash) fails
    // after emitting the drop-ledger report, instead of completing leniently. Opt-in
    // (default false) so existing green runs keep succeeding: drops are always logged
    // and ledgered, but only this flag turns a drop into a non-zero exit.
    val failOnDrop: Boolean = false
)
