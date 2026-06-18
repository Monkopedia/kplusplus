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
#include "geometry.h"

#include <cmath> // std::sqrt — a libc/libm function, NOT libstdc++ template surface.

namespace geo {

double Point::distanceTo(const Point& other) const {
    const double dx = x - other.x;
    const double dy = y - other.y;
    return std::sqrt(dx * dx + dy * dy);
}

double Rect::area() const {
    return width * height;
}

Point Rect::center() const {
    return Point{origin.x + width / 2, origin.y + height / 2};
}

} // namespace geo
