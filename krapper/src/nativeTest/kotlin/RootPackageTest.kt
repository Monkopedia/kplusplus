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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.generator.model.WrappedKotlinType
import com.monkopedia.krapper.generator.model.fullyQualifiedType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exercises both paths of the configurable root-package override
 * (`config.rootPackage` -> `GenerationContext.rootPackage`): UNSET (null) preserves the
 * historical `root`/bare-namespace packages, and SET rebases every wrapper
 * package under the configured segments. The UNSET path is also covered
 * byte-for-byte by the full featuregen golden suite.
 */
class RootPackageTest {

    // Run [body] under a GenerationContext rooted at [rootPackage]. `using` restores the
    // previous installation on the way out, so — unlike the `reset()`-a-global shape this
    // replaced (brick B4) — an override cannot leak into another test even if body throws.
    private fun <T> withRootPackage(rootPackage: String?, body: () -> T): T =
        GenerationContext.using(GenerationContext(rootPackage), body)

    @Test
    fun unsetTopLevelGoesToRoot() = withRootPackage(null) {
        val type = fullyQualifiedType("Widget", isWrapper = true)
        assertEquals("root", type.pkg)
        assertEquals("Widget", type.name)
    }

    @Test
    fun unsetNamespaceStaysBare() = withRootPackage(null) {
        // A namespaced wrapper (`std::vector` -> `std.Vector`) keeps its bare path.
        val type = fullyQualifiedType("std.Vector", isWrapper = true)
        assertEquals("std", type.pkg)
        assertEquals("Vector", type.name)
    }

    @Test
    fun setRootRebasesTopLevel() = withRootPackage("com.acme.app") {
        val type = fullyQualifiedType("Widget", isWrapper = true)
        assertEquals("com.acme.app", type.pkg)
        assertEquals("Widget", type.name)
    }

    @Test
    fun setRootPrefixesNamespace() = withRootPackage("com.acme.app") {
        // Mirrors how `std::vector<int>`'s base `std::vector` is packaged: the
        // namespace path is prefixed with the configured root package.
        val type = fullyQualifiedType("std.Vector", isWrapper = true)
        assertEquals("com.acme.app.std", type.pkg)
        assertEquals("Vector", type.name)
    }

    @Test
    fun setRootIsIdempotentForAlreadyRootedNames() = withRootPackage("com.acme.app") {
        // The template path re-wraps an already-rooted package (baseType.pkg + the
        // mangled name) back through fullyQualifiedType. Rooting must be idempotent or
        // `std::vector<int>` would double to `com.acme.app.com.acme.app.std.Vector__Int`.
        val type = fullyQualifiedType("com.acme.app.std.Vector__Int", isWrapper = true)
        assertEquals("com.acme.app.std", type.pkg)
        assertEquals("Vector__Int", type.name)
    }

    @Test
    fun setRootViaTemplateFactory() = withRootPackage("com.acme.app") {
        // The string factory drives the real codegen path: `std::vector<int>`
        // -> base `std.Vector__Int` in package `com.acme.app.std`.
        val type = WrappedKotlinType("std.Vector__Int")
        assertEquals("com.acme.app.std", type.pkg)
    }

    @Test
    fun setRootDoesNotTouchNativeTypes() = withRootPackage("com.acme.app") {
        // Non-wrapper (platform/native) names must never be rebased — they are
        // already correct fully-qualified Kotlin/Native typealiases.
        val type = fullyQualifiedType("platform.posix.size_t", isWrapper = false)
        assertEquals("platform.posix", type.pkg)
        assertEquals("size_t", type.name)
    }
}
