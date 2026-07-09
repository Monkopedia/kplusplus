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
import clang.tooling.buildASTFromCode
import kotlinx.cinterop.memScoped
import kotlin.system.exitProcess

// One walked top-level declaration, collected from the AST so the walk can be ASSERTED as a
// whole rather than printed. methods is (name -> return-type-spelling); bases is the list of
// base-class spellings (CXXBaseSpecifier::getType().getAsString()). For non-records both are
// empty; for a record both come from the SAME owning class (#40's cumulative-forcing fix).
private data class WalkedDecl(
    val kind: String,
    val name: String,
    val typeStr: String,
    val bases: List<String>,
    val methods: List<Pair<String, String>>,
)

// Mirror of krapper_parse's self-check helper (KrapperParse.kt:check): record every assertion,
// print PASS/FAIL with a message, and let main() exit non-zero if ANY failed — so
// `:clangwalk:runReleaseExecutableKlinker` is self-verifying instead of a println demo.
private var failures = 0

private fun check(name: String, pass: Boolean, detail: String = "") {
    if (!pass) failures++
    println("  [${if (pass) "PASS" else "FAIL"}] $name${if (detail.isEmpty()) "" else " — $detail"}")
}

// Stage1 self-bootstrap: parse C++ into a real Clang AST and walk it — entirely on the
// kplusplus-generated bindings of Clang's own C++ AST API (no libclang). This is the proof
// that kplusplus can host the very frontend it was built with.
fun main(args: Array<String>) = memScoped {
    val source = """
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

    // Entry point: clang::tooling::buildASTFromCode (unique_ptr<ASTUnit> -> owned ASTUnit).
    val unit = buildASTFromCode(source, "input.cc")
        ?: return@memScoped println("clangwalk: buildASTFromCode returned null")
    val context = unit.getASTContext()
        ?: return@memScoped println("clangwalk: no ASTContext")
    val tu = context.getTranslationUnitDecl()
        ?: return@memScoped println("clangwalk: no TranslationUnitDecl")

    println("clangwalk: top-level declarations of the parsed TU")
    val walked = mutableListOf<WalkedDecl>()
    // TranslationUnitDecl is-a DeclContext (upcast); decls() materializes the
    // llvm::iterator_range<decl_iterator> into an iterable std::vector<clang::Decl*>.
    for (decl in tu.asDeclContext().decls()) {
        if (decl == null) continue
        val kind = decl.getDeclKindName() ?: "?"
        // A NamedDecl has a name; down-cast (llvm::dyn_cast) to read it. getNameAsString()
        // returns std::string by value — under IGNORE_MISSING that return only binds because
        // #37 normalizes the canonical basic_string<char> spelling to a Kotlin String.
        val name = decl.asNamedDecl()?.getNameAsString() ?: "<unnamed>"
        // A ValueDecl (var / function) carries a QualType; QualType::getAsString() is the
        // second by-value std::string EXTRACT primitive — the type spelling as Clang reports.
        val typeStr = decl.asValueDecl()?.getType()?.getAsString() ?: ""

        // TRAVERSE: for a record, descend into its members. dyn_cast Decl -> CXXRecordDecl
        // (null for non-records). #40's cumulative-forcing fix is what lets BOTH range
        // accessors survive together: bases() materializes std::vector<CXXBaseSpecifier*>
        // and methods() materializes std::vector<CXXMethodDecl*> off the SAME owning class.
        val record = decl.asCXXRecordDecl()
        val bases = mutableListOf<String>()
        val methods = mutableListOf<Pair<String, String>>()
        if (record != null) {
            // Each CXXBaseSpecifier carries the base class as a QualType (getType()); its
            // getAsString() spells the base — the inheritance edge the reducer flattens over.
            for (base in record.bases()) {
                if (base == null) continue
                bases += base.getType().getAsString() ?: "?"
            }
            // Each CXXMethodDecl: its name comes from NamedDecl (upcast getNameAsString) and
            // its return type from FunctionDecl (upcast getReturnType -> QualType ->
            // getAsString). This is the reducer's core per-record operation: enumerate
            // methods, extract each signature's name + return type.
            for (method in record.methods()) {
                if (method == null) continue
                val mName = method.asNamedDecl()?.getNameAsString() ?: "<unnamed>"
                val mRet = method.asFunctionDecl()?.getReturnType()?.getAsString() ?: "?"
                methods += mName to mRet
            }
        }
        walked += WalkedDecl(kind, name, typeStr, bases, methods)
        println(
            "  [$kind] $name${if (typeStr.isEmpty()) "" else " : $typeStr"}" +
                bases.joinToString("") { "\n      <base> $it" } +
                methods.joinToString("") { "\n      <method> ${it.first} : ${it.second}" },
        )
    }

    // ---- Assertions: the walk is now a TEST, not a println. Every expected kind/name/type/
    // base/method spelling below is the EXACT Clang getAsString()/getDeclKindName() oracle
    // captured by running the walk — not a guess. Any mismatch fails loudly (exit non-zero).
    println("\nclangwalk: self-checks")

    // The parsed TU is preceded by 5 implicit compiler-builtin typedefs that Clang injects
    // into every TU (int128 helpers, the ObjC constant-string stub, the va_list builtins),
    // then the 5 user decls. Assert the REAL observed set — count + ordered (kind,name).
    val expectedKindNames = listOf(
        "Typedef" to "__int128_t",
        "Typedef" to "__uint128_t",
        "Typedef" to "__NSConstantString",
        "Typedef" to "__builtin_ms_va_list",
        "Typedef" to "__builtin_va_list",
        "CXXRecord" to "Point",
        "CXXRecord" to "Shape",
        "CXXRecord" to "Circle",
        "Function" to "freeFunction",
        "Var" to "globalVar",
    )
    check("walked exactly 10 top-level decls", walked.size == 10, "got ${walked.size}")
    check(
        "top-level (kind,name) sequence matches the parsed TU",
        walked.map { it.kind to it.name } == expectedKindNames,
        walked.map { "${it.kind}:${it.name}" }.toString(),
    )

    val byName = walked.associateBy { it.name }
    fun decl(name: String): WalkedDecl =
        byName[name] ?: WalkedDecl("<absent>", name, "", emptyList(), emptyList())

    // Point: a record of fields x/y only. Fields aren't walked and it has no bases, so BOTH
    // range accessors are empty — the degenerate TRAVERSE case.
    val point = decl("Point")
    check("Point is a CXXRecord", point.kind == "CXXRecord", point.kind)
    check("Point has 0 bases", point.bases.isEmpty(), point.bases.toString())
    check("Point has 0 methods", point.methods.isEmpty(), point.methods.toString())

    // Shape: 0 bases, exactly the 3 declared methods with their EXACT return-type spellings
    // (note `const char *` — getAsString's canonical pointer spelling, the #37 EXTRACT payoff).
    val shape = decl("Shape")
    check("Shape is a CXXRecord", shape.kind == "CXXRecord", shape.kind)
    check("Shape has 0 bases", shape.bases.isEmpty(), shape.bases.toString())
    check(
        "Shape methods == {area:int, name:const char *, scale:double}",
        shape.methods == listOf("area" to "int", "name" to "const char *", "scale" to "double"),
        shape.methods.toString(),
    )

    // Circle: the inheritance edge — base spelled exactly `Shape` — plus its one method.
    val circle = decl("Circle")
    check("Circle is a CXXRecord", circle.kind == "CXXRecord", circle.kind)
    check("Circle bases == {Shape}", circle.bases == listOf("Shape"), circle.bases.toString())
    check(
        "Circle methods == {radius:double}",
        circle.methods == listOf("radius" to "double"),
        circle.methods.toString(),
    )

    // freeFunction: a Function decl whose ValueDecl type is the full function-type spelling.
    val freeFunction = decl("freeFunction")
    check("freeFunction is a Function", freeFunction.kind == "Function", freeFunction.kind)
    check(
        "freeFunction type spelling == `void (int)`",
        freeFunction.typeStr == "void (int)",
        freeFunction.typeStr,
    )

    // globalVar: a Var decl whose type spelling is the bare `int`.
    val globalVar = decl("globalVar")
    check("globalVar is a Var", globalVar.kind == "Var", globalVar.kind)
    check("globalVar type spelling == `int`", globalVar.typeStr == "int", globalVar.typeStr)

    if (failures > 0) {
        println("clangwalk: $failures self-check(s) FAILED")
        exitProcess(1)
    }
    println("clangwalk: all self-checks passed — walked ${walked.size} top-level decls via real libclang-cpp")
}
