/*
 * Copyright 2026 Jason Monk
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
package com.monkopedia.krapper.generator.resolvedmodel

// These two enums are the only resolved-schema types carried as DATA on the parse-output
// model itself (WrappedMethod.methodType / WrappedConstructor.allocationStyle), so they
// live with the model module; the rest of the resolved schema stays in :krapper_gen.

enum class MethodType {
    CONSTRUCTOR,
    DESTRUCTOR,
    METHOD,
    STATIC_OP,
    STATIC,
    SIZE_OF,
    ALIGN_OF
}

enum class AllocationStyle {
    DIRECT,
    STACK
}
