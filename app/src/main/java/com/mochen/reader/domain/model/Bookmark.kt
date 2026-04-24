package com.mochen.reader.domain.model

data class Bookmark(
    val id: Long = 0,
    val bookId: Long,
    val chapterIndex: Int,
    val chapterTitle: String,
    val position: Int,
    val content: String? = null,
    val color: BookmarkColor = BookmarkColor.RED,
    val createdAt: Long = System.currentTimeMillis(),
    val note: String? = null
)

enum class BookmarkColor(val value: Int, val colorHex: Long) {
    RED(0, 0xFFF44336),
    ORANGE(1, 0xFFFF9800),
    YELLOW(2, 0xFFFFEB3B),
    GREEN(3, 0xFF4CAF50);

    companion object {
        fun fromValue(value: Int): BookmarkColor {
            return entries.find { it.value == value } ?: RED
        }
    }
}
