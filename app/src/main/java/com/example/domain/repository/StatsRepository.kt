package com.example.domain.repository

import com.example.domain.model.WeeklyReviewItem
import com.example.domain.model.WeeklyStatsSummary
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun observeWeeklyStats(startEpochDay: Long, endEpochDay: Long): Flow<WeeklyStatsSummary>
    fun observeAllReviews(): Flow<List<WeeklyReviewItem>>
    fun observeReviewForWeek(weekStartEpochDay: Long): Flow<WeeklyReviewItem?>
    suspend fun saveReview(review: WeeklyReviewItem)
    suspend fun deleteReview(id: String)
    suspend fun generateReviewDraft(weekStartEpochDay: Long, weekEndEpochDay: Long): String
}
