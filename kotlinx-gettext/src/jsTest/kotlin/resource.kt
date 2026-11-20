/*
 * Copyright 2022 Victor Kropp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.kropp.kotlinx.gettext

import org.khronos.webgl.Int8Array

external val process: dynamic

external fun require(module: String): dynamic

// The Gradle build sets this variable to the commonTest resources directory.
private const val RESOURCES_VARIABLE = "KOTLINX_GETTEXT_TEST_RESOURCES"

actual fun resource(name: String): ByteArray {
    val directory = process.env[RESOURCES_VARIABLE] as String?
        ?: error("$RESOURCES_VARIABLE is not set; run the tests through Gradle")
    val buffer = require("fs").readFileSync(require("path").join(directory, name))
    // Node returns a Buffer, which is a Uint8Array; Kotlin/JS represents ByteArray as an Int8Array.
    return Int8Array(buffer.buffer, buffer.byteOffset, buffer.length).unsafeCast<ByteArray>()
}