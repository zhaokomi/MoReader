package com.mochen.reader.domain.repository

import com.mochen.reader.domain.model.Chapter
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {
    fun getChaptersByBookId(bookId: Long): Flow<List<Chapter>>
    suspend fun getChaptersByBookIdSync(bookId: Long): List<Chapter>
    suspend fun getChapter(bookId: Long, chapterIndex: Int): Chapter?
    suspend fun insertChapters(chapters: List<Chapter>)
    suspend fun deleteChaptersByBookId(bookId: Long)
}
