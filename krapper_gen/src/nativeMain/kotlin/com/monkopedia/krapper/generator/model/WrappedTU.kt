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
import com.monkopedia.krapper.generator.resolved_model.ResolvedTU

class WrappedTU : WrappedElement() {
    operator fun plus(other: WrappedTU): WrappedTU {
        return WrappedTU().also {
            it.addAllChildren(children)
            it.addAllChildren(other.children)
            it.children.forEach { c ->
                c.parent = it
            }
        }
    }

    override fun clone(): WrappedElement {
        return WrappedTU().also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    override suspend fun resolve(resolverContext: ResolveContext): ResolvedTU {
        return ResolvedTU().also {
            it.addAllChildren(children.mapNotNull { it.resolve(resolverContext) })
        }
    }
}
