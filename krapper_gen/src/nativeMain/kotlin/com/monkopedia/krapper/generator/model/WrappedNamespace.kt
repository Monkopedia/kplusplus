package com.monkopedia.krapper.generator.model

class WrappedNamespace(val namespace: String) : WrappedElement() {
    override fun clone(): WrappedElement {
        return WrappedNamespace(namespace).also {
            it.children.addAll(children)
            it.parent = parent
        }
    }

    override fun toString(): String {
        return "nm($namespace)"
    }
}
