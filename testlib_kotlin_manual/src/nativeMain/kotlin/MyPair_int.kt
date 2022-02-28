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
import kotlinx.cinterop.MemScope
import platform.linux.free
import testlib.TestLib_MyPair__int
import testlib.TestLib_MyPair__int_a_get
import testlib.TestLib_MyPair__int_a_set
import testlib.TestLib_MyPair__int_b_get
import testlib.TestLib_MyPair__int_b_set
import testlib.TestLib_MyPair__int_get_max
import testlib.TestLib_MyPair__int_new

value class MyPair_int public constructor(
    val source: Pair<TestLib_MyPair__int, MemScope>
) {
    val ptr: TestLib_MyPair__int
        inline get() = source.first
    val memScope: MemScope
        inline get() = source.second

    var a: Int
        inline get() {
            return TestLib_MyPair__int_a_get(ptr)
        }
        inline set(value) {
            TestLib_MyPair__int_a_set(ptr, value)
        }

    var b: Int
        inline get() {
            return TestLib_MyPair__int_b_get(ptr)
        }
        inline set(value) {
            TestLib_MyPair__int_b_set(ptr, value)
        }

    inline fun getMax(): Int {
        return TestLib_MyPair__int_get_max(ptr)
    }

    companion object {
        inline fun MemScope.MyPair_int(first: Int, second: Int): MyPair_int {
            val obj = TestLib_MyPair__int_new(first, second)
                ?: error("Failed to create object")
            defer {
                free(obj)
            }
            return MyPair_int(obj to this)
        }
    }
}
