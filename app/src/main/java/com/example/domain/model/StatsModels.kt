package com.example.domain.model

data class DayMetricPoint(
    val dateLabel: String,
    val epochDay: Long,
    val tasksCompleted: Int,
    val habitsCompleted: Int,
    val waterMl: Int,
    val studyMinutes: Int,
    val focusMinutes: Int
)

data class WeeklyStatsSummary(
    val weekStartEpochDay: Long,
    val weekEndEpochDay: Long,
    val totalTasksCompleted: Int,
    val totalTasksCreated: Int,
    val taskCompletionRate: Float?,
    val habitsConsistencyRate: Float?,
    val totalHabitsLogged: Int,
    val activeHabitsCount: Int,
    val totalStudyMinutes: Int,
    val totalFocusMinutes: Int,
    val averageWaterMl: Int,
    val dailyPoints: List<DayMetricPoint>,
    val activeGoalsCount: Int,
    val activeProjectsCount: Int
)

data class WeeklyReviewItem(
    val id: String,
    val weekStartEpochDay: Long,
    val weekEndEpochDay: Long,
    val accomplishments: String,
    val challenges: String,
    val nextWeekFocus: String,
    val notes: String = "",
    val energyRating: Int = 3,
    val productivityRating: Int = 3,
    val createdAt: Long = System.currentTimeMillis()
)
