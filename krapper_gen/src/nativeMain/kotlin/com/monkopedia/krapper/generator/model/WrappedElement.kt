package com.monkopedia.krapper.generator.model

import clang.CXCursor
import clang.CXCursorKind
import clang.CXCursorKind.CXCursor_FunctionTemplate
import clang.CXCursorKind.Companion
import clang.CX_CXXAccessSpecifier
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.accessSpecifier
import com.monkopedia.krapper.generator.forEachRecursive
import com.monkopedia.krapper.generator.fullyQualified
import com.monkopedia.krapper.generator.hash
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.lexicalParent
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import com.monkopedia.krapper.generator.usr
import kotlinx.cinterop.CValue
import platform.posix.usleep

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

    companion object {
        fun mapAll(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder): WrappedElement? {
            val element = map(value, resolverBuilder) ?: return null
            value.forEachRecursive { child, parent ->
                val parent = map(parent, resolverBuilder) ?: return@forEachRecursive
                val child = map(child, resolverBuilder) ?: return@forEachRecursive
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
                    throw IllegalArgumentException("$parent already contains $child")
                }
                parent.addChild(child)
                child.parent = parent
            }
            return element
        }

        private fun map(
            value: CValue<CXCursor>,
            resolverBuilder: ResolverBuilder
        ): WrappedElement? {
            return fetchElement(value, resolverBuilder)
        }

        private fun fetchElement(
            value: CValue<CXCursor>,
            resolverBuilder: ResolverBuilder
        ): WrappedElement? {
            val strTag = value.usr.toKString() ?: "${value.spelling.toKString()}:${value.hash}"

            elementLookup[strTag]?.let { return it }
            if (value.fullyQualified == "TestLib::MyPair") {
                println("TestLib::MyPair ${value.kind}")
                var parent = value.lexicalParent
                while (parent.kind < CXCursorKind.CXCursor_FirstInvalid || parent.kind > CXCursorKind.CXCursor_LastInvalid) {
                    println("Parent ${parent.fullyQualified} ${parent.kind}")
                    parent = parent.lexicalParent
                }
            }

            if (value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPrivate || value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXProtected) {
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
                    value.spelling.toKString() ?: "constructor", WrappedType.VOID
                )
                CXCursorKind.CXCursor_Destructor -> WrappedConstructor(
                    value.spelling.toKString() ?: "constructor", WrappedType.VOID
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
//                CXCursorKind.CXCursor_TypeRef -> TODO()
                CXCursorKind.CXCursor_CXXBaseSpecifier -> WrappedBase(
                    WrappedType(
                        value.type,
                        resolverBuilder
                    )
                )
                CXCursorKind.CXCursor_TemplateRef -> WrappedType(
                    value.type,
                    resolverBuilder
                )
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
