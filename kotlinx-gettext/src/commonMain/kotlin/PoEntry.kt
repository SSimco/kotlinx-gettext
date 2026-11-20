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
 * Data for a single PO file entry.
 */
public data class PoEntry(
    /**
     * Translated string, or an empty string for plural entries.
     */
    public val str: String,
    /**
     * Untranslated plural form from `msgid_plural`, or `null` if the entry has no plural form.
     */
    public val plural: String?,
    /**
     * Translated plural forms, indexed by the plural rule result.
     * A form is `null` when its translation is empty.
     */
    public val cases: List<String?>,
)
