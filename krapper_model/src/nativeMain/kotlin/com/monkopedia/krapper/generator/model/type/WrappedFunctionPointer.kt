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
package com.monkopedia.krapper.generator.model.type

/**
 * A C function-pointer typedef (e.g. `typedef int (*IntTransform)(int);`) used as a
 * method parameter or return type. libclang surfaces these as a typedef over a
 * pointer-to-function-proto whose underlying declarator can't be resolved through the
 * normal paths, so the method would otherwise be silently dropped.
 *
 * We treat the pointer as an opaque NATIVE value that crosses the C boundary as-is.
 * [cName] is the UNqualified typedef spelling (`IntTransform`): it is re-declared
 * verbatim in the generated C interop header (which doesn't pull in the user header),
 * names the type at the extern-"C" boundary, and is what [toString] renders (so the
 * default rendering — used by the C header — stays valid C).
 *
 * [cppName] is the FULLY-QUALIFIED spelling (`nn::handler_t` for a namespace-scoped
 * typedef; identical to [cName] for a global one). It is what the generated C++ WRAPPER
 * (the `.cc`) renders instead: a namespace-scoped typedef written UNqualified
 * (`handler_t`) is "unknown type name 'handler_t'; did you mean 'nn::handler_t'?" outside
 * its namespace — the same family as the conversion-operator qualification fix. The
 * unqualified [cName] (extern-"C" declaration) and qualified [cppName] (wrapper
 * definition) are both aliases for the identical `R(*)(A...)`, so they stay
 * ABI-compatible. CppFactory selects [cppName] via its qualifyFunctionPointers flag.
 *
 * The Kotlin side surfaces it as a `CPointer<CFunction<...>>?` (see WrappedKotlinType).
 * [returnType] and [argTypes] are retained for that Kotlin rendering; no
 * trampoline/lambda bridge is built here.
 */
class WrappedFunctionPointer(
    val cName: String,
    val returnType: WrappedType,
    val argTypes: List<WrappedType>,
    val cppName: String = cName
) : WrappedType() {
    override val cType: WrappedType
        get() = this

    override val isNative: Boolean
        get() = true
    override val isString: Boolean
        get() = false
    override val isReturnable: Boolean
        get() = true
    override val isVoid: Boolean
        get() = false

    override val pointed: WrappedType
        get() = error("Cannot get pointee of function pointer $this")
    override val isPointer: Boolean
        get() = false

    override val isArray: Boolean
        get() = false

    override val unreferenced: WrappedType
        get() = error("Cannot unreference function pointer $this")

    override val isReference: Boolean
        get() = false
    override val isConst: Boolean
        get() = false
    override val unconst: WrappedType
        get() = error("Cannot unconst function pointer $this")

    override fun toString(): String = cName
}
