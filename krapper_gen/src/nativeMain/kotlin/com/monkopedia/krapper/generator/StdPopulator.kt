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
package com.monkopedia.krapper.generator

import clang.CXType
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTypeReference
import com.monkopedia.krapper.generator.model.WrappedTypeReference.Companion.VOID
import com.monkopedia.krapper.generator.model.WrappedTypeReference.Companion.pointerTo
import com.monkopedia.krapper.generator.model.WrappedTypeReference.Companion.referenceTo
import kotlinx.cinterop.CValue

private typealias WrappedClassFactory =
    (ResolverBuilder, Array<WrappedTypeReference>) -> WrappedClass

object StdPopulator {

    private val wrappedClassGen: Map<String, WrappedClassFactory> =
        mapOf(
            "std::vector" to ::stdVector,
            "std::vector::iterator" to ::stdIterator,
            "std::vector::reverse_iterator" to ::stdReverseIterator,
        )

    fun maybePopulate(
        cls: WrappedClass,
        type: CValue<CXType>,
        resolverBuilder: ResolverBuilder
    ): WrappedClass {
        return wrappedClassGen[cls.fullyQualified]?.invoke(
            resolverBuilder,
            extractTypes(type, resolverBuilder)
        ) ?: cls
    }

    fun maybePopulate(cls: WrappedTypeReference, resolverBuilder: ResolverBuilder): WrappedClass? {
        return wrappedClassGen[baseName(cls.name)]?.invoke(
            resolverBuilder,
            extractTypes(cls.name, resolverBuilder)
        )
    }

    private fun baseName(name: String): String = name.replace(Regex("<.*>"), "")

    private fun extractTypes(
        type: String,
        resolverBuilder: ResolverBuilder
    ): Array<WrappedTypeReference> {
        if (!type.contains("<")) return emptyArray()
        val startIndex = type.indexOf('<')
        val endIndex = type.indexOf('>')
        return type.substring(startIndex + 1, endIndex).split(",").map {
            WrappedTypeReference(it)
        }.toTypedArray().also { println("Extracted $type as ${it.toList()}") }
    }

    private fun extractTypes(
        type: CValue<CXType>,
        resolverBuilder: ResolverBuilder
    ): Array<WrappedTypeReference> {
        return Array(type.numTemplateArguments.coerceAtLeast(0)) {
            WrappedTypeReference(type.getTemplateArgumentKind(it.toUInt()), resolverBuilder)
        }.also { println("Extracted ${type.spelling.toKString()} as ${it.toList()}") }
    }

