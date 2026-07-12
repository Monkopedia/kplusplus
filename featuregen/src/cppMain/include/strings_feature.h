#ifndef STRINGS_FEATURE_H
#define STRINGS_FEATURE_H

#include <string>
#include <cstddef>

// C typedef-alias round-trip (PR-ptrdiff / PR-wchar, generator-backed): verifies
// the generated binding preserves the platform.posix.<name> alias (ptrdiff_t ->
// Long, wchar_t -> Int) instead of collapsing to the bare integer.
struct AliasFeature {
    static ptrdiff_t echoDiff(ptrdiff_t d) { return d; }
    static wchar_t echoWide(wchar_t c) { return c; }
};

// Unscoped (C-style) enum round-trip (EN-unscoped).
enum Direction { North = 0, East = 1, South = 2, West = 3 };

struct DirectionFeature {
    static Direction turnRight(Direction d) { return static_cast<Direction>((d + 1) % 4); }
    static int toInt(Direction d) { return d; }
    static Direction fromInt(int i) { return static_cast<Direction>(i % 4); }
};

// Scoped enum round-trip (EN-scoped): `enum class` with explicit underlying int.
enum class Color : int { Red = 0, Green = 1, Blue = 2 };

struct EnumFeature {
    static Color next(Color c) { return static_cast<Color>((static_cast<int>(c) + 1) % 3); }
    static int toInt(Color c) { return static_cast<int>(c); }
    static Color fromInt(int i) { return static_cast<Color>(i); }
};

// OP-compound-assign-enum (#105 Family 2): compound-assignment operators returning
// `T&` where T reduces to a value-type at the boundary. `Flag` is an unscoped enum
// (reduces to its underlying int); the free `operator|=/&=/^=` mutate the lvalue and
// return `Flag&`. On the unfixed generator the reference-return of a value-reduced
// type is mishandled: it is bound as a by-value ENUM_RETURN (the extern hands back a
// pointer) and the C wrapper is UB (`return &(local)`). ByteFlags exercises the same
// shape as MEMBER operators so a Kotlin binding is generated + behaviourally tested.
enum Flag { FlagNone = 0, FlagA = 1, FlagB = 2, FlagC = 4 };

struct ByteFlags {
    Flag bits;
    ByteFlags() : bits(FlagNone) {}
    // member compound-assignment operators returning `ByteFlags&` where the operand /
    // stored state is a value-reduced enum. Returns the receiver so calls chain.
    ByteFlags& operator|=(Flag f) { bits = static_cast<Flag>(bits | f); return *this; }
    ByteFlags& operator&=(Flag f) { bits = static_cast<Flag>(bits & f); return *this; }
    ByteFlags& operator^=(Flag f) { bits = static_cast<Flag>(bits ^ f); return *this; }
    int value() const { return (int) bits; }
};

// A FREE-enum compound-assignment returning the value-reduced enum reference itself
// (the exact <ios> `_Ios_Fmtflags& operator|=` shape). Exposed via a probe struct's
// static methods so krapper wraps them.
inline Flag& flagOrEq(Flag& a, Flag b) { a = static_cast<Flag>(a | b); return a; }

struct FlagProbe {
    inline static Flag state = FlagNone;
    static void reset() { state = FlagNone; }
    // returns `Flag&` (value-reduced enum reference) — the Family-2 return shape.
    static Flag& orInto(Flag b) { return flagOrEq(state, b); }
    static int stateVal() { return (int) state; }
};

// A plain user struct, used as the element type of std::vector<Point> to test
// "user type inside a generated container" (CV-elem-class).
struct Point {
    int x;
    int y;
    Point() : x(0), y(0) {}
    Point(int x_, int y_) : x(x_), y(y_) {}
};

// A C++ method whose name IS a Kotlin hard keyword — the generated binding must
// back-tick-escape it or the Kotlin won't parse.
struct KeywordMethods {
    int value;
    int in() const { return value; }
    bool is() const { return value != 0; }
};

// Plain user-defined class surface (UC-* rows): default + args ctors, a void
// mutator, const value-returning methods, a static factory returning by value,
// and equality / arithmetic operators. All inline so the wrapper carries impls.
struct Widget {
    int count;
    double scale;
    Widget() : count(0), scale(0.0) {}
    Widget(int count_, double scale_) : count(count_), scale(scale_) {}
    void reset() { count = 0; scale = 0.0; }
    int compute(int x) const { return count + x; }
    double scaled() const { return count * scale; }
    static Widget make(int count_) { return Widget(count_, 1.0); }
    bool operator==(const Widget& o) const { return count == o.count && scale == o.scale; }
    Widget operator+(const Widget& o) const { return Widget(count + o.count, scale + o.scale); }
};

// Header-only std::string feature surface. Inline functions + an inline static
// inspector field (C++17) so the generated wrapper carries the definitions with
// no separate library to link.
struct StringFeature {
    inline static std::string last;

    static void take(const std::string& s) { last = s; }
    static std::size_t lastSize() { return last.size(); }
    static int lastByteAt(std::size_t i) {
        return i < last.size() ? (int) (unsigned char) last[i] : -1;
    }
    static void reset() { last.clear(); }

    static std::string produce() { return std::string("produced!"); }
    static std::string echo(const std::string& s) { return s; }
};

