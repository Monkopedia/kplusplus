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

import clang.CXAvailabilityKind
import clang.CXCursor
import clang.CXCursorKind
import clang.CXCursorKind.CXCursor_ClassDecl
import clang.CXCursorKind.CXCursor_ClassTemplate
import clang.CXCursorKind.CXCursor_StructDecl
import clang.CX_CXXAccessSpecifier
import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.accessSpecifier
import com.monkopedia.krapper.generator.availability
import com.monkopedia.krapper.generator.forEachRecursive
import com.monkopedia.krapper.generator.getArgument
import com.monkopedia.krapper.generator.isCopyConstructor
import com.monkopedia.krapper.generator.isDefaultConstructor
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.numArguments
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.semanticParent
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import com.monkopedia.krapper.generator.usr
import kotlinx.cinterop.CValue

@ThreadLocal
private val elementLookup = mutableMapOf<String, WrappedElement>()

abstract class WrappedElement(
    private val mutableChildren: MutableList<WrappedElement> = mutableListOf()
) {
    val children: List<WrappedElement>
        get() = mutableChildren
    var parent: WrappedElement? = null

    fun clearChildren() {
        mutableChildren.clear()
    }

    open fun addAllChildren(list: List<WrappedElement>) {
        list.forEach {
            require(!children.contains(it)) {
                "$this already contains $it"
            }
        }
        mutableChildren.addAll(list)
        list.forEach { it.parent = this }
    }

    open fun addChild(child: WrappedElement) {
        require(!children.contains(child)) {
            "$this already contain a $child"
        }
        mutableChildren.add(child)
        child.parent = this
    }

    fun removeChild(child: WrappedElement) {
        mutableChildren.remove(child)
    }

    abstract fun clone(): WrappedElement
    abstract suspend fun resolve(resolverContext: ResolveContext): ResolvedElement?

    companion object {
        fun mapAll(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder): WrappedElement? {
            val element = map(value, null, null, resolverBuilder) ?: return null
            value.forEachRecursive { childCursor, parentCursor ->
                val parentUsr = parentCursor.usr.toKString()

                val parent =
                    map(parentCursor, null, null, resolverBuilder) ?: return@forEachRecursive
                val child =
                    map(childCursor, parent, parentUsr, resolverBuilder) ?: return@forEachRecursive
                if (child is WrappedTemplate) {
                    child.templateArgCounter = 0
                }
                if (child == element) return@forEachRecursive
                if (child.parent == parent) return@forEachRecursive
                if (parent.children.contains(child)) {
                    if (child is WrappedTemplateParam ||
                        parent is WrappedTemplateParam ||
                        child is WrappedTemplateRef ||
                        child is WrappedNamespace ||
                        child == WrappedType.UNRESOLVABLE
                    ) return@forEachRecursive
//                    throw IllegalArgumentException(
//                        "Parent ($parent) already contains child ($child)"
//                    )
                    return@forEachRecursive
                }
                if (child is WrappedMethod && parent is WrappedNamespace) {
                    // Don't add a method to a namespace when its already been added to a class.
                    if (child.parent is WrappedClass) {
                        return@forEachRecursive
                    }
                    val parentKind = childCursor.semanticParent.kind
                    if (parentKind == CXCursor_ClassDecl ||
                        parentKind == CXCursor_ClassTemplate ||
                        parentKind == CXCursor_StructDecl
                    ) {
                        return@forEachRecursive
                    }
                }
                parent.addChild(child)
            }
            return element
        }

        private fun map(
            value: CValue<CXCursor>,
            parent: WrappedElement?,
            parentUsr: String?,
            resolverBuilder: ResolverBuilder
        ): WrappedElement? {
            val strTag =
                value.usr.toKString().orEmpty()
                    .ifEmpty { "$parentUsr:${value.spelling.toKString()}" }

            elementLookup[strTag]?.let { return it }

            if (value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPrivate ||
                value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXProtected ||
                value.availability == CXAvailabilityKind.CXAvailability_NotAvailable
            ) {
                if (value.kind == CXCursorKind.CXCursor_Constructor) {
                    (parent as? WrappedClass)?.metadata?.hasConstructor = true
                    (parent as? WrappedTemplate)?.metadata?.hasConstructor = true
                }
                if (value.kind == CXCursorKind.CXCursor_CXXMethod) {
                    val opName = value.referenced.spelling.toKString()
                    if (opName == "operator new") {
                        (parent as? WrappedClass)?.metadata?.hasHiddenNew = true
                        (parent as? WrappedTemplate)?.metadata?.hasHiddenNew = true
                    } else if (opName == "operator delete") {
                        (parent as? WrappedClass)?.metadata?.hasHiddenDelete = true
                        (parent as? WrappedTemplate)?.metadata?.hasHiddenDelete = true
                    }
                }
                if (value.kind == CXCursorKind.CXCursor_FieldDecl) {
                    if (WrappedType(value.type, resolverBuilder).isConst) {
                        (parent as? WrappedClass)?.metadata?.hasPrivateConstField = true
                        (parent as? WrappedTemplate)?.metadata?.hasPrivateConstField = true
                    }
                }
                return null
            }
            val element = when (value.kind) {
//                CXCursorKind.CXCursor_UnexposedDecl -> TODO()
//                CXCursorKind.CXCursor_UnionDecl -> TODO()
                CXCursorKind.CXCursor_StructDecl,
                CXCursorKind.CXCursor_ClassDecl -> WrappedClass(value, resolverBuilder)
//                CXCursorKind.CXCursor_EnumDecl -> TODO()
//                CXCursorKind.CXCursor_EnumConstantDecl -> TODO()
                CXCursorKind.CXCursor_FieldDecl -> WrappedField(value, resolverBuilder)
                CXCursorKind.CXCursor_ParmDecl -> return null
                // WrappedArgument(value, resolverBuilder)
                CXCursorKind.CXCursor_TypedefDecl ->
                    try {
                        WrappedTypedef(value, resolverBuilder)
                    } catch (t: IllegalArgumentException) {
                        // Don't mind when parsing everything, if this reference is needed,
                        // it'll come up in resolution
                        return null
                    }
                CXCursorKind.CXCursor_FunctionDecl,
                CXCursorKind.CXCursor_CXXMethod -> {
                    if (value.referenced.spelling.toKString() in listOf(
                            "operator new",
                            "operator new[]",
                            "operator delete",
                            "operator delete[]"
                        )
                    ) {
                        return null
                    }
                    WrappedMethod(value, resolverBuilder).also {
                        for (i in 0 until value.numArguments) {
                            it.addChild(
                                WrappedArgument(
                                    value.getArgument(i.toUInt()),
                                    resolverBuilder,
                                    i
                                )
                            )
                        }
                    }
                }
                CXCursorKind.CXCursor_Namespace -> WrappedNamespace(
                    value.spelling.toKString() ?: error("Namespace without name")
                )
                CXCursorKind.CXCursor_Constructor ->
                    WrappedConstructor(
                        value.spelling.toKString() ?: "constructor", WrappedType.VOID,
                        value.isCopyConstructor, value.isDefaultConstructor
                    ).also {
                        for (i in 0 until value.numArguments) {
                            it.addChild(
                                WrappedArgument(
                                    value.getArgument(i.toUInt()),
                                    resolverBuilder,
                                    i
                                )
                            )
                        }
                    }
                CXCursorKind.CXCursor_Destructor ->
                    WrappedDestructor(
                        value.spelling.toKString() ?: "destructor", WrappedType.VOID
                    ).also {
                        for (i in 0 until value.numArguments) {
                            it.addChild(
                                WrappedArgument(
                                    value.getArgument(i.toUInt()),
                                    resolverBuilder,
                                    i
                                )
                            )
                        }
                    }
//                CXCursorKind.CXCursor_NamespaceAlias -> TODO()
                CXCursorKind.CXCursor_TemplateTypeParameter -> WrappedTemplateParam(
                    value,
                    resolverBuilder
                )
//                CXCursorKind.CXCursor_NonTypeTemplateParameter -> TODO()
//                CXCursorKind.CXCursor_TemplateTemplateParameter -> TODO()
                CXCursorKind.CXCursor_ClassTemplate -> WrappedTemplate(value, resolverBuilder)
//                CXCursorKind.CXCursor_ClassTemplatePartialSpecialization -> TODO()
//                CXCursorKind.CXCursor_TypeAliasDecl -> TODO()
                CXCursorKind.CXCursor_TypeRef -> WrappedTemplateRef(
                    value.spelling.toKString() ?: error("TypeRef without a name")
                )
                CXCursorKind.CXCursor_CXXBaseSpecifier -> WrappedBase(
                    try {
                        WrappedType(value.type, resolverBuilder)
                    } catch (t: IllegalArgumentException) {
                        // Don't mind when parsing everything, if this reference is needed,
                        // it'll come up in resolution
                        return null
                    }
                )
                CXCursorKind.CXCursor_TemplateRef ->
                    try {
                        WrappedType(value.type, resolverBuilder)
                    } catch (t: IllegalArgumentException) {
                        // Don't mind when parsing everything, if this reference is needed,
                        // it'll come up in resolution
                        return null
                    }
                CXCursorKind.CXCursor_TranslationUnit -> WrappedTU()
                else -> return null
            }
            elementLookup[strTag] = element
            return element
        }
    }
}

fun WrappedElement.forEachRecursive(onEach: (WrappedElement) -> Unit) {
    for (child in children) {
        onEach(child)
        child.forEachRecursive(onEach)
    }
}

fun WrappedElement.filterRecursive(
    ret: MutableList<WrappedElement> = mutableListOf(),
    onEach: (WrappedElement) -> Boolean
): List<WrappedElement> {
    for (child in children) {
        if (onEach(child)) {
            ret.add(child)
        }
        child.filterRecursive(ret, onEach)
    }
    return ret
}

fun <T : WrappedElement> T.cloneRecursive(): T {
    return clone().also {
        val newChildren = it.children.map { it.cloneRecursive() }
        it.clearChildren()
        it.addAllChildren(newChildren)
    } as T
}
