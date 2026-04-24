package com.mochen.reader.parser

import android.content.Context
import android.net.Uri
import com.mochen.reader.domain.model.Chapter
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpubParser @Inject constructor() {

    suspend fun parseChapters(context: Context, uri: Uri, bookId: Long): List<Chapter> {
        val chapters = mutableListOf<Chapter>()

        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return chapters
            val epubReader = EpubReader()
            val book = epubReader.readEpub(inputStream)
            inputStream.close()

            val spine = book.spine
            val toc = book.tableOfContents

            var chapterIndex = 0
            var wordCount = 0

            for (i in 0 until spine.size()) {
                val spineReference = spine.getSpineReferences()[i]
                val resource = spineReference.resource

                if (resource != null) {
                    val chapterInputStream = resource.inputStream
                    val htmlContent = chapterInputStream.bufferedReader().readText()
                    chapterInputStream.close()

                    // Extract text content for word count
                    val textContent = Jsoup.parse(htmlContent).text()
                    wordCount = textContent.length

                    // Get title from TOC or generate one
                    val title = if (toc.tocReferences.size > i) {
                        toc.tocReferences[i].title ?: "第${i + 1}章"
                    } else {
                        "第${i + 1}章"
                    }

                    chapters.add(
                        Chapter(
                            bookId = bookId,
                            chapterIndex = chapterIndex,
                            title = title,
                            startPosition = i.toLong(),
                            endPosition = i.toLong(),
                            wordCount = wordCount
                        )
                    )
                    chapterIndex++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return chapters
    }

    suspend fun readChapterContent(context: Context, uri: Uri, chapter: Chapter): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val epubReader = EpubReader()
            val book = epubReader.readEpub(inputStream)
            inputStream.close()

            val spine = book.spine
            if (chapter.chapterIndex < spine.size()) {
                val spineReference = spine.getSpineReferences()[chapter.chapterIndex]
                val resource = spineReference.resource
                if (resource != null) {
                    val chapterInputStream = resource.inputStream
                    val htmlContent = chapterInputStream.bufferedReader().readText()
                    chapterInputStream.close()

                    // Parse HTML and extract text
                    val doc = Jsoup.parse(htmlContent)
                    // Remove scripts and styles
                    doc.select("script, style").remove()
                    // Get text content
                    doc.body().text()
                } else ""
            } else ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun extractCover(context: Context, uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val epubReader = EpubReader()
            val book = epubReader.readEpub(inputStream)
            inputStream.close()

            val coverId = book.coverId
            if (coverId != null) {
                val coverResource = book.getResource(coverId)
                if (coverResource != null) {
                    // Save cover to cache and return URI
                    val coverFile = java.io.File(context.cacheDir, "cover_${uri.hashCode()}.jpg")
                    coverFile.outputStream().use { output ->
                        coverResource.inputStream.copyTo(output)
                    }
                    Uri.fromFile(coverFile)
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getBookInfo(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val epubReader = EpubReader()
            val book = epubReader.readEpub(inputStream)
            inputStream.close()

            val title = book.title ?: "Unknown"
            val author = book.metadata.authors.joinToString(", ") { "${it.firstname} ${it.lastname}".trim() }
                .ifEmpty { "Unknown" }

            Pair(title, author)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
