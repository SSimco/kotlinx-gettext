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
import kotlin.test.assertFailsWith

// mo.mo and mo-be.mo are compiled from mo.po with GNU msgfmt:
//   msgfmt -o mo.mo mo.po
//   msgfmt --endianness=big -o mo-be.mo mo.po
class MoDataTest {
    @Test
    fun littleEndian() {
        assertTranslations(Gettext.load(Locale("ru"), resource("mo.mo")))
    }

    @Test
    fun bigEndian() {
        assertTranslations(Gettext.load(Locale("ru"), resource("mo-be.mo")))
    }

    @Test
    fun sameAsPo() {
        val po = Gettext.load(Locale("ru"), resource("mo.po"))
        val mo = Gettext.load(Locale("ru"), resource("mo.mo"))

        for (n in listOf(0, 1, 2, 5, 11, 21, 22, 25, 101)) {
            assertEquals(po.trn("File", "Files", n), mo.trn("File", "Files", n))
            assertEquals(po.trnc("menu", "Item", "Items", n), mo.trnc("menu", "Item", "Items", n))
        }
        for (text in listOf("Hello World!", "Very very long\ndescription", "C:\\new\\folder\t\"quoted\"", "Untranslated")) {
            assertEquals(po.tr(text), mo.tr(text))
        }
        assertEquals(po.trc("feminine", "Installed"), mo.trc("feminine", "Installed"))
        assertEquals(po.trc("masculine", "Installed"), mo.trc("masculine", "Installed"))
    }

    @Test
    fun readCatalogEntriesSameAsPo() {
        assertEquals(PoData.readCatalog(resource("mo.po")).entries, PoData.readCatalog(resource("mo.mo")).entries)
        assertEquals(PoData.read(resource("mo.po")).entries, PoData.readCatalog(resource("mo-be.mo")).entries)
    }

    @Test
    fun emptyPluralFormsFallBack() {
        val gettext = Gettext.load(
            Locale("de"),
            mo("" to "Plural-Forms: nplurals=3; plural=(n==1 ? 0 : (n==2 ? 1 : 2));\n", "File\u0000Files" to "Datei\u0000\u0000Dateien"),
        )

        assertEquals("Datei", gettext.trn("File", "Files", 1))
        assertEquals("Files", gettext.trn("File", "Files", 2))
        assertEquals("Dateien", gettext.trn("File", "Files", 3))
    }

    @Test
    fun emptyTranslationFallsBack() {
        val gettext = Gettext.load(Locale("de"), mo("Hello" to ""))

        assertEquals("Hello", gettext.tr("Hello"))
    }

    @Test
    fun missingHeaderUsesDefaultPluralRule() {
        val gettext = Gettext.load(Locale("de"), mo("File\u0000Files" to "Datei\u0000Dateien"))

        // Without a Plural-Forms header, the GNU default rule n != 1 applies.
        assertEquals("Dateien", gettext.trn("File", "Files", 0))
        assertEquals("Datei", gettext.trn("File", "Files", 1))
        assertEquals("Dateien", gettext.trn("File", "Files", 2))
    }

    @Test
    fun mixedSourcesKeepPriority() {
        val gettext = Gettext.load(
            Locale("ru"),
            mo("Hello World!" to "Hallo Welt!"),
            resource("de.po"),
            resource("mo.mo"),
        )

        assertEquals("Hallo Welt!", gettext.tr("Hello World!"))
        assertEquals("Sehr sehr lange\n\nBeschreibung", gettext.tr("Very very long\n\ndescription"))
        assertEquals("Установлена", gettext.trc("feminine", "Installed"))
    }

    @Test
    fun emptySourceIsEmptyPo() {
        val gettext = Gettext.load(Locale("de"), ByteArray(0))

        assertEquals("Hello", gettext.tr("Hello"))
    }

    @Test
    fun tooShort() {
        assertFailsWith<IllegalArgumentException> {
            Gettext.load(Locale("de"), mo("Hello" to "Hallo").copyOf(20))
        }
    }

