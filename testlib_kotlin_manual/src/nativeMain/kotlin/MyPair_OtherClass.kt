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
import kotlinx.cinterop.MemScope
import platform.linux.free
import testlib.TestLib_MyPair__OtherClass__P
import testlib.TestLib_MyPair__OtherClass__P_a_get
import testlib.TestLib_MyPair__OtherClass__P_a_set
import testlib.TestLib_MyPair__OtherClass__P_b_get
import testlib.TestLib_MyPair__OtherClass__P_b_set
import testlib.TestLib_MyPair__OtherClass__P_get_max
import testlib.TestLib_MyPair__OtherClass__P_new

value class MyPair_OtherClass_P constructor(
    val source: Pair<TestLib_MyPair__OtherClass__P, MemScope>
) {
    val ptr: TestLib_MyPair__OtherClass__P
        inline get() = source.first
    val memScope: MemScope
        inline get() = source.second

    var a: OtherClass?
        inline get() {
            val ptr = TestLib_MyPair__OtherClass__P_a_get(ptr) ?: return null
            return OtherClass(ptr to memScope)
        }
        inline set(value) {
            TestLib_MyPair__OtherClass__P_a_set(ptr, value?.ptr)
        }

    var b: OtherClass?
        inline get() {
            val ptr = TestLib_MyPair__OtherClass__P_b_get(ptr) ?: return null
            return OtherClass(ptr to memScope)
        }
        inline set(value) {
            TestLib_MyPair__OtherClass__P_b_set(ptr, value?.ptr)
        }

    inline fun getMax(): OtherClass? {
        val ptr = TestLib_MyPair__OtherClass__P_get_max(ptr) ?: return null
        return OtherClass(ptr to memScope)
    }

    companion object {
        inline fun MemScope.MyPair_OtherClass_P(
            first: OtherClass,
            second: OtherClass
        ): MyPair_OtherClass_P {
            val obj = TestLib_MyPair__OtherClass__P_new(first.ptr, second.ptr)
                ?: error("Failed to create object")
            defer {
                free(obj)
            }
            return MyPair_OtherClass_P(obj to this)
        }
    }
}
