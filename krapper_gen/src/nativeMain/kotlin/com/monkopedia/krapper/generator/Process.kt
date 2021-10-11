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
package com.monkopedia.krapper.generator

import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.F_SETFL
import platform.posix.O_NONBLOCK
import platform.posix.SIGKILL
import platform.posix.STDERR_FILENO
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.__pid_t
import platform.posix.close
import platform.posix.dup2
import platform.posix.exit
import platform.posix.fcntl
import platform.posix.fork
import platform.posix.kill
import platform.posix.pipe
import platform.posix.waitpid

class Process(private val otherProcess: () -> Unit) {

    val inPipe = allocPipe()
    val outPipe = allocPipe()

    private fun allocPipe(): Pair<Int, Int> = memScoped {
        val holder = allocArray<IntVar>(2)
        pipe(holder)
        fcntl(holder[0], F_SETFL, O_NONBLOCK)

        holder[0] to holder[1]
    }

    private var pid: __pid_t = 0

    fun start() {
        pid = fork()
        if (pid == 0) {
            dup2(outPipe.first, STDIN_FILENO)
            dup2(inPipe.second, STDOUT_FILENO)
            dup2(inPipe.second, STDERR_FILENO)

            otherProcess()
            close(inPipe.second)
            exit(0)
        }
    }

    fun wait() = memScoped {
        val status = alloc<IntVar>()
        waitpid(pid, status.ptr, 0)
        status.value
    }

    fun kill(): Int {
        require(pid != 0) {
            "Process has not been started"
        }
        kill(pid, SIGKILL)
        return wait()
    }

    fun stdIn() = outPipe.second
    fun stdOut() = inPipe.first
}
