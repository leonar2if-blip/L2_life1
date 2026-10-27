package com.example.data.repository

import com.example.data.local.dao.EventDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.PreferenceDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.RoutineDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.WellnessDao
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.PreferenceEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.RoutineEntity
import com.example.data.local.entity.RoutineStepEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.domain.model.CalendarEventItem
import com.example.domain.model.EnergyLevel
import com.example.domain.model.HabitFrequency
import com.example.domain.model.HabitItem
import com.example.domain.model.HabitLogItem
import com.example.domain.model.HabitType
import com.example.domain.model.HomeSectionId
import com.example.domain.model.NoteItem
import com.example.domain.model.Priority
import com.example.domain.model.ProjectDetail
import com.example.domain.model.ProjectItem
import com.example.domain.model.ProjectStatus
import com.example.domain.model.RoutineItem
import com.example.domain.model.RoutineStepItem
import com.example.domain.model.RoutineWithSteps
import com.example.domain.model.ScheduleEntry
import com.example.domain.model.TaskItem
import com.example.domain.model.TaskStatus
import com.example.domain.model.TimeOfDaySlot
import com.example.domain.model.UserPreferences
import com.example.domain.model.WellnessRecord
import com.example.domain.model.WellnessType
import com.example.domain.repository.CalendarRepository
import com.example.domain.repository.HabitRepository
import com.example.domain.repository.NoteRepository
import com.example.domain.repository.PreferenceRepository
import com.example.domain.repository.ProjectRepository
import com.example.domain.repository.RoutineRepository
import com.example.domain.repository.TaskRepository
import com.example.domain.repository.WellnessRepository
import com.example.ui.theme.ThemeSetting
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object DateUtils {
    fun getTodayEpochDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis / (24L * 60 * 60 * 1000L)
    }

    fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getTodayEndMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}

class TaskRepositoryImpl(private val taskDao: TaskDao) : TaskRepository {
    override fun observeAllTasks(): Flow<List<TaskItem>> =
        taskDao.observeAllTasks().map { list -> list.map { it.toDomain() } }

    override fun observePendingTasks(): Flow<List<TaskItem>> =
        taskDao.observePendingTasks().map { list -> list.map { it.toDomain() } }

    override fun observeTasksForToday(): Flow<List<TaskItem>> =
        taskDao.observeTasksForToday(DateUtils.getTodayStartMillis(), DateUtils.getTodayEndMillis())
            .map { list -> list.map { it.toDomain() } }

    override fun observeTasksForProject(projectId: String): Flow<List<TaskItem>> =
        taskDao.observeTasksForProject(projectId).map { list -> list.map { it.toDomain() } }

    override fun observeTasksBetween(startTime: Long, endTime: Long): Flow<List<TaskItem>> =
        taskDao.observeTasksBetween(startTime, endTime).map { list -> list.map { it.toDomain() } }

    override suspend fun getTaskById(id: String): TaskItem? =
        taskDao.getTaskById(id)?.toDomain()

    override suspend fun createTask(
        title: String,
        description: String,
        priority: Priority,
        dueDate: Long?,
        scheduledDate: Long?,
        projectId: String?,
        category: String
    ): TaskItem {
        val entity = TaskEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            priority = priority.name,
            status = TaskStatus.PENDING.name,
            category = category,
            dueDate = dueDate,
            scheduledDate = scheduledDate,
            projectId = projectId,
            createdAt = System.currentTimeMillis()
        )
        taskDao.insertTask(entity)
        return entity.toDomain()
    }

    override suspend fun updateTaskStatus(id: String, status: TaskStatus) {
        val completedAt = if (status == TaskStatus.COMPLETED) System.currentTimeMillis() else null
        taskDao.updateTaskStatus(id, status.name, completedAt)
    }

    override suspend fun updateTask(task: TaskItem) {
        val entity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            priority = task.priority.name,
            status = task.status.name,
            energyLevel = task.energyLevel.name,
            category = task.category,
            estimatedMinutes = task.estimatedMinutes,
            dueDate = task.dueDate,
            scheduledDate = task.scheduledDate,
            projectId = task.projectId,
            parentTaskId = task.parentTaskId,
            createdAt = task.createdAt,
            completedAt = task.completedAt,
            isRecurring = task.isRecurring,
            recurrenceRule = task.recurrenceRule
        )
        taskDao.updateTask(entity)
    }

    override suspend fun deleteTask(id: String) {
        taskDao.deleteTaskById(id)
    }

    override fun observePendingCount(): Flow<Int> =
        taskDao.observePendingTaskCount()

    override fun observeCompletedTodayCount(): Flow<Int> =
        taskDao.observeCompletedTodayCount(DateUtils.getTodayStartMillis())

    private fun TaskEntity.toDomain(): TaskItem = TaskItem(
        id = id,
        title = title,
        description = description,
        priority = Priority.fromString(priority),
        status = TaskStatus.fromString(status),
        energyLevel = EnergyLevel.fromString(energyLevel),
        category = category,
        estimatedMinutes = estimatedMinutes,
        dueDate = dueDate,
        scheduledDate = scheduledDate,
        projectId = projectId,
        parentTaskId = parentTaskId,
        createdAt = createdAt,
        completedAt = completedAt,
        isRecurring = isRecurring,
        recurrenceRule = recurrenceRule
    )
}

