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

// #47 flip brick B2 — the GENERALITY-DEMO surface. A deliberately std-free, single-header
// C++ struct so the cpp front-end (:krapper_parse) parses it cleanly: it proves the generic
// `-Pkpp.frontend.<module>=cpp` path drives a module's OWN kplusplus{} config (NOT just
// featuregen's std-heavy strings_feature.h). Mirrors the proven `geo::Vec` shape (NsSingle):
// default + args ctor, fields, a const method, a by-value method, a static factory.
struct Point2D {
    double x;
    double y;

    Point2D() : x(0.0), y(0.0) {}
    Point2D(double x, double y) : x(x), y(y) {}

    double lengthSq() const { return x * x + y * y; }
    Point2D translated(double dx, double dy) const { return Point2D(x + dx, y + dy); }
    static Point2D make(double x, double y) { return Point2D(x, y); }
};
