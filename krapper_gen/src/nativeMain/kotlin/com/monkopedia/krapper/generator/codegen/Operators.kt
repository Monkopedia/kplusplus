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

    // Shared base for the operators whose C wrapper name is derived mechanically
    // from a C-spelling fragment (`cOp`): `<cName>_op_<cOp split + lowercased>`.
    // ConversionOperator does not extend this — it names by destination type.
    abstract class NamedByCOp : Operator() {
        protected abstract val cOp: String

        override fun name(namer: Namer, method: WrappedMethod): String = with(namer) {
            cName + "_op_" + cOp.splitCamelcase().joinToString("_") { it.lowercase() }
        }
    }

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
            BasicCallOperator.CALL,
            ConversionOperator.CONVERSION
        )

        fun from(method: WrappedMethod): Operator? {
            if (!method.name.startsWith("operator")) return null
            return ALL_OPERATORS.find { it.matches(method) }
        }
    }
}

data class BasicBinaryOperator private constructor(
    val cppOp: String,
    override val cOp: String,
    override val resolvedOperator: ResolvedOperator,
    val supportsDirectCall: Boolean = true
) : Operator.NamedByCOp() {

    override fun matches(method: WrappedMethod): Boolean =
        method.args.size == 1 && method.name.substring("operator".length) == cppOp

    override fun toString(): String = "binary($cppOp, $cOp, $resolvedOperator)"

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
            ResolvedOperator.POST_INC,
            supportsDirectCall = false
        )
        val POST_DEC = BasicBinaryOperator(
            "--",
            "PostDecrement",
            ResolvedOperator.POST_DEC,
            supportsDirectCall = false
        )
    }
}

data class BasicAssignmentOperator private constructor(
    private val cppOp: String,
    override val cOp: String,
    override val resolvedOperator: ResolvedOperator
) : Operator.NamedByCOp() {

    override fun matches(method: WrappedMethod): Boolean =
        method.args.size == 1 && method.name.substring("operator".length) == cppOp

    override fun toString(): String = "assignment($cppOp, $cOp, $resolvedOperator)"

    companion object {
        val ASSIGN = BasicAssignmentOperator("=", "Assign", ResolvedOperator.ASSIGN)
        val PLUS_EQUALS = BasicAssignmentOperator("+=", "PlusEquals", ResolvedOperator.PLUS_EQUALS)
    }
}

data class BasicCallOperator private constructor(
    private val cppOp: String,
    override val cOp: String,
    override val resolvedOperator: ResolvedOperator
) : Operator.NamedByCOp() {

    // operator() can have any arity; match on name alone.
    override fun matches(method: WrappedMethod): Boolean =
        method.name.substring("operator".length) == cppOp

    override fun toString(): String = "call($cppOp, $cOp, $resolvedOperator)"

    companion object {
        val CALL = BasicCallOperator("()", "Call", ResolvedOperator.CALL)
    }
}

// A user-defined conversion operator to a primitive scalar, e.g. `explicit
// operator double() const`. It has no parameters and its clang spelling is
// `operator <target>` (the target being the C++ destination type). Only the
// scalar targets that map to an idiomatic Kotlin `toX()` are handled; conversions
// to class/pointer/reference types are intentionally left unmatched (and so are
// dropped, as before) — those would need wrapper/placement-new handling.
data class ConversionOperator private constructor(override val resolvedOperator: ResolvedOperator) :
    Operator() {

    // The C wrapper name encodes the destination so multiple conversions on one
    // class (e.g. `operator double` + `operator int`) don't collide.
    override fun name(namer: Namer, method: WrappedMethod): String = with(namer) {
        return cName + "_op_to_" + targetOf(method)!!
    }

    override fun matches(method: WrappedMethod): Boolean =
        method.args.isEmpty() && targetOf(method) != null

    override fun toString(): String = "conversion($resolvedOperator)"

    companion object {
        val CONVERSION = ConversionOperator(ResolvedOperator.CONVERSION)

        // C++ conversion-target spelling -> C-identifier suffix. Restricted to the
        // primitive scalars with a clean Kotlin `toX()`. `bool` maps to `boolean`
        // (the Kotlin idiom `toBoolean()`), the rest to their Kotlin lowercase name.
        private val SCALAR_TARGETS = mapOf(
            "double" to "double",
            "float" to "float",
            "int" to "int",
            "long" to "long",
            "bool" to "boolean"
        )

        // The C-identifier suffix for a method's conversion target, or null if the
        // method isn't an `operator <known-scalar>`.
        fun targetOf(method: WrappedMethod): String? {
            if (!method.name.startsWith("operator ")) return null
            val target = method.name.substring("operator ".length).trim()
            return SCALAR_TARGETS[target]
        }
    }
}

data class BasicUnaryOperator private constructor(
    private val cppOp: String,
    override val cOp: String,
    override val resolvedOperator: ResolvedOperator
) : Operator.NamedByCOp() {

    override fun matches(method: WrappedMethod): Boolean =
        method.args.isEmpty() && method.name.substring("operator".length) == cppOp

    override fun toString(): String = "unary($cppOp, $cOp, $resolvedOperator)"

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