// std::string_view (C++17) is not exercised here. The cppStandard knob can bump
// the parse+compile to c++17, but krapper_gen still doesn't wrap string_view as
// a type (take() would be silently dropped), and c++17 separately drops
// std::string's const char* constructor. Tracked as ST-stringview-in 🔴.

// std::wstring — wide string round-trip.
struct WStringFeature {
    inline static std::wstring last;

    static void take(const std::wstring& w) { last = w; }
    static std::size_t lastSize() { return last.size(); }
    static long codeAt(std::size_t i) {
        return i < last.size() ? (long) last[i] : -1;
    }
    static void reset() { last.clear(); }

    static std::wstring echo(const std::wstring& w) { return w; }
};

// Operator overloads on a user-defined value type (OP-* rows). All inline. Note:
// operator() is deliberately omitted — it SIGABRTs krapper_gen during sync
// (operator() isn't in ALL_OPERATORS and Operator.from has no guard). Tracked.
struct Vec2 {
    double x, y;
    Vec2() : x(0.0), y(0.0) {}
    Vec2(double x_, double y_) : x(x_), y(y_) {}

    bool operator==(const Vec2& o) const { return x == o.x && y == o.y; }   // OP-eq
    bool operator!=(const Vec2& o) const { return !(*this == o); }          // OP-neq
    Vec2 operator+(const Vec2& o) const { return Vec2(x + o.x, y + o.y); }  // OP-plus
    Vec2 operator-(const Vec2& o) const { return Vec2(x - o.x, y - o.y); }  // OP-minus
    Vec2 operator*(double scalar) const { return Vec2(x * scalar, y * scalar); } // OP-scale
    double operator[](int i) const { return i == 0 ? x : y; }              // OP-index
    bool operator<(const Vec2& o) const {                                  // OP-compare
        return x < o.x || (x == o.x && y < o.y);
    }
    Vec2& operator=(const Vec2& o) { x = o.x; y = o.y; return *this; }      // OP-assign
    Vec2& operator+=(const Vec2& o) { x += o.x; y += o.y; return *this; }   // OP-pluseq
    // OP-compound-assign (T-op): compound assignments NOT covered by
    // ALL_OPERATORS. On the unfixed generator their C wrapper names leak the
    // raw `-` / `/` / `%` symbol and truncate to a colliding `..._operator`.
    Vec2& operator-=(const Vec2& o) { x -= o.x; y -= o.y; return *this; }   // OP-minuseq
    Vec2& operator/=(double s) { x /= s; y /= s; return *this; }            // OP-diveq
    Vec2& operator%=(int m) {                                               // OP-modeq
        x = (double)(((int)x) % m);
        y = (double)(((int)y) % m);
        return *this;
    }
    Vec2 operator-() const { return Vec2(-x, -y); }                        // OP-unary-minus
    explicit operator double() const { return x * x + y * y; }             // OP-convert
    double operator()(double s) const { return x * s + y * s; }            // OP-call → operator_call
};

// OP-eq-defaulted (T-op): a C++20 `= default`-ed operator==, which the compiler
// GUARANTEES is a memberwise comparison over ALL members. krapper trusts that
// guarantee: it binds this to a real Kotlin `equals` (so `a == b` runs the C++
// `==`) PAIRED with a field-fold `hashCode` — the contract is provably sound
// because equal objects have all members equal. Contrast Vec2's hand-written
// `operator==`, which krapper can't prove memberwise so it falls back to an
// identity `equals` (pointer) + a `valueEquals` for the value comparison.
struct VecEq {
    double x, y;
    VecEq() : x(0.0), y(0.0) {}
    VecEq(double x_, double y_) : x(x_), y(y_) {}
    bool operator==(const VecEq& o) const = default;   // OP-eq-defaulted
};

// OB-* : passing/returning user objects across the boundary. A small aggregate
// (Point2) + a struct of static methods, one per convention (krapper wraps only
// struct/class members, not namespace-scope free functions).
struct Point2 {
    int x;
    int y;
    Point2() : x(0), y(0) {}
    Point2(int x_, int y_) : x(x_), y(y_) {}
    bool operator==(const Point2& o) const { return x == o.x && y == o.y; }
};

struct PassObj {
    inline static int describeCount = 0;
    inline static int lastDescribeX = 0;
    inline static int lastDescribeY = 0;
    inline static int lastSum = 0;
    inline static bool shiftSeen = false;
    inline static bool zeroSeen = false;

    static Point2 makePoint(int x, int y) { return Point2(x, y); }
    static void describePoint(Point2 p) {
        describeCount++;
        lastDescribeX = p.x;
        lastDescribeY = p.y;
        p.x = -999;
        p.y = -999;
    }
    static int sumCoords(const Point2& p) {
        lastSum = p.x + p.y;
        return p.x + p.y;
    }
    static void shiftPoint(Point2* p, int dx, int dy) {
        shiftSeen = true;
        if (p) { p->x += dx; p->y += dy; }
    }
    static void zeroPoint(Point2& p) {
        zeroSeen = true;
        p.x = 0;
        p.y = 0;
    }
    static Point2 clonePoint(const Point2& p) { return Point2(p.x, p.y); }
    static Point2* borrowGlobal() {
        static Point2 g(11, 22);
        return &g;
    }
    static Point2* allocPoint(int x, int y) { return new Point2(x, y); }
    static void freePoint(Point2* p) { delete p; }
    static Point2& globalRef() {
        static Point2 g(33, 44);
        return g;
    }
    static Point2* maybePoint(bool give) {
        static Point2 g(55, 66);
        return give ? &g : nullptr;
    }
    static int  describeCountVal() { return describeCount; }
    static int  lastDescribeXVal() { return lastDescribeX; }
    static int  lastDescribeYVal() { return lastDescribeY; }
    static int  sumResultVal()     { return lastSum; }
    static bool shiftSeenVal()     { return shiftSeen; }
    static bool zeroSeenVal()      { return zeroSeen; }
};

