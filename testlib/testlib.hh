#pragma once

#include <iostream>
using namespace std;

namespace TestLib {

class Complex {
    private:
      float real;
      float imag;
    public:
       Complex();
       void input();

       // Operator overloading
       Complex operator - (Complex c2);

       void output();
};

}
