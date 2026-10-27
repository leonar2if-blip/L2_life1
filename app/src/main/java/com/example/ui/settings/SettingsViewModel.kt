package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.HomeSectionId
import com.example.domain.model.Priority
import com.example.domain.model.UserPreferences
import com.example.domain.repository.HabitRepository
import com.example.domain.repository.NoteRepository
import com.example.domain.repository.PreferenceRepository
import com.example.domain.repository.TaskRepository
import com.example.domain.repository.WellnessRepository
import com.example.ui.theme.ThemeSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val storedCounts: Map<String, Int> = emptyMap(),
    val isClearing: Boolean = false
)

class SettingsViewModel(
    private val preferenceRepository: PreferenceRepository,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val noteRepository: NoteRepository,
    private val wellnessRepository: WellnessRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferenceRepository.observeUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _dbCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dbCounts: StateFlow<Map<String, Int>> = _dbCounts.asStateFlow()

    init {
        refreshCounts()
    }

    fun refreshCounts() {
        viewModelScope.launch {
            _dbCounts.value = preferenceRepository.getCounts()
        }
    }

    fun setTheme(theme: ThemeSetting) {
        viewModelScope.launch {
            preferenceRepository.setThemeMode(theme)
        }
    }

    fun toggleSection(sectionId: HomeSectionId, enabled: Boolean) {
        viewModelScope.launch {
            preferenceRepository.toggleHomeSection(sectionId, enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            preferenceRepository.clearDatabase()
            refreshCounts()
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            taskRepository.createTask(
                title = "Revisar arquitectura de L2 Life",
                description = "Validar modelos de datos y flujo de persistencia offline-first con Room",
                priority = Priority.URGENT,
                category = "Trabajo"
            )
            taskRepository.createTask(
                title = "Planificar sesiones de concentración",
                description = "Estructurar bloques de trabajo y rutinas diarias",
                priority = Priority.MEDIUM,
                category = "Personal"
            )
            habitRepository.createHabit(
                title = "Lectura matutina (15 min)",
                targetValue = 1.0,
                unit = "veces",
                isPositive = true
            )
            habitRepository.createHabit(
                title = "Evitar distracciones nocturnas",
                targetValue = 1.0,
                unit = "días",
                isPositive = false
            )
            noteRepository.createNote(
                title = "Visión L2 Life",
                content = "Una aplicación integral donde tareas, hábitos, notas y bienestar conviven en armonía sin dependencias de red.",
                category = "Ideas"
            )
            wellnessRepository.logWater(500)
            refreshCounts()
        }
    }
}

class SettingsViewModelFactory(
    private val preferenceRepository: PreferenceRepository,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val noteRepository: NoteRepository,
    private val wellnessRepository: WellnessRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            preferenceRepository,
            taskRepository,
            habitRepository,
            noteRepository,
            wellnessRepository
        ) as T
    }
}
