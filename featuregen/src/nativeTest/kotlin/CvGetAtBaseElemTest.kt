import kotlinx.cinterop.memScoped
import root.Circle
import root.Rectangle
import root.ShapeBag
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

// CV-getat-base-elem (#7 item 3): ShapeBag is a `get`/`at`-keyed index container
// (size_t size + size_t index, no operator[]) whose ELEMENT type is the BASE class
// `Shape*`. WITHOUT the item-3 fix, the generator upcast the `at`/`get` return to
// `ShapeApi?` while the synthesized `iterator()`'s `next()` declared the CONCRETE
// element type — so the emitted `next(): Shape? = at(i): ShapeApi?` wouldn't typecheck
// and the featuregen build would fail to compile. The mere fact that THIS module
// compiles + these tests run proves the fix (the get/at index-get now keeps the
// concrete element type, matching operator[]). The asserts then round-trip the stored
// concrete Shapes and dispatch their virtual area() through the element.
class CvGetAtBaseElemTest {
    @Test fun at_and_get_return_concrete_elements() = memScoped {
        val ms = this
        val bag = with(ShapeBag) { ms.ShapeBag() }
        val circle = with(Circle) { ms.Circle__double(2.0) }            // area = π·4
        val rect = with(Rectangle) { ms.Rectangle__double_double(3.0, 4.0) } // area = 12
        bag.add(circle)
        bag.add(rect)

        assertEquals(2uL, bag.size())
        // at(i)/get(i) keep the CONCRETE element type; virtual area() dispatches to the
        // actual stored subtype.
        assertEquals(PI * 4.0, bag.at(0uL)?.area() ?: 0.0, 1e-9)
        assertEquals(12.0, bag.at(1uL)?.area() ?: 0.0, 1e-9)
        assertEquals(PI * 4.0, bag.get(0uL)?.area() ?: 0.0, 1e-9)
        assertEquals(12.0, bag.get(1uL)?.area() ?: 0.0, 1e-9)
    }

    // The synthesized Iterable<Shape> over the get/at index-get: `next()` lines up with
    // the concrete element type, so iteration + the stdlib Iterable surface work.
    @Test fun iterates_as_concrete_elements() = memScoped {
        val ms = this
        val bag = with(ShapeBag) { ms.ShapeBag() }
        bag.add(with(Circle) { ms.Circle__double(1.0) })   // area = π
        bag.add(with(Rectangle) { ms.Rectangle__double_double(2.0, 5.0) }) // area = 10
        bag.add(with(Circle) { ms.Circle__double(3.0) })   // area = π·9

        // The iterator yields the CONCRETE element type (`Shape?`), so area() dispatches
        // through it; the elements are non-null here.
        val areas = bag.map { it?.area() ?: 0.0 }
        assertEquals(3, areas.size)
        assertEquals(PI, areas[0], 1e-9)
        assertEquals(10.0, areas[1], 1e-9)
        assertEquals(PI * 9.0, areas[2], 1e-9)
    }
}
