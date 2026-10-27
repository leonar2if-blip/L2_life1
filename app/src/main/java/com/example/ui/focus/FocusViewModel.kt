package com.example.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.FocusMode
import com.example.domain.model.FocusSessionItem
import com.example.domain.model.FocusTimerState
import com.example.domain.model.FocusTimerStatus
import com.example.domain.model.LearningTopicItem
import com.example.domain.model.TaskItem
import com.example.domain.repository.FocusRepository
import com.example.domain.repository.LearningRepository
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusUiState(
    val timerState: FocusTimerState = FocusTimerState(),
    val availableTasks: List<TaskItem> = emptyList(),
    val availableTopics: List<LearningTopicItem> = emptyList(),
    val todaySessions: List<FocusSessionItem> = emptyList(),
    val allSessions: List<FocusSessionItem> = emptyList(),
    val showCompletionDialog: Boolean = false,
    val completionNote: String = "",
    val markTaskAsDone: Boolean = false
)

class FocusViewModel(
    private val focusRepository: FocusRepository,
    private val taskRepository: TaskRepository,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _timerState = MutableStateFlow(FocusTimerState())
    private val _showCompletionDialog = MutableStateFlow(false)
    private val _completionNote = MutableStateFlow("")
    private val _markTaskAsDone = MutableStateFlow(false)

    private var timerJob: Job? = null
    private var targetEndTimeMillis: Long? = null
    private var isSavingSession = false

    val uiState: StateFlow<FocusUiState> = combine(
        _timerState,
        taskRepository.observePendingTasks(),
        learningRepository.observeTopics(),
        focusRepository.observeTodaySessions(),
        focusRepository.observeAllSessions(),
        _showCompletionDialog,
        _completionNote,
        _markTaskAsDone
    ) { args: Array<Any?> ->
        val timer = args[0] as FocusTimerState
        @Suppress("UNCHECKED_CAST")
        val tasks = args[1] as List<TaskItem>
        @Suppress("UNCHECKED_CAST")
        val topics = args[2] as List<LearningTopicItem>
        @Suppress("UNCHECKED_CAST")
        val todaySessions = args[3] as List<FocusSessionItem>
        @Suppress("UNCHECKED_CAST")
        val allSessions = args[4] as List<FocusSessionItem>
        val showDialog = args[5] as Boolean
        val note = args[6] as String
        val markDone = args[7] as Boolean

        FocusUiState(
            timerState = timer.copy(
                sessionsCompletedToday = todaySessions.count { it.completed },
                totalFocusMinutesToday = todaySessions.filter { it.completed }.sumOf { it.durationMinutes }
            ),
            availableTasks = tasks,
            availableTopics = topics,
            todaySessions = todaySessions,
            allSessions = allSessions,
            showCompletionDialog = showDialog,
            completionNote = note,
            markTaskAsDone = markDone
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FocusUiState()
    )

    fun setMode(mode: FocusMode) {
        pauseTimer()
        val totalSecs = mode.defaultMinutes * 60
        _timerState.update {
            it.copy(
                mode = mode,
                totalSeconds = totalSecs,
                remainingSeconds = totalSecs,
                timerStatus = FocusTimerStatus.IDLE
            )
        }
    }

    fun setCustomDuration(minutes: Int) {
        if (minutes <= 0) return
        pauseTimer()
        val totalSecs = minutes * 60
        _timerState.update {
            it.copy(
                customMinutes = minutes,
                totalSeconds = totalSecs,
                remainingSeconds = totalSecs,
                timerStatus = FocusTimerStatus.IDLE
            )
        }
    }

    fun selectTask(taskId: String?, taskTitle: String?) {
        _timerState.update {
            it.copy(
                linkedTaskId = taskId,
                linkedTaskTitle = taskTitle
            )
        }
    }

    fun selectTopic(topicId: String?, topicTitle: String?) {
        _timerState.update {
            it.copy(
                linkedTopicId = topicId,
                linkedTopicTitle = topicTitle
            )
        }
    }

    fun startTimer() {
        if (_timerState.value.timerStatus == FocusTimerStatus.RUNNING || isSavingSession) return

        val remainingSecs = _timerState.value.remainingSeconds
        if (remainingSecs <= 0) return

        targetEndTimeMillis = System.currentTimeMillis() + (remainingSecs * 1000L)
        _timerState.update { it.copy(timerStatus = FocusTimerStatus.RUNNING) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(500L)
                val target = targetEndTimeMillis ?: break
                val now = System.currentTimeMillis()
                val diffMs = target - now
                val remaining = ((diffMs + 999) / 1000).coerceAtLeast(0).toInt()

                if (diffMs <= 0) {
                    targetEndTimeMillis = null
                    _timerState.update { it.copy(timerStatus = FocusTimerStatus.FINISHED, remainingSeconds = 0) }
                    _showCompletionDialog.value = true
                    break
                } else {
                    _timerState.update { current ->
                        if (current.timerStatus != FocusTimerStatus.RUNNING) current
                        else current.copy(remainingSeconds = remaining)
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        val target = targetEndTimeMillis
        if (target != null) {
            val remaining = ((target - System.currentTimeMillis() + 999) / 1000).coerceAtLeast(0).toInt()
            targetEndTimeMillis = null
            _timerState.update {
                if (it.timerStatus == FocusTimerStatus.RUNNING) {
                    it.copy(timerStatus = FocusTimerStatus.PAUSED, remainingSeconds = remaining)
                } else it
            }
        } else {
            _timerState.update {
                if (it.timerStatus == FocusTimerStatus.RUNNING) {
                    it.copy(timerStatus = FocusTimerStatus.PAUSED)
                } else it
            }
        }
    }

    fun resetTimer() {
        pauseTimer()
        targetEndTimeMillis = null
        _timerState.update {
            it.copy(
                remainingSeconds = it.totalSeconds,
                timerStatus = FocusTimerStatus.IDLE
            )
        }
    }

    fun triggerFinishManually() {
        pauseTimer()
        _timerState.update { it.copy(timerStatus = FocusTimerStatus.FINISHED) }
        _showCompletionDialog.value = true
    }

    fun dismissCompletionDialog() {
        _showCompletionDialog.value = false
        _completionNote.value = ""
        _markTaskAsDone.value = false
        resetTimer()
    }

    fun updateCompletionNote(note: String) {
        _completionNote.value = note
    }

    fun toggleMarkTaskAsDone(checked: Boolean) {
        _markTaskAsDone.value = checked
    }

    fun confirmSaveCompletedSession() {
        if (isSavingSession) return
        val current = _timerState.value
        val elapsedSeconds = current.totalSeconds - current.remainingSeconds
        val elapsedMinutes = elapsedSeconds / 60

        // Prevent saving empty or fraudulent sessions where the user worked less than 1 minute
        if (elapsedMinutes <= 0) {
            _showCompletionDialog.value = false
            _completionNote.value = ""
            _markTaskAsDone.value = false
            resetTimer()
            return
        }

        isSavingSession = true
        val note = _completionNote.value
        val markTask = _markTaskAsDone.value && current.linkedTaskId != null

        viewModelScope.launch {
            try {
                focusRepository.recordFocusSession(
                    mode = current.mode,
                    durationMinutes = elapsedMinutes,
                    title = if (current.mode == FocusMode.WORK) {
                        current.linkedTaskTitle ?: current.linkedTopicTitle ?: "Bloque de enfoque"
                    } else current.mode.labelEs,
                    linkedTaskId = current.linkedTaskId,
                    linkedTopicId = current.linkedTopicId,
                    notes = note,
                    markTaskCompleted = markTask
                )
            } finally {
                isSavingSession = false
                _showCompletionDialog.value = false
                _completionNote.value = ""
                _markTaskAsDone.value = false
                resetTimer()
            }
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            focusRepository.deleteSession(id)
        }
    }
}

class FocusViewModelFactory(
    private val focusRepository: FocusRepository,
    private val taskRepository: TaskRepository,
    private val learningRepository: LearningRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FocusViewModel(focusRepository, taskRepository, learningRepository) as T
    }
}
