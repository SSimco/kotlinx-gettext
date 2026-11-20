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

/**
 * Locale of a translation catalog, identified by a BCP 47 language tag.
 *
 * The constructor accepts a language tag, such as `de` or `de-DE`,
 * or a POSIX locale name, such as `de_DE` or `de_DE.UTF-8@euro`.
 * It keeps the language, script, and region, and ignores other parts of the name.
 * A name without a valid language, such as an empty string or `und`, gives the root locale.
 * The constructor never throws.
 */
public class Locale(name: String) {
    /**
     * Lowercase language code, such as `de`, or an empty string for the root locale.
     */
    public val language: String

    /**
     * Title-case script code, such as `Latn`, or `null` if the locale has no script.
     */
    public val script: String?

    /**
     * Uppercase region code, such as `DE`, a UN M.49 area code, such as `419`,
     * or `null` if the locale has no region.
     */
    public val region: String?

    /**
     * Normalized language tag, such as `de-DE`, or an empty string for the root locale.
     */
    public val tag: String

    init {
        // A POSIX name has the form language_REGION.encoding@modifier.
        val subtags = name.substringBefore('.').substringBefore('@').replace('_', '-').split('-')
        val first = subtags[0]
        if (first.length in 2..8 && first.all { it.isAsciiLetter() } && !first.equals("und", ignoreCase = true)) {
            language = first.lowercase()
            var next = 1
            val scriptCandidate = subtags.getOrNull(next)
            script = if (scriptCandidate != null && scriptCandidate.length == 4 && scriptCandidate.all { it.isAsciiLetter() }) {
                next++
                scriptCandidate.lowercase().replaceFirstChar { it.uppercaseChar() }
            } else {
                null
            }
            val regionCandidate = subtags.getOrNull(next)
            region = if (regionCandidate != null && regionCandidate.isRegion()) regionCandidate.uppercase() else null
        } else {
            language = ""
            script = null
            region = null
        }
        tag = listOfNotNull(language.ifEmpty { null }, script, region).joinToString("-")
    }

    override fun equals(other: Any?): Boolean = other is Locale && tag == other.tag

    override fun hashCode(): Int = tag.hashCode()

    /**
     * Returns [tag].
     */
    override fun toString(): String = tag
}

/**
 * Root locale, used when no translations are loaded.
 */
public val DefaultLocale: Locale = Locale("")

private fun Char.isAsciiLetter(): Boolean = this in 'a'..'z' || this in 'A'..'Z'

private fun String.isRegion(): Boolean =
    length == 2 && all { it.isAsciiLetter() } || length == 3 && all { it in '0'..'9' }
