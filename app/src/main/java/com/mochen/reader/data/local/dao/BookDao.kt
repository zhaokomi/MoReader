package com.mochen.reader.data.local.dao

import androidx.room.*
import com.mochen.reader.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books WHERE deletedAt IS NULL ORDER BY lastReadTime DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE deletedAt IS NULL ORDER BY lastReadTime DESC LIMIT :limit")
    fun getRecentBooks(limit: Int = 5): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE groupId = :groupId AND deletedAt IS NULL ORDER BY lastReadTime DESC")
    fun getBooksByGroup(groupId: Long): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id AND deletedAt IS NULL")
    suspend fun getBookById(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE id = :id AND deletedAt IS NULL")
    fun getBookByIdFlow(id: Long): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' AND deletedAt IS NULL")
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET lastReadTime = :lastReadTime, lastReadChapter = :chapterIndex, lastReadPosition = :position, currentProgress = :progress WHERE id = :bookId")
    suspend fun updateReadingProgress(bookId: Long, lastReadTime: Long, chapterIndex: Int, position: Int, progress: Float)

    @Query("UPDATE books SET coverPath = :coverPath WHERE id = :bookId")
    suspend fun updateCover(bookId: Long, coverPath: String)

    @Query("UPDATE books SET groupId = :groupId WHERE id = :bookId")
    suspend fun updateGroup(bookId: Long, groupId: Long)

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :bookId")
    suspend fun updateFavorite(bookId: Long, isFavorite: Boolean)

    @Query("UPDATE books SET deletedAt = :deletedAt WHERE id = :bookId")
    suspend fun softDelete(bookId: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBook(bookId: Long)

    @Query("SELECT * FROM books WHERE filePath = :filePath AND deletedAt IS NULL")
    suspend fun getBookByPath(filePath: String): BookEntity?

    @Query("SELECT COUNT(*) FROM books WHERE deletedAt IS NULL")
    fun getTotalBooksCount(): Flow<Int>

    @Query("SELECT SUM(currentProgress * totalWords) FROM books WHERE deletedAt IS NULL")
    fun getTotalWordsRead(): Flow<Int?>
}
