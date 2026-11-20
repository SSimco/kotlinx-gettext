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
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PoTest {
    @Test
    fun `write empty`() {
        val out = ByteArrayOutputStream()
        PoFile(emptyList(), empty).write(out)
        assertEquals("msgid \"\"\nmsgstr \"\"\n\n", out.toString())
    }

    @Test
    fun `write default`() {
        val out = ByteArrayOutputStream()
        PoFile(emptyList()).write(out)
        assertEquals("$DEFAULT_POT_HEADER\n", out.toString())
    }

    @Test
    fun `write single entry`() {
        val out = ByteArrayOutputStream()
        PoFile(listOf(PoEntry(emptyList(), emptyList(), emptyList(), null, emptyList(), null, "message-id", null, emptyList()))).write(out)
        assertEquals("$DEFAULT_POT_HEADER\n\nmsgid \"message-id\"\nmsgstr \"\"\n", out.toString())
    }

    @Test
    fun `read simple entry`() {
        val s = "$DEFAULT_POT_HEADER\n\nmsgid \"message-id\"\nmsgstr \"\"\n"
        val po = PoFile.read(s.byteInputStream())

        assertEquals("", po.header.text)
        assertEquals(1, po.header.cases.size)
        assertEquals(DEFAULT_POT_HEADER.cases[0], po.header.cases[0])
        assertEquals(1, po.entries.size)
        assertEquals("message-id", po.entries[0].text)
        assertEquals(1, po.entries[0].cases.size)
        assertEquals("", po.entries[0].cases[0])
    }

    @Test
    fun `read simple entry with excess whitespace`() {
        val s = "$DEFAULT_POT_HEADER\n\nmsgid   \"message-id\"\nmsgstr  \"\"\n"
        val po = PoFile.read(s.byteInputStream())

        assertEquals("", po.header.text)
        assertEquals(1, po.header.cases.size)
        assertEquals(DEFAULT_POT_HEADER.cases[0], po.header.cases[0])
        assertEquals(1, po.entries.size)
        assertEquals("message-id", po.entries[0].text)
        assertEquals(1, po.entries[0].cases.size)
        assertEquals("", po.entries[0].cases[0])
    }

    @Test
    fun `read quoted entry`() {
        val s = "$DEFAULT_POT_HEADER\n\nmsgid \"message-id\"\nmsgstr \"Hello \\\"World\\\"!\"\n"
        val po = PoFile.read(s.byteInputStream())

        assertEquals("", po.header.text)
        assertEquals(1, po.header.cases.size)
        assertEquals(1, po.entries.size)
        assertEquals(DEFAULT_POT_HEADER.cases[0], po.header.cases[0])
        assertEquals("message-id", po.entries[0].text)
        assertEquals(1, po.entries[0].cases.size)
        assertEquals("Hello \"World\"!", po.entries[0].cases[0])
    }

    @Test
    fun `write quoted entry`() {
        val out = ByteArrayOutputStream()
        PoFile(listOf(PoEntry(emptyList(), emptyList(), emptyList(), null, emptyList(), null, "message-id", null, listOf("Hello \"World\"!")))).write(out)
        assertEquals("$DEFAULT_POT_HEADER\n\nmsgid \"message-id\"\nmsgstr \"Hello \\\"World\\\"!\"\n", out.toString())
    }

    @Test
    fun `read entry with newline`() {
        val s = "$DEFAULT_POT_HEADER\n\nmsgid \"message-id\"\nmsgstr \"Hello\\n\\\"World\\\"!\"\n"
        val po = PoFile.read(s.byteInputStream())

        assertEquals("", po.header.text)
        assertEquals(1, po.header.cases.size)
        assertEquals(DEFAULT_POT_HEADER.cases[0], po.header.cases[0])
        assertEquals(1, po.entries.size)
        assertEquals("message-id", po.entries[0].text)
        assertEquals(1, po.entries[0].cases.size)
        assertEquals("Hello\n\"World\"!", po.entries[0].cases[0])
    }

    @Test
    fun `write entry with newline`() {
        val out = ByteArrayOutputStream()
        PoFile(listOf(PoEntry(emptyList(), emptyList(), emptyList(), null, emptyList(), null, "message-id", null, listOf("Hello\n\"World\"!")))).write(out)
        assertEquals("$DEFAULT_POT_HEADER\n\nmsgid \"message-id\"\nmsgstr \"\"\n\"Hello\\n\"\n\"\\\"World\\\"!\"\n", out.toString())
    }

    @Test
    fun `write entry with trailing newline on one line`() {
        val out = ByteArrayOutputStream()
        PoFile(listOf(PoEntry(emptyList(), emptyList(), emptyList(), null, emptyList(), null, "Hello\n", null, emptyList()))).write(out)
        assertEquals("$DEFAULT_POT_HEADER\n\nmsgid \"Hello\\n\"\nmsgstr \"\"\n", out.toString())
    }

    @Test
    fun `write default header in multiline format`() {
        val header = DEFAULT_POT_HEADER.toString()

        assertTrue(header.contains("msgstr \"\"\n\"Project-Id-Version: PACKAGE VERSION\\n\"\n\"Report-Msgid-Bugs-To: \\n\"\n\"\\n\"\n"))
        assertTrue(header.endsWith("\n\"Plural-Forms: nplurals=INTEGER; plural=EXPRESSION;\\n\"\n"))
    }

    @Test
    fun `read multiline header`() {
        val po = PoFile.read(multilineHeader.byteInputStream())

        assertEquals(1, po.header.cases.size)
        assertEquals(
            "Project-Id-Version: sample\nReport-Msgid-Bugs-To: \n\nContent-Type: text/plain; charset=UTF-8\nX-Quote: \"quoted\"\n",
            po.header.cases[0]
        )
    }

    @Test
    fun `round trip multiline header`() {
        val out = ByteArrayOutputStream()
        PoFile.read(multilineHeader.byteInputStream()).write(out)

        assertEquals(multilineHeader, out.toString())
    }

    @Test
    fun `update keeps multiline header`() {
        val original = PoFile.read(multilineHeader.byteInputStream())
        val updated = original.update(listOf(PoEntry(emptyList(), emptyList(), listOf("main.kt:2"), null, emptyList(), null, "message-id", null, emptyList())))
        val out = ByteArrayOutputStream()
        updated.write(out)

        assertEquals("$multilineHeader\n#: main.kt:2\nmsgid \"message-id\"\nmsgstr \"\"\n", out.toString())
    }

    @Test
    fun `read multiline fields`() {
        val s = """
            |msgctxt ""
            |"first context\n"
            |"second context"
            |msgid ""
            |"One \"file\"\n"
            |"in a folder"
            |msgid_plural ""
            |"Many files\n"
            |"in a folder"
            |msgstr[0] ""
            |"Eine Datei\n"
            |"im Ordner"
            |msgstr[1] ""
            |"Viele Dateien\n"
            |"im Ordner"
            |""".trimMargin()
        val po = PoFile.read(s.byteInputStream())

        assertEquals(1, po.entries.size)
        val entry = po.entries[0]
        assertEquals("first context\nsecond context", entry.context)
        assertEquals("One \"file\"\nin a folder", entry.text)
        assertEquals("Many files\nin a folder", entry.plural)
        assertEquals(listOf("Eine Datei\nim Ordner", "Viele Dateien\nim Ordner"), entry.cases)
    }

    @Test
    fun `round trip multiline fields`() {
        val entry = PoEntry(
            emptyList(), emptyList(), emptyList(), null, emptyList(),
            "first context\nsecond context",
            "One \"file\"\nin a folder",
            "Many files\nin a folder",
            listOf("Eine Datei\nim Ordner", "Viele Dateien\nim Ordner\n")
        )
        val out = ByteArrayOutputStream()
        PoFile(listOf(entry)).write(out)

        assertEquals(entry, PoFile.read(out.toByteArray().inputStream()).entries.single())
    }

    private val multilineHeader = """
        |# SOME DESCRIPTIVE TITLE.
        |#, fuzzy
        |msgid ""
        |msgstr ""
        |"Project-Id-Version: sample\n"
        |"Report-Msgid-Bugs-To: \n"
        |"\n"
        |"Content-Type: text/plain; charset=UTF-8\n"
        |"X-Quote: \"quoted\"\n"
        |
        |""".trimMargin()

    private val empty = PoEntry(emptyList(), emptyList(), emptyList(), null, emptyList(), null, "", null, emptyList())
}