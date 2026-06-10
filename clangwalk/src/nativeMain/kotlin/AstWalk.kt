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
    var count = 0
    // TranslationUnitDecl is-a DeclContext (upcast); decls() materializes the
    // llvm::iterator_range<decl_iterator> into an iterable std::vector<clang::Decl*>.
    for (decl in tu.asDeclContext().decls()) {
        if (decl == null) continue
        count++
        val kind = decl.getDeclKindName() ?: "?"
        // A NamedDecl has a name; down-cast (llvm::dyn_cast) to read it. getNameAsString()
        // returns std::string by value — under IGNORE_MISSING that return only binds because
        // #37 normalizes the canonical basic_string<char> spelling to a Kotlin String.
        val name = decl.asNamedDecl()?.getNameAsString() ?: "<unnamed>"
        // A ValueDecl (var / function) carries a QualType; QualType::getAsString() is the
        // second by-value std::string EXTRACT primitive — the type spelling as Clang reports.
        val typeStr = decl.asValueDecl()?.getType()?.getAsString()?.let { " : $it" } ?: ""
        println("  [$kind] $name$typeStr")

        // TRAVERSE: for a record, descend into its members. dyn_cast Decl -> CXXRecordDecl
        // (null for non-records). #40's cumulative-forcing fix is what lets BOTH range
        // accessors survive together: bases() materializes std::vector<CXXBaseSpecifier*>
        // and methods() materializes std::vector<CXXMethodDecl*> off the SAME owning class.
        val record = decl.asCXXRecordDecl() ?: continue
        // Each CXXBaseSpecifier carries the base class as a QualType (getType()); its
        // getAsString() spells the base — the inheritance edge the reducer flattens over.
        for (base in record.bases()) {
            if (base == null) continue
            println("      <base> ${base.getType().getAsString()}")
        }
        // Each CXXMethodDecl: its name comes from NamedDecl (upcast getNameAsString) and its
        // return type from FunctionDecl (upcast getReturnType -> QualType -> getAsString).
        // This is the reducer's core per-record operation: enumerate methods, extract each
        // signature's name + return type.
        for (method in record.methods()) {
            if (method == null) continue
            val mName = method.asNamedDecl()?.getNameAsString() ?: "<unnamed>"
            val mRet = method.asFunctionDecl()?.getReturnType()?.getAsString() ?: "?"
            println("      <method> $mName : $mRet")
        }
    }
    println("clangwalk: walked $count top-level declarations via real libclang-cpp")
}
