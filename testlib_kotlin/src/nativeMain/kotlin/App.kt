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
import TestClass.Companion.TestClass
import TestLib.TestClass.Companion.TestClass
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.DoubleVar
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.ShortVar
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.cValue
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    memScoped {
        val c1 = TestClass()
        val c2 = TestClass()
        val result = TestClass()

        val pb = cValue<BooleanVar>().getPointer(this).also { it.pointed.value = false }
        val pc = cValue<ByteVar>().getPointer(this).also { it.pointed.value = 21 }
        val puc = cValue<UByteVar>().getPointer(this).also { it.pointed.value = 22U }
        val ps = cValue<ShortVar>().getPointer(this).also { it.pointed.value = 23 }
        val pus = cValue<UShortVar>().getPointer(this).also { it.pointed.value = 24U }
        val pi = cValue<IntVar>().getPointer(this).also { it.pointed.value = 25 }
        val pui = cValue<UIntVar>().getPointer(this).also { it.pointed.value = 26U }
        val pl = cValue<LongVar>().getPointer(this).also { it.pointed.value = 27 }
        val pul = cValue<ULongVar>().getPointer(this).also { it.pointed.value = 28UL }
        val pll = cValue<LongVar>().getPointer(this).also { it.pointed.value = 29 }
        val pull = cValue<ULongVar>().getPointer(this).also { it.pointed.value = 30UL }
        val pf = cValue<FloatVar>().getPointer(this).also { it.pointed.value = 31f }
        val pd = cValue<DoubleVar>().getPointer(this).also { it.pointed.value = 32.0 }

        c1.b = true
        println("Set boolean")
        c1.output()

//        c1.st = 1UL
//        println("Set st")
//        c1.output()
//
//        c1.uit = 2U
//        println("Set uit")
//        c1.output()
//
//        c1.array = intArrayOf(
//            3,
//            4,
//            5,
//            6,
//            7
//        )
//        println("Set array")
//        c1.output()

        c1.str = "My test string"
        println("Set str")
        c1.output()

        c1.c = 8
        println("Set c")
        c1.output()

        c1.uc = 9U
        println("Set uc")
        c1.output()

        c1.s = 10
        println("Set s")
        c1.output()

        c1.us = 11U
        println("Set us")
        c1.output()

        c1.i = 12
        println("Set 12")
        c1.output()

        c1.ui = 13U
        println("Set ui")
        c1.output()

        c1.l = 14
        println("Set l")
        c1.output()

        c1.ul = 15UL
        println("Set ul")
        c1.output()

        c1.ll = 16
        println("Set ll")
        c1.output()

        c1.ull = 17UL
        println("Set ull")
        c1.output()

        c1.f = 18f
        println("Set f")
        c1.output()

        c1.d = 19.0
        println("Set d")
        c1.output()

        c1.ld = 20.0
        println("Set ld")
        c1.output()

        c1.pb = pb
        println("Set pb")
        c1.output()

        c1.pc = pc
        println("Set pc")
        c1.output()

        c1.pc = pc
        println("Set pc")
        c1.output()

        c1.puc = puc
        println("Set puc")
        c1.output()

        c1.ps = ps
        println("Set ps")
        c1.output()

        c1.pus = pus
        println("Set pus")
        c1.output()

        c1.pi = pi
        println("Set pi")
        c1.output()

        c1.pl = pl
        println("Set pl")
        c1.output()

        c1.pul = pul
        println("Set pul")
        c1.output()

        c1.pll = pll
        println("Set pll")
        c1.output()

        c1.pull = pull
        println("Set pull")
        c1.output()

        c1.pf = pf
        println("Set pf")
        c1.output()

        c1.pd = pd
        println("Set pd")
        c1.output()

        c1.setPrivateString("Unary private string")
        c2.setPrivateString("My private string")
        val op1 = c1 - c2
        println("op1 minus")
        op1.output()

        val op2 = -c1
        println("op2 unary minus")
        op2.output()

        val op3 = c1 + c2
        println("op3 plus")
        op3.output()

        val op4 = +c1
        println("op4 unary plus")
        op4.output()

        val op5 = c1 * c2
        println("op5 times")
        op5.output()

        val op6 = c1 / c2
        println("op6 divide")
        op6.output()

        val op7 = c1 % c2
        println("op7 mod")
        op7.output()

        var c = TestClass(c1)
        val op8 = ++c
        println("op8 preinc")
        op8.output()

        val op9 = c1.postIncrement()
        println("op9 postinc")
        op9.output()

        var c10 = TestClass(c1)
        val op10 = --c10
        println("op10 predec")
        op10.output()

        val op11 = c1.postDecrement()
        println("op11 postdec")
        op11.output()

        val op12 = c1 eq c2
        println("op12 eq comp")
        op12.output()

        val op13 = c1 notEq c2
        println("op13 neq")
        op13.output()

        val op14 = c1 lt c2
        println("op14 less than")
        op14.output()

        val op15 = c1 gt c2
        println("op15 greater than")
        op15.output()

        val op16 = c1 lteq c2
        println("op16 lt eq")
        op16.output()

        val op17 = c1 gteq c2
        println("op17 gt eq")
        op17.output()

        val op18 = !c1
        println("op18 boolean not")
        op18.output()

        val op19 = c1 band c2
        println("op19 boolean and")
        op19.output()

        val op20 = c1 bor c2
        println("op20 boolean or")
        op20.output()

        val op21 = c1.inv()
        println("op21 bit not")
        op21.output()

        val op22 = c1 and c2
        println("op22 bit and")
        op22.output()

        val op23 = c1 or c2
        println("op23 bit or")
        op23.output()

        val op24 = c1 xor c2
        println("op24 xor")
        op24.output()

        val op25 = c1 shl c2
        println("op25 shl")
        op25.output()

        val op26 = c1 shr c2
        println("op26 shr")
        op26.output()

        val op27 = c1["something"]
        println("op27 shr")
        op27.output()

        println("Sum: ${c1.sum()}")
        println("Long pointer: " + c1.longPointer()?.get(0) + "")

        c1.setSome(1, 2, 3)
        println("setSome")
        c1.output()

        val a = cValue<IntVar>().getPointer(this).also { it.pointed.value = 11 }
        val b = cValue<LongVar>().getPointer(this).also { it.pointed.value = 12 }
        val sc = cValue<LongVar>().getPointer(this).also { it.pointed.value = 13 }
        c1.setPointers(a, b, sc)
        println("setPointers")
        c1.output()

        val other = OtherClass()
        val vect = listOf(
            "First!",
            "second one",
            "THIRD"
        )

        other.privateString = "Prefix: "
        other.appendText(vect)
        println("Other text: ${other.privateString}")

        c1.setPrivateFrom(other)
        println("Set from other")
        c1.output()
    }
    println("Done")
}
