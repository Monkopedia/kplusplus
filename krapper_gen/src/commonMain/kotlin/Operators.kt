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
package com.monkopedia.krapper

sealed class KotlinOperatorType

data class KotlinOperator(val name: String) : KotlinOperatorType()
data class InfixMethod(val name: String) : KotlinOperatorType()
data class BasicWithDummyMethod(val name: String) : KotlinOperatorType()
data class BasicMethod(val name: String) : KotlinOperatorType()

enum class OperatorType {
    BINARY,
    ASSIGN,
    UNARY,
    REFERENCE
}

enum class ResolvedOperator(
    val cppOp: String,
    val cOp: String,
    val kotlinOperatorType: KotlinOperatorType,
    val operatorType: OperatorType,
    val supportsDirectCall: Boolean = false
) {
    MINUS("-", "Minus", KotlinOperator("minus"), OperatorType.BINARY, supportsDirectCall = true),
    PLUS("+", "Plus", KotlinOperator("plus"), OperatorType.BINARY, supportsDirectCall = true),
    TIMES("*", "Times", KotlinOperator("times"), OperatorType.BINARY, supportsDirectCall = true),
    DIV("/", "Divide", KotlinOperator("div"), OperatorType.BINARY, supportsDirectCall = true),
    MOD("%", "Mod", KotlinOperator("rem"), OperatorType.BINARY, supportsDirectCall = true),
    EQ("==", "Eq", InfixMethod("eq"), OperatorType.BINARY, supportsDirectCall = true),
    NEQ("!=", "Neq", InfixMethod("neq"), OperatorType.BINARY, supportsDirectCall = true),
    LT("<", "Lt", InfixMethod("lt"), OperatorType.BINARY, supportsDirectCall = true),
    GT(">", "Gt", InfixMethod("gt"), OperatorType.BINARY, supportsDirectCall = true),
    LTEQ("<=", "Lteq", InfixMethod("lteq"), OperatorType.BINARY, supportsDirectCall = true),
    GTEQ(">=", "Gteq", InfixMethod("gteq"), OperatorType.BINARY, supportsDirectCall = true),
    BINARY_AND(
        "&&",
        "Binary_And",
        InfixMethod("binAnd"),
        OperatorType.BINARY,
        supportsDirectCall = true
    ),
    BINARY_OR(
        "||",
        "Binary_Or",
        InfixMethod("binOr"),
        OperatorType.BINARY,
        supportsDirectCall = true
    ),
    AND("&", "And", InfixMethod("and"), OperatorType.BINARY, supportsDirectCall = true),
    OR("|", "Or", InfixMethod("or"), OperatorType.BINARY, supportsDirectCall = true),
    XOR("^", "Xor", InfixMethod("xor"), OperatorType.BINARY, supportsDirectCall = true),
    SHL("<<", "Shl", InfixMethod("shl"), OperatorType.BINARY, supportsDirectCall = true),
    SHR(">>", "Shr", InfixMethod("shr"), OperatorType.BINARY, supportsDirectCall = true),
    IND("[]", "Ind", KotlinOperator("get"), OperatorType.BINARY),
    POST_INC(
        "++",
        "PostIncrement",
        BasicWithDummyMethod("postIncrement"), OperatorType.BINARY
    ),
    POST_DEC(
        "--",
        "PostDecrement",
        BasicWithDummyMethod("postDecrement"), OperatorType.BINARY
    ),
    ASSIGN("=", "Assign", InfixMethod("assign"), OperatorType.ASSIGN),
    PLUS_EQUALS("+=", "PlusEquals", InfixMethod("plusEquals"), OperatorType.ASSIGN),
    INC("++", "Increment", KotlinOperator("inc"), OperatorType.UNARY),
    DEC("--", "Decrement", KotlinOperator("dec"), OperatorType.UNARY),
    UNARY_MINUS("-", "UnaryMinus", KotlinOperator("unaryMinus"), OperatorType.UNARY),
    UNARY_PLUS("+", "UnaryPlus", KotlinOperator("unaryPlus"), OperatorType.UNARY),
    NOT("!", "Not", KotlinOperator("not"), OperatorType.UNARY),
    INV("~", "Inv", BasicMethod("inv"), OperatorType.UNARY),
    REFERENCE("*", "Reference", BasicMethod("reference"), OperatorType.REFERENCE),
    POINTER_REFERENCE(
        "->",
        "PointerReference",
        BasicMethod("pointer_reference"),
        OperatorType.UNARY
    ),
}
