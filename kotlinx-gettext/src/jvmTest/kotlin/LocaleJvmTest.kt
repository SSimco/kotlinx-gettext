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

import kotlin.test.Test
import kotlin.test.assertEquals

class LocaleJvmTest {
    @Test
    fun toJavaLocale() {
        assertEquals(java.util.Locale.GERMAN, Locale("de").toJavaLocale())
        assertEquals(java.util.Locale.GERMANY, Locale("de_DE.UTF-8").toJavaLocale())
        assertEquals(java.util.Locale.ROOT, DefaultLocale.toJavaLocale())
    }

    @Test
    fun toGettextLocale() {
        assertEquals(Locale("de-DE"), java.util.Locale.GERMANY.toGettextLocale())
        assertEquals(DefaultLocale, java.util.Locale.ROOT.toGettextLocale())
    }

    @Test
    fun roundTrip() {
        val locale = Locale("sr-Latn-RS")

        assertEquals(locale, locale.toJavaLocale().toGettextLocale())
    }

    @Test
    fun loadWithJavaLocale() {
        assertEquals(Locale("de"), Gettext.load(java.util.Locale.GERMAN, resource("empty.po")).locale)
    }
}
