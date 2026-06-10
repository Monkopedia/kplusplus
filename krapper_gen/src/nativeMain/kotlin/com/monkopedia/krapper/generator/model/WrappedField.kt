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
package com.monkopedia.krapper.generator.model

import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedArgument
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedField
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedFieldGetter
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedFieldSetter
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle
import kotlinx.serialization.Transient

data class WrappedField(val name: String, val type: WrappedType) : WrappedElement() {
    @Transient
    internal val other = Any()

    override fun clone(): WrappedElement = WrappedField(name, type).also {
        it.addAllChildren(children)
        it.parent = parent
    }

    override fun toString(): String = "$name: $type"

    override suspend fun resolve(resolverContext: ResolveContext): ResolvedField? =
        with(resolverContext.currentNamer) {
            val (mappedType, resolvedType) = resolverContext.mapAndResolve(type)
                ?: return resolverContext.notifyFailed(this@WrappedField, type, "Field type")
            val type =
                if (mappedType.isReference) {
                    val unreferenced = mappedType.unreferenced
                    resolverContext.map(unreferenced)
                        ?: return resolverContext.notifyFailed(
                            this@WrappedField,
                            unreferenced,
                            "Field unreferenced type"
                        )
                } else {
                    mappedType
                }
            val needsDereference =
                !type.isPointer && !type.isNative && type != WrappedType.LONG_DOUBLE &&
                    // An enum crosses the boundary as its underlying integer (by value), not a
                    // dereferenced pointer — matching the method-return path. Without this an
                    // enum FIELD getter resolved `pointerTo(enum)`, losing the enum identity +
                    // package, so its `fromValue` call rendered as a mashed reference
                    // (`TranslationUnitKindCompanionfromValue`) and the type emitted as a wrapper.
                    !type.isEnum
            val getterStyle = determineReturnStyle(type, resolverContext)
            // T-skip: a by-value (ARG_CAST/Holder) field getter copy-constructs the member
            // into a Holder buffer (`new (buf) T(thiz->field)`). If T `= delete`s its copy
            // constructor that call won't compile, so the getter is unmodelable. A field
            // with no usable getter is useless, so DROP the whole field (skip-not-crash);
            // sibling fields/methods still bind.
            if (getterStyle == ReturnStyle.ARG_CAST && !resolverContext.canCopyConstruct(type)) {
                return resolverContext.notifyFailed(
                    this@WrappedField,
                    type,
                    "By-value field getter of a non-copy-constructible type (deleted copy ctor)"
                )
            }
            // T-skip: a field whose type `= delete`s its copy assignment can't be written
            // through the generated `field = *value` setter. Mark it unassignable (drop the
            // setter); the getter is unaffected. canAssign returns true for pointer/native/
            // implicitly-assignable types, so only genuinely non-assignable members drop.
            val setterUnassignable = !resolverContext.canAssign(type)
            val wrappedArgType = if (needsDereference) WrappedType.pointerTo(type) else type
            val argType = resolverContext.resolve(wrappedArgType)
                ?: return resolverContext.notifyFailed(
                    this@WrappedField,
                    wrappedArgType,
                    "Arg type"
                )
            return ResolvedField(
                name,
                // Mark the field unassignable (no `_set`) when it is const OR a
                // reference member: a reference can't be rebound, so assigning through
                // it would be a compile error just like a const member. `type` here has
                // already been unreferenced, so test the original field type for the
                // reference case. Also drop the setter when the field's type can't be
                // copy-assigned (deleted copy assignment — T-skip).
                type.isConst || this@WrappedField.type.isReference || setterUnassignable,
                ResolvedFieldGetter(
                    uniqueCGetter,
                    getterStyle,
                    argType,
                    listOf(
                        createThisArg(resolverContext) ?: return null
                    ),
                    needsDereference = needsDereference
                ),
                ResolvedFieldSetter(
                    uniqueCSetter,
                    listOf(
                        createThisArg(resolverContext) ?: return null,
                        ResolvedArgument(
                            "value",
                            resolvedType,
                            argType,
                            "",
                            determineArgumentCastMode(
                                type,
                                mappedType.isReference,
                                resolverContext
                            ),
                            needsDereference,
                            false
                        )
                    )
                )
            )
        }
}
