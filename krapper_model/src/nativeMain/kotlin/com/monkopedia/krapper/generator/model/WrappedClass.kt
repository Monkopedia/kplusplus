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
package com.monkopedia.krapper.generator.model

import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.ALIGN_OF
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.SIZE_OF

class WrappedBase(
    val type: WrappedType?,
    val isPublic: Boolean = true,
    val isVirtualBase: Boolean = false
) : WrappedElement() {
    override fun clone(): WrappedElement = WrappedBase(type, isPublic, isVirtualBase).also {
        it.addAllChildren(children.map { it.clone() })
    }
}

data class ClassMetadata(
    var hasHiddenNew: Boolean = false,
    var hasHiddenDelete: Boolean = false,
    var hasConstructor: Boolean = false,
    var hasPrivateConstField: Boolean = false,
    var hasDefaultConstructor: Boolean = false,
    var hasCopyConstructor: Boolean = false,
    // Set when the class's copy constructor is inaccessible for the generated C wrapper:
    // either explicitly `= delete`d or private/protected. Such a type can't be copied into
    // a by-value Holder (placement-new copy), so a method/field that returns it by value is
    // unmodelable and is dropped (T-skip). Recorded at parse time because the offending
    // constructor cursor is filtered out (NotAvailable/non-public) before it ever becomes a
    // child, so it can't be re-derived from the resolved children later.
    var hasDeletedCopyConstructor: Boolean = false,
    // Same idea for copy ASSIGNMENT (`operator=`): when it's deleted/inaccessible the
    // generated field setter's `field = *value` won't compile, so the setter is dropped.
    var hasDeletedCopyAssignment: Boolean = false,
    // Set when this class can't be DEFAULT-constructed because it has a (public) reference
    // data member and declares no usable constructor: its implicit default constructor is
    // deleted (a reference must be initialized), so krapper must NOT synthesize a
    // default-construct path (`new T()`) for it — that would emit a call to the
    // implicitly-deleted default ctor (T-skip residual, e.g. FunctionEffectSet::Conflict).
    var hasDeletedDefaultConstructor: Boolean = false
)

class WrappedClass(
    val name: String,
    var isAbstract: Boolean = false,
    val specifiedType: WrappedType? = null
) : WrappedElement() {
    var metadata: ClassMetadata = ClassMetadata()
    val baseClass: WrappedType?
        get() = children.filterIsInstance<WrappedBase>().firstOrNull()?.type

    // Every base specifier of this class (primary + secondary), in declaration order.
    // `baseClass` above is the FIRST (offset-0) base used by the existing single-
    // inheritance paths; multi-inheritance codegen uses this full list so 2nd+ bases
    // (which are NOT at offset 0) can be flattened with an offset-aware C wrapper.
    val baseClasses: List<WrappedBase>
        get() = children.filterIsInstance<WrappedBase>()

    val type: WrappedType
        get() = specifiedType ?: WrappedType(qualified)

    override fun clone(): WrappedClass = clone(this.specifiedType)

    fun clone(specifiedType: WrappedType? = this.specifiedType): WrappedClass =
        WrappedClass(name, isAbstract, specifiedType).also {
            it.parent = parent
            it.addAllChildren(children)
            it.metadata = metadata.copy()
        }

    override fun toString(): String = qualified

    private var isNotEmptyCache: Boolean? = null

    override fun addChild(child: WrappedElement) {
        isNotEmptyCache = null
        super.addChild(child)
    }

    override fun addAllChildren(list: List<WrappedElement>) {
        isNotEmptyCache = null
        super.addAllChildren(list)
    }

    fun isNotEmpty(): Boolean = isNotEmptyCache ?: calculateNotEmpty().also {
        isNotEmptyCache = it
    }

    private fun calculateNotEmpty() = baseClass != null || children.any {
        (it !is WrappedBase) &&
            ((it as? WrappedMethod)?.methodType != SIZE_OF) &&
            ((it as? WrappedMethod)?.methodType != ALIGN_OF) &&
            ((it as? WrappedConstructor)?.children?.isNotEmpty() != false)
    }
}

val WrappedElement.qualified: String
    get() = withParents.mapNotNull { it.named }.joinToString("::")

// T1.0c: fully-qualified spelling of a FREE FUNCTION (a STATIC method with no parent
// class). `qualified` yields only the enclosing namespace path because a method name is
// not a `named` segment, so append the method name to get `<namespace>::<name>` (or just
// `<name>` for a function at file scope). Used to match free functions against an
// AllowListFilter the same way classes are matched by `type`.
val WrappedMethod.freeFunctionQualifiedName: String
    get() = qualified.let { if (it.isEmpty()) name else "$it::$name" }
private val WrappedElement.withParents: List<WrappedElement>
    get() = this@withParents.parent?.withParents?.plus(listOf(this@withParents))
        ?: listOf(this@withParents)
private val WrappedElement.named: String?
    get() = when (this) {
        is WrappedClass -> this@named.name
        is WrappedNamespace -> this@named.namespace
        else -> null
    }
