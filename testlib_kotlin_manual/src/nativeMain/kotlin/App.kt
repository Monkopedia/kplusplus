import Complex.Companion.Complex
import kotlinx.cinterop.memScoped

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    memScoped {
        var c1 = Complex()
        var c2 = Complex()

        println("Enter first complex number:")
        c1.input()

        println("Enter second complex number:")
        c2.input()

        // In case of operator overloading of binary operators in C++ programming,
        // the object on right hand side of operator is always assumed as argument by compiler.

        // In case of operator overloading of binary operators in C++ programming,
        // the object on right hand side of operator is always assumed as argument by compiler.
        val result = c1 - c2
        result.output()
    }
    println("Done")
}