// OB-return-ptr-owned / IH-destruction : caller-controlled destruction. Every
// `T*`/`T&` return is a NON-owning wrapper, so the generated wrapper surfaces two
// caller-choice methods on any class WITH a destructor:
//   * `dispose()` — run `~Tracked()` now.
//   * `owned()`   — register `memScope.defer { ~Tracked() }` and return `this`.
// `Tracked`'s destructor has an OBSERVABLE side effect (it bumps a static counter)
// so a test can prove the destructor actually ran. `makeTracked()` heap-`new`s a
// raw `Tracked*` returned non-owning — the caller decides its fate.
struct Tracked {
    inline static int destroyed = 0;
    int id;
    Tracked() : id(0) {}
    Tracked(int id_) : id(id_) {}
    ~Tracked() { destroyed++; }
    int getId() const { return id; }
    static int destroyedCount() { return destroyed; }
    static void resetDestroyed() { destroyed = 0; }
    static Tracked* makeTracked(int id_) { return new Tracked(id_); }
};

// DA-* : default arguments & overloading (static methods of a probe struct).
#include <initializer_list>
#include <cstdarg>

struct DefaultArgs {
    inline static int lastA = 0;
    inline static int lastB = 0;
    inline static int lastC = 0;

    static int add(int a, int b = 10, int c = 0) {
        lastA = a; lastB = b; lastC = c;
        return a + b + c;
    }
    static int recvA() { return lastA; }
    static int recvB() { return lastB; }
    static int recvC() { return lastC; }

    static int process(int x) { return x * 1; }
    static int process(int x, int y) { return x + y; }
    static int process(double x) { return (int)(x * 10.0); }

    static int format(int s, int width = 0, char fill = ' ') {
        return width <= 0 ? s : width;
    }

    static int sumIlist(std::initializer_list<int> vals) {
        int t = 0;
        for (int v : vals) t += v;
        return t;
    }

    static int sumN(int count, ...) {
        va_list ap;
        va_start(ap, count);
        int t = 0;
        for (int i = 0; i < count; i++) t += va_arg(ap, int);
        va_end(ap);
        return t;
    }
};

// DA-const-overload: const vs non-const overload pair (the __<argtypes> suffix is
// keyed on param types, not this-constness).
struct Buffer {
    char data[4];
    Buffer() { data[0] = 'a'; data[1] = 'b'; data[2] = 'c'; data[3] = 'd'; }
    char& at(int i) { return data[i]; }
    const char& at(int i) const { return data[i]; }
    int getAt(int i) const { return (int)(unsigned char)data[i]; }
};

// NS-* : namespaces & nested types. (Anonymous namespace omitted — it emits an
// invalid empty `package ` line; see Known issues.)
namespace geo {
    struct Vec {
        double x, y;
        Vec() : x(0.0), y(0.0) {}
        Vec(double x_, double y_) : x(x_), y(y_) {}
        double lengthSq() const { return x * x + y * y; }
        static Vec make(double x_, double y_) { return Vec(x_, y_); }
    };
}

namespace geo::detail {
    struct Impl {
        int tag;
        Impl() : tag(0) {}
        Impl(int t) : tag(t) {}
        int doubled() const { return tag * 2; }
    };
}

struct Outer {
    struct Inner {
        int v;
        Inner() : v(0) {}
        Inner(int v_) : v(v_) {}
        int get() const { return v; }
    };
    int who;
    Outer() : who(7) {}
    int whoVal() const { return who; }
};

namespace acolor {
    struct Tag {
        int r, g, b;
        Tag() : r(0), g(0), b(0) {}
        Tag(int r_, int g_, int b_) : r(r_), g(g_), b(b_) {}
        int sum() const { return r + g + b; }
    };
}
namespace bflavor {
    struct Tag {
        int sweet, sour;
        Tag() : sweet(0), sour(0) {}
        Tag(int s_, int o_) : sweet(s_), sour(o_) {}
        int total() const { return sweet + sour; }
    };
}

// IH-* : single (one base) public inheritance — flatten inherited public members
// onto the derived binding (decision A). No virtual destructor / operator() here.
struct Base {
    int bx;
    Base() : bx(0) {}
    void move(int d) { bx += d; }            // non-virtual inherited
    int baseOnly() const { return bx; }      // base-only
    const char* label() const { return "base"; }   // non-virtual, SHADOWED below
    virtual int area() const { return 0; }   // virtual, overridden below
};
struct Derived : Base {
    int dy;
    Derived() : dy(0) {}
    int derivedOnly() const { return dy; }
    const char* label() const { return "derived"; }  // non-virtual SHADOW -> expect `derivedLabel`
    int area() const override { return 42; }          // override -> stays `area`
};

