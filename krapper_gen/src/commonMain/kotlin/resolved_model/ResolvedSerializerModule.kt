package com.monkopedia.krapper.generator.resolved_model

import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCppType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedType
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val resolvedSerializerModule = SerializersModule {
    polymorphic(ResolvedElement::class) {
        subclass(ResolvedTU::class)
        subclass(ResolvedNamespace::class)
        subclass(ResolvedClass::class)
        subclass(ResolvedTemplate::class)
        subclass(ResolvedTypedef::class)
        subclass(ResolvedConstructor::class)
        subclass(ResolvedDestructor::class)
        subclass(ResolvedMethod::class)
        subclass(ResolvedField::class)
        subclass(ResolvedType::class)
        subclass(ResolvedCType::class)
        subclass(ResolvedCppType::class)
        subclass(ResolvedKotlinType::class)
    }
}