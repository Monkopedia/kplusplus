#include "testlib.hh"

using namespace std;

namespace TestLib {

Complex::Complex(): real(0), imag(0){ }
void Complex::input() {
   cout << "Enter real and imaginary parts respectively: ";
   cin >> real;
   cin >> imag;
}

// Operator overloading
Complex Complex::operator - (Complex c2) {
   Complex temp;
   temp.real = real - c2.real;
   temp.imag = imag - c2.imag;

   return temp;
}

void Complex::output() {
   if(imag < 0)
       cout << "Output Complex number: "<< real << imag << "i";
   else
       cout << "Output Complex number: " << real << "+" << imag << "i";
}

}
