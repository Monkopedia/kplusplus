/*
 * Copyright 2022 Jason Monk
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

import com.monkopedia.krapper.ResolvedOperator
import com.monkopedia.krapper.generator.model.WrappedMethod

sealed class Operator {

    abstract val resolvedOperator: ResolvedOperator

    abstract fun name(namer: Namer, method: WrappedMethod): String
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
    override val resolvedOperator: ResolvedOperator,
    val supportsDirectCall: Boolean = true
) : Operator() {

    override fun name(namer: Namer, method: WrappedMethod): String =
        with(namer) {
            return cName + "_op_" + cOp.splitCamelcase().joinToString("_") { it.toLowerCase() }
        }

    override fun matches(method: WrappedMethod): Boolean {
        return method.args.size == 1 && method.name.substring("operator".length) == cppOp
    }

    override fun toString(): String {
        return "binary($cppOp, $cOp, $resolvedOperator)"
    }

    companion object {
        val MINUS = BasicBinaryOperator("-", "Minus", ResolvedOperator.MINUS)
        val PLUS = BasicBinaryOperator("+", "Plus", ResolvedOperator.PLUS)
        val TIMES = BasicBinaryOperator("*", "Times", ResolvedOperator.TIMES)
        val DIV = BasicBinaryOperator("/", "Divide", ResolvedOperator.DIV)
        val MOD = BasicBinaryOperator("%", "Mod", ResolvedOperator.MOD)
        val EQ = BasicBinaryOperator("==", "Eq", ResolvedOperator.EQ)
        val NEQ = BasicBinaryOperator("!=", "Neq", ResolvedOperator.NEQ)
        val LT = BasicBinaryOperator("<", "Lt", ResolvedOperator.LT)
        val GT = BasicBinaryOperator(">", "Gt", ResolvedOperator.GT)
        val LTEQ = BasicBinaryOperator("<=", "Lteq", ResolvedOperator.LTEQ)
        val GTEQ = BasicBinaryOperator(">=", "Gteq", ResolvedOperator.GTEQ)
        val BINARY_AND = BasicBinaryOperator("&&", "Binary_And", ResolvedOperator.BINARY_AND)
        val BINARY_OR = BasicBinaryOperator("||", "Binary_Or", ResolvedOperator.BINARY_OR)
        val AND = BasicBinaryOperator("&", "And", ResolvedOperator.AND)
        val OR = BasicBinaryOperator("|", "Or", ResolvedOperator.OR)
        val XOR = BasicBinaryOperator("^", "Xor", ResolvedOperator.XOR)
        val SHL = BasicBinaryOperator("<<", "Shl", ResolvedOperator.SHL)
        val SHR = BasicBinaryOperator(">>", "Shr", ResolvedOperator.SHR)
        val IND =
            BasicBinaryOperator("[]", "Ind", ResolvedOperator.IND, supportsDirectCall = false)
        val POST_INC = BasicBinaryOperator(
            "++",
            "PostIncrement",
            ResolvedOperator.POST_INC, supportsDirectCall = false
        )
        val POST_DEC = BasicBinaryOperator(
            "--",
            "PostDecrement",
            ResolvedOperator.POST_DEC, supportsDirectCall = false
        )
    }
}

data class BasicAssignmentOperator private constructor(
    private val cppOp: String,
    private val cOp: String,
    override val resolvedOperator: ResolvedOperator
) : Operator() {

    override fun name(namer: Namer, method: WrappedMethod): String =
        with(namer) {
            return cName + "_op_" + cOp.splitCamelcase().joinToString("_") { it.toLowerCase() }
        }

    override fun matches(method: WrappedMethod): Boolean {
        return method.args.size == 1 && method.name.substring("operator".length) == cppOp
    }

    override fun toString(): String {
        return "assignment($cppOp, $cOp, $resolvedOperator)"
    }

    companion object {
        val ASSIGN = BasicAssignmentOperator("=", "Assign", ResolvedOperator.ASSIGN)
        val PLUS_EQUALS = BasicAssignmentOperator("+=", "PlusEquals", ResolvedOperator.PLUS_EQUALS)
    }
}

data class BasicUnaryOperator private constructor(
    private val cppOp: String,
    private val cOp: String,
    override val resolvedOperator: ResolvedOperator
) : Operator() {

    override fun name(namer: Namer, method: WrappedMethod): String =
        with(namer) {
            return cName + "_op_" + cOp.splitCamelcase().joinToString("_") { it.toLowerCase() }
        }

    override fun matches(method: WrappedMethod): Boolean {
        return method.args.isEmpty() && method.name.substring("operator".length) == cppOp
    }

    override fun toString(): String {
        return "unary($cppOp, $cOp, $resolvedOperator)"
    }

    companion object {
        val INC = BasicUnaryOperator("++", "Increment", ResolvedOperator.INC)
        val DEC = BasicUnaryOperator("--", "Decrement", ResolvedOperator.DEC)
        val UNARY_MINUS = BasicUnaryOperator("-", "UnaryMinus", ResolvedOperator.UNARY_MINUS)
        val UNARY_PLUS = BasicUnaryOperator("+", "UnaryPlus", ResolvedOperator.UNARY_PLUS)
        val NOT = BasicUnaryOperator("!", "Not", ResolvedOperator.NOT)
        val INV = BasicUnaryOperator("~", "Inv", ResolvedOperator.INV)
        val REFERENCE = BasicUnaryOperator("*", "Reference", ResolvedOperator.REFERENCE)
        val POINTER_REFERENCE =
            BasicUnaryOperator("->", "PointerReference", ResolvedOperator.POINTER_REFERENCE)
    }
}
