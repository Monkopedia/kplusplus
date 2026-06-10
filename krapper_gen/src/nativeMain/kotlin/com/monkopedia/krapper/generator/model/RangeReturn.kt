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
package com.monkopedia.krapper.generator.model

/**
 * The result of recognizing a `llvm::iterator_range<It>`-returning method (T1.3).
 *
 * [elementSpelling] is the fully-qualified C++ class the range iterates over with all
 * cv-qualifiers and pointer/reference decoration stripped (e.g. `clang::Decl`,
 * `clang::CXXMethodDecl`, `clang::CXXBaseSpecifier`). It is both the element type bound
 * into the materialized `std::vector<Elem*>` return and the explicit template argument
 * for the `kpp_to_elem_ptr<Elem>(*it)` normalizer the CppWriter loop emits.
 *
 * Recognition/extraction is cursor-level work and lives with the libclang front-end
 * (ModelFactories.kt); this file carries only the front-end-agnostic result.
 */
data class RangeReturn(val elementSpelling: String)
