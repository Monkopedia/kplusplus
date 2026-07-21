import com.monkopedia.kplusplus.cppUniquePtr
import kotlin.test.Test
import kotlin.test.assertNull
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import std.Unique_ptr__Int

// SP-unique: std::unique_ptr<int> via the transparent cppUniquePtr<T>() facade.
//
// The point of this row is the pointer-typedef fix: get()/release()/operator->()
// return the `pointer` member alias (= int*), which previously resolved to an
// unsubstituted template ref and got dropped. With the fix they generate as
// `CValuesRef<IntVar>?` and are callable.
//
// The facade lowers to the `_Holder` factory, which allocs raw (unconstructed)
// storage — there is no wrapped default ctor for unique_ptr yet. A unique_ptr<int>
// is a single pointer member, so a zeroed block IS a valid null unique_ptr; we
// zero it explicitly before exercising the accessors so the reads are defined.
class SpUniquePtrTest {
    // Make the raw Holder storage a valid null unique_ptr (its single pointer
    // member set to null) before calling any accessor.
    private fun Unique_ptr__Int.makeNull() = also { it.ptr.reinterpret<LongVar>()[0] = 0L }

    @Test fun default_null_get() = memScoped {
        val up = cppUniquePtr<Int>().makeNull()
        // get() on a null unique_ptr returns nullptr.
        assertNull(up.get(), "get() on a null unique_ptr should be null")
    }

    @Test fun release_of_null_is_null() = memScoped {
        val up = cppUniquePtr<Int>().makeNull()
        // release() on a null unique_ptr returns nullptr and leaves it null.
        assertNull(up.release(), "release() on a null unique_ptr should be null")
        assertNull(up.get(), "get() after release() should still be null")
    }

    @Test fun operator_arrow_generates_and_is_callable() = memScoped {
        // operator->() (Kotlin name `pointer_reference`) is proven to generate by
        // resolving and being callable here. We don't dereference a null unique_ptr
        // (that is UB), but on a null unique_ptr operator->() returns the same null
        // pointer as get(), which is well-defined to read as a value.
        val up = cppUniquePtr<Int>().makeNull()
        assertNull(up.pointer_reference(), "operator->() on a null unique_ptr is null")
    }
}
