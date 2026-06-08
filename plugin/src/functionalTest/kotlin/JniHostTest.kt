/*
 * Copyright 2026 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.monkopedia.kplusplus.plugin

import com.monkopedia.krapper.KrapperNativeHost
import com.monkopedia.krapper.KrapperService
import com.monkopedia.ksrpc.jni.JniSerialized
import com.monkopedia.ksrpc.jni.KsrpcNativeHost
import com.monkopedia.ksrpc.toStub
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * Smoke test for the ksrpc-jni host bridge (#209): load libkrapper.so into the JVM,
 * connect via KsrpcNativeHost, get a KrapperService stub over JNI, and round-trip
 * a ping. Proves the bridge works end-to-end before the compiler-plugin
 * integration.
 */
class JniHostTest {

    @Test
    fun pingOverJni(): Unit = runBlocking {
        val libPath = File(
            "../krapper_gen/build/bin/native/krapperDebugShared/libkrapper.so"
        ).absoluteFile
        assertTrue(libPath.exists(), "Expected libkrapper.so at $libPath")
        System.load(libPath.path)

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val connection = KsrpcNativeHost.connect(scope, KrapperNativeHost::initialize)
            try {
                val service = connection.defaultChannel()
                    .toStub<KrapperService, JniSerialized>()
                val pong = withContext(Dispatchers.IO) { service.ping("JNI hello") }
                println("ping pong via JNI: $pong")
                assertTrue(pong.contains("JNI hello"), "ping echoed message; got '$pong'")
            } finally {
                connection.close()
            }
        } finally {
            scope.cancel()
        }
    }
}
