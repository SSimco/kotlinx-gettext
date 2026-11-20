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

import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Single entry of [PoFile]
 */
data class PoEntry(
    /**
     * Translator comments
     */
    val comments: List<String>,
    /**
     * Comments extracted from the source code
     */
    val extractedComments: List<String>,
    /**
     * Reference to the source code where this entry has been extracted from
     */
    val references: List<String>,
    /**
     * Flags
     */
    val flags: String?,
    /**
     * Previous entries of the same entry
     */
    val previous: List<String>,
    /**
     * Contextual information
     */
    val context: String?,
    /**
     * Original message, usually an untranslated string
     */
    val text: String,
    /**
     * Plural form of original untranslated string
     */
    val plural: String?,
    /**
     * Different cases of translated string for different plural forms
     */
    val cases: List<String>
) {
    fun write(out: PrintStream) {
        for (comment in comments) {
            out.println("# $comment")
        }
        for (comment in extractedComments) {
            out.println("#. $comment")
        }
        for (reference in references) {
            out.println("#: $reference")
        }
        if (flags != null) {
            out.println("#, $flags")
        }
        for (prev in previous) {
            out.println("#| $prev")
        }
        if (context != null) {
            out.printValue("msgctxt", context)
        }
        out.printValue("msgid", text)
        if (plural != null) {
            out.printValue("msgid_plural", plural)
            cases.forEachIndexed { idx, msg ->
                out.printValue("msgstr[$idx]", msg)
            }
        } else {
            out.printValue("msgstr", cases.firstOrNull() ?: "")
        }
    }

    /**
     * Prints [value] after [keyword], following the GNU gettext layout.
     * A value with a newline before its last character starts with an empty string,
     * followed by one quoted line per newline-terminated segment.
     */
    private fun PrintStream.printValue(keyword: String, value: String) {
        val newline = value.indexOf('\n')
        if (newline < 0 || newline == value.lastIndex) {
            println("$keyword \"${value.escape()}\"")
            return
        }
        println("$keyword \"\"")
        var start = 0
        while (start < value.length) {
            val end = value.indexOf('\n', start).let { if (it < 0) value.length else it + 1 }
            println("\"${value.substring(start, end).escape()}\"")
            start = end
        }
    }

    private fun String.escape(): String {
        return replace("\n", "\\n").replace("\"", "\\\"")
    }

    override fun toString(): String {
        return ByteArrayOutputStream().use {
            PrintStream(it, false, Charsets.UTF_8).use { printer -> write(printer) }
            it.toString()
        }
    }
}