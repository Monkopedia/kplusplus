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

import clang.CXCursor
import clang.CXTypeKind
import com.monkopedia.krapper.generator.ResolveContext
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.ResolvedTemplate
import com.monkopedia.krapper.generator.resolved_model.ResolvedTemplateParam
import com.monkopedia.krapper.generator.resolved_model.ResolvedTypedef
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import com.monkopedia.krapper.generator.usr
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents

data class WrappedTemplate(val name: String) : WrappedElement() {
    val baseClass: WrappedType?
        get() = children.filterIsInstance<WrappedBase>().firstOrNull()?.type
    var metadata: ClassMetadata = ClassMetadata()

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
    var templateArgCounter = 0

    constructor(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        value.spelling.toKString() ?: error("Missing name")
    )

    override fun resolve(resolverContext: ResolveContext): ResolvedTemplate? {
        return ResolvedTemplate(
            name,
            baseClass?.let {
                resolverContext.resolve(it)
                    ?: return resolverContext.notifyFailed(this, it, "Base class")
            },
            metadata.toResolved(),
            qualified,
            templateArgs.mapNotNull { it.resolveTemplateParam(resolverContext) }
        )
    }

    override fun addChild(child: WrappedElement) {
        if (child is WrappedTemplateParam) {
            if (templateArgCounter < templateArgs.size) {
                templateArgs[templateArgCounter++].merge(child)
                return
            }
            templateArgCounter++
        }
        super.addChild(child)
    }

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
            it.metadata = metadata.copy()
        }
    }
}

class WrappedTemplateParam(val name: String, val usr: String) : WrappedElement() {
    val defaultType: WrappedType?
        get() {
            if (children.isEmpty()) return null
            val type = children.find { it is WrappedType } ?: return null
            val template = children.filterIsInstance<WrappedTemplateRef>()
            return WrappedTemplateType(type as WrappedType, template)
        }
    val otherParams = mutableListOf<WrappedTemplateParam>()

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

    override fun toString(): String {
        return "$name${defaultType?.let { " $it" } ?: ""} ($children)"
    }

    override fun resolve(resolverContext: ResolveContext): ResolvedElement? = null

    fun resolveTemplateParam(resolverContext: ResolveContext): ResolvedTemplateParam? {
        return ResolvedTemplateParam(
            name,
            usr,
            defaultType?.let {
                resolverContext.resolve(it)
                    ?: return resolverContext.notifyFailed(this, it, "Default type")
            }
        )
    }

    fun merge(child: WrappedTemplateParam) {
        otherParams.add(child)
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

    override fun toString(): String {
        return "typedef $name = $targetType"
    }

    override fun resolve(resolverContext: ResolveContext): ResolvedElement? {
        return ResolvedTypedef(
            name,
            resolverContext.resolve(targetType) ?: return resolverContext.notifyFailed(
                this,
                targetType,
                "Failed to resolve typedef"
            )
        )
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
