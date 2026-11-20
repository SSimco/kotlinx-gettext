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

import java.io.InputStream
import java.text.MessageFormat

/**
 * Translate [MessageFormat]-compatible formatted [text] with provided [args].
 */
public fun I18n.tr(text: String, vararg args: Any?): String =
    formatMessage(tr(text), args)

/**
 * Translate [MessageFormat]-compatible formatted [text] with different [plural] form
 * with provided [args], chosen based on provided number [n].
 */
public fun I18n.trn(text: String, plural: String, n: Int, vararg args: Any?): String =
    formatMessage(trn(text, plural, n), args)

/**
 * Translate [MessageFormat]-compatible formatted [text] with different [plural] form
 * with provided [args], chosen based on provided number [n].
 */
public fun I18n.trn(text: String, plural: String, n: Long, vararg args: Any?): String =
    trn(text, plural, n.toInt(), *args)

/**
 * Translate [MessageFormat]-compatible formatted [text] with provided [args] in a given [context].
 */
public fun I18n.trc(context: String, text: String, vararg args: Any?): String =
    formatMessage(trc(context, text), args)

/**
 * Translate [MessageFormat]-compatible formatted [text] with different [plural] form
 * with provided [args], chosen based on provided number [n] in a given [context].
 */
public fun I18n.trnc(context: String, text: String, plural: String, n: Int, vararg args: Any?): String =
    formatMessage(trnc(context, text, plural, n), args)

/**
 * Translate [MessageFormat]-compatible formatted [text] with different [plural] form
 * with provided [args], chosen based on provided number [n] in a given [context].
 */
public fun I18n.trnc(context: String, text: String, plural: String, n: Long, vararg args: Any?): String =
    trnc(context, text, plural, n.toInt(), *args)

/**
 * Loads a PO file from the given input stream and closes it.
 */
public fun PoData.Companion.read(input: InputStream): PoData =
    read(input.use { it.readBytes() })

/**
 * Load translations for given [locale] from given input stream and close it.
 * The stream can contain a PO file or a compiled MO file.
 */
public fun Gettext.Companion.load(locale: Locale, s: InputStream): Gettext =
    load(locale, s.use { it.readBytes() })

/**
 * Load translations for given [locale] from given input stream and close it.
 * The stream can contain a PO file or a compiled MO file.
 */
public fun Gettext.Companion.load(locale: java.util.Locale, s: InputStream): Gettext =
    load(locale.toGettextLocale(), s)

/**
 * Load translations for given [locale] from the given [catalog] file content.
 * The catalog can be a PO file or a compiled MO file.
 */
public fun Gettext.Companion.load(locale: java.util.Locale, catalog: ByteArray): Gettext =
    load(locale.toGettextLocale(), catalog)

/**
 * Load translations for given [locale] from the given [catalogs] file contents.
 * Catalogs are checked in order of priority, first available translation is used.
 */
public fun Gettext.Companion.load(locale: java.util.Locale, vararg catalogs: ByteArray): Gettext =
    load(locale.toGettextLocale(), *catalogs)

// Formats with the catalog locale, so numbers and dates match the translation.
private fun I18n.formatMessage(pattern: String, args: Array<out Any?>): String =
    MessageFormat(pattern, locale.toJavaLocale()).format(args)
