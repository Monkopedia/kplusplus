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

import clang.CXCursor
import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.resolved_model.ResolvedArgument
import com.monkopedia.krapper.generator.resolved_model.ResolvedField
import com.monkopedia.krapper.generator.resolved_model.ResolvedFieldGetter
import com.monkopedia.krapper.generator.resolved_model.ResolvedFieldSetter
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import kotlinx.cinterop.CValue
import kotlinx.serialization.Transient

data class WrappedField(
    val name: String,
    val type: WrappedType
) : WrappedElement() {
    @Transient
    internal val other = Any()

    constructor(field: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        field.referenced.spelling.toKString() ?: error("Can't find name for $field"),
        WrappedType(field.type, resolverBuilder)
    )

    override fun clone(): WrappedElement {
        return WrappedField(name, type).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    override fun toString(): String {
        return "$name: $type"
    }

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
                } else mappedType
            val needsDereference =
                !type.isPointer && !type.isNative && type != WrappedType.LONG_DOUBLE
            val wrappedArgType = if (needsDereference) WrappedType.pointerTo(type) else type
            val argType = resolverContext.resolve(wrappedArgType)
                ?: return resolverContext.notifyFailed(
                    this@WrappedField,
                    wrappedArgType,
                    "Arg type"
                )
            return ResolvedField(
                name,
                type.isConst,
                ResolvedFieldGetter(
                    uniqueCGetter,
                    determineReturnStyle(type, resolverContext),
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
