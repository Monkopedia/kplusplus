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
import clang.Decl
import clang.tooling.buildASTFromCode
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toLong

// DECL-IDENTITY PROBE (clangwalk/decl-identity-probe):
// The reducer that the C++-AST front-end must replace keys its core maps (elementLookup,
// classMap, template-argument substitution) on libclang's USR string. The front-end needs an
// EQUIVALENT stable, hashable identity derived from a generated clang::Decl wrapper. This
// probe proves one exists.
//
// The identity primitive: normalize ANY decl wrapper to its CANONICAL clang::Decl* (Clang
// collapses every redeclaration of one entity — forward decls, the definition, decls reached
// via different paths — onto a single canonical Decl), THEN read an identity off it:
//   - idKey():  Decl::getID() — an ASTContext-assigned stable integer (Long). Type-agnostic:
//               it is stored once on the canonical Decl, so it is immune to the base-offset
//               pointer adjustment that multiple inheritance imposes on raw `this` pointers.
//   - ptrKey(): the raw canonical Decl* as a Long. Equally stable PROVIDED you always
//               normalize through asDecl()->Decl::getCanonicalDecl() so the static type (and
//               thus the subobject offset) is fixed to clang::Decl*.
//
// getCanonicalDecl() returns DeclApi? whose runtime type is the concrete Decl wrapper.
private fun Decl.canonical(): Decl = (getCanonicalDecl() as? Decl) ?: this
private fun Decl.idKey(): Long = canonical().getID()
private fun Decl.ptrKey(): Long = canonical().ptr.toLong()

fun main(args: Array<String>) = memScoped {
    // Fixture engineered to exercise USR-equivalence the way the reducer relies on it:
    //  * Point is FORWARD-DECLARED, then DEFINED  -> two distinct top-level Decl objects that
    //    MUST map to the same identity (the redeclaration-collapse property).
    //  * Shape/Circle methods let us reach the SAME CXXRecordDecl two ways: as a top-level
    //    walked decl, and via each method's getParent() -> the keys must match.
    val source = """
        struct Point;
        struct Point { int x; int y; };
        class Shape {
        public:
            int area() const;
            const char* name() const;
            double scale(double factor) const;
        };
        struct Circle : Shape { double radius() const; };
        void freeFunction(int n);
        int globalVar;
    """.trimIndent()

    val unit = buildASTFromCode(source, "input.cc")
        ?: return@memScoped println("clangwalk: buildASTFromCode returned null")
    val context = unit.getASTContext()
        ?: return@memScoped println("clangwalk: no ASTContext")
    val tu = context.getTranslationUnitDecl()
        ?: return@memScoped println("clangwalk: no TranslationUnitDecl")

    println("clangwalk: top-level declarations of the parsed TU (key = canonical id / ptr)")
    var count = 0
    // name -> set of distinct idKeys seen for that spelling. The forward-decl + definition of
    // Point must collapse to a SINGLE idKey here (proving same-entity-same-key).
    val idKeysByName = mutableMapOf<String, MutableSet<Long>>()
    var crossPathChecks = 0
    var crossPathFailures = 0

    for (decl in tu.asDeclContext().decls()) {
        if (decl == null) continue
        count++
        val kind = decl.getDeclKindName() ?: "?"
        val name = decl.asNamedDecl()?.getNameAsString() ?: "<unnamed>"
        val typeStr = decl.asValueDecl()?.getType()?.getAsString()?.let { " : $it" } ?: ""
        val id = decl.idKey()
        val ptr = decl.ptrKey()
        println("  [$kind] $name$typeStr  {id=$id ptr=0x${ptr.toString(16)}}")
        if (name != "<unnamed>") {
            idKeysByName.getOrPut(name) { mutableSetOf() }.add(id)
        }

        val record = decl.asCXXRecordDecl() ?: continue
        // The top-level record's canonical identity, reached via Decl::getCanonicalDecl().
        val recordId = decl.idKey()
        for (base in record.bases()) {
            if (base == null) continue
            println("      <base> ${base.getType().getAsString()}")
        }
        for (method in record.methods()) {
            if (method == null) continue
            val mName = method.asNamedDecl()?.getNameAsString() ?: "<unnamed>"
            val mRet = method.asFunctionDecl()?.getReturnType()?.getAsString() ?: "?"
            // CROSS-PATH: reach the owning record a DIFFERENT way (method -> getParent()),
            // normalize to canonical Decl*, and confirm the identity matches the top-level
            // record's. This is exactly the reducer's "two cursors, same entity" situation.
            val parentId = method.getParent()?.asDecl()?.idKey()
            val match = parentId == recordId
            crossPathChecks++
            if (!match) crossPathFailures++
            val flag = if (match) "OK same-entity" else "MISMATCH"
            println("      <method> $mName : $mRet  {parentId=$parentId vs recordId=$recordId -> $flag}")
        }
    }
    println("clangwalk: walked $count top-level declarations via real libclang-cpp")

    // -------- PROBE VERDICT --------
    println("")
    println("== DECL-IDENTITY PROBE RESULTS ==")
    // (1) Redeclaration collapse: forward decl + definition share one identity.
    val pointKeys = idKeysByName["Point"]
    val redeclPass = pointKeys != null && pointKeys.size == 1
    println("redecl-collapse (Point fwd-decl + definition -> one id): " +
        "${if (redeclPass) "PASS" else "FAIL"}  keys=$pointKeys")
    // (2) Distinct entities -> distinct identities (every named top-level decl unique).
    val allIds = idKeysByName.values.flatten()
    val distinctPass = allIds.size == allIds.toSet().size
    println("distinct-entities-distinct-id: ${if (distinctPass) "PASS" else "FAIL"}  " +
        "(${allIds.size} named decls, ${allIds.toSet().size} distinct ids)")
    // (3) Cross-path: method.getParent() identity == top-level record identity.
    val crossPass = crossPathChecks > 0 && crossPathFailures == 0
    println("cross-path (method.parent == top-level record): ${if (crossPass) "PASS" else "FAIL"}  " +
        "($crossPathChecks checks, $crossPathFailures failures)")
    val verdict = redeclPass && distinctPass && crossPass
    println("VERDICT: ${if (verdict) "STABLE HASHABLE DECL IDENTITY AVAILABLE" else "PROBE FAILED"}")
}
