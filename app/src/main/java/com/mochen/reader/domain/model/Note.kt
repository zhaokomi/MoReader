package com.mochen.reader.domain.model

data class Note(
    val id: Long = 0,
    val bookId: Long,
    val chapterIndex: Int,
    val chapterTitle: String,
    val startPosition: Int,
    val endPosition: Int,
    val highlightedText: String,
    val note: String,
    val highlightColor: HighlightColor = HighlightColor.YELLOW,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class HighlightColor(val value: Int, val colorHex: Long) {
    YELLOW(0, 0xFFFFEB3B),
    GREEN(1, 0xFF4CAF50),
    BLUE(2, 0xFF2196F3),
    PINK(3, 0xFFE91E63);

    companion object {
        fun fromValue(value: Int): HighlightColor {
            return entries.find { it.value == value } ?: YELLOW
        }
    }
}
