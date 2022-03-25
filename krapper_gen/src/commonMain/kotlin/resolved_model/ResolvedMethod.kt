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
package com.monkopedia.krapper.generator.resolved_model

import com.monkopedia.krapper.ResolvedOperator
import com.monkopedia.krapper.generator.resolved_model.MethodType.METHOD
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCppType

enum class MethodType {
    CONSTRUCTOR,
    DESTRUCTOR,
    METHOD,
    STATIC_OP,
    SIZE_OF
}

enum class ReturnStyle {
    VOID,
    VOIDP,
    VOIDP_REFERENCE,
    ARG_CAST,
    STRING,
    STRING_POINTER,
    COPY_CONSTRUCTOR,
    RETURN,
}

class ResolvedConstructor(
    name: String,
    returnType: ResolvedCppType,
    var isCopyConstructor: Boolean,
    var isDefaultConstructor: Boolean,
    uniqueCName: String?,
    args: List<ResolvedArgument>
) : ResolvedMethod(
    name,
    returnType,
    MethodType.CONSTRUCTOR,
    uniqueCName,
    null,
    args,
    ReturnStyle.VOIDP,
    false
)

class ResolvedDestructor(
    name: String,
    returnType: ResolvedCppType,
    uniqueCName: String?,
    args: List<ResolvedArgument>
) : ResolvedMethod(
    name,
    returnType,
    MethodType.DESTRUCTOR,
    uniqueCName,
    null,
    args,
    ReturnStyle.VOID,
    false
)

open class ResolvedMethod(
    var name: String,
    var returnType: ResolvedCppType,
    var methodType: MethodType = METHOD,
    var uniqueCName: String?,
    var operator: ResolvedOperator?,
    var args: List<ResolvedArgument>,
    var returnStyle: ReturnStyle,
    var argCastNeedsPointer: Boolean
) : ResolvedElement() {

    fun copy(
        name: String = this.name,
        returnType: ResolvedCppType = this.returnType,
        methodType: MethodType = this.methodType,
        uniqueCName: String? = this.uniqueCName,
        operator: ResolvedOperator? = this.operator,
        args: List<ResolvedArgument> = this.args,
        returnStyle: ReturnStyle = this.returnStyle,
        argCastNeedsPointer: Boolean = this.argCastNeedsPointer
    ): ResolvedMethod {
        return ResolvedMethod(
            name,
            returnType,
            methodType,
            uniqueCName,
            operator,
            args,
            returnStyle,
            argCastNeedsPointer
        )
    }

    override fun toString(): String {
        return "fun $name(${args.joinToString(", ")}): $returnType"
    }
}

enum class ArgumentCastMode {
    NATIVE,
    STRING,
    REINT_CAST,
    RAW_CAST,
    STD_MOVE
}

data class ResolvedArgument(
    val name: String,
    var type: ResolvedCppType,
    var signatureType: ResolvedCppType,
    var usr: String = "",
    var castMode: ArgumentCastMode,
    var needsDereference: Boolean,
) {

    override fun toString(): String {
        return "$name: $type"
    }

    override fun equals(other: Any?): Boolean {
        return (other as? ResolvedArgument)?.name == name && other.type == type
    }
}
