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

package io.github.ssimco.kotlinx.gettext

// Reads compiled GNU gettext MO files into the same PoData model as PO files.
// The format is described at
// https://www.gnu.org/software/gettext/manual/html_node/MO-Files.html
internal object MoData {
    private const val MAGIC = 0x950412deL
    private const val MAGIC_SWAPPED = 0xde120495L
    private const val HEADER_SIZE = 28
    private const val TABLE_ENTRY_SIZE = 8
    private const val NUL = '\u0000'

    // Returns true when the data starts with the MO magic number in either byte order.
    fun isMo(data: ByteArray): Boolean {
        if (data.size < 4) return false
        val magic = (0 until 4).fold(0L) { value, i -> value or ((data[i].toLong() and 0xff) shl (8 * i)) }
        return magic == MAGIC || magic == MAGIC_SWAPPED
    }

    // Reads an MO file.
    // Throws IllegalArgumentException when the file is malformed.
    fun read(data: ByteArray): PoData = Reader(data).read()

    private class Reader(private val data: ByteArray) {
        private val bigEndian: Boolean

        init {
            require(data.size >= HEADER_SIZE) { "MO file is too short" }
            // The magic number reads as MAGIC_SWAPPED when the file is big-endian.
            bigEndian = when (uint(0, bigEndian = false)) {
                MAGIC -> false
                MAGIC_SWAPPED -> true
                else -> throw IllegalArgumentException("Invalid MO magic number")
            }
        }

        fun read(): PoData {
            // Major revisions 0 and 1 share the string tables used here.
            // Revision 1 adds system-dependent strings, which only apply to C programs.
            val majorRevision = uint(4) shr 16
            require(majorRevision <= 1) { "Unsupported MO revision $majorRevision" }

            val count = uint(8)
            val originals = uint(12)
            val translations = uint(16)
            val tableSize = count * TABLE_ENTRY_SIZE
            require(originals + tableSize <= data.size && translations + tableSize <= data.size) {
                "MO string table is out of bounds"
            }

            val entries = mutableMapOf<String, PoEntry>()
            var pluralRule = ""
            for (i in 0 until count.toInt()) {
                // The original string has the form "context\u0004id\u0000plural".
                // Context and plural parts are optional.
                val original = string(originals + i * TABLE_ENTRY_SIZE)
                val translation = string(translations + i * TABLE_ENTRY_SIZE)
                if (original.isEmpty()) {
                    pluralRule = PoData.pluralRuleFromHeader(translation)
                    continue
                }
                val id = original.substringBefore(NUL)
                if (NUL in original) {
                    // Plural forms are separated by NUL characters.
                    val cases = translation.split(NUL).withIndex().associate { it.index to it.value }
                    PoData.entry(entries, null, id, "", original.substringAfter(NUL), cases)
                } else {
                    PoData.entry(entries, null, id, translation, null, emptyMap())
                }
            }
            return PoData(entries, PoData.parsePluralRule(pluralRule))
        }

        // Reads the string described by the table entry at the given offset.
        // A table entry holds the string length followed by the string offset.
        private fun string(entryOffset: Long): String {
            val length = uint(entryOffset)
            val offset = uint(entryOffset + 4)
            require(offset + length <= data.size) { "MO string is out of bounds" }
            return data.decodeToString(offset.toInt(), (offset + length).toInt())
        }

        // Reads an unsigned 32-bit integer.
        // Callers check that the offset is within the data.
        private fun uint(offset: Long, bigEndian: Boolean = this.bigEndian): Long {
            val i = offset.toInt()
            val b0 = data[i].toLong() and 0xff
            val b1 = data[i + 1].toLong() and 0xff
            val b2 = data[i + 2].toLong() and 0xff
            val b3 = data[i + 3].toLong() and 0xff
            return if (bigEndian) {
                (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
            } else {
                (b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0
            }
        }
    }
}
