package com.example.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.LearningTopicItem
import com.example.domain.repository.LearningRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LearningViewModel(
    private val learningRepository: LearningRepository
) : ViewModel() {

    val topics: StateFlow<List<LearningTopicItem>> = learningRepository.observeTopics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createTopic(title: String, description: String, category: String) {
        viewModelScope.launch {
            learningRepository.createTopic(
                title = title,
                description = description,
                category = category
            )
        }
    }

    fun logStudySession(topicId: String, durationMinutes: Int, notes: String) {
        viewModelScope.launch {
            learningRepository.logSession(
                topicId = topicId,
                durationMinutes = durationMinutes,
                notes = notes
            )
        }
    }

    fun deleteTopic(topicId: String) {
        viewModelScope.launch {
            learningRepository.deleteTopic(topicId)
        }
    }
}

class LearningViewModelFactory(
    private val learningRepository: LearningRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LearningViewModel(learningRepository) as T
    }
}
