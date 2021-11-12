package com.monkopedia.krapper.generator.model

import clang.CXCursor
import clang.CXTypeKind
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
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
            it.children.addAll(children)
            it.parent = parent
        }
    }
}

class WrappedTemplateParam(val name: String, val defaultType: WrappedType?) : WrappedElement() {
    constructor(
        value: CValue<CXCursor>,
        resolverBuilder: ResolverBuilder
    ) : this(
        value.spelling.toKString() ?: error("Template param without name $value"),
        determineType(value, resolverBuilder)
    )

    override fun clone(): WrappedTemplateParam {
        return WrappedTemplateParam(name, defaultType).also {
            it.children.addAll(children)
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
                if (kind == CXTypeKind.CXType_Invalid || kind == CXTypeKind.CXType_Unexposed) {
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
            it.children.addAll(children)
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
