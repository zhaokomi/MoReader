package com.mochen.reader.domain.repository

import com.mochen.reader.domain.model.ReadingStatistics
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun getTodayReadingTime(): Flow<Long>
    fun getTotalReadingTime(): Flow<Long>
    fun getReadingHistory(days: Int = 30): Flow<Map<String, Long>>
    suspend fun addReadingTime(bookId: Long, seconds: Long)
    fun getDailyGoal(): Flow<Int>
    suspend fun setDailyGoal(minutes: Int)
}
