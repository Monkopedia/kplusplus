package com.monkopedia.krapper.generator.model.type

class WrappedTypedefRef(val usr: String) : WrappedType() {
    override val cType: WrappedType
        get() = error("Cannot convert typedef($usr) to cType")

    override fun toString(): String {
        return "unresolved_typedef($usr)"
    }
}