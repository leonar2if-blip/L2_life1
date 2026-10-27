package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["status"]),
        Index(value = ["dueDate"]),
        Index(value = ["projectId"])
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val priority: String = "MEDIUM",
    val status: String = "PENDING",
    val energyLevel: String = "MEDIUM",
    val category: String = "Personal",
    val estimatedMinutes: Int = 0,
    val dueDate: Long? = null,
    val scheduledDate: Long? = null,
    val projectId: String? = null,
    val parentTaskId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val colorHex: String = "#2563EB",
    val category: String = "General",
    val status: String = "ACTIVE",
    val targetDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val type: String = "BOOLEAN",
    val targetValue: Double = 1.0,
    val unit: String = "veces",
    val frequency: String = "DAILY",
    val isPositive: Boolean = true,
    val colorHex: String = "#059669",
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["habitId", "dateEpochDay"], unique = true)
    ]
)
data class HabitLogEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val dateEpochDay: Long,
    val value: Double = 1.0,
    val completed: Boolean = true,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val category: String = "Ideas",
    val tagsCsv: String = "",
    val isPinned: Boolean = false,
    val linkedTaskId: String? = null,
    val linkedProjectId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "wellness_logs",
    indices = [
        Index(value = ["type", "dateEpochDay"])
    ]
)
data class WellnessLogEntity(
    @PrimaryKey val id: String,
    val type: String,
    val numericValue: Double,
    val unit: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateEpochDay: Long
)

@Entity(tableName = "user_preferences")
data class PreferenceEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val timeOfDaySlot: String = "MORNING",
    val estimatedMinutes: Int = 15,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "routine_steps",
    indices = [
        Index(value = ["routineId", "orderIndex"])
    ]
)
data class RoutineStepEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val title: String,
    val description: String = "",
    val durationMinutes: Int = 5,
    val orderIndex: Int = 0,
    val habitId: String? = null,
    val isCompleted: Boolean = false,
    val isSkipped: Boolean = false
)

@Entity(
    tableName = "calendar_events",
    indices = [
        Index(value = ["startTime", "endTime"])
    ]
)
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val category: String = "General",
    val startTime: Long,
    val endTime: Long,
    val isAllDay: Boolean = false,
    val linkedTaskId: String? = null,
    val linkedProjectId: String? = null,
    val colorHex: String = "#2563EB",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "diary_entries",
    indices = [
        Index(value = ["dateEpochDay"], unique = true)
    ]
)
data class DiaryEntryEntity(
    @PrimaryKey val id: String,
    val dateEpochDay: Long,
    val title: String,
    val content: String,
    val derivedSummary: String = "",
    val mood: String = "Neutral",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "goals",
    indices = [
        Index(value = ["timeHorizon"]),
        Index(value = ["status"])
    ]
)
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val timeHorizon: String = "MEDIUM_TERM",
    val category: String = "Personal",
    val status: String = "ACTIVE",
    val targetDate: Long? = null,
    val metricName: String = "",
    val startValue: Double = 0.0,
    val currentValue: Double = 0.0,
    val targetValue: Double = 100.0,
    val unit: String = "%",
    val linkedProjectId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "goal_milestones",
    indices = [
        Index(value = ["goalId", "orderIndex"])
    ]
)
data class GoalMilestoneEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val title: String,
    val targetDate: Long? = null,
    val isAchieved: Boolean = false,
    val orderIndex: Int = 0,
    val achievedAt: Long? = null
)

@Entity(tableName = "learning_topics")
data class LearningTopicEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val category: String = "Tecnología",
    val status: String = "IN_PROGRESS",
    val totalStudyMinutes: Int = 0,
    val resourcesCount: Int = 0,
    val linkedGoalId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "learning_sessions",
    indices = [
        Index(value = ["topicId"])
    ]
)
data class LearningSessionEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val durationMinutes: Int,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateEpochDay: Long
)

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val alias: String = "",
    val category: String = "OTHER",
    val importantDatesNotes: String = "",
    val notes: String = "",
    val nextAction: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "focus_sessions",
    indices = [
        Index(value = ["dateEpochDay"]),
        Index(value = ["timestamp"])
    ]
)
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val mode: String = "WORK",
    val durationMinutes: Int = 25,
    val title: String = "Sesión de Enfoque",
    val linkedTaskId: String? = null,
    val linkedTopicId: String? = null,
    val notes: String = "",
    val completed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val dateEpochDay: Long
)

@Entity(
    tableName = "weekly_reviews",
    indices = [
        Index(value = ["weekStartEpochDay"], unique = true)
    ]
)
data class WeeklyReviewEntity(
    @PrimaryKey val id: String,
    val weekStartEpochDay: Long,
    val weekEndEpochDay: Long,
    val accomplishments: String = "",
    val challenges: String = "",
    val nextWeekFocus: String = "",
    val notes: String = "",
    val energyRating: Int = 3,
    val productivityRating: Int = 3,
    val createdAt: Long = System.currentTimeMillis()
)
