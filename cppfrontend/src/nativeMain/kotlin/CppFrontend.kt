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
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.WrappedTypedef
import com.monkopedia.krapper.generator.model.type.WrappedEnumConstant
import com.monkopedia.krapper.generator.model.type.WrappedEnumType
import com.monkopedia.krapper.generator.model.type.WrappedModifiedType
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
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
// Brick 4 grows it with the STRUCTURAL type surface (Scene + the aliases + Holder): a
// `const Shape&` param, `Shape*` returns (one through the G8 const-method rule), a
// constant-array field, size_t/size_type preservation, typedef/`using` collapse, and a
// template-typed field. Holder is defined in-fixture rather than using std::vector so the
// case carries no std-header weight and the decoded shape is byte-deterministic.
// Brick 5 grows it with template DECLARATIONS and enums: Holder gains a T field, a
// T-returning const method, a `const T&` param and an in-template typedef (the dependent
// WrappedTemplateRef surface); the enums cover unscoped-implicit (Color: computed
// `unsigned int`), scoped-explicit (Mode: `unsigned char`), unscoped-explicit with a
// SIGNED constant (Status: `long`, OK=-2) and class-nested (Palette::Flavor); Palette
// exercises enum-typed fields, params and returns. Expected underlyings/values are pinned
// against the libclang oracle (clang_getEnumDeclIntegerType/clang_getEnumConstantDeclValue
// run on this exact fixture).
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
        Circle* makeCircle();
    }

    typedef unsigned long size_t;
    typedef int MyInt;
    using Real = double;

    enum Color { RED, GREEN = 5 };
    enum class Mode : unsigned char { A, B };
    enum Status : long { OK = -2 };

    template <typename T>
    struct Holder {
        typedef T value_type;
        T value;
        T get() const;
        void set(const T& v);
    };

    struct Scene {
        typedef unsigned long size_type;
        Shape* current();
        Shape* cloneShape() const;
        void describe(const Shape& shape);
        void reserve(size_t capacity);
        void setId(MyInt id);
        void setScale(Real scale);
        size_type size() const;
        int corners[4];
        Holder<Shape*> shapes;
    };

    struct Palette {
        enum Flavor { MILD, BOLD };
        Color primary;
        Mode mode;
        Color cycle(Color from);
        void setMode(Mode m);
        Mode currentMode() const;
        Status status() const;
        Flavor flavor() const;
    };
