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
package com.monkopedia.krapper.generator.resolved_model

import com.monkopedia.krapper.FilterableTypes.CLASS
import com.monkopedia.krapper.TypeTarget
import com.monkopedia.krapper.generator.resolved_model.MethodType.SIZE_OF
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResolvedClassMetadata(
    val hasHiddenNew: Boolean = false,
    val hasHiddenDelete: Boolean = false,
    val hasConstructor: Boolean = false,
    val hasPrivateConstField: Boolean = false,
    val hasDefaultConstructor: Boolean = false,
    val hasCopyConstructor: Boolean = false,
)

@Serializable
@SerialName("class")
data class ResolvedClass(
    val name: String,
    val isAbstract: Boolean = false,
    val specifiedType: ResolvedType? = null,
    val metadata: ResolvedClassMetadata,
    val baseClass: ResolvedType?,
    val type: ResolvedCppType
) : ResolvedElement() {

    override fun toString(): String {
        return "cls($name)"
    }

    private var isNotEmptyCache: Boolean? = null

    override fun cloneWithoutChildren(): ResolvedClass {
        return copy(
            specifiedType = specifiedType?.cloneWithoutChildren(),
            type = type.copy(),
            baseClass = baseClass?.cloneWithoutChildren(),
            metadata = metadata.copy()
        )
    }

    override fun addAllChildren(list: List<ResolvedElement>) {
        isNotEmptyCache = null
        super.addAllChildren(list)
    }

    override fun addChild(child: ResolvedElement) {
        isNotEmptyCache = null
        super.addChild(child)
    }

    fun isNotEmpty(): Boolean {
        return isNotEmptyCache ?: calculateNotEmpty().also {
            isNotEmptyCache = it
        }
    }

    private fun calculateNotEmpty(): Boolean {
        return baseClass != null || children.any {
            ((it as? ResolvedMethod)?.methodType != SIZE_OF) &&
                ((it as? ResolvedConstructor)?.children?.isNotEmpty() != false)
        }
    }

    companion object : TypeTarget<ResolvedClass>(CLASS, ResolvedClass::class)
}
