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
package com.monkopedia.krapper.generator.resolvedmodel

import com.monkopedia.krapper.FilterableTypes
import com.monkopedia.krapper.ResolvedOperator
import com.monkopedia.krapper.TypeTarget
import com.monkopedia.krapper.generator.resolvedmodel.AllocationStyle.DIRECT
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.METHOD
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCppType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MethodType {
    CONSTRUCTOR,
    DESTRUCTOR,
    METHOD,
    STATIC_OP,
    STATIC,
    SIZE_OF,
    ALIGN_OF
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

    // Returns an enum value: the wrapper's C signature returns the underlying
    // integer, so the call result (the real enum type) is cast back to it. Needed
    // because a scoped enum does not implicitly convert to its integer.
    ENUM_RETURN
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
    ): ResolvedMethod = ResolvedConstructor(
        name,
        returnType,
        isCopyConstructor,
        isDefaultConstructor,
        uniqueCName,
        args.map { it.copy() },
        allocationStyle
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
    ): ResolvedConstructor = ResolvedConstructor(
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
    ): ResolvedDestructor = ResolvedDestructor(
        name,
        returnType,
        uniqueCName,
        args.map { it.copy() }
    ).also {
        it.parent = parent
        it.isVirtual = isVirtual
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

    // Mirror of WrappedMethod.isVirtual: true for a C++ `virtual` method (incl.
    // overrides). Used by the inheritance-flatten naming rule (decision A) to tell
    // a virtual override (one logical method, bare name) from a non-virtual shadow
    // (class-prefixed). A `var` so `resolve()`/`copy()` propagate it (it isn't a
    // primary-constructor param); also serialized for the model round-trip.
    var isVirtual: Boolean = false

    // Mirror of WrappedMethod.returnsPairSecond: the underlying C++ method returns
    // `std::pair<iterator, bool>` whose iterator half can't be wrapped, so the
    // returnType has been rewritten to `bool` and CppWriter must emit the call's
    // `.second` member. A `var` so resolve()/copy() propagate it; serialized for the
    // model round-trip.
    var returnsPairSecond: Boolean = false

    // Mirror of WrappedMethod.returnViaMemberCall: a non-owning view return (e.g.
    // llvm::StringRef) whose returnType was rewritten to an owned type (std::string);
    // CppWriter emits this member call (e.g. `.str()`) on the result to materialize it.
    // A `var` so resolve()/copy() propagate it; serialized for the model round-trip.
    var returnViaMemberCall: String? = null

    // Mirror of WrappedMethod.rangeElementType: an `llvm::iterator_range<It>` return
    // materialized into a bound `std::vector<Elem*>` (T1.3); holds Elem's fully-qualified
    // C++ spelling. When non-null, CppWriter emits a `r.begin()`/`r.end()` loop building
    // the vector instead of a direct call. A `var` so resolve()/copy() propagate it;
    // serialized for the model round-trip.
    var rangeElementType: String? = null

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
    ): ResolvedMethod = ResolvedMethod(
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
        it.isVirtual = isVirtual
        it.returnsPairSecond = returnsPairSecond
        it.returnViaMemberCall = returnViaMemberCall
        it.rangeElementType = rangeElementType
    }

    override fun cloneWithoutChildren(): ResolvedMethod = copy()

    override fun toString(): String = "fun $name(${args.joinToString(", ")}): $returnType"

    companion object : TypeTarget<ResolvedMethod>(FilterableTypes.METHOD, ResolvedMethod::class)
}

enum class ArgumentCastMode {
    NATIVE,
    STRING,

    // T1.10p: an inbound non-owning string view (e.g. `llvm::StringRef`). The Kotlin side
    // passes a `String`, the C boundary takes a `const char*`, and the wrapper constructs the
    // view from it at the call site (`llvm::StringRef arg_cast = llvm::StringRef(arg)`). The
    // inbound inverse of T1.10's outbound StringRef->std::string return marshalling. The view
    // type's C++ spelling is carried on the argument's signatureType.typeString.
    STRING_VIEW,
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
    var hasDefault: Boolean,
    // The C++ source text of the parameter's default-value expression (e.g. "10",
    // "' '", "true", "nullptr"), or null when absent or for synthetic args (thiz,
    // location, callback). KotlinWriter maps a renderable subset to a Kotlin default.
    var defaultValue: String? = null
) {

    override fun toString(): String = "$name: $type"

    override fun equals(other: Any?): Boolean =
        (other as? ResolvedArgument)?.name == name && other.type == type
}
