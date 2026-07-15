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

class WrappedTemplateType(val baseType: WrappedType, rawTemplateArgs: List<WrappedType>) :
    WrappedType() {

    // A defaulted STL policy argument (comparator / hasher / equality / allocator) can reach
    // us as a BARE class-template reference — e.g. `std::map<K, V>`'s comparator surfaces as
    // the argument-less `std::less` rather than `std::less<K>` — because the front-end lost
    // the default argument's own template arguments. Emitting `std::map<K, V, std::less>`
    // fails to compile ("use of class template 'std::less' requires template arguments").
    // A bare policy name only ever appears in this defaulted-trailing position (an explicit
    // policy carries its args), so dropping such trailing arguments falls back to the C++
    // default — `std::map<K, V, std::less<K>>` — which is exactly the intended type. Trimming
    // the args list here keeps the C++ spelling (toString) and the Kotlin name mangling (which
    // both read templateArgs) consistent. (broad-surface #10)
    val templateArgs: List<WrappedType> = rawTemplateArgs.dropLastWhile {
        it is WrappedTypeReference && it.name in DEFAULTED_POLICY_TEMPLATES
    }
    override val cType: WrappedType
        get() {
            baseType.cType
            templateArgs.forEach { it.cType }
            return pointerTo(VOID)
        }

    init {
        // A base whose spelling already ends in this exact `<args>` is a self-referential
        // (looping) template we can't model; fail loudly rather than build a degenerate type.
        require(!baseType.toString().endsWith("<${templateArgs.joinToString(", ")}>")) {
            "Looping templates $baseType $templateArgs"
        }
    }

    override val isReturnable: Boolean
        get() = false
    override val isNative: Boolean
        get() = false
    override val isString: Boolean
        get() = false

    override val isVoid: Boolean
        get() = false

    override val pointed: WrappedType
        get() = error("Cannot get pointee of non-pointer templated $this")
    override val isPointer: Boolean
        get() = false

    override val isArray: Boolean
        get() = false

    override val unreferenced: WrappedType
        get() = error("Cannot get unreference of non-reference templated $this")

    override val isReference: Boolean
        get() = false
    override val isConst: Boolean
        get() = false
    override val unconst: WrappedTypeReference
        get() = error("Cannot unconst non-const templated $this")

    override fun toString(): String = "$baseType<${templateArgs.joinToString(", ")}>"

    companion object {
        // STL policy class templates that appear as DEFAULTED trailing container arguments.
        // When one reaches us bare (no `<...>`), it is a dropped default we trim so C++ can
        // re-supply it. An explicitly-spelled policy always carries its own arguments and so
        // is a WrappedTemplateType, never a bare WrappedTypeReference — it is never trimmed.
        private val DEFAULTED_POLICY_TEMPLATES = setOf(
            "std::less",
            "std::allocator",
            "std::hash",
            "std::equal_to"
        )
    }
}
