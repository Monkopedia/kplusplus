package com.monkopedia.krapper.generator

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.posix.ICANON
import platform.posix.TCSANOW
import platform.posix.memcpy
import platform.posix.tcgetattr
import platform.posix.tcsetattr
import platform.posix.termios

@OptIn(ExperimentalContracts::class)
inline fun withoutIcanon(callback: () -> Unit) {
    contract {
        callsInPlace(callback, EXACTLY_ONCE)
    }
    memScoped {
        val current = alloc<termios>()
        val old = alloc<termios>()
        initTermios(current.ptr, old.ptr)
        callback()
        resetTermios(old.ptr)
    }
}

fun initTermios(current: CPointer<termios>, old: CPointer<termios>) {
    tcgetattr(0, old)
    memcpy(current, old, sizeOf<termios>().toULong())
    current.pointed.c_lflag = current.pointed.c_lflag and ICANON.inv().toUInt()
    tcsetattr(0, TCSANOW, current)
}

fun resetTermios(old: CPointer<termios>) {
    tcsetattr(0, TCSANOW, old)
}