// IH-upcast / IH-virtual-dispatch : single public inheritance with a polymorphic
// base. `Shape` has a virtual area()/virtual dtor; Circle/Rectangle override area().
// ShapeProbe's static methods take/return a `Shape*` so a Circle can be passed where
// a Shape is wanted (upcast) and area() called through the base pointer vtable-
// dispatches to the derived override (returns the derived's area, not the base's 0).
struct Shape {
    virtual ~Shape() {}
    virtual double area() const { return 0.0; }
    int sides() const { return 0; }   // non-virtual base-only helper
};
struct Circle : Shape {
    double r;
    Circle() : r(0.0) {}
    Circle(double r_) : r(r_) {}
    double area() const override { return 3.14159265358979 * r * r; }
};
struct Rectangle : Shape {
    double w, h;
    Rectangle() : w(0.0), h(0.0) {}
    Rectangle(double w_, double h_) : w(w_), h(h_) {}
    double area() const override { return w * h; }
};
struct ShapeProbe {
    // Upcast: accept any Shape (a Circle passed here) and read its area via the
    // virtual dispatch through the base pointer.
    static double areaOf(const Shape* s) { return s ? s->area() : -1.0; }
    static double areaOfRef(const Shape& s) { return s.area(); }
    // Return a Shape* (really a Circle) so the caller can upcast-receive it and
    // still dispatch area() to the Circle override.
    static Shape* biggest(Shape* a, Shape* b) {
        if (!a) return b;
        if (!b) return a;
        return a->area() >= b->area() ? a : b;
    }
};

// CV-getat-base-elem (#7 item 3): a `get`/`at`-keyed index container whose ELEMENT
// type is a BASE class (`Shape*`, which has a generated `ShapeApi` interface). The
// generator upcasts the `at(...)` return to `ShapeApi?`, but the synthesized
// `iterator()`'s `next()` declares the CONCRETE element type — so WITHOUT the item-3
// fix the emitted `next(): Shape? = at(i): ShapeApi?` doesn't typecheck and the
// featuregen build fails to compile. With the fix the `at`/`get` get keeps the
// concrete element type (matching `operator[]`), so the container binds and the stored
// concrete Shapes round-trip (area() virtual-dispatches through the element).
//
// Index keyed by `at`/`get` (NOT `operator[]`) with a `size_t` size + `size_t` index,
// so detectIndexIterable selects the `at` fallback rather than the IND operator path.
struct ShapeBag {
    Shape* items[3];
    std::size_t n;
    ShapeBag() : n(0) { items[0] = items[1] = items[2] = nullptr; }
    void add(Shape* s) { if (n < 3) items[n++] = s; }
    std::size_t size() const { return n; }
    Shape* at(std::size_t i) const { return i < n ? items[i] : nullptr; }
    Shape* get(std::size_t i) const { return at(i); }
};

// IH-flatten (3-level chain): Animal <- Dog <- Puppy. `Animal` is polymorphic
// (virtual speak()/legs() + virtual dtor); `Dog` overrides speak(); `Puppy`
// overrides speak() AGAIN and inherits legs() unchanged. This exercises the
// intermediate-override de-dup in handleSuperClassesRecursive: when generating
// Puppy, the inherited virtual speak() must appear exactly once (Puppy's own
// override), NOT once per ancestor that re-declares it, and legs() (declared on
// Animal, never overridden) must appear exactly once too. AnimalProbe takes an
// `Animal*` so speak() dispatches through the base to the most-derived override.
struct Animal {
    virtual ~Animal() {}
    virtual int speak() const { return 0; }   // overridden by Dog, then Puppy
    virtual int legs() const { return 4; }     // declared on Animal, never overridden
};
struct Dog : Animal {
    int speak() const override { return 1; }   // intermediate override
};
struct Puppy : Dog {
    int speak() const override { return 2; }   // leaf override (over Dog's)
    // legs() inherited from Animal unchanged.
};
struct AnimalProbe {
    // Dispatch speak() through a base Animal* (upcast): a Puppy passed here returns 2.
    static int speakOf(const Animal* a) { return a ? a->speak() : -1; }
    static int legsOf(const Animal* a) { return a ? a->legs() : -1; }
};

// IH-abstract: `Drawable` has a PURE virtual draw()/kind() (= 0) so it is an
// abstract class — no constructor/factory is emitted for it, but it still gets a
// usable wrapper + a `DrawableApi` interface (interface-per-base). `Button` is a
// concrete subclass that implements both, constructs normally, and is usable as a
// `DrawableApi` with virtual dispatch. DrawableProbe takes/returns a `Drawable*`.
struct Drawable {
    virtual ~Drawable();
    virtual void draw() const = 0;     // pure virtual
    virtual int kind() const = 0;      // pure virtual, returns a testable value
};
inline Drawable::~Drawable() {}
struct Button : Drawable {
    int painted;
    Button() : painted(0) {}
    void draw() const override { /* no-op; concrete impl */ }
    int kind() const override { return 7; }   // Button's kind
};
struct DrawableProbe {
    // Dispatch kind() through a base Drawable* (upcast): a Button passed here returns 7.
    static int kindOf(const Drawable* d) { return d ? d->kind() : -1; }
    // Return a Drawable* (really the Button passed in) so the caller upcast-receives it.
    static Drawable* identity(Drawable* d) { return d; }
};

