@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package com.monkopedia.krapper

import com.monkopedia.jni.JNIEnvVar
import com.monkopedia.jni.jobject
import com.monkopedia.krapper.generator.KrapperServiceImpl
import com.monkopedia.ksrpc.channels.registerDefault
import com.monkopedia.ksrpc.jni.ksrpcHostConnection
import kotlin.experimental.ExperimentalNativeApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * JNI entry point that ksrpc-jni's KsrpcNativeHost.connect calls into when this
 * .so is loaded into a JVM. Hosts KrapperServiceImpl as the default ksrpc service
 * on the connection, so JVM consumers get a KrapperService stub via:
 *   KsrpcNativeHost.connect(scope, KrapperNativeHost::initialize)
 *     .defaultChannel().toStub<KrapperService, JniSerialized>()
 *
 * The @CName name encodes the matching JVM-side `object KrapperNativeHost` with
 * `external fun initialize(host: JniHostInit)` in package com.monkopedia.krapper.
 */
@CName("Java_com_monkopedia_krapper_KrapperNativeHost_initialize")
fun initialize(env: CPointer<JNIEnvVar>, clazz: jobject, host: jobject) =
    ksrpcHostConnection(env, host) { conn ->
        conn.registerDefault(KrapperServiceImpl())
    }
