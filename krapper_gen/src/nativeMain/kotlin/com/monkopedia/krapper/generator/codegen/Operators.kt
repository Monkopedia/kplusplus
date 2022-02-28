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
package com.monkopedia.krapper.generator.codegen

import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedMethod

sealed class KotlinOperatorType

data class KotlinOperator(val name: String) : KotlinOperatorType()
data class InfixMethod(val name: String) : KotlinOperatorType()
data class BasicWithDummyMethod(val name: String) : KotlinOperatorType()
data class BasicMethod(val name: String) : KotlinOperatorType()

sealed class Operator {

    abstract val kotlinOperatorType: KotlinOperatorType

    abstract fun name(namer: Namer, cls: WrappedClass, method: WrappedMethod): String
    protected abstract fun matches(method: WrappedMethod): Boolean

    companion object {
        val ALL_OPERATORS = listOf(
            BasicBinaryOperator.MINUS,
            BasicBinaryOperator.PLUS,
            BasicBinaryOperator.TIMES,
            BasicBinaryOperator.DIV,
            BasicBinaryOperator.MOD,
            BasicBinaryOperator.EQ,
            BasicBinaryOperator.NEQ,
            BasicBinaryOperator.LT,
            BasicBinaryOperator.GT,
            BasicBinaryOperator.LTEQ,
            BasicBinaryOperator.GTEQ,
            BasicBinaryOperator.BINARY_AND,
            BasicBinaryOperator.BINARY_OR,
            BasicBinaryOperator.AND,
            BasicBinaryOperator.OR,
            BasicBinaryOperator.XOR,
            BasicBinaryOperator.SHL,
            BasicBinaryOperator.SHR,
            BasicBinaryOperator.IND,
            BasicBinaryOperator.POST_INC,
            BasicBinaryOperator.POST_DEC,
            BasicUnaryOperator.INC,
            BasicUnaryOperator.DEC,
            BasicUnaryOperator.UNARY_MINUS,
            BasicUnaryOperator.UNARY_PLUS,
            BasicUnaryOperator.NOT,
            BasicUnaryOperator.INV,
            BasicUnaryOperator.REFERENCE,
            BasicUnaryOperator.POINTER_REFERENCE,
            BasicAssignmentOperator.ASSIGN,
            BasicAssignmentOperator.PLUS_EQUALS,
        )

        fun from(method: WrappedMethod): Operator? {
            if (!method.name.startsWith("operator")) return null
            return ALL_OPERATORS.find { it.matches(method) }
        }
    }
}