// IH-nvdtor (decision B diagnostic): `Gadget` is polymorphic (it has a virtual
// tick() AND is used as a base by Sprocket) but its destructor is NON-virtual
// (the implicit one). Deleting a Sprocket through a Gadget* is undefined in C++,
// so the generator must emit a non-fatal `WARNING: polymorphic class with a
// non-virtual destructor ...` comment on the Gadget binding (and on Sprocket,
// which inherits the non-virtual dtor). The binding must still generate + work —
// the diagnostic is advisory only. Sprocket overrides tick().
struct Gadget {
    int ticks;
    Gadget() : ticks(0) {}
    virtual void tick() { ticks++; }     // virtual -> polymorphic
    int read() const { return ticks; }
    // NOTE: no `virtual ~Gadget()` — deliberately non-virtual.
};
struct Sprocket : Gadget {
    int spins;
    Sprocket() : spins(0) {}
    void tick() override { spins++; }    // override -> stays `tick`
    int spun() const { return spins; }
};

// IH-multi-inherit / IH-cast: uniform offset-correct `.asBase()` upcast methods.
//
// (1) MULTIPLE INHERITANCE. Widget2 derives from TWO polymorphic bases. The second
// base (Tagged) sits at a NON-ZERO offset inside a Widget2, so recovering a `Tagged*`
// requires the base-subobject offset that `static_cast<Tagged*>` applies. A plain
// reinterpret_cast (offset 0) would make tag() read garbage / dispatch wrong. The
// generated `widget2.asDrawable2()` / `widget2.asTagged()` must each return a wrapper
// that dispatches the right base's virtual to Widget2's distinct overrides.
struct Drawable2 {
    virtual ~Drawable2() {}
    virtual int draw() const { return 0; }
};
struct Tagged {
    virtual ~Tagged() {}
    virtual int tag() const { return 0; }
};
struct Widget2 : public Drawable2, public Tagged {
    int draw() const override { return 11; }   // distinct from tag()
    int tag() const override { return 22; }    // distinct from draw()
};
struct MiProbe {
    // Read each base's virtual through a base pointer (proves the upcast handed back a
    // correctly-offset subobject pointer): a Widget2 upcast to Tagged returns 22 here.
    static int drawOf(const Drawable2* d) { return d ? d->draw() : -1; }
    static int tagOf(const Tagged* t) { return t ? t->tag() : -1; }
};

// (2) SINGLE INHERITANCE, NON-ZERO OFFSET (Case 2). PlainBase is NON-polymorphic (no
// vtable). VDerived ADDS a virtual method, giving VDerived a vptr at offset 0 and
// PUSHING the PlainBase subobject to a non-zero offset. So even this single-base case
// needs `static_cast<PlainBase*>` to read px correctly; a plain reinterpret_cast would
// read the vptr region as px (garbage).
struct PlainBase {
    int px;
    PlainBase() : px(0) {}
    int getPx() const { return px; }   // read px through a PlainBase*
    void setPx(int v) { px = v; }
};
struct VDerived : public PlainBase {
    int vy;
    VDerived() : vy(0) {}
    virtual int vid() const { return 99; }   // adds a vtable -> PlainBase offset != 0
};

// (3) CHECKED DOWN-CAST (T1.2 dyn_cast). A normal polymorphic hierarchy compiled with
// RTTI on: `DcBase` <- `Derived1` / `Derived2`. The generated `base.asDerived1()` /
// `base.asDerived2()` are NULLABLE down-casts backed by a `DcBase_dyncast_DerivedN` C
// shim doing `dynamic_cast<DerivedN*>` — returning a wrapper only when the runtime
// object really is that derived, else null. (This is the generic RTTI path; the LLVM
// `classof`/`dyn_cast` path is validated separately against the real Clang AST.) A
// virtual method makes the hierarchy polymorphic so dynamic_cast is well-defined.
struct DcBase {
    virtual ~DcBase() {}
    virtual int kind() const { return 0; }
};
struct Derived1 : public DcBase {
    int kind() const override { return 1; }
    int one() const { return 111; }    // only on Derived1
};
struct Derived2 : public DcBase {
    int kind() const override { return 2; }
    int two() const { return 222; }    // only on Derived2
};
// Hands back a `DcBase*` that actually points at a Derived1 (proves the down-cast checks
// the real runtime type, not a compile-time guess).
struct DcFactory {
    static DcBase* makeDerived1() { return new Derived1(); }
};

inline int cb_dbl(int n) { return n * 2; }
typedef int (*IntTransform)(int);
struct CbProbe {
    static int applyTransform(int x, IntTransform fn) { return fn(x); }
    static IntTransform makeDouble() { return cb_dbl; }
};

typedef int (*IntCtxFn)(void* ctx, int n);
struct CbCtx {
    static int callWith(IntCtxFn fn, void* userData, int x) { return fn(userData, x); }
};

