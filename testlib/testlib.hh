#pragma once

#include <iostream>
#include <vector>
#include <string>

namespace TestLib {


class OtherClass {
    private:
        std::string privateString;
    public:
        std::string getPrivateString();
        void setPrivateString(std::string value);
        void appendText(std::vector<std::string> text);
};

class TestClass {
    private:
        std::string privateString;
    public:
        bool b;
        size_t st;
        uint16_t uit;

        int array[5];
        char* str;

        signed char c;
        unsigned char uc;
        signed short s;
        unsigned short us;
        signed int i;
        unsigned int ui;
        signed long l;
        unsigned long ul;
        signed long long ll;
        unsigned long long ull;
        float f;
        double d;
        long double ld;

        bool *pb;
        signed char *pc;
        unsigned char *puc;
        signed short *ps;
        unsigned short *pus;
        signed int *pi;
        unsigned int *pui;
        signed long *pl;
        unsigned long *pul;
        signed long long *pll;
        unsigned long long *pull;
        float *pf;
        double *pd;

        TestClass();
        TestClass(const TestClass &other);
        TestClass(int a);
        TestClass(int a, double b);

        long sum();
        long* longPointer();
        void setSome(int a, long b, long long c);
        void setPointers(int* a, long* b, long long* c);
        void setPrivateString(std::string value);
        void setPrivateFrom(OtherClass* other);
 
        void output();

        // Operator overloading
        TestClass operator-(TestClass c2);
        TestClass operator-();
        TestClass operator+(TestClass c2);
        TestClass operator+();
        TestClass operator*(TestClass c2);
        TestClass operator/(TestClass c2);
        TestClass operator%(TestClass c2);
        TestClass operator++();
        TestClass operator++(int dummy);
        TestClass operator--();
        TestClass operator--(int dummy);
        TestClass operator==(TestClass &c2);
        TestClass operator!=(TestClass &c2);
        TestClass operator<(TestClass &c2);
        TestClass operator>(TestClass &c2);
        TestClass operator<=(TestClass &c2);
        TestClass operator>=(TestClass &c2);
        TestClass operator!();
        TestClass operator&&(TestClass &c2);
        TestClass operator||(TestClass &c2);
        TestClass operator~();
        TestClass operator&(TestClass &c2);
        TestClass operator|(TestClass &c2);
        TestClass operator^(TestClass &c2);
        TestClass operator<<(TestClass &c2);
        TestClass operator>>(TestClass &c2);
        TestClass operator[](std::string &c2);
};

}
