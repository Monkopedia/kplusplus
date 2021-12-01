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
import com.monkopedia.krapper.generator.ResolverBuilder
import com.monkopedia.krapper.generator.isStatic
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.referenced
import com.monkopedia.krapper.generator.result
import com.monkopedia.krapper.generator.spelling
import com.monkopedia.krapper.generator.toKString
import com.monkopedia.krapper.generator.type
import kotlinx.cinterop.CValue

enum class MethodType {
    CONSTRUCTOR,
    DESTRUCTOR,
    METHOD,
    STATIC_OP
}

class WrappedConstructor(
    name: String,
    returnType: WrappedType,
) : WrappedMethod(name, returnType, false, MethodType.CONSTRUCTOR) {
    override fun copy(
        name: String,
        returnType: WrappedType,
        isStatic: Boolean,
        methodType: MethodType,
        children: List<WrappedElement>
    ): WrappedMethod {
        return WrappedConstructor(name, returnType).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    override fun clone(): WrappedConstructor {
        return WrappedConstructor(name, returnType).also {
            it.parent = parent
            it.addAllChildren(children)
        }
    }
}

class WrappedDestructor(
    name: String,
    returnType: WrappedType,
) : WrappedMethod(name, returnType, false, MethodType.DESTRUCTOR) {
    override fun copy(
        name: String,
        returnType: WrappedType,
        isStatic: Boolean,
        methodType: MethodType,
        children: List<WrappedElement>
    ): WrappedMethod {
        return WrappedDestructor(name, returnType).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    override fun clone(): WrappedDestructor {
        return WrappedDestructor(name, returnType).also {
            it.parent = parent
            it.addAllChildren(children)
        }
    }
}

open class WrappedMethod(
    val name: String,
    val returnType: WrappedType,
    val isStatic: Boolean,
    val methodType: MethodType = MethodType.METHOD
) : WrappedElement() {
    val args: List<WrappedArgument>
        get() = children.filterIsInstance<WrappedArgument>()

    constructor(method: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        method.referenced.spelling.toKString() ?: error("Can't find name of $method"),
        WrappedType(method.type.result, resolverBuilder),
        method.isStatic,
    )

    open fun copy(
        name: String = this.name,
        returnType: WrappedType = this.returnType,
        isStatic: Boolean = this.isStatic,
        methodType: MethodType = this.methodType,
        children: List<WrappedElement> = this.children.toList()
    ): WrappedMethod {
        return WrappedMethod(name, returnType, isStatic, methodType).also {
            it.addAllChildren(children)
            it.parent = parent
        }
    }

    override fun clone(): WrappedMethod {
        return WrappedMethod(name, returnType, isStatic, methodType).also {
            it.parent = parent
            it.addAllChildren(children)
        }
    }

    override fun toString(): String {
        return "${if (isStatic) "static " else ""}fun $name(${args.joinToString(", ")}): " +
            returnType
    }
}

class WrappedArgument(val name: String, val type: WrappedType) : WrappedElement() {
    constructor(arg: CValue<CXCursor>, resolverBuilder: ResolverBuilder) : this(
        arg.spelling.toKString() ?: error("Can't find name of $arg"),
        WrappedType(arg.type, resolverBuilder)
    )

    override fun clone(): WrappedArgument {
        return WrappedArgument(name, type).also {
            it.parent = parent
            it.addAllChildren(children)
        }
    }

    override fun toString(): String {
        return "$name: $type"
    }
}