// T-forcing-scope-2b: a function-pointer typedef declared INSIDE A NAMESPACE
// (`hh::handler_t`, a `void(*)()` shape mirroring std::terminate_handler/new_handler).
// A method taking/returning it reaches the WrappedFunctionPointer path with libclang's
// UNqualified typedef spelling `handler_t`. The generated C interop header re-declares
// the typedef at GLOBAL scope (`typedef void (*handler_t)();`), but the .cc ALSO pulls
// in the user header where the SAME name lives in `hh`, so the bare `handler_t` in the
// wrapper body either resolves to the wrong (global) typedef or, when the global
// re-declaration is absent, fails: "unknown type name 'handler_t'; did you mean
// 'hh::handler_t'?". This reproduces the real-Clang std::set_terminate/set_new_handler
// break the T-forcing pulls in from <exception>/<new>. The fix qualifies the typedef
// with its namespace (`hh::handler_t`) in the C++ wrapper while keeping the C-header
// re-declaration unqualified, so the generated .cc compiles.
namespace hh {
    typedef void (*handler_t)();
    inline void default_handler() {}
}

struct HandlerProbe {
    inline static hh::handler_t current = nullptr;
    static hh::handler_t setHandler(hh::handler_t h) {
        hh::handler_t prev = current;
        current = h;
        return prev;
    }
    static hh::handler_t getHandler() { return current; }
    static hh::handler_t defaultHandler() { return &hh::default_handler; }
    static bool isDefault(hh::handler_t h) { return h == &hh::default_handler; }
};

template<class T> class Box {
    T v;
public:
    Box() : v() {}
    void set(T x) { v = x; }
    T get() const { return v; }
};

// RG-range (T1.3): a method returning an `llvm::iterator_range<It>` is materialized
// into a bound `std::vector<Thing*>` (a `for`-iterable on the Kotlin side). The fixture
// mirrors the real Clang shape — a header-only `iterator_range` template with
// begin()/end() — using a POINTER iterator (`Thing**`, like clang::CXXRecordDecl::bases()
// → iterator_range<CXXBaseSpecifier*> and one decls() canonicalization → Decl**). A
// pointer iterator needs no separate iterator class (built-in ++/*/!=), so nothing extra
// is bound. `RangeHolder::items()` returns the range; the generator rewrites it to
// `std::vector<Thing*>` and emits a begin()/end() materializing loop.
namespace llvm {
    template <class It>
    class iterator_range {
        It b_, e_;
    public:
        iterator_range(It b, It e) : b_(b), e_(e) {}
        It begin() const { return b_; }
        It end() const { return e_; }
    };
}

struct Thing {
    int id;
    Thing() : id(0) {}
    Thing(int id_) : id(id_) {}
    int getId() const { return id; }
};

struct RangeHolder {
    // Backing storage for up to 4 Things; populated by add().
    Thing* slots_[4];
    int n_;
    RangeHolder() : n_(0) {}
    void add(Thing* t) { if (n_ < 4) slots_[n_++] = t; }
    int count() const { return n_; }
    // The range-returning method (mirrors decls()/methods()/bases()). The iterator is a
    // raw `Thing**`; `*it` is a `Thing*`, so the materialized vector is std::vector<Thing*>.
    llvm::iterator_range<Thing**> items() {
        return llvm::iterator_range<Thing**>(slots_, slots_ + n_);
    }
};

// T-defarg: a method whose parameter type krapper CANNOT model. `Mask<4>` is a
// non-type-template-parameter class the generator does not bind, so `countIf`'s
// `Mask<4>` param fails to resolve. Crucially, the NON-TYPE template argument `4`
// is exposed by libclang as an IntegerLiteral child cursor of the ParmDecl, and
// krapper's cursor-children default-arg heuristic mistakes that literal for a C++
// DEFAULT VALUE. So on the UNFIXED generator the failed (un-modelable) param is
// treated as "trailing & defaulted" and silently TRIMMED, leaving the method
// emitted with a short C++ call (`thiz_cast->countIf();`) that fails to compile
// ("too few arguments"). The fix drops the WHOLE method when a REQUIRED param
// can't be modeled (a param is only safely omittable when it has a GENUINE C++
// default, which a failed non-type-template-arg param does not). The sibling
// `sum()` (modelable) MUST still bind.
template <int N> struct Mask {
    int bits[N];
    bool at(int i) const { return i >= 0 && i < N && bits[i] != 0; }
};

struct Filterable {
    int values[4];
    Filterable() { values[0] = 1; values[1] = 2; values[2] = 3; values[3] = 4; }
    int countIf(Mask<4> mask) const {
        int n = 0;
        for (int i = 0; i < 4; i++) if (mask.at(i)) n++;
        return n;
    }
    int sum() const { return values[0] + values[1] + values[2] + values[3]; }
};

// T-defarg-bool (fix #1): the SAME false-default bug as Mask<4>, but the non-type
// template argument is a `bool` rather than a number. `Flags<true>` is an un-modelable
// non-type-template class; the `true` token is exposed as a CXXBoolLiteralExpr child of
// the ParmDecl and misread by the hasDefault cursor heuristic as a default value. The
// OLD numeric-only check (`toDoubleOrNull`) did NOT catch `true`, so `tally`'s required
// `Flags<true>` param was wrongly treated as omittable and TRIMMED -> short, non-compiling
// `thiz_cast->tally();`. The broadened value-literal classifier (bool/char/hex/number)
// catches it, so the whole method is dropped (skip-not-crash). Sibling `base()` binds.
// The "keep omittable" reference case (a genuinely-defaulted by-value template over only
// TYPE args, like buildASTFromCode's shared_ptr<PCHContainerOperations>) is pinned by
// krapper_gen's FalseDefaultTest.type_arg_template_stays_omittable instead.
template <bool On> struct Flags {
    int v;
    bool on() const { return On; }
};

