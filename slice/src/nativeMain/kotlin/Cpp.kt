package com.monkopedia.kplusplus

import kotlinx.cinterop.MemScope

/**
 * Transparent template-instantiation entry point. The kplusplus compiler plugin
 * refines `cppVector<Int>()`'s return type to the generated binding (e.g.
 * std.Vector__Int) so member calls type-check, and lowers the call to construct
 * the real binding using the MemScope receiver. The declared body is never run.
 */
fun <T> MemScope.cppVector(): T =
    throw NotImplementedError("lowered by the kplusplus compiler plugin")

/**
 * Transparent template-instantiation entry point for std::map<K, V>. Same
 * lifecycle as cppVector: the compiler plugin refines the call's return type
 * to the generated binding (e.g. std.Map__Int__Int) and lowers the call to
 * its companion factory. Return type is Any because the two type params don't
 * line up with a natural Kotlin-side return; the refinement replaces it.
 */
fun <K, V> MemScope.cppMap(): Any =
    throw NotImplementedError("lowered by the kplusplus compiler plugin")
