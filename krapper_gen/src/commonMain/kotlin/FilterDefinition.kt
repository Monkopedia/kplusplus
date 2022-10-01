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
package com.monkopedia.krapper

import kotlinx.serialization.Serializable

@Serializable
sealed class FilterDefinition

@Serializable
object DefaultFilter : FilterDefinition()

enum class FilterableTypes {
    CLASS,
    METHOD,
    FIELD,
    TYPE,
    NAMESPACE;
}

@Serializable
class TypeFilter(vararg val types: FilterableTypes) : FilterDefinition()

@Serializable
class AndFilter(vararg val elements: FilterDefinition) : FilterDefinition()

@Serializable
class OrFilter(vararg val elements: FilterDefinition) : FilterDefinition()

@Serializable
class NotFilter(val base: FilterDefinition) : FilterDefinition()

enum class HierarchyTarget {
    PARENT,
    BASE,
    ANY_CHILD,
    ALL_CHILDREN
}

@Serializable
class HierarchyFilter(val target: HierarchyTarget, val filter: FilterDefinition) :
    FilterDefinition()

enum class StringMatcherType {
    STARTS_WITH,
    CONTAINS,
    EQUALS,
    ENDS_WITH,
    REGEX
}

@Serializable
class StringMatcher(val type: StringMatcherType, val str: String)

enum class StringSelector {
    STRINGIFY,
    CLASS_NAME,
    CLASS_QUALIFIED,
    METHOD_NAME,
    METHOD_TYPE,
    METHOD_RETURN_TYPE,
    NAMESPACE
}

@Serializable
class StringFilter(val selector: StringSelector, val matcher: StringMatcher) : FilterDefinition()
