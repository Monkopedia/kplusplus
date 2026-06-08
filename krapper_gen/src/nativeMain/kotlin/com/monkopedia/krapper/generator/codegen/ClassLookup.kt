/*
 * Copyright 2022 Jason Monk
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
package com.monkopedia.krapper.generator.codegen

import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedType

class ClassLookup(private val classes: List<ResolvedClass>) {
    private val quickLook = mutableMapOf<String, ResolvedClass?>()

    // Every bound class, for whole-program passes like the down-cast inverse map
    // (which must scan all derived classes to find those reaching a given base).
    val allClasses: List<ResolvedClass> get() = classes

    operator fun get(type: ResolvedType): ResolvedClass? = this[type.toString()]

    operator fun get(str: String): ResolvedClass? = quickLook.getOrPut(str) {
        classes.find { it.type.toString() == str } ?: null.also {
            println("Warning: Couldn't find class $str")
        }
    }
}
