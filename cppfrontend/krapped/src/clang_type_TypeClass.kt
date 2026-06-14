package clang.type

// BEGIN KRAPPER GEN for enum TypeClass
enum class TypeClass(val value: UInt) {
    Adjusted(0u), Decayed(1u), ConstantArray(2u), ArrayParameter(3u), DependentSizedArray(4u), IncompleteArray(5u), VariableArray(6u), Atomic(7u), Attributed(8u), BTFTagAttributed(9u), BitInt(10u), BlockPointer(11u), CountAttributed(12u), Builtin(13u), Complex(14u), Decltype(15u), Auto(16u), DeducedTemplateSpecialization(17u), DependentAddressSpace(18u), DependentBitInt(19u), DependentName(20u), DependentSizedExtVector(21u), DependentVector(22u), FunctionNoProto(23u), FunctionProto(24u), HLSLAttributedResource(25u), HLSLInlineSpirv(26u), MacroQualified(27u), ConstantMatrix(28u), DependentSizedMatrix(29u), MemberPointer(30u), ObjCObjectPointer(31u), ObjCObject(32u), ObjCInterface(33u), ObjCTypeParam(34u), PackExpansion(35u), PackIndexing(36u), Paren(37u), Pipe(38u), Pointer(39u), PredefinedSugar(40u), LValueReference(41u), RValueReference(42u), SubstBuiltinTemplatePack(43u), SubstTemplateTypeParmPack(44u), SubstTemplateTypeParm(45u), Enum(46u), InjectedClassName(47u), Record(48u), TemplateSpecialization(49u), TemplateTypeParm(50u), TypeOfExpr(51u), TypeOf(52u), Typedef(53u), UnaryTransform(54u), UnresolvedUsing(55u), Using(56u), Vector(57u), ExtVector(58u), TypeLast(58u);

    companion object {
        fun fromValue(v: UInt): TypeClass = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum TypeClass
