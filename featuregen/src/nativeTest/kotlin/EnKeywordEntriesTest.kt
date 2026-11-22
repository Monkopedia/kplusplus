import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.KeywordEntry
import root.KeywordEntryFeature

// EN-keyword-entries (#319): C++ enumerators spelled like Kotlin keywords, or like members every
// Kotlin enum already has. Keywords are back-tick-escaped and keep their C++ spelling. The
// member clashes get a trailing `_` per collision: `name` becomes `name__` because `name_` is
// itself an enumerator. Without the escaping the generated enum doesn't compile, and the whole
// featuregen build fails with it.
class EnKeywordEntriesTest {
    @Test fun entries_keep_their_cpp_spelling_except_member_clashes() {
        val cpp = listOf(
            "as", "fun", "in", "interface", "is", "null", "object", "package", "super",
            "typealias", "val", "var", "when", "by", "constructor", "delegate", "dynamic",
            "field", "file", "finally", "get", "import", "init", "param", "property", "receiver",
            "set", "setparam", "where", "abstract", "actual", "annotation", "companion",
            "context", "crossinline", "data", "expect", "external", "final", "infix", "inner",
            "internal", "lateinit", "noinline", "open", "out", "override", "reified", "sealed",
            "suspend", "tailrec", "vararg", "it", "all", "contract"
        )
        val renamed = listOf("value_", "name__", "ordinal_", "entries_", "Companion_", "name_", "A")
        assertEquals(cpp + renamed, KeywordEntry.entries.map { it.name })
        assertEquals(KeywordEntry.entries.indices.toList(), KeywordEntry.entries.map { it.value })
    }

    @Test fun escaped_and_renamed_entries_round_trip_through_cpp() = memScoped {
        with(KeywordEntryFeature) {
            assertEquals(2, toInt(KeywordEntry.`in`))
            assertEquals(10, toInt(KeywordEntry.`val`))
            assertEquals(35, toInt(KeywordEntry.`data`))
            assertEquals(55, toInt(KeywordEntry.value_))
            assertEquals(56, toInt(KeywordEntry.name__))
            assertEquals(60, toInt(KeywordEntry.name_))
            assertEquals(KeywordEntry.entries_, fromInt(58))
            assertEquals(KeywordEntry.Companion_, fromInt(59))
            assertEquals(KeywordEntry.A, fromInt(61))
        }
    }
}
