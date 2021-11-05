#include "testlib.hh"
#include <string>

using namespace std;

namespace TestLib {


TestClass::TestClass(): 
        privateString(""),
        b(0),
        st(0),
        uit(0),

        c('a'),
        uc('a'),
        s(0),
        us(0),
        i(0),
        ui(0),
        l(0),
        ul(0),
        ll(0),
        ull(0),
        f(0),
        d(0),
        ld(0),
        str(NULL),
        pb(NULL),
        pc(NULL),
        puc(NULL),
        ps(NULL),
        pus(NULL),
        pi(NULL),
        pui(NULL),
        pl(NULL),
        pul(NULL),
        pll(NULL),
        pull(NULL),
        pf(NULL),
        pd(NULL) {
    array[0] = 0;
    array[1] = 0;
    array[2] = 0;
    array[3] = 0;
    array[4] = 0;
}

TestClass::TestClass(const TestClass &other) :
        privateString(other.privateString),
        b(other.b),
        st(other.st),
        uit(other.uit),
        str(other.str),

        c(other.c),
        uc(other.uc),
        s(other.s),
        us(other.us),
        i(other.i),
        ui(other.ui),
        l(other.l),
        ul(other.ul),
        ll(other.ll),
        ull(other.ull),
        f(other.f),
        d(other.d),
        ld(other.ld),

        pb(other.pb),
        pc(other.pc),
        puc(other.puc),
        ps(other.ps),
        pus(other.pus),
        pi(other.pi),
        pui(other.pui),
        pl(other.pl),
        pul(other.pul),
        pll(other.pll),
        pull(other.pull),
        pf(other.pf),
        pd(other.pd){
    array[0] = other.array[0];
    array[1] = other.array[1];
    array[2] = other.array[2];
    array[3] = other.array[3];
    array[4] = other.array[4];
}

TestClass::TestClass(int a): i(a),
        privateString(""),
        b(0),
        st(0),
        uit(0),

        c('a'),
        uc('a'),
        s(0),
        us(0),
        ui(0),
        l(0),
        ul(0),
        ll(0),
        ull(0),
        f(0),
        d(0),
        ld(0),
        str(NULL),
        pb(NULL),
        pc(NULL),
        puc(NULL),
        ps(NULL),
        pus(NULL),
        pi(NULL),
        pui(NULL),
        pl(NULL),
        pul(NULL),
        pll(NULL),
        pull(NULL),
        pf(NULL),
        pd(NULL) {
    array[0] = 0;
    array[1] = 0;
    array[2] = 0;
    array[3] = 0;
    array[4] = 0;
}

TestClass::TestClass(int a, double b): i(a), d(b),
        privateString(""),
        b(0),
        st(0),
        uit(0),

        c('a'),
        uc('a'),
        s(0),
        us(0),
        ui(0),
        l(0),
        ul(0),
        ll(0),
        ull(0),
        f(0),
        ld(0),
        str(NULL),
        pb(NULL),
        pc(NULL),
        puc(NULL),
        ps(NULL),
        pus(NULL),
        pi(NULL),
        pui(NULL),
        pl(NULL),
        pul(NULL),
        pll(NULL),
        pull(NULL),
        pf(NULL),
        pd(NULL) {
    array[0] = 0;
    array[1] = 0;
    array[2] = 0;
    array[3] = 0;
    array[4] = 0;
}

long TestClass::sum() {
    return st + uit + c + uc + s + us + i + ui + l + ul + ll + ull + f + d + ld;
}

void TestClass::setPrivateString(std::string value) {
    privateString = value;
}

void TestClass::setSome(int a, long b, long long c) {
    this->i = a;
    this->l = b;
    this->ll = c;
}

long* TestClass::longPointer() {
    return &this->l;
}

void TestClass::setPointers(int* a, long* b, long long* c) {
    this->i = *a;
    this->l = *b;
    this->ll = *c;
}

void TestClass::output() {
    cout << "TestClass values\n";
    cout << "privateString " << privateString << "\n";
    cout << "bool " << b << "\n";
    cout << "size_t " << st << "\n";
    cout << "uint16_t " << uit << "\n";

    cout << "int " << array[0] << " " << array[1] << " " << array[2] << " " << array[3] << " " << array[4] << "\n";
    if (str != NULL) {cout << "char* " << str << "\n";}

    cout << "signed char " << c << "\n";
    cout << "unsigned char " << uc << "\n";
    cout << "signed short " << s << "\n";
    cout << "unsigned short " << us << "\n";
    cout << "signed int " << i << "\n";
    cout << "unsigned int " << ui << "\n";
    cout << "signed long " << l << "\n";
    cout << "unsigned long " << ul << "\n";
    cout << "signed long long " << ll << "\n";
    cout << "unsigned long long " << ull << "\n";
    cout << "float " << f << "\n";
    cout << "double " << d << "\n";
    cout << "long double " << ld << "\n";

    if (pb != NULL) {cout << "bool " << *pb << "\n";}
    if (pc != NULL) {cout << "signed char " << *pc << "\n";}
    if (puc != NULL) {cout << "unsigned char " << *puc << "\n";}
    if (ps != NULL) {cout << "signed short " << *ps << "\n";}
    if (pus != NULL) {cout << "unsigned short " << *pus << "\n";}
    if (pi != NULL) {cout << "signed int " << *pi << "\n";}
    if (pui != NULL) {cout << "unsigned int " << *pui << "\n";}
    if (pl != NULL) {cout << "signed long " << *pl << "\n";}
    if (pul != NULL) {cout << "unsigned long " << *pul << "\n";}
    if (pll != NULL) {cout << "signed long long " << *pll << "\n";}
    if (pull != NULL) {cout << "unsigned long long " << *pull << "\n";}
    if (pf != NULL) {cout << "float " << *pf << "\n";}
    if (pd != NULL) {cout << "double " << *pd << "\n";}
    cout << flush;
}

// Operator overloading
TestClass TestClass::operator-(TestClass c2) {
    TestClass ret(c2);
    ret.privateString = "operator-" + privateString;
    return ret;
}

TestClass TestClass::operator-() {
    TestClass ret(*this);
    ret.privateString = "operator-()" + privateString;
    return ret;
}

TestClass TestClass::operator+(TestClass c2) {
    TestClass ret(c2);
    ret.privateString = "operator+" + privateString;
    return ret;
}

TestClass TestClass::operator+() {
    TestClass ret(*this);
    ret.privateString = "operator+()" + privateString;
    return ret;
}

TestClass TestClass::operator*(TestClass c2) {
    TestClass ret(c2);
    ret.privateString = "operator*" + privateString;
    return ret;
}

TestClass TestClass::operator/(TestClass c2) {
    TestClass ret(c2);
    ret.privateString = "operator/" + privateString;
    return ret;
}

TestClass TestClass::operator%(TestClass c2) {
    TestClass ret(c2);
    ret.privateString = "operator%" + privateString;
    return ret;
}

TestClass TestClass::operator++() {
    TestClass ret(*this);
    ret.privateString = "operator++" + privateString;
    return ret;
}

TestClass TestClass::operator++(int dummy) {
    TestClass ret(*this);
    ret.privateString = "operator++(dummy)" + privateString;
    return ret;
}

TestClass TestClass::operator--() {
    TestClass ret(*this);
    ret.privateString = "operator--" + privateString;
    return ret;
}

TestClass TestClass::operator--(int dummy) {
    TestClass ret(*this);
    ret.privateString = "operator--(dummy)" + privateString;
    return ret;
}

TestClass TestClass::operator==(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator==" + privateString;
    return ret;
}

TestClass TestClass::operator!=(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator!=" + privateString;
    return ret;
}

TestClass TestClass::operator<(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator<" + privateString;
    return ret;
}

TestClass TestClass::operator>(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator>" + privateString;
    return ret;
}

TestClass TestClass::operator<=(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator<=" + privateString;
    return ret;
}

TestClass TestClass::operator>=(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator>=" + privateString;
    return ret;
}

TestClass TestClass::operator!() {
    TestClass ret(*this);
    ret.privateString = "operator!" + privateString;
    return ret;
}

TestClass TestClass::operator&&(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator&&" + privateString;
    return ret;
}

TestClass TestClass::operator||(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator||" + privateString;
    return ret;
}

TestClass TestClass::operator~() {
    TestClass ret(*this);
    ret.privateString = "operator~" + privateString;
    return ret;
}

TestClass TestClass::operator&(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator&" + privateString;
    return ret;
}

TestClass TestClass::operator|(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator|" + privateString;
    return ret;
}

TestClass TestClass::operator^(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator^" + privateString;
    return ret;
}

TestClass TestClass::operator<<(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator<<" + privateString;
    return ret;
}

TestClass TestClass::operator>>(TestClass &c2) {
    TestClass ret(c2);
    ret.privateString = "operator>>" + privateString;
    return ret;
}

TestClass TestClass::operator[](std::string &c2) {
    TestClass ret(*this);
    ret.privateString = "operator[" + c2 + "]" + privateString;
    return ret;
}

void TestClass::setPrivateFrom(OtherClass* other) {
    this->privateString = other->getPrivateString();
}

std::string OtherClass::getPrivateString() {
    return this->privateString;
}

void OtherClass::setPrivateString(std::string value) {
    this->privateString = value;
}

void OtherClass::appendText(std::vector<std::string> text) {
    string line = "";

    for (string str: text) {
      line.append(str);
      line.append(",");
    }
    this->privateString.append(line);
}

MyPair<OtherClass*>* OtherClass::copies() {
    return new MyPair<OtherClass*>(this, this);
}

MyPair<int>* OtherClass::ints() {
    return new MyPair<int>(5, 0);
}

}
