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
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedTemplateType
import com.monkopedia.krapper.generator.model.WrappedTypeReference
import kotlinx.cinterop.CValue

private typealias WrappedClassFactory =
    (ResolverBuilder, Array<WrappedTypeReference>) -> WrappedClass

object StdPopulator {
    val template = WrappedTemplateType("T")

    private val wrappedClassGen: Map<String, WrappedClassFactory> =
        mapOf(
//            "std::vector" to ::stdVector,
//            "std::vector::iterator" to ::stdIterator,
//            "std::vector::reverse_iterator" to ::stdReverseIterator,
        )

    fun maybePopulate(
        cls: WrappedClass,
        type: CValue<CXType>,
        resolverBuilder: ResolverBuilder
    ): WrappedClass {
        val fullyQualified = cls.type.toString()
        return maybeCreate(fullyQualified, type, resolverBuilder) ?: cls
    }

    fun maybeCreate(
        fullyQualified: String,
        type: CValue<CXType>,
        resolverBuilder: ResolverBuilder
    ): WrappedClass? = wrappedClassGen[fullyQualified]?.invoke(
        resolverBuilder,
        extractTypes(type, resolverBuilder)
    )

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

//    val vectorIterator by lazy {
//        val templateList = listOf(template)
//        val selfReference = WrappedTemplatedReference("std::vector<T>::iterator", templateList)
//        WrappedTemplate(
//            selfReference.fullyQualifiedTemplate,
//            null,
//            listOf(),
//            listOf(
//                WrappedTemplateMethod(
//                    "iterator",
//                    selfReference,
//                    emptyList(),
//                    false,
//                    CONSTRUCTOR
//                ),
//                WrappedTemplateMethod(
//                    "operator++",
//                    selfReference,
//                    emptyList(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "operator++",
//                    selfReference,
//                    listOf(WrappedTemplateArgument("dummy", WrappedTypeReference("int"))),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "operator==",
//                    WrappedTypeReference("bool"),
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    STATIC_OP
//                ),
//                WrappedTemplateMethod(
//                    "operator!=",
//                    WrappedTypeReference("bool"),
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    STATIC_OP
//                ),
//                WrappedTemplateMethod(
//                    "operator*",
//                    WrappedType.referenceTo(template),
//                    listOf(),
//                    false,
//                    METHOD
//                )
//            ),
//            emptyList(),
//            templateList
//        )
//    }
//
//    fun stdIterator(
//        resolverBuilder: ResolverBuilder,
//        vararg types: WrappedTypeReference
//    ): WrappedClass {
//        require(types.size == 1) {
//            "Wrong number of type arguments for std::vector::iterator, not sure how to handle"
//        }
//        val type = types[0]
//
//        return vectorIterator.typedWith(resolverBuilder, template to type)
//            .also { println("Declaring ${it.fullyQualified}") }
//    }
//
//    val vectorReverseIterator by lazy {
//        val templateList = listOf(template)
//        val selfReference =
//            WrappedTemplatedReference("std::vector<T>::reverse_iterator", templateList)
//        WrappedTemplate(
//            selfReference.fullyQualifiedTemplate,
//            null,
//            listOf(),
//            listOf(
//                WrappedTemplateMethod(
//                    "reverse_iterator",
//                    selfReference,
//                    emptyList(),
//                    false,
//                    CONSTRUCTOR
//                ),
//                WrappedTemplateMethod(
//                    "operator++",
//                    selfReference,
//                    emptyList(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "operator++",
//                    selfReference,
//                    listOf(WrappedTemplateArgument("dummy", WrappedTypeReference("int"))),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "operator==",
//                    WrappedTypeReference("bool"),
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    STATIC_OP
//                ),
//                WrappedTemplateMethod(
//                    "operator!=",
//                    WrappedTypeReference("bool"),
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    STATIC_OP
//                ),
//                WrappedTemplateMethod(
//                    "operator*",
//                    WrappedType.referenceTo(template),
//                    listOf(),
//                    false,
//                    METHOD
//                )
//
//            ),
//            emptyList(),
//            templateList
//        )
//    }
//
//    fun stdReverseIterator(
//        resolverBuilder: ResolverBuilder,
//        vararg types: WrappedTypeReference
//    ): WrappedClass {
//        require(types.size == 1) {
//            "Wrong number of type arguments for std::vector::reverse_iterator, " +
//                "not sure how to handle"
//        }
//        val type = types[0]
//
//        return vectorReverseIterator.typedWith(resolverBuilder, template to type)
//            .also { println("Declaring ${it.fullyQualified}") }
//    }
//
//    val vectorTemplate by lazy {
//        val templateList = listOf(template)
//        val selfReference = WrappedTemplatedReference("std::vector<T>", templateList)
//        val iterator = WrappedTemplatedReference("std::vector<T>::iterator", templateList)
//        val reverseIterator =
//            WrappedTemplatedReference("std::vector<T>::reverse_iterator", templateList)
//        WrappedTemplate(
//            selfReference.fullyQualifiedTemplate,
//            null,
//            listOf(),
//            listOf(
//                WrappedTemplateMethod(
//                    selfReference.fullyQualifiedTemplate,
//                    VOID,
//                    emptyList(),
//                    false,
//                    CONSTRUCTOR
//                ),
//                WrappedTemplateMethod(
//                    selfReference.fullyQualifiedTemplate,
//                    VOID,
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    CONSTRUCTOR
//                ),
//                WrappedTemplateMethod(
//                    selfReference.fullyQualifiedTemplate,
//                    VOID,
//                    listOf(),
//                    false,
//                    DESTRUCTOR
//                ),
//                WrappedTemplateMethod(
//                    "at",
//                    WrappedType.referenceTo(template),
//                    listOf(WrappedTemplateArgument("pos", WrappedTypeReference("std::size_t"))),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "operator[]",
//                    WrappedType.referenceTo(template),
//                    listOf(WrappedTemplateArgument("pos", WrappedTypeReference("std::size_t"))),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "front",
//                    WrappedType.referenceTo(template),
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "back",
//                    WrappedType.referenceTo(template),
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "data",
//                    WrappedType.pointerTo(template),
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "begin",
//                    iterator,
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "end",
//                    iterator,
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "rbegin",
//                    reverseIterator,
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "rend",
//                    reverseIterator,
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "empty",
//                    WrappedTypeReference("bool"),
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "size",
//                    WrappedTypeReference("std::size_t"),
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "max_size",
//                    WrappedTypeReference("std::size_t"),
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "reserve",
//                    VOID,
//                    listOf(WrappedTemplateArgument("new_cap", WrappedTypeReference("std::size_t"))),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "capacity",
//                    WrappedTypeReference("std::size_t"),
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "shrink_to_fit",
//                    VOID,
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "clear",
//                    VOID,
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "insert",
//                    iterator,
//                    listOf(
//                        WrappedTemplateArgument("pos", iterator),
//                        WrappedTemplateArgument("value", WrappedType.referenceTo(template))
//                    ),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "push_back",
//                    VOID,
//                    listOf(WrappedTemplateArgument("value", WrappedType.referenceTo(template))),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "pop_back",
//                    VOID,
//                    listOf(),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "resize",
//                    VOID,
//                    listOf(WrappedTemplateArgument("count", WrappedTypeReference("std::size_t"))),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "swap",
//                    VOID,
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    METHOD
//                ),
//                WrappedTemplateMethod(
//                    "operator==",
//                    WrappedTypeReference("bool"),
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    STATIC_OP
//                ),
//                WrappedTemplateMethod(
//                    "operator!=",
//                    WrappedTypeReference("bool"),
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    STATIC_OP
//                ),
//                WrappedTemplateMethod(
//                    "operator<",
//                    WrappedTypeReference("bool"),
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    STATIC_OP
//                ),
//                WrappedTemplateMethod(
//                    "operator<=",
//                    WrappedTypeReference("bool"),
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    STATIC_OP
//                ),
//                WrappedTemplateMethod(
//                    "operator>",
//                    WrappedTypeReference("bool"),
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    STATIC_OP
//                ),
//                WrappedTemplateMethod(
//                    "operator>=",
//                    WrappedTypeReference("bool"),
//                    listOf(
//                        WrappedTemplateArgument(
//                            "other",
//                            WrappedType.referenceTo(selfReference)
//                        )
//                    ),
//                    false,
//                    STATIC_OP
//                )
//            ),
//            emptyList(),
//            templateList
//        )
//    }
//
//    fun stdVector(
//        resolverBuilder: ResolverBuilder,
//        vararg types: WrappedTypeReference
//    ): WrappedClass {
//        require(types.size == 1) {
//            "Wrong number of type arguments for std::vector, not sure how to handle"
//        }
//        val type = types[0]
//        return vectorTemplate.typedWith(resolverBuilder, template to type).also {
//            println("Declaring ${it.fullyQualified}")
//        }
//    }

    private fun ResolverBuilder.type(s: String): WrappedTypeReference {
        return WrappedTypeReference(s).also { visit(it) }
    }
}
