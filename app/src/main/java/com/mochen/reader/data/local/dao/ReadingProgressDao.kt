package com.mochen.reader.data.local.dao

import androidx.room.*
import com.mochen.reader.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId AND date = :date")
    suspend fun getProgressByBookAndDate(bookId: Long, date: String): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress WHERE date = :date")
    fun getProgressByDate(date: String): Flow<List<ReadingProgressEntity>>

    @Query("SELECT SUM(readingTimeSeconds) FROM reading_progress WHERE date = :date")
    fun getReadingTimeByDate(date: String): Flow<Long?>

    @Query("SELECT SUM(readingTimeSeconds) FROM reading_progress")
    fun getTotalReadingTime(): Flow<Long?>

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestProgress(bookId: Long): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId ORDER BY date DESC")
    fun getProgressHistory(bookId: Long): Flow<List<ReadingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ReadingProgressEntity): Long

    @Update
    suspend fun updateProgress(progress: ReadingProgressEntity)

    @Query("UPDATE reading_progress SET readingTimeSeconds = readingTimeSeconds + :seconds, updatedAt = :updatedAt WHERE bookId = :bookId AND date = :date")
    suspend fun addReadingTime(bookId: Long, date: String, seconds: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM reading_progress WHERE bookId = :bookId")
    suspend fun deleteProgressByBookId(bookId: Long)
}
