package com.monkopedia.krapper

import com.monkopedia.ksrpc.jni.JniHostInit

/**
 * JVM-side binding for the @CName entry in libkrapper.so. Class FQN must match
 * the JNI name mangling: `Java_com_monkopedia_krapper_KrapperNativeHost_initialize`.
 */
internal object KrapperNativeHost {
    external fun initialize(host: JniHostInit)
}
