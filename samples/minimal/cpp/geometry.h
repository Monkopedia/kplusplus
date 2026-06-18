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
#pragma once

// A deliberately tiny C++ geometry library: plain structs with public fields and
// a few by-value / const-ref methods. No templates, no STL, no stdlib includes —
// exactly the kind of surface kplusplus binds cleanly with zero fixups. This is
// the newcomer sample: read this header, then samples/minimal/src/.../Main.kt to
// see the generated Kotlin API that mirrors it one-to-one.
namespace geo {

// A 2D point. `distanceTo` takes the other point by const reference and returns a
// plain double — both map to ordinary Kotlin value parameters / returns.
struct Point {
    double x;
    double y;

    double distanceTo(const Point& other) const;
};

// An axis-aligned rectangle. `center()` returns a Point BY VALUE — on the Kotlin
// side that result is constructed into the surrounding MemScope (see Main.kt).
struct Rect {
    Point origin;
    double width;
    double height;

    double area() const;
    Point center() const;
};

} // namespace geo
