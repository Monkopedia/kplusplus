package com.monkopedia.krapper.generator.model

class WrappedTU : WrappedElement() {
    operator fun plus(other: WrappedTU): WrappedTU {
        return WrappedTU().also {
            it.children.addAll(children)
            it.children.addAll(other.children)
            it.children.forEach { c ->
                c.parent = it
            }
        }
    }

    override fun clone(): WrappedElement {
        return WrappedTU().also {
            it.children.addAll(children)
            it.parent = parent
        }
    }
}
