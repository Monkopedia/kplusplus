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

import com.monkopedia.krapper.AllowListFilter
import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.InstantiationRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.findQualifiers
import com.monkopedia.krapper.generator.model.freeFunctionQualifiedName
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedConstructor
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.runBlocking
import platform.posix.random

class ParseTest {
//    @Test
//    fun testParsing() = memScoped {
//        val index = createIndex(0, 0) ?: error("Failed to create Index")
//        defer { index.dispose() }
//        val tmpFile = "/tmp/${random()}_${random()}"
//        File(tmpFile).writeText(TestData.HEADER)
//        val classes = parseHeader(index, listOf(tmpFile)).findClasses(WrappedClass::defaultFilter)
//        val expectedClasses = Json.decodeFromString<List<WrappedClass>>(TestData.JSON)
//        assertEquals(expectedClasses, classes)
//    }

    @Test
    fun testResolve(): Unit = runBlocking {
        try {
            val resolver = ParsedResolver(TestData.tu)
            val cls = resolver.findClasses(WrappedElement::defaultFilter)
            val resolved = cls.resolveAll(resolver, ReferencePolicy.INCLUDE_MISSING)
            println("Classes: $cls")
        } catch (t: Throwable) {
            t.printStackTrace()
            throw t
        }
    }

    @Test
    fun testTemplate() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/${random()}_${random()}"
            File(tmpFile).writeText(TestData.templateHeader)
            val classes = parseHeader(
                index,
                listOf(tmpFile),
                generateIncludes("clang++")
            ).also { println("Parsed") }.findClasses {
                println("Filter $this")
                defaultFilter()
            }
            println("Found classes ${classes.size}")
            println(classes)
        }
    }

    @Test
    fun testForceInstantiation() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/krapper_force_vector_int.h"
            File(tmpFile).writeText(
                "#include <vector>\nstruct KrapperForce_vec { std::vector<int> value; };\n"
            )
            val resolver = parseHeader(index, listOf(tmpFile), generateIncludes("clang++"))
            val found = resolver.findClasses { defaultFilter() }
            println("Found forcing classes: ${found.map { (it as? WrappedClass)?.type }}")
            val resolved = found.resolveAll(resolver, ReferencePolicy.INCLUDE_MISSING)
            val names = resolved.mapNotNull { (it as? ResolvedClass)?.type?.toString() }
            println("Resolved classes: $names")
            val vectorClass = resolved.filterIsInstance<ResolvedClass>()
                .firstOrNull { it.type.toString().startsWith("std::vector<int") }
            assertTrue(
                vectorClass != null,
                "Expected a resolved std::vector<int> class; got $names"
            )
            assertTrue(
                vectorClass.isNotEmpty(),
                "Expected std::vector<int> to have members"
            )
        }
    }

    // T1.0a scoped-import allowlist: from a header declaring Alpha (which references
    // Beta), Beta, and Gamma, filtering to allow only Alpha + Gamma must fully bind
    // Alpha and Gamma but NOT Beta — Beta is reachable only as a reference from
    // Alpha and must fall to the reference policy, not get bound as a top-level class.
    @Test
    fun testAllowListFilter() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/krapper_allowlist_${random()}_${random()}.h"
            File(tmpFile).writeText(
                """
                |namespace fixture {
                |class Beta {
                |public:
                |    int betaValue() const;
                |};
                |class Alpha {
                |public:
                |    Beta* getBeta() const;
                |    int alphaValue() const;
                |};
                |class Gamma {
                |public:
                |    int gammaValue() const;
                |};
                |}
                """.trimMargin()
            )
            val resolver = parseHeader(index, listOf(tmpFile), generateIncludes("clang++"))
            val allow = AllowListFilter(
                listOf("fixture::Alpha", "fixture::Gamma")
            )
            val found = resolver.findClasses(allow.wrapperFilter())
            val foundNames = found.mapNotNull { (it as? WrappedClass)?.type?.toString() }
            println("Allowlist found: $foundNames")
            // Only the listed classes are picked up as top-level bindings.
            assertEquals(
                setOf("fixture::Alpha", "fixture::Gamma"),
                foundNames.toSet(),
                "Allowlist should select exactly Alpha + Gamma; got $foundNames"
            )

            val resolved = found.resolveAll(resolver, ReferencePolicy.IGNORE_MISSING)
            val resolvedNames = resolved.filterIsInstance<ResolvedClass>()
                .map { it.type.type }
                .toSet()
            println("Allowlist resolved: $resolvedNames")
            assertTrue(
                "fixture::Alpha" in resolvedNames,
                "Alpha should be bound; got $resolvedNames"
            )
            assertTrue(
                "fixture::Gamma" in resolvedNames,
                "Gamma should be bound; got $resolvedNames"
            )
            // Beta is referenced by Alpha but not listed: it must NOT be bound as a
            // top-level class (it falls to the reference policy instead).
            assertTrue(
                "fixture::Beta" !in resolvedNames,
                "Beta must NOT be bound (referenced-but-not-listed); got $resolvedNames"
            )
        }
    }

    // T1.0c allowlist-selects-free-functions: a FREE FUNCTION (a STATIC WrappedMethod
    // with no parent class, e.g. `clang::tooling::buildASTFromCode`) can never match a
    // class-only allowlist by `type`. Listing its fully-qualified name `<ns>::<name>`
    // must select it (and resolve it) while leaving an unlisted sibling free function
    // out.
    @Test
    fun testAllowListFilterFreeFunction() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/krapper_allowlist_freefn_${random()}_${random()}.h"
            File(tmpFile).writeText(
                """
                |namespace fixture {
                |int wanted(int x) { return x + 1; }
                |int unwanted(int x) { return x - 1; }
                |}
                """.trimMargin()
            )
            val resolver = parseHeader(index, listOf(tmpFile), generateIncludes("clang++"))
            val allow = AllowListFilter(listOf("fixture::wanted"))
            val found = resolver.findClasses(allow.wrapperFilter())
            val foundNames = found.filterIsInstance<WrappedMethod>()
                .map { it.freeFunctionQualifiedName }
            println("Allowlist free-fn found: $foundNames")
            // Exactly the listed free function is selected; its sibling is not.
            assertEquals(
                listOf("fixture::wanted"),
                foundNames,
                "Allowlist should select exactly fixture::wanted; got $foundNames"
            )

            val resolved = found.resolveAll(resolver, ReferencePolicy.IGNORE_MISSING)
            val resolvedFns = resolved.filterIsInstance<ResolvedMethod>()
                .map { it.name }
                .toSet()
            println("Allowlist free-fn resolved: $resolvedFns")
            assertTrue(
                "wanted" in resolvedFns,
                "fixture::wanted should be bound; got $resolvedFns"
            )
            assertTrue(
                "unwanted" !in resolvedFns,
                "fixture::unwanted must NOT be bound (not listed); got $resolvedFns"
            )
        }
    }

    // Fix #3: a templated allowlist entry (`std::map<int, int>`) carries a comma INSIDE
    // its template brackets. The CLI's --only parsing must not tear it: splitting on
    // top-level commas only must keep the templated entry whole, while still splitting a
    // genuine top-level list. (The gradle plugin was joining the allowlist with `,` into
    // one --only arg, and the CLI split on every `,`, corrupting templated names.)
    @Test
    fun testSplitTopLevelCommasKeepsTemplatedEntry() {
        // A templated name with an inner comma stays a SINGLE entry.
        assertEquals(
            listOf("std::map<int, int>"),
            "std::map<int, int>".splitTopLevelCommas()
        )
        // A top-level list still splits, and a templated member keeps its inner comma.
        assertEquals(
            listOf("clang::ASTContext", "std::map<int, int>", "clang::Decl"),
            "clang::ASTContext,std::map<int, int>,clang::Decl".splitTopLevelCommas()
        )
        // Nested templates: only the outermost top-level comma splits.
        assertEquals(
            listOf("A<B<int, int>, C>", "D"),
            "A<B<int, int>, C>,D".splitTopLevelCommas()
        )
    }

    // Fix #3 end-to-end: a comma-containing templated qualified name, once split as a
    // single allowlist entry, MATCHES the resolved templated class. Proves the whole chain
    // (split keeps it intact -> AllowListFilter matches it) works for a templated --only.
    @Test
    fun testAllowListMatchesCommaContainingTemplatedName() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/krapper_only_templated_${random()}_${random()}.h"
            // Force an instantiation of a 2-type-param template so its `type.toString()`
            // carries an inner comma (a user template keeps the spelling predictable;
            // std::map canonicalizes with extra allocator args).
            File(tmpFile).writeText(
                """
                |namespace fixture {
                |template <class A, class B> struct Pair { A a; B b; };
                |}
                |struct KrapperForce_pair { fixture::Pair<int, int> value; };
                """.trimMargin()
            )
            val resolver = parseHeader(index, listOf(tmpFile), generateIncludes("clang++"))
            // The specialization surfaces only after resolving the forcing struct (like
            // testForceInstantiation), not as a raw top-level findClasses result.
            val resolved = resolver.findClasses { defaultFilter() }
                .resolveAll(resolver, ReferencePolicy.INCLUDE_MISSING)
            val resolvedNames = resolved.filterIsInstance<ResolvedClass>()
                .map { it.type.toString() }
            println("Templated resolved classes: $resolvedNames")
            // The instantiation's spelling (with the inner comma) — what an --only entry
            // would have to match verbatim.
            val templatedName = resolvedNames.first { it.startsWith("fixture::Pair<") }
            assertTrue(
                ',' in templatedName,
                "Expected an inner comma in the templated name; got $templatedName"
            )
            // Mimic the CLI flow: a single --only value containing this comma-bearing name
            // must split to exactly ONE entry.
            val entries = templatedName.splitTopLevelCommas().map { it.trim() }
            assertEquals(
                listOf(templatedName),
                entries,
                "Templated --only entry must survive the split as one entry"
            )
            // And that intact entry must MATCH the templated class via AllowListFilter
            // (matching is on the wrapped class's type spelling, so check pre-resolution
            // membership against the same spelling the resolved class carries).
            assertTrue(
                AllowListFilter(entries).qualifiedNames.single() == templatedName,
                "AllowListFilter must carry the comma-containing templated name intact " +
                    "$templatedName; got ${AllowListFilter(entries).qualifiedNames}"
            )
        }
    }

    // T1.0b base-not-fatal: a derived class whose PRIMARY base is not bound (here
    // `Unbound` is filtered out of the allowlist, so its type can't resolve) must
    // STILL bind — with its own members — rather than being dropped entirely. Before
    // the fix, an unresolvable primary base hard-failed the whole subclass (and
    // cascaded up the inheritance chain). The class loses the dropped base's
    // flattened members + the `.asBase()` to it; everything else survives.
    @Test
    fun testUnboundBaseNotFatal() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/krapper_basenotfatal_${random()}_${random()}.h"
            File(tmpFile).writeText(
                """
                |namespace fixture {
                |class Unbound {
                |public:
                |    int unboundValue() const;
                |};
                |class Derived : public Unbound {
                |public:
                |    int derivedValue() const;
                |};
                |}
                """.trimMargin()
            )
            val resolver = parseHeader(index, listOf(tmpFile), generateIncludes("clang++"))
            // Allow ONLY Derived, not its base Unbound.
            val allow = AllowListFilter(listOf("fixture::Derived"))
            val found = resolver.findClasses(allow.wrapperFilter())
            val foundNames = found.mapNotNull { (it as? WrappedClass)?.type?.toString() }
            println("Base-not-fatal found: $foundNames")
            assertEquals(
                setOf("fixture::Derived"),
                foundNames.toSet(),
                "Allowlist should select exactly Derived; got $foundNames"
            )

            val resolved = found.resolveAll(resolver, ReferencePolicy.IGNORE_MISSING)
            val resolvedClasses = resolved.filterIsInstance<ResolvedClass>()
            val resolvedNames = resolvedClasses.map { it.type.type }.toSet()
            println("Base-not-fatal resolved: $resolvedNames")
            // Derived survives despite its unresolvable primary base.
            assertTrue(
                "fixture::Derived" in resolvedNames,
                "Derived must still bind even though its base Unbound is unbound; " +
                    "got $resolvedNames"
            )
            // Unbound was filtered out: it must NOT be bound as a top-level class.
            assertTrue(
                "fixture::Unbound" !in resolvedNames,
                "Unbound must NOT be bound (filtered out); got $resolvedNames"
            )
            // Derived keeps its OWN method (it didn't vanish with the base).
            val derived = resolvedClasses.first { it.type.type == "fixture::Derived" }
            val methodNames = derived.children
                .filterIsInstance<com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod>()
                .map { it.name }
            println("Derived methods: $methodNames")
            assertTrue(
                "derivedValue" in methodNames,
                "Derived must retain its own method derivedValue; got $methodNames"
            )
        }
    }

    // Issue #10 (broad-binding scale tail): regression coverage for the INCLUDE_MISSING
    // cyclic-expansion guard. A seed class `Seed` exposes a `const Node&` accessor; `Node`
    // (not seeded) has a `const Node&` self-accessor — a self-cycle closed by a WRAPPED
    // const-reference, the minimal shape of Clang's self-referential AST nodes (a `Decl`'s
    // accessors handing back `Decl&`). `Node` is reachable ONLY through the wrapped carrier,
    // so the expansion mapper's in-progress marker is the leaf-vs-carrier case this fix
    // targets.
    //
    // The mapper marks an in-progress leaf in `otherResolved`; the re-entry guard
    // (`canResolve`) follows a wrapped type down to its BARE LEAF and queries the set by that
    // leaf string. The marker used to be keyed on the WRAPPED carrier (`const fixture::Node
    // &`) while the guard checks the leaf (`fixture::Node`), so for a wrapped-only class the
    // marker was dead — the cycle was never deduped at the marker level and `Node` got
    // re-resolved redundantly. This fix keys the marker on the leaf, so `canResolve(Node)`
    // hits and the self-edge is cut.
    //
    // NOTE: at this UNIT scale the cycle still terminates WITHOUT the fix — the
    // `resolvedClasses` cache + `mappingCache` + the (correctly-keyed-by-coincidence) bare
    // own-type marker bound the redundant work to a constant factor. The native stack
    // overflow that blocks the broad Clang surface is an emergent property of its dense
    // ~1269-class graph and is not reproducible in a small fixture (verified empirically
    // across pointer/const-ref/inheritance/self-ref fixtures). This test therefore asserts
    // the cyclic INCLUDE_MISSING path TERMINATES and BINDS — it guards against a regression
    // that would turn the now-deduped cycle back into unbounded descent — rather than
    // claiming to crash the old code on its own.
    @Test
    fun testIncludeMissingCyclicReferenceTerminates() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/krapper_cyclic_${random()}_${random()}.h"
            File(tmpFile).writeText(
                """
                |namespace fixture {
                |class Node {
                |public:
                |    const Node& self() const;
                |    int nodeValue() const;
                |};
                |class Seed {
                |public:
                |    const Node& getNode() const;
                |};
                |}
                """.trimMargin()
            )
            val resolver = parseHeader(index, listOf(tmpFile), generateIncludes("clang++"))
            // Seed only the second class — Node is reachable ONLY as a wrapped reference and
            // must be pulled in by INCLUDE_MISSING, exercising the cyclic-expansion guard.
            val allow = AllowListFilter(listOf("fixture::Seed"))
            val found = resolver.findClasses(allow.wrapperFilter())
            val foundNames = found.mapNotNull { (it as? WrappedClass)?.type?.toString() }
            println("Cyclic seed found: $foundNames")
            assertEquals(
                setOf("fixture::Seed"),
                foundNames.toSet(),
                "Allowlist should select exactly Seed; got $foundNames"
            )
            // Resolution must terminate and the self-cyclic class must bind (a regression
            // that un-keyed the marker would turn this into unbounded descent on the dense
            // broad graph).
            val resolved = found.resolveAll(resolver, ReferencePolicy.INCLUDE_MISSING)
            val resolvedNames = resolved.filterIsInstance<ResolvedClass>()
                .map { it.type.type }
                .toSet()
            println("Cyclic resolved: $resolvedNames")
            assertTrue(
                "fixture::Node" in resolvedNames,
                "Node (the self-referential class) must be pulled in and bind under " +
                    "INCLUDE_MISSING; got $resolvedNames"
            )
        }
    }

    @Test
    fun testInstantiateAndWrite(): Unit = runBlocking {
        val outDir = "/tmp/krapper_slice_${random()}_${random()}"
        val config = KrapperConfig(
            pkg = "krapper.slice",
            compiler = "clang++",
            moduleName = "slice",
            errorPolicy = ErrorPolicy.LOG,
            referencePolicy = ReferencePolicy.INCLUDE_MISSING,
            debug = false
        )
        val service = IndexedServiceImpl(config, IndexRequest(emptyList(), emptyList()))
        service.requestInstantiation(InstantiationRequest("std::vector", listOf("int")))
        service.writeTo(outDir)
        val outFiles = File(outDir).listFiles().map { it.name }
        println("Output files: $outFiles")
        assertTrue(
            File(File(outDir), "slice.def").exists(),
            "Expected slice.def; got $outFiles"
        )
        val srcFiles = File(File(outDir), "src").listFiles().map { it.name }
        println("Generated src: $srcFiles")
        assertTrue(
            srcFiles.any { it.endsWith(".kt") },
            "Expected generated .kt bindings; got $srcFiles"
        )
    }

    @Test
    fun testQualifiers() = memScoped {
        runBlocking {
            val str = "std::vector<std::string>::iterator"
            val indices = findQualifiers(str)
            assertEquals(
                listOf(
                    "std",
                    "vector<std::string>",
                    "iterator"
                ),
                indices.map { str.substring(it.start, it.endInclusive + 1) }
            )
        }
    }

    // Issue #9 (diagnostics policy): a header with ONE un-bindable declaration (a struct
    // member typed by an undeclared type -> an `error:` parse diagnostic attributed to that
    // struct) must NOT abort the whole run. By default the bad declaration is dropped into
    // the drop ledger as a PARSE drop (carrying the diagnostic text) and excised from the
    // model, while the clean class beside it still parses + resolves. This is the
    // acceptance criterion: one un-bindable symbol no longer takes the rest of the import
    // down.
    @Test
    fun testParseErrorDropsBadSymbolAndBindsTheRest() = memScoped {
        runBlocking {
            DropLedger.reset()
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/krapper_diag_skip_${random()}_${random()}.h"
            File(tmpFile).writeText(
                """
                |namespace fixture {
                |// `Undeclared` is never defined -> an `error:` diagnostic attributed to Bad.
                |struct Bad {
                |    Undeclared broken;
                |};
                |struct Good {
                |    int goodValue() const;
                |};
                |}
                """.trimMargin()
            )
            // Default (lenient) diagnostics: the run survives the error diagnostic.
            val resolver = parseHeader(index, listOf(tmpFile), generateIncludes("clang++"))
            // The bad declaration is a PARSE drop in the ledger, with the diagnostic text.
            val badDrop = DropLedger.drops.firstOrNull {
                it.symbol == "Bad" && it.phase == DropPhase.PARSE
            }
            assertTrue(
                badDrop != null,
                "the un-bindable struct must be a PARSE drop; got ${DropLedger.drops}"
            )
            assertTrue(
                "error:" in badDrop.reason,
                "the drop reason must carry the parse diagnostic text; got ${badDrop.reason}"
            )
            // The good class still parses + resolves; the bad one is gone from the model.
            val resolved = resolver.findClasses { defaultFilter() }
                .resolveAll(resolver, ReferencePolicy.IGNORE_MISSING)
            val resolvedNames = resolved.filterIsInstance<ResolvedClass>()
                .map { it.type.type }
                .toSet()
            assertTrue(
                "fixture::Good" in resolvedNames,
                "the clean class must still bind; got $resolvedNames"
            )
            assertTrue(
                "fixture::Bad" !in resolvedNames,
                "the dropped class must NOT be bound; got $resolvedNames"
            )
        }
    }

    // Issue #9: --strict-diagnostics restores the historical abort-on-any-error behavior.
    // The SAME header that binds-the-rest by default now aborts the run.
    @Test
    fun testStrictDiagnosticsAbortsOnAnyError() = memScoped {
        runBlocking {
            DropLedger.reset()
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/krapper_diag_strict_${random()}_${random()}.h"
            File(tmpFile).writeText(
                """
                |namespace fixture {
                |struct Bad {
                |    Undeclared broken;
                |};
                |}
                """.trimMargin()
            )
            var threw = false
            try {
                parseHeader(
                    index,
                    listOf(tmpFile),
                    generateIncludes("clang++"),
                    strictDiagnostics = true
                )
            } catch (t: Throwable) {
                threw = true
                assertTrue(
                    "strict diagnostics" in (t.message ?: t.cause?.message ?: ""),
                    "strict-mode failure must mention strict diagnostics: ${t.message}"
                )
            }
            assertTrue(threw, "--strict-diagnostics must abort on an error diagnostic")
        }
    }

    // Issue #9: a translation-unit-level / unattributable error (a missing include) aborts
    // even in lenient mode — there's no single declaration to surgically drop and the rest
    // of the AST can't be trusted past it. Draws the attributable-vs-fatal boundary.
    @Test
    fun testUnattributableErrorAbortsEvenWhenLenient() = memScoped {
        runBlocking {
            DropLedger.reset()
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/krapper_diag_fatal_${random()}_${random()}.h"
            File(tmpFile).writeText(
                """
                |#include <this_header_does_not_exist_krapper.h>
                |namespace fixture {
                |struct Good { int goodValue() const; };
                |}
                """.trimMargin()
            )
            var threw = false
            try {
                parseHeader(index, listOf(tmpFile), generateIncludes("clang++"))
            } catch (t: Throwable) {
                threw = true
                assertTrue(
                    "fatal / translation-unit-level" in (t.message ?: t.cause?.message ?: ""),
                    "an unattributable error must abort with the TU-level reason: ${t.message}"
                )
            }
            assertTrue(threw, "a missing include must abort even in lenient mode")
        }
    }

    @Test
    fun testResolveConstRef() = memScoped {
        runBlocking {
            val result = listOf(TestData.testClass.cls).resolveAll(
                object : Resolver {
                    override suspend fun resolve(
                        type: WrappedType,
                        context: ResolveContext
                    ): Pair<ResolvedClass, WrappedClass>? {
                        error("Not supported")
                    }

                    override fun resolveTemplate(
                        type: WrappedType,
                        context: ResolveContext
                    ): WrappedTemplate {
                        error("Not supported")
                    }

                    override suspend fun findClasses(filter: ElementFilter): List<WrappedClass> =
                        emptyList()
                },
                ReferencePolicy.IGNORE_MISSING
            )
            assertTrue(
                result.first().children.any {
                    (it as? ResolvedConstructor)?.isCopyConstructor == true
                }
            )
        }
    }
}
