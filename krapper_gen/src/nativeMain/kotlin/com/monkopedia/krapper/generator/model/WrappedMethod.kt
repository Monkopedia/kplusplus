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
package com.monkopedia.krapper.generator.model

import clang.CXCursor
import clang.CXCursorKind
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.getArgument
import com.monkopedia.krapper.generator.isStatic
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.numArguments
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.result
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import kotlinx.cinterop.CValue
import kotlinx.serialization.Serializable

enum class MethodType {
    CONSTRUCTOR,
    DESTRUCTOR,
    METHOD,
    STATIC_OP
}

@Serializable
data class WrappedMethod(
    val name: String,
    val returnType: WrappedTypeReference,
    val args: List<WrappedArgument>,
    val isStatic: Boolean,
    val methodType: MethodType
) {
    constructor(method: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        method.referenced.spelling.toKString() ?: error("Can't find name of $method"),
        WrappedTypeReference(method.type.result, resolverBuilder),
        (0 until method.numArguments).map {
            WrappedArgument(method.getArgument(it.toUInt()), resolverBuilder)
        },
        method.isStatic,
        when (method.kind) {
            CXCursorKind.CXCursor_Constructor -> MethodType.CONSTRUCTOR
            CXCursorKind.CXCursor_Destructor -> MethodType.DESTRUCTOR
            else -> MethodType.METHOD
        }
    )

    override fun toString(): String {
        return "${if (isStatic) "static " else ""}fun $name(${args.joinToString(", ")}): " +
            returnType
    }
}

@Serializable
data class WrappedArgument(val name: String, val type: WrappedTypeReference) {
    constructor(arg: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        arg.spelling.toKString() ?: error("Can't find name of $arg"),
        WrappedTypeReference(arg.type, resolverBuilder)
    )

    override fun toString(): String {
        return "$name: $type"
    }
}
