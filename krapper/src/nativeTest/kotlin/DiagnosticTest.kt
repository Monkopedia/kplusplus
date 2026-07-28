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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.Severity
import com.monkopedia.krapper.SourceLocation
import com.monkopedia.krapper.generator.codegen.CppCompiler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * #185: the two places a `file:line:col` is recovered from text — the `kppbridge::declLocation`
 * spelling stamped onto every parsed element, and the wrapper compile's own diagnostic lines.
 * Both feed [com.monkopedia.krapper.Diagnostic.location], which is what makes a drop or a
 * failed compile actionable on the build side instead of a bare symbol name.
 */
class DiagnosticTest {

    @Test
    fun sourceLocationParsesTheBridgeSpelling() {
        assertEquals(
            SourceLocation("/usr/include/v8-isolate.h", 218, 25),
            SourceLocation.parse("/usr/include/v8-isolate.h:218:25")
        )
    }

    @Test
    fun sourceLocationSplitsFromTheRightSoAColonInThePathSurvives() {
        assertEquals(
            SourceLocation("/w:orkspace/a.h", 7, 3),
            SourceLocation.parse("/w:orkspace/a.h:7:3")
        )
    }

    @Test
    fun sourceLocationRejectsAnythingThatIsNotAPosition() {
        // An invalid/absent location comes back from the bridge as "", and a bare path has no
        // position — neither may become a bogus SourceLocation.
        assertNull(SourceLocation.parse(""))
        assertNull(SourceLocation.parse("/usr/include/v8.h"))
        assertNull(SourceLocation.parse("/usr/include/v8.h:notaline:3"))
    }

    @Test
    fun compilerOutputYieldsOnePositionedDiagnosticPerDiagnosticLine() {
        val diagnostics = CppCompiler.parseCompilerOutput(
            """
            |/tmp/krapped/v8.cc:8297:33: error: static assertion failed: not instantiable
            |    return (void*)&((*thiz_cast = *that_cast));
            |                                 ^
            |/tmp/krapped/v8.cc:24929:6: warning: 'unexpected_handler' is deprecated
            |/usr/include/exception:101:18: note: marked deprecated here
            |8 warnings and 1 error generated.
            """.trimMargin()
        )

        assertEquals(
            listOf(Severity.ERROR, Severity.WARNING, Severity.INFO),
            diagnostics.map { it.severity },
            "the caret art and the summary line are not diagnostics: $diagnostics"
        )
        val error = diagnostics.first()
        assertEquals(SourceLocation("/tmp/krapped/v8.cc", 8297, 33), error.location)
        assertEquals("static assertion failed: not instantiable", error.message)
        // Only the error explains the failure, so only it carries the consumer-facing hint.
        assertNull(diagnostics[1].hint)
    }
}
