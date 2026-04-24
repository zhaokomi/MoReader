package com.mochen.reader.parser

import android.content.Context
import android.net.Uri
import com.mochen.reader.domain.model.Book
import com.mochen.reader.domain.model.BookFormat
import com.mochen.reader.domain.model.Chapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookParser @Inject constructor(
    private val txtParser: TxtParser,
    private val epubParser: EpubParser,
    private val pdfParser: PdfParser
) {
    fun parseBook(context: Context, uri: Uri): Book? {
        val contentResolver = context.contentResolver
        val fileName = getFileName(context, uri) ?: return null
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val format = BookFormat.fromExtension(extension)

        val fileSize = try {
            contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        } catch (e: Exception) {
            0L
        }

        val title = fileName.substringBeforeLast('.')

        return Book(
            title = title,
            filePath = uri.toString(),
            format = format,
            fileSize = fileSize
        )
    }

    suspend fun parseChapters(context: Context, uri: Uri, bookId: Long): List<Chapter> {
        val fileName = getFileName(context, uri) ?: return emptyList()
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val format = BookFormat.fromExtension(extension)

        return when (format) {
            BookFormat.TXT -> txtParser.parseChapters(context, uri, bookId)
            BookFormat.EPUB -> epubParser.parseChapters(context, uri, bookId)
            BookFormat.PDF -> pdfParser.parseChapters(context, uri, bookId)
            else -> emptyList()
        }
    }

    suspend fun readChapterContent(context: Context, uri: Uri, chapter: Chapter): String {
        val fileName = getFileName(context, uri) ?: return ""
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val format = BookFormat.fromExtension(extension)

        return when (format) {
            BookFormat.TXT -> txtParser.readChapterContent(context, uri, chapter)
            BookFormat.EPUB -> epubParser.readChapterContent(context, uri, chapter)
            BookFormat.PDF -> pdfParser.readChapterContent(context, uri, chapter)
            else -> ""
        }
    }

    fun extractCover(context: Context, uri: Uri): Uri? {
        val fileName = getFileName(context, uri) ?: return null
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val format = BookFormat.fromExtension(extension)

        return when (format) {
            BookFormat.EPUB -> epubParser.extractCover(context, uri)
            else -> null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else null
        }
    }
}
