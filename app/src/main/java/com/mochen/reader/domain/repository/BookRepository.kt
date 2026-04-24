package com.mochen.reader.domain.repository

import com.mochen.reader.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    fun getRecentBooks(limit: Int = 5): Flow<List<Book>>
    fun getBooksByGroup(groupId: Long): Flow<List<Book>>
    fun getBookById(id: Long): Flow<Book?>
    fun searchBooks(query: String): Flow<List<Book>>
    suspend fun insertBook(book: Book): Long
    suspend fun updateBook(book: Book)
    suspend fun updateReadingProgress(bookId: Long, chapterIndex: Int, position: Int, progress: Float)
    suspend fun updateCover(bookId: Long, coverPath: String)
    suspend fun updateGroup(bookId: Long, groupId: Long)
    suspend fun updateFavorite(bookId: Long, isFavorite: Boolean)
    suspend fun deleteBook(bookId: Long)
    suspend fun getBookByPath(filePath: String): Book?
    fun getTotalBooksCount(): Flow<Int>
    fun getTotalWordsRead(): Flow<Int>
}
