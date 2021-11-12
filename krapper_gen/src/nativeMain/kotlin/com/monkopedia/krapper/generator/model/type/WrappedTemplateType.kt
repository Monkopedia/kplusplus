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

    override fun toString(): String {
        return "$baseType<${templateArgs.joinToString(", ")}>"
    }
}
