package com.mochen.reader.data.repository

import com.mochen.reader.data.local.dao.BookDao
import com.mochen.reader.data.local.entity.BookEntity
import com.mochen.reader.domain.model.Book
import com.mochen.reader.domain.model.BookFormat
import com.mochen.reader.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentBooks(limit: Int): Flow<List<Book>> {
        return bookDao.getRecentBooks(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBooksByGroup(groupId: Long): Flow<List<Book>> {
        return bookDao.getBooksByGroup(groupId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBookById(id: Long): Flow<Book?> {
        return bookDao.getBookByIdFlow(id).map { it?.toDomain() }
    }

    override fun searchBooks(query: String): Flow<List<Book>> {
        return bookDao.searchBooks(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book.toEntity())
    }

    override suspend fun updateBook(book: Book) {
        bookDao.updateBook(book.toEntity())
    }

    override suspend fun updateReadingProgress(bookId: Long, chapterIndex: Int, position: Int, progress: Float) {
        bookDao.updateReadingProgress(
            bookId = bookId,
            lastReadTime = System.currentTimeMillis(),
            chapterIndex = chapterIndex,
            position = position,
            progress = progress
        )
    }

    override suspend fun updateCover(bookId: Long, coverPath: String) {
        bookDao.updateCover(bookId, coverPath)
    }

    override suspend fun updateGroup(bookId: Long, groupId: Long) {
        bookDao.updateGroup(bookId, groupId)
    }

    override suspend fun updateFavorite(bookId: Long, isFavorite: Boolean) {
        bookDao.updateFavorite(bookId, isFavorite)
    }

    override suspend fun deleteBook(bookId: Long) {
        bookDao.softDelete(bookId)
    }

    override suspend fun getBookByPath(filePath: String): Book? {
        return bookDao.getBookByPath(filePath)?.toDomain()
    }

    override fun getTotalBooksCount(): Flow<Int> {
        return bookDao.getTotalBooksCount()
    }

    override fun getTotalWordsRead(): Flow<Int> {
        return bookDao.getTotalWordsRead().map { it ?: 0 }
    }

    private fun BookEntity.toDomain(): Book {
        return Book(
            id = id,
            title = title,
            author = author,
            filePath = filePath,
            coverPath = coverPath,
            format = BookFormat.fromExtension(format),
            fileSize = fileSize,
            importTime = importTime,
            lastReadTime = lastReadTime,
            lastReadChapter = lastReadChapter,
            lastReadPosition = lastReadPosition,
            totalChapters = totalChapters,
            totalWords = totalWords,
            currentProgress = currentProgress,
            encoding = encoding,
            groupId = groupId,
            isFavorite = isFavorite
        )
    }

    private fun Book.toEntity(): BookEntity {
        return BookEntity(
            id = id,
            title = title,
            author = author,
            filePath = filePath,
            coverPath = coverPath,
            format = format.extension,
            fileSize = fileSize,
            importTime = importTime,
            lastReadTime = lastReadTime,
            lastReadChapter = lastReadChapter,
            lastReadPosition = lastReadPosition,
            totalChapters = totalChapters,
            totalWords = totalWords,
            currentProgress = currentProgress,
            encoding = encoding,
            groupId = groupId,
            isFavorite = isFavorite
        )
    }
}
