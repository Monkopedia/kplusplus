import kotlinx.cinterop.memScoped
import root.RangeHolder
import root.Thing
import kotlin.test.Test
import kotlin.test.assertEquals

// RG-range (T1.3, generator-backed): a method returning `llvm::iterator_range<It>` is
// materialized into a bound `std::vector<Thing*>` and surfaces on the Kotlin side as a
// `for`-iterable `Iterable<Thing>`. RangeHolder::items() returns the range; the generator
// rewrote it to `std::vector<Thing*>` + a begin()/end() loop (`kpp_to_elem_ptr<Thing>`).
// This is the generic reproduction of Clang's decls()/methods()/fields()/bases().
//
// #18 (resolveForcing characterization) — PASS-3 RANGE-METHOD RECOVERY at the integration
// level: `items()`'s return type is the std::vector<Thing*> container, which is NOT bound
// when `RangeHolder` is first resolved (pass 1 drops `items()` with "Couldn't resolve
// return"). It is RECOVERED only because `resolveForcing` pass 3 re-resolves the bound
// classes against the now-forced container (evict-then-rebuild; see Resolver.kt ~L282).
// So this test passing is the integration-level proof that pass-3 recovery works: if
// pass-3 recovery regressed (e.g. the fresh `mappingCache`/`resolvedClasses` eviction were
// dropped), `holder.items()` would not exist and this whole class would fail to compile.
class RgRangeTest {
    @Test fun items_is_for_iterable() = memScoped {
        val ms = this
        val holder = with(RangeHolder) { ms.RangeHolder() }
        val t1 = with(Thing) { ms.Thing__int(10) }
        val t2 = with(Thing) { ms.Thing__int(20) }
        val t3 = with(Thing) { ms.Thing__int(30) }
        holder.add(t1)
        holder.add(t2)
        holder.add(t3)
        assertEquals(3, holder.count())

        // The materialized range is a `for`-iterable: iterate it and collect the ids.
        val ids = mutableListOf<Int>()
        for (t in holder.items()) {
            ids.add(t?.getId() ?: -1)
        }
        assertEquals(listOf(10, 20, 30), ids)
    }

    @Test fun items_indexable_and_sized() = memScoped {
        val ms = this
        val holder = with(RangeHolder) { ms.RangeHolder() }
        holder.add(with(Thing) { ms.Thing__int(7) })
        holder.add(with(Thing) { ms.Thing__int(8) })

        val v = holder.items()
        assertEquals(2uL, v.size())
        assertEquals(7, v[0uL]?.getId())
        assertEquals(8, v[1uL]?.getId())
    }

    @Test fun empty_range_materializes_empty() = memScoped {
        val ms = this
        val holder = with(RangeHolder) { ms.RangeHolder() }
        assertEquals(0, holder.count())
        assertEquals(0uL, holder.items().size())
        var iterations = 0
        for (@Suppress("UNUSED_VARIABLE") t in holder.items()) iterations++
        assertEquals(0, iterations)
    }
}
