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
package com.monkopedia.krapper.generator.resolved_model

import com.monkopedia.krapper.FilterableTypes
import com.monkopedia.krapper.TypeTarget
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("namespace")
data class ResolvedNamespace(val namespace: String) : ResolvedElement() {
    override fun toString(): String {
        return "nm($namespace)"
    }

    override fun cloneWithoutChildren(): ResolvedNamespace {
        return copy()
    }

    companion object :
        TypeTarget<ResolvedNamespace>(FilterableTypes.NAMESPACE, ResolvedNamespace::class)
}
