package com.example.domain.model

data class FocusSessionItem(
    val id: String,
    val mode: FocusMode,
    val durationMinutes: Int,
    val title: String,
    val linkedTaskId: String? = null,
    val linkedTaskTitle: String? = null,
    val linkedTopicId: String? = null,
    val linkedTopicTitle: String? = null,
    val notes: String = "",
    val completed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val dateEpochDay: Long
)

data class FocusTimerState(
    val mode: FocusMode = FocusMode.WORK,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val timerStatus: FocusTimerStatus = FocusTimerStatus.IDLE,
    val linkedTaskId: String? = null,
    val linkedTaskTitle: String? = null,
    val linkedTopicId: String? = null,
    val linkedTopicTitle: String? = null,
    val customMinutes: Int = 25,
    val sessionsCompletedToday: Int = 0,
    val totalFocusMinutesToday: Int = 0
)