struct BoolFilterable {
    int n;
    BoolFilterable() : n(7) {}
    int tally(Flags<true> f) const { return f.on() ? n : 0; }
    int base() const { return n; }
};

// UT-pair2: a user class template over TWO type params, exercising the arity>1
// facade/plugin path (interface Pair2<T1, T2> + `fun <T1, T2> MemScope.Pair2()`).
template<class A, class B> class Pair2 {
    A a; B b;
public:
    Pair2() : a(), b() {}
    void setA(A x) { a = x; }
    void setB(B x) { b = x; }
    A getA() const { return a; }
    B getB() const { return b; }
};

// T-constholder: a method returning a small COPYABLE struct BY VALUE but with the
// return type top-level `const`-qualified. The by-value return is materialized into a
// scope-bound Holder via placement-new (`new (ret_value_cast) Coord(thiz->origin())`).
// On the UNFIXED generator the Holder target buffer is typed `const Coord*`, and
// placement-new into a const-qualified pointer is ill-formed ("placement new expression
// with a const-qualified argument ... is not allowed") — so the featuregen native
// compile fails. Coord IS copyable; the only problem is the meaningless const on the
// fresh Holder buffer. The fix strips top-level const from the Holder TARGET type (the
// Kotlin-facing type is unchanged), so it binds and compiles. The non-const sibling
// `plain()` returns the same struct by value and must keep binding.
struct Coord {
    int x;
    int y;
    Coord() : x(0), y(0) {}
    Coord(int x_, int y_) : x(x_), y(y_) {}
    int sum() const { return x + y; }
};

struct ConstRetHolder {
    Coord origin_;
    ConstRetHolder() : origin_(3, 4) {}
    // const-qualified by-value return -> the const Holder-target bug (T-constholder).
    const Coord makeConst() const { return origin_; }
    // non-const by-value return of the same copyable struct (control).
    Coord plain() const { return origin_; }
};

// T-skip (a): a class whose copy constructor is `= delete`d (NonCopyable). A by-value
// (Holder) field getter of such a type emits `new (ret_value_cast) NonCopyable(thiz->nc)`
// — a call to the DELETED copy constructor, which won't compile. Skip-not-crash: DROP the
// whole `nc` field (a field with no usable getter is useless), while the sibling `tag`
// (a plain int field) keeps both getter and setter. NonCopyable keeps a usable non-copy
// constructor so the surrounding struct is still default-constructible and bindable.
//
// (A *method* returning NonCopyable by value would trip the identical generator path —
// the same canCopyConstruct drop in WrappedMethod.resolve guards it — but such a method
// can't be written as a compilable C++14 inline body, since returning a prvalue still
// needs an accessible copy/move ctor; the by-value field getter exercises the exact same
// Holder copy-construct shape and IS expressible, so it is the fixture here.)
class NonCopyable {
    int v_;
public:
    NonCopyable() : v_(0) {}
    explicit NonCopyable(int v) : v_(v) {}
    NonCopyable(const NonCopyable&) = delete;
    NonCopyable& operator=(const NonCopyable&) = delete;
    int value() const { return v_; }
};

struct NonCopyableHolder {
    NonCopyable nc;   // non-copy-constructible -> by-value getter dropped -> field dropped
    int tag;          // ordinary field -> getter + setter both bind
    NonCopyableHolder() : nc(7), tag(42) {}
};

// T-skip (b): a struct with a public field whose type `= delete`s its copy ASSIGNMENT
// but KEEPS a copy constructor (NoAssign). The by-value field GETTER copy-constructs the
// member fine, but the generated field SETTER does `thiz->locked = *value_cast`
// (copy-assign), which won't compile. Skip-not-crash: DROP just the setter (the getter is
// kept), while the sibling `count` field keeps both its getter and setter.
class NoAssign {
    int w_;
public:
    NoAssign() : w_(0) {}
    explicit NoAssign(int w) : w_(w) {}
    NoAssign(const NoAssign&) = default;
    NoAssign& operator=(const NoAssign&) = delete;
    int weight() const { return w_; }
};

struct FieldSetterHolder {
    NoAssign locked;   // non-copy-assignable -> setter dropped, getter kept
    int count;         // ordinary field -> getter + setter both bind
    FieldSetterHolder() : locked(5), count(0) {}
};

// T-typename-a: a user-defined conversion operator whose TARGET type lives in a
// namespace (`operator nn::Tok`). libclang spells the method `operator Tok` (the type
// UNqualified), so the generated C wrapper emitted `thiz_cast->operator Tok()`, which
// fails to compile outside `nn` ("unknown type name 'Tok'; did you mean 'nn::Tok'?").
// The fix re-spells the conversion call with the fully-qualified return type
// (`thiz_cast->operator nn::Tok()`). The non-namespaced `operator double()` on Vec2
// (OP-convert) takes the scalar `(double)*self` cast path and is unaffected.
namespace nn {
    struct Tok {
        int v;
        Tok() : v(0) {}
        Tok(int v_) : v(v_) {}
        int value() const { return v; }
    };
}

