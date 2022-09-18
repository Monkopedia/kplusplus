package com.monkopedia.krapper

import com.monkopedia.krapper.FilterDsl.ElementTarget.ALL_CHILDREN
import com.monkopedia.krapper.FilterDsl.ElementTarget.BASE
import com.monkopedia.krapper.FilterDsl.ElementTarget.CHILD
import com.monkopedia.krapper.FilterDsl.ElementTarget.PARENT
import com.monkopedia.krapper.FilterDsl.ElementTarget.THIS
import com.monkopedia.krapper.StringMatcherType.CONTAINS
import com.monkopedia.krapper.StringMatcherType.ENDS_WITH
import com.monkopedia.krapper.StringMatcherType.EQUALS
import com.monkopedia.krapper.StringMatcherType.REGEX
import com.monkopedia.krapper.StringMatcherType.STARTS_WITH
import com.monkopedia.krapper.StringSelector.CLASS_NAME
import com.monkopedia.krapper.StringSelector.CLASS_QUALIFIED
import com.monkopedia.krapper.StringSelector.METHOD_NAME
import com.monkopedia.krapper.StringSelector.METHOD_RETURN_TYPE
import com.monkopedia.krapper.StringSelector.METHOD_TYPE
import com.monkopedia.krapper.StringSelector.NAMESPACE
import com.monkopedia.krapper.StringSelector.STRINGIFY
import kotlin.reflect.KClass

inline fun filter(filterBuilder: FilterDsl.() -> FilterDefinition): FilterDefinition {
    return FilterDsl().filterBuilder()
}

open class TypeTarget<T : Any>(val filterType: FilterableTypes, val targetClass: KClass<T>)

class FilterDsl {

    enum class ElementTarget(val target: HierarchyTarget?) :
        (FilterDefinition) -> FilterDefinition {
        THIS(null),
        PARENT(HierarchyTarget.PARENT),
        BASE(HierarchyTarget.BASE),
        CHILD(HierarchyTarget.ANY_CHILD),
        ALL_CHILDREN(HierarchyTarget.ALL_CHILDREN);

        fun wrap(filter: FilterDefinition): FilterDefinition {
            return HierarchyFilter(target ?: return filter, filter)
        }

        override fun invoke(filter: FilterDefinition): FilterDefinition = wrap(filter)
    }

    val className: StringSelector
        inline get() = CLASS_NAME
    val qualified: StringSelector
        inline get() = CLASS_QUALIFIED
    val namespace: StringSelector
        inline get() = NAMESPACE
    val methodName: StringSelector
        inline get() = METHOD_NAME
    val methodType: StringSelector
        inline get() = METHOD_TYPE
    val methodReturnType: StringSelector
        inline get() = METHOD_RETURN_TYPE

    val stringified: StringSelector
        inline get() = STRINGIFY
    val thiz: ElementTarget
        inline get() = THIS
    val parent: ElementTarget
        inline get() = PARENT
    val base: ElementTarget
        inline get() = BASE
    val child: ElementTarget
        inline get() = CHILD
    val allChildren: ElementTarget
        inline get() = ALL_CHILDREN

    operator fun FilterDefinition.not(): FilterDefinition {
        return NotFilter(this)
    }

    operator fun FilterDefinition.plus(other: FilterDefinition): FilterDefinition = and(other)

    inline infix fun ElementTarget.isType(target: TypeTarget): FilterDefinition {
        return wrap(TypeFilter(target.filterType))
    }

    inline infix fun FilterDefinition.and(other: FilterDefinition): FilterDefinition {
        val thisElements = if (this is AndFilter) elements else arrayOf(this)
        val otherElements = if (other is AndFilter) other.elements else arrayOf(other)
        return AndFilter(*thisElements, *otherElements)
    }

    inline infix fun FilterDefinition.or(other: FilterDefinition): FilterDefinition {
        val thisElements = if (this is OrFilter) elements else arrayOf(this)
        val otherElements = if (other is OrFilter) other.elements else arrayOf(other)
        return OrFilter(*thisElements, *otherElements)
    }

    inline infix fun StringSelector.eq(str: String): FilterDefinition {
        return StringFilter(this, StringMatcher(EQUALS, str))
    }

    inline infix fun StringSelector.contains(str: String): FilterDefinition {
        return StringFilter(this, StringMatcher(CONTAINS, str))
    }

    inline infix fun StringSelector.startsWith(str: String): FilterDefinition {
        return StringFilter(this, StringMatcher(STARTS_WITH, str))
    }

    inline infix fun StringSelector.endsWith(str: String): FilterDefinition {
        return StringFilter(this, StringMatcher(ENDS_WITH, str))
    }

    inline infix fun StringSelector.regex(str: String): FilterDefinition {
        return StringFilter(this, StringMatcher(REGEX, str))
    }
}
