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

import com.monkopedia.krapper.FilterableTypes
import com.monkopedia.krapper.ResolvedOperator
import com.monkopedia.krapper.TypeTarget
import com.monkopedia.krapper.generator.resolved_model.AllocationStyle.DIRECT
import com.monkopedia.krapper.generator.resolved_model.MethodType.METHOD
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCppType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MethodType {
    CONSTRUCTOR,
    DESTRUCTOR,
    METHOD,
    STATIC_OP,
    STATIC,
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
    RETURN_REFERENCE,
}

enum class AllocationStyle {
    DIRECT,
    STACK
}

@Serializable
@SerialName("constructor")
class ResolvedConstructor : ResolvedMethod {
    var isCopyConstructor: Boolean = false
    var isDefaultConstructor: Boolean = false
    var allocationStyle: AllocationStyle = DIRECT

    constructor(
        name: String,
        returnType: ResolvedCppType,
        isCopyConstructor: Boolean,
        isDefaultConstructor: Boolean,
        uniqueCName: String?,
        args: List<ResolvedArgument>,
        allocationStyle: AllocationStyle
    ) : super(
        name,
        returnType,
        MethodType.CONSTRUCTOR,
        uniqueCName,
        null,
        args,
        ReturnStyle.VOIDP,
        false,
        returnType.typeString
    ) {
        this.isCopyConstructor = isCopyConstructor
        this.isDefaultConstructor = isDefaultConstructor
        this.allocationStyle = allocationStyle
    }

    fun copy(
        name: String = this.name,
        returnType: ResolvedCppType = this.returnType,
        uniqueCName: String? = this.uniqueCName,
        args: List<ResolvedArgument> = this.args,
        isDefaultConstructor: Boolean = this.isDefaultConstructor,
        isCopyConstructor: Boolean = this.isCopyConstructor,
        allocationStyle: AllocationStyle = this.allocationStyle
    ): ResolvedMethod {
        return ResolvedConstructor(
            name,
            returnType,
            isCopyConstructor,
            isDefaultConstructor,
            uniqueCName,
            args.map { it.copy() },
            allocationStyle
        )
    }

    override fun copy(
        name: String,
        returnType: ResolvedCppType,
        methodType: MethodType,
        uniqueCName: String?,
        operator: ResolvedOperator?,
        args: List<ResolvedArgument>,
        returnStyle: ReturnStyle,
        argCastNeedsPointer: Boolean,
        qualified: String
    ): ResolvedConstructor {
        return ResolvedConstructor(
            name,
            returnType,
            isCopyConstructor,
            isDefaultConstructor,
            uniqueCName,
            args.map { it.copy() },
            allocationStyle
        ).also {
            it.parent = parent
        }
    }
}

@Serializable
@SerialName("destructor")
class ResolvedDestructor : ResolvedMethod {
    constructor(
        name: String,
        returnType: ResolvedCppType,
        uniqueCName: String?,
        args: List<ResolvedArgument>
    ) : super(
        name,
        returnType,
        MethodType.DESTRUCTOR,
        uniqueCName,
        null,
        args,
        ReturnStyle.VOID,
        false,
        returnType.typeString
    )

    override fun copy(
        name: String,
        returnType: ResolvedCppType,
        methodType: MethodType,
        uniqueCName: String?,
        operator: ResolvedOperator?,
        args: List<ResolvedArgument>,
        returnStyle: ReturnStyle,
        argCastNeedsPointer: Boolean,
        qualified: String
    ): ResolvedDestructor {
        return ResolvedDestructor(
            name,
            returnType,
            uniqueCName,
            args.map { it.copy() }
        ).also {
            it.parent = parent
        }
    }
}

@Serializable
@SerialName("method")
open class ResolvedMethod(
    var name: String,
    var returnType: ResolvedCppType,
    var methodType: MethodType = METHOD,
    var uniqueCName: String?,
    var operator: ResolvedOperator?,
    var args: List<ResolvedArgument>,
    var returnStyle: ReturnStyle,
    var argCastNeedsPointer: Boolean,
    var qualified: String
) : ResolvedElement() {

    open fun copy(
        name: String = this.name,
        returnType: ResolvedCppType = this.returnType,
        methodType: MethodType = this.methodType,
        uniqueCName: String? = this.uniqueCName,
        operator: ResolvedOperator? = this.operator,
        args: List<ResolvedArgument> = this.args,
        returnStyle: ReturnStyle = this.returnStyle,
        argCastNeedsPointer: Boolean = this.argCastNeedsPointer,
        qualified: String = this.qualified
    ): ResolvedMethod {
        return ResolvedMethod(
            name,
            returnType,
            methodType,
            uniqueCName,
            operator,
            args.map { it.copy() },
            returnStyle,
            argCastNeedsPointer,
            qualified
        ).also {
            it.parent = parent
        }
    }

    override fun cloneWithoutChildren(): ResolvedMethod {
        return copy()
    }

    override fun toString(): String {
        return "fun $name(${args.joinToString(", ")}): $returnType"
    }

    companion object : TypeTarget<ResolvedMethod>(FilterableTypes.METHOD, ResolvedMethod::class)
}

enum class ArgumentCastMode {
    NATIVE,
    STRING,
    REINT_CAST,
    RAW_CAST,
    STD_MOVE
}

@Serializable
data class ResolvedArgument(
    val name: String,
    var type: ResolvedCppType,
    var signatureType: ResolvedCppType,
    var usr: String = "",
    var castMode: ArgumentCastMode,
    var needsDereference: Boolean,
    var hasDefault: Boolean
) {

    override fun toString(): String {
        return "$name: $type"
    }

    override fun equals(other: Any?): Boolean {
        return (other as? ResolvedArgument)?.name == name && other.type == type
    }
}
