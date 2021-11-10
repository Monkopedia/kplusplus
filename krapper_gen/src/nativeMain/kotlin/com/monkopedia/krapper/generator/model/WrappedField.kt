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
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.referenced
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
        WrappedTypeReference(field.type, resolverBuilder)
    )

    override fun clone(): WrappedElement {
        return WrappedField(name, type).also {
            it.children.addAll(children)
            it.parent = parent
        }
    }

    override fun toString(): String {
        return "$name: $type"
    }
}
