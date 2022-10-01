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
import kotlinx.cinterop.memScoped
import testlib.std_vector_string
import testlib.std_vector_string_dispose
import testlib.std_vector_string_new
import testlib.std_vector_string_push_back

value class VectorString(val source: Pair<std_vector_string, MemScope>) {
    inline fun push_back(str: String) {
        std_vector_string_push_back(source.first, str)
    }

    companion object {
        inline fun List<String>.asVector(exec: (VectorString) -> Unit) {
            memScoped {
                exec(
                    VectorString().also {
                        for (s in this@asVector) {
                            it.push_back(s)
                        }
                    }
                )
            }
        }
        fun MemScope.VectorString(): VectorString {
            val obj = std_vector_string_new()
            defer {
                std_vector_string_dispose(obj)
            }
            return VectorString((obj ?: error("Creation failed")) to this)
        }
    }
}
