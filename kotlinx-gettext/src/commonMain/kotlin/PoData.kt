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

import okio.Source
import okio.buffer

/**
 * Low-level representation of PO file.
 * Clients should use [I18n] instead.
 */
public class PoData(
    private val strings: Map<String, PoEntry>,
    private val pluralRule: PluralRuleExpression
) {
    /**
     * Get translation string for the key [id].
     * Note: When context is needed, the key is prefixed with it and [CONTEXT_DELIMITER].
     */
    public operator fun get(id: String): String? {
        return strings[id]?.str
    }

    /**
     * Get plural translation for the key [id]. The plural form is evaluated against the number [n].
     * Note: When context is needed, the key is prefixed with it and [CONTEXT_DELIMITER].
     */
    public operator fun get(id: String, n: Int): String? {
        val entry = strings[id] ?: return null
        val case = pluralRule.evaluate(n)
        if (0 <= case && case < entry.cases.size) {
            return entry.cases[case]
        }
        return entry.plural
    }

    private enum class Key {
        Context,
        Id,
        Str,
        Plural,
        Cases,
        Unknown
    }

    public companion object {
        /**
         * Context and key separator.
         */
        public const val CONTEXT_DELIMITER: Char = '\u0004'

        // Real languages use at most six plural forms.
        // A larger msgstr[N] index is malformed and is skipped to avoid a huge allocation.
        private const val MAX_PLURAL_FORMS = 100

        /**
         * Loads PO file from provided [input].
         */
        public fun read(input: Source): PoData {
            val entries = mutableMapOf<String, PoEntry>()
            var pluralRule = ""

            var context: String? = null
            var id: String? = null
            var str = ""
            var plural: String? = null
            // Plural forms by their msgstr[N] index.
            var cases = mutableMapOf<Int, String>()
            var caseIndex = 0

            var key = Key.Unknown

            val bufferedReader = input.buffer()
            try {
                while(true) {
                    val line = bufferedReader.readUtf8Line() ?: break
                    when {
                        line.isBlank() -> {
                            entry(entries, context, id, str, plural, cases)
                            context = null
                            id = null
                            str = ""
                            plural = null
                            cases = mutableMapOf()
                            key = Key.Unknown
                        }
                        line.startsWith("msgctxt ") -> {
                            context = line.substringAfter("msgctxt ").unescape()
                            key = Key.Context
                        }
                        line.startsWith("msgid ") -> {
                            id = line.substringAfter("msgid ").unescape()
                            key = Key.Id
                        }
                        line.startsWith("msgid_plural ") -> {
                            plural = line.substringAfter("msgid_plural ").unescape()
                            key = Key.Plural
                        }
                        line.startsWith("msgstr ") -> {
                            str = line.substringAfter("msgstr ").unescape()
                            key = Key.Str
                        }
                        line.startsWith("msgstr[") -> {
                            val index = line.substringAfter("msgstr[").substringBefore(']').trim().toIntOrNull()
                            if (index != null && index in 0 until MAX_PLURAL_FORMS) {
                                caseIndex = index
                                cases[index] = line.substringAfter(']').unescape()
                                key = Key.Cases
                            } else {
                                key = Key.Unknown
                            }
                        }
                        else -> {
                            val unescaped = line.unescape()
                            if (id?.isEmpty() == true && key == Key.Str && unescaped.startsWith("Plural-Forms:")) {
                                pluralRule = unescaped.substringAfter("plural=").substringBefore(';')
                            } else {
                                when (key) {
                                    Key.Context -> context += unescaped
                                    Key.Id -> id += unescaped
                                    Key.Str -> str += unescaped
                                    Key.Plural -> plural += unescaped
                                    Key.Cases -> cases[caseIndex] = cases.getValue(caseIndex) + unescaped

                                    else -> {}
                                }
                            }
                        }
                    }
                }
                entry(entries, context, id, str, plural, cases)
            } finally {
                bufferedReader.close()
            }
            return PoData(entries, PluralRule(pluralRule))
        }

        private fun String.unescape(): String {
            val trimmed = trim()
            val unquoted = if (trimmed.startsWith('"')) trimmed.substring(1, trimmed.lastIndex) else trimmed
            // Decode in one pass so that an escaped backslash never starts another escape.
            return buildString(unquoted.length) {
                var i = 0
                while (i < unquoted.length) {
                    val c = unquoted[i]
                    if (c == '\\' && i + 1 < unquoted.length) {
                        when (val next = unquoted[i + 1]) {
                            'n' -> append('\n')
                            't' -> append('\t')
                            'r' -> append('\r')
                            '"' -> append('"')
                            '\\' -> append('\\')
                            // Keep unsupported escape sequences as written.
                            else -> append(c).append(next)
                        }
                        i += 2
                    } else {
                        append(c)
                        i++
                    }
                }
            }
        }

        private fun entry(entries: MutableMap<String, PoEntry>, context: String?, id: String?, str: String, plural: String?, cases: Map<Int, String>) {
            if (!id.isNullOrEmpty() && (str.isNotEmpty() || cases.isNotEmpty())) {
                // Missing and empty forms become null, so lookups fall back to the untranslated text.
                val forms = List((cases.keys.maxOrNull() ?: -1) + 1) { cases[it]?.ifEmpty { null } }
                entries[if (context != null) "$context$CONTEXT_DELIMITER$id" else id] = PoEntry(str, plural, forms)
            }
        }
    }
}