""".trimIndent()

private var failures = 0

private fun check(name: String, pass: Boolean, detail: String = "") {
    if (!pass) failures++
    println("  [${if (pass) "PASS" else "FAIL"}] $name${if (detail.isEmpty()) "" else " — $detail"}")
}

// #44 bricks 3-5: parse the fixture with Clang's C++ AST (on the kplusplus-generated
// libclang-cpp bindings), CONSTRUCT :krapper_model's WrappedTU mirroring ModelFactories'
// full decl/class/member contract — with every type decoded STRUCTURALLY from the QualType
// tree (TypeBuilder.kt), template declarations as WrappedTemplate, and enums as
// WrappedEnumType leaves — and self-check each constructed shape against the model.
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
        "geo holds add+enabled+makeCircle (from both blocks) as STATIC free functions",
        geoFns.map { it.name }.sorted() == listOf("add", "enabled", "makeCircle") &&
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

    // -- Brick 4: STRUCTURAL type construction (TypeBuilder.kt mirroring TypeFactories.kt).
    // Each check asserts the constructed WrappedType TREE (not just its spelling) AND that
    // toString() round-trips to the libclang path's spelling, citing the mirrored rule.
    val scene = tu.children.filterIsInstance<WrappedClass>().find { it.name == "Scene" }
    check("TU contains class Scene", scene != null)
    val sceneMethods = scene?.children?.filterIsInstance<WrappedMethod>().orEmpty()
    val sceneFields = scene?.children?.filterIsInstance<WrappedField>().orEmpty()

    // Builtin leaf (createForType `else -> WrappedType(spelling)`) + the PrintingPolicy
    // bridge: getAsString()'s default policy spells C's `_Bool`; the libclang path (the
    // TU's C++ LangOpts) spells `bool` — normalized in TypeBuilder.spellingOf.
    val enabled = geoFns.find { it.name == "enabled" }
    check(
        "bool return: builtin leaf normalizes _Bool -> WrappedTypeReference(\"bool\")",
        (enabled?.returnType as? WrappedTypeReference)?.name == "bool",
        "got ${enabled?.returnType}"
    )

    // Record leaf: createForType's ClassDecl/StructDecl -> WrappedType(fullyQualified) —
    // a namespaced record spells its "::"-joined semantic-parent chain.
    val makeCircle = geoFns.find { it.name == "makeCircle" }
    check(
        "makeCircle return spells the QUALIFIED record leaf: 'geo::Circle*'",
        makeCircle?.returnType?.toString() == "geo::Circle*",
        "got ${makeCircle?.returnType}"
    )

    // Pointer shape: invoke's `spelling.endsWith("*") -> pointerTo(invoke(pointee))`.
    val currentRet = sceneMethods.find { it.name == "current" }?.returnType
    check(
        "Shape* return: WrappedModifiedType(\"*\") over record leaf WrappedTypeReference(\"Shape\")",
        currentRet is WrappedModifiedType && currentRet.modifier == "*" &&
            (currentRet.baseType as? WrappedTypeReference)?.name == "Shape",
        "got $currentRet"
    )
    check("Shape* round-trips to 'Shape*'", currentRet?.toString() == "Shape*")

    // G8 const-method rule (ModelFactories.WrappedMethod): a const method's constness
    // carries to a pointer return as an OUTER const — const(pointerTo(Shape)).
    val cloneRet = sceneMethods.find { it.name == "cloneShape" }?.returnType
    check(
        "const method's Shape* return gains OUTER const: WrappedPrefixedType(const, Shape*)",
        cloneRet is WrappedPrefixedType && cloneRet.modifier == "const" &&
            (cloneRet.baseType as? WrappedModifiedType)?.modifier == "*",
        "got $cloneRet"
    )

    // Reference shape: invoke's `spelling.endsWith("&") -> referenceTo(invoke(pointee))`;
    // the const is the POINTEE's qualifier, so it nests INSIDE: &(const(Shape)).
    val describeArg = sceneMethods.find { it.name == "describe" }?.args?.singleOrNull()?.type
    val describeBase = (describeArg as? WrappedModifiedType)?.baseType
    check(
        "const Shape& param: WrappedModifiedType(\"&\") over WrappedPrefixedType(const) over Shape",
        describeArg is WrappedModifiedType && describeArg.modifier == "&" &&
            describeBase is WrappedPrefixedType && describeBase.modifier == "const" &&
            (describeBase.baseType as? WrappedTypeReference)?.name == "Shape",
        "got $describeArg"
    )
    check(
        "const Shape& round-trips to 'const Shape&'",
        describeArg?.toString() == "const Shape&"
    )
    // The same shape now backs Shape's copy-ctor arg (brick 3 parsed it from a spelling).
    val copyArg = copyCtor?.args?.singleOrNull()?.type
    check(
        "copy-ctor's const Shape& arg decodes to the same structural shape",
        copyArg is WrappedModifiedType && copyArg.modifier == "&" &&
            (copyArg.baseType as? WrappedPrefixedType)?.modifier == "const",
        "got $copyArg"
    )
    // ...and name()'s return is the pointer flavor: *(const(char)).
    val nameRet = name?.returnType
    check(
        "const char* return: WrappedModifiedType(\"*\") over WrappedPrefixedType(const) over char",
        nameRet is WrappedModifiedType && nameRet.modifier == "*" &&
            ((nameRet.baseType as? WrappedPrefixedType)?.baseType as? WrappedTypeReference)
                ?.name == "char",
        "got $nameRet"
    )

    // size_t param: cAliasTypedefElement preserves the alias NAME (not the canonical
    // 'unsigned long'), so the Kotlin side surfaces platform.posix.size_t.
    val reserveArg = sceneMethods.find { it.name == "reserve" }?.args?.singleOrNull()?.type
    check(
        "size_t param preserved as the NATIVE alias leaf WrappedTypeReference(\"size_t\")",
        (reserveArg as? WrappedTypeReference)?.name == "size_t" && reserveArg?.isNative == true,
        "got $reserveArg"
    )
    // size_type return: sizeTypedefElement always normalizes size_type -> size_t.
    val sizeRet = sceneMethods.find { it.name == "size" }?.returnType
    check(
        "size_type return normalizes to WrappedTypeReference(\"size_t\")",
        (sizeRet as? WrappedTypeReference)?.name == "size_t",
        "got $sizeRet"
    )
    // Ordinary typedef collapse (ResolverBuilderImpl.visit's CXCursor_TypedefDecl branch).
    val setIdArg = sceneMethods.find { it.name == "setId" }?.args?.singleOrNull()?.type
    check(
        "typedef'd MyInt param collapses to the underlying builtin 'int'",
        (setIdArg as? WrappedTypeReference)?.name == "int",
        "got $setIdArg"
    )
    // `using` alias collapse — deterministic here; ORDER-DEPENDENT on the libclang path
    // (the documented Phase C divergence, see TypeBuilder.kt's typedef branch).
    val setScaleArg = sceneMethods.find { it.name == "setScale" }?.args?.singleOrNull()?.type
    check(
        "`using Real` param collapses to the underlying builtin 'double'",
        (setScaleArg as? WrappedTypeReference)?.name == "double",
        "got $setScaleArg"
    )

    // Constant array: the libclang leaf spelling is "int [4]" (krapper_gen TestData
    // golden "int [5]"); WrappedTypeReference parses element/extent off that name.
    val corners = sceneFields.find { it.name == "corners" }?.type
    check(
        "int[4] field: WrappedTypeReference(\"int [4]\") with isArray, size 4, element int",
        (corners as? WrappedTypeReference)?.let {
            it.name == "int [4]" && it.isArray && it.arraySize == 4 && it.arrayType.name == "int"
        } == true,
        "got $corners"
    )

    // Template-typed reference: WrappedTemplateType(base ref, decoded args) — base from
    // the specialization decl's qualified name, each arg structurally decoded.
    val shapes = sceneFields.find { it.name == "shapes" }?.type
    val shapesArg = (shapes as? WrappedTemplateType)?.templateArgs?.singleOrNull()
    check(
        "Holder<Shape*> field: WrappedTemplateType(base WrappedTypeReference(\"Holder\"), 1 arg)",
        shapes is WrappedTemplateType &&
            (shapes.baseType as? WrappedTypeReference)?.name == "Holder" &&
            shapes.templateArgs.size == 1,
        "got $shapes"
    )
    check(
        "Holder's template arg decodes structurally to *(Shape)",
        shapesArg is WrappedModifiedType && shapesArg.modifier == "*" &&
            (shapesArg.baseType as? WrappedTypeReference)?.name == "Shape",
        "got $shapesArg"
    )
    check(
        "Holder<Shape*> round-trips to 'Holder<Shape*>'",
        shapes?.toString() == "Holder<Shape*>"
    )

    // -- Brick 5: template DECLARATION construction (ModelFactories' ClassTemplate branch).
    val holders = tu.children.filterIsInstance<WrappedTemplate>()
    val holder = holders.find { it.name == "Holder" }
    check(
        "TU contains exactly one WrappedTemplate: Holder",
        holders.size == 1 && holder != null,
        "got ${holders.map { it.name }}"
    )
    check(
        "no WrappedClass shadows Holder (implicit specialization never surfaced)",
        tu.children.filterIsInstance<WrappedClass>().map { it.name }.sorted() ==
            listOf("Palette", "Scene", "Shape"),
        "got ${tu.children.filterIsInstance<WrappedClass>().map { it.name }}"
    )
    val tParam = holder?.templateArgs?.singleOrNull()
    check(
        "Holder has ONE type param 'T' carrying cpp:<canonical-id> identity",
        tParam != null && tParam.name == "T" && tParam.usr.startsWith("cpp:"),
        "got ${holder?.templateArgs}"
    )
    val valueField = holder?.fields?.singleOrNull()
    check(
        "field 'T value': WrappedTemplateRef keyed to the param's usr (substitution contract)",
        valueField != null && valueField.name == "value" &&
            (valueField.type as? WrappedTemplateRef)?.target == tParam?.usr,
        "got $valueField"
    )
    val getMethod = holder?.methods?.find { it.name == "get" }
    check(
        "T get() const: BARE WrappedTemplateRef return (G8: no const wrap on by-value)",
        getMethod != null && getMethod.isConst &&
            (getMethod.returnType as? WrappedTemplateRef)?.target == tParam?.usr,
        "got ${getMethod?.returnType}"
    )
    val setArg = holder?.methods?.find { it.name == "set" }?.args?.singleOrNull()?.type
    val setBase = (setArg as? WrappedModifiedType)?.baseType as? WrappedPrefixedType
    check(
        "set(const T&): the dependent type nests structurally — &(const(WrappedTemplateRef))",
        setArg is WrappedModifiedType && setArg.modifier == "&" &&
            setBase?.modifier == "const" &&
            (setBase?.baseType as? WrappedTemplateRef)?.target == tParam?.usr,
        "got $setArg"
    )
    val valueTypedef = holder?.children?.filterIsInstance<WrappedTypedef>()?.singleOrNull()
    check(
        "in-template `typedef T value_type`: WrappedTypedef -> WrappedTemplateRef(param usr)",
        valueTypedef != null && valueTypedef.name == "value_type" &&
            (valueTypedef.targetType as? WrappedTemplateRef)?.target == tParam?.usr,
        "got $valueTypedef"
    )

    // -- Brick 5: typedef ELEMENTS (map's CXCursor_TypedefDecl branch — and the absence of
    // a CXCursor_TypeAliasDecl branch, so `using Real` produces NO element).
    val tuTypedefs = tu.children.filterIsInstance<WrappedTypedef>()
    check(
        "TU typedef elements are size_t + MyInt only (`using Real` has no element)",
        tuTypedefs.map { it.name } == listOf("size_t", "MyInt"),
        "got ${tuTypedefs.map { it.name }}"
    )
    check(
        "typedef targets run the reducer stack: size_t keeps the C-alias NAME, MyInt -> int",
        tuTypedefs.find { it.name == "size_t" }?.targetType?.toString() == "size_t" &&
            tuTypedefs.find { it.name == "MyInt" }?.targetType?.toString() == "int",
        "got ${tuTypedefs.map { "${it.name}=${it.targetType}" }}"
    )
    val sizeTypedef = scene?.children?.filterIsInstance<WrappedTypedef>()?.singleOrNull()
    check(
        "Scene's member `typedef ... size_type` element normalizes to size_t",
        sizeTypedef != null && sizeTypedef.name == "size_type" &&
            sizeTypedef.targetType.toString() == "size_t",
        "got $sizeTypedef"
    )

    // -- Brick 5: enum construction. NO top-level element (ModelFactories.map has no
    // EnumDecl branch — `else -> null`); the payload rides on every enum-typed LEAF as
    // WrappedEnumType (TypeFactories' CXCursor_EnumDecl branch). Underlyings + values are
    // pinned against the libclang oracle for this fixture.
    check(
        "TU has exactly 8 children (the 3 enum DECLs contribute NO elements)",
        tu.children.size == 8,
        "got ${tu.children.size}"
    )
    val palette = tu.children.filterIsInstance<WrappedClass>().find { it.name == "Palette" }
    check("TU contains class Palette", palette != null)
    val paletteMethods = palette?.children?.filterIsInstance<WrappedMethod>().orEmpty()
    val paletteFields = palette?.children?.filterIsInstance<WrappedField>().orEmpty()
    val primary = paletteFields.find { it.name == "primary" }?.type as? WrappedEnumType
    check(
        "Color field: WrappedEnumType 'Color' with the COMPUTED underlying 'unsigned int'",
        primary != null && primary.cppName == "Color" && primary.toString() == "Color" &&
            primary.underlying.toString() == "unsigned int",
        "got $primary underlying=${primary?.underlying}"
    )
    check(
        "Color constants mirror the oracle: RED=0, GREEN=5",
        primary?.constants ==
            listOf(WrappedEnumConstant("RED", 0L), WrappedEnumConstant("GREEN", 5L)),
        "got ${primary?.constants}"
    )
    check(
        "enum leaf flags: isEnum, NOT native (boundary casts), cType = the underlying",
        primary != null && primary.isEnum && !primary.isNative &&
            primary.cType == primary.underlying
    )
    val modeArg =
        paletteMethods.find { it.name == "setMode" }?.args?.singleOrNull()?.type as? WrappedEnumType
    check(
        "scoped `enum class Mode : unsigned char` arg: EXPLICIT underlying + A=0, B=1",
        modeArg != null && modeArg.cppName == "Mode" &&
            modeArg.underlying.toString() == "unsigned char" &&
            modeArg.constants ==
            listOf(WrappedEnumConstant("A", 0L), WrappedEnumConstant("B", 1L)),
        "got $modeArg underlying=${modeArg?.underlying} constants=${modeArg?.constants}"
    )
    val modeRet = paletteMethods.find { it.name == "currentMode" }?.returnType
    check(
        "Mode return on a const method: bare WrappedEnumType (G8: by-value, no const wrap)",
        (modeRet as? WrappedEnumType)?.cppName == "Mode",
        "got $modeRet"
    )
    val cycle = paletteMethods.find { it.name == "cycle" }
    check(
        "Color cycle(Color): the enum decodes as BOTH return and argument",
        (cycle?.returnType as? WrappedEnumType)?.cppName == "Color" &&
            (cycle?.args?.singleOrNull()?.type as? WrappedEnumType)?.cppName == "Color",
        "got ret=${cycle?.returnType} args=${cycle?.args}"
    )
    val statusRet = paletteMethods.find { it.name == "status" }?.returnType as? WrappedEnumType
    check(
        "unscoped-explicit `enum Status : long`: underlying 'long', SIGNED value OK=-2",
        statusRet != null && statusRet.underlying.toString() == "long" &&
            statusRet.constants == listOf(WrappedEnumConstant("OK", -2L)),
        "got $statusRet underlying=${statusRet?.underlying} constants=${statusRet?.constants}"
    )
    val flavorRet = paletteMethods.find { it.name == "flavor" }?.returnType as? WrappedEnumType
    check(
        "nested enum spells QUALIFIED: 'Palette::Flavor' (fullyQualified rule), MILD+BOLD",
        flavorRet != null && flavorRet.cppName == "Palette::Flavor" &&
            flavorRet.constants.map { it.name } == listOf("MILD", "BOLD"),
        "got $flavorRet constants=${flavorRet?.constants}"
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
            is WrappedTemplate -> {
                println(
                    "${indent}template<${child.templateArgs.joinToString(", ") { it.name }}> " +
                        "class ${child.name} (${child.children.size} children)"
                )
                printElements(child.children, "$indent  ")
            }
            is WrappedTypedef -> println("${indent}typedef ${child.name} = ${child.targetType}")
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
