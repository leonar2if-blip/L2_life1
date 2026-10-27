package com.example.ui.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.RoutineWithSteps
import com.example.domain.model.TimeOfDaySlot
import com.example.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoutinesUiState(
    val routines: List<RoutineWithSteps> = emptyList(),
    val activeRoutineId: String? = null
)

class RoutinesViewModel(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _activeRoutineId = MutableStateFlow<String?>(null)
    val activeRoutineId: StateFlow<String?> = _activeRoutineId.asStateFlow()

    val uiState: StateFlow<RoutinesUiState> = combine(
        routineRepository.observeRoutines(),
        _activeRoutineId
    ) { routines, activeId ->
        RoutinesUiState(
            routines = routines,
            activeRoutineId = activeId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RoutinesUiState()
    )

    fun openRoutineRunner(routineId: String) {
        _activeRoutineId.value = routineId
    }

    fun closeRoutineRunner() {
        _activeRoutineId.value = null
    }

    fun toggleStep(stepId: String, currentDone: Boolean) {
        viewModelScope.launch {
            routineRepository.updateStepStatus(stepId, isCompleted = !currentDone, isSkipped = false)
        }
    }

    fun skipStep(stepId: String) {
        viewModelScope.launch {
            routineRepository.updateStepStatus(stepId, isCompleted = false, isSkipped = true)
        }
    }

    fun resetRoutine(routineId: String) {
        viewModelScope.launch {
            routineRepository.resetRoutine(routineId)
        }
    }

    fun createRoutine(
        title: String,
        description: String,
        slot: TimeOfDaySlot,
        steps: List<Pair<String, Int>>
    ) {
        viewModelScope.launch {
            routineRepository.createRoutine(title, description, slot, steps)
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            if (_activeRoutineId.value == routineId) {
                _activeRoutineId.value = null
            }
            routineRepository.deleteRoutine(routineId)
        }
    }
}

class RoutinesViewModelFactory(
    private val routineRepository: RoutineRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RoutinesViewModel(routineRepository) as T
    }
}
