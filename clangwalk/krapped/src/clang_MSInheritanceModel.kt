package clang

// BEGIN KRAPPER GEN for enum MSInheritanceModel
enum class MSInheritanceModel(val value: Int) {
    Single(0), Multiple(1), Virtual(2), Unspecified(3);

    companion object {
        fun fromValue(v: Int): MSInheritanceModel = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum MSInheritanceModel
