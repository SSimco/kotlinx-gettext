/*
 * Copyright 2026 SSimco
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

package io.github.ssimco.kotlinx.gettext

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageFormatTest {
    private val previousDefault = java.util.Locale.getDefault()

    @BeforeTest
    fun setDefaultLocale() {
        // A default that differs from the catalog locale shows which locale formats the numbers.
        java.util.Locale.setDefault(java.util.Locale.US)
    }

    @AfterTest
    fun restoreDefaultLocale() {
        java.util.Locale.setDefault(previousDefault)
    }

    private val i18n = Gettext.load(Locale("de"), resource("empty.po"))

    @Test
    fun tr() {
        assertEquals("1.234,5 Dateien", i18n.tr("{0,number} Dateien", 1234.5))
    }

    @Test
    fun trn() {
        assertEquals("1.234,5 Dateien", i18n.trn("{0,number} Datei", "{0,number} Dateien", 2, 1234.5))
    }

    @Test
    fun trc() {
        assertEquals("1.234,5 Dateien", i18n.trc("ctx", "{0,number} Dateien", 1234.5))
    }

    @Test
    fun trnc() {
        assertEquals("1.234,5 Dateien", i18n.trnc("ctx", "{0,number} Datei", "{0,number} Dateien", 2, 1234.5))
    }

    @Test
    fun rootLocale() {
        java.util.Locale.setDefault(java.util.Locale.GERMANY)

        assertEquals("1,234.5", Gettext.Fallback.tr("{0,number}", 1234.5))
    }
}
