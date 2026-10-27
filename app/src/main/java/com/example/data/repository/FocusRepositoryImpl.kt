package com.example.data.repository

import com.example.data.local.dao.FocusDao
import com.example.data.local.dao.LearningDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.FocusSessionEntity
import com.example.data.local.entity.LearningSessionEntity
import com.example.domain.model.FocusMode
import com.example.domain.model.FocusSessionItem
import com.example.domain.model.TaskStatus
import com.example.domain.repository.FocusRepository
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FocusRepositoryImpl(
    private val focusDao: FocusDao,
    private val taskDao: TaskDao,
    private val learningDao: LearningDao
) : FocusRepository {

    private val currentEpochDay: Long
        get() = LocalDate.now().toEpochDay()

    override fun observeAllSessions(): Flow<List<FocusSessionItem>> {
        return focusDao.observeAllSessions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeTodaySessions(): Flow<List<FocusSessionItem>> {
        return focusDao.observeSessionsForDate(currentEpochDay).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeTodayFocusMinutes(): Flow<Int> {
        return focusDao.observeTodayFocusMinutes(currentEpochDay)
    }

    override fun observeTodayCompletedCount(): Flow<Int> {
        return focusDao.observeTodayCompletedCount(currentEpochDay)
    }

    override suspend fun recordFocusSession(
        mode: FocusMode,
        durationMinutes: Int,
        title: String,
        linkedTaskId: String?,
        linkedTopicId: String?,
        notes: String,
        markTaskCompleted: Boolean
    ): FocusSessionItem {
        val now = System.currentTimeMillis()
        val epochDay = LocalDate.now().toEpochDay()
        val sessionId = UUID.randomUUID().toString()

        var taskTitle: String? = null
        if (!linkedTaskId.isNullOrBlank()) {
            val task = taskDao.getTaskById(linkedTaskId)
            taskTitle = task?.title
            if (markTaskCompleted && task != null) {
                taskDao.updateTaskStatus(linkedTaskId, TaskStatus.COMPLETED.name, now)
            }
        }

        var topicTitle: String? = null
        if (!linkedTopicId.isNullOrBlank()) {
            val topic = learningDao.getTopicById(linkedTopicId)
            topicTitle = topic?.title
            if (mode == FocusMode.WORK && durationMinutes > 0) {
                learningDao.addStudyMinutes(linkedTopicId, durationMinutes, now)
                learningDao.insertSession(
                    LearningSessionEntity(
                        id = "focus_$sessionId",
                        topicId = linkedTopicId,
                        durationMinutes = durationMinutes,
                        notes = "Sesión de enfoque Pomodoro: $title",
                        timestamp = now,
                        dateEpochDay = epochDay
                    )
                )
            }
        }

        val sessionEntity = FocusSessionEntity(
            id = sessionId,
            mode = mode.name,
            durationMinutes = durationMinutes,
            title = if (title.isBlank()) mode.labelEs else title,
            linkedTaskId = linkedTaskId,
            linkedTopicId = linkedTopicId,
            notes = notes,
            completed = true,
            timestamp = now,
            dateEpochDay = epochDay
        )

        focusDao.insertSession(sessionEntity)

        return FocusSessionItem(
            id = sessionId,
            mode = mode,
            durationMinutes = durationMinutes,
            title = sessionEntity.title,
            linkedTaskId = linkedTaskId,
            linkedTaskTitle = taskTitle,
            linkedTopicId = linkedTopicId,
            linkedTopicTitle = topicTitle,
            notes = notes,
            completed = true,
            timestamp = now,
            dateEpochDay = epochDay
        )
    }

    override suspend fun deleteSession(id: String) {
        val session = focusDao.getSessionById(id)
        if (session != null) {
            val topicId = session.linkedTopicId
            if (!topicId.isNullOrBlank() && session.mode == FocusMode.WORK.name && session.durationMinutes > 0) {
                learningDao.subtractStudyMinutes(topicId, session.durationMinutes, System.currentTimeMillis())
                learningDao.deleteSessionById("focus_${session.id}")
            }
            // Coherent policy for tasks: tasks marked completed are kept completed
            // to avoid corrupting task state if completed legitimately.
            focusDao.deleteSessionById(id)
        }
    }

    private fun FocusSessionEntity.toDomain(): FocusSessionItem = FocusSessionItem(
        id = id,
        mode = FocusMode.fromString(mode),
        durationMinutes = durationMinutes,
        title = title,
        linkedTaskId = linkedTaskId,
        linkedTopicId = linkedTopicId,
        notes = notes,
        completed = completed,
        timestamp = timestamp,
        dateEpochDay = dateEpochDay
    )
}
