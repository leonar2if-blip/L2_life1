package com.example.domain.repository

import com.example.domain.model.CalendarEventItem
import com.example.domain.model.DailySummary
import com.example.domain.model.HabitItem
import com.example.domain.model.HabitLogItem
import com.example.domain.model.HomeSectionId
import com.example.domain.model.NoteItem
import com.example.domain.model.Priority
import com.example.domain.model.ProjectDetail
import com.example.domain.model.ProjectItem
import com.example.domain.model.ProjectStatus
import com.example.domain.model.RoutineItem
import com.example.domain.model.RoutineWithSteps
import com.example.domain.model.ScheduleEntry
import com.example.domain.model.TaskItem
import com.example.domain.model.TaskStatus
import com.example.domain.model.TimeOfDaySlot
import com.example.domain.model.UserPreferences
import com.example.domain.model.WellnessRecord
import com.example.ui.theme.ThemeSetting
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeAllTasks(): Flow<List<TaskItem>>
    fun observePendingTasks(): Flow<List<TaskItem>>
    fun observeTasksForToday(): Flow<List<TaskItem>>
    fun observeTasksForProject(projectId: String): Flow<List<TaskItem>>
    fun observeTasksBetween(startTime: Long, endTime: Long): Flow<List<TaskItem>>
    suspend fun getTaskById(id: String): TaskItem?
    suspend fun createTask(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        dueDate: Long? = null,
        scheduledDate: Long? = null,
        projectId: String? = null,
        category: String = "Personal"
    ): TaskItem
    suspend fun updateTaskStatus(id: String, status: TaskStatus)
    suspend fun updateTask(task: TaskItem)
    suspend fun deleteTask(id: String)
    fun observePendingCount(): Flow<Int>
    fun observeCompletedTodayCount(): Flow<Int>
}

interface ProjectRepository {
    fun observeActiveProjectsWithStats(): Flow<List<ProjectDetail>>
    fun observeAllProjectsWithStats(): Flow<List<ProjectDetail>>
    fun observeProjectDetail(projectId: String): Flow<ProjectDetail?>
    suspend fun getProjectById(id: String): ProjectItem?
    suspend fun createProject(
        name: String,
        description: String = "",
        colorHex: String = "#2563EB",
        category: String = "General",
        targetDate: Long? = null
    ): ProjectItem
    suspend fun updateProject(project: ProjectItem)
    suspend fun updateProjectStatus(id: String, status: ProjectStatus)
    suspend fun deleteProject(id: String)
}

interface HabitRepository {
    fun observeActiveHabits(): Flow<List<HabitItem>>
    fun observeTodayLogs(): Flow<List<HabitLogItem>>
    suspend fun createHabit(
        title: String,
        description: String = "",
        targetValue: Double = 1.0,
        unit: String = "veces",
        isPositive: Boolean = true
    ): HabitItem
    suspend fun toggleHabitToday(habitId: String, completed: Boolean)
    suspend fun deleteHabit(habitId: String)
}

interface RoutineRepository {
    fun observeRoutines(): Flow<List<RoutineWithSteps>>
    fun observeRoutine(id: String): Flow<RoutineWithSteps?>
    suspend fun createRoutine(
        title: String,
        description: String,
        timeOfDaySlot: TimeOfDaySlot,
        steps: List<Pair<String, Int>> // title to durationMinutes
    ): RoutineItem
    suspend fun updateStepStatus(stepId: String, isCompleted: Boolean, isSkipped: Boolean)
    suspend fun resetRoutine(routineId: String)
    suspend fun deleteRoutine(id: String)
}

interface CalendarRepository {
    fun observeScheduleForRange(startMillis: Long, endMillis: Long): Flow<List<ScheduleEntry>>
    suspend fun createEvent(
        title: String,
        description: String = "",
        startTime: Long,
        endTime: Long,
        category: String = "General",
        isAllDay: Boolean = false,
        linkedTaskId: String? = null,
        linkedProjectId: String? = null,
        colorHex: String = "#2563EB"
    ): CalendarEventItem
    suspend fun deleteEvent(id: String)
}

interface NoteRepository {
    fun observeRecentNotes(): Flow<List<NoteItem>>
    fun searchNotes(query: String): Flow<List<NoteItem>>
    suspend fun createNote(
        title: String,
        content: String,
        category: String = "Ideas",
        tags: List<String> = emptyList()
    ): NoteItem
    suspend fun deleteNote(id: String)
}

interface WellnessRepository {
    fun observeTodayWaterMl(): Flow<Int>
    fun observeTodayLogs(): Flow<List<WellnessRecord>>
    suspend fun logWater(amountMl: Int)
    suspend fun undoLatestWater()
}

interface PreferenceRepository {
    fun observeUserPreferences(): Flow<UserPreferences>
    suspend fun setThemeMode(mode: ThemeSetting)
    suspend fun setWaterGoal(goalMl: Int)
    suspend fun toggleHomeSection(sectionId: HomeSectionId, enabled: Boolean)
    suspend fun clearDatabase()
    suspend fun getCounts(): Map<String, Int>
}

interface DiaryRepository {
    fun observeAllDiaryEntries(): Flow<List<com.example.domain.model.DiaryEntryItem>>
    fun observeDiaryForDate(dateEpochDay: Long): Flow<com.example.domain.model.DiaryEntryItem?>
    suspend fun generateDayDraft(dateEpochDay: Long): String
    suspend fun saveDiaryEntry(
        dateEpochDay: Long,
        title: String,
        content: String,
        derivedSummary: String,
        mood: String
    ): com.example.domain.model.DiaryEntryItem
    suspend fun deleteDiaryEntry(id: String)
}

interface GoalRepository {
    fun observeGoalsWithProgress(): Flow<List<com.example.domain.model.GoalDetail>>
    fun observeGoalDetail(goalId: String): Flow<com.example.domain.model.GoalDetail?>
    suspend fun createGoal(
        title: String,
        description: String = "",
        timeHorizon: com.example.domain.model.GoalHorizon = com.example.domain.model.GoalHorizon.MEDIUM_TERM,
        category: String = "Personal",
        targetDate: Long? = null,
        metricName: String = "",
        targetValue: Double = 100.0,
        unit: String = "%",
        linkedProjectId: String? = null
    ): com.example.domain.model.GoalItem
    suspend fun addMilestone(goalId: String, title: String, targetDate: Long? = null)
    suspend fun toggleMilestone(milestoneId: String, isAchieved: Boolean)
    suspend fun updateGoalValue(goalId: String, currentValue: Double)
    suspend fun deleteGoal(goalId: String)
}

interface LearningRepository {
    fun observeTopics(): Flow<List<com.example.domain.model.LearningTopicItem>>
    fun observeSessionsForTopic(topicId: String): Flow<List<com.example.domain.model.LearningSessionItem>>
    suspend fun createTopic(
        title: String,
        description: String = "",
        category: String = "Tecnología",
        linkedGoalId: String? = null
    ): com.example.domain.model.LearningTopicItem
    suspend fun logSession(topicId: String, durationMinutes: Int, notes: String)
    suspend fun deleteTopic(topicId: String)
}

interface PersonRepository {
    fun observePeople(): Flow<List<com.example.domain.model.PersonItem>>
    suspend fun createPerson(
        fullName: String,
        alias: String = "",
        category: com.example.domain.model.PersonCategory = com.example.domain.model.PersonCategory.OTHER,
        importantDatesNotes: String = "",
        notes: String = "",
        nextAction: String = ""
    ): com.example.domain.model.PersonItem
    suspend fun updatePerson(person: com.example.domain.model.PersonItem)
    suspend fun deletePerson(id: String)
}

