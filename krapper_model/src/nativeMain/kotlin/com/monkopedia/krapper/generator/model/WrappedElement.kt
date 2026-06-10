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

abstract class WrappedElement(
    private val mutableChildren: MutableList<WrappedElement> = mutableListOf()
) {
    val children: List<WrappedElement>
        get() = mutableChildren
    var parent: WrappedElement? = null

    fun clearChildren() {
        mutableChildren.clear()
    }

    open fun addAllChildren(list: List<WrappedElement>) {
        list.forEach {
            require(!children.contains(it)) {
                "$this already contains $it"
            }
        }
        mutableChildren.addAll(list)
        list.forEach { it.parent = this }
    }

    open fun addChild(child: WrappedElement) {
        require(!children.contains(child)) {
            "$this already contain a $child"
        }
        mutableChildren.add(child)
        child.parent = this
    }

    fun removeChild(child: WrappedElement) {
        mutableChildren.remove(child)
    }

    abstract fun clone(): WrappedElement

    // Cursor-side construction (mapAll/map and friends) lives in the libclang front-end
    // (ModelFactories.kt) as extensions on this companion, and resolution against the
    // type graph lives in the resolver (ModelResolution.kt) as a when-dispatch over the
    // element hierarchy — the model itself carries no clang/cinterop or codegen
    // dependency (the front-end seam, issue #44).
    companion object
}

fun WrappedElement.forEachRecursive(onEach: (WrappedElement) -> Unit) {
    for (child in children.toList()) {
        onEach(child)
        child.forEachRecursive(onEach)
    }
}

fun WrappedElement.filterRecursive(
    ret: MutableList<WrappedElement> = mutableListOf(),
    onEach: (WrappedElement) -> Boolean
): List<WrappedElement> {
    for (child in children.toList()) {
        if (onEach(child)) {
            ret.add(child)
        }
        child.filterRecursive(ret, onEach)
    }
    return ret
}

fun <T : WrappedElement> T.cloneRecursive(): T = clone().also {
    val newChildren = it.children.map { it.cloneRecursive() }
    it.clearChildren()
    it.addAllChildren(newChildren)
} as T
