package com.monkopedia.kplusplus

import kotlinx.cinterop.MemScope

// Transparent container facades. The kplusplus compiler plugin recognizes these
// by their callableId (package com.monkopedia.kplusplus + name) — it refines the
// return type to the generated binding and lowers the call to the binding's
// factory. The declared bodies never run. (These ultimately belong in a shared
// kplusplus runtime lib; hosted here for the generator-backed feature tests.)

fun <T> MemScope.cppVector(): T =
    throw NotImplementedError("lowered by the kplusplus compiler plugin")

fun <K, V> MemScope.cppMap(): Any =
    throw NotImplementedError("lowered by the kplusplus compiler plugin")

fun <A, B> MemScope.cppPair(): Any =
    throw NotImplementedError("lowered by the kplusplus compiler plugin")

fun <T> MemScope.cppUniquePtr(): T =
    throw NotImplementedError("lowered by the kplusplus compiler plugin")

fun <K, V> MemScope.cppUnorderedMap(): Any =
    throw NotImplementedError("lowered by the kplusplus compiler plugin")

fun <T> MemScope.cppUnorderedSet(): Any =
    throw NotImplementedError("lowered by the kplusplus compiler plugin")

fun <T> MemScope.cppSet(): Any =
    throw NotImplementedError("lowered by the kplusplus compiler plugin")
