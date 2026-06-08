/*
 * Copyright 2026 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.monkopedia.krapper

import com.monkopedia.ksrpc.jni.JniHostInit

/**
 * JVM-side binding for the @CName entry point declared in krapper_gen
 * nativeMain. The class FQN must match the @CName JNI mangling:
 *   `Java_com_monkopedia_krapper_KrapperNativeHost_initialize`
 * — i.e. top-level `object` in package `com.monkopedia.krapper`.
 */
object KrapperNativeHost {
    external fun initialize(host: JniHostInit)
}
