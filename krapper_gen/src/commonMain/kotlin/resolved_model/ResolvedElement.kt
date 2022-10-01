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

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule

@Serializable
abstract class ResolvedElement(
    private val mutableChildren: MutableList<ResolvedElement> = mutableListOf()
) {
    val children: List<ResolvedElement>
        get() = mutableChildren

    @Transient
    var parent: ResolvedElement? = null

    fun clearChildren() {
        mutableChildren.clear()
    }

    fun clone(): ResolvedElement {
        return cloneWithoutChildren().also { clone ->
            clone.addAllChildren(children.map { it.clone() })
        }
    }

    abstract fun cloneWithoutChildren(): ResolvedElement

    internal open fun addAllChildren(list: List<ResolvedElement>) {
        list.forEach {
            require(!children.contains(it)) {
                "$this already contains $it"
            }
        }
        mutableChildren.addAll(list)
        list.forEach {
            it.parent = this
        }
    }

    internal open fun addChild(child: ResolvedElement) {
        require(!children.contains(child)) {
            "$this already contain a $child"
        }
        mutableChildren.add(child)
        child.parent = this
    }

    internal fun removeChild(child: ResolvedElement) {
        mutableChildren.remove(child)
    }

    internal fun setParents() {
        for (child in mutableChildren) {
            child.parent = this
            child.setParents()
        }
    }
}

fun Collection<ResolvedElement>.recursiveSequence() = sequence {
    for (element in this@recursiveSequence) {
        this.emitRecursive(element)
    }
}

private suspend fun SequenceScope<ResolvedElement>.emitRecursive(element: ResolvedElement) {
    yield(element)
    for (child in element.children) {
        emitRecursive(child)
    }
}
