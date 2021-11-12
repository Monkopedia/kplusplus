package com.monkopedia.krapper.generator.model.type

class WrappedTemplateRef(val target: String) : WrappedType() {
    override val cType: WrappedType
        get() = error("Can't convert template $target")

    override fun toString(): String {
        return "template<$target>"
    }
}