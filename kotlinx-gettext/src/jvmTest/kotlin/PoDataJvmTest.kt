package io.github.ssimco.kotlinx.gettext

import kotlin.test.Test
import kotlin.test.assertEquals

class PoDataJvmTest {
    @Test
    fun readInputStream() {
        val input = """
            msgid "Hello"
            msgstr "Hallo"
        """.trimIndent().byteInputStream()

        assertEquals(mapOf("Hello" to PoEntry("Hallo", null, emptyList())), PoData.read(input).entries)
    }
}
