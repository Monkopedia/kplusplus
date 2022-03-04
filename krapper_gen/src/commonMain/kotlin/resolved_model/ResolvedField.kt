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

import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedKotlinType

data class ResolvedFieldGetter(
    var uniqueCName: String?,
    var returnStyle: ReturnStyle,
    var returnType: ResolvedCppType,
    val args: List<ResolvedArgument>,
    var needsDereference: Boolean
)

data class ResolvedFieldSetter(
    var uniqueCName: String?,
    var argument: List<ResolvedArgument>
)

data class ResolvedField(
    val name: String,
    var isConst: Boolean,
    var getter: ResolvedFieldGetter,
    var setter: ResolvedFieldSetter,
    var kotlinType: ResolvedKotlinType = (getter.returnType as? ResolvedCppType)?.kotlinType
        ?: error("No type supplied")
) : ResolvedElement() {

    override fun toString(): String {
        return "$name: $getter, $setter"
    }
}
