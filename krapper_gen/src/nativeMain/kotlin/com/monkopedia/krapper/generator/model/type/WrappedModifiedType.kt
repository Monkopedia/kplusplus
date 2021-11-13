package com.monkopedia.krapper.generator.model.type

class WrappedModifiedType(val baseType: WrappedType, val modifier: String) : WrappedType() {
    override val isReturnable: Boolean
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

    override val isNative: Boolean
        get() = baseType.isNative
    override val isString: Boolean
        get() = baseType.isString

    override val isVoid: Boolean
        get() = false

    override val pointed: WrappedType
        get() = if (modifier == "*") baseType else error("Cannot find pointed of non-pointer $this")
    override val isPointer: Boolean
        get() = ((this as? WrappedModifiedType)?.modifier == "*")

    override val isArray: Boolean
        get() = modifier == "[]"

    override val unreferenced: WrappedType
        get() = if (modifier == "&") baseType else error("Cannot unreference non-reference $this")

    override val isReference: Boolean
        get() = modifier == "&"
    override val isConst: Boolean
        get() = baseType.isConst
    override val unconst: WrappedType
        get() = baseType.unconst

    override fun toString(): String {
        return "${baseType}$modifier"
    }
}
class WrappedPrefixedType(val baseType: WrappedType, val modifier: String) : WrappedType() {
    override val isReturnable: Boolean
        get() = baseType.isReturnable
    override val cType: WrappedType
        get() = if (baseType.isString) baseType.cType else when (modifier) {
            "const" -> const(baseType.cType)
            else -> error("Don't know how to handle $modifier")
        }

    override val isNative: Boolean
        get() = baseType.isNative
    override val isString: Boolean
        get() = baseType.isString

    override val isVoid: Boolean
        get() = false

    override val pointed: WrappedType
        get() = if (modifier == "*") baseType else error("Cannot find pointed of non-pointer $this")
    override val isPointer: Boolean
        get() = baseType.isPointer

    override val isArray: Boolean
        get() = baseType.isArray

    override val unreferenced: WrappedType
        get() = const(baseType.unreferenced)

    override val isReference: Boolean
        get() = baseType.isReference
    override val isConst: Boolean
        get() = modifier == "const" || baseType.isConst
    override val unconst: WrappedType
        get() = if (isConst) baseType.unconst else WrappedPrefixedType(baseType.unconst, modifier)

    override fun toString(): String {
        return "$modifier $baseType"
    }
}