#include "testlib.hh"

using namespace std;
using namespace TestLib;

int main() {
    TestClass c1, c2, result;

    c1.b = true;
    cout << "Set boolean\n" << flush;
    c1.output();

    c1.st = 1;
    cout << "Set st\n" << flush;
    c1.output();

    c1.uit = 2;
    cout << "Set uit\n" << flush;
    c1.output();

    c1.array[0] = 3;
    c1.array[1] = 4;
    c1.array[2] = 5;
    c1.array[3] = 6;
    c1.array[4] = 7;
    cout << "Set array\n" << flush;
    c1.output();

    c1.str = "My test string";
    cout << "Set str\n" << flush << flush;
    c1.output();

    c1.c = 8;
    cout << "Set c\n" << flush;
    c1.output();

    c1.uc = 9;
    cout << "Set uc\n" << flush;
    c1.output();

    c1.s = 10;
    cout << "Set s\n" << flush;
    c1.output();

    c1.us = 11;
    cout << "Set us\n" << flush;
    c1.output();

    c1.i = 12;
    cout << "Set 12\n" << flush;
    c1.output();

    c1.ui = 13;
    cout << "Set ui\n" << flush;
    c1.output();

    c1.l = 14;
    cout << "Set l\n" << flush;
    c1.output();

    c1.ul = 15;
    cout << "Set ul\n" << flush;
    c1.output();

    c1.ll = 16;
    cout << "Set ll\n" << flush;
    c1.output();

    c1.ull = 17;
    cout << "Set ull\n" << flush;
    c1.output();

    c1.f = 18;
    cout << "Set f\n" << flush;
    c1.output();

    c1.d = 19;
    cout << "Set d\n" << flush;
    c1.output();

    c1.ld = 20;
    cout << "Set ld\n" << flush;
    c1.output();

    bool pb = false;
    signed char pc = 21;
    unsigned char puc = 22;
    signed short ps = 23;
    unsigned short pus = 24;
    signed int pi = 25;
    unsigned int pui = 26;
    signed long pl = 27;
    unsigned long pul = 28;
    signed long long pll = 29;
    unsigned long long pull = 30;
    float pf = 31;
    double pd = 32;

    c1.pb = &pb;
    cout << "Set pb\n" << flush;
    c1.output();

    c1.pc = &pc;
    cout << "Set pc\n" << flush;
    c1.output();

    c1.pc = &pc;
    cout << "Set pc\n" << flush;
    c1.output();

    c1.puc = &puc;
    cout << "Set puc\n" << flush;
    c1.output();

    c1.ps = &ps;
    cout << "Set ps\n" << flush;
    c1.output();

    c1.pus = &pus;
    cout << "Set pus\n" << flush;
    c1.output();

    c1.pi = &pi;
    cout << "Set pi\n" << flush;
    c1.output();

    c1.pl = &pl;
    cout << "Set pl\n" << flush;
    c1.output();

    c1.pul = &pul;
    cout << "Set pul\n" << flush;
    c1.output();

    c1.pll = &pll;
    cout << "Set pll\n" << flush;
    c1.output();

    c1.pull = &pull;
    cout << "Set pull\n" << flush;
    c1.output();

    c1.pf = &pf;
    cout << "Set pf\n" << flush;
    c1.output();

    c1.pd = &pd;
    cout << "Set pd\n" << flush;
    c1.output();

    c1.setPrivateString("Unary private string");
    c2.setPrivateString("My private string");
    TestClass op1 = c1 - c2;
    cout << "op1 minus\n" << flush;
    op1.output();

    TestClass op2 = -c1;
    cout << "op2 unary minus\n" << flush;
    op2.output();

    TestClass op3 = c1 + c2;
    cout << "op3 plus\n" << flush;
    op3.output();

    TestClass op4 = +c1;
    cout << "op4 unary plus\n" << flush;
    op4.output();

    TestClass op5 = c1 * c2;
    cout << "op5 times\n" << flush;
    op5.output();

    TestClass op6 = c1 / c2;
    cout << "op6 divide\n" << flush;
    op6.output();

    TestClass op7 = c1 % c2;
    cout << "op7 mod\n" << flush;
    op7.output();

    TestClass op8 = ++c1;
    cout << "op8 preinc\n" << flush;
    op8.output();

    TestClass op9 = c1++;
    cout << "op9 postinc\n" << flush;
    op9.output();

    TestClass op10 = --c1;
    cout << "op10 predec\n" << flush;
    op10.output();

    TestClass op11 = c1--;
    cout << "op11 postdec\n" << flush;
    op11.output();

    TestClass op12 = c1 == c2;
    cout << "op12 eq comp\n" << flush;
    op12.output();

    TestClass op13 = c1 != c2;
    cout << "op13 neq\n" << flush;
    op13.output();

    TestClass op14 = c1 < c2;
    cout << "op14 less than\n" << flush;
    op14.output();

    TestClass op15 = c1 > c2;
    cout << "op15 greater than\n" << flush;
    op15.output();

    TestClass op16 = c1 <= c2;
    cout << "op16 lt eq\n" << flush;
    op16.output();

    TestClass op17 = c1 >= c2;
    cout << "op17 gt eq\n" << flush;
    op17.output();

    TestClass op18 = !c1;
    cout << "op18 boolean not\n" << flush;
    op18.output();

    TestClass op19 = c1 && c2;
    cout << "op19 boolean and\n" << flush;
    op19.output();

    TestClass op20 = c1 || c2;
    cout << "op20 boolean or\n" << flush;
    op20.output();

    TestClass op21 = ~c1;
    cout << "op21 bit not\n" << flush;
    op21.output();

    TestClass op22 = c1 & c2;
    cout << "op22 bit and\n" << flush;
    op22.output();

    TestClass op23 = c1 | c2;
    cout << "op23 bit or\n" << flush;
    op23.output();

    TestClass op24 = c1 ^ c2;
    cout << "op24 xor\n" << flush;
    op24.output();

    TestClass op25 = c1 << c2;
    cout << "op25 shl\n" << flush;
    op25.output();

    TestClass op26 = c1 >> c2;
    cout << "op26 shr\n" << flush;
    op26.output();

    string str("something");
    TestClass op27 = c1[str];
    cout << "op27 shr\n" << flush;
    op27.output();

    cout << "Sum: " << c1.sum() << "\n" << flush;
    cout << "Long pointer: " << *c1.longPointer() << "\n" << flush;

    c1.setSome(1, 2, 3);
    cout << "setSome\n" << flush;
    c1.output();

    int a = 11;
    long b = 12;
    long long c = 13;
    c1.setPointers(&a, &b, &c);
    cout << "setPointers\n" << flush;
    c1.output();

    OtherClass other;
    vector<string> vect;
    vect.push_back("First!");
    vect.push_back("second one");
    vect.push_back("THIRD");

    other.setPrivateString("Prefix: ");
    other.appendText(vect);
    cout << "Other text: " << other.getPrivateString() << "\n" << flush;

    c1.setPrivateFrom(&other);
    cout << "Set from other\n" << flush;
    c1.output();

    return 0;
}

