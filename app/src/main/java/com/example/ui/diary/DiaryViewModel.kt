package com.example.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DateUtils
import com.example.domain.model.DiaryEntryItem
import com.example.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiaryUiState(
    val entries: List<DiaryEntryItem> = emptyList(),
    val todayEntry: DiaryEntryItem? = null,
    val draftSummary: String = "",
    val isGeneratingDraft: Boolean = false
)

class DiaryViewModel(
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    val entries: StateFlow<List<DiaryEntryItem>> = diaryRepository.observeAllDiaryEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()

    fun generateTodayDraft() {
        viewModelScope.launch {
            val today = DateUtils.getTodayEpochDay()
            val text = diaryRepository.generateDayDraft(today)
            _draft.value = text
        }
    }

    fun saveEntry(title: String, content: String, mood: String) {
        viewModelScope.launch {
            val today = DateUtils.getTodayEpochDay()
            diaryRepository.saveDiaryEntry(
                dateEpochDay = today,
                title = title,
                content = content,
                derivedSummary = _draft.value,
                mood = mood
            )
            _draft.value = ""
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            diaryRepository.deleteDiaryEntry(id)
        }
    }
}

class DiaryViewModelFactory(
    private val diaryRepository: DiaryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DiaryViewModel(diaryRepository) as T
    }
}
