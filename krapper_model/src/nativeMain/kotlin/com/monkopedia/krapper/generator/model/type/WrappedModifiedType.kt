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

class WrappedModifiedType(val baseType: WrappedType, val modifier: String) : WrappedType() {
    override val isReturnable: Boolean
        get() = modifier == "*" || modifier == "&" || baseType.isReturnable
    override val cType: WrappedType
        get() = if (baseType.isString) {
            baseType.cType
        } else {
            when (modifier) {
                "*",
                "&" -> pointerTo(
                    if (baseType.isNative || (baseType == LONG_DOUBLE)) {
                        baseType.cType
                    } else {
                        VOID
                    }
                )

                "[]" -> arrayOf(baseType.cType)

                else -> error("Don't know how to handle $modifier")
            }
        }

    override val isNative: Boolean
        get() = baseType.isNative

    // A reference to an enum is still an enum value at the boundary (the reference
    // is stripped before resolution); a pointer/array to one is a real pointer.
    override val isEnum: Boolean
        get() = modifier == "&" && baseType.isEnum
    override val isString: Boolean
        get() = baseType.isString

    override val isVoid: Boolean
        get() = false

    override val pointed: WrappedType
        get() = if (modifier == "*") baseType else error("Cannot find pointed of non-pointer $this")
    override val isPointer: Boolean
        get() = ((this as? WrappedModifiedType)?.modifier == "*")

    override val isArray: Boolean
        get() = modifier == "[]"

    override val unreferenced: WrappedType
        get() = if (modifier == "&") baseType else error("Cannot unreference non-reference $this")

    override val isReference: Boolean
        get() = modifier == "&"
    override val isConst: Boolean
        get() = baseType.isConst
    override val unconst: WrappedType
        get() = WrappedModifiedType(baseType.unconst, modifier)

    // A pointer/reference/array applied on top of a base type. The base renders itself,
    // then this level's `*`/`&`/`[]` is appended.
    //
    // One base shape needs care: a `const`-qualified POINTER (`WrappedPrefixedType(ptr,
    // "const")`, i.e. a `T* const` — the top-level const TypeBuilder re-applies to a pointer
    // via maybeConst). That base renders itself WEST as `const T*`; appending this level's
    // `*` yields `const T**`, which C++ re-parses as the DIFFERENT type
    // pointer-to-(pointer-to-const-T) — the pointee's const wrongly migrated one level in.
    // The intended type is pointer-to-(`T* const`), which C++ can only spell EAST as
    // `T* const*`. So when the base is exactly a const-qualified pointer, spell that base
    // east before appending. (A bare top-level `const T*` return/param base is never wrapped
    // by an outer modifier, so its idiomatic west spelling is untouched — see the clangwalk
    // slice, unchanged.) Reached e.g. by ArrayRef<T*>'s `const T* data` ctor arg with
    // T = `IdentifierInfo*`: `const T*` -> `IdentifierInfo* const*`, matching the ArrayRef
    // `const T *` constructor instead of failing with "no matching constructor".
    override fun toString(): String {
        val base = baseType
        val baseSpelling =
            if (base is WrappedPrefixedType &&
                base.modifier == "const" &&
                base.baseType.isPointer
            ) {
                "${base.baseType} ${base.modifier}"
            } else {
                base.toString()
            }
        return "$baseSpelling$modifier"
    }
}

class WrappedPrefixedType(val baseType: WrappedType, val modifier: String) : WrappedType() {
    override val isReturnable: Boolean
        get() = baseType.isReturnable
    override val cType: WrappedType
        get() = if (baseType.isString) {
            baseType.cType
        } else {
            when (modifier) {
                "const" -> const(baseType.cType)
                else -> error("Don't know how to handle $modifier")
            }
        }

    override val isNative: Boolean
        get() = baseType.isNative

    // A const enum (by value or reference) is still an enum at the boundary.
    override val isEnum: Boolean
        get() = baseType.isEnum
    override val isString: Boolean
        get() = baseType.isString

    override val isVoid: Boolean
        get() = false

    override val pointed: WrappedType
        get() =
            if (baseType.isPointer) {
                WrappedPrefixedType(baseType.pointed, modifier)
            } else {
                error("Cannot find pointed of non-pointer $this")
            }
    override val isPointer: Boolean
        get() = baseType.isPointer

    override val isArray: Boolean
        get() = baseType.isArray

    override val unreferenced: WrappedType
        get() = const(baseType.unreferenced)

    override val isReference: Boolean
        get() = baseType.isReference
    override val isConst: Boolean
        get() = modifier == "const" || baseType.isConst
    override val unconst: WrappedType
        get() =
            if (modifier == "const") {
                baseType
            } else {
                WrappedPrefixedType(baseType.unconst, modifier)
            }

    override fun toString(): String = "$modifier $baseType"
}
