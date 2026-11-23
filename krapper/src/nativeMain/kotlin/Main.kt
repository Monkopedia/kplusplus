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
import com.github.ajalt.clikt.core.main
import com.monkopedia.krapper.generator.Krapper
import com.monkopedia.krapper.generator.Utils
import kotlin.experimental.ExperimentalNativeApi
import kotlin.system.exitProcess

@OptIn(ExperimentalNativeApi::class)
fun main(args: Array<String>) {
    // #323: an exception nothing caught, on any thread (a generator guard's `check`, a service
    // call's failure), would otherwise abort the process: exit 134 and a core dump, with the
    // message buried in a Kotlin/Native crash report. Report it as one line on stderr, exit 1.
    setUnhandledExceptionHook { t ->
        Utils.printerrln("krapper: fatal: ${t::class.simpleName}: ${t.message}")
        exitProcess(1)
    }
    Krapper().main(args)
}
