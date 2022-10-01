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
import com.monkopedia.krapper.generator.resolved_model.ResolvedNamespace

class WrappedNamespace(val namespace: String) : WrappedElement() {
    val fullyQualified: String
        get() = withParents.mapNotNull { it.named }.joinToString("::")
    private val WrappedElement.withParents: List<WrappedElement>
        get() = this@withParents.parent?.withParents?.plus(listOf(this@withParents))
            ?: listOf(this@withParents)
    private val WrappedElement.named: String?
        get() = when (this) {
            is WrappedClass -> this@named.name
            is WrappedNamespace -> this@named.namespace
            else -> null
        }

    override fun clone(): WrappedElement {
        return WrappedNamespace(namespace).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    override suspend fun resolve(resolverContext: ResolveContext): ResolvedNamespace {
        return ResolvedNamespace(namespace).also {
            it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
        }
    }

    override fun toString(): String {
        return "nm($namespace)"
    }
}
