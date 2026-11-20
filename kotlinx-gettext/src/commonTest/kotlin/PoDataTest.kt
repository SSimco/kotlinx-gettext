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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PoDataTest {
    @Test
    fun test() {
        val po = PoData.read(resource("en.po"))

        assertEquals("Update available", po["Update available"])
        assertEquals("{0,number,integer} updates available", po["Update available", 0])
        assertEquals("Update available", po["Update available", 1])
        assertEquals("{0,number,integer} updates available", po["Update available", 2])

        assertEquals("{0} doesn''t exist", po["{0} doesn't exist"])
        assertEquals(null, po["{0} doesn't exist", 1])

        assertEquals(null, po["nonexistent key"])
    }

    @Test
    fun multilineKey() {
        val po = PoData.read(resource("de.po"))

        assertEquals("Sehr sehr lange\n\nBeschreibung", po["Very very long\n\ndescription"])
    }

    @Test
    fun quotedStrings() {
        val po = PoData.read(resource("quoted.po"))

        assertEquals("Hallo \"Welt\"!", po["Hello \"World\"!"])
    }

    @Test
    fun quotedMultilineStrings() {
        val po = PoData.read(resource("quoted-multiline.po"))

        assertEquals("Linia unu.\nLinia doi cu \"ghilimele\".", po["Line one.\nLine two with \"quotes\"."])
    }

    @Test
    fun quotedMultilineStrings2() {
        val po = PoData.read(resource("quoted-multiline-2.po"))

        assertEquals(
            "Wähle jede Nacht 1 Spieler & 1 guten Charakter: Der Spieler ist morgen \"besessen\" davon, dass er der Charakter ist, oder könnte hingerichtet werden.",
            po["Each night, choose a player & a good character: they are \"mad\" they are this character tomorrow, or might be executed."]
        )
    }

    @Test
    fun excessWhitespace() {
        val po = PoData.read(resource("whitespace.po"))

        assertEquals("Hallo Welt!", po["Hello World!"])
    }

    @Test
    fun ignoreEmptyString() {
        val po = PoData.read(resource("empty.po"))

        assertNull(po["empty"])
    }

    @Test
    fun ignoreEmptyPlural() {
        val po = PoData.read(resource("empty-plural.po"))

        assertNull(po["Update available", 1])
        assertNull(po["Update available", 2])
    }

    @Test
    fun someEmptyPlural() {
        val po = PoData.read(resource("some-plural.po"))

        assertEquals("Aktualisierung verfügbar", po["Update available", 1])
        assertNull(po["Update available", 2])
    }

    @Test
    fun invalidPluralRule() {
        val po = PoData.read(resource("invalid-plural.po"))

        // An invalid rule falls back to the GNU default, n != 1.
        assertEquals("Aktualisierung verfügbar", po["Update available", 1])
        assertEquals("{{count}} Aktualisierungen verfügbar", po["Update available", 2])
    }

    @Test
    fun missingPluralRule() {
        val po = PoData.read(resource("no-plural-forms.po"))

        // A missing rule falls back to the GNU default, n != 1.
        assertEquals("{{count}} Aktualisierungen verfügbar", po["Update available", 0])
        assertEquals("Aktualisierung verfügbar", po["Update available", 1])
        assertEquals("{{count}} Aktualisierungen verfügbar", po["Update available", 2])
    }

    @Test
    fun pluralEntryWithoutCount() {
        assertEquals("Aktualisierung verfügbar", PoData.read(resource("plural.po"))["Update available"])
        assertEquals("Aktualisierung verfügbar", PoData.read(resource("some-plural.po"))["Update available"])
        assertNull(PoData.read(resource("empty-plural.po"))["Update available"])
    }

    @Test
    fun escapeSequences() {
        val po = PoData.read(resource("escapes.po"))

        assertEquals("C:\\neu\\Ordner", po["C:\\new\\folder"])
        assertEquals("Name:\tWert", po["Name:\tValue"])
        assertEquals("Endet mit Backslash\\", po["Ends with backslash\\"])
        assertEquals("Zeilen\r\numbruch", po["Line\r\nbreak"])
        assertEquals("Maskierter Backslash \\n bleibt Text", po["Escaped backslash \\n stays text"])
        assertEquals("Unbekanntes \\q Escape", po["Unknown \\q escape"])
    }

    @Test
    fun outOfOrderPlural() {
        val po = PoData.read(resource("plural-out-of-order.po"))

        assertEquals("Aktualisierung verfügbar", po["Update available", 1])
        assertEquals("{{count}} Aktualisierungen verfügbar", po["Update available", 2])
    }

    @Test
    fun sparsePlural() {
        val po = PoData.read(resource("plural-sparse.po"))

        assertEquals("Aktualisierung verfügbar", po["Update available", 1])
        assertNull(po["Update available", 2])
        assertEquals("{{count}} Aktualisierungen\nverfügbar", po["Update available", 5])
    }

    @Test
    fun sparsePluralFallsBackToUntranslated() {
        val gettext = Gettext.load(Locale("de"), resource("plural-sparse.po"))

        assertEquals("{{count}} updates available", gettext.trn("Update available", "{{count}} updates available", 2))
        assertEquals("{{count}} Aktualisierungen\nverfügbar", gettext.trn("Update available", "{{count}} updates available", 5))
    }

    @Test
    fun pluralIndexAboveLimitIsSkipped() {
        val po = PoData.read(resource("plural-huge-index.po"))

        assertEquals("Aktualisierung verfügbar", po["Update available", 1])
        assertEquals("{{count}} Aktualisierungen verfügbar", po["Update available", 2])
    }
}
