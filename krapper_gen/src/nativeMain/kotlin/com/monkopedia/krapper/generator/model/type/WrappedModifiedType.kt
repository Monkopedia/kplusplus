package com.monkopedia.krapper.generator.model.type

class WrappedModifiedType(val baseType: WrappedType, val modifier: String) : WrappedType() {
    val isReturnable: Boolean
        get() = modifier == "*" || modifier == "&" || baseType.isReturnable
    override val cType: WrappedType
        get() = if (baseType.isString) baseType.cType else when (modifier) {
            "*",
            "&" -> pointerTo(
                if (baseType.isNative || (baseType == LONG_DOUBLE)) baseType.cType
                else VOID
            )
            "[]" -> arrayOf(baseType.cType)
            else -> error("Don't know how to handle $modifier")
        }

    override fun toString(): String {
        return "${baseType}$modifier"
    }
}