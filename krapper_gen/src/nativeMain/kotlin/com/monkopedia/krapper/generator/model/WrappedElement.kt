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
package com.monkopedia.krapper.generator.model

import clang.CXAvailabilityKind
import clang.CXCursor
import clang.CXCursorKind
import clang.CX_CXXAccessSpecifier
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.accessSpecifier
import com.monkopedia.krapper.generator.availability
import com.monkopedia.krapper.generator.forEachRecursive
import com.monkopedia.krapper.generator.isCopyConstructor
import com.monkopedia.krapper.generator.isDefaultConstructor
import com.monkopedia.krapper.generator.isStatic
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import com.monkopedia.krapper.generator.usr
import kotlinx.cinterop.CValue

@ThreadLocal
private val elementLookup = mutableMapOf<String, WrappedElement>()

open class WrappedElement(
    private val mutableChildren: MutableList<WrappedElement> = mutableListOf()
) {
    val children: List<WrappedElement>
        get() = mutableChildren
    var parent: WrappedElement? = null

    fun clearChildren() {
        mutableChildren.clear()
    }

    fun addAllChildren(list: List<WrappedElement>) {
        list.forEach {
            require(!children.contains(it)) {
                "$this already contains $it"
            }
        }
        mutableChildren.addAll(list)
    }

    fun addChild(child: WrappedElement) {
        require(!children.contains(child)) {
            "$this already contain a $child"
        }
        mutableChildren.add(child)
    }

    open fun clone(): WrappedElement {
        return WrappedElement(children.toMutableList()).also {
            it.parent = parent
        }
    }

    fun removeChild(child: WrappedElement) {
        mutableChildren.remove(child)
    }

    companion object {
        fun mapAll(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder): WrappedElement? {
            val element = map(value, null, null, resolverBuilder) ?: return null
            value.forEachRecursive { child, parent ->
                val parentUsr = parent.usr.toKString()
                val strTag = child.usr.toKString().orEmpty()
                    .ifEmpty { "$parentUsr:${child.spelling.toKString()}" }

                val parent = map(parent, null, null, resolverBuilder) ?: return@forEachRecursive
                val child =
                    map(child, parent, parentUsr, resolverBuilder) ?: return@forEachRecursive
                if (child == element) return@forEachRecursive
                if (child.parent == parent) return@forEachRecursive
                if (parent is WrappedMethod && child is WrappedArgument) {
                    // Hack for now to handle complicated parts of the AST from clang.
                    // There are multiple method declarations with the same usr, but
                    // containing params with different usr, not sure what to do with that...
                    if (parent.args.any { it.name == child.name }) {
                        return@forEachRecursive
                    }
                }
                if (parent.children.contains(child)) {
                    if (child is WrappedTemplateParam ||
                        parent is WrappedTemplateParam ||
                        child is WrappedTemplateRef ||
                        child is WrappedNamespace ||
                        child == WrappedType.UNRESOLVABLE
                    ) return@forEachRecursive
                    throw IllegalArgumentException(
                        "Parent ($parent) already contains child ($child)"
                    )
                }
                parent.addChild(child)
                child.parent = parent
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
                /*if (value.kind == CXCursorKind.CXCursor_TemplateTypeParameter)  "${parentUsr}:${value.spelling.toKString()}" else  */
                value.usr.toKString().orEmpty()
                    .ifEmpty { "$parentUsr:${value.spelling.toKString()}" }

            elementLookup[strTag]?.let { return it }
            // if (value.fullyQualified == "TestLib::MyPair") {
            // println("TestLib::MyPair ${value.kind}")
            // var parent = value.lexicalParent
            // while (parent.kind < CXCursorKind.CXCursor_FirstInvalid || parent.kind > CXCursorKind.CXCursor_LastInvalid) {
            // println("Parent ${parent.fullyQualified} ${parent.kind}")
            // parent = parent.lexicalParent
            // }
            // }

            if (value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPrivate ||
                value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXProtected ||
                value.availability == CXAvailabilityKind.CXAvailability_NotAvailable
            ) {
                if (value.kind == CXCursorKind.CXCursor_Constructor) {
                    (parent as? WrappedClass)?.hasConstructor = true
                    (parent as? WrappedTemplate)?.hasConstructor = true
                }
                if (value.kind == CXCursorKind.CXCursor_CXXMethod) {
                    val opName = value.referenced.spelling.toKString()
                    if (opName == "operator new") {
                        (parent as? WrappedClass)?.hasHiddenNew = true
                        (parent as? WrappedTemplate)?.hasHiddenNew = true
                    } else if (opName == "operator delete") {
                        (parent as? WrappedClass)?.hasHiddenDelete = true
                        (parent as? WrappedTemplate)?.hasHiddenDelete = true
                    }
                }
                return null
            }
            val element = when (value.kind) {
//                CXCursorKind.CXCursor_UnexposedDecl -> TODO()
//                CXCursorKind.CXCursor_StructDecl -> TODO()
//                CXCursorKind.CXCursor_UnionDecl -> TODO()
                CXCursorKind.CXCursor_ClassDecl -> WrappedClass(value, resolverBuilder)
//                CXCursorKind.CXCursor_EnumDecl -> TODO()
//                CXCursorKind.CXCursor_EnumConstantDecl -> TODO()
                CXCursorKind.CXCursor_FieldDecl -> WrappedField(value, resolverBuilder)
                CXCursorKind.CXCursor_ParmDecl -> WrappedArgument(value, resolverBuilder)
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
                    if (value.isStatic) return null
                    if (value.referenced.spelling.toKString() in listOf(
                            "operator new",
                            "operator new[]",
                            "operator delete",
                            "operator delete[]"
                        )
                    ) {
                        return null
                    }
                    WrappedMethod(value, resolverBuilder)
                }
                CXCursorKind.CXCursor_Namespace -> WrappedNamespace(
                    value.spelling.toKString() ?: error("Namespace without name")
                )
                CXCursorKind.CXCursor_Constructor -> WrappedConstructor(
                    value.spelling.toKString() ?: "constructor", WrappedType.VOID,
                    value.isCopyConstructor, value.isDefaultConstructor
                )
                CXCursorKind.CXCursor_Destructor -> WrappedDestructor(
                    value.spelling.toKString() ?: "destructor", WrappedType.VOID
                )
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
