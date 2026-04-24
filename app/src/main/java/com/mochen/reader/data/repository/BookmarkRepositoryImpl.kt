package com.mochen.reader.data.repository

import com.mochen.reader.data.local.dao.BookmarkDao
import com.mochen.reader.data.local.entity.BookmarkEntity
import com.mochen.reader.domain.model.Bookmark
import com.mochen.reader.domain.model.BookmarkColor
import com.mochen.reader.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {

    override fun getBookmarksByBookId(bookId: Long): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByBookId(bookId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertBookmark(bookmark: Bookmark): Long {
        return bookmarkDao.insertBookmark(bookmark.toEntity())
    }

    override suspend fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark.toEntity())
    }

    override suspend fun deleteBookmark(id: Long) {
        bookmarkDao.deleteBookmarkById(id)
    }

    override suspend fun deleteBookmarksByBookId(bookId: Long) {
        bookmarkDao.deleteBookmarksByBookId(bookId)
    }

    private fun BookmarkEntity.toDomain(): Bookmark {
        return Bookmark(
            id = id,
            bookId = bookId,
            chapterIndex = chapterIndex,
            chapterTitle = chapterTitle,
            position = position,
            content = content,
            color = BookmarkColor.fromValue(color),
            createdAt = createdAt,
            note = note
        )
    }

    private fun Bookmark.toEntity(): BookmarkEntity {
        return BookmarkEntity(
            id = id,
            bookId = bookId,
            chapterIndex = chapterIndex,
            chapterTitle = chapterTitle,
            position = position,
            content = content,
            color = color.value,
            createdAt = createdAt,
            note = note
        )
    }
}
