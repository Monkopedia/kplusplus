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
import geo.Point
import geo.Point.Companion.Point
import geo.Rect
import geo.Rect.Companion.Rect
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals

// The guard for #195: samples/minimal is a from-published consumer, so it only ever
// sees a generated-shape change (e.g. #145/#146's context-parameter migration) at the
// point this sample next resolves a new plugin version -- there is no in-tree signal.
// This test (and Main.kt, which it deliberately mirrors) exercises every accessor kind
// the generator emits for this header: scalar `var` properties (x/y/width/height),
// object-valued get/set pairs that need a MemScope (`origin()` / `_origin(v)`), a
// by-value-returning method that needs a MemScope (`center()`), and a plain const-ref
// method that doesn't (`distanceTo`). If the generated shape drifts, this fails to
// COMPILE (the same way `rect.origin = …` did) before it can fail to assert.
class GeometryTest {
    @Test
    fun rectExposesOriginAsAccessorsNotAProperty() =
        memScoped {
            val rect = Rect()
            rect._origin(Point().apply { x = 1.0; y = 2.0 })

            val origin = rect.origin()!!
            assertEquals(1.0, origin.x)
            assertEquals(2.0, origin.y)
        }

    @Test
    fun rectAreaAndCenterMatchTheGeometry() =
        memScoped {
            val rect = Rect()
            rect._origin(Point().apply { x = 0.0; y = 0.0 })
            rect.width = 2.0
            rect.height = 3.0

            assertEquals(6.0, rect.area())

            val center = rect.center()
            assertEquals(1.0, center.x)
            assertEquals(1.5, center.y)
        }

    @Test
    fun pointDistanceToIsAPlainConstRefMethod() =
        memScoped {
            val a = Point().apply { x = 0.0; y = 0.0 }
            val b = Point().apply { x = 3.0; y = 4.0 }

            assertEquals(5.0, a.distanceTo(b))
        }
}
