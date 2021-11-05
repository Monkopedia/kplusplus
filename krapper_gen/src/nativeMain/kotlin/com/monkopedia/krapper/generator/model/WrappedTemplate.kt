package com.monkopedia.krapper.generator.model

import clang.CXCursor
import clang.CXCursorKind
import clang.CX_CXXAccessSpecifier
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.accessSpecifier
import com.monkopedia.krapper.generator.filterChildrenRecursive
import com.monkopedia.krapper.generator.fullyQualified
import com.monkopedia.krapper.generator.getArgument
import com.monkopedia.krapper.generator.isStatic
import com.monkopedia.krapper.generator.kind
import com.monkopedia.krapper.generator.numArguments
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.result
import com.monkopedia.krapper.generator.semanticParent
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import kotlinx.cinterop.CValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class WrappedTemplate(
    val fullyQualifiedTemplate: String,
    var baseClass: WrappedType? = null,
    val fields: List<WrappedTemplateField> = emptyList(),
    val methods: List<WrappedTemplateMethod> = emptyList(),
    val templateTypedefs: List<WrappedTemplateType>,
    val templateArgs: List<WrappedTemplateType>
) {

    val type: WrappedType
        get() = WrappedTemplatedReference(fullyQualifiedTemplate, templateArgs)

    constructor(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        value.fullyQualified,
        value.filterChildrenRecursive {
            it.kind == CXCursorKind.CXCursor_CXXBaseSpecifier ||
                it.kind == CXCursorKind.CXCursor_CXXMethod ||
                it.kind == CXCursorKind.CXCursor_Constructor ||
                it.kind == CXCursorKind.CXCursor_Destructor ||
                it.kind == CXCursorKind.CXCursor_FieldDecl ||
                (it.semanticParent == value &&
                        it.kind == CXCursorKind.CXCursor_TemplateTypeParameter)
        },
        resolverBuilder
    )

    constructor(name: String, children: List<CValue<CXCursor>>, resolverBuilder: ResolverBuilder) :
        this(
            name,
            children.findTemplateBaseClass(resolverBuilder),
            children.findTemplateFields(resolverBuilder),
            children.findTemplateMethods(resolverBuilder),
            children.findTemplateArguments(),
            children.findTemplateArguments()
        )

    inline fun typedWith(
        resolverBuilder: ResolverBuilder,
        vararg values: Pair<WrappedTemplateType, WrappedTypeReference>
    ): WrappedClass = typedWith(resolverBuilder, values.toMap())

    fun typedWith(
        resolverBuilder: ResolverBuilder,
        values: Map<WrappedTemplateType, WrappedTypeReference>
    ): WrappedClass {
        for (t in templateArgs) {
            require(t in values.keys) {
                "Missing parameter $t in instance"
            }
        }
        return WrappedClass(
            type.typedWith(resolverBuilder, values).name,
            baseClass?.typedWith(resolverBuilder, values),
            fields.map { it.typedWith(resolverBuilder, values) },
            methods.map { it.typedWith(resolverBuilder, values) }
        )
    }

    override fun toString(): String {
        return buildString {
            append("class $fullyQualifiedTemplate<${templateArgs.joinToString(", ")}> {\n")
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
}

private fun List<CValue<CXCursor>>.findTemplateArguments(): List<WrappedTemplateType> {
    return filter {
        it.kind == CXCursorKind.CXCursor_TemplateTypeParameter
    }.map {
        WrappedTemplateType(it.spelling.toKString() ?: error("Can't find name"))
    }
}

private fun List<CValue<CXCursor>>.findTemplateMethods(
    resolverBuilder: ResolverBuilder
): List<WrappedTemplateMethod> {
    return filter {
        (
            it.kind == CXCursorKind.CXCursor_CXXMethod ||
                it.kind == CXCursorKind.CXCursor_Constructor ||
                it.kind == CXCursorKind.CXCursor_Destructor
            ) &&
            it.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPublic
    }.map {
        WrappedTemplateMethod(it, resolverBuilder)
    }
}

private fun List<CValue<CXCursor>>.findTemplateFields(
    resolverBuilder: ResolverBuilder
): List<WrappedTemplateField> {
    return filter {
        it.kind == CXCursorKind.CXCursor_FieldDecl &&
            it.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPublic
    }.map {
        WrappedTemplateField(it, resolverBuilder)
    }
}

private fun List<CValue<CXCursor>>.findTemplateBaseClass(
    resolverBuilder: ResolverBuilder
): WrappedType? {
    return find {
        it.kind == CXCursorKind.CXCursor_CXXBaseSpecifier
    }?.let {
        WrappedTemplateType(it.type, resolverBuilder)
    }
}

@Serializable
data class WrappedTemplateMethod(
    val name: String,
    val returnType: WrappedType,
    val args: List<WrappedTemplateArgument>,
    val isStatic: Boolean,
    val methodType: MethodType
) {
    constructor(method: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        method.referenced.spelling.toKString() ?: error("Can't find name of $method"),
        WrappedTemplateType(method.type.result, resolverBuilder),
        (0 until method.numArguments).map {
            WrappedTemplateArgument(method.getArgument(it.toUInt()), resolverBuilder)
        },
        method.isStatic,
        when (method.kind) {
            CXCursorKind.CXCursor_Constructor -> MethodType.CONSTRUCTOR
            CXCursorKind.CXCursor_Destructor -> MethodType.DESTRUCTOR
            else -> MethodType.METHOD
        }
    )

    fun typedWith(
        resolverBuilder: ResolverBuilder,
        values: Map<WrappedTemplateType, WrappedTypeReference>
    ): WrappedMethod {
        return WrappedMethod(
            name,
            returnType.typedWith(resolverBuilder, values),
            args.map { it.typedWith(resolverBuilder, values) },
            isStatic,
            methodType
        )
    }

    override fun toString(): String {
        return "${if (isStatic) "static " else ""}fun $name(${args.joinToString(", ")}): " +
            returnType
    }
}

@Serializable
data class WrappedTemplateArgument(val name: String, val type: WrappedType) {
    constructor(arg: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        arg.spelling.toKString() ?: error("Can't find name of $arg"),
        WrappedTemplateType(arg.type, resolverBuilder)
    )

    fun typedWith(
        resolverBuilder: ResolverBuilder,
        values: Map<WrappedTemplateType, WrappedTypeReference>
    ): WrappedArgument {
        return WrappedArgument(name, type.typedWith(resolverBuilder, values))
    }

    override fun toString(): String {
        return "$name: $type"
    }
}

@Serializable
data class WrappedTemplateField(
    val name: String,
    val type: WrappedType
) {
    @Transient
    internal val other = Any()

    constructor(field: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        field.referenced.spelling.toKString() ?: error("Can't find name for $field"),
        WrappedTemplateType(field.type, resolverBuilder)
    )

    override fun toString(): String {
        return "$name: $type"
    }

    fun typedWith(
        resolverBuilder: ResolverBuilder,
        values: Map<WrappedTemplateType, WrappedTypeReference>
    ): WrappedField {
        return WrappedField(name, type.typedWith(resolverBuilder, values))
    }
}
