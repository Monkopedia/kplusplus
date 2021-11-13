package com.monkopedia.krapper.generator.model.type

class WrappedTemplateRef(val target: String) : WrappedType() {
    override val cType: WrappedType
        get() = error("Can't convert template $target")

    override val isReturnable: Boolean
        get() = false
    override val isNative: Boolean
        get() = false
    override val isString: Boolean
        get() = false

    override val isVoid: Boolean
        get() = false

    override val pointed: WrappedType
        get() = error("Cannot get pointee of non-pointer template $this")
    override val isPointer: Boolean
        get() = false

    override val isArray: Boolean
        get() = false

    override val unreferenced: WrappedType
        get() = error("Cannot get unreference of non-reference template $this")

    override val isReference: Boolean
        get() = false
    override val isConst: Boolean
        get() = false
    override val unconst: WrappedTypeReference
        get() = error("Cannot unconst non-const template $this")

    override fun toString(): String {
        return "template<$target>"
    }
}