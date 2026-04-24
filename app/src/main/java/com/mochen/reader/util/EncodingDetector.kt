package com.mochen.reader.util

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object EncodingDetector {

    private val SUPPORTED_ENCODINGS = listOf(
        StandardCharsets.UTF_8,
        Charset.forName("GBK"),
        Charset.forName("GB2312"),
        Charset.forName("BIG5"),
        StandardCharsets.ISO_8859_1,
        StandardCharsets.US_ASCII
    )

    fun detect(bytes: ByteArray): String {
        // Check BOM
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return "UTF-8"
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return "UTF-16LE"
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return "UTF-16BE"
        }

        // Try to detect encoding by analyzing byte patterns
        val encoding = detectByAnalysis(bytes)
        if (encoding != null) {
            return encoding
        }

        // Default to UTF-8
        return "UTF-8"
    }

    private fun detectByAnalysis(bytes: ByteArray): String? {
        // Simple heuristic: check for common Chinese character byte patterns
        var likelyGB = 0
        var likelyUTF8 = 0

        var i = 0
        while (i < bytes.size - 1) {
            val b1 = bytes[i].toInt() and 0xFF
            val b2 = if (i + 1 < bytes.size) bytes[i + 1].toInt() and 0xFF else 0

            // GBK/GB2312: 0x81-0xFE followed by 0x40-0xFE
            if (b1 in 0x81..0xFE && b2 in 0x40..0xFE) {
                likelyGB++
                i += 2
                continue
            }

            // UTF-8 multi-byte sequences
            if (b1 in 0xE4..0xE9 && bytes.size > i + 2) {
                val b2Check = bytes[i + 1].toInt() and 0xFF
                val b3 = if (i + 2 < bytes.size) bytes[i + 2].toInt() and 0xFF else 0
                // Check UTF-8 3-byte sequence pattern
                if (b2Check in 0x80..0xBF && b3 in 0x80..0xBF) {
                    likelyUTF8++
                    i += 3
                    continue
                }
            } else if (b1 in 0xC0..0xDF && bytes.size > i + 1) {
                // UTF-8 2-byte sequence
                if (b2 in 0x80..0xBF) {
                    likelyUTF8++
                    i += 2
                    continue
                }
            }

            i++
        }

        // If GB characters are more prevalent, it's likely GB encoding
        return when {
            likelyGB > likelyUTF8 * 2 -> "GBK"
            likelyUTF8 > likelyGB * 2 -> "UTF-8"
            else -> null
        }
    }

    fun isValidEncoding(encoding: String): Boolean {
        return try {
            Charset.forName(encoding)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun convertToString(bytes: ByteArray, encoding: String): String {
        return try {
            String(bytes, Charset.forName(encoding))
        } catch (e: Exception) {
            String(bytes, StandardCharsets.UTF_8)
        }
    }
}
