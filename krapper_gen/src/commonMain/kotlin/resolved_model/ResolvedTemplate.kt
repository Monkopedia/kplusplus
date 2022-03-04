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
package com.monkopedia.krapper.generator.resolved_model

import com.monkopedia.krapper.generator.resolved_model.type.ResolvedType

data class ResolvedTemplate(
    val name: String,
    val baseClass: ResolvedType?,
    var hasConstructor: Boolean = false,
    var hasHiddenNew: Boolean = false,
    var hasHiddenDelete: Boolean = false,
    val qualified: String,
    val templateArgs: List<ResolvedTemplateParam>,
) : ResolvedElement() {

    val fields: List<ResolvedField>
        get() = children.filterIsInstance<ResolvedField>()
    val methods: List<ResolvedMethod>
        get() = children.filterIsInstance<ResolvedMethod>()

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
}

data class ResolvedTemplateParam(
    var name: String,
    var usr: String,
    var defaultType: ResolvedType?
) {

    override fun toString(): String {
        return "$name${defaultType?.let { " $it" } ?: ""}"
    }
}

data class ResolvedTypedef(val name: String, val targetType: ResolvedType) : ResolvedElement() {
    override fun toString(): String {
        return "typedef $name = $targetType"
    }
}
