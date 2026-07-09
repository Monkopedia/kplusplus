package krapper

annotation class CppBinding(val spec: String)

// Marks a generated scoped template factory (`fun <T> MemScope.Box(): Box<T>`)
// so the kplusplus compiler plugin can derive its container mapping (C++ base,
// binding-name prefix, package) and refine `Box<Int>()` to the concrete
// `Box__Int` binding — the generic generalization of the hardcoded cppVector
// facade recognition. (Arity comes from the factory's own type params.)
annotation class CppTemplate(
    val base: String,
    val prefix: String,
    val pkg: String
)
