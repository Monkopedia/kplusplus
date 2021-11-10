package com.monkopedia.krapper.generator.model

import clang.CXCursor
import clang.CXCursorKind
import clang.CX_CXXAccessSpecifier
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.accessSpecifier
import com.monkopedia.krapper.generator.forEach
import com.monkopedia.krapper.generator.hash
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import com.monkopedia.krapper.generator.usr
import kotlinx.cinterop.CValue

@ThreadLocal
private val elementLookup = mutableMapOf<String, WrappedElement>()

open class WrappedElement(
    val children: MutableList<WrappedElement> = mutableListOf()
) : Iterable<WrappedElement> by children {
    var parent: WrappedElement? = null

    open fun clone(): WrappedElement {
        return WrappedElement(children.toMutableList()).also {
            it.parent = parent
        }
    }

    companion object {
        fun map(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder): WrappedElement? {
            val strTag = value.usr.toKString() ?: "${value.spelling.toKString()}:${value.hash}"
            elementLookup[strTag]?.let { return it }

            if (value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPrivate) {
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
                CXCursorKind.CXCursor_FunctionDecl -> WrappedMethod(value, resolverBuilder)
                CXCursorKind.CXCursor_ParmDecl -> WrappedArgument(value, resolverBuilder)
                CXCursorKind.CXCursor_TypedefDecl -> WrappedTypedef(value, resolverBuilder)
                CXCursorKind.CXCursor_CXXMethod -> WrappedMethod(value, resolverBuilder)
                CXCursorKind.CXCursor_Namespace -> WrappedNamespace(
                    value.spelling.toKString() ?: error("Namespace without name")
                )
                CXCursorKind.CXCursor_Constructor -> WrappedConstructor(
                    value.spelling.toKString() ?: "constructor", WrappedTypeReference.VOID
                )
                CXCursorKind.CXCursor_Destructor -> WrappedConstructor(
                    value.spelling.toKString() ?: "constructor", WrappedTypeReference.VOID
                )
//                CXCursorKind.CXCursor_NamespaceAlias -> TODO()
                CXCursorKind.CXCursor_TemplateTypeParameter -> WrappedTemplateParam(value, resolverBuilder)
//                CXCursorKind.CXCursor_NonTypeTemplateParameter -> TODO()
//                CXCursorKind.CXCursor_TemplateTemplateParameter -> TODO()
                CXCursorKind.CXCursor_ClassTemplate -> WrappedTemplate(value, resolverBuilder)
//                CXCursorKind.CXCursor_ClassTemplatePartialSpecialization -> TODO()
//                CXCursorKind.CXCursor_TypeAliasDecl -> TODO()
//                CXCursorKind.CXCursor_TypeRef -> TODO()
                CXCursorKind.CXCursor_CXXBaseSpecifier -> WrappedBase(
                    WrappedTypeReference(
                        value.type,
                        resolverBuilder
                    )
                )
                CXCursorKind.CXCursor_TemplateRef -> WrappedTypeReference(
                    value.type,
                    resolverBuilder
                )
                CXCursorKind.CXCursor_TranslationUnit -> WrappedTU()
                else -> return null
            }
            elementLookup[strTag] = element
            value.forEach { child ->
                val strTag = value.usr.toKString() ?: "${value.spelling.toKString()}:${value.hash}"
                if (elementLookup.containsKey(strTag)) return@forEach
                map(child, resolverBuilder)?.let { mappedChild ->
                    element.children.add(mappedChild)
                    mappedChild.parent = element
                }
            }
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

fun WrappedElement.filterRecursive(onEach: (WrappedElement) -> Boolean): List<WrappedElement> {
    val ret = mutableListOf<WrappedElement>()
    for (child in children) {
        if (onEach(child)) {
            ret.add(child)
        }
        child.filterRecursive(onEach)
    }
    return ret
}
