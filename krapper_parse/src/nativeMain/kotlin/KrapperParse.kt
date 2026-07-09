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
import com.monkopedia.krapper.generator.model.ForcingHeader
import com.monkopedia.krapper.generator.model.ModelIo
import com.monkopedia.krapper.generator.model.WrappedBase
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedDestructor
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.WrappedTypedef
import com.monkopedia.krapper.generator.model.serialized
import com.monkopedia.krapper.generator.model.type.WrappedEnumConstant
import com.monkopedia.krapper.generator.model.type.WrappedEnumType
import com.monkopedia.krapper.generator.model.type.WrappedFunctionPointer
import com.monkopedia.krapper.generator.model.type.WrappedModifiedType
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedTypeReference
import com.monkopedia.krapper.generator.resolvedmodel.MethodType
import kotlin.system.exitProcess
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kppbridge.buildASTWithArgs
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

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
// Brick 6 grows it with FUNCTION-POINTER TYPEDEFS and DEFAULT ARGUMENTS: Callback (the
// CB-cfnptr shape), Notifier (the `void*` Mode-1 context slot), ShapeVisitor (a
// class-pointer param failing the stage-1 compat gate — CB-cfnptr-richsig), HandlerFn (a
// `using` fn-ptr alias, which gets NO WrappedFunctionPointer on the libclang path) and the
// namespace-scoped geo::Predicate (the cName/cppName qualification split); Dispatcher
// carries the five default-arg value shapes (int literal, negative, enum constant,
// nullptr, constructed) plus a multi-default trailing run (configure). Expected
// defaultValue strings are the libclang token-join contract (ModelFactories.defaultValue).
// Phase D (#46) grows it with the REMAINING typedef-reducer mirrors and defaultType:
// SmartPtr (a dependent `pointer` alias + `using element_type` — unique_ptr's shape,
// pointerTypedefElement), RawHolder (a CONCRETE pointer typedef that must NOT be
// rewritten), MiniMap/MiniSet (dependent key_type/mapped_type/value_type through trait
// scopes — unordered_map's and set's shapes, assocTypedefElement), and DefBox/DefPair
// (defaulted template params — the cursor-walk residue WrappedTemplateParam.defaultType
// reads; expected shapes pinned against the libclang oracle for this exact fixture).
// D1b (#46) grows it with MiniStr + an in-fixture std::initializer_list declaration: the
// two name-tref dependent initializer_list element shapes (direct param — basic_string's
// family; typedef-to-param — vector's) whose members must SURVIVE with the libclang
// survivor's exact name-tref spelling. MiniStr's param is deliberately named T: the
// libclang typedef-to-param collapse spells the FIRST-SEEN param-0 name from visit()'s
// seenNames cache (here Holder's "T"; probed — a "C"-named param diverged as
// cpp=template<C> vs libclang=template<T>), the same N5-family order-dependence D3
// ledger-accepts. libstdc++'s uniform `_Tp`-style naming keeps production on the
// convergent path (vector's survivors spell `template__Tp` on both sides — the parity
// ratchet pins the actuals); naming the fixture param T pins that same path.
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
        typedef bool (*Predicate)(double);
        void scan(Predicate p);
    }

    typedef unsigned long size_t;
    typedef int MyInt;
    using Real = double;

    typedef void (*Callback)(int);
    typedef int (*Notifier)(void*, int);
    typedef void (*ShapeVisitor)(Shape*);
    using HandlerFn = int (*)(double);

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

    struct Dispatcher {
        void onEvent(Callback cb);
        Callback current() const;
        void notifyAll(Notifier n);
        void visit(ShapeVisitor v);
        void onHandle(HandlerFn h);
        void resize(int size = 5);
        void shift(int delta = -1);
        void paint(Color tint = RED);
        void fill(Shape* target = nullptr);
        void blend(Palette p = Palette());
        void configure(int a, int b = 1, Shape* c = nullptr);
    };

    template <typename T> struct PtrTrait { typedef T type; };

    template <typename T>
    struct SmartPtr {
        using element_type = T;
        typedef typename PtrTrait<T>::type pointer;
        pointer get() const;
    };

    struct RawHolder {
        typedef int* pointer;
        pointer raw();
    };

    template <typename K, typename V> struct MapTraits { typedef K key; typedef V mapped; };

    template <typename K, typename V>
    struct MiniMap {
        typedef typename MapTraits<K, V>::key key_type;
        typedef typename MapTraits<K, V>::mapped mapped_type;
        mapped_type& at(const key_type& k);
    };

    template <typename K> struct SetTraits { typedef K stored; };

    template <typename K>
    struct MiniSet {
        typedef typename SetTraits<K>::stored key_type;
        typedef typename SetTraits<K>::stored value_type;
        void insert(const value_type& v);
    };

    template <typename H> struct HashTraits { typedef H hash_fn; };

    template <typename K, typename H>
    struct MiniHash {
        typedef HashTraits<H> traits_type;
        typedef typename HashTraits<H>::hash_fn hasher;
        typedef typename traits_type::hash_fn key_equal;
        void rehash(const hasher& h);
        void requal(const key_equal& q);
    };

    template <typename T> struct DefAlloc {};

    template <typename T, typename A = DefAlloc<T>>
    struct DefBox {
        T item;
        A alloc() const;
    };

    template <typename T, typename U = T>
    struct DefPair {};

    // Guarded with libstdc++'s own include guard: the generated wrapper #includes this
    // fixture BEFORE its <vector>/<string> boilerplate (handoffGenerate's compile step),
    // so defining _INITIALIZER_LIST here makes the real header a no-op there. The class
    // must then be REAL-equivalent enough for the stl headers compiled after it:
    // layout-compatible (pointer + length) and carrying begin()/end()/size(), which
    // vector<bool>'s inline bodies call on the CONCRETE initializer_list<bool> (checked
    // at parse, no instantiation needed). The param is named T for the same seenNames
    // reason as MiniStr's. Standalone parses (BOTH front-ends, the same bytes) see this
    // declaration — the macro is never defined there.
    #ifndef _INITIALIZER_LIST
    #define _INITIALIZER_LIST
    namespace std {
        template <typename T> class initializer_list {
            const T* items;
            unsigned long len;
        public:
            constexpr unsigned long size() const noexcept { return len; }
            constexpr const T* begin() const noexcept { return items; }
            constexpr const T* end() const noexcept { return items + len; }
        };
    }
    #endif

    template <typename T>
    struct MiniStr {
        typedef T value_type;
        void append(std::initializer_list<T> l);
        void assignAll(std::initializer_list<value_type> l);
    };
