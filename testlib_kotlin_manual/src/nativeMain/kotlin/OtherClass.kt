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
import VectorString.Companion.asVector
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import testlib.TestLib_MyPair__OtherClass__P
import testlib.TestLib_OtherClass
import testlib.TestLib_OtherClass_appendText
import testlib.TestLib_OtherClass_copies
import testlib.TestLib_OtherClass_dispose
import testlib.TestLib_OtherClass_getPrivateString
import testlib.TestLib_OtherClass_ints
import testlib.TestLib_OtherClass_new
import testlib.TestLib_OtherClass_setPrivateString

value class OtherClass constructor(val source: Pair<TestLib_OtherClass, MemScope>) {
    val ptr: TestLib_MyPair__OtherClass__P
        inline get() = source.first
    val memScope: MemScope
        inline get() = source.second

    var privateString: String?
        get() = TestLib_OtherClass_getPrivateString(source.first)?.toKString()
        set(value) {
            TestLib_OtherClass_setPrivateString(source.first, value)
        }

    inline fun appendText(text: List<String>) {
        text.asVector { vector ->
            TestLib_OtherClass_appendText(source.first, vector.source.first)
        }
    }

    inline fun copies(): MyPair_OtherClass_P? {
        val ptr = TestLib_OtherClass_copies(ptr) ?: return null
        return MyPair_OtherClass_P(ptr to memScope)
    }

    inline fun ints(): MyPair_int? {
        val ptr = TestLib_OtherClass_ints(ptr) ?: return null
        return MyPair_int(ptr to memScope)
    }

    companion object {
        fun MemScope.OtherClass(): OtherClass {
            val obj = TestLib_OtherClass_new()
            defer {
                TestLib_OtherClass_dispose(obj)
            }
            return OtherClass((obj ?: error("Creation failed")) to this)
        }
    }
}
