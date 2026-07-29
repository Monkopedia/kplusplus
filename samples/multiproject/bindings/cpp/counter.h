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

// The whole C++ surface of the multi-project canary: header-only (every method is defined
// inline, so there is no library to build or link) and std-free, so a sync here exercises the
// PLUGIN, not the binding generator. Deliberately boring — what is under test is whether the
// plugin can talk to krapper at all from a subproject's classloader (#194).
struct Counter {
    int value;

    Counter() : value(0) {}
    explicit Counter(int start) : value(start) {}

    int next() { return ++value; }
    int peek() const { return value; }
};
