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
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.builders.LogPolicy
import com.monkopedia.krapper.generator.codegen.CppWriter
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.HeaderWriter
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.resolvedmodel.ArgumentCastMode
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.runBlocking
import platform.posix.random

// Wave-B return shapes (clang-bootstrap T1.10 / T1.6 / G8). Each parses a tiny
// header, binds a single probe class, and asserts the probe method SURVIVES
// resolution (recon-2: these return shapes silently dropped with "Couldn't
// resolve return").
class ReturnShapeTest {

    private fun methodsOf(header: String, className: String): List<ResolvedMethod> =
        classOf(header, className).children.filterIsInstance<ResolvedMethod>()

    private fun classOf(header: String, className: String): ResolvedClass = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val tmpFile = "/tmp/krapper_retshape_${random()}_${random()}.h"
            File(tmpFile).writeText(header)
            val resolver = parseHeader(index, listOf(tmpFile), generateIncludes("clang++"))
            val allow = AllowListFilter(listOf(className))
            val found = resolver.findClasses(allow.wrapperFilter())
            val resolved = found.resolveAll(resolver, ReferencePolicy.INCLUDE_MISSING)
            resolved.filterIsInstance<ResolvedClass>().first { it.type.type == className }
        }
    }

    // Resolve [className] under a SCOPED import (only [className] is allowlisted) with the
    // IGNORE_MISSING policy — the clangwalk self-host harness's exact configuration. Unlike
    // [classOf] (INCLUDE_MISSING, which pulls std::basic_string in as a wrapped class), here
    // basic_string is a missing reference, so the GAP B normalization is what keeps a
    // std::string-by-value return alive (as a STRING boundary) instead of dropping it.
    private fun scopedMethodsOf(header: String, className: String): List<ResolvedMethod> =
        memScoped {
            runBlocking {
                val index = createIndex(0, 0) ?: error("Failed to create Index")
                defer { index.dispose() }
                val tmpFile = "/tmp/krapper_retshape_scoped_${random()}_${random()}.h"
                File(tmpFile).writeText(header)
                val resolver = parseHeader(index, listOf(tmpFile), generateIncludes("clang++"))
                val found = resolver.findClasses(AllowListFilter(listOf(className)).wrapperFilter())
                found.resolveAll(resolver, ReferencePolicy.IGNORE_MISSING)
                    .filterIsInstance<ResolvedClass>()
                    .first { it.type.type == className }
                    .children.filterIsInstance<ResolvedMethod>()
            }
        }

    // Emit the C++ wrapper body for one method of a resolved class (the same CppWriter path
    // the real generator runs) so a test can assert the emitted C boundary code.
    private fun emittedCpp(cls: ResolvedClass, method: ResolvedMethod): String = runBlocking {
        val code = CppCodeBuilder()
        with(CppWriter(File("/tmp/krapper_retshape_out.cpp"), code)) {
            code.onGenerate(cls, method)
        }
        code.toString()
    }

    // T1.10: `std::string name() const { return "x"; }` — std::string returned by
    // value (the existing featuregen StringFeature.produce path).
    @Test
    fun stringByValueReturnBinds() {
        val methods = methodsOf(
            """
            |#include <string>
            |struct StrProbe {
            |    std::string name() const { return "x"; }
            |};
            """.trimMargin(),
            "StrProbe"
        )
        assertTrue(
            methods.any { it.name == "name" },
            "std::string-by-value method `name` must bind; got ${methods.map { it.name }}"
        )
    }

    // T1.10 / G8: `const std::string name() const` — the SAME by-value return but
    // with a (meaningless) top-level const on the returned temporary. recon-2:
    // this dropped on Clang's getNameAsString()/QualType::getAsString().
    @Test
    fun constStringByValueReturnBinds() {
        val methods = methodsOf(
            """
            |#include <string>
            |struct ConstStrProbe {
            |    const std::string name() const { return "x"; }
            |};
            """.trimMargin(),
            "ConstStrProbe"
        )
        assertTrue(
            methods.any { it.name == "name" },
            "const std::string-by-value method `name` must bind; got ${methods.map { it.name }}"
        )
    }

    // GAP B (self-bootstrap front-end): under a SCOPED import (`only(...)`) + IGNORE_MISSING
    // — the clangwalk harness config — a `std::string`-by-value return surfaces under its
    // canonical template spelling `std::__cxx11::basic_string<char>`, whose basic_string
    // class is NOT allowlisted. Before the fix the whole method was silently DROPPED as a
    // missing dependency (this is the wall for Clang's `QualType::getAsString` /
    // `NamedDecl::getNameAsString`). The normalization rewrites the unbound basic_string<char>
    // to `std::string`, so the method SURVIVES and surfaces as Kotlin String. Covers the
    // by-value and the top-level-`const` by-value forms (both real on the Clang surface).
    @Test
    fun scopedStringByValueReturnBindsAsString() {
        val methods = scopedMethodsOf(
            """
            |#include <string>
            |struct ScopedStrProbe {
            |    std::string getNameAsString() const { return "x"; }
            |    const std::string getAsString() const { return "y"; }
            |    int id() const { return 1; }
            |};
            """.trimMargin(),
            "ScopedStrProbe"
        )
        for (name in listOf("getNameAsString", "getAsString")) {
            val method = methods.firstOrNull { it.name == name }
            assertTrue(
                method != null,
                "scoped std::string-by-value method `$name` must survive IGNORE_MISSING; " +
                    "got ${methods.map { it.name }}"
            )
            assertTrue(
                method.returnType.kotlinType.fullyQualified.startsWith("kotlin.String"),
                "`$name` must surface as Kotlin String; " +
                    "got ${method.returnType.kotlinType.fullyQualified}"
            )
        }
        // Sanity: a sibling plain method binds too (the class itself isn't the problem).
        assertTrue(
            methods.any { it.name == "id" },
            "sibling plain method `id` must bind; got ${methods.map { it.name }}"
        )
    }

    // GAP B regression: the normalization must NOT reach a basic_string<char> behind a POINTER.
    // A `std::string*` out-param (the real `clang::Decl::getAvailability(std::string*)` shape)
    // can't ride the STRING -> `const char*` boundary: the cast-var is declared with the
    // pointer-typed targetType, so normalizing it emitted `std::string* x = std::string(arg)`,
    // which does NOT compile and aborted the whole clangwalk wrapper build. The pointer-carried
    // method must DROP cleanly (the pre-GAP-B behavior), while a by-value and a `const&` string
    // on the same class still survive (those marshal through a value-typed cast-var).
    @Test
    fun scopedStringPointerParamDropsNotCrashes() {
        val methods = scopedMethodsOf(
            """
            |#include <string>
            |struct PtrStrProbe {
            |    std::string getName() const { return "x"; }
            |    int byConstRef(const std::string& s) const { return (int) s.size(); }
            |    bool isDeprecated(std::string* message) const { return message != nullptr; }
            |    int id() const { return 1; }
            |};
            """.trimMargin(),
            "PtrStrProbe"
        )
        // The std::string* out-param method must drop (it can't marshal a pointer-to-string).
        assertTrue(
            methods.none { it.name == "isDeprecated" },
            "a std::string* out-param method must DROP, not bind with broken codegen; " +
                "got ${methods.map { it.name }}"
        )
        // ...but a `const std::string&` IN param still marshals from Kotlin String.
        assertTrue(
            methods.any { it.name == "byConstRef" },
            "a const std::string& in-param method must still bind; got ${methods.map { it.name }}"
        )
        // ...while the by-value std::string return and the sibling plain method still bind.
        val getName = methods.firstOrNull { it.name == "getName" }
        val getNameType = getName?.returnType?.kotlinType?.fullyQualified
        assertTrue(
            getNameType?.startsWith("kotlin.String") == true,
            "by-value std::string return `getName` must still survive as Kotlin String; " +
                "got ${methods.map { it.name }}"
        )
        assertTrue(
            methods.any { it.name == "id" },
            "sibling plain method `id` must bind; got ${methods.map { it.name }}"
        )
    }

    // T1.6 / G8: a trivially-copyable value type returned by value, AND with a
    // top-level const. Both must bind (the const-by-value one must not get a
    // spurious `const T` in its emitted C type).
    @Test
    fun constValueByValueReturnBinds() {
        val methods = methodsOf(
            """
            |struct Val { int x; };
            |struct ValProbe {
            |    Val make() const { return Val{1}; }
            |    const Val makeConst() const { return Val{2}; }
            |};
            """.trimMargin(),
            "ValProbe"
        )
        assertTrue(
            methods.any { it.name == "make" },
            "value-by-value method `make` must bind; got ${methods.map { it.name }}"
        )
        assertTrue(
            methods.any { it.name == "makeConst" },
            "const-value-by-value method `makeConst` must bind; got ${methods.map { it.name }}"
        )
    }

    // T1.10: a non-owning string view (`llvm::StringRef`-shaped, matched by qualified
    // name) returned by a method must bind — the return is rewritten to std::string and
    // CppWriter emits `.str()`. The method surfaces (not dropped) and its Kotlin type is
    // String. (Mirrors the real llvm::StringRef shape; LLVM-header validation is the
    // separate real-Clang compile check.)
    @Test
    fun stringRefViewReturnBindsAsString() {
        val methods = methodsOf(
            """
            |#include <string>
            |#include <cstddef>
            |namespace llvm {
            |class StringRef {
            |    const char* data_;
            |    size_t size_;
            |public:
            |    StringRef() : data_(nullptr), size_(0) {}
            |    StringRef(const char* d, size_t s) : data_(d), size_(s) {}
            |    std::string str() const { return std::string(data_, size_); }
            |};
            |}
            |struct ViewProbe {
            |    llvm::StringRef getName() const { return llvm::StringRef("x", 1); }
            |};
            """.trimMargin(),
            "ViewProbe"
        )
        val getName = methods.firstOrNull { it.name == "getName" }
        assertTrue(
            getName != null,
            "StringRef-returning method `getName` must bind; got ${methods.map { it.name }}"
        )
        assertTrue(
            getName.returnType.kotlinType.fullyQualified.startsWith("kotlin.String"),
            "getName must surface as Kotlin String; got ${getName.returnType.kotlinType.fullyQualified}"
        )
    }

    // T1.10p: an inbound `llvm::StringRef` PARAMETER (the inverse of T1.10's StringRef
    // RETURN). It must marshal a Kotlin `String` -> `const char*` at the C boundary, then
    // construct the view from it (`llvm::StringRef arg_cast = llvm::StringRef(arg)`) before
    // calling through — NOT REINT_CAST the String as an opaque pointer. Asserts the arg
    // surfaces as Kotlin String with STRING_VIEW cast mode, and the emitted C++ constructs
    // the StringRef from the char* argument.
    @Test
    fun stringRefParamMarshalsFromString() {
        val cls = classOf(
            """
            |#include <string>
            |#include <cstddef>
            |namespace llvm {
            |class StringRef {
            |    const char* data_;
            |    size_t size_;
            |public:
            |    StringRef() : data_(nullptr), size_(0) {}
            |    StringRef(const char* d) : data_(d), size_(0) {}
            |    StringRef(const char* d, size_t s) : data_(d), size_(s) {}
            |    size_t size() const { return size_; }
            |};
            |}
            |struct SinkProbe {
            |    int take(llvm::StringRef s) const { return (int) s.size(); }
            |};
            """.trimMargin(),
            "SinkProbe"
        )
        val take = cls.children.filterIsInstance<ResolvedMethod>().firstOrNull { it.name == "take" }
        assertTrue(take != null, "StringRef-param method `take` must bind")
        val arg = take.args.first { it.name != "thiz" }
        assertTrue(
            arg.castMode == ArgumentCastMode.STRING_VIEW,
            "the StringRef param must use STRING_VIEW cast mode; got ${arg.castMode}"
        )
        assertTrue(
            arg.signatureType.kotlinType.fullyQualified.startsWith("kotlin.String"),
            "the param must surface as Kotlin String; got " +
                arg.signatureType.kotlinType.fullyQualified
        )
        assertTrue(
            arg.signatureType.cType.typeString.contains("char"),
            "the C boundary param must be a char*; got ${arg.signatureType.cType.typeString}"
        )
        val cpp = emittedCpp(cls, take)
        assertTrue(
            cpp.contains("llvm::StringRef(s)") || cpp.contains("llvm::StringRef( s )"),
            "emitted wrapper must construct the view from the char* arg; got:\n$cpp"
        )
    }

    // T1.7e: a by-value `std::unique_ptr<T>` return is move-only, so the by-value Holder
    // placement-new path can't carry it and the method would silently drop. The return is
    // rewritten to a raw `T*` and CppWriter emits `.release()`. The method must bind and
    // surface a (nullable) pointer wrapper to T — the same shape as a raw `T*` factory.
    @Test
    fun uniquePtrReturnBindsAsRawPointer() {
        val methods = methodsOf(
            """
            |#include <memory>
            |struct Owned { int id; int getId() const { return id; } };
            |struct UpProbe {
            |    std::unique_ptr<Owned> make() const {
            |        return std::unique_ptr<Owned>(new Owned{5});
            |    }
            |};
            """.trimMargin(),
            "UpProbe"
        )
        val make = methods.firstOrNull { it.name == "make" }
        assertTrue(
            make != null,
            "unique_ptr<Owned>-returning method `make` must bind; got ${methods.map { it.name }}"
        )
        assertTrue(
            make.returnViaMemberCall == "release",
            "make must release ownership via .release(); got ${make.returnViaMemberCall}"
        )
        assertTrue(
            make.returnType.typeString.trimEnd('*').endsWith("Owned") &&
                make.returnType.typeString.endsWith("*"),
            "make must return a raw `Owned*`; got ${make.returnType.typeString}"
        )
    }

    // T1.3: a method returning `llvm::iterator_range<It>` (here mimicking the real Clang
    // shape: a class iterator whose `*it` is a `Thing*`) must bind — the return is
    // materialized into a `std::vector<Thing*>` and `rangeElementType` records the element
    // class. recon-2: decls()/methods()/fields()/bases() all dropped with "Couldn't
    // resolve return"; this is the generic reproduction.
    @Test
    fun iteratorRangeReturnMaterializesVector() {
        val methods = methodsOf(
            """
            |#include <vector>
            |namespace llvm {
            |template <class It> class iterator_range {
            |    It b_, e_;
            |public:
            |    iterator_range(It b, It e) : b_(b), e_(e) {}
            |    It begin() const { return b_; }
            |    It end() const { return e_; }
            |};
            |}
            |struct Thing { int x; };
            |struct ThingIter {
            |    typedef Thing* value_type;
            |    Thing** p_;
            |    Thing* operator*() const { return *p_; }
            |    ThingIter& operator++() { ++p_; return *this; }
            |    bool operator!=(const ThingIter& o) const { return p_ != o.p_; }
            |};
            |struct RangeHolder {
            |    Thing** data_;
            |    unsigned n_;
            |    llvm::iterator_range<ThingIter> items() const {
            |        return llvm::iterator_range<ThingIter>(ThingIter{data_}, ThingIter{data_ + n_});
            |    }
            |};
            """.trimMargin(),
            "RangeHolder"
        )
        val items = methods.firstOrNull { it.name == "items" }
        assertTrue(
            items != null,
            "iterator_range-returning method `items` must bind; got ${methods.map { it.name }}"
        )
        assertTrue(
            items.rangeElementType == "Thing",
            "items must record element type Thing; got ${items.rangeElementType}"
        )
        assertTrue(
            items.returnType.typeString.contains("Vector") ||
                items.returnType.typeString.contains("vector"),
            "items must return a materialized vector; got ${items.returnType.typeString}"
        )
    }

    // #7 item 5: a method returning `llvm::iterator_range<It>` whose element type CAN'T be
    // recovered from `It` (a plain class iterator with NO `value_type` typedef and no
    // template argument — none of elementOfIterator's pointer/value_type/template-arg shapes
    // match) is DROPPED from range materialization. The drop DECISION is unchanged, but it
    // must now be DISCOVERABLE: elementOfIterator records a PARSE DropLedger entry instead of
    // returning null silently. Asserts the ledger captured the drop with a reason, the
    // dropped method carries no rangeElementType, and a sibling plain method still binds.
    @Test
    fun unextractableIteratorRangeRecordsDrop() {
        DropLedger.reset()
        val methods = methodsOf(
            """
            |namespace llvm {
            |template <class It> class iterator_range {
            |    It b_, e_;
            |public:
            |    iterator_range(It b, It e) : b_(b), e_(e) {}
            |    It begin() const { return b_; }
            |    It end() const { return e_; }
            |};
            |}
            |struct Thing { int x; };
            |// A plain (non-template) iterator class with NO value_type typedef: none of
            |// elementOfIterator's shapes (pointer / value_type / first-template-arg) apply,
            |// so the element can't be recovered.
            |struct OpaqueIter {
            |    Thing* p_;
            |    Thing& operator*() const { return *p_; }
            |    OpaqueIter& operator++() { ++p_; return *this; }
            |    bool operator!=(const OpaqueIter& o) const { return p_ != o.p_; }
            |};
            |struct OpaqueRangeHolder {
            |    OpaqueIter b_, e_;
            |    llvm::iterator_range<OpaqueIter> things() const {
            |        return llvm::iterator_range<OpaqueIter>(b_, e_);
            |    }
            |    int count() const { return 0; }
            |};
            """.trimMargin(),
            "OpaqueRangeHolder"
        )
        // The drop is recorded (discoverable), with a non-empty reason, in the PARSE phase.
        val rangeDrops = DropLedger.drops.filter {
            it.phase == DropPhase.PARSE && it.reason.contains("iterator_range")
        }
        assertTrue(
            rangeDrops.isNotEmpty(),
            "an unextractable iterator_range must record a PARSE DropLedger entry; " +
                "ledger was ${DropLedger.drops}"
        )
        assertTrue(
            rangeDrops.all { it.reason.isNotBlank() },
            "the drop record must carry a reason; got ${rangeDrops.map { it.reason }}"
        )
        // The range method got no element type (the drop DECISION is unchanged)...
        val things = methods.firstOrNull { it.name == "things" }
        assertTrue(
            things == null || things.rangeElementType == null,
            "the unextractable range method must not record an element type; " +
                "got ${things?.rangeElementType}"
        )
        // ...but the rest of the class still binds.
        assertTrue(
            methods.any { it.name == "count" },
            "the sibling plain method `count` must still bind; got ${methods.map { it.name }}"
        )
    }

    // A `const value_type&` arg where value_type is a POINTER (the std::vector<Thing*>
    // push_back/assign/fill-ctor shape the range materialization needs) must resolve to a
    // plain `Thing*`, NOT `const Thing*`. The top-level const on a by-value pointer is
    // meaningless and was rendering as pointer-to-const (`const Thing*`), which fails to
    // match the real `Thing* const&` signature.
    @Test
    fun constRefToPointerArgDropsSpuriousConst() {
        val methods = methodsOf(
            """
            |struct Thing { int id; };
            |struct PtrArgProbe {
            |    typedef Thing* value_type;
            |    void take(const value_type& x) { (void)x; }
            |};
            """.trimMargin(),
            "PtrArgProbe"
        )
        val take = methods.firstOrNull { it.name == "take" }
        assertTrue(take != null, "take must bind; got ${methods.map { it.name }}")
        val arg = take.args.first { it.name != "thiz" }
        assertTrue(
            arg.type.typeString == "Thing*",
            "const value_type& (value_type=Thing*) must resolve to `Thing*`, not " +
                "`${arg.type.typeString}`"
        )
    }

    // T-forcing-scope-2: a forced container instantiation (`--instantiate
    // "std::vector<Thing*>"`) must BIND even when the MAIN reference policy is
    // IGNORE_MISSING. This is the real-Clang demo path: INCLUDE_MISSING explodes the
    // whole library, so the main resolve runs under IGNORE_MISSING — but under IGNORE a
    // `std::` container specialization is dropped (defaultFilter excludes std:: classes,
    // so the spec is never in tracker.classes, and IGNORE only keeps already-tracked
    // types). `resolveForcing` fixes this with a 3-pass shared-tracker resolve: pass 2
    // forces the container under INCLUDE_MISSING semantics regardless of the main policy.
    // This test reproduces the IndexedServiceImpl.requestInstantiation flow at unit scale
    // and asserts the container binds (BEFORE the fix: "Forcing introduced 0 new").
    @Test
    fun forcedContainerBindsUnderIgnoreMissing() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val header =
                """
                |struct Thing { int id; };
                |struct RangeHolder { Thing* p_; };
                """.trimMargin()
            val headerFile = "/tmp/krapper_force2_${random()}_${random()}.h"
            File(headerFile).writeText(header)

            // Main resolve under IGNORE_MISSING (the real-Clang policy that hides the bug).
            val mainResolver =
                parseHeader(index, listOf(headerFile), generateIncludes("clang++"))
            val bound = mainResolver.findClasses(
                AllowListFilter(listOf("RangeHolder")).wrapperFilter()
            )
                .resolveAll(mainResolver, ReferencePolicy.IGNORE_MISSING)
            val alreadyBoundKeys =
                bound.filterIsInstance<ResolvedClass>().mapTo(HashSet()) { it.type.toString() }

            // Synthesize the forcing struct exactly as requestInstantiation does and
            // re-parse it (it #includes the consumer header + <vector>).
            val target = "std::vector<Thing*>"
            val forceName = "KrapperForce_std_vector_ThingPtr"
            val forceFile = "/tmp/krapper_force2_inst_${random()}.h"
            File(forceFile).writeText(
                """
                |#include <vector>
                |#include "$headerFile"
                |struct $forceName { $target value; };
                """.trimMargin()
            )
            val forceResolver =
                parseHeader(index, listOf(forceFile), generateIncludes("clang++"))
            // Scope to the forcing struct + already-bound classes (the requestInstantiation
            // scoping that kills the explosion).
            val scoped = forceResolver.findClasses { defaultFilter() }.filter {
                val cls = it as? WrappedClass ?: return@filter true
                val key = cls.type.toString()
                key.contains(forceName) || key in alreadyBoundKeys
            }
            val forced = scoped.resolveForcing(
                forceResolver,
                ReferencePolicy.IGNORE_MISSING,
                alreadyBoundKeys
            )
            val newSpecs = forced.filterIsInstance<ResolvedClass>()
                .map { it.type.toString() }
                .filter { it !in alreadyBoundKeys }
            assertTrue(
                newSpecs.any { it.contains("vector", ignoreCase = true) },
                "Forcing std::vector<Thing*> under IGNORE_MISSING must bind the container " +
                    "specialization; new specs were $newSpecs"
            )
        }
    }

    // TRAVERSE gap: when SEVERAL element vectors are force-instantiated over the SAME owning
    // class, ALL of that class's range accessors must survive — not just the last one. This is
    // the analog of Clang's `CXXRecordDecl` with `methods() -> vector<CXXMethodDecl*>`,
    // `bases() -> vector<CXXBaseSpecifier*>` and `decls() -> vector<Decl*>`: each request runs
    // its OWN resolveForcing with a fresh tracker holding only THAT request's container, so
    // pass 3 re-resolves the owning class against one container, recovers THAT range method and
    // re-drops the others — and the writer's last-wins dedup (KotlinWriter `associateBy` by
    // type) keeps whichever request ran last, silently losing the rest (`decls()` survived only
    // by sorting last). The cumulative `forcedContainerKeys` makes every prior container visible
    // to pass 3, so the recovery is COMPLETE and order-independent.
    //
    // Both containers are instantiated in a SINGLE forcing parse (two `KrapperForce_*` structs),
    // then resolveForcing is driven once PER container — mirroring two requestInstantiation
    // calls accumulating `classes` with the last-wins merge — to avoid a second `<vector>`
    // forcing TU (see ForcingCharacterizationTest's note on repeated-forcing-TU fragility).
    // PRE-FIX: only the last-forced accessor (`bitems`) survives; this asserts BOTH do.
    @Test
    fun multiContainerForcingRecoversAllRangeAccessorsUnderIgnoreMissing() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            // Owner has TWO iterator_range accessors over DIFFERENT element types; each
            // materializes to a distinct std::vector<X*> (the recon-2 range shape), so each
            // needs its own forced container.
            val header =
                """
                |#include <vector>
                |namespace llvm {
                |template <class It> class iterator_range {
                |    It b_, e_;
                |public:
                |    iterator_range(It b, It e) : b_(b), e_(e) {}
                |    It begin() const { return b_; }
                |    It end() const { return e_; }
                |};
                |}
                |struct Alpha { int x; };
                |struct Beta { int y; };
                |struct AlphaIter {
                |    typedef Alpha* value_type;
                |    Alpha** p_;
                |    Alpha* operator*() const { return *p_; }
                |    AlphaIter& operator++() { ++p_; return *this; }
                |    bool operator!=(const AlphaIter& o) const { return p_ != o.p_; }
                |};
                |struct BetaIter {
                |    typedef Beta* value_type;
                |    Beta** p_;
                |    Beta* operator*() const { return *p_; }
                |    BetaIter& operator++() { ++p_; return *this; }
                |    bool operator!=(const BetaIter& o) const { return p_ != o.p_; }
                |};
                |struct Owner {
                |    Alpha** adata_; unsigned an_;
                |    Beta** bdata_; unsigned bn_;
                |    llvm::iterator_range<AlphaIter> aitems() const {
                |        return llvm::iterator_range<AlphaIter>(AlphaIter{adata_}, AlphaIter{adata_ + an_});
                |    }
                |    llvm::iterator_range<BetaIter> bitems() const {
                |        return llvm::iterator_range<BetaIter>(BetaIter{bdata_}, BetaIter{bdata_ + bn_});
                |    }
                |};
                """.trimMargin()
            val headerFile = "/tmp/krapper_multiforce_${random()}_${random()}.h"
            File(headerFile).writeText(header)

            // Main resolve under IGNORE_MISSING (the real-Clang policy): both range accessors
            // drop, their materialized vector<X*> containers being unbound.
            val mainResolver =
                parseHeader(index, listOf(headerFile), generateIncludes("clang++"))
            var classes: List<ResolvedElement> =
                mainResolver.findClasses(AllowListFilter(listOf("Owner")).wrapperFilter())
                    .resolveAll(mainResolver, ReferencePolicy.IGNORE_MISSING)

            // ONE forcing parse with BOTH KrapperForce structs (libclang instantiates both
            // vector specs without a second <vector> TU).
            val forceFile = "/tmp/krapper_multiforce_inst_${random()}.h"
            File(forceFile).writeText(
                """
                |#include <vector>
                |#include "$headerFile"
                |struct KrapperForce_Alpha { std::vector<Alpha*> value; };
                |struct KrapperForce_Beta { std::vector<Beta*> value; };
                """.trimMargin()
            )
            val forceResolver =
                parseHeader(index, listOf(forceFile), generateIncludes("clang++"))

            // Drive resolveForcing once per container, accumulating `classes` (append +
            // last-wins) and SHARING the cumulative forcedContainerKeys set across calls —
            // exactly what IndexedServiceImpl.requestInstantiation does across requests.
            val forcedKeys = mutableSetOf<String>()
            for (forceName in listOf("KrapperForce_Alpha", "KrapperForce_Beta")) {
                val alreadyBound = classes.filterIsInstance<ResolvedClass>()
                    .mapTo(HashSet()) { it.type.toString() }
                val scoped = forceResolver.findClasses { defaultFilter() }.filter {
                    val cls = it as? WrappedClass ?: return@filter true
                    val key = cls.type.toString()
                    key.contains(forceName) || key in alreadyBound
                }
                classes = classes + scoped.resolveForcing(
                    forceResolver,
                    ReferencePolicy.IGNORE_MISSING,
                    alreadyBound,
                    forcedKeys
                )
            }

            // The writer dedups resolved classes last-wins (KotlinWriter associateBy type), so
            // assert against the LAST surviving Owner — the copy that actually reaches codegen.
            val owner = classes.filterIsInstance<ResolvedClass>()
                .associateBy { it.type.toString() }
                .values.first { it.type.type == "Owner" }
            val accessors = owner.children.filterIsInstance<ResolvedMethod>().map { it.name }
            assertTrue(
                "aitems" in accessors && "bitems" in accessors,
                "BOTH range accessors must survive after forcing both element containers; " +
                    "got $accessors (pre-fix only the last-forced `bitems` survives)"
            )
        }
    }

    // #57: synthetic sizeOf/alignOf must be IDEMPOTENT across re-resolutions. Each
    // requestInstantiation re-resolves every already-bound class (resolveForcing pass 1 +
    // pass 3), and `modifyMethodsIfNeeded` mutates the SHARED WrappedClass — before the fix
    // it appended a FRESH sizeOf/alignOf pair on every resolve, which the per-pass
    // NameHandler `_`-uniquified instead of deduping (timespec_size_of emitted 324 times in
    // featuregen's krapped output, up to 34 leading underscores). Drive resolveForcing twice
    // over the same bound class (mirroring two requestInstantiation calls) and assert the
    // last-wins copy carries exactly ONE pair with the CLEAN names.
    @Test
    fun repeatedForcingDoesNotDuplicateSyntheticMembers() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val header =
                """
                |struct Thing { int id; };
                |struct Holder { Thing* p_; };
                """.trimMargin()
            val headerFile = "/tmp/krapper_synth_${random()}_${random()}.h"
            File(headerFile).writeText(header)

            val mainResolver =
                parseHeader(index, listOf(headerFile), generateIncludes("clang++"))
            var classes: List<ResolvedElement> =
                mainResolver.findClasses(AllowListFilter(listOf("Holder")).wrapperFilter())
                    .resolveAll(mainResolver, ReferencePolicy.IGNORE_MISSING)

            val forceFile = "/tmp/krapper_synth_inst_${random()}.h"
            File(forceFile).writeText(
                """
                |#include <vector>
                |#include "$headerFile"
                |struct KrapperForce_Thing { std::vector<Thing*> value; };
                """.trimMargin()
            )
            val forceResolver =
                parseHeader(index, listOf(forceFile), generateIncludes("clang++"))

            // Two rounds over the SAME forcing TU — each re-resolves the bound Holder twice
            // (pass 1 + pass 3), so pre-fix the final copy carries 5 sizeOf children.
            val forcedKeys = mutableSetOf<String>()
            repeat(2) {
                val alreadyBound = classes.filterIsInstance<ResolvedClass>()
                    .mapTo(HashSet()) { it.type.toString() }
                val scoped = forceResolver.findClasses { defaultFilter() }.filter {
                    val cls = it as? WrappedClass ?: return@filter true
                    val key = cls.type.toString()
                    key.contains("KrapperForce_Thing") || key in alreadyBound
                }
                classes = classes + scoped.resolveForcing(
                    forceResolver,
                    ReferencePolicy.IGNORE_MISSING,
                    alreadyBound,
                    forcedKeys
                )
            }

            val holder = classes.filterIsInstance<ResolvedClass>()
                .associateBy { it.type.toString() }
                .values.first { it.type.type == "Holder" }
            val names = holder.children.filterIsInstance<ResolvedMethod>()
                .filter { it.methodType in listOf(MethodType.SIZE_OF, MethodType.ALIGN_OF) }
                .map { it.uniqueCName }
            assertTrue(
                names.sortedBy { it ?: "" } == listOf("Holder_align_of", "Holder_size_of"),
                "Re-resolution must keep exactly ONE clean-named sizeOf/alignOf pair; got $names"
            )
        }
    }

    // #60: per-instantiation SECTION duplication. Every requestInstantiation appends a
    // re-resolved copy of each already-bound class (the carrier of pass-3 recovery), and
    // the C++ writers iterated the RAW list — emitting a full wrapper section per copy
    // (header parse + 17 instantiations = 18 sections per class in featuregen's .h/.cc;
    // the .cc only compiled because Scope.allocateName renamed the colliding definitions
    // into dead `_`-prefixed variants). Drive resolveForcing twice over a bound class
    // whose range method is only recovered by pass 3 (so the copies genuinely differ),
    // dedup as requestInstantiation now does, and assert (a) one copy per type-key,
    // (b) the survivor is the LAST (recovered) copy, (c) HeaderWriter emits exactly one
    // section per class.
    @Test
    fun repeatedForcingEmitsOneSectionPerClass() = memScoped {
        runBlocking {
            val index = createIndex(0, 0) ?: error("Failed to create Index")
            defer { index.dispose() }
            val headerFile = "/tmp/krapper_dedup_${random()}_${random()}.h"
            File(headerFile).writeText(
                """
                |#include <vector>
                |struct Thing { int id; };
                |struct Holder {
                |    std::vector<Thing*> items() const { return {}; }
                |};
                """.trimMargin()
            )
            val mainResolver =
                parseHeader(index, listOf(headerFile), generateIncludes("clang++"))
            var classes: List<ResolvedElement> =
                mainResolver.findClasses(AllowListFilter(listOf("Holder")).wrapperFilter())
                    .resolveAll(mainResolver, ReferencePolicy.IGNORE_MISSING)

            val forceFile = "/tmp/krapper_dedup_inst_${random()}.h"
            File(forceFile).writeText(
                """
                |#include <vector>
                |#include "$headerFile"
                |struct KrapperForce_Holder { std::vector<Thing*> value; };
                """.trimMargin()
            )
            val forceResolver =
                parseHeader(index, listOf(forceFile), generateIncludes("clang++"))

            // Two rounds = two instantiation requests, each appending a re-resolved copy
            // of the bound Holder (pre-fix: 3 raw copies, 3 emitted sections).
            val forcedKeys = mutableSetOf<String>()
            repeat(2) {
                val alreadyBound = classes.filterIsInstance<ResolvedClass>()
                    .mapTo(HashSet()) { it.type.toString() }
                val scoped = forceResolver.findClasses { defaultFilter() }.filter {
                    val cls = it as? WrappedClass ?: return@filter true
                    val key = cls.type.toString()
                    key.contains("KrapperForce_Holder") || key in alreadyBound
                }
                classes = (
                    classes + scoped.resolveForcing(
                        forceResolver,
                        ReferencePolicy.IGNORE_MISSING,
                        alreadyBound,
                        forcedKeys
                    )
                    ).dedupClassesLastWins()
            }

            // (a) one copy per class type-key.
            val keys = classes.filterIsInstance<ResolvedClass>().map { it.type.toString() }
            assertTrue(
                keys.size == keys.distinct().size,
                "dedup must keep one copy per class type-key; got $keys"
            )
            // (b) LAST-wins: the main-resolve Holder dropped items() (the container was
            // unbound), so only the pass-3-recovered copy carries it.
            val holder =
                classes.filterIsInstance<ResolvedClass>().first { it.type.type == "Holder" }
            assertTrue(
                holder.children.filterIsInstance<ResolvedMethod>().any { it.name == "items" },
                "the surviving Holder must be the recovered (last) copy carrying items()"
            )
            // (c) writer-level: exactly one wrapper section per class.
            val builder = CppCodeBuilder()
            HeaderWriter(builder, policy = LogPolicy)
                .generate("dedup_probe", listOf(headerFile), classes)
            val sections = builder.toString().lines()
                .filter { it.contains("BEGIN KRAPPER GEN for") }
            assertTrue(
                sections.size == sections.distinct().size &&
                    sections.any { it.contains("for Holder") },
                "header must emit exactly one section per class; got $sections"
            )
        }
    }
}
