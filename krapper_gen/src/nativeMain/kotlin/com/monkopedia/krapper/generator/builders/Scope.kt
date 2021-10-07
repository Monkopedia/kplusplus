/*
 * Copyright 2021 Jason Monk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.krapper.generator.builders

import com.monkopedia.krapper.generator.model.WrappedTypeReference
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class Scope<T : LangFactory>(private val parent: Scope<T>? = null) {
    private val names = mutableSetOf<String>()

    private fun isUsed(name: String): Boolean {
        return names.contains(name) || parent?.isUsed(name) == true
    }

    fun allocateName(desiredName: String): String {
        if (isUsed(desiredName)) {
            return allocateName("_$desiredName")
        }
        return desiredName
    }
}

val <T : LangFactory> CodeBuilder<T>.base: CodeBuilderBase<T>
    get() =
        (this as? CodeBuilderBase<T>)
            ?: (this as? BlockSymbol<T>)?.parent?.base
            ?: error("Dangling builder $this")

val <T : LangFactory> CodeBuilder<T>.scope: Scope<T>
    get() = base.currentScope

fun <T : LangFactory> CodeBuilder<T>.define(
    desiredName: String,
    type: WrappedTypeReference,
    initializer: Symbol? = null
): LocalVar {
    val name = scope.allocateName(desiredName)
    return factory.define(name, type, initializer)
}

@OptIn(ExperimentalContracts::class)
inline fun <T : LangFactory> CodeBuilder<T>.functionScope(inScope: CodeBuilder<T>.() -> Unit) {
    contract {
        callsInPlace(inScope, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    base.pushScope()
    inScope()
    base.popScope()
}
