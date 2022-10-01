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
import com.monkopedia.klinker.klinkedExecutable
import com.monkopedia.kplusplus.find
import com.monkopedia.kplusplus.onEach
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.resolved_model.ArgumentCastMode.REINT_CAST
import com.monkopedia.krapper.generator.resolved_model.MethodType.METHOD
import com.monkopedia.krapper.generator.resolved_model.ResolvedArgument
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle
import com.monkopedia.krapper.generator.resolved_model.ReturnStyle.VOIDP
import com.monkopedia.krapper.generator.resolved_model.type.ResolvedCType

buildscript {
    repositories {
        mavenCentral()
        jcenter()
        mavenLocal()
    }
    dependencies {
        classpath("com.monkopedia.klinker:plugin:0.1.0")
    }
}
plugins {
    kotlin("multiplatform") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.monkopedia.klinker.plugin") version "0.1.0"
    id("com.monkopedia.kplusplus.plugin")
}

repositories {
    mavenLocal()
    jcenter()
}

kotlin {
    // Determine host preset.
    val hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs.startsWith("Windows") -> mingwX64("native")
        else -> throw GradleException(
            "Host OS '$hostOs' is not supported in Kotlin/Native $project."
        )
    }

    hostTarget.apply {
        binaries {
            klinkedExecutable {
                compilerOpts("-g")
                runTask {
                }
            }
        }
    }
    sourceSets["nativeMain"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-native-mt")
    }
}

kplusplus {
    config {
        referencePolicy = ReferencePolicy.INCLUDE_MISSING
    }
    import {
        library.srcDir("../")
        library.include("libv8_monolith.a")
        headers.srcDir("../include/")
        headers.include("v8-combined.h")

        map(ResolvedMethod) {
            find {
                parent(qualified eq "v8::ScriptOrigin") and
                    (methodName eq "options")
            }
            onEach { element ->
                println("Setting return type on $element")
                val retType = element.returnType.copy(
                    typeString = element.returnType.typeString.removePrefix("const ").trimEnd('*')
                )
                element.replaceWith(
                    element.copy(
                        returnStyle = ReturnStyle.COPY_CONSTRUCTOR,
                        returnType = retType
                    )
                )
            }
        }
        map(ResolvedMethod) {
            find {
                (methodReturnType startsWith "const v8::Local<") or
                    (methodReturnType startsWith "const v8::Maybe<") or
                    (methodReturnType startsWith "const v8::MaybeLocal<") or
                    (methodReturnType startsWith "const v8::ScriptOrigin<") or
                    (methodReturnType startsWith "const v8::Location<")
            }
            onEach { element ->
                println("Clearing const return type on $element")
                val nonConstReturn = element.returnType.copy(
                    typeString = element.returnType.typeString.removePrefix("const ")
                )
                element.replaceWith(element.copy(returnType = nonConstReturn))
            }
        }
        map(ResolvedMethod) {
            find {
                parent(qualified eq "v8::Persistent<v8::Value>") or
                    parent(qualified eq "v8::platform::tracing::TraceWriter")
            }
            onEach { element ->
                if (element.uniqueCName == "_v8_Persistent_v8_Value_new" ||
                    element.uniqueCName == "v8_Persistent_v8_Value_op_assign" ||
                    element.uniqueCName == "v8_platform_tracing_TraceWriter_create_system_instrumentation_trace_writer"
                ) {
                    println("Removing $element")
                    element.remove()
                }
            }
        }
        map(ResolvedClass) {
            find {
                (thiz isType ResolvedClass) and
                    (qualified startsWith "std::unique_ptr") and
                    (className eq "unique_ptr")
            }
            onEach { parent ->
                val baseType = parent.type.typeString.replace(
                    "std::unique_ptr<",
                    ""
                ).removeSuffix(">")
                val thisPtrType = parent.type.copy(
                    typeString = parent.type.type + "*",
                    cType = ResolvedCType("void*", false)
                )
                val thizArgument = ResolvedArgument(
                    "thiz",
                    thisPtrType,
                    thisPtrType,
                    "",
                    REINT_CAST,
                    needsDereference = true,
                    hasDefault = false
                )
                val uniqueCName = "_custom_unique_ptr_get_" +
                    parent.type.typeString.replace("<", "_").replace(">", "_")
                        .replace("::", "_")
                val returnType = parent.type.copy(
                    typeString = baseType,
                    kotlinType = resolvedKotlinType(baseType),
                    cType = resolvedCType(baseType)
                )
                val getMethod = ResolvedMethod(
                    name = "get",
                    returnType = returnType,
                    methodType = METHOD,
                    uniqueCName = uniqueCName,
                    operator = null,
                    args = listOf(thizArgument),
                    returnStyle = VOIDP,
                    argCastNeedsPointer = false,
                    qualified = parent.type.typeString
                )
                println("Adding $getMethod to $parent")
                parent.add(getMethod)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xskip-prerelease-check"
        freeCompilerArgs = freeCompilerArgs + "-Xno-param-assertions"
    }
}
