package name.kropp.kotlinx.gettext

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
