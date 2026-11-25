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
