package com.mochen.reader.domain.model

data class ReadingStatistics(
    val totalBooksRead: Int = 0,
    val totalReadingTimeSeconds: Long = 0,
    val totalWordsRead: Int = 0,
    val todayReadingTimeSeconds: Long = 0,
    val dailyGoalMinutes: Int = 30,
    val dailyReadingHistory: Map<String, Long> = emptyMap() // date -> seconds
) {
    val todayGoalProgress: Float
        get() = if (dailyGoalMinutes > 0) {
            (todayReadingTimeSeconds / 60f) / dailyGoalMinutes
        } else 0f

    val isGoalReached: Boolean
        get() = (todayReadingTimeSeconds / 60) >= dailyGoalMinutes

    val formattedTotalTime: String
        get() {
            val hours = totalReadingTimeSeconds / 3600
            val minutes = (totalReadingTimeSeconds % 3600) / 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }

    val formattedTodayTime: String
        get() {
            val minutes = todayReadingTimeSeconds / 60
            val seconds = todayReadingTimeSeconds % 60
            return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
        }
}
