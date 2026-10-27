package com.example.domain.repository

import com.example.domain.model.FocusMode
import com.example.domain.model.FocusSessionItem
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    fun observeAllSessions(): Flow<List<FocusSessionItem>>
    fun observeTodaySessions(): Flow<List<FocusSessionItem>>
    fun observeTodayFocusMinutes(): Flow<Int>
    fun observeTodayCompletedCount(): Flow<Int>
    suspend fun recordFocusSession(
        mode: FocusMode,
        durationMinutes: Int,
        title: String,
        linkedTaskId: String?,
        linkedTopicId: String?,
        notes: String,
        markTaskCompleted: Boolean
    ): FocusSessionItem
    suspend fun deleteSession(id: String)
}