data class BasicBinaryOperator private constructor(
    val cppOp: String,
    private val cOp: String,
    override val kotlinOperatorType: KotlinOperatorType,
    val supportsDirectCall: Boolean = true
) : Operator() {

    override fun name(namer: Namer, cls: WrappedClass, method: WrappedMethod): String =
        with(namer) {
            return cName + "_op_" + cOp.splitCamelcase().joinToString("_") { it.toLowerCase() }
        }

    override fun matches(method: WrappedMethod): Boolean {
        return method.args.size == 1 && method.name.substring("operator".length) == cppOp
    }

    override fun toString(): String {
        return "binary($cppOp, $cOp, $kotlinOperatorType)"
    }

    companion object {
        val MINUS = BasicBinaryOperator("-", "Minus", KotlinOperator("minus"))
        val PLUS = BasicBinaryOperator("+", "Plus", KotlinOperator("plus"))
        val TIMES = BasicBinaryOperator("*", "Times", KotlinOperator("times"))
        val DIV = BasicBinaryOperator("/", "Divide", KotlinOperator("div"))
        val MOD = BasicBinaryOperator("%", "Mod", KotlinOperator("rem"))
        val EQ = BasicBinaryOperator("==", "Eq", InfixMethod("eq"))
        val NEQ = BasicBinaryOperator("!=", "Neq", InfixMethod("neq"))
        val LT = BasicBinaryOperator("<", "Lt", InfixMethod("lt"))
        val GT = BasicBinaryOperator(">", "Gt", InfixMethod("gt"))
        val LTEQ = BasicBinaryOperator("<=", "Lteq", InfixMethod("lteq"))
        val GTEQ = BasicBinaryOperator(">=", "Gteq", InfixMethod("gteq"))
        val BINARY_AND = BasicBinaryOperator("&&", "Binary_And", InfixMethod("binAnd"))
        val BINARY_OR = BasicBinaryOperator("||", "Binary_Or", InfixMethod("binOr"))
        val AND = BasicBinaryOperator("&", "And", InfixMethod("and"))
        val OR = BasicBinaryOperator("|", "Or", InfixMethod("or"))
        val XOR = BasicBinaryOperator("^", "Xor", InfixMethod("xor"))
        val SHL = BasicBinaryOperator("<<", "Shl", InfixMethod("shl"))
        val SHR = BasicBinaryOperator(">>", "Shr", InfixMethod("shr"))
        val IND =
            BasicBinaryOperator("[]", "Ind", KotlinOperator("get"), supportsDirectCall = false)
        val POST_INC = BasicBinaryOperator(
            "++",
            "PostIncrement",
            BasicWithDummyMethod("postIncrement"), supportsDirectCall = false
        )
        val POST_DEC = BasicBinaryOperator(
            "--",
            "PostDecrement",
            BasicWithDummyMethod("postDecrement"), supportsDirectCall = false
        )
    }
}

data class BasicAssignmentOperator private constructor(
    private val cppOp: String,
    private val cOp: String,
    override val kotlinOperatorType: KotlinOperatorType
) : Operator() {

    override fun name(namer: Namer, cls: WrappedClass, method: WrappedMethod): String =
        with(namer) {
            return cName + "_op_" + cOp.splitCamelcase().joinToString("_") { it.toLowerCase() }
        }

    override fun matches(method: WrappedMethod): Boolean {
        return method.args.size == 1 && method.name.substring("operator".length) == cppOp
    }

    override fun toString(): String {
        return "assignment($cppOp, $cOp, $kotlinOperatorType)"
    }

    companion object {
        val ASSIGN = BasicAssignmentOperator("=", "Assign", InfixMethod("assign"))
        val PLUS_EQUALS = BasicAssignmentOperator("+=", "PlusEquals", InfixMethod("plusEquals"))
    }
}

data class BasicUnaryOperator private constructor(
    private val cppOp: String,
    private val cOp: String,
    override val kotlinOperatorType: KotlinOperatorType
) : Operator() {

    override fun name(namer: Namer, cls: WrappedClass, method: WrappedMethod): String =
        with(namer) {
            return cName + "_op_" + cOp.splitCamelcase().joinToString("_") { it.toLowerCase() }
        }

    override fun matches(method: WrappedMethod): Boolean {
        return method.args.isEmpty() && method.name.substring("operator".length) == cppOp
    }

    override fun toString(): String {
        return "unary($cppOp, $cOp, $kotlinOperatorType)"
    }

    companion object {
        val INC = BasicUnaryOperator("++", "Increment", KotlinOperator("inc"))
        val DEC = BasicUnaryOperator("--", "Decrement", KotlinOperator("dec"))
        val UNARY_MINUS = BasicUnaryOperator("-", "UnaryMinus", KotlinOperator("unaryMinus"))
        val UNARY_PLUS = BasicUnaryOperator("+", "UnaryPlus", KotlinOperator("unaryPlus"))
        val NOT = BasicUnaryOperator("!", "Not", KotlinOperator("not"))
        val INV = BasicUnaryOperator("~", "Inv", BasicMethod("inv"))
        val REFERENCE = BasicUnaryOperator("*", "Reference", BasicMethod("reference"))
        val POINTER_REFERENCE =
            BasicUnaryOperator("->", "PointerReference", BasicMethod("pointer_reference"))
    }
}
