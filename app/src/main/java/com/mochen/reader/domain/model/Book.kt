package com.mochen.reader.domain.model

data class Book(
    val id: Long = 0,
    val title: String,
    val author: String = "未知作者",
    val filePath: String,
    val coverPath: String? = null,
    val format: BookFormat,
    val fileSize: Long = 0,
    val importTime: Long = System.currentTimeMillis(),
    val lastReadTime: Long = 0,
    val lastReadChapter: Int = 0,
    val lastReadPosition: Int = 0,
    val totalChapters: Int = 0,
    val totalWords: Int = 0,
    val currentProgress: Float = 0f,
    val encoding: String = "UTF-8",
    val groupId: Long = 0,
    val isFavorite: Boolean = false
) {
    val progressPercentage: Int
        get() = (currentProgress * 100).toInt()

    val formattedFileSize: String
        get() {
            return when {
                fileSize < 1024 -> "$fileSize B"
                fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
                else -> String.format("%.1f MB", fileSize / (1024.0 * 1024.0))
            }
        }
}

enum class BookFormat(val extension: String, val mimeType: String) {
    TXT("txt", "text/plain"),
    EPUB("epub", "application/epub+zip"),
    MOBI("mobi", "application/x-mobipocket-ebook"),
    AZW3("azw3", "application/x-azw3"),
    PDF("pdf", "application/pdf"),
    UNKNOWN("", "");

    companion object {
        fun fromExtension(ext: String): BookFormat {
            return entries.find { it.extension.equals(ext, ignoreCase = true) } ?: UNKNOWN
        }

        fun fromMimeType(mimeType: String): BookFormat {
            return entries.find { it.mimeType == mimeType } ?: UNKNOWN
        }
    }
}