class ProjectRepositoryImpl(
    private val projectDao: ProjectDao,
    private val taskDao: TaskDao
) : ProjectRepository {

    override fun observeActiveProjectsWithStats(): Flow<List<ProjectDetail>> =
        combine(projectDao.observeActiveProjects(), taskDao.observeAllTasks()) { projects, tasks ->
            projects.map { p -> buildProjectDetail(p, tasks) }
        }

    override fun observeAllProjectsWithStats(): Flow<List<ProjectDetail>> =
        combine(projectDao.observeAllProjects(), taskDao.observeAllTasks()) { projects, tasks ->
            projects.map { p -> buildProjectDetail(p, tasks) }
        }

    override fun observeProjectDetail(projectId: String): Flow<ProjectDetail?> =
        combine(projectDao.observeProjectById(projectId), taskDao.observeTasksForProject(projectId)) { projectEntity, tasks ->
            projectEntity?.let { p ->
                val total = tasks.size
                val completed = tasks.count { it.status == TaskStatus.COMPLETED.name }
                val pending = total - completed
                val progress = if (total == 0) 0f else completed.toFloat() / total.toFloat()
                ProjectDetail(
                    project = p.toDomain(),
                    totalTasks = total,
                    completedTasks = completed,
                    pendingTasks = pending,
                    progress = progress
                )
            }
        }

    override suspend fun getProjectById(id: String): ProjectItem? =
        projectDao.getProjectById(id)?.toDomain()

    override suspend fun createProject(
        name: String,
        description: String,
        colorHex: String,
        category: String,
        targetDate: Long?
    ): ProjectItem {
        val entity = ProjectEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            description = description.trim(),
            colorHex = colorHex,
            category = category,
            status = ProjectStatus.ACTIVE.name,
            targetDate = targetDate,
            createdAt = System.currentTimeMillis()
        )
        projectDao.insertProject(entity)
        return entity.toDomain()
    }

    override suspend fun updateProject(project: ProjectItem) {
        val entity = ProjectEntity(
            id = project.id,
            name = project.name,
            description = project.description,
            colorHex = project.colorHex,
            category = project.category,
            status = project.status.name,
            targetDate = project.targetDate,
            createdAt = project.createdAt
        )
        projectDao.updateProject(entity)
    }

    override suspend fun updateProjectStatus(id: String, status: ProjectStatus) {
        val existing = projectDao.getProjectById(id)
        if (existing != null) {
            projectDao.updateProject(existing.copy(status = status.name))
        }
    }

    override suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }

    private fun buildProjectDetail(project: ProjectEntity, allTasks: List<TaskEntity>): ProjectDetail {
        val projectTasks = allTasks.filter { it.projectId == project.id }
        val total = projectTasks.size
        val completed = projectTasks.count { it.status == TaskStatus.COMPLETED.name }
        val pending = total - completed
        val progress = if (total == 0) 0f else completed.toFloat() / total.toFloat()
        return ProjectDetail(
            project = project.toDomain(),
            totalTasks = total,
            completedTasks = completed,
            pendingTasks = pending,
            progress = progress
        )
    }

    private fun ProjectEntity.toDomain(): ProjectItem = ProjectItem(
        id = id,
        name = name,
        description = description,
        colorHex = colorHex,
        category = category,
        status = ProjectStatus.fromString(status),
        targetDate = targetDate,
        createdAt = createdAt
    )
}

class RoutineRepositoryImpl(private val routineDao: RoutineDao) : RoutineRepository {

    override fun observeRoutines(): Flow<List<RoutineWithSteps>> =
        routineDao.observeAllRoutines().map { routines ->
            routines.map { r ->
                val steps = routineDao.getStepsForRoutine(r.id)
                RoutineWithSteps(
                    routine = r.toDomain(),
                    steps = steps.map { it.toDomain() }
                )
            }
        }

