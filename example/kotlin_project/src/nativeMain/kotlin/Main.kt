import kotlinx.cinterop.memScoped
import v8.Context.Companion.New
import v8.DeserializeInternalFieldsCallback.Companion.DeserializeInternalFieldsCallback
import v8.HandleScope.Companion.HandleScope
import v8.Isolate.Companion.New
import v8.MaybeLocal__ObjectTemplate.Companion.MaybeLocal__ObjectTemplate
import v8.MaybeLocal__Value.Companion.MaybeLocal__Value
import v8.Script.Companion.Compile
import v8.String.Companion.NewFromUtf8
import v8.V8.Companion.Dispose
import v8.V8.Companion.DisposePlatform
import v8.V8.Companion.Initialize
import v8.V8.Companion.InitializeExternalStartupData
import v8.V8.Companion.InitializeICUDefaultLocation
import v8.V8.Companion.InitializePlatform
import v8.arrayBuffer.Allocator.Companion.NewDefaultAllocator
import v8.context.Scope.Companion.Scope
import v8.isolate.CreateParams.Companion.CreateParams
import v8.isolate.Scope.Companion.Scope
import v8.platform.NewSingleThreadedDefaultPlatform
import v8.string.Utf8Value.Companion.Utf8Value

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

fun main(args: List<String>): Int {
    println("Got args: ${args.size}")
    for (arg in args) {
        println("   $arg")
    }
    memScoped {
        println("Executing kotlin")
        InitializeICUDefaultLocation("", "")
        InitializeExternalStartupData("")
        val platform = NewSingleThreadedDefaultPlatform()
        InitializePlatform(platform.get())
        Initialize()

        val createParams = CreateParams()
        createParams.array_buffer_allocator = NewDefaultAllocator()
        val isolate = New(createParams)
        memScoped {
            Scope(isolate)
            HandleScope(isolate) { handleScope ->
                val context = New(
                    isolate,
                    null,
                    MaybeLocal__ObjectTemplate(),
                    MaybeLocal__Value(),
                    DeserializeInternalFieldsCallback(),
                    null
                )
                Scope(context)
                memScoped {
                    val source = NewFromUtf8(isolate, "'Hello' + ', Worldy!'").ToLocalChecked()

                    val script = Compile(context, source, null).ToLocalChecked()
                    val result = script.reference()?.Run(context)?.ToLocalChecked()
                    val utf8 = Utf8Value(isolate, result ?: error("No result"))
                    println("Got: ${utf8._reference()}")
                }
            }
        }
        isolate?.Dispose()
        Dispose()
        DisposePlatform()
    }
    return 0
}