struct Convertible {
    int n;
    Convertible() : n(0) {}
    explicit Convertible(int n_) : n(n_) {}
    // conversion operator to a NAMESPACED struct -> the unqualified-spelling bug.
    operator nn::Tok() const { return nn::Tok(n); }
};

// T-typename-b: a class TEMPLATE with a nested struct `Wrapper<T>::Inner`. The nested
// type's qualified name can't be spelled standalone (naming it needs the enclosing
// template's argument), so krapper's bare-name flattening produced an unnameable
// `Inner`/`clang::Inner` and emitted `sizeof`/`reinterpret_cast`/accessors for a type
// the compiler can't find. Skip-not-crash: any class lexically nested inside a class
// TEMPLATE is DROPPED. The nested `Inner` here must NOT bind, while the sibling
// non-template nested `Outer::Inner` (above) and ordinary top-level classes still bind.
// `value` keeps the template referenced so its body (and the nested struct) is parsed.
template <class T> struct Wrapper {
    struct Inner {
        int x;
        Inner() : x(0) {}
        int getX() const { return x; }
    };
    T value;
    Inner inner;
    Wrapper() : value(), inner() {}
};

// A non-template nested class as an extra T-typename-b CONTROL (distinct from Outer):
// nested inside a plain struct, so it IS nameable (`Outer2::Inner2`) and must still bind.
struct Outer2 {
    struct Inner2 {
        int y;
        Inner2() : y(0) {}
        Inner2(int y_) : y(y_) {}
        int getY() const { return y; }
    };
    int who2;
    Outer2() : who2(11) {}
};

// T-skip-residuals (a): a struct whose copy ASSIGNMENT is IMPLICITLY deleted because it
// has a const data member (no explicit `= delete`, so the filtered-cursor signal never
// fired). Used as a public field type: the generated setter `thiz->slot = *value` is a
// copy-assign that won't compile. Skip-not-crash drops just the SETTER (getter kept).
struct ImplicitNoAssign {
    const int c;
    int extra;
    ImplicitNoAssign() : c(0), extra(0) {}
    ImplicitNoAssign(int c_) : c(c_), extra(0) {}
    int getC() const { return c; }
};

struct ImplicitAssignHolder {
    ImplicitNoAssign slot;   // implicitly non-copy-assignable -> setter dropped, getter kept
    int tally;               // ordinary field -> getter + setter both bind
    ImplicitAssignHolder() : slot(9), tally(0) {}
};

// T-skip-residuals (b): a struct whose DEFAULT constructor is IMPLICITLY deleted because
// it has a reference data member (a reference must be initialized). It declares its own
// (non-default) ctor, so krapper would otherwise NOT synthesize a default ctor — but a
// reference member ALSO makes it non-default-constructible AND non-copy-assignable. The
// struct is held as a public field; the by-value getter copy-constructs fine (a defaulted
// copy ctor exists), but the SETTER (`= *value`) is dropped (reference can't be rebound).
// The sibling `note` int field keeps both accessors.
struct RefMember {
    int& ref;
    explicit RefMember(int& r) : ref(r) {}
    int read() const { return ref; }
};

// A struct with a reference member and NO user constructor: its implicit DEFAULT ctor is
// deleted. krapper must NOT synthesize a `new RefHolder()` default-construct path (that
// would call the implicitly-deleted default ctor). Skip-not-crash: no default factory is
// emitted for it; the sibling top-level classes are unaffected. (It is only parsed, not
// constructed from Kotlin — proving the bogus default-construct path is suppressed.)
struct RefHolder {
    int& slot;
    int note;
};

// T-inaccessible (#123): a public method whose SIGNATURE references a protected/private
// nested type can't be bound — the generated wrapper is a free function, so it can't name
// the type to cast to it (the LLVM-22 repro: `clang::ASTContext::getPredefinedSugarType`
// takes the protected enum `clang::Type::PredefinedSugarKind`). Skip-not-crash: the
// generator DROPS such a method (its signature type resolves to UNRESOLVABLE), while
// sibling methods with accessible signatures still bind. Two inaccessible kinds are
// covered — a protected nested ENUM and a private nested STRUCT — plus a method over the
// accessible top-level `Color` enum as a control that must still bind.
struct AccessProbe {
protected:
    enum HiddenMode { HM_A = 1, HM_B = 2 };   // protected nested enum
private:
    struct HiddenTag { int t; };              // private nested struct
public:
    AccessProbe() {}
    // DROPPED: param is the protected enum (can't be named from the wrapper).
    int useHiddenEnum(HiddenMode m) const { return (int)m; }
    // DROPPED: return type is the private nested struct.
    HiddenTag makeHiddenTag() const { return HiddenTag{7}; }
    // BOUND: param is the accessible top-level `Color` enum (control for the drop).
    int useColor(Color c) const { return (int)c; }
    // BOUND: a plain int accessor sibling, always bindable.
    int plain() const { return 99; }
};

#endif
