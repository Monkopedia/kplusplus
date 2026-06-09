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

import clang.CXCursorKind.CXCursor_TypeAliasDecl
import clang.CXCursorKind.CXCursor_TypedefDecl
import clang.CXType
import clang.CXTypeKind.CXType_Pointer
import com.monkopedia.krapper.generator.DropLedger
import com.monkopedia.krapper.generator.DropPhase
import com.monkopedia.krapper.generator.canonicalType
import com.monkopedia.krapper.generator.children
import com.monkopedia.krapper.generator.getTemplateArgumentType
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.numTemplateArguments
import com.monkopedia.krapper.generator.pointeeType
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.typeDeclaration
import com.monkopedia.krapper.generator.typedefDeclUnderlyingType
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents

/**
 * The result of recognizing a `llvm::iterator_range<It>`-returning method (T1.3).
 *
 * [elementSpelling] is the fully-qualified C++ class the range iterates over with all
 * cv-qualifiers and pointer/reference decoration stripped (e.g. `clang::Decl`,
 * `clang::CXXMethodDecl`, `clang::CXXBaseSpecifier`). It is both the element type bound
 * into the materialized `std::vector<Elem*>` return and the explicit template argument
 * for the `kpp_to_elem_ptr<Elem>(*it)` normalizer the CppWriter loop emits.
 */
data class RangeReturn(val elementSpelling: String)

/**
 * Detect a method return type of `llvm::iterator_range<It>` and recover the element class
 * `Elem` the range yields, so the method can be materialized into a bound
 * `std::vector<Elem*>` (T1.3). Returns null for any non-range return (the method keeps its
 * original return handling).
 *
 * Works off the CANONICAL return type, so the `using X_range = iterator_range<...>` member
 * aliases Clang sprinkles everywhere (`decl_range`, `method_range`, `base_class_range`, …)
 * are all seen through to their `llvm::iterator_range<It>` form before matching.
 *
 * Element extraction mirrors what `*it` dereferences to, normalized to the underlying
 * class (the vector holds `Elem*`):
 *  - `It` is a pointer (`Decl**`, `CXXBaseSpecifier*`): the iterated element is the
 *    pointee; if the pointee is itself a pointer (`Decl**` → `Decl*`) strip that level too,
 *    so a pointer-of-pointer iterator still yields the leaf class `Decl`.
 *  - `It` is an iterator class exposing a `value_type` typedef (`decl_iterator`'s
 *    `value_type = Decl*`): strip any trailing `*` to get the class.
 *  - `It` is a template specialization with no usable `value_type`
 *    (`specific_decl_iterator<CXXMethodDecl>`, whose `operator*`/`value_type` are dependent
 *    and unreadable): take the iterator's own first template argument as the element.
 *
 * All three cases were validated against the real Clang AST for `decls()`/`methods()`/
 * `fields()`/`bases()`; the resulting C++ `kpp_to_elem_ptr<Elem>(*it)` push compiles
 * against libclang-cpp for every shape.
 */
fun extractRangeReturn(resultType: CValue<CXType>): RangeReturn? {
    val canonical = resultType.canonicalType
    val canonicalSpelling = canonical.spelling.toKString() ?: return null
    if (!canonicalSpelling.contains("iterator_range<")) return null
    if (canonical.numTemplateArguments < 1) return null
    val iterator = canonical.getTemplateArgumentType(0u)
    val element = elementOfIterator(iterator) ?: return null
    return RangeReturn(element)
}

private fun elementOfIterator(iterator: CValue<CXType>): String? {
    val canon = iterator.canonicalType
    if (canon.useContents { kind } == CXType_Pointer) {
        val pointee = canon.pointeeType.canonicalType
        // `Decl**` → element is `Decl` (peel the second pointer); `CXXBaseSpecifier*` →
        // element is `CXXBaseSpecifier` (peel the lone pointer).
        val element =
            if (pointee.useContents { kind } == CXType_Pointer) {
                pointee.pointeeType.canonicalType
            } else {
                pointee
            }
        return stripDecoration(element.spelling.toKString())
    }
    // A class iterator. Prefer its `value_type` typedef (e.g. decl_iterator's `Decl*`).
    val decl = canon.typeDeclaration
    val valueType = decl.children.firstOrNull {
        (it.kind == CXCursor_TypedefDecl || it.kind == CXCursor_TypeAliasDecl) &&
            it.spelling.toKString() == "value_type"
    }
    if (valueType != null) {
        val underlying = valueType.typedefDeclUnderlyingType.canonicalType
        return stripDecoration(underlying.spelling.toKString())
    }
    // No readable value_type (e.g. specific_decl_iterator<X>, whose members are dependent):
    // the iterator's own first template argument is the element class.
    if (canon.numTemplateArguments >= 1) {
        return stripDecoration(canon.getTemplateArgumentType(0u).spelling.toKString())
    }
    // Recognized an `iterator_range<It>` but couldn't recover the element from `It` (no
    // pointer/`value_type`/template-arg shape matched). The range method is dropped — keep
    // that DECISION, but make it discoverable: a silent null here was an unanswerable "did
    // the generator drop it?". PARSE phase: this is a cursor/CXType-level pass with no
    // ResolveContext yet. (DropLedger.record is non-suspend, so it's callable here.)
    DropLedger.record(
        symbol = iterator.spelling.toKString() ?: "<unknown iterator_range element>",
        reason = "iterator_range element type not extractable from iterator " +
            "(no pointer/value_type/template-argument shape matched)",
        phase = DropPhase.PARSE
    )
    return null
}

// Reduce a type spelling to its bare class name: drop a leading `const`, then any
// trailing `*`/`&`/`const`/whitespace. `clang::Decl *const *` → `clang::Decl`,
// `const clang::CXXBaseSpecifier` → `clang::CXXBaseSpecifier`.
private fun stripDecoration(spelling: String?): String? {
    var s = (spelling ?: return null).trim()
    if (s.isEmpty()) return null
    while (true) {
        val before = s
        if (s.startsWith("const ")) s = s.substring("const ".length).trim()
        if (s.endsWith("*") || s.endsWith("&")) s = s.dropLast(1).trim()
        if (s.endsWith("const")) s = s.dropLast("const".length).trim()
        if (s == before) break
    }
    return s.ifEmpty { null }
}