    override fun observeRoutine(id: String): Flow<RoutineWithSteps?> =
        combine(
            routineDao.observeRoutineById(id),
            routineDao.observeStepsForRoutine(id)
        ) { routineEntity, steps ->
            routineEntity?.let { r ->
                RoutineWithSteps(
                    routine = r.toDomain(),
                    steps = steps.map { it.toDomain() }
                )
            }
        }

    override suspend fun createRoutine(
        title: String,
        description: String,
        timeOfDaySlot: TimeOfDaySlot,
        steps: List<Pair<String, Int>>
    ): RoutineItem {
        val routineId = UUID.randomUUID().toString()
        val routineEntity = RoutineEntity(
            id = routineId,
            title = title.trim(),
            description = description.trim(),
            timeOfDaySlot = timeOfDaySlot.name,
            estimatedMinutes = steps.sumOf { it.second }.coerceAtLeast(5),
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
        routineDao.insertRoutine(routineEntity)

        val stepEntities = steps.mapIndexed { index, pair ->
            RoutineStepEntity(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                title = pair.first.trim(),
                durationMinutes = pair.second,
                orderIndex = index,
                isCompleted = false,
                isSkipped = false
            )
        }
        routineDao.insertSteps(stepEntities)
        return routineEntity.toDomain()
    }

    override suspend fun updateStepStatus(stepId: String, isCompleted: Boolean, isSkipped: Boolean) {
        routineDao.updateStepStatus(stepId, isCompleted, isSkipped)
    }

    override suspend fun resetRoutine(routineId: String) {
        routineDao.resetRoutineSteps(routineId)
    }

    override suspend fun deleteRoutine(id: String) {
        routineDao.deleteStepsForRoutine(id)
        routineDao.deleteRoutineById(id)
    }

    private fun RoutineEntity.toDomain(): RoutineItem = RoutineItem(
        id = id,
        title = title,
        description = description,
        timeOfDaySlot = TimeOfDaySlot.fromString(timeOfDaySlot),
        estimatedMinutes = estimatedMinutes,
        isActive = isActive,
        createdAt = createdAt
    )

    private fun RoutineStepEntity.toDomain(): RoutineStepItem = RoutineStepItem(
        id = id,
        routineId = routineId,
        title = title,
        description = description,
        durationMinutes = durationMinutes,
        orderIndex = orderIndex,
        habitId = habitId,
        isCompleted = isCompleted,
        isSkipped = isSkipped
    )
}

class CalendarRepositoryImpl(
    private val eventDao: EventDao,
    private val taskDao: TaskDao
) : CalendarRepository {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun observeScheduleForRange(startMillis: Long, endMillis: Long): Flow<List<ScheduleEntry>> =
        combine(
            eventDao.observeEventsBetween(startMillis, endMillis),
            taskDao.observeTasksBetween(startMillis, endMillis)
        ) { events, tasks ->
            val entries = mutableListOf<ScheduleEntry>()

            // Add events
            events.forEach { e ->
                val timeLabel = if (e.isAllDay) "Todo el día"
                else "${timeFormat.format(Date(e.startTime))} - ${timeFormat.format(Date(e.endTime))}"
                entries.add(
                    ScheduleEntry(
                        id = "event_${e.id}",
                        title = e.title,
                        subtitle = if (e.description.isNotBlank()) e.description else e.category,
                        timeLabel = timeLabel,
                        isTaskDeadline = false,
                        isCompleted = false,
                        colorHex = e.colorHex,
                        originalEventId = e.id
                    )
                )
            }

            // Add tasks with scheduled date or deadline
            tasks.forEach { t ->
                val timeLabel = when {
                    t.scheduledDate != null -> "Hora reservada: ${timeFormat.format(Date(t.scheduledDate))}"
                    t.dueDate != null -> "Fecha límite: ${timeFormat.format(Date(t.dueDate))}"
                    else -> "Sin hora fijada"
                }
                entries.add(
                    ScheduleEntry(
                        id = "task_${t.id}",
                        title = t.title,
                        subtitle = "Prioridad ${t.priority} • ${t.category}",
                        timeLabel = timeLabel,
                        isTaskDeadline = t.scheduledDate == null,
                        isCompleted = t.status == TaskStatus.COMPLETED.name,
                        priority = Priority.fromString(t.priority),
                        colorHex = when (t.priority) {
                            "URGENT" -> "#DC2626"
                            "HIGH" -> "#EA580C"
                            "LOW" -> "#16A34A"
                            else -> "#2563EB"
                        },
                        originalTaskId = t.id
                    )
                )
            }

            entries.sortedBy { it.timeLabel }
        }

    override suspend fun createEvent(
        title: String,
        description: String,
        startTime: Long,
        endTime: Long,
        category: String,
        isAllDay: Boolean,
        linkedTaskId: String?,
        linkedProjectId: String?,
        colorHex: String
    ): CalendarEventItem {
        val entity = CalendarEventEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            startTime = startTime,
            endTime = endTime,
            category = category,
            isAllDay = isAllDay,
            linkedTaskId = linkedTaskId,
            linkedProjectId = linkedProjectId,
            colorHex = colorHex,
            createdAt = System.currentTimeMillis()
        )
        eventDao.insertEvent(entity)
        return CalendarEventItem(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            category = entity.category,
            startTime = entity.startTime,
            endTime = entity.endTime,
            isAllDay = entity.isAllDay,
            linkedTaskId = entity.linkedTaskId,
            linkedProjectId = entity.linkedProjectId,
            colorHex = entity.colorHex,
            createdAt = entity.createdAt
        )
    }

