package com.mochen.reader.parser

import android.content.Context
import android.net.Uri
import com.mochen.reader.domain.model.Chapter
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton
import com.mochen.reader.util.EncodingDetector

@Singleton
class TxtParser @Inject constructor() {

    private val chapterPatterns = listOf(
        Regex("^(第[一二三四五六七八九十百千万\\d]+[章节部卷集篇回])\\s*[^\\s]"),
        Regex("^(Chapter|CH|第)\\s*(\\d+|[一二三四五六七八九十]+)[^\\u4e00-\\u9fa5]", RegexOption.IGNORE_CASE),
        Regex("^(\\d{1,4})\\s*\\.[^\\d]"),
        Regex("^[IVXLCDMivxlcdm]+\\s*\\.[^\\d]", RegexOption.IGNORE_CASE),
        Regex("^【[^】]+】")
    )

    suspend fun parseChapters(context: Context, uri: Uri, bookId: Long): List<Chapter> {
        val chapters = mutableListOf<Chapter>()

        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return chapters
            val encoding = EncodingDetector.detect(inputStream.readBytes())
            inputStream.close()

            val reader = BufferedReader(
                InputStreamReader(context.contentResolver.openInputStream(uri)!!, Charset.forName(encoding))
            )

            val content = reader.readText()
            reader.close()

            val lines = content.lines()
            var currentChapterStart = 0L
            var chapterIndex = 0
            var chapterTitle = "第一章"
            var volumeIndex = 0
            var currentVolumeTitle: String? = null
            var wordCount = 0

            for ((index, line) in lines.withIndex()) {
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) continue

                val isChapterStart = chapterPatterns.any { it.containsMatchIn(trimmedLine) }

                if (isChapterStart || index == 0) {
                    if (index > 0) {
                        chapters.add(
                            Chapter(
                                bookId = bookId,
                                chapterIndex = chapterIndex,
                                title = chapterTitle,
                                volumeIndex = volumeIndex,
                                volumeTitle = currentVolumeTitle,
                                startPosition = currentChapterStart,
                                endPosition = index.toLong(),
                                wordCount = wordCount
                            )
                        )
                        chapterIndex++
                    }
                    chapterTitle = trimmedLine
                    currentChapterStart = index.toLong()
                    wordCount = 0
                }

                wordCount += trimmedLine.length
            }

            // Add last chapter
            if (lines.isNotEmpty()) {
                chapters.add(
                    Chapter(
                        bookId = bookId,
                        chapterIndex = chapterIndex,
                        title = chapterTitle,
                        volumeIndex = volumeIndex,
                        volumeTitle = currentVolumeTitle,
                        startPosition = currentChapterStart,
                        endPosition = lines.size.toLong(),
                        wordCount = wordCount
                    )
                )
            }

            // If no chapters detected, create a single chapter
            if (chapters.isEmpty()) {
                chapters.add(
                    Chapter(
                        bookId = bookId,
                        chapterIndex = 0,
                        title = "全文",
                        startPosition = 0,
                        endPosition = lines.size.toLong(),
                        wordCount = lines.joinToString("").length
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return chapters
    }

    suspend fun readChapterContent(context: Context, uri: Uri, chapter: Chapter): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val encoding = EncodingDetector.detect(inputStream.readBytes())
            inputStream.close()

            val reader = BufferedReader(
                InputStreamReader(context.contentResolver.openInputStream(uri)!!, Charset.forName(encoding))
            )

            val allLines = reader.readLines()
            reader.close()

            val start = chapter.startPosition.toInt().coerceIn(0, allLines.size)
            val end = chapter.endPosition.toInt().coerceIn(start, allLines.size)

            allLines.subList(start, end).joinToString("\n")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun readFullContent(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val encoding = EncodingDetector.detect(inputStream.readBytes())
            inputStream.close()

            val reader = BufferedReader(
                InputStreamReader(context.contentResolver.openInputStream(uri)!!, Charset.forName(encoding))
            )

            val content = reader.readText()
            reader.close()
            content
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
