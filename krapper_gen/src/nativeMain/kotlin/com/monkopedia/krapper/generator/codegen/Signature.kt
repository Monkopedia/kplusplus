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

import com.monkopedia.krapper.generator.builders.Call
import com.monkopedia.krapper.generator.builders.FunctionBuilder
import com.monkopedia.krapper.generator.builders.LangFactory
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.builders.Symbol
import com.monkopedia.krapper.generator.builders.dereference
import com.monkopedia.krapper.generator.builders.reference
import com.monkopedia.krapper.generator.builders.type
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.REINT_CAST
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.STD_MOVE
import com.monkopedia.krapper.generator.resolved_model.MethodType
import com.monkopedia.krapper.generator.resolved_model.ResolvedArgument
import com.monkopedia.krapper.generator.resolved_model.ResolvedField
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.ARG_CAST
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedType.Companion.VOID

private const val BETWEEN_LOWER_AND_UPPER = "(?<=\\p{Ll})(?=\\p{Lu})"
private const val BEFORE_UPPER_AND_LOWER = "(?<=\\p{L})(?=\\p{Lu}\\p{Ll})"
private val REGEX = Regex("$BETWEEN_LOWER_AND_UPPER|$BEFORE_UPPER_AND_LOWER")
fun String.splitCamelcase(): List<String> {
    return split(REGEX)
}

inline fun <T : LangFactory> FunctionBuilder<T>.generateMethodSignature(
    method: ResolvedMethod
) {
    return when (method.methodType) {
        MethodType.CONSTRUCTOR -> {
            name = method.uniqueCName
            retType = functionBuilder.type(method.returnType.cType)
        }
        MethodType.DESTRUCTOR -> {
            retType = null
            name = method.uniqueCName
        }
        MethodType.SIZE_OF,
        MethodType.STATIC_OP,
        MethodType.METHOD -> {
            retType =
                method.returnType.takeIf {
                    method.returnStyle != ARG_CAST && method.returnStyle != ReturnStyle.VOID
                }?.cType?.let(functionBuilder::type)
            name = method.uniqueCName
        }
    }
}

data class SignatureArgument(
    val arg: ResolvedArgument,
    val localVar: LocalVar,
) {
    val targetType: ResolvedCppType
        get() = arg.signatureType
    private val needsDereference: Boolean
        get() = arg.needsDereference
    val reference: Symbol
        get() = when {
            arg.castMode == STD_MOVE -> Call("std::move", localVar.dereference)
            needsDereference -> localVar.dereference
            else -> localVar.reference
        }
    val pointerReference: Symbol
        get() {
            require(needsDereference) {
                "Can't reference non-pointer..."
            }
            return localVar.reference
        }
}

inline fun <T : LangFactory> FunctionBuilder<T>.addArgs(
    classLookup: ClassLookup,
    type: ResolvedType,
    method: ResolvedMethod
): List<SignatureArgument> {
    val args = if (method.returnStyle == ARG_CAST) method.args + ResolvedArgument(
        "ret_value",
        method.returnType,
        method.returnType,
        "",
        REINT_CAST,
        method.argCastNeedsPointer
    ) else method.args
    return args.map {
        defineWrapperArgument(it)
    }
}

fun <T : LangFactory> FunctionBuilder<T>.defineWrapperArgument(
    arg: ResolvedArgument
) = SignatureArgument(arg, define(arg.name, arg.signatureType.cType))

inline fun <T : LangFactory> FunctionBuilder<T>.generateFieldGet(
    field: ResolvedField
): List<SignatureArgument> {
    name = field.getter.uniqueCName
    retType =
        field.getter.returnType.takeIf {
            field.getter.returnStyle != ARG_CAST && field.getter.returnStyle != ReturnStyle.VOID
        }?.cType?.let(functionBuilder::type)
            ?: functionBuilder.type(VOID)
    val args = if (field.getter.returnStyle == ARG_CAST) field.getter.args + ResolvedArgument(
        "ret_value",
        field.getter.returnType,
        field.getter.returnType,
        "",
        REINT_CAST,
        field.getter.needsDereference
    ) else field.getter.args
    return args.map {
        defineWrapperArgument(it)
    }
}

inline fun <T : LangFactory> FunctionBuilder<T>.generateFieldSet(
    field: ResolvedField
): List<SignatureArgument> {
    name = field.setter.uniqueCName
    retType = functionBuilder.type(VOID)
    return field.setter.argument.map {
        defineWrapperArgument(it)
    }
}
