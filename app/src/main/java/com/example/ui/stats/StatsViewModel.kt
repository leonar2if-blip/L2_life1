package com.example.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.WeeklyReviewItem
import com.example.domain.model.WeeklyStatsSummary
import com.example.domain.repository.StatsRepository
import java.util.Calendar
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsUiState(
    val weekOffset: Int = 0,
    val weekRangeLabel: String = "",
    val startEpochDay: Long = 0L,
    val endEpochDay: Long = 0L,
    val stats: WeeklyStatsSummary? = null,
    val allReviews: List<WeeklyReviewItem> = emptyList(),
    val currentWeekReview: WeeklyReviewItem? = null,
    val isDraftGenerating: Boolean = false,
    val reviewDraftText: String = "",
    val showReviewEditor: Boolean = false,
    val reviewAccomplishments: String = "",
    val reviewChallenges: String = "",
    val reviewNextWeekFocus: String = "",
    val reviewNotes: String = "",
    val reviewEnergyRating: Int = 3,
    val reviewProductivityRating: Int = 3
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _weekOffset = MutableStateFlow(0)
    private val _isDraftGenerating = MutableStateFlow(false)
    private val _reviewDraftText = MutableStateFlow("")
    private val _showReviewEditor = MutableStateFlow(false)

    private val _reviewAccomplishments = MutableStateFlow("")
    private val _reviewChallenges = MutableStateFlow("")
    private val _reviewNextWeekFocus = MutableStateFlow("")
    private val _reviewNotes = MutableStateFlow("")
    private val _reviewEnergyRating = MutableStateFlow(3)
    private val _reviewProductivityRating = MutableStateFlow(3)

    private fun getWeekEpochRange(offsetWeeks: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.add(Calendar.WEEK_OF_YEAR, offsetWeeks)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startEpochDay = cal.timeInMillis / 86400000L

        cal.add(Calendar.DAY_OF_WEEK, 6)
        val endEpochDay = cal.timeInMillis / 86400000L
        return Pair(startEpochDay, endEpochDay)
    }

    private val weekRangeFlow = _weekOffset.flatMapLatest { offset ->
        val (start, end) = getWeekEpochRange(offset)
        statsRepository.observeWeeklyStats(start, end).combine(
            statsRepository.observeReviewForWeek(start)
        ) { summary, currentReview ->
            Triple(summary, currentReview, Pair(start, end))
        }
    }

    val uiState: StateFlow<StatsUiState> = combine(
        _weekOffset,
        weekRangeFlow,
        statsRepository.observeAllReviews(),
        _isDraftGenerating,
        _reviewDraftText,
        _showReviewEditor,
        _reviewAccomplishments,
        _reviewChallenges,
        _reviewNextWeekFocus,
        _reviewNotes,
        _reviewEnergyRating,
        _reviewProductivityRating
    ) { args ->
        val offset = args[0] as Int
        @Suppress("UNCHECKED_CAST")
        val triple = args[1] as Triple<WeeklyStatsSummary, WeeklyReviewItem?, Pair<Long, Long>>
        @Suppress("UNCHECKED_CAST")
        val allReviews = args[2] as List<WeeklyReviewItem>
        val isDraftGen = args[3] as Boolean
        val draftText = args[4] as String
        val showEditor = args[5] as Boolean
        val accomplishments = args[6] as String
        val challenges = args[7] as String
        val nextWeek = args[8] as String
        val notes = args[9] as String
        val energy = args[10] as Int
        val prod = args[11] as Int

        val (startDay, endDay) = triple.third
        val rangeLabel = when (offset) {
            0 -> "Esta semana"
            -1 -> "Semana pasada"
            1 -> "Próxima semana"
            else -> if (offset < 0) "Hace ${-offset} semanas" else "En $offset semanas"
        }

        StatsUiState(
            weekOffset = offset,
            weekRangeLabel = rangeLabel,
            startEpochDay = startDay,
            endEpochDay = endDay,
            stats = triple.first,
            allReviews = allReviews,
            currentWeekReview = triple.second,
            isDraftGenerating = isDraftGen,
            reviewDraftText = draftText,
            showReviewEditor = showEditor,
            reviewAccomplishments = accomplishments,
            reviewChallenges = challenges,
            reviewNextWeekFocus = nextWeek,
            reviewNotes = notes,
            reviewEnergyRating = energy,
            reviewProductivityRating = prod
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    fun previousWeek() {
        _weekOffset.update { it - 1 }
    }

    fun nextWeek() {
        _weekOffset.update { it + 1 }
    }

    fun resetToCurrentWeek() {
        _weekOffset.value = 0
    }

    fun generateDraft() {
        val (start, end) = getWeekEpochRange(_weekOffset.value)
        _isDraftGenerating.value = true
        viewModelScope.launch {
            val draft = statsRepository.generateReviewDraft(start, end)
            _reviewDraftText.value = draft
            _isDraftGenerating.value = false
            // Preserve user data: only pre-fill if accomplishments field is currently blank
            if (_reviewAccomplishments.value.isBlank()) {
                _reviewAccomplishments.value = draft
            }
            _showReviewEditor.value = true
        }
    }

    fun openReviewEditor(existing: WeeklyReviewItem? = null) {
        if (existing != null) {
            _reviewAccomplishments.value = existing.accomplishments
            _reviewChallenges.value = existing.challenges
            _reviewNextWeekFocus.value = existing.nextWeekFocus
            _reviewNotes.value = existing.notes
            _reviewEnergyRating.value = existing.energyRating
            _reviewProductivityRating.value = existing.productivityRating
        } else {
            val currentRev = uiState.value.currentWeekReview
            if (currentRev != null) {
                _reviewAccomplishments.value = currentRev.accomplishments
                _reviewChallenges.value = currentRev.challenges
                _reviewNextWeekFocus.value = currentRev.nextWeekFocus
                _reviewNotes.value = currentRev.notes
                _reviewEnergyRating.value = currentRev.energyRating
                _reviewProductivityRating.value = currentRev.productivityRating
            } else {
                _reviewAccomplishments.value = ""
                _reviewChallenges.value = ""
                _reviewNextWeekFocus.value = ""
                _reviewNotes.value = ""
                _reviewEnergyRating.value = 3
                _reviewProductivityRating.value = 3
            }
        }
        _showReviewEditor.value = true
    }

    fun closeReviewEditor() {
        _showReviewEditor.value = false
    }

    fun updateAccomplishments(text: String) { _reviewAccomplishments.value = text }
    fun updateChallenges(text: String) { _reviewChallenges.value = text }
    fun updateNextWeekFocus(text: String) { _reviewNextWeekFocus.value = text }
    fun updateNotes(text: String) { _reviewNotes.value = text }
    fun updateEnergyRating(rating: Int) { _reviewEnergyRating.value = rating }
    fun updateProductivityRating(rating: Int) { _reviewProductivityRating.value = rating }

    fun saveReview() {
        val (start, end) = getWeekEpochRange(_weekOffset.value)
        val existing = uiState.value.currentWeekReview
        val review = WeeklyReviewItem(
            id = existing?.id ?: UUID.randomUUID().toString(),
            weekStartEpochDay = start,
            weekEndEpochDay = end,
            accomplishments = _reviewAccomplishments.value.trim(),
            challenges = _reviewChallenges.value.trim(),
            nextWeekFocus = _reviewNextWeekFocus.value.trim(),
            notes = _reviewNotes.value.trim(),
            energyRating = _reviewEnergyRating.value,
            productivityRating = _reviewProductivityRating.value,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )

        viewModelScope.launch {
            statsRepository.saveReview(review)
            _showReviewEditor.value = false
        }
    }

    fun deleteReview(id: String) {
        viewModelScope.launch {
            statsRepository.deleteReview(id)
        }
    }
}

class StatsViewModelFactory(
    private val statsRepository: StatsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatsViewModel(statsRepository) as T
    }
}
