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
package com.monkopedia.krapper.generator.codegen

import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.type.WrappedModifiedType
import com.monkopedia.krapper.generator.model.type.WrappedPrefixedType
import com.monkopedia.krapper.generator.model.type.WrappedType

class ClassLookup(private val classes: List<WrappedClass>) {
    private val quickLook = mutableMapOf<String, WrappedClass?>()

    operator fun get(type: WrappedType): WrappedClass? {
        if (type is WrappedModifiedType) return this[type.baseType]
        if (type is WrappedPrefixedType) return this[type.baseType]
        return this[type.toString()]
    }

    operator fun get(str: String): WrappedClass? {
        return quickLook.getOrPut(str) {
            classes.find { it.type.toString() == str } ?: null.also {
                println("Warning: Couldn't find class $str")
            }
        }
    }
}