    fun stdIterator(
        resolverBuilder: ResolverBuilder,
        vararg types: WrappedTypeReference
    ): WrappedClass {
        require(types.size == 1) {
            "Wrong number of type arguments for std::vector::iterator, not sure how to handle"
        }
        val type = types[0]
        val fullyQualified = "std::vector<${type.name}>::iterator"

        return WrappedClass(
            fullyQualified,
            null,
            listOf(),
            listOf(
                WrappedMethod(
                    "iterator",
                    WrappedTypeReference(fullyQualified),
                    emptyList(),
                    false,
                    MethodType.CONSTRUCTOR
                ),
                WrappedMethod(
                    "operator++",
                    WrappedTypeReference(fullyQualified),
                    emptyList(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "operator++",
                    WrappedTypeReference(fullyQualified),
                    listOf(WrappedArgument("dummy", WrappedTypeReference("int"))),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "operator==",
                    WrappedTypeReference("bool"),
                    listOf(WrappedArgument("other", WrappedTypeReference("$fullyQualified &"))),
                    false,
                    MethodType.STATIC_OP
                ),
                WrappedMethod(
                    "operator!=",
                    WrappedTypeReference("bool"),
                    listOf(WrappedArgument("other", WrappedTypeReference("$fullyQualified &"))),
                    false,
                    MethodType.STATIC_OP
                ),
                WrappedMethod(
                    "operator*",
                    referenceTo(type),
                    listOf(),
                    false,
                    MethodType.METHOD
                )
            )
        ).also { println("Declaring $fullyQualified") }
    }

    fun stdReverseIterator(
        resolverBuilder: ResolverBuilder,
        vararg types: WrappedTypeReference
    ): WrappedClass {
        require(types.size == 1) {
            "Wrong number of type arguments for std::vector::reverse_iterator, " +
                "not sure how to handle"
        }
        val type = types[0]
        val fullyQualified = "std::vector<${type.name}>::reverse_iterator"

        return WrappedClass(
            fullyQualified,
            null,
            listOf(),
            listOf(
                WrappedMethod(
                    "reverse_iterator",
                    WrappedTypeReference(fullyQualified),
                    emptyList(),
                    false,
                    MethodType.CONSTRUCTOR
                ),
                WrappedMethod(
                    "operator++",
                    WrappedTypeReference(fullyQualified),
                    emptyList(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "operator++",
                    WrappedTypeReference(fullyQualified),
                    listOf(WrappedArgument("dummy", WrappedTypeReference("int"))),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "operator==",
                    WrappedTypeReference("bool"),
                    listOf(WrappedArgument("other", WrappedTypeReference("$fullyQualified &"))),
                    false,
                    MethodType.STATIC_OP
                ),
                WrappedMethod(
                    "operator!=",
                    WrappedTypeReference("bool"),
                    listOf(WrappedArgument("other", WrappedTypeReference("$fullyQualified &"))),
                    false,
                    MethodType.STATIC_OP
                ),
                WrappedMethod(
                    "operator*",
                    referenceTo(type),
                    listOf(),
                    false,
                    MethodType.METHOD
                )
            )
        ).also { println("Declaring $fullyQualified") }
    }

    fun stdVector(
        resolverBuilder: ResolverBuilder,
        vararg types: WrappedTypeReference
    ): WrappedClass {
        require(types.size == 1) {
            "Wrong number of type arguments for std::vector, not sure how to handle"
        }
        val type = types[0]
        val fullyQualified = "std::vector<${type.name}>"
        val iterator = resolverBuilder.type("std::vector<$type>::iterator")
        val reverseIterator = resolverBuilder.type("std::vector<$type>::reverse_iterator")
        return WrappedClass(
            fullyQualified,
            null,
            listOf(),
            listOf(
                WrappedMethod(
                    fullyQualified,
                    VOID,
                    emptyList(),
                    false,
                    MethodType.CONSTRUCTOR
                ),
                WrappedMethod(
                    fullyQualified,
                    VOID,
                    listOf(WrappedArgument("other", WrappedTypeReference("$fullyQualified &"))),
                    false,
                    MethodType.CONSTRUCTOR
                ),
                WrappedMethod(
                    fullyQualified,
                    VOID,
                    listOf(),
                    false,
                    MethodType.DESTRUCTOR
                ),
                WrappedMethod(
                    "at",
                    referenceTo(type),
                    listOf(WrappedArgument("pos", resolverBuilder.type("std::size_t"))),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "operator[]",
                    referenceTo(type),
                    listOf(WrappedArgument("pos", resolverBuilder.type("std::size_t"))),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "front",
                    referenceTo(type),
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "back",
                    referenceTo(type),
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "data",
                    pointerTo(type),
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "begin",
                    iterator,
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "end",
                    iterator,
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "rbegin",
                    reverseIterator,
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "rend",
                    reverseIterator,
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "empty",
                    resolverBuilder.type("bool"),
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "size",
                    resolverBuilder.type("std::size_t"),
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "max_size",
                    resolverBuilder.type("std::size_t"),
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "reserve",
                    VOID,
                    listOf(WrappedArgument("new_cap", resolverBuilder.type("std::size_t"))),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "capacity",
                    resolverBuilder.type("std::size_t"),
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "shrink_to_fit",
                    VOID,
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "clear",
                    VOID,
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "insert",
                    iterator,
                    listOf(
                        WrappedArgument("pos", iterator),
                        WrappedArgument("value", referenceTo(type))
                    ),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "push_back",
                    VOID,
                    listOf(WrappedArgument("value", referenceTo(type))),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "pop_back",
                    VOID,
                    listOf(),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "resize",
                    VOID,
                    listOf(WrappedArgument("count", resolverBuilder.type("std::size_t"))),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "swap",
                    VOID,
                    listOf(
                        WrappedArgument(
                            "other",
                            referenceTo(WrappedTypeReference(fullyQualified))
                        )
                    ),
                    false,
                    MethodType.METHOD
                ),
                WrappedMethod(
                    "operator==",
                    WrappedTypeReference("bool"),
                    listOf(
                        WrappedArgument(
                            "other",
                            referenceTo(WrappedTypeReference(fullyQualified))
                        )
                    ),
                    false,
                    MethodType.STATIC_OP
                ),
                WrappedMethod(
                    "operator!=",
                    WrappedTypeReference("bool"),
                    listOf(
                        WrappedArgument(
                            "other",
                            referenceTo(WrappedTypeReference(fullyQualified))
                        )
                    ),
                    false,
                    MethodType.STATIC_OP
                ),
                WrappedMethod(
                    "operator<",
                    WrappedTypeReference("bool"),
                    listOf(
                        WrappedArgument(
                            "other",
                            referenceTo(WrappedTypeReference(fullyQualified))
                        )
                    ),
                    false,
                    MethodType.STATIC_OP
                ),
                WrappedMethod(
                    "operator<=",
                    WrappedTypeReference("bool"),
                    listOf(
                        WrappedArgument(
                            "other",
                            referenceTo(WrappedTypeReference(fullyQualified))
                        )
                    ),
                    false,
                    MethodType.STATIC_OP
                ),
                WrappedMethod(
                    "operator>",
                    WrappedTypeReference("bool"),
                    listOf(
                        WrappedArgument(
                            "other",
                            referenceTo(WrappedTypeReference(fullyQualified))
                        )
                    ),
                    false,
                    MethodType.STATIC_OP
                ),
                WrappedMethod(
                    "operator>=",
                    WrappedTypeReference("bool"),
                    listOf(
                        WrappedArgument(
                            "other",
                            referenceTo(WrappedTypeReference(fullyQualified))
                        )
                    ),
                    false,
                    MethodType.STATIC_OP
                )
            )
        ).also { println("Declaring $fullyQualified") }
    }

    private fun ResolverBuilder.type(s: String): WrappedTypeReference {
        return WrappedTypeReference(s).also { visit(it) }
    }
}
