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
import com.monkopedia.krapper.generator.model.WrappedBase
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedDestructor
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import kotlin.system.exitProcess
import kotlinx.cinterop.memScoped
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// The brick-3 fixture "header" (#44 construction depth): exercises the full decl/class/
// member construction contract — access filtering (public kept, private/protected filtered
// with metadata), ctors (default + copy) and a virtual dtor, a deleted copy assignment,
// a static method, public/private fields (incl. the const-field metadata rules), an
// abstract class, namespace nesting with block merging, and a derived class's bases.
private val FIXTURE_HEADER = """
    void freeFunction(int);

    struct Shape {
        Shape();
        Shape(const Shape& other);
        virtual ~Shape();
        Shape& operator=(const Shape& other) = delete;
        virtual int area() const = 0;
        const char* name() const;
        static int count();
        int id;
    private:
        int secret;
        const int cache;
    };

    namespace geo {
        int add(int a, int b);

        class Circle : public Shape {
        public:
            Circle(double r);
            int area() const override;
            double radius() const;
            double r;
            const int version;
        private:
            Circle();
            double cached;
        };
    }

    namespace geo {
        bool enabled();
    }
""".trimIndent()

private var failures = 0

private fun check(name: String, pass: Boolean, detail: String = "") {
    if (!pass) failures++
    println("  [${if (pass) "PASS" else "FAIL"}] $name${if (detail.isEmpty()) "" else " — $detail"}")
}

