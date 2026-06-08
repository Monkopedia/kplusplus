import com.monkopedia.kplusplus.cppMap
import com.monkopedia.kplusplus.cppVector
import kotlinx.cinterop.memScoped
import com.monkopedia.slice.std.Vector__Int

// Spike: transparent generic syntax across two element types. The compiler plugin
// refines each cppVector<T>() to the matching generated binding (std.Vector__Int /
// std.Vector__Double) and lowers the call to construct it — no annotation, no
// explicit binding import.
fun main(args: Array<String>) {
    memScoped {
        val ints = cppVector<Int>()
        ints.push_back(7)
        ints.push_back(8)

        val doubles = cppVector<Double>()
        doubles.push_back(1.5)

        // New type — never hardcoded anywhere — proves the auto-detect→generate link.
        val floats = cppVector<Float>()
        floats.push_back(3.14f)
        floats.push_back(2.71f)
        floats.push_back(1.41f)

        // Second container kind — proves the generalization off the per-container hardcoding.
        val intMap = cppMap<Int, Int>()

        // M11: user-defined element type. std.Vector__Int is itself a generated binding
        // carrying @krapper.CppBinding("std::vector<int>"); the plugin reads that annotation
        // to derive the outer C++ spec std::vector<std::vector<int>> with no hardcoding.
        // Two-cycle sync expected: first cycle generates the inner instantiation so its
        // class+annotation reach the classpath, second cycle generates the outer one.
        val nested = cppVector<Vector__Int>()

        check(ints.size() == 2uL) { "expected int vector size 2, got ${ints.size()}" }
        check(doubles.size() == 1uL) { "expected double vector size 1, got ${doubles.size()}" }
        check(floats.size() == 3uL) { "expected float vector size 3, got ${floats.size()}" }
        check(intMap.empty()) { "expected std::map<int,int> empty initially" }
        check(nested.empty()) { "expected std::vector<std::vector<int>> empty initially" }
        println(
            "transparent kplusplus round-trip ok: " +
                "ints=${ints.size()} doubles=${doubles.size()} floats=${floats.size()} " +
                "mapEmpty=${intMap.empty()} nestedEmpty=${nested.empty()}"
        )
    }
}
