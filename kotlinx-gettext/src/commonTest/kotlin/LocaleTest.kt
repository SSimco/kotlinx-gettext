package name.kropp.kotlinx.gettext

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class LocaleTest {
    @Test
    fun language() {
        val locale = Locale("de")

        assertEquals("de", locale.language)
        assertNull(locale.script)
        assertNull(locale.region)
        assertEquals("de", locale.tag)
    }

    @Test
    fun languageAndRegion() {
        val locale = Locale("de-DE")

        assertEquals("de", locale.language)
        assertEquals("DE", locale.region)
        assertEquals("de-DE", locale.tag)
        assertEquals("de-DE", locale.toString())
    }

    @Test
    fun scriptAndRegion() {
        val locale = Locale("sr-latn-rs")

        assertEquals("sr", locale.language)
        assertEquals("Latn", locale.script)
        assertEquals("RS", locale.region)
        assertEquals("sr-Latn-RS", locale.tag)
    }

    @Test
    fun numericRegion() {
        assertEquals("es-419", Locale("es-419").tag)
    }

    @Test
    fun normalizesCase() {
        assertEquals(Locale("de-DE"), Locale("DE-de"))
    }

    @Test
    fun posixName() {
        assertEquals(Locale("de-DE"), Locale("de_DE"))
        assertEquals(Locale("de-DE"), Locale("de_DE.UTF-8"))
        assertEquals(Locale("de-DE"), Locale("de_DE@euro"))
        assertEquals(Locale("de-DE"), Locale("de_DE.ISO-8859-15@euro"))
    }

    @Test
    fun ignoresOtherSubtags() {
        assertEquals("de-CH", Locale("de-CH-1996").tag)
        assertEquals("de", Locale("de-x-private").tag)
    }

    @Test
    fun rootLocale() {
        assertEquals("", Locale("").tag)
        assertEquals("", Locale("und").tag)
        assertEquals("", Locale("C").tag)
        assertEquals("", Locale("123").tag)
        assertEquals(DefaultLocale, Locale(""))
    }

    @Test
    fun equality() {
        assertEquals(Locale("de"), Locale("de"))
        assertEquals(Locale("de").hashCode(), Locale("de").hashCode())
        assertNotEquals(Locale("de"), Locale("de-DE"))
        assertNotEquals(Locale("de"), Locale("ru"))
    }

    @Test
    fun gettextLocale() {
        assertEquals(Locale("ru"), Gettext.load(Locale("ru"), resource("ru.po")).locale)
    }
}
