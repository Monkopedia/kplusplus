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
package com.monkopedia.krapper.generator.builders

data class PropertyBuilder(
    var getter: Symbol? = null,
    var setter: Symbol? = null
)

inline fun KotlinCodeBuilder.property(varDecl: LocalVar, builder: PropertyBuilder.() -> Unit) {
    val props = PropertyBuilder().apply(builder)
    (varDecl as? KotlinLocalVar)?.isVal = props.setter == null
    addSymbol(Property(varDecl, props.getter, props.setter))
}

class Property(
    private val varDecl: LocalVar,
    private val getter: Symbol?,
    private val setter: Symbol?
) : Symbol, SymbolContainer {
    override val symbols: List<Symbol>
        get() = listOfNotNull(varDecl, getter, setter)

    override fun build(builder: CodeStringBuilder) {
        varDecl.build(builder)
        builder.block {
            append('\n')
            getter?.let {
                it.build(this)
                append('\n')
            }
            setter?.let {
                it.build(this)
                append('\n')
            }
        }
    }

    override fun toString(): String {
        return "Prop:$varDecl $getter $setter"
    }
}
