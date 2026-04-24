package com.mochen.reader.domain.model

data class Chapter(
    val id: Long = 0,
    val bookId: Long,
    val chapterIndex: Int,
    val title: String,
    val volumeIndex: Int = 0,
    val volumeTitle: String? = null,
    val startPosition: Long = 0,
    val endPosition: Long = 0,
    val wordCount: Int = 0
)

data class ChapterContent(
    val chapter: Chapter,
    val content: String,
    val pages: List<String> = emptyList()
)
