package com.monkopedia.krapper.generator.model

import clang.CXCursor
import clang.CXTypeKind
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.accessSpecifier
import com.monkopedia.krapper.generator.arrayElementType
import com.monkopedia.krapper.generator.availability
import com.monkopedia.krapper.generator.canonicalCursor
import com.monkopedia.krapper.generator.canonicalType
import com.monkopedia.krapper.generator.classType
import com.monkopedia.krapper.generator.definition
import com.monkopedia.krapper.generator.displayName
import com.monkopedia.krapper.generator.elementType
import com.monkopedia.krapper.generator.extend
import com.monkopedia.krapper.generator.fullyQualified
import com.monkopedia.krapper.generator.getArgType
import com.monkopedia.krapper.generator.getTemplateArgumentType
import com.monkopedia.krapper.generator.hash
import com.monkopedia.krapper.generator.isAbstract
import com.monkopedia.krapper.generator.isAnonymous
import com.monkopedia.krapper.generator.isDefaulted
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.mapChildren
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.modifiedType
import com.monkopedia.krapper.generator.namedType
import com.monkopedia.krapper.generator.pointeeType
import com.monkopedia.krapper.generator.rawCommentText
import com.monkopedia.krapper.generator.refQualifier
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.result
import com.monkopedia.krapper.generator.resultType
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import com.monkopedia.krapper.generator.typeDeclaration
import com.monkopedia.krapper.generator.typedefDeclUnderlyingType
import com.monkopedia.krapper.generator.usr
import com.monkopedia.krapper.generator.valueType
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents

data class WrappedTemplate(val name: String) : WrappedElement() {
    val baseClass: WrappedType?
        get() = children.filterIsInstance<WrappedBase>().firstOrNull()?.type

    val qualified: String
        get() = withParents.mapNotNull { it.named }.joinToString("::")
    private val WrappedElement.withParents: List<WrappedElement>
        get() = this@withParents.parent?.withParents?.plus(listOf(this@withParents))
            ?: listOf(this@withParents)
    private val WrappedElement.named: String?
        get() = when (this) {
            is WrappedClass -> this@named.name
            is WrappedTemplate -> this@named.name
            is WrappedNamespace -> this@named.namespace
            else -> null
        }
    val templateArgs: List<WrappedTemplateParam>
        get() = children.filterIsInstance<WrappedTemplateParam>()
    val fields: List<WrappedField>
        get() = children.filterIsInstance<WrappedField>()
    val methods: List<WrappedMethod>
        get() = children.filterIsInstance<WrappedMethod>()

    constructor(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        value.spelling.toKString() ?: error("Missing name")
    )

//    inline fun typedWith(
//        resolverBuilder: ResolverBuilder,
//        vararg values: Pair<WrappedTemplateType, WrappedType>
//    ): WrappedClass = typedWith(resolverBuilder, values.toMap())
//
//    fun typedWith(
//        resolverBuilder: ResolverBuilder,
//        values: Map<WrappedTemplateType, WrappedType>
//    ): WrappedClass {
//        for (t in templateArgs) {
//            require(t in values.keys) {
//                "Missing parameter $t in instance"
//            }
//        }
//        return WrappedClass(
//            type.typedWith(resolverBuilder, values).name,
//            baseClass?.typedWith(resolverBuilder, values),
//            fields.map { it.typedWith(resolverBuilder, values) },
//            methods.map { it.typedWith(resolverBuilder, values) }
//        )
//    }

    override fun toString(): String {
        return buildString {
            append("class $qualified<${templateArgs.joinToString(", ")}> {\n")
            baseClass?.let {
                append("    super $it")
            }
            append("\n")
            for (field in fields) {
                append("    $field\n")
            }
            append("\n")
            for (method in methods) {
                append("    $method\n")
            }
            append("\n")

            append("}\n")
        }
    }

    override fun clone(): WrappedElement {
        return WrappedTemplate(name).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }
}

class WrappedTemplateParam(val name: String, val usr: String) : WrappedElement() {
    val defaultType: WrappedType?
        get() {
            if (children.isEmpty()) return null
            return null
        }

    constructor(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        value.spelling.toKString() ?: error("Template param without name $value"),
        value.usr.toKString() ?: error("Template param without USR $value")
    )

    override fun clone(): WrappedTemplateParam {
        return WrappedTemplateParam(name, usr).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    companion object {
        private fun determineType(
            value: CValue<CXCursor>,
            resolverBuilder: ResolverBuilder
        ): WrappedType? {
            val type = value.type
            type.useContents {
                if (kind == CXTypeKind.CXType_Invalid) {
                    return@determineType null
                }
            }
            return WrappedType(type, resolverBuilder)
        }
    }
}

class WrappedTypedef(val name: String, val targetType: WrappedType) : WrappedElement() {
    constructor(
        value: CValue<CXCursor>,
        resolverBuilder: ResolverBuilder
    ) : this(
        value.spelling.toKString() ?: error("Template param without name $value"),
        determineType(value, resolverBuilder)
    )

    override fun clone(): WrappedTypedef {
        return WrappedTypedef(name, targetType).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    companion object {
        private fun determineType(
            value: CValue<CXCursor>,
            resolverBuilder: ResolverBuilder
        ): WrappedType {
            return WrappedType(value.type, resolverBuilder)
        }
    }
}
