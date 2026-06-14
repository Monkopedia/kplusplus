package clang.unaryTransformType

// BEGIN KRAPPER GEN for enum UTTKind
enum class UTTKind(val value: UInt) {
    AddLvalueReference(0u), AddPointer(1u), AddRvalueReference(2u), Decay(3u), MakeSigned(4u), MakeUnsigned(5u), RemoveAllExtents(6u), RemoveConst(7u), RemoveCV(8u), RemoveCVRef(9u), RemoveExtent(10u), RemovePointer(11u), RemoveReference(12u), RemoveRestrict(13u), RemoveVolatile(14u), EnumUnderlyingType(15u);

    companion object {
        fun fromValue(v: UInt): UTTKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum UTTKind
