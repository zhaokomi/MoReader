package com.mochen.reader.parser

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.mochen.reader.domain.model.Chapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfParser @Inject constructor() {

    suspend fun parseChapters(context: Context, uri: Uri, bookId: Long): List<Chapter> {
        val chapters = mutableListOf<Chapter>()

        try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return chapters
            val renderer = PdfRenderer(fileDescriptor)
            val pageCount = renderer.pageCount

            // For PDF, each page is treated as a chapter
            for (i in 0 until pageCount) {
                chapters.add(
                    Chapter(
                        bookId = bookId,
                        chapterIndex = i,
                        title = "第${i + 1}页",
                        startPosition = i.toLong(),
                        endPosition = i.toLong(),
                        wordCount = 0 // PDF text extraction is complex, leave as 0
                    )
                )
            }

            renderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return chapters
    }

    suspend fun readChapterContent(context: Context, uri: Uri, chapter: Chapter): String {
        // For PDF, content is rendered as images, so we return page info
        return "第 ${chapter.chapterIndex + 1} 页"
    }

    suspend fun getPageCount(context: Context, uri: Uri): Int {
        return try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return 0
            val renderer = PdfRenderer(fileDescriptor)
            val count = renderer.pageCount
            renderer.close()
            fileDescriptor.close()
            count
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun renderPage(context: Context, uri: Uri, pageIndex: Int, width: Int, height: Int): android.graphics.Bitmap? {
        return try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            val renderer = PdfRenderer(fileDescriptor)

            if (pageIndex >= renderer.pageCount) {
                renderer.close()
                fileDescriptor.close()
                return null
            }

            val page = renderer.openPage(pageIndex)

            // Calculate scale to fit desired dimensions
            val scaleX = width.toFloat() / page.width
            val scaleY = height.toFloat() / page.height
            val scale = minOf(scaleX, scaleY)

            val bitmap = android.graphics.Bitmap.createBitmap(
                (page.width * scale).toInt(),
                (page.height * scale).toInt(),
                android.graphics.Bitmap.Config.ARGB_8888
            )

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            renderer.close()
            fileDescriptor.close()

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
