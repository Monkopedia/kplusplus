package clang.decl

// BEGIN KRAPPER GEN for enum ModuleOwnershipKind
enum class ModuleOwnershipKind(val value: UByte) {
    Unowned(0.toUByte()), Visible(1.toUByte()), VisibleWhenImported(2.toUByte()), VisiblePromoted(3.toUByte()), ReachableWhenImported(4.toUByte()), ModulePrivate(5.toUByte());

    companion object {
        fun fromValue(v: UByte): ModuleOwnershipKind = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum ModuleOwnershipKind