    @Test
    fun unsupportedRevision() {
        val bytes = mo("Hello" to "Hallo")
        bytes.setIntLe(4, 2 shl 16)

        assertFailsWith<IllegalArgumentException> { Gettext.load(Locale("de"), bytes) }
    }

    @Test
    fun minorRevisionIsAccepted() {
        val bytes = mo("Hello" to "Hallo")
        bytes.setIntLe(4, 1)

        assertEquals("Hallo", Gettext.load(Locale("de"), bytes).tr("Hello"))
    }

    @Test
    fun tableOutOfBounds() {
        val bytes = mo("Hello" to "Hallo")
        bytes.setIntLe(16, bytes.size - 4)

        assertFailsWith<IllegalArgumentException> { Gettext.load(Locale("de"), bytes) }
    }

    @Test
    fun hugeStringCount() {
        val bytes = mo("Hello" to "Hallo")
        bytes.setIntLe(8, -1)

        assertFailsWith<IllegalArgumentException> { Gettext.load(Locale("de"), bytes) }
    }

    @Test
    fun stringOutOfBounds() {
        val bytes = mo("Hello" to "Hallo")
        // The length of the first translated string.
        bytes.setIntLe(36, bytes.size)

        assertFailsWith<IllegalArgumentException> { Gettext.load(Locale("de"), bytes) }
    }

    @Test
    fun hugeStringOffset() {
        val bytes = mo("Hello" to "Hallo")
        // The offset of the first translated string.
        bytes.setIntLe(40, -1)

        assertFailsWith<IllegalArgumentException> { Gettext.load(Locale("de"), bytes) }
    }

    private fun assertTranslations(gettext: Gettext) {
        assertEquals("Привет, мир!", gettext.tr("Hello World!"))
        assertEquals("Очень очень длинное\nописание", gettext.tr("Very very long\ndescription"))
        assertEquals("C:\\новая\\папка\t\"в кавычках\"", gettext.tr("C:\\new\\folder\t\"quoted\""))
        assertEquals("Untranslated", gettext.tr("Untranslated"))
        assertEquals("Missing", gettext.tr("Missing"))

        assertEquals("Установлена", gettext.trc("feminine", "Installed"))
        assertEquals("Установлен", gettext.trc("masculine", "Installed"))
        assertEquals("Installed", gettext.trc("neuter", "Installed"))

        assertEquals("Файл", gettext.trn("File", "Files", 1))
        assertEquals("Файла", gettext.trn("File", "Files", 3))
        assertEquals("Файлов", gettext.trn("File", "Files", 5))
        assertEquals("Файлов", gettext.trn("File", "Files", 11))
        assertEquals("Файл", gettext.trn("File", "Files", 21))

        assertEquals("Пункт", gettext.trnc("menu", "Item", "Items", 1))
        assertEquals("Пункта", gettext.trnc("menu", "Item", "Items", 2))
        assertEquals("Пунктов", gettext.trnc("menu", "Item", "Items", 7))
    }

    // Builds a little-endian MO file with the given original and translated strings.
    private fun mo(vararg strings: Pair<String, String>): ByteArray {
        val originals = strings.map { it.first.encodeToByteArray() }
        val translations = strings.map { it.second.encodeToByteArray() }
        val tableSize = strings.size * 8
        var offset = 28 + 2 * tableSize

        val header = mutableListOf(0x950412de.toInt(), 0, strings.size, 28, 28 + tableSize, 0, 0)
        val data = mutableListOf<Byte>()
        for (bytes in originals + translations) {
            header += listOf(bytes.size, offset)
            data += bytes.toList() + 0.toByte()
            offset += bytes.size + 1
        }
        val result = ByteArray(header.size * 4) + data.toByteArray()
        header.forEachIndexed { i, value -> result.setIntLe(i * 4, value) }
        return result
    }

    private fun ByteArray.setIntLe(offset: Int, value: Int) {
        for (i in 0 until 4) {
            this[offset + i] = (value shr (8 * i)).toByte()
        }
    }
}
