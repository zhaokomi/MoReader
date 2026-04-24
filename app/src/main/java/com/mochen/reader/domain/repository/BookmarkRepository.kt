package com.mochen.reader.domain.repository

import com.mochen.reader.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getBookmarksByBookId(bookId: Long): Flow<List<Bookmark>>
    fun getAllBookmarks(): Flow<List<Bookmark>>
    suspend fun insertBookmark(bookmark: Bookmark): Long
    suspend fun updateBookmark(bookmark: Bookmark)
    suspend fun deleteBookmark(id: Long)
    suspend fun deleteBookmarksByBookId(bookId: Long)
}