    override suspend fun deleteEvent(id: String) {
        eventDao.deleteEventById(id)
    }
}

class HabitRepositoryImpl(private val habitDao: HabitDao) : HabitRepository {
    override fun observeActiveHabits(): Flow<List<HabitItem>> =
        habitDao.observeActiveHabits().map { list -> list.map { it.toDomain() } }

    override fun observeTodayLogs(): Flow<List<HabitLogItem>> =
        habitDao.observeLogsForDate(DateUtils.getTodayEpochDay()).map { list -> list.map { it.toDomain() } }

    override suspend fun createHabit(
        title: String,
        description: String,
        targetValue: Double,
        unit: String,
        isPositive: Boolean
    ): HabitItem {
        val entity = HabitEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            targetValue = targetValue,
            unit = unit,
            isPositive = isPositive,
            createdAt = System.currentTimeMillis()
        )
        habitDao.insertHabit(entity)
        return entity.toDomain()
    }

    override suspend fun toggleHabitToday(habitId: String, completed: Boolean) {
        val today = DateUtils.getTodayEpochDay()
        if (completed) {
            val log = HabitLogEntity(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                dateEpochDay = today,
                value = 1.0,
                completed = true,
                timestamp = System.currentTimeMillis()
            )
            habitDao.insertOrUpdateLog(log)
        } else {
            habitDao.deleteLog(habitId, today)
        }
    }

    override suspend fun deleteHabit(habitId: String) {
        habitDao.deleteHabitById(habitId)
    }

    private fun HabitEntity.toDomain(): HabitItem = HabitItem(
        id = id,
        title = title,
        description = description,
        type = HabitType.fromString(type),
        targetValue = targetValue,
        unit = unit,
        frequency = HabitFrequency.fromString(frequency),
        isPositive = isPositive,
        colorHex = colorHex,
        createdAt = createdAt,
        isArchived = isArchived
    )

    private fun HabitLogEntity.toDomain(): HabitLogItem = HabitLogItem(
        id = id,
        habitId = habitId,
        dateEpochDay = dateEpochDay,
        value = value,
        completed = completed,
        notes = notes,
        timestamp = timestamp
    )
}

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {
    override fun observeRecentNotes(): Flow<List<NoteItem>> =
        noteDao.observeAllNotes().map { list -> list.map { it.toDomain() } }

    override fun searchNotes(query: String): Flow<List<NoteItem>> =
        noteDao.searchNotes(query).map { list -> list.map { it.toDomain() } }

    override suspend fun createNote(
        title: String,
        content: String,
        category: String,
        tags: List<String>
    ): NoteItem {
        val now = System.currentTimeMillis()
        val entity = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            content = content.trim(),
            category = category,
            tagsCsv = tags.joinToString(","),
            createdAt = now,
            updatedAt = now
        )
        noteDao.insertNote(entity)
        return entity.toDomain()
    }

    override suspend fun deleteNote(id: String) {
        noteDao.deleteNoteById(id)
    }

    private fun NoteEntity.toDomain(): NoteItem = NoteItem(
        id = id,
        title = title,
        content = content,
        category = category,
        tags = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split(",").map { it.trim() },
        isPinned = isPinned,
        linkedTaskId = linkedTaskId,
        linkedProjectId = linkedProjectId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

class WellnessRepositoryImpl(private val wellnessDao: WellnessDao) : WellnessRepository {
    override fun observeTodayWaterMl(): Flow<Int> =
        wellnessDao.observeWaterLogsForDate(DateUtils.getTodayEpochDay()).map { logs ->
            logs.sumOf { it.numericValue.toInt() }
        }

    override fun observeTodayLogs(): Flow<List<WellnessRecord>> =
        wellnessDao.observeAllLogsForDate(DateUtils.getTodayEpochDay()).map { logs ->
            logs.map { it.toDomain() }
        }

    override suspend fun logWater(amountMl: Int) {
        val log = WellnessLogEntity(
            id = UUID.randomUUID().toString(),
            type = "WATER",
            numericValue = amountMl.toDouble(),
            unit = "ml",
            timestamp = System.currentTimeMillis(),
            dateEpochDay = DateUtils.getTodayEpochDay()
        )
        wellnessDao.insertLog(log)
    }

    override suspend fun undoLatestWater() {
        wellnessDao.deleteLatestWaterLog(DateUtils.getTodayEpochDay())
    }

    private fun WellnessLogEntity.toDomain(): WellnessRecord = WellnessRecord(
        id = id,
        type = WellnessType.fromString(type),
        numericValue = numericValue,
        unit = unit,
        note = note,
        timestamp = timestamp,
        dateEpochDay = dateEpochDay
    )
}

class PreferenceRepositoryImpl(
    private val preferenceDao: PreferenceDao,
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val noteDao: NoteDao,
    private val projectDao: ProjectDao,
    private val wellnessDao: WellnessDao,
    private val routineDao: RoutineDao,
    private val eventDao: EventDao,
    private val diaryDao: com.example.data.local.dao.DiaryDao,
    private val goalDao: com.example.data.local.dao.GoalDao,
    private val learningDao: com.example.data.local.dao.LearningDao,
    private val personDao: com.example.data.local.dao.PersonDao
) : PreferenceRepository {

    override fun observeUserPreferences(): Flow<UserPreferences> =
        preferenceDao.observeAllPreferences().map { prefs ->
            val map = prefs.associate { it.key to it.value }
            val theme = map["theme_mode"] ?: "SYSTEM"
            val startMonday = map["start_monday"]?.toBooleanStrictOrNull() ?: true
            val waterGoal = map["water_goal"]?.toIntOrNull() ?: 2000
            val sectionsRaw = map["home_sections"]
            val sections = if (sectionsRaw.isNullOrBlank()) {
                HomeSectionId.defaultSections()
            } else {
                sectionsRaw.split(",")
                    .mapNotNull { HomeSectionId.fromString(it.trim()) }
                    .ifEmpty { HomeSectionId.defaultSections() }
            }
            UserPreferences(
                themeMode = theme,
                startOfWeekMonday = startMonday,
                waterGoalMl = waterGoal,
                enabledHomeSections = sections
            )
        }

    override suspend fun setThemeMode(mode: ThemeSetting) {
        preferenceDao.setPreference(PreferenceEntity("theme_mode", mode.name))
    }

    override suspend fun setWaterGoal(goalMl: Int) {
        preferenceDao.setPreference(PreferenceEntity("water_goal", goalMl.toString()))
    }

    override suspend fun toggleHomeSection(sectionId: HomeSectionId, enabled: Boolean) {
        val currentPref = preferenceDao.getPreference("home_sections")?.value
        val currentSections = if (currentPref.isNullOrBlank()) {
            HomeSectionId.defaultSections()
        } else {
            currentPref.split(",").mapNotNull { HomeSectionId.fromString(it.trim()) }
        }.toMutableList()

        if (enabled) {
            if (!currentSections.contains(sectionId)) {
                currentSections.add(sectionId)
            }
        } else {
            currentSections.remove(sectionId)
        }

        preferenceDao.setPreference(
            PreferenceEntity("home_sections", currentSections.joinToString(",") { it.name })
        )
    }

    override suspend fun clearDatabase() {
        taskDao.clearAll()
        habitDao.clearAll()
        noteDao.clearAll()
        projectDao.clearAll()
        wellnessDao.clearAll()
        routineDao.clearAllRoutines()
        routineDao.clearAllRoutineSteps()
        eventDao.clearAll()
        diaryDao.clearAllDiaryEntries()
        goalDao.clearAllGoals()
        goalDao.clearAllMilestones()
        learningDao.clearAllTopics()
        learningDao.clearAllSessions()
        personDao.clearAllPeople()
        preferenceDao.clearAll()
    }

    override suspend fun getCounts(): Map<String, Int> {
        return mapOf(
            "tasks" to taskDao.countAllTasks(),
            "habits" to habitDao.countActiveHabits(),
            "notes" to noteDao.countAllNotes(),
            "projects" to projectDao.countAllProjects(),
            "routines" to routineDao.countAllRoutines(),
            "events" to eventDao.countAllEvents(),
            "diary" to diaryDao.countAllDiaryEntries(),
            "goals" to goalDao.countAllGoals(),
            "learning" to learningDao.countAllTopics(),
            "people" to personDao.countAllPeople()
        )
    }
}

class DiaryRepositoryImpl(
    private val diaryDao: com.example.data.local.dao.DiaryDao,
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val wellnessDao: WellnessDao
) : com.example.domain.repository.DiaryRepository {

    override fun observeAllDiaryEntries(): Flow<List<com.example.domain.model.DiaryEntryItem>> =
        diaryDao.observeAllDiaryEntries().map { list ->
            list.map {
                com.example.domain.model.DiaryEntryItem(
                    id = it.id,
                    dateEpochDay = it.dateEpochDay,
                    title = it.title,
                    content = it.content,
                    derivedSummary = it.derivedSummary,
                    mood = it.mood,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

    override fun observeDiaryForDate(dateEpochDay: Long): Flow<com.example.domain.model.DiaryEntryItem?> =
        diaryDao.observeDiaryEntryByDate(dateEpochDay).map { entity ->
            entity?.let {
                com.example.domain.model.DiaryEntryItem(
                    id = it.id,
                    dateEpochDay = it.dateEpochDay,
                    title = it.title,
                    content = it.content,
                    derivedSummary = it.derivedSummary,
                    mood = it.mood,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

    override suspend fun generateDayDraft(dateEpochDay: Long): String {
        val startOfDay = DateUtils.getTodayStartMillis()
        val endOfDay = DateUtils.getTodayEndMillis()
        val tasks = taskDao.observeTasksForToday(startOfDay, endOfDay).first()
        val completedTasks = tasks.filter { it.status == "COMPLETED" }

        val habitLogs = habitDao.observeLogsForDate(dateEpochDay).first()
        val activeHabits = habitDao.observeActiveHabits().first()
        val completedHabitNames = habitLogs.filter { it.completed }.mapNotNull { log ->
            activeHabits.find { it.id == log.habitId }?.title
        }

        val waterLogs = wellnessDao.observeWaterLogsForDate(dateEpochDay).first()
        val waterTotal = waterLogs.sumOf { it.numericValue.toInt() }

        val sb = StringBuilder()
        sb.append("Resumen del día generado a partir de registros reales:\n")
        if (completedTasks.isNotEmpty()) {
            sb.append("\n• Tareas completadas (${completedTasks.size}):\n")
            completedTasks.forEach { sb.append("  - ${it.title} [${it.priority}]\n") }
        } else {
            sb.append("\n• Tareas: Ninguna completada hoy.\n")
        }

        if (completedHabitNames.isNotEmpty()) {
            sb.append("\n• Hábitos logrados (${completedHabitNames.size}):\n")
            completedHabitNames.forEach { sb.append("  - $it\n") }
        } else {
            sb.append("\n• Hábitos: Sin registros de cumplimiento hoy.\n")
        }

        sb.append("\n• Hidratación: $waterTotal ml registrados.\n")
        return sb.toString()
    }

    override suspend fun saveDiaryEntry(
        dateEpochDay: Long,
        title: String,
        content: String,
        derivedSummary: String,
        mood: String
    ): com.example.domain.model.DiaryEntryItem {
        val existing = diaryDao.getDiaryEntryByDate(dateEpochDay)
        val now = System.currentTimeMillis()
        val entity = com.example.data.local.entity.DiaryEntryEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            dateEpochDay = dateEpochDay,
            title = title.trim(),
            content = content.trim(),
            derivedSummary = derivedSummary.trim(),
            mood = mood,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        diaryDao.insertDiaryEntry(entity)
        return com.example.domain.model.DiaryEntryItem(
            id = entity.id,
            dateEpochDay = entity.dateEpochDay,
            title = entity.title,
            content = entity.content,
            derivedSummary = entity.derivedSummary,
            mood = entity.mood,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override suspend fun deleteDiaryEntry(id: String) {
        diaryDao.deleteDiaryEntryById(id)
    }
}

class GoalRepositoryImpl(
    private val goalDao: com.example.data.local.dao.GoalDao
) : com.example.domain.repository.GoalRepository {

    override fun observeGoalsWithProgress(): Flow<List<com.example.domain.model.GoalDetail>> =
        goalDao.observeAllGoals().map { goals ->
            goals.map { g ->
                val milestones = goalDao.getMilestonesForGoal(g.id)
                buildGoalDetail(g, milestones)
            }
        }

    override fun observeGoalDetail(goalId: String): Flow<com.example.domain.model.GoalDetail?> =
        combine(goalDao.observeGoalById(goalId), goalDao.observeMilestonesForGoal(goalId)) { goalEntity, milestones ->
            goalEntity?.let { buildGoalDetail(it, milestones) }
        }

    override suspend fun createGoal(
        title: String,
        description: String,
        timeHorizon: com.example.domain.model.GoalHorizon,
        category: String,
        targetDate: Long?,
        metricName: String,
        targetValue: Double,
        unit: String,
        linkedProjectId: String?
    ): com.example.domain.model.GoalItem {
        val entity = com.example.data.local.entity.GoalEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            timeHorizon = timeHorizon.name,
            category = category,
            status = com.example.domain.model.GoalStatus.ACTIVE.name,
            targetDate = targetDate,
            metricName = metricName,
            startValue = 0.0,
            currentValue = 0.0,
            targetValue = targetValue,
            unit = unit,
            linkedProjectId = linkedProjectId,
            createdAt = System.currentTimeMillis()
        )
        goalDao.insertGoal(entity)
        return entity.toDomain()
    }

    override suspend fun addMilestone(goalId: String, title: String, targetDate: Long?) {
        val existing = goalDao.getMilestonesForGoal(goalId)
        val milestone = com.example.data.local.entity.GoalMilestoneEntity(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            title = title.trim(),
            targetDate = targetDate,
            isAchieved = false,
            orderIndex = existing.size
        )
        goalDao.insertMilestone(milestone)
    }

    override suspend fun toggleMilestone(milestoneId: String, isAchieved: Boolean) {
        val achievedAt = if (isAchieved) System.currentTimeMillis() else null
        goalDao.updateMilestoneStatus(milestoneId, isAchieved, achievedAt)
    }

    override suspend fun updateGoalValue(goalId: String, currentValue: Double) {
        val goal = goalDao.getGoalById(goalId)
        if (goal != null) {
            goalDao.updateGoal(goal.copy(currentValue = currentValue))
        }
    }

    override suspend fun deleteGoal(goalId: String) {
        goalDao.deleteMilestonesForGoal(goalId)
        goalDao.deleteGoalById(goalId)
    }

    private fun buildGoalDetail(
        g: com.example.data.local.entity.GoalEntity,
        milestones: List<com.example.data.local.entity.GoalMilestoneEntity>
    ): com.example.domain.model.GoalDetail {
        val progress = if (milestones.isNotEmpty()) {
            val achieved = milestones.count { it.isAchieved }
            achieved.toFloat() / milestones.size.toFloat()
        } else {
            val span = g.targetValue - g.startValue
            if (span > 0) ((g.currentValue - g.startValue) / span).toFloat().coerceIn(0f, 1f) else 0f
        }
        return com.example.domain.model.GoalDetail(
            goal = g.toDomain(),
            milestones = milestones.map { it.toDomain() },
            progress = progress
        )
    }

    private fun com.example.data.local.entity.GoalEntity.toDomain() = com.example.domain.model.GoalItem(
        id = id,
        title = title,
        description = description,
        timeHorizon = com.example.domain.model.GoalHorizon.fromString(timeHorizon),
        category = category,
        status = com.example.domain.model.GoalStatus.fromString(status),
        targetDate = targetDate,
        metricName = metricName,
        startValue = startValue,
        currentValue = currentValue,
        targetValue = targetValue,
        unit = unit,
        linkedProjectId = linkedProjectId,
        createdAt = createdAt
    )

    private fun com.example.data.local.entity.GoalMilestoneEntity.toDomain() = com.example.domain.model.GoalMilestoneItem(
        id = id,
        goalId = goalId,
        title = title,
        targetDate = targetDate,
        isAchieved = isAchieved,
        orderIndex = orderIndex,
        achievedAt = achievedAt
    )
}

class LearningRepositoryImpl(
    private val learningDao: com.example.data.local.dao.LearningDao
) : com.example.domain.repository.LearningRepository {

    override fun observeTopics(): Flow<List<com.example.domain.model.LearningTopicItem>> =
        learningDao.observeAllTopics().map { list ->
            list.map {
                com.example.domain.model.LearningTopicItem(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    category = it.category,
                    status = com.example.domain.model.LearningStatus.fromString(it.status),
                    totalStudyMinutes = it.totalStudyMinutes,
                    resourcesCount = it.resourcesCount,
                    linkedGoalId = it.linkedGoalId,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

    override fun observeSessionsForTopic(topicId: String): Flow<List<com.example.domain.model.LearningSessionItem>> =
        learningDao.observeSessionsForTopic(topicId).map { list ->
            list.map {
                com.example.domain.model.LearningSessionItem(
                    id = it.id,
                    topicId = it.topicId,
                    durationMinutes = it.durationMinutes,
                    notes = it.notes,
                    timestamp = it.timestamp,
                    dateEpochDay = it.dateEpochDay
                )
            }
        }

    override suspend fun createTopic(
        title: String,
        description: String,
        category: String,
        linkedGoalId: String?
    ): com.example.domain.model.LearningTopicItem {
        val now = System.currentTimeMillis()
        val entity = com.example.data.local.entity.LearningTopicEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            category = category,
            status = com.example.domain.model.LearningStatus.IN_PROGRESS.name,
            totalStudyMinutes = 0,
            resourcesCount = 0,
            linkedGoalId = linkedGoalId,
            createdAt = now,
            updatedAt = now
        )
        learningDao.insertTopic(entity)
        return com.example.domain.model.LearningTopicItem(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            category = entity.category,
            status = com.example.domain.model.LearningStatus.IN_PROGRESS,
            totalStudyMinutes = 0,
            resourcesCount = 0,
            linkedGoalId = linkedGoalId,
            createdAt = now,
            updatedAt = now
        )
    }

    override suspend fun logSession(topicId: String, durationMinutes: Int, notes: String) {
        val now = System.currentTimeMillis()
        val session = com.example.data.local.entity.LearningSessionEntity(
            id = UUID.randomUUID().toString(),
            topicId = topicId,
            durationMinutes = durationMinutes,
            notes = notes.trim(),
            timestamp = now,
            dateEpochDay = DateUtils.getTodayEpochDay()
        )
        learningDao.insertSession(session)
        learningDao.addStudyMinutes(topicId, durationMinutes, now)
    }

    override suspend fun deleteTopic(topicId: String) {
        learningDao.deleteTopicById(topicId)
    }
}

class PersonRepositoryImpl(
    private val personDao: com.example.data.local.dao.PersonDao
) : com.example.domain.repository.PersonRepository {

    override fun observePeople(): Flow<List<com.example.domain.model.PersonItem>> =
        personDao.observeAllPeople().map { list ->
            list.map {
                com.example.domain.model.PersonItem(
                    id = it.id,
                    fullName = it.fullName,
                    alias = it.alias,
                    category = com.example.domain.model.PersonCategory.fromString(it.category),
                    importantDatesNotes = it.importantDatesNotes,
                    notes = it.notes,
                    nextAction = it.nextAction,
                    createdAt = it.createdAt
                )
            }
        }

    override suspend fun createPerson(
        fullName: String,
        alias: String,
        category: com.example.domain.model.PersonCategory,
        importantDatesNotes: String,
        notes: String,
        nextAction: String
    ): com.example.domain.model.PersonItem {
        val entity = com.example.data.local.entity.PersonEntity(
            id = UUID.randomUUID().toString(),
            fullName = fullName.trim(),
            alias = alias.trim(),
            category = category.name,
            importantDatesNotes = importantDatesNotes.trim(),
            notes = notes.trim(),
            nextAction = nextAction.trim(),
            createdAt = System.currentTimeMillis()
        )
        personDao.insertPerson(entity)
        return com.example.domain.model.PersonItem(
            id = entity.id,
            fullName = entity.fullName,
            alias = entity.alias,
            category = category,
            importantDatesNotes = entity.importantDatesNotes,
            notes = entity.notes,
            nextAction = entity.nextAction,
            createdAt = entity.createdAt
        )
    }

    override suspend fun updatePerson(person: com.example.domain.model.PersonItem) {
        val entity = com.example.data.local.entity.PersonEntity(
            id = person.id,
            fullName = person.fullName,
            alias = person.alias,
            category = person.category.name,
            importantDatesNotes = person.importantDatesNotes,
            notes = person.notes,
            nextAction = person.nextAction,
            createdAt = person.createdAt
        )
        personDao.updatePerson(entity)
    }

    override suspend fun deletePerson(id: String) {
        personDao.deletePersonById(id)
    }
}

