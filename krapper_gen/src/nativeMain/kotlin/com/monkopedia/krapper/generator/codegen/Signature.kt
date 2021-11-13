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

import com.monkopedia.krapper.generator.builders.FunctionBuilder
import com.monkopedia.krapper.generator.builders.LangFactory
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.builders.Symbol
import com.monkopedia.krapper.generator.builders.dereference
import com.monkopedia.krapper.generator.builders.reference
import com.monkopedia.krapper.generator.builders.type
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.VOID
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo

private const val BETWEEN_LOWER_AND_UPPER = "(?<=\\p{Ll})(?=\\p{Lu})"
private const val BEFORE_UPPER_AND_LOWER = "(?<=\\p{L})(?=\\p{Lu}\\p{Ll})"
private val REGEX = Regex("$BETWEEN_LOWER_AND_UPPER|$BEFORE_UPPER_AND_LOWER")
fun String.splitCamelcase(): List<String> {
    return split(REGEX)
}

inline fun <T : LangFactory> FunctionBuilder<T>.generateMethodSignature(
    type: WrappedType,
    method: WrappedMethod,
    namer: Namer
) = with(namer) {
    when (method.methodType) {
        MethodType.CONSTRUCTOR -> {
            name = method.uniqueCName
            retType = functionBuilder.type(pointerTo(type).cType)
        }
        MethodType.DESTRUCTOR -> {
            retType = null
            name = method.uniqueCName
        }
        MethodType.STATIC_OP,
        MethodType.METHOD -> {
            retType = method.returnType.takeIf { it.isReturnable }?.cType?.let(
                functionBuilder::type
            )
            name = method.uniqueCName
        }
    }
}

data class WrapperArgument(
    val arg: WrappedArgument?,
    val targetType: WrappedType,
    val localVar: LocalVar,
    val needsDereference: Boolean
) {
    val reference: Symbol
        get() = if (needsDereference) localVar.dereference else localVar.reference
    val pointerReference: Symbol
        get() {
            require(needsDereference) {
                "Can't reference non-pointer..."
            }
            return localVar.reference
        }
}

inline fun <T : LangFactory> FunctionBuilder<T>.addArgs(
    type: WrappedType,
    method: WrappedMethod
): List<WrapperArgument> {
    return listOfNotNull(
        if (method.methodType != MethodType.CONSTRUCTOR) WrapperArgument(
            null,
            pointerTo(type),
            define("thiz", pointerTo(type).cType),
            true
        )
        else null
    ) + method.args.map {
        val type = if (it.type.isReference) it.type.unreferenced else it.type
        if (type.isPointer || type.isNative) {
            WrapperArgument(it, type, define(it.name, type.cType), false)
        } else {
            val type = pointerTo(type)
            WrapperArgument(it, type, define(it.name, type.cType), true)
        }
    } + listOfNotNull(
        if (method.methodType == MethodType.METHOD && !method.returnType.isReturnable) {
            val type = method.returnType
            if (type.isPointer) {
                WrapperArgument(null, type, define("ret_value", type.cType), false)
            } else {
                val type = pointerTo(type)
                WrapperArgument(null, type, define("ret_value", type.cType), true)
            }
        } else null
    )
}

inline fun <T : LangFactory> FunctionBuilder<T>.generateFieldGet(
    type: WrappedType,
    field: WrappedField,
    namer: Namer
): List<WrapperArgument> = with(namer) {
    if (field.type.isArray) {
        throw UnsupportedOperationException("Arrays are not supported")
    }
    name = field.uniqueCGetter
    retType =
        if (field.type.isReturnable) functionBuilder.type(field.type.cType)
        else functionBuilder.type(VOID)
    val thiz = WrapperArgument(null, pointerTo(type), define("thiz", pointerTo(type).cType), true)
    return listOfNotNull(
        thiz,
        if (!field.type.isReturnable) WrapperArgument(
            null,
            field.type,
            define("ret_value", field.type.cType),
            true
        )
        else null
    )
}

inline fun <T : LangFactory> FunctionBuilder<T>.generateFieldSet(
    type: WrappedType,
    field: WrappedField,
    namer: Namer
): List<WrapperArgument> = with(namer) {
    if (field.type.isArray) {
        throw UnsupportedOperationException("Arrays are not supported")
    }
    name = field.uniqueCSetter
    retType = functionBuilder.type(VOID)
    val thiz = WrapperArgument(null, pointerTo(type), define("thiz", pointerTo(type).cType), true)
    val type = field.type
    val value = if (type.isPointer || type.isReturnable) {
        WrapperArgument(null, type, define("ret_value", type.cType), false)
    } else {
        val type = pointerTo(type)
        WrapperArgument(null, type, define("ret_value", type.cType), true)
    }
    return listOf(thiz, value)
}
