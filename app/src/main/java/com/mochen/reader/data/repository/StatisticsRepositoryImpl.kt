package com.mochen.reader.data.repository

import com.mochen.reader.data.datastore.SettingsDataStore
import com.mochen.reader.data.local.dao.ReadingProgressDao
import com.mochen.reader.data.local.entity.ReadingProgressEntity
import com.mochen.reader.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val readingProgressDao: ReadingProgressDao,
    private val settingsDataStore: SettingsDataStore
) : StatisticsRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun getTodayReadingTime(): Flow<Long> {
        val today = dateFormat.format(Date())
        return readingProgressDao.getReadingTimeByDate(today).map { it ?: 0L }
    }

    override fun getTotalReadingTime(): Flow<Long> {
        return readingProgressDao.getTotalReadingTime().map { it ?: 0L }
    }

    override fun getReadingHistory(days: Int): Flow<Map<String, Long>> {
        val calendar = Calendar.getInstance()
        val dates = (0 until days).map {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -it)
            dateFormat.format(calendar.time)
        }
        calendar.time = Date()

        return readingProgressDao.getProgressByDate(dates.first()).map { progressList ->
            progressList.associate { it.date to it.readingTimeSeconds }
        }
    }

    override suspend fun addReadingTime(bookId: Long, seconds: Long) {
        val today = dateFormat.format(Date())
        val existing = readingProgressDao.getProgressByBookAndDate(bookId, today)
        if (existing != null) {
            readingProgressDao.addReadingTime(bookId, today, seconds)
        } else {
            readingProgressDao.insertProgress(
                ReadingProgressEntity(
                    bookId = bookId,
                    chapterIndex = 0,
                    position = 0,
                    progress = 0f,
                    readingTimeSeconds = seconds,
                    date = today
                )
            )
        }
    }

    override fun getDailyGoal(): Flow<Int> {
        return settingsDataStore.dailyGoalMinutes
    }

    override suspend fun setDailyGoal(minutes: Int) {
        settingsDataStore.setDailyGoalMinutes(minutes)
    }
}
