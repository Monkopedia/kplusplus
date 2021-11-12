package com.monkopedia.krapper.generator.model.type

import com.monkopedia.krapper.generator.model.WrappedTypedef

class WrappedTypedefRef(val target: WrappedTypedef) : WrappedType() {
    override val cType: WrappedType
        get() = target.targetType.cType

    override fun toString(): String {
        return target.targetType.toString()
    }
}