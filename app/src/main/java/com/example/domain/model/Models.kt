package com.example.domain.model

data class TaskItem(
    val id: String,
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
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
) {
    val isCompleted: Boolean get() = status == TaskStatus.COMPLETED
}

data class ProjectItem(
    val id: String,
    val name: String,
    val description: String = "",
    val colorHex: String = "#2563EB",
    val category: String = "General",
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    val targetDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class HabitItem(
    val id: String,
    val title: String,
    val description: String = "",
    val type: HabitType = HabitType.BOOLEAN,
    val targetValue: Double = 1.0,
    val unit: String = "veces",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val isPositive: Boolean = true,
    val colorHex: String = "#059669",
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

data class HabitLogItem(
    val id: String,
    val habitId: String,
    val dateEpochDay: Long,
    val value: Double = 1.0,
    val completed: Boolean = true,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val category: String = "Ideas",
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val linkedTaskId: String? = null,
    val linkedProjectId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class WellnessRecord(
    val id: String,
    val type: WellnessType,
    val numericValue: Double,
    val unit: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateEpochDay: Long
)

data class DailySummary(
    val totalPendingTasks: Int,
    val completedTasksToday: Int,
    val activeHabitsCount: Int,
    val completedHabitsToday: Int,
    val waterIntakeMl: Int,
    val waterGoalMl: Int = 2000,
    val notesCount: Int
)

data class UserPreferences(
    val themeMode: String = "SYSTEM",
    val startOfWeekMonday: Boolean = true,
    val waterGoalMl: Int = 2000,
    val enabledHomeSections: List<HomeSectionId> = HomeSectionId.defaultSections(),
    val userName: String = ""
)

data class ProjectDetail(
    val project: ProjectItem,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val progress: Float = 0f
)

data class RoutineItem(
    val id: String,
    val title: String,
    val description: String = "",
    val timeOfDaySlot: TimeOfDaySlot = TimeOfDaySlot.MORNING,
    val estimatedMinutes: Int = 15,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class RoutineStepItem(
    val id: String,
    val routineId: String,
    val title: String,
    val description: String = "",
    val durationMinutes: Int = 5,
    val orderIndex: Int = 0,
    val habitId: String? = null,
    val isCompleted: Boolean = false,
    val isSkipped: Boolean = false
)

data class RoutineWithSteps(
    val routine: RoutineItem,
    val steps: List<RoutineStepItem> = emptyList()
) {
    val totalDurationMinutes: Int get() = steps.sumOf { it.durationMinutes }
    val completedStepsCount: Int get() = steps.count { it.isCompleted }
    val progress: Float get() = if (steps.isEmpty()) 0f else completedStepsCount.toFloat() / steps.size.toFloat()
    val isFullyCompleted: Boolean get() = steps.isNotEmpty() && steps.all { it.isCompleted || it.isSkipped }
}

data class CalendarEventItem(
    val id: String,
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

data class ScheduleEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeLabel: String,
    val isTaskDeadline: Boolean,
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val colorHex: String = "#2563EB",
    val originalTaskId: String? = null,
    val originalEventId: String? = null
)

data class DiaryEntryItem(
    val id: String,
    val dateEpochDay: Long,
    val title: String,
    val content: String,
    val derivedSummary: String = "",
    val mood: String = "Neutral",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class GoalItem(
    val id: String,
    val title: String,
    val description: String = "",
    val timeHorizon: GoalHorizon = GoalHorizon.MEDIUM_TERM,
    val category: String = "Personal",
    val status: GoalStatus = GoalStatus.ACTIVE,
    val targetDate: Long? = null,
    val metricName: String = "",
    val startValue: Double = 0.0,
    val currentValue: Double = 0.0,
    val targetValue: Double = 100.0,
    val unit: String = "%",
    val linkedProjectId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class GoalMilestoneItem(
    val id: String,
    val goalId: String,
    val title: String,
    val targetDate: Long? = null,
    val isAchieved: Boolean = false,
    val orderIndex: Int = 0,
    val achievedAt: Long? = null
)

data class GoalDetail(
    val goal: GoalItem,
    val milestones: List<GoalMilestoneItem> = emptyList(),
    val progress: Float = 0f
)

data class LearningTopicItem(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "Tecnología",
    val status: LearningStatus = LearningStatus.IN_PROGRESS,
    val totalStudyMinutes: Int = 0,
    val resourcesCount: Int = 0,
    val linkedGoalId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalHours: Float get() = (totalStudyMinutes / 60f)
}

data class LearningSessionItem(
    val id: String,
    val topicId: String,
    val durationMinutes: Int,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateEpochDay: Long
)

data class PersonItem(
    val id: String,
    val fullName: String,
    val alias: String = "",
    val category: PersonCategory = PersonCategory.OTHER,
    val importantDatesNotes: String = "",
    val notes: String = "",
    val nextAction: String = "",
    val createdAt: Long = System.currentTimeMillis()
)


