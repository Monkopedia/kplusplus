package com.monkopedia.krapper.generator.model.type

class WrappedTemplateType(
    val baseType: WrappedType,
    val templateArgs: List<WrappedType>
) : WrappedType() {
    override val cType: WrappedType
        get() {
            baseType.cType
            templateArgs.forEach { it.cType }
            return pointerTo(VOID)
        }

    override val isReturnable: Boolean
        get() = false
    override val isNative: Boolean
        get() = false
    override val isString: Boolean
        get() = false

    override val isVoid: Boolean
        get() = false

    override val pointed: WrappedType
        get() = error("Cannot get pointee of non-pointer templated $this")
    override val isPointer: Boolean
        get() = false

    override val isArray: Boolean
        get() = false

    override val unreferenced: WrappedType
        get() = error("Cannot get unreference of non-reference templated $this")

    override val isReference: Boolean
        get() = false
    override val isConst: Boolean
        get() = false
    override val unconst: WrappedTypeReference
        get() = error("Cannot unconst non-const templated $this")

    override fun toString(): String {
        return "$baseType<${templateArgs.joinToString(", ")}>"
    }
}
