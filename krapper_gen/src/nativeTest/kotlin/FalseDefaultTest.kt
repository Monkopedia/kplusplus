/*
 * Copyright 2022 Jason Monk
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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.const
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.referenceTo
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Fix #1: a by-value param whose type is a template instantiation carries a FALSE
 * default — the [WrappedArgument.hasDefault] cursor heuristic misreads the non-type
 * template argument's literal (e.g. `Mask<4>`'s `4`) as a C++ default value — only
 * when at least one template argument is a VALUE (non-type) argument. A template over
 * only TYPE args (`shared_ptr<X>`, buildASTFromCode's genuinely-defaulted trailing
 * param) keeps a real default and must STAY omittable.
 *
 * These assert on [WrappedArgument.isOmittableDefault], the gate that decides whether
 * the generated C++ call may drop the param. A wrongly-omittable required param emits a
 * short, non-compiling call. The OLD numeric-only check (`toDoubleOrNull`) regressed the
 * bool / char / hex value-arg forms (treating them as omittable); these pin the fix.
 */
class FalseDefaultTest {
    // hasDefault=true simulates the cursor heuristic firing (it does, for the value
    // literal inside the template arg). A VALUE template arg => false default => the
    // param is NOT omittable (the whole method drops rather than emit a short call).
    private fun arg(type: WrappedType) = WrappedArgument("p", type, hasDefault = true)

    private fun template(base: String, vararg args: String) =
        WrappedTemplateType(WrappedType(base), args.map { WrappedType(it) })

    // The STRUCTURAL signal that actually fires end-to-end: a non-type template arg is
    // CXType_Invalid -> the type builder maps it to UNRESOLVABLE, so `Mask<4>` /
    // `Flags<true>` surface as `Mask<unresolveable>` regardless of the literal's kind.
    @Test fun unresolveable_value_arg_is_not_omittable() {
        assertFalse(arg(template("Mask", "unresolveable")).isOmittableDefault)
        assertFalse(arg(template("Flags", "unresolveable")).isOmittableDefault)
    }

    @Test fun numeric_value_arg_is_not_omittable() {
        assertFalse(arg(template("Mask", "4")).isOmittableDefault)
    }

    @Test fun bool_value_arg_is_not_omittable() {
        assertFalse(arg(template("Flags", "true")).isOmittableDefault)
        assertFalse(arg(template("Flags", "false")).isOmittableDefault)
    }

    @Test fun char_value_arg_is_not_omittable() {
        assertFalse(arg(template("Sep", "'x'")).isOmittableDefault)
    }

    @Test fun hex_value_arg_is_not_omittable() {
        // `0x4` is rejected by toDoubleOrNull (the old check) -> wrongly omittable.
        assertFalse(arg(template("Mask", "0x4")).isOmittableDefault)
    }

    @Test fun suffixed_numeric_value_arg_is_not_omittable() {
        assertFalse(arg(template("Mask", "4u")).isOmittableDefault)
        assertFalse(arg(template("Mask", "0x4ULL")).isOmittableDefault)
    }

    // The reference case to KEEP working: all TYPE args => the default is REAL => the
    // param stays omittable (the C++ call legitimately drops it).
    @Test fun type_arg_template_stays_omittable() {
        assertTrue(
            arg(template("std::shared_ptr", "clang::PCHContainerOperations"))
                .isOmittableDefault
        )
        assertTrue(arg(template("Wrap", "int")).isOmittableDefault)
    }

    // A NON-CONST lvalue reference is an output/in-out param (CollectInheritedProtocols's
    // `SmallPtrSetImpl<ObjCProtocolDecl*>&`): a misread hasDefault must NOT trim it, even
    // when the referent parsed as a bare string (no structured template args to catch).
    @Test fun nonconst_output_reference_is_not_omittable() {
        assertFalse(
            arg(referenceTo(WrappedType("SmallPtrSetImpl<ObjCProtocolDecl*>"))).isOmittableDefault
        )
        assertFalse(arg(referenceTo(template("SmallPtrSetImpl", "X"))).isOmittableDefault)
    }

    // A CONST reference CAN carry a genuine, omittable default —
    // `std::basic_string(const CharT*, const Alloc& = Alloc())`'s trailing allocator param
    // is `const Alloc&`. A blanket `isReference -> non-omittable` wrongly made it required,
    // dropping the const-char*/char/initializer_list string constructors.
    @Test fun const_reference_default_stays_omittable() {
        assertTrue(arg(referenceTo(const(WrappedType("Alloc")))).isOmittableDefault)
        assertTrue(arg(referenceTo(const(WrappedType("std::allocator<char>")))).isOmittableDefault)
    }
}
