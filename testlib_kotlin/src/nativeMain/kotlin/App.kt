/*
 * Copyright 2021 Jason Monk
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