// #44 brick 3, construction depth: parse the fixture with Clang's C++ AST (on the
// kplusplus-generated libclang-cpp bindings), CONSTRUCT :krapper_model's WrappedTU
// mirroring ModelFactories' full decl/class/member contract, and self-check every
// constructed shape against the model.
@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>): Unit = memScoped {
    // The virtual filename must spell a C++ extension: buildASTFromCode infers the language
    // from it, and a ".h" parses as C (where `int area() const` is ill-formed and Shape is a
    // plain RecordDecl the CXXRecordDecl walk never sees).
    val unit = buildASTFromCode(FIXTURE_HEADER, "fixture.cc")
        ?: return@memScoped fail("buildASTFromCode returned null")
    val context = unit.getASTContext()
        ?: return@memScoped fail("no ASTContext")
    val tuDecl = context.getTranslationUnitDecl()
        ?: return@memScoped fail("no TranslationUnitDecl")

    val tu = buildWrappedTU(tuDecl)

    // ---- Structural summary ----
    println("cppfrontend: constructed WrappedTU from the Clang C++ AST")
    printElements(tu.children, indent = "  ")

    // ---- Serialize ----
    val json = Json { prettyPrint = true }.encodeToString(tu.serialized())
    println("\ncppfrontend: serialized WrappedTU")
    println(json)

    // ---- Self-checks ----
    println("\ncppfrontend: self-checks")

    // -- Shape: TU-level abstract class with ctors/dtor/methods/fields + metadata.
    val shape = tu.children.filterIsInstance<WrappedClass>().find { it.name == "Shape" }
    check("TU contains class Shape", shape != null)
    check("Shape is abstract (pure-virtual area)", shape?.isAbstract == true)

    val shapeMethods = shape?.children?.filterIsInstance<WrappedMethod>().orEmpty()
        .filter { it !is WrappedConstructor && it !is WrappedDestructor }
    check(
        "Shape has 3 plain methods (area, name, count)",
        shapeMethods.map { it.name }.sorted() == listOf("area", "count", "name"),
        "got ${shapeMethods.map { it.name }}"
    )
    val area = shapeMethods.find { it.name == "area" }
    check(
        "area return type spelling is 'int'",
        area?.returnType?.toString() == "int",
        "got ${area?.returnType}"
    )
    check("area is const", area?.isConst == true)
    check("area is virtual", area?.isVirtual == true)
    check("area is an instance METHOD", area?.methodType == MethodType.METHOD)
    val name = shapeMethods.find { it.name == "name" }
    check(
        "name return type spelling is 'const char*'",
        name?.returnType?.toString() == "const char*",
        "got ${name?.returnType}"
    )
    val count = shapeMethods.find { it.name == "count" }
    check("static count() maps to MethodType.STATIC", count?.methodType == MethodType.STATIC)

    val shapeCtors = shape?.children?.filterIsInstance<WrappedConstructor>().orEmpty()
    check("Shape has 2 constructors", shapeCtors.size == 2, "got ${shapeCtors.size}")
    val defaultCtor = shapeCtors.find { it.isDefaultConstructor }
    check(
        "default ctor: isDefaultConstructor, not copy, 0 args",
        defaultCtor != null && !defaultCtor.isCopyConstructor && defaultCtor.args.isEmpty()
    )
    val copyCtor = shapeCtors.find { it.isCopyConstructor }
    check(
        "copy ctor: isCopyConstructor with 1 reference arg",
        copyCtor != null && copyCtor.args.singleOrNull()?.type?.isReference == true,
        "got ${copyCtor?.args}"
    )
    val dtor = shape?.children?.filterIsInstance<WrappedDestructor>()?.singleOrNull()
    check("Shape has a destructor and it is virtual", dtor?.isVirtual == true)

    check(
        "deleted operator= is filtered out",
        shapeMethods.none { it.name == "operator=" }
    )
    check(
        "Shape metadata: hasDeletedCopyAssignment (deleted operator=)",
        shape?.metadata?.hasDeletedCopyAssignment == true
    )
    check(
        "Shape metadata: hasPrivateConstField (private 'const int cache')",
        shape?.metadata?.hasPrivateConstField == true
    )
    // ModelFactories only sets hasConstructor for a FILTERED ctor; Shape's are public.
    check(
        "Shape metadata: hasConstructor stays false for public-only ctors",
        shape?.metadata?.hasConstructor == false
    )
    val shapeFields = shape?.children?.filterIsInstance<WrappedField>().orEmpty()
    check(
        "Shape public field 'id: int' is the only field (private ones filtered)",
        shapeFields.singleOrNull()?.let { it.name == "id" && it.type.toString() == "int" } == true,
        "got $shapeFields"
    )

    // -- geo: namespace nesting, block merging, members landing inside it.
    val geos = tu.children.filterIsInstance<WrappedNamespace>().filter { it.namespace == "geo" }
    check("both 'namespace geo' blocks merge into ONE WrappedNamespace", geos.size == 1)
    val geo = geos.firstOrNull()
    val geoFns = geo?.children?.filterIsInstance<WrappedMethod>().orEmpty()
    check(
        "geo holds add+enabled (from both blocks) as STATIC free functions",
        geoFns.map { it.name }.sorted() == listOf("add", "enabled") &&
            geoFns.all { it.methodType == MethodType.STATIC },
        "got $geoFns"
    )
    val add = geoFns.find { it.name == "add" }
    check(
        "add(int a, int b) carries real parameter names",
        add?.args?.map { it.name } == listOf("a", "b"),
        "got ${add?.args}"
    )

    // -- Circle: derived class inside the namespace.
    val circle = geo?.children?.filterIsInstance<WrappedClass>()?.find { it.name == "Circle" }
    check("geo contains class Circle", circle != null)
    check("Circle is not abstract (area overridden)", circle?.isAbstract == false)
    val base = circle?.children?.filterIsInstance<WrappedBase>()?.singleOrNull()
    check(
        "Circle base: public, non-virtual, type Shape",
        base != null && base.isPublic && !base.isVirtualBase && base.type?.toString() == "Shape",
        "got ${base?.type} public=${base?.isPublic} virtual=${base?.isVirtualBase}"
    )
    val circleCtors = circle?.children?.filterIsInstance<WrappedConstructor>().orEmpty()
    check(
        "Circle keeps 1 public ctor (the private one is filtered)",
        circleCtors.size == 1,
        "got ${circleCtors.size}"
    )
    val circleCtor = circleCtors.firstOrNull()
    check(
        "Circle(double r): not copy, not default, 1 double arg named r",
        circleCtor != null && !circleCtor.isCopyConstructor && !circleCtor.isDefaultConstructor &&
            circleCtor.args.singleOrNull()
                ?.let { it.name == "r" && it.type.toString() == "double" } == true,
        "got ${circleCtor?.args}"
    )
    check(
        "Circle metadata: hasConstructor (private ctor recorded)",
        circle?.metadata?.hasConstructor == true
    )
    val circleFields = circle?.children?.filterIsInstance<WrappedField>().orEmpty()
    check(
        "Circle public fields are r + version (private 'cached' filtered)",
        circleFields.map { it.name } == listOf("r", "version"),
        "got $circleFields"
    )
    check(
        "Circle metadata: hasDeletedCopyAssignment (public 'const int version')",
        circle?.metadata?.hasDeletedCopyAssignment == true
    )
    val circleArea = circle?.children?.filterIsInstance<WrappedMethod>()
        ?.find { it.name == "area" && it !is WrappedConstructor }
    check("Circle's area override is virtual", circleArea?.isVirtual == true)

    // -- TU-level free function (unchanged from brick 2).
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

private fun printElements(elements: List<WrappedElement>, indent: String) {
    for (child in elements) {
        when (child) {
            is WrappedNamespace -> {
                println("${indent}namespace ${child.namespace}")
                printElements(child.children, "$indent  ")
            }
            is WrappedClass -> {
                println(
                    "${indent}class ${child.name}" +
                        (if (child.isAbstract) " (abstract)" else "") +
                        " (${child.children.size} members)"
                )
                printElements(child.children, "$indent  ")
            }
            is WrappedBase -> println("$indent: ${child.type} (public=${child.isPublic})")
            is WrappedField -> println("${indent}val $child")
            is WrappedMethod -> println(
                "$indent$child [${child.methodType}]" +
                    (if (child.isConst) " const" else "") +
                    (if (child.isVirtual) " virtual" else "")
            )
            else -> println("$indent${child::class.simpleName}")
        }
    }
}

private fun fail(message: String): Nothing {
    println("cppfrontend: FAIL — $message")
    exitProcess(1)
}