""".trimIndent()

// #45 brick 3: the INSTANTIATION-FORCING fixture — a SIBLING of FIXTURE_HEADER (extending
// it would put all of <vector> under the golden compare's full-tree diff). Mirrors
// featuregen's RangeHolder shape reduced to its essence: an element class (Item) and an
// owner (Bag) whose range accessor returns a std::vector specialization that only an
// `--instantiate "std::vector<Item*>"` request materializes. On the main resolve
// (IGNORE_MISSING) `items()` DROPS — the container isn't bound yet — and is recovered by
// resolveForcing's pass 3, exactly the libclang flow this fixture cross-checks. The
// include guard matters: the generated wrapper #includes bag.h both directly and through
// the re-materialized KrapperForce_*.h.
private val FORCING_FIXTURE_HEADER = """
    #pragma once
    #include <vector>

    struct Item {
        int id;
        Item() : id(0) {}
        Item(int id_) : id(id_) {}
        int getId() const { return id; }
    };

    struct Bag {
        Bag();
        void add(Item* t);
        int count() const;
        std::vector<Item*> items() const;
    };
""".trimIndent()

// The fixture's instantiation requests — one per `--instantiate` the generate step makes.
private val FORCING_TARGETS = listOf("std::vector<Item*>")

// #101 — the NONCOPYABLE DETERMINISM fixture. A self-contained reduction of v8::Persistent's
// exact shape: a NON-COPYABLE base (`= delete`d copy ctor + copy assignment), a traits class
// whose `Copy` is a `static_assert(sizeof(T) < 0)` (un-instantiable on use), and the NC
// template that USER-DECLARES a copy ctor + copy assignment whose bodies call the trap. The
// `ForceNC` struct's `value` member implicitly INSTANTIATES `NC<Value>` — the same trigger
// the synthesized forcing header (ForcingHeader) is for `v8::Persistent<v8::Value>`.
//
// THE INVARIANT THIS LOCKS (#101): the special-member set of a NonCopyable type must be
// IDENTICAL across parses. The original report saw `Persistent<v8::Value>` surface its copy
// ctor on one parse and its copy assignment on another. The root cause that COULD produce
// that is lazy template-member instantiation: Clang instantiates a specialization's member
// FUNCTIONS on first use, so an implicitly-instantiated specialization's `decls()` carries
// only what THIS parse happened to trigger — a parse-dependent set. This front-end is immune
// BY CONSTRUCTION because it never walks the specialization (ModelBuilder.addContextDecls
// SKIPS ClassTemplateSpecialization, citing #101): NC's members ride the template PATTERN,
// whose `decls()` are fixed source-order declarations, and the resolver materializes
// `NC<Value>` by deterministic param substitution (Parsing.typedAs). This guard parses the
// fixture repeatedly and asserts the pattern's special-member set never varies — so a future
// change that started walking the lazily-instantiated specialization would fail here.
private val NONCOPYABLE_FIXTURE = """
    struct Value { int x; };

    template <class T>
    class NCBase {
     public:
        void reset();
        bool isEmpty() const { return ptr_ == nullptr; }
        NCBase(const NCBase&) = delete;
        void operator=(const NCBase&) = delete;
     protected:
        explicit NCBase(T* p) : ptr_(p) {}
        T* ptr_;
    };

    template <class T>
    struct NonCopyableTraits {
        template <class S>
        static void Copy(const S* from, S* to) {
            static_assert(sizeof(T) < 0, "NonCopyableTraits::Copy is not instantiable");
        }
    };

    template <class T, class M = NonCopyableTraits<T> >
    class NC : public NCBase<T> {
     public:
        NC() : NCBase<T>(nullptr) {}
        NC(const NC& that) : NCBase<T>(nullptr) { M::Copy(that.ptr_, this->ptr_); }
        NC& operator=(const NC& that) { M::Copy(that.ptr_, this->ptr_); return *this; }
        ~NC() {}
        int get() const;
    };

    struct ForceNC { NC<Value> value; };
