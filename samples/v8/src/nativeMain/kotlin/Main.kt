import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import platform.posix.memset
import v8.Context.Companion.New
import v8.DeserializeInternalFieldsCallback
import v8.DeserializeInternalFieldsCallback.Companion.DeserializeInternalFieldsCallback_Holder
import v8.HandleScope.Companion.HandleScope
import v8.Isolate.Companion.New
import v8.MaybeLocal__ObjectTemplate.Companion.MaybeLocal__ObjectTemplate
import v8.MaybeLocal__Value.Companion.MaybeLocal__Value
import v8.NewStringType
import v8.Script.Companion.Compile
import v8.Value
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
import v8.platform.IdleTaskSupport
import v8.platform.InProcessStackDumping
import v8.platform.NewSingleThreadedDefaultPlatform
import v8.string.Utf8Value.Companion.Utf8Value
import std.Unique_ptr__TracingController
import std.Unique_ptr__TracingController.Companion.Unique_ptr__TracingController_Holder

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

// v8 brick 3 (#99): a `_Holder()` factory only allocates the struct's storage; it does NOT
// run a C++ constructor. For the two by-value structs we pass to v8 below
// (std::unique_ptr<TracingController> and DeserializeInternalFieldsCallback) the safe
// default state is all-zero bits (an empty unique_ptr / a {nullptr, nullptr} callback), and
// the v8 wrapper consumes them with `std::move(*ptr)` / by copy, so zero the freshly
// allocated storage before handing it over.
private fun COpaquePointer.zeroed(size: Int): COpaquePointer {
    memset(this, 0, size.convert())
    return this
}

fun main(args: Array<String>) {
    println("Got args: ${args.size}")
    for (arg in args) {
        println("   $arg")
    }
    memScoped {
        println("Executing kotlin")
        InitializeICUDefaultLocation("", "")
        InitializeExternalStartupData("")
        // v8 brick 3 (#99): the cpp front-end does not materialize ENUM-/CLASS-typed default
        // arguments (only literal ones like `length = -1` survive), so the generated
        // NewSingleThreadedDefaultPlatform requires all three params that C++ defaults. Pass
        // the C++ defaults explicitly: kDisabled / kDisabled / an empty TracingController
        // unique_ptr (zeroed Holder storage — see COpaquePointer.zeroed above).
        val tracingController = Unique_ptr__TracingController_Holder()
        tracingController.ptr.zeroed(Unique_ptr__TracingController.size)
        val platform = NewSingleThreadedDefaultPlatform(
            IdleTaskSupport.kDisabled,
            InProcessStackDumping.kDisabled,
            tracingController
        )
        // The generated NewSingleThreadedDefaultPlatform already `.release()`s the
        // std::unique_ptr<Platform> and returns the raw Platform?, so pass it straight to
        // InitializePlatform(Platform?) — there is no unique_ptr `.get()` wrapper here.
        InitializePlatform(platform)
        Initialize()

        val createParams = CreateParams()
        createParams.array_buffer_allocator = NewDefaultAllocator()
        val isolate = New(createParams)
        memScoped {
            Scope(isolate)
            HandleScope(isolate) { handleScope ->
                // Empty (no-op) internal-fields deserializer — zeroed Holder storage, see above.
                val deserializer = DeserializeInternalFieldsCallback_Holder()
                deserializer.ptr.zeroed(DeserializeInternalFieldsCallback.size)
                val context = New(
                    isolate,
                    null,
                    MaybeLocal__ObjectTemplate(),
                    MaybeLocal__Value(),
                    deserializer,
                    null
                )
                Scope(context)
                memScoped {
                    // v8 brick 3 (#99): `type`'s enum default (NewStringType::kNormal) is not
                    // materialized by the cpp front-end either — pass it explicitly.
                    val source = NewFromUtf8(
                        isolate,
                        "'Hello' + ', Worldy!'",
                        NewStringType.kNormal
                    ).ToLocalChecked()

                    val script = Compile(context, source, null).ToLocalChecked()
                    val result = script.reference()?.Run(context)?.ToLocalChecked()
                    val utf8 = Utf8Value(isolate, result ?: error("No result"))
                    // Generated accessor for operator* is `reference()` (was `_reference()`
                    // under the libclang front-end).
                    println("Got: ${utf8.reference()}")
                }
                memScoped {
                    // v8 brick 2 (#99): a RICHER operation beyond string concat — evaluate an
                    // ARITHMETIC script and read the numeric result back as a 32-bit int via
                    // Value::Int32Value(context), which returns v8::Maybe<int32_t>. This
                    // exercises the Maybe<int>/Int32Value path: the FIR manifest discovers the
                    // method's templated return, and the `instantiate("v8::Maybe<int>")` spec in
                    // build.gradle.kts forces the Maybe<int> model so the method survives the
                    // IGNORE_MISSING drop and FromJust() (which yields the unwrapped Int) binds.
                    val numSource = NewFromUtf8(
                        isolate,
                        "40 + 2",
                        NewStringType.kNormal
                    ).ToLocalChecked()
                    val numScript = Compile(context, numSource, null).ToLocalChecked()
                    val numResult = numScript.reference()?.Run(context)?.ToLocalChecked()
                    // Local<Value>.reference() returns the EMPTY marker interface ValueApi (a
                    // polymorphic base gets only `val ptr`, with the real methods on the concrete
                    // Value class), so the `as? Value` downcast recovers them — reference() does
                    // construct a real Value under the hood, so the cast always succeeds. See the
                    // tracked generator gap (#107): a Local<PolymorphicBase>.reference() should
                    // expose the base's instance methods directly. Int32Value -> Maybe<int>;
                    // FromJust() unwraps the present value (the script always produces a number).
                    val numValue = numResult?.reference() as? Value
                    val computed = numValue?.Int32Value(context)?.FromJust()
                    println("Computed: $computed")
                }
            }
        }
        isolate?.Dispose()
        Dispose()
        DisposePlatform()
    }
}
