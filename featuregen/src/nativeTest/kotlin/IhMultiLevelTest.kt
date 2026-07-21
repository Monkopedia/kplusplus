import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.Animal
import root.AnimalApi
import root.AnimalProbe
import root.Dog
import root.Puppy

// IH-flatten (3-level chain): Animal <- Dog <- Puppy. The inherited virtual
// surface flattens onto each binding with the intermediate-override de-dup intact:
//  - speak() is declared on Animal, overridden by Dog, then overridden again by
//    Puppy. On the Puppy binding speak() must appear EXACTLY ONCE and resolve to
//    Puppy's own override (returns 2) — not Dog's (1) or Animal's (0), and not
//    double-emitted once per ancestor.
//  - legs() is declared on Animal and never overridden; it flattens onto Dog and
//    Puppy exactly once each, calling Animal's impl (4).
//  - Through an `Animal*` (generated `AnimalApi?`), speak() vtable-dispatches to the
//    most-derived override of whatever was actually passed.
class IhMultiLevelTest {
    // speak() resolves to each class's own most-derived override; legs() is the
    // single inherited Animal impl everywhere.
    @Test fun three_level_override_resolves_per_class() = memScoped {
        val animal = with(Animal) { Animal() }
        val dog = with(Dog) { Dog() }
        val puppy = with(Puppy) { Puppy() }
        assertEquals(0, animal.speak()) // Animal's base
        assertEquals(1, dog.speak()) // Dog's intermediate override
        assertEquals(2, puppy.speak()) // Puppy's leaf override (over Dog's)
        // legs() never overridden -> Animal's impl on every level.
        assertEquals(4, animal.legs())
        assertEquals(4, dog.legs())
        assertEquals(4, puppy.legs())
    }

    // IH-virtual-dispatch through the base: a Puppy passed as an Animal* dispatches
    // speak() to Puppy's override (2), and a Dog to Dog's (1).
    @Test fun dispatch_through_animal_base_pointer() = memScoped {
        val dog = with(Dog) { Dog() }
        val puppy = with(Puppy) { Puppy() }
        assertEquals(1, with(AnimalProbe) { speakOf(dog) })
        assertEquals(2, with(AnimalProbe) { speakOf(puppy) })
        // legs() dispatches to the single inherited Animal impl regardless of subtype.
        assertEquals(4, with(AnimalProbe) { legsOf(puppy) })
    }

    // A Puppy held in a polymorphic `AnimalApi?` still dispatches to Puppy's override.
    @Test fun puppy_as_animal_api_dispatches() = memScoped {
        val puppy = with(Puppy) { Puppy() }
        val asBase: AnimalApi = puppy
        assertEquals(2, asBase.speak())
        assertEquals(4, asBase.legs())
    }
}
