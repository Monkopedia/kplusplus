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

import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod

interface Namer {
    val cName: String
    val WrappedMethod.uniqueCName: String
    val WrappedField.uniqueCGetter: String
    val WrappedField.uniqueCSetter: String

    fun uniqify(source: Any, name: String): String
}

class NameHandler {
    object Empty : Namer {
        override val cName: String
            get() = throw NotImplementedError("Root naming unavailable")
        override val WrappedMethod.uniqueCName: String
            get() = throw NotImplementedError("Root naming unavailable")
        override val WrappedField.uniqueCGetter: String
            get() = throw NotImplementedError("Root naming unavailable")
        override val WrappedField.uniqueCSetter: String
            get() = throw NotImplementedError("Root naming unavailable")

        override fun uniqify(source: Any, name: String): String =
            throw NotImplementedError("Root naming unavailable")
    }

    private val allNames = mutableSetOf<String>()
    private val namerStorage = mutableMapOf<WrappedClass, NamerImpl>()

    inline fun withNamer(cls: WrappedClass, execNamed: Namer.() -> Unit) {
        cls.namer().apply(execNamed)
    }

    private fun uniqueNameFor(name: String): String {
        if (!allNames.add(name)) {
            return uniqueNameFor("_$name")
        }
        return name
    }

    fun WrappedClass.namer(): Namer {
        return namerStorage.getOrPut(this) { NamerImpl(this) }
    }

    private inner class NamerImpl(private val cls: WrappedClass) : Namer {
        private val nameLookup = mutableMapOf<Any, String>()
        override val cName = cls.type.toString()
            .replace("::", "_")
            .replace("<", "_")
            .replace(",", "__")
            .replace(">", "")
            .replace("*", "_P")
            .replace(" ", "_")
        override val WrappedMethod.uniqueCName: String
            get() = name(this) {
                uniqueNameFor(
                    when (methodType) {
                        MethodType.CONSTRUCTOR -> {
                            cName + "_new"
                        }
                        MethodType.DESTRUCTOR -> {
                            cName + "_dispose"
                        }
                        MethodType.SIZE_OF -> {
                            cName + "_size_of"
                        }
                        MethodType.STATIC,
                        MethodType.STATIC_OP,
                        MethodType.METHOD -> {
                            Operator.from(this)?.let {
                                it.name(this@NamerImpl, cls, this)
                            } ?: cName + "_" + name.splitCamelcase()
                                .joinToString("_") { it.toLowerCase() }.replace("=", "_eq")
                        }
                    }
                )
            }

        private inline fun name(obj: Any, generator: () -> String): String {
            return nameLookup.getOrPut(obj, generator)
        }

        override val WrappedField.uniqueCGetter: String
            get() = name(this) {
                "${cName}_${name}_get"
            }
        override val WrappedField.uniqueCSetter: String
            get() = name(this.other) {
                "${cName}_${name}_set"
            }

        override fun uniqify(source: Any, name: String): String = name(source) { name }
    }
}
