package com.mochen.reader.parser

import android.content.Context
import android.net.Uri
import com.mochen.reader.domain.model.Chapter
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser for MOBI and AZW3 (Kindle) ebook formats.
 * MOBI format is based on PalmDOC with additional MOBI header structures.
 * AZW3 (KF8) is Amazon's enhanced version with additional features.
 */
@Singleton
class MobipocketParser @Inject constructor() {

    companion object {
        // MOBI format identifiers
        private const val PDB_NAME = "TURBO"
        private const val MOBI_HEADER_MAGIC = 0x4D4F4249 // "MOBI"
        private const val MOBI_HEADER_MAGIC2 = 0x424F4F4B // "BOOK" (alternate)
        private const val AZW3_MAGIC = 0x414B5742 // "AWBZ" (AZW3 identifier)

        // Record type identifiers
        private const val PALMDOC_TYPE = 1
        private const val MOBI_TYPE = 2
        private const val AZW3_TYPE = 3
    }

    /**
     * Parse chapters from a MOBI/AZW3 file.
     * MOBI files have a complex structure:
     * - PDB header (78 bytes)
     * - MOBI header (various fields including chapter markers)
     * - Text records
     */
    suspend fun parseChapters(context: Context, uri: Uri, bookId: Long): List<Chapter> {
        val chapters = mutableListOf<Chapter>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()

                // Parse PDB header
                val pdbParser = PdbParser(bytes)
                if (!pdbParser.isValid) return@use

                // Extract chapter information
                val tocRecords = pdbParser.getTocRecords()
                val textRecords = pdbParser.getTextRecords()

                if (tocRecords.isNotEmpty()) {
                    // Use TOC records as chapters
                    tocRecords.forEachIndexed { index, tocRecord ->
                        chapters.add(
                            Chapter(
                                bookId = bookId,
                                chapterIndex = index,
                                title = tocRecord.title.ifEmpty { "第${index + 1}章" },
                                startPosition = tocRecord.startPos.toLong(),
                                endPosition = tocRecord.endPos.toLong(),
                                wordCount = 0
                            )
                        )
                    }
                } else if (textRecords.isNotEmpty()) {
                    // Fallback: create chapters from text records
                    // MOBI files often have one record per section
                    textRecords.forEachIndexed { index, _ ->
                        chapters.add(
                            Chapter(
                                bookId = bookId,
                                chapterIndex = index,
                                title = "第${index + 1}节",
                                startPosition = index.toLong(),
                                endPosition = index.toLong(),
                                wordCount = 0
                            )
                        )
                    }
                } else {
                    // Single chapter fallback
                    chapters.add(
                        Chapter(
                            bookId = bookId,
                            chapterIndex = 0,
                            title = "全文",
                            startPosition = 0,
                            endPosition = 0,
                            wordCount = 0
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return chapters
    }

    /**
     * Read chapter content from a MOBI/AZW3 file.
     */
    suspend fun readChapterContent(context: Context, uri: Uri, chapter: Chapter): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val pdbParser = PdbParser(bytes)

                if (!pdbParser.isValid) return@use ""

                val textRecords = pdbParser.getTextRecords()
                val chapterIndex = chapter.chapterIndex.toInt()

                if (chapterIndex < textRecords.size) {
                    // Decode the text record
                    val record = textRecords[chapterIndex]
                    decodeTextRecord(record)
                } else {
                    // Try to read from start position
                    val text = StringBuilder()
                    textRecords.forEachIndexed { index, recordBytes ->
                        if (index >= chapter.startPosition.toInt()) {
                            text.append(decodeTextRecord(recordBytes))
                            text.append("\n\n")
                        }
                    }
                    text.toString()
                }
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Extract book metadata from MOBI/AZW3 file.
     */
    suspend fun getBookInfo(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val pdbParser = PdbParser(bytes)

                if (!pdbParser.isValid) return@use null

                val title = pdbParser.getTitle()
                val author = pdbParser.getAuthor()

                if (title != null || author != null) {
                    Pair(title ?: "未知书名", author ?: "未知作者")
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decode a MOBI text record.
     * MOBI records can be compressed using PalmDOC, zlib, or stored as-is.
     */
    private fun decodeTextRecord(record: ByteArray): String {
        if (record.isEmpty()) return ""

        return try {
            // Check for zlib compression (most common)
            if (record.size >= 2) {
                // Check for zlib header (0x78 with follow-up byte)
                if (record[0].toInt() == 0x78) {
                    try {
                        val decompressed = decompressZlib(record)
                        if (decompressed != null) {
                            return decompressed.trim()
                        }
                    } catch (e: Exception) {
                        // Not zlib, try as plain text
                    }
                }

                // Check for PalmDOC compression (first 2 bytes indicate compression)
                val compression = ((record[0].toInt() and 0xFF) shl 8) or (record[1].toInt() and 0xFF)
                if (compression == 1 || (record[0].toInt() and 0xFF) == 0) {
                    // PalmDOC compression
                    return decompressPalmdoc(record)
                }
            }

            // Fallback: return as UTF-8 or Latin-1
            try {
                String(record, Charsets.UTF_8).trim()
            } catch (e: Exception) {
                String(record, Charsets.ISO_8859_1).trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Decompress zlib compressed data.
     */
    private fun decompressZlib(data: ByteArray): String? {
        return try {
            val inflater = java.util.zip.Inflater()
            inflater.setInput(data)
            val output = ByteArray(8192)
            val result = inflater.inflate(output)
            inflater.end()

            if (result > 0) {
                String(output, 0, result, Charsets.UTF_8).trim()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decompress PalmDOC compressed data.
     * PalmDOC uses a simple LZ77 variant with Huffman coding.
     */
    private fun decompressPalmdoc(data: ByteArray): String {
        if (data.size < 2) return String(data, Charsets.UTF_8)

        val compression = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
        if (compression != 1) {
            // Not PalmDOC compressed
            return String(data, Charsets.UTF_8).trim()
        }

        // For now, return raw text (PalmDOC decompression is complex)
        // A full implementation would decode the Huffman table and LZ77
        return try {
            String(data, 2, data.size - 2, Charsets.UTF_8).trim()
        } catch (e: Exception) {
            String(data, Charsets.ISO_8859_1).trim()
        }
    }
}

/**
 * Internal PDB file parser for MOBI format.
 * PDB files have a specific header structure followed by record entries.
 */
private class PdbParser(private val data: ByteArray) {

    data class TocRecord(
        val title: String,
        val startPos: Int,
        val endPos: Int
    )

    val isValid: Boolean
        get() = data.size >= 78 && hasValidPdbHeader

    private val hasValidPdbHeader: Boolean
        get() {
            // PDB files start with a name field (32 bytes) + attributes + unique ID
            // Check for common MOBI format markers
            val name = String(data, 0, minOf(32, data.size), Charsets.US_ASCII).trim()
            return name.isNotEmpty() || isMobiFormat
        }

    private val isMobiFormat: Boolean
        get() {
            // Check for MOBI magic bytes in the first 256 bytes
            for (i in 0 until minOf(data.size - 4, 256)) {
                val word = ((data[i].toInt() and 0xFF) shl 24) or
                        ((data[i + 1].toInt() and 0xFF) shl 16) or
                        ((data[i + 2].toInt() and 0xFF) shl 8) or
                        (data[i + 3].toInt() and 0xFF)
                if (word == 0x4D4F4249 || word == 0x424F4F4B) {
                    return true
                }
            }
            return false
        }

    private var mobiHeaderOffset: Int = -1
    private var textRecordCount: Int = 0
    private var textRecordOffset: Int = 0
    private var title: String? = null
    private var author: String? = null
    private var tocRecords: MutableList<TocRecord> = mutableListOf()
    private var textRecords: MutableList<ByteArray> = mutableListOf()

    init {
        parsePdbHeader()
    }

    private fun parsePdbHeader() {
        if (data.size < 78) return

        try {
            // PDB header structure:
            // 0-31: Name (32 bytes)
            // 32-33: Attributes
            // 34-35: Version
            // 36-39: Creation time
            // 40-43: Modification time
            // 44-47: Last backup time
            // 48-49: Modification number
            // 50-51: App info ID
            // 52-55: Sort info ID
            // 56-59: Type (4 bytes) - "BOOK" or "MOBI"
            // 60-63: Creator (4 bytes) - "MOBI" or "AKPF"
            // 64-67: Unique ID seed
            // 68-69: Next record list ID
            // 70-71: Number of records
            // 72+: Record entries (8 bytes each)

            val recordCount = ((data[70].toInt() and 0xFF) shl 8) or (data[71].toInt() and 0xFF)

            // Parse record entries to find text records
            var currentOffset = 0
            for (i in 0 until recordCount) {
                val entryOffset = 72 + i * 8
                if (entryOffset + 8 > data.size) break

                val recOffset = ((data[entryOffset].toInt() and 0xFF) shl 24) or
                        ((data[entryOffset + 1].toInt() and 0xFF) shl 16) or
                        ((data[entryOffset + 2].toInt() and 0xFF) shl 8) or
                        (data[entryOffset + 3].toInt() and 0xFF)

                val recLen = ((data[entryOffset + 4].toInt() and 0xFF) shl 8) or
                        (data[entryOffset + 5].toInt() and 0xFF)

                // Check record attributes
                val recAttrs = (data[entryOffset + 6].toInt() and 0xFF)

                // Text records typically have specific attributes
                // Record 0 often contains the MOBI header
                if (i == 0) {
                    mobiHeaderOffset = recOffset
                    parseMobiHeader(recOffset)
                }

                // Read text content from data records
                if (recOffset > 0 && recLen > 0 && recOffset < data.size) {
                    val recordData = data.copyOfRange(recOffset, minOf(recOffset + recLen, data.size))
                    if (recordData.isNotEmpty()) {
                        textRecords.add(recordData)
                    }
                }
            }

            // Extract TOC from INDX records if available
            extractTocFromIndx()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseMobiHeader(offset: Int) {
        if (offset + 132 > data.size) return

        try {
            // MOBI header starts at offset + 16 from the record data
            // Full MOBI header is complex, key fields are:
            // 0-3: Magic "MOBI"
            // 4-7: Header length
            // 8-11: MOBI type
            // 12-15: Text encoding (65001 = UTF-8, 1252 = Latin-1)
            // 16-19: Unique ID
            // 20-23: File version
            // 24-27: Orthographic index
            // 28-31: inflection index
            // 32-35: Index names (look for "INDX" markers)
            // 36-39: Total index records
            // 40-43: Primary index record
            // 44-47: Metadata index
            // 48-51: Content index
            // 52-55: FIST index
            // 56-59: DATP index
            // 60-63: FLIS index
            // 64-67: CRP index
            // 68-71: SRP index
            // 72-75: SRP start
            // 76-79: Number of SRP entries
            // 80-83: SRP index type
            // 84-87: SRP index encoding
            // 88-91: SRP index language
            // 92-95: First content record number
            // 96-99: Last content record number
            // 100-103: First non-book record
            // 104-107: Last non-book record
            // 108-111: First book record
            // 112-115: Last book record
            // 116-119: EXTH record count
            // 120-123: EXTH record offset

            // Check for EXTH records (extended metadata)
            val exthOffset = ((data[offset + 120].toInt() and 0xFF) shl 24) or
                    ((data[offset + 121].toInt() and 0xFF) shl 16) or
                    ((data[offset + 122].toInt() and 0xFF) shl 8) or
                    (data[offset + 123].toInt() and 0xFF)

            val exthCount = ((data[offset + 116].toInt() and 0xFF) shl 24) or
                    ((data[offset + 117].toInt() and 0xFF) shl 16) or
                    ((data[offset + 118].toInt() and 0xFF) shl 8) or
                    (data[offset + 119].toInt() and 0xFF)

            if (exthOffset > 0 && exthCount > 0) {
                parseExthRecords(offset + exthOffset, exthCount)
            }

            // Get text record info
            textRecordCount = ((data[offset + 92].toInt() and 0xFF) shl 24) or
                    ((data[offset + 93].toInt() and 0xFF) shl 16) or
                    ((data[offset + 94].toInt() and 0xFF) shl 8) or
                    (data[offset + 95].toInt() and 0xFF)

            textRecordOffset = ((data[offset + 108].toInt() and 0xFF) shl 24) or
                    ((data[offset + 109].toInt() and 0xFF) shl 16) or
                    ((data[offset + 110].toInt() and 0xFF) shl 8) or
                    (data[offset + 111].toInt() and 0xFF)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseExthRecords(offset: Int, count: Int) {
        try {
            if (offset >= data.size) return

            // EXTH header: "EXTH" + record size
            if (offset + 4 > data.size) return

            var pos = offset + 4 // Skip "EXTH" and record size

            for (i in 0 until count) {
                if (pos + 8 > data.size) break

                val recType = ((data[pos].toInt() and 0xFF) shl 24) or
                        ((data[pos + 1].toInt() and 0xFF) shl 16) or
                        ((data[pos + 2].toInt() and 0xFF) shl 8) or
                        (data[pos + 3].toInt() and 0xFF)

                val recLen = ((data[pos + 4].toInt() and 0xFF) shl 24) or
                        ((data[pos + 5].toInt() and 0xFF) shl 16) or
                        ((data[pos + 6].toInt() and 0xFF) shl 8) or
                        (data[pos + 7].toInt() and 0xFF)

                if (recLen <= 8 || pos + recLen > data.size) break

                val recData = data.copyOfRange(pos + 8, pos + recLen)

                when (recType) {
                    // 100 = Title
                    100 -> {
                        title = String(recData, Charsets.UTF_8).trim()
                    }
                    // 101 = Author
                    101 -> {
                        author = String(recData, Charsets.UTF_8).trim()
                    }
                    // 503 = Publisher
                    503 -> {
                        // Publisher info
                    }
                    // 504 = Imprint
                    504 -> {}
                    // 505 = Description
                    505 -> {}
                    // 508 = Language
                    508 -> {}
                    // 517 = Copyright
                    517 -> {}
                    // 518 = ISBN
                    518 -> {}
                    // 519 = Subject
                    519 -> {}
                    // 523 = Review
                    523 -> {}
                }

                pos += recLen
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractTocFromIndx() {
        // Look for INDX records which contain table of contents
        // This is a simplified implementation
        // Full TOC extraction requires parsing the complex INDX structure
        try {
            for (record in textRecords) {
                if (record.size >= 4) {
                    val marker = String(record, 0, 4, Charsets.US_ASCII)
                    if (marker == "INDX") {
                        // Parse INDX record for chapter markers
                        parseIndxRecord(record)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseIndxRecord(record: ByteArray) {
        // INDX records contain offset table entries with chapter positions
        // This is a simplified extraction
        try {
            // Skip header and find TAGX/TAG section
            var pos = 8
            while (pos + 8 < record.size) {
                val tag = ((record[pos].toInt() and 0xFF) shl 8) or (record[pos + 1].toInt() and 0xFF)
                val dataLen = ((record[pos + 2].toInt() and 0xFF) shl 8) or (record[pos + 3].toInt() and 0xFF)

                // tag 0x1 = name/label
                // tag 0x2 = position/cfi
                if (tag == 0x1 && dataLen > 0) {
                    val nameLen = ((record[pos + 4].toInt() and 0xFF) shl 8) or (record[pos + 5].toInt() and 0xFF)
                    if (nameLen > 0 && pos + 8 + nameLen <= record.size) {
                        val name = String(record, pos + 8, nameLen, Charsets.UTF_8).trim()
                        if (name.isNotEmpty()) {
                            // Found a chapter title
                            // Try to find corresponding position
                            if (tocRecords.isNotEmpty()) {
                                val last = tocRecords.last()
                                tocRecords.add(TocRecord(name, last.endPos + 1, last.endPos + 1))
                            } else {
                                tocRecords.add(TocRecord(name, tocRecords.size, tocRecords.size))
                            }
                        }
                    }
                }
                pos += 8 + dataLen
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTitle(): String? = title
    fun getAuthor(): String? = author
    fun getTocRecords(): List<TocRecord> = tocRecords
    fun getTextRecords(): List<ByteArray> = textRecords
}
