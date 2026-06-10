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
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import kotlin.system.exitProcess
import kotlinx.cinterop.memScoped
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// The brick-2 fixture "header": deliberately minimal (one free function + one plain class)
// — later bricks grow it, not this one. Fed to buildASTFromCode as an in-memory source,
// the same way :clangwalk feeds its fixture.
private val FIXTURE_HEADER = """
    void freeFunction(int);
    struct Shape {
        int area() const;
        const char* name() const;
    };
""".trimIndent()

private var failures = 0

private fun check(name: String, pass: Boolean, detail: String = "") {
    if (!pass) failures++
    println("  [${if (pass) "PASS" else "FAIL"}] $name${if (detail.isEmpty()) "" else " — $detail"}")
}

// #44 brick 2, the front-end scaffold: parse the fixture with Clang's C++ AST (on the
// kplusplus-generated libclang-cpp bindings), CONSTRUCT :krapper_model's WrappedTU from
// the walk, and serialize it to JSON — the end-to-end skeleton the next bricks fill in.
@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>): Unit = memScoped {
    val unit = buildASTFromCode(FIXTURE_HEADER, "fixture.h")
        ?: return@memScoped fail("buildASTFromCode returned null")
    val context = unit.getASTContext()
        ?: return@memScoped fail("no ASTContext")
    val tuDecl = context.getTranslationUnitDecl()
        ?: return@memScoped fail("no TranslationUnitDecl")

    val tu = buildWrappedTU(tuDecl)

    // ---- Structural summary ----
    println("cppfrontend: constructed WrappedTU from the Clang C++ AST")
    for (child in tu.children) {
        when (child) {
            is WrappedClass -> {
                println("  class ${child.name} (${child.children.size} members)")
                child.children.filterIsInstance<WrappedMethod>().forEach {
                    println("    $it${if (it.isConst) " const" else ""}")
                }
            }
            is WrappedMethod -> println("  free fn $child [${child.methodType}]")
            else -> println("  ${child::class.simpleName}")
        }
    }

    // ---- Serialize ----
    val json = Json { prettyPrint = true }.encodeToString(tu.serialized())
    println("\ncppfrontend: serialized WrappedTU")
    println(json)

    // ---- Self-checks ----
    println("\ncppfrontend: self-checks")
    val shape = tu.children.filterIsInstance<WrappedClass>().find { it.name == "Shape" }
    val methods = shape?.children?.filterIsInstance<WrappedMethod>().orEmpty()
    check("TU contains class Shape", shape != null)
    check("Shape has 2 methods", methods.size == 2, "got ${methods.size}")
    val area = methods.find { it.name == "area" }
    check(
        "area return type spelling is 'int'",
        area?.returnType?.toString() == "int",
        "got ${area?.returnType}"
    )
    check("area is const", area?.isConst == true)
    val name = methods.find { it.name == "name" }
    check(
        "name return type spelling is 'const char*'",
        name?.returnType?.toString() == "const char*",
        "got ${name?.returnType}"
    )
    val freeFn = tu.children.filterIsInstance<WrappedMethod>().find { it.name == "freeFunction" }
    check(
        "freeFunction is a TU-level STATIC method",
        freeFn != null && freeFn.methodType == MethodType.STATIC
    )
    check(
        "freeFunction has 1 arg of type 'int'",
        freeFn?.args?.singleOrNull()?.type?.toString() == "int",
        "got ${freeFn?.args}"
    )
    check(
        "freeFunction arg carries cpp:<canonical-id> identity",
        freeFn?.args?.firstOrNull()?.usr?.startsWith("cpp:") == true,
        "got '${freeFn?.args?.firstOrNull()?.usr}'"
    )
    check("JSON is non-empty", json.length > 2)

    if (failures > 0) fail("$failures self-check(s) failed")
    println("cppfrontend: ALL SELF-CHECKS PASSED")
}

private fun fail(message: String): Nothing {
    println("cppfrontend: FAIL — $message")
    exitProcess(1)
}
