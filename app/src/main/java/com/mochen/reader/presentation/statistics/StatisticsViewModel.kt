package com.mochen.reader.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochen.reader.domain.model.ReadingStatistics
import com.mochen.reader.domain.repository.BookRepository
import com.mochen.reader.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class StatisticsUiState(
    val statistics: ReadingStatistics = ReadingStatistics(),
    val readingHistory: Map<String, Long> = emptyMap(),
    val isLoading: Boolean = false
)

private data class FiveTuple<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                bookRepository.getTotalBooksCount(),
                bookRepository.getTotalWordsRead(),
                statisticsRepository.getTodayReadingTime(),
                statisticsRepository.getTotalReadingTime(),
                statisticsRepository.getDailyGoal()
            ) { totalBooks, totalWords, todayTime, totalTime, dailyGoal ->
                FiveTuple(totalBooks, totalWords, todayTime, totalTime, dailyGoal)
            }.combine(statisticsRepository.getReadingHistory(30)) { tuple, history ->
                ReadingStatistics(
                    totalBooksRead = tuple.a,
                    totalReadingTimeSeconds = tuple.d,
                    totalWordsRead = tuple.b,
                    todayReadingTimeSeconds = tuple.c,
                    dailyGoalMinutes = tuple.e,
                    dailyReadingHistory = history
                )
            }.collect { stats ->
                _uiState.update {
                    it.copy(
                        statistics = stats,
                        readingHistory = stats.dailyReadingHistory,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setDailyGoal(minutes: Int) {
        viewModelScope.launch {
            statisticsRepository.setDailyGoal(minutes)
        }
    }
}