""".trimIndent()

// Parse the NonCopyable fixture this many times; any cross-parse divergence fails the guard.
private const val NONCOPYABLE_PARSE_COUNT = 8

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
// Brick 7's golden-emit mode (see KrapperParse.kt --golden-emit below):
//  --golden-emit <dir>: write the fixture (for krapper_gen to parse the SAME bytes) and
//    this front-end's handoff JSON, then exit (self-checks stay on the default run).
// The historical --golden-compare mode (structural diff vs krapper_gen's libclang-C
// reducer) was removed in #92: the reducer was deleted in the self-hosting flip (B5, #88),
// so there is no second front-end to diff against.
fun main(args: Array<String>): Unit = memScoped {
    if (args.firstOrNull() == "--handoff-emit") {
        val dir = args.getOrNull(1) ?: fail("--handoff-emit <dir>")
        handoffEmit(dir)
        return@memScoped
    }
    if (args.firstOrNull() == "--noncopyable-determinism") {
        // #101 — parse the NonCopyable fixture NONCOPYABLE_PARSE_COUNT times and assert the
        // special-member set never varies across parses (the cpp regression lock for the
        // Persistent<v8::Value> parse-dependent copy-member report). Self-contained: no
        // external header, so the only gradle wiring is `dependsOn(linkRelease…)`.
        noncopyableDeterminismCheck()
        return@memScoped
    }
    if (args.firstOrNull() == "--parity-emit") {
        // --parity-emit <dir> <header> <std> [-I<dir>...] [spec...] — Phase D (#46), see
        // parityEmit. The trailing args mix the module's extra include dirs (each a `-I<dir>`
        // token, threaded by the gradle plugin from `kplusplus { headerDirectory(...) }`) with
        // the instantiation specs (C++ type strings, which never start with `-I`); parityEmit
        // partitions them.
        val usage = "--parity-emit <dir> <header> <std> [-I<dir>...] [spec...]"
        parityEmit(
            args.getOrNull(1) ?: fail(usage),
            args.getOrNull(2) ?: fail(usage),
            args.getOrNull(3) ?: fail(usage),
            args.drop(4)
        )
        return@memScoped
    }
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

    if (args.firstOrNull() == "--golden-emit") {
        val dir = args.getOrNull(1) ?: fail("--golden-emit <dir>")
        // The fixture is written out so krapper_gen parses the EXACT same bytes this
        // front-end just consumed (buildASTFromCode above) — the one-source guarantee.
        writeFile("$dir/fixture.h", FIXTURE_HEADER + "\n")
        writeFile("$dir/cpp.json", Json.encodeToString(tu.serialized()))
        // #45 brick 2 (THE HANDOFF): alongside the lossy-by-design compare projection
        // above, emit the FULL-fidelity ModelIo round-trip JSON — the --frontend=cpp
        // handoff format krapper_gen loads to run resolution+codegen on this tree.
        // ModelIo's encoder is DAG-aware (Def/Ref), so any element/type instances this
        // front-end shares across use sites encode as back-references, exactly like a
        // libclang-built tree's interned types.
        writeFile("$dir/model.json", ModelIo.encodeToString(tu))
        println(
            "krapper_parse: golden emit -> $dir/fixture.h + $dir/cpp.json + $dir/model.json"
        )
        return@memScoped
    }

    // ---- Structural summary ----
    println("krapper_parse: constructed WrappedTU from the Clang C++ AST")
    printElements(tu.children, indent = "  ")

    // ---- Serialize ----
    val json = Json { prettyPrint = true }.encodeToString(tu.serialized())
    println("\nkrapper_parse: serialized WrappedTU")
    println(json)

    // ---- Self-checks ----
    println("\nkrapper_parse: self-checks")

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
        "geo holds add+enabled+makeCircle+scan (from both blocks) as STATIC free functions",
        geoFns.map { it.name }.sorted() == listOf("add", "enabled", "makeCircle", "scan") &&
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

    // Constant array: LLVM-22's libclang leaf spelling is "int[4]" — no space (pinned by
    // the golden compare; the old TestData golden "int [5]" predates the TypePrinter
    // change). WrappedTypeReference parses element/extent off that name either way.
    val corners = sceneFields.find { it.name == "corners" }?.type
    check(
        "int[4] field: WrappedTypeReference(\"int[4]\") with isArray, size 4, element int",
        (corners as? WrappedTypeReference)?.let {
            it.name == "int[4]" && it.isArray && it.arraySize == 4 && it.arrayType.name == "int"
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
        "TU contains the 13 WrappedTemplates in source order (Holder + the Phase D shapes)",
        holders.map { it.name } == listOf(
            "Holder", "PtrTrait", "SmartPtr", "MapTraits", "MiniMap",
            "SetTraits", "MiniSet", "HashTraits", "MiniHash", "DefAlloc", "DefBox",
            "DefPair", "MiniStr"
        ),
        "got ${holders.map { it.name }}"
    )
    check(
        "no WrappedClass shadows a template (implicit specialization never surfaced)",
        tu.children.filterIsInstance<WrappedClass>().map { it.name }.sorted() ==
            listOf("Dispatcher", "Palette", "RawHolder", "Scene", "Shape"),
        "got ${tu.children.filterIsInstance<WrappedClass>().map { it.name }}"
    )
    val tParam = holder?.templateArgs?.singleOrNull()
    check(
        "Holder has ONE type param 'T' carrying cpp:<canonical-id> identity",
        tParam != null && tParam.name == "T" && tParam.usr.startsWith("cpp:"),
        "got ${holder?.templateArgs}"
    )
    // #45 brick 3 revision: dependent-T refs key on the param's NAME (the libclang
    // in-template fallback every member use actually hits — and the spelling baked into
    // uniqueCNames), not the cpp:<id> identity. typedAs maps name AND usr, so either
    // key substitutes; the name is what output parity requires (TypeBuilder's TTPT branch).
    val valueField = holder?.fields?.singleOrNull()
    check(
        "field 'T value': WrappedTemplateRef keyed to the param's NAME (uniqueCName parity)",
        valueField != null && valueField.name == "value" &&
            (valueField.type as? WrappedTemplateRef)?.target == "T",
        "got $valueField"
    )
    val getMethod = holder?.methods?.find { it.name == "get" }
    check(
        "T get() const: BARE WrappedTemplateRef return (G8: no const wrap on by-value)",
        getMethod != null && getMethod.isConst &&
            (getMethod.returnType as? WrappedTemplateRef)?.target == "T",
        "got ${getMethod?.returnType}"
    )
    val setArg = holder?.methods?.find { it.name == "set" }?.args?.singleOrNull()?.type
    val setBase = (setArg as? WrappedModifiedType)?.baseType as? WrappedPrefixedType
    check(
        "set(const T&): the dependent type nests structurally — &(const(WrappedTemplateRef))",
        setArg is WrappedModifiedType && setArg.modifier == "&" &&
            setBase?.modifier == "const" &&
            (setBase?.baseType as? WrappedTemplateRef)?.target == "T",
        "got $setArg"
    )
    val valueTypedef = holder?.children?.filterIsInstance<WrappedTypedef>()?.singleOrNull()
    check(
        "in-template `typedef T value_type`: WrappedTypedef -> WrappedTemplateRef(param name)",
        valueTypedef != null && valueTypedef.name == "value_type" &&
            (valueTypedef.targetType as? WrappedTemplateRef)?.target == "T",
        "got $valueTypedef"
    )

    // -- Brick 5: typedef ELEMENTS (map's CXCursor_TypedefDecl branch — and the absence of
    // a CXCursor_TypeAliasDecl branch, so `using Real` produces NO element).
    val tuTypedefs = tu.children.filterIsInstance<WrappedTypedef>()
    check(
        "TU typedef elements: the `typedef`s only (`using Real`/`using HandlerFn` have none)",
        tuTypedefs.map { it.name } ==
            listOf("size_t", "MyInt", "Callback", "Notifier", "ShapeVisitor"),
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
        "TU has exactly 26 children (the 3 enum DECLs + 2 `using` aliases contribute NONE)",
        tu.children.size == 26,
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

    // -- Brick 6: function-pointer typedefs (TypeFactories.functionPointerTypedefElement).
    val dispatcher = tu.children.filterIsInstance<WrappedClass>().find { it.name == "Dispatcher" }
    check("TU contains class Dispatcher", dispatcher != null)
    val dMethods = dispatcher?.children?.filterIsInstance<WrappedMethod>().orEmpty()

    // The typedef ELEMENT's target runs the same reducer (WrappedTypedef(spelling,
    // WrappedType(cursor.type)) on the libclang path hits functionPointerTypedefElement).
    val callback = tuTypedefs.find { it.name == "Callback" }?.targetType
    check(
        "Callback typedef: WrappedFunctionPointer, cName == cppName == 'Callback' (global)",
        (callback as? WrappedFunctionPointer)
            ?.let { it.cName == "Callback" && it.cppName == "Callback" } == true,
        "got $callback"
    )
    check(
        "Callback proto decodes structurally: return 'void', args ['int']",
        (callback as? WrappedFunctionPointer)?.let {
            it.returnType.toString() == "void" &&
                it.argTypes.map { a -> a.toString() } == listOf("int")
        } == true,
        "got ret=${(callback as? WrappedFunctionPointer)?.returnType} " +
            "args=${(callback as? WrappedFunctionPointer)?.argTypes}"
    )
    // The same construction through the TYPE-USE path (a param spelled `Callback`).
    val onEventArg = dMethods.find { it.name == "onEvent" }?.args?.singleOrNull()?.type
    check(
        "onEvent(Callback) param: WrappedFunctionPointer, NATIVE, spells the typedef name",
        onEventArg is WrappedFunctionPointer && onEventArg.isNative &&
            onEventArg.toString() == "Callback",
        "got $onEventArg"
    )
    // A fn-ptr is neither pointer nor reference in the model, so the G8 const-method rule
    // never wraps it — the const method returns the BARE WrappedFunctionPointer
    // (CB-cfnptr-ret: the returned pointer is directly invokable).
    val currentFnRet = dMethods.find { it.name == "current" }?.returnType
    check(
        "Callback current() const: bare WrappedFunctionPointer return (no const wrap)",
        (currentFnRet as? WrappedFunctionPointer)?.cName == "Callback",
        "got $currentFnRet"
    )
    // The `void*` Mode-1 context slot is compatible (isPointer && pointed.isVoid).
    val notifier = tuTypedefs.find { it.name == "Notifier" }?.targetType
    check(
        "Notifier(void*, int): the void* context slot passes the stage-1 compat gate",
        (notifier as? WrappedFunctionPointer)?.let {
            it.returnType.toString() == "int" &&
                it.argTypes.map { a -> a.toString() } == listOf("void*", "int")
        } == true,
        "got $notifier"
    )
    // CB-cfnptr-richsig: a class-pointer param (`Shape*` != `void*`) fails the gate, so
    // the typedef falls through to the normal alias collapse — the bare fn-ptr leaf
    // (createForType's `else -> WrappedType(spelling)` after visit()'s collapse).
    val visitor = tuTypedefs.find { it.name == "ShapeVisitor" }?.targetType
    check(
        "ShapeVisitor (Shape* param) fails the gate: NOT a WrappedFunctionPointer",
        visitor !is WrappedFunctionPointer && visitor?.toString() == "void (*)(Shape *)",
        "got $visitor"
    )
    val visitArg = dMethods.find { it.name == "visit" }?.args?.singleOrNull()?.type
    check(
        "visit(ShapeVisitor) param falls through the same way",
        visitArg !is WrappedFunctionPointer && visitArg?.toString() == "void (*)(Shape *)",
        "got $visitArg"
    )
    // A `using` fn-ptr alias NEVER becomes a WrappedFunctionPointer
    // (functionPointerTypedefElement is keyed on CXCursor_TypedefDecl only); here it
    // collapses to the bare fn-ptr leaf (the documented using-collapse divergence — the
    // libclang path leaves the order-dependent alias-name leaf instead).
    val onHandleArg = dMethods.find { it.name == "onHandle" }?.args?.singleOrNull()?.type
    check(
        "`using HandlerFn` param: NOT a WrappedFunctionPointer (typedef-only contract)",
        onHandleArg !is WrappedFunctionPointer && onHandleArg?.toString() == "int (*)(double)",
        "got $onHandleArg"
    )
    // A namespace-scoped typedef splits the names: cName stays UNqualified (the extern-"C"
    // redeclaration), cppName is qualified (the wrapper spelling; fullyQualified rule).
    val predicate = geo?.children?.filterIsInstance<WrappedTypedef>()
        ?.find { it.name == "Predicate" }?.targetType
    check(
        "geo::Predicate: cName 'Predicate', cppName 'geo::Predicate', bool(double) decoded",
        (predicate as? WrappedFunctionPointer)?.let {
            it.cName == "Predicate" && it.cppName == "geo::Predicate" &&
                it.returnType.toString() == "bool" &&
                it.argTypes.map { a -> a.toString() } == listOf("double")
        } == true,
        "got $predicate"
    )
    val scanArg = geoFns.find { it.name == "scan" }?.args?.singleOrNull()?.type
    check(
        "scan(Predicate) param carries the same WrappedFunctionPointer",
        (scanArg as? WrappedFunctionPointer)?.cppName == "geo::Predicate",
        "got $scanArg"
    )

    // -- Brick 6: default arguments. hasDefault = ParmVarDecl::hasDefaultArg() (the
    // first-class fact replacing the libclang cursor-children token heuristic);
    // defaultValue strings are pinned to the libclang token-join contract
    // (ModelFactories.defaultValue: tokenSpellings().joinToString("")).
    fun argOf(method: String, index: Int = 0) =
        dMethods.find { it.name == method }?.args?.getOrNull(index)
    check(
        "int literal default: resize(int size = 5) -> hasDefault, \"5\"",
        argOf("resize")?.let { it.hasDefault && it.defaultValue == "5" } == true,
        "got ${argOf("resize")?.hasDefault}/${argOf("resize")?.defaultValue}"
    )
    check(
        "negative default: shift(int delta = -1) -> \"-1\" (unary expr tokens joined)",
        argOf("shift")?.let { it.hasDefault && it.defaultValue == "-1" } == true,
        "got ${argOf("shift")?.defaultValue}"
    )
    check(
        "enum-constant default: paint(Color tint = RED) -> \"RED\" on a WrappedEnumType arg",
        argOf("paint")?.let {
            it.hasDefault && it.defaultValue == "RED" && it.type is WrappedEnumType
        } == true,
        "got ${argOf("paint")?.defaultValue} type=${argOf("paint")?.type}"
    )
    check(
        "nullptr default: fill(Shape* target = nullptr) -> \"nullptr\"",
        argOf("fill")?.let { it.hasDefault && it.defaultValue == "nullptr" } == true,
        "got ${argOf("fill")?.defaultValue}"
    )
    check(
        "constructed default: blend(Palette p = Palette()) -> the call spelling \"Palette()\"",
        argOf("blend")?.let { it.hasDefault && it.defaultValue == "Palette()" } == true,
        "got ${argOf("blend")?.defaultValue}"
    )
    // Multi-default trailing run: the shape the trailing-defaulted-omit shortcut
    // (ModelResolution's isOmittableDefault subList check) consumes — a REQUIRED head arg
    // plus omittable defaulted tails. isOmittableDefault is model-side; it holds here
    // because hasDefaultArg() never fires on the false-default type shapes (#36/#41).
    check(
        "configure(int a, int b = 1, Shape* c = nullptr): a required, b \"1\", c \"nullptr\"",
        argOf("configure", 0)?.let { !it.hasDefault && it.defaultValue == null } == true &&
            argOf("configure", 1)?.let { it.hasDefault && it.defaultValue == "1" } == true &&
            argOf("configure", 2)
            ?.let { it.hasDefault && it.defaultValue == "nullptr" } == true,
        "got ${dMethods.find { it.name == "configure" }?.args
            ?.map { "${it.hasDefault}/${it.defaultValue}" }}"
    )
    check(
        "configure's defaulted tail is omittable (the resolution shortcut's contract)",
        dMethods.find { it.name == "configure" }?.args?.drop(1)
            ?.all { it.isOmittableDefault } == true
    )
    // No false positives anywhere else: every pre-brick-6 arg stays non-defaulted
    // (hasDefaultArg() is authoritative; nothing to repair on this path).
    check(
        "no spurious hasDefault on non-defaulted args (freeFunction, describe, onEvent)",
        freeFn?.args?.none { it.hasDefault } == true &&
            sceneMethods.find { it.name == "describe" }?.args?.none { it.hasDefault } == true &&
            dMethods.find { it.name == "onEvent" }?.args?.none { it.hasDefault } == true
    )

    // -- Phase D (#46): the remaining typedef-reducer mirrors + defaultType capture.
    // pointerTypedefElement: a DEPENDENT `pointer` alias (unique_ptr's shape — the
    // underlying routes through a trait scope) reduces to element_type* so get()
    // survives; the `using element_type` sibling is found through the dual-kind filter.
    val smartPtr = holders.find { it.name == "SmartPtr" }
    val smartPointerTypedef = smartPtr?.children?.filterIsInstance<WrappedTypedef>()
        ?.find { it.name == "pointer" }
    check(
        "SmartPtr's dependent `pointer` typedef reduces to element_type*: *(template<T>)",
        (smartPointerTypedef?.targetType as? WrappedModifiedType)?.let {
            it.modifier == "*" && (it.baseType as? WrappedTemplateRef)?.target == "T"
        } == true,
        "got ${smartPointerTypedef?.targetType}"
    )
    check(
        "SmartPtr::get() survives: const-method pointer return gains the G8 outer const",
        smartPtr?.methods?.find { it.name == "get" }
            ?.returnType?.toString() == "const template<T>*",
        "got ${smartPtr?.methods?.find { it.name == "get" }?.returnType}"
    )
    // The negative gate: a CONCRETE pointer typedef must fall through to normal collapse.
    val rawHolder = tu.children.filterIsInstance<WrappedClass>().find { it.name == "RawHolder" }
    check(
        "RawHolder's CONCRETE `typedef int* pointer` is NOT rewritten: collapses to 'int*'",
        rawHolder?.children?.filterIsInstance<WrappedTypedef>()?.singleOrNull()
            ?.targetType?.toString() == "int*",
        "got ${rawHolder?.children?.filterIsInstance<WrappedTypedef>()}"
    )
    // assocTypedefElement, key→value shape (unordered_map's): dependent key_type/
    // mapped_type reduce to the enclosing template's params 0/1.
    val miniMap = holders.find { it.name == "MiniMap" }
    val miniMapTypedefs = miniMap?.children?.filterIsInstance<WrappedTypedef>().orEmpty()
    check(
        "MiniMap's dependent key_type/mapped_type reduce to params 0/1 (assoc map shape)",
        (miniMapTypedefs.find { it.name == "key_type" }?.targetType as? WrappedTemplateRef)
            ?.target == "K" &&
            (miniMapTypedefs.find { it.name == "mapped_type" }?.targetType as? WrappedTemplateRef)
                ?.target == "V",
        "got ${miniMapTypedefs.map { "${it.name}=${it.targetType}" }}"
    )
    val atMethod = miniMap?.methods?.find { it.name == "at" }
    check(
        "MiniMap::at survives: 'template<V>&' return, 'const template<K>&' arg",
        atMethod?.returnType?.toString() == "template<V>&" &&
            atMethod?.args?.singleOrNull()?.type?.toString() == "const template<K>&",
        "got ret=${atMethod?.returnType} args=${atMethod?.args}"
    )
    // assocTypedefElement, key-only shape (set's): key_type AND the self-referential
    // value_type both reduce to param 0.
    val miniSet = holders.find { it.name == "MiniSet" }
    val miniSetTypedefs = miniSet?.children?.filterIsInstance<WrappedTypedef>().orEmpty()
    check(
        "MiniSet's dependent key_type AND value_type reduce to the key param (key-only shape)",
        (miniSetTypedefs.find { it.name == "key_type" }?.targetType as? WrappedTemplateRef)
            ?.target == "K" &&
            (miniSetTypedefs.find { it.name == "value_type" }?.targetType as? WrappedTemplateRef)
                ?.target == "K",
        "got ${miniSetTypedefs.map { "${it.name}=${it.targetType}" }}"
    )
    check(
        "MiniSet::insert(const value_type&) arg carries the reduced shape",
        miniSet?.methods?.find { it.name == "insert" }?.args?.singleOrNull()
            ?.type?.toString() == "const template<K>&",
        "got ${miniSet?.methods?.find { it.name == "insert" }?.args}"
    )
    // The D3 dependent-typename leaf (unordered_map/set's hasher shape): a member
    // typedef whose underlying is a DependentNameType NO reducer claims keeps the
    // WRITTEN `typename <qual>::<name>` spelling as a WrappedTypename — the
    // deterministic mirror of TypeFactories' `WrappedType(spelling)` else-branch
    // (the libclang cache's order-dependent remappings are ledger-accepted, D3).
    val miniHash = holders.find { it.name == "MiniHash" }
    val miniHashTypedefs = miniHash?.children?.filterIsInstance<WrappedTypedef>().orEmpty()
    check(
        "MiniHash's dependent aliases keep the WRITTEN typename spelling, '<'-truncated " +
            "(template-id qualifier collapses; bare-typedef qualifier survives whole)",
        miniHashTypedefs.find { it.name == "hasher" }?.targetType
            ?.toString() == "typename!<HashTraits>" &&
            miniHashTypedefs.find { it.name == "key_equal" }?.targetType
            ?.toString() == "typename!<traits_type::hash_fn>",
        "got ${miniHashTypedefs.map { "${it.name}=${it.targetType}" }}"
    )
    check(
        "MiniHash::rehash/requal args carry the typename leaves (not unresolveable)",
        miniHash?.methods?.find { it.name == "rehash" }?.args?.singleOrNull()
            ?.type?.toString() == "const typename!<HashTraits>&" &&
            miniHash?.methods?.find { it.name == "requal" }?.args?.singleOrNull()
            ?.type?.toString() == "const typename!<traits_type::hash_fn>&",
        "got rehash=${miniHash?.methods?.find { it.name == "rehash" }?.args} " +
            "requal=${miniHash?.methods?.find { it.name == "requal" }?.args}"
    )
    // WrappedTemplateParam.defaultType: the cursor-walk residue shapes, pinned against
    // the libclang oracle (see TypeBuilder.defaultTypeResidue).
    val defBox = holders.find { it.name == "DefBox" }
    check(
        "DefBox param T carries NO default; param A captures 'unresolveable<template<T>>'",
        defBox?.templateArgs?.getOrNull(0)?.defaultType == null &&
            defBox?.templateArgs?.getOrNull(1)?.defaultType
            ?.toString() == "unresolveable<template<T>>",
        "got ${defBox?.templateArgs?.map { "${it.name}=${it.defaultType}" }}"
    )
    check(
        "DefPair param U = T: defaultType is the self-residue 'template<T><template<T>>'",
        holders.find { it.name == "DefPair" }?.templateArgs?.getOrNull(1)?.defaultType
            ?.toString() == "template<T><template<T>>",
        "got ${holders.find { it.name == "DefPair" }?.templateArgs
            ?.map { "${it.name}=${it.defaultType}" }}"
    )
    // D1b (#46): a dependent initializer_list element that is a bare ref to an enclosing
    // template param — written DIRECTLY (basic_string's ctor/op=/op+=/append/assign shape)
    // or through a plain TYPEDEF-TO-PARAM (vector's `typedef _Tp value_type` shape) —
    // decodes faithfully as the NAME-tref the libclang survivor spells
    // (`std::initializer_list<template<_CharT>>` in the live oracle's --dumpParsedModel),
    // so the member survives with a converging uniqueCName. The negative half of the
    // discrimination (assoc-reduced D1c elements + non-tref dependent shapes stay
    // UNRESOLVABLE) is pinned by the parity ratchet: set/unordered_set's libclang
    // survivors are USR-keyed, so any cpp-side leak-through grows their unit diffs.
    val miniStr = holders.find { it.name == "MiniStr" }
    check(
        "MiniStr::append(initializer_list<T>): the DIRECT dependent element survives " +
            "as the name-tref shape (D1b)",
        miniStr?.methods?.find { it.name == "append" }?.args?.singleOrNull()
            ?.type?.toString() == "std::initializer_list<template<T>>",
        "got ${miniStr?.methods?.find { it.name == "append" }?.args}"
    )
    check(
        "MiniStr::assignAll(initializer_list<value_type>): the typedef-to-param element " +
            "survives as the same name-tref shape (D1b)",
        miniStr?.methods?.find { it.name == "assignAll" }?.args?.singleOrNull()
            ?.type?.toString() == "std::initializer_list<template<T>>",
        "got ${miniStr?.methods?.find { it.name == "assignAll" }?.args}"
    )

    check("JSON is non-empty", json.length > 2)

    if (failures > 0) fail("$failures self-check(s) failed")
    println("krapper_parse: ALL SELF-CHECKS PASSED")
}

/**
 * #101 — NONCOPYABLE SPECIAL-MEMBER DETERMINISM GUARD. Parse [NONCOPYABLE_FIXTURE]
 * [NONCOPYABLE_PARSE_COUNT] times (each a SEPARATE buildASTWithArgs / ASTContext) and assert:
 *  1. every parse's full serialized model is byte-identical — the front-end is deterministic
 *     parse-to-parse for a NonCopyable type (the exact #101 acceptance);
 *  2. the `NC` template PATTERN carries BOTH special members on EVERY parse — a copy
 *     constructor AND a copy assignment — so the set is COMPLETE and never the report's
 *     "one parse the ctor, another the assignment". This holds because the members ride the
 *     template pattern's fixed source-order `decls()`, not a specialization's lazily-
 *     instantiated `decls()` (ModelBuilder skips the specialization — see its #101 note).
 *
 * Note this guard is at the FRONT-END (model) boundary on purpose: the un-instantiable copy
 * ops are a body-only `static_assert`, invisible to every copy-constructibility AST query
 * (`isDeleted()` / `is_copy_constructible` are FALSE/true respectively — the trap only fires
 * if the body is instantiated), so the front-end cannot faithfully SUPPRESS them; it can only
 * emit them DETERMINISTICALLY (the consumer drops the un-compilable pair by uniqueCName).
 */
private fun MemScope.noncopyableDeterminismCheck() {
    val parseArgs = "-std=c++17\n-resource-dir=" + commandOutput("clang++ -print-resource-dir")
    val models = (1..NONCOPYABLE_PARSE_COUNT).map { i ->
        parseToWrappedTU(NONCOPYABLE_FIXTURE, "noncopyable_$i.cc", parseArgs)
    }
    val jsons = models.map { ModelIo.encodeToString(it) }
    val divergent = jsons.withIndex().filter { it.value != jsons.first() }.map { it.index + 1 }
    check(
        "$NONCOPYABLE_PARSE_COUNT parses of the NonCopyable fixture are byte-identical (#101)",
        divergent.isEmpty(),
        "parse(s) $divergent diverged from parse 1"
    )
    // The special-member set of the NC PATTERN, per parse: the copy-ctor + copy-assignment
    // signal that survives substitution into NC<Value>. Both must be present on EVERY parse.
    fun specialMembers(tu: WrappedTU): Pair<Boolean, Boolean> {
        val members = tu.children.filterIsInstance<WrappedTemplate>()
            .find { it.name == "NC" }?.children.orEmpty()
        val hasCopyCtor = members.filterIsInstance<WrappedConstructor>()
            .any { it.isCopyConstructor }
        val hasCopyAssign = members.filterIsInstance<WrappedMethod>()
            .any { it !is WrappedConstructor && it !is WrappedDestructor && it.name == "operator=" }
        return hasCopyCtor to hasCopyAssign
    }
    val sets = models.map { specialMembers(it) }
    check(
        "NC pattern carries BOTH copy ctor and copy assignment on every parse (#101)",
        sets.all { it == (true to true) },
        "per-parse (copyCtor, copyAssign): $sets"
    )
    if (failures > 0) fail("$failures NonCopyable-determinism check(s) failed")
    println("krapper_parse: NONCOPYABLE DETERMINISM CHECK PASSED ($NONCOPYABLE_PARSE_COUNT parses)")
}

/**
 * #45 brick 3 — INSTANTIATION-FORCING HANDOFF EMIT. The cpp front-end's half of the
 * `--frontend=cpp --instantiate` flow:
 *  1. write the forcing fixture (bag.h) so krapper_gen consumes the same bytes;
 *  2. parse it (the BASE model: the args-bearing parse, since the fixture #includes
 *     <vector>) -> bag_model.json (--parsedModel);
 *  3. for each instantiation target, synthesize the SAME forcing header the libclang path
 *     synthesizes (ForcingHeader, :krapper_model), parse it as a SEPARATE translation
 *     unit, and emit its model -> <forceName>.json (--forcingModel).
 * Identity note: each payload is self-contained — cpp:<id> identities are per-ASTContext
 * (Decl::getID() is NOT stable across buildASTFromCode calls), but nothing references
 * identity ACROSS payloads; krapper_gen's forcing flow merges the trees by structural
 * identity (type-string keys: alreadyBoundKeys / tracker maps / last-wins dedup), the same
 * keys the libclang path's USR memo collapses to one instance.
 */
private fun MemScope.handoffEmit(dir: String) {
    val fixturePath = "$dir/bag.h"
    writeFile(fixturePath, FORCING_FIXTURE_HEADER + "\n")
    // The std-header-bearing parses need real driver arguments: the tooling driver can't
    // compute its resource dir (its "binary" is the virtual "clang-tool"), so the builtin
    // headers (<stddef.h> etc., behind <vector>) only resolve with an explicit
    // -resource-dir; system C++ headers are found by the driver's normal GCC detection.
    // c++17 pins the same standard the generate step parses/compiles under.
    val parseArgs = "-std=c++17\n-resource-dir=" + commandOutput("clang++ -print-resource-dir")
    val baseTu = parseToWrappedTU(FORCING_FIXTURE_HEADER, "bag.cc", parseArgs)
    writeFile("$dir/bag_model.json", ModelIo.encodeToString(baseTu))
    val emitted = mutableListOf("bag.h", "bag_model.json")
    for (target in FORCING_TARGETS) {
        val forceName = ForcingHeader.forceName(target)
        val content = ForcingHeader.contentFor(target, listOf(fixturePath))
        val forcingTu = parseToWrappedTU(content, "$forceName.cc", parseArgs)
        writeFile("$dir/$forceName.json", ModelIo.encodeToString(forcingTu))
        emitted.add("$forceName.json")
    }
    println("krapper_parse: handoff-emit -> $dir/{${emitted.joinToString(", ")}}")
}

/**
 * --parity-emit (#46) — the cpp front-end's MODEL EMIT for the production sync. The
 * kplusplus gradle plugin's kplusplusSync drives this for ANY module flipped to the cpp
 * front-end: generalizing handoffEmit from the fixed bag fixture to an ARBITRARY user
 * header + instantiation specs (e.g. featuregen's strings_feature.h + its worklist). One
 * invocation per payload, so a crash on one spec's parse never takes the others down (the
 * caller records it as a per-spec emit failure):
 *  - no specs: parse [headerPath]'s bytes (the base model) -> base_model.json, print
 *    `PARITY_BASE <path>`;
 *  - per spec: synthesize the SAME ForcingHeader the libclang path synthesizes
 *    (#include-ing [headerPath] by the same path string krapper_gen gets via --header),
 *    parse it, emit <forceName>.json, print `PARITY_MODEL <spec>=<path>` — the line the
 *    gradle task turns into krapper_gen's --forcingModel argument.
 * [std] pins the SAME standard featuregen's sync parses under (krapper_gen's default).
 *
 * [trailing] mixes the module's extra include dirs (each a `-I<dir>` token, threaded by the
 * gradle plugin from `kplusplus { headerDirectory(...) }`) with the instantiation specs; we
 * partition on the `-I` prefix (C++ type specs never start with `-I`) and fold the include
 * flags into the driver args so cross-directory quote-includes resolve in BOTH the base and
 * per-spec forcing parses — exactly as krapper_gen's wrapper compile sees them.
 */
private fun MemScope.parityEmit(
    dir: String,
    headerPath: String,
    std: String,
    trailing: List<String>
) {
    val (includeFlags, specs) = trailing.partition { it.startsWith("-I") }
    val parseArgs = (
        listOf("-std=$std", "-resource-dir=" + commandOutput("clang++ -print-resource-dir")) +
            includeFlags
        ).joinToString("\n")
    if (specs.isEmpty()) {
        val baseTu = parseToWrappedTU(readFile(headerPath), "parity_base.cc", parseArgs)
        val path = "$dir/base_model.json"
        writeFile(path, ModelIo.encodeToString(baseTu))
        println("PARITY_BASE $path")
        return
    }
    for (spec in specs) {
        val forceName = ForcingHeader.forceName(spec)
        val content = ForcingHeader.contentFor(spec, listOf(headerPath))
        val tu = parseToWrappedTU(content, "$forceName.cc", parseArgs)
        val path = "$dir/$forceName.json"
        writeFile(path, ModelIo.encodeToString(tu))
        println("PARITY_MODEL $spec=$path")
    }
}

// Parse [code] with the '\n'-joined driver [args] (kppbridge.buildASTWithArgs — see
// clang_slice.h) and construct the WrappedTU. The virtual filename must spell a C++
// extension (same rule as the buildASTFromCode call in main).
private fun MemScope.parseToWrappedTU(code: String, filename: String, args: String) =
    buildWrappedTU(
        buildASTWithArgs(code, filename, args)
            ?.getASTContext()
            ?.getTranslationUnitDecl()
            ?: fail("buildASTWithArgs failed for $filename")
    )

// First line of [cmd]'s stdout (popen), trimmed. Used for `clang++ -print-resource-dir`.
private fun commandOutput(cmd: String): String = memScoped {
    val fp = popen(cmd, "r") ?: fail("popen failed: $cmd")
    val buffer = allocArray<ByteVar>(BUFFER_SIZE)
    val line = fgets(buffer, BUFFER_SIZE, fp)?.toKString().orEmpty().trim()
    pclose(fp)
    if (line.isEmpty()) fail("no output from: $cmd") else line
}

private const val BUFFER_SIZE = 1024

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

internal fun fail(message: String): Nothing {
    println("krapper_parse: FAIL — $message")
    exitProcess(1)
}
