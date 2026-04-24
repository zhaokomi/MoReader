package com.mochen.reader.domain.model

data class SearchResult(
    val bookId: Long,
    val bookTitle: String,
    val chapterIndex: Int,
    val chapterTitle: String,
    val keyword: String,
    val context: String,
    val position: Int
)
