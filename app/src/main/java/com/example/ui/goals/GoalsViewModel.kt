package com.example.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.GoalDetail
import com.example.domain.model.GoalHorizon
import com.example.domain.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalsUiState(
    val goals: List<GoalDetail> = emptyList(),
    val selectedHorizon: GoalHorizon? = null
)

class GoalsViewModel(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _selectedHorizon = MutableStateFlow<GoalHorizon?>(null)
    val selectedHorizon: StateFlow<GoalHorizon?> = _selectedHorizon.asStateFlow()

    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.observeGoalsWithProgress(),
        _selectedHorizon
    ) { allGoals, horizon ->
        val filtered = if (horizon == null) allGoals else allGoals.filter { it.goal.timeHorizon == horizon }
        GoalsUiState(
            goals = filtered,
            selectedHorizon = horizon
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsUiState()
    )

    fun filterByHorizon(horizon: GoalHorizon?) {
        _selectedHorizon.value = horizon
    }

    fun createGoal(
        title: String,
        description: String,
        horizon: GoalHorizon,
        category: String,
        targetValue: Double,
        unit: String
    ) {
        viewModelScope.launch {
            goalRepository.createGoal(
                title = title,
                description = description,
                timeHorizon = horizon,
                category = category,
                targetValue = targetValue,
                unit = unit
            )
        }
    }

    fun addMilestone(goalId: String, title: String) {
        viewModelScope.launch {
            goalRepository.addMilestone(goalId, title)
        }
    }

    fun toggleMilestone(milestoneId: String, currentAchieved: Boolean) {
        viewModelScope.launch {
            goalRepository.toggleMilestone(milestoneId, !currentAchieved)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goalId)
        }
    }
}

class GoalsViewModelFactory(
    private val goalRepository: GoalRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GoalsViewModel(goalRepository) as T
    }
}
