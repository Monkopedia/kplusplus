package com.monkopedia.krapper.generator.model.type

import com.monkopedia.krapper.generator.model.WrappedElement

class WrappedTemplateRef(val target: WrappedElement) : WrappedType() {
    override val cType: WrappedType
        get() = error("Can't convert template $target")

    override fun toString(): String {
        return target.toString()
    }
}