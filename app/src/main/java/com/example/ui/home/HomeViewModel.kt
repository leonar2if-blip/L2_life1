package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.DailySummary
import com.example.domain.model.HabitItem
import com.example.domain.model.HomeSectionId
import com.example.domain.model.NoteItem
import com.example.domain.model.Priority
import com.example.domain.model.TaskItem
import com.example.domain.model.TaskStatus
import com.example.domain.repository.HabitRepository
import com.example.domain.repository.NoteRepository
import com.example.domain.repository.PreferenceRepository
import com.example.domain.repository.TaskRepository
import com.example.domain.repository.WellnessRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitStatusItem(
    val habit: HabitItem,
    val isCompletedToday: Boolean
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val dateFormatted: String = "",
    val summary: DailySummary = DailySummary(0, 0, 0, 0, 0, 2000, 0),
    val tasks: List<TaskItem> = emptyList(),
    val habits: List<HabitStatusItem> = emptyList(),
    val waterIntakeMl: Int = 0,
    val waterGoalMl: Int = 2000,
    val recentNotes: List<NoteItem> = emptyList(),
    val enabledSections: List<HomeSectionId> = HomeSectionId.defaultSections()
)

class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val noteRepository: NoteRepository,
    private val wellnessRepository: WellnessRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))

    val uiState: StateFlow<HomeUiState> = combine(
        taskRepository.observeTasksForToday(),
        taskRepository.observePendingCount(),
        taskRepository.observeCompletedTodayCount(),
        habitRepository.observeActiveHabits(),
        habitRepository.observeTodayLogs(),
        wellnessRepository.observeTodayWaterMl(),
        noteRepository.observeRecentNotes(),
        preferenceRepository.observeUserPreferences()
    ) { params ->
        val tasksToday = params[0] as List<TaskItem>
        val pendingCount = params[1] as Int
        val completedToday = params[2] as Int
        val activeHabits = params[3] as List<HabitItem>
        val todayLogs = params[4] as List<com.example.domain.model.HabitLogItem>
        val waterMl = params[5] as Int
        val recentNotes = params[6] as List<NoteItem>
        val prefs = params[7] as com.example.domain.model.UserPreferences

        val completedHabitIds = todayLogs.filter { it.completed }.map { it.habitId }.toSet()
        val habitItems = activeHabits.map { habit ->
            HabitStatusItem(
                habit = habit,
                isCompletedToday = completedHabitIds.contains(habit.id)
            )
        }

        val formattedDate = dateFormat.format(Date()).replaceFirstChar { it.uppercase() }

        val summary = DailySummary(
            totalPendingTasks = pendingCount,
            completedTasksToday = completedToday,
            activeHabitsCount = activeHabits.size,
            completedHabitsToday = completedHabitIds.size,
            waterIntakeMl = waterMl,
            waterGoalMl = prefs.waterGoalMl,
            notesCount = recentNotes.size
        )

        HomeUiState(
            isLoading = false,
            dateFormatted = formattedDate,
            summary = summary,
            tasks = tasksToday,
            habits = habitItems,
            waterIntakeMl = waterMl,
            waterGoalMl = prefs.waterGoalMl,
            recentNotes = recentNotes.take(4),
            enabledSections = prefs.enabledHomeSections
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(
            dateFormatted = dateFormat.format(Date()).replaceFirstChar { it.uppercase() }
        )
    )

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            val newStatus = if (task.isCompleted) TaskStatus.PENDING else TaskStatus.COMPLETED
            taskRepository.updateTaskStatus(task.id, newStatus)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }

    fun toggleHabit(habitId: String, currentDone: Boolean) {
        viewModelScope.launch {
            habitRepository.toggleHabitToday(habitId, !currentDone)
        }
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            wellnessRepository.logWater(amountMl)
        }
    }

    fun undoWater() {
        viewModelScope.launch {
            wellnessRepository.undoLatestWater()
        }
    }

    fun createTask(title: String, description: String, priority: Priority, category: String) {
        viewModelScope.launch {
            taskRepository.createTask(
                title = title,
                description = description,
                priority = priority,
                category = category
            )
        }
    }

    fun createHabit(title: String, target: Double, unit: String, isPositive: Boolean) {
        viewModelScope.launch {
            habitRepository.createHabit(
                title = title,
                targetValue = target,
                unit = unit,
                isPositive = isPositive
            )
        }
    }

    fun createNote(title: String, content: String, category: String) {
        viewModelScope.launch {
            noteRepository.createNote(
                title = title,
                content = content,
                category = category
            )
        }
    }
}

class HomeViewModelFactory(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val noteRepository: NoteRepository,
    private val wellnessRepository: WellnessRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            taskRepository,
            habitRepository,
            noteRepository,
            wellnessRepository,
            preferenceRepository
        ) as T
    }
}
