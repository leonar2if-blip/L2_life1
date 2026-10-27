package com.example.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CalendarViewMode
import com.example.domain.model.ScheduleEntry
import com.example.domain.model.TaskItem
import com.example.domain.model.TaskStatus
import com.example.domain.repository.CalendarRepository
import com.example.domain.repository.TaskRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.map

data class CalendarUiState(
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val viewMode: CalendarViewMode = CalendarViewMode.DAY,
    val formattedPeriod: String = "",
    val scheduleEntries: List<ScheduleEntry> = emptyList()
)

class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(CalendarViewMode.DAY)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()

    private val dayFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    private val weekFormat = SimpleDateFormat("d MMM", Locale("es", "ES"))

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> = kotlinx.coroutines.flow.combine(
        _selectedDate,
        _viewMode
    ) { date: Long, mode: CalendarViewMode ->
        date to mode
    }.flatMapLatest { (date, mode) ->
        val (start, end) = calculateRange(date, mode)
        val periodText = formatPeriod(date, mode)
        calendarRepository.observeScheduleForRange(start, end).map { entries ->
            CalendarUiState(
                selectedDateMillis = date,
                viewMode = mode,
                formattedPeriod = periodText,
                scheduleEntries = entries
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    fun goToToday() {
        _selectedDate.value = System.currentTimeMillis()
    }

    fun nextPeriod() {
        val cal = Calendar.getInstance().apply { timeInMillis = _selectedDate.value }
        when (_viewMode.value) {
            CalendarViewMode.DAY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            CalendarViewMode.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            CalendarViewMode.AGENDA -> cal.add(Calendar.WEEK_OF_YEAR, 1)
        }
        _selectedDate.value = cal.timeInMillis
    }

    fun previousPeriod() {
        val cal = Calendar.getInstance().apply { timeInMillis = _selectedDate.value }
        when (_viewMode.value) {
            CalendarViewMode.DAY -> cal.add(Calendar.DAY_OF_YEAR, -1)
            CalendarViewMode.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, -1)
            CalendarViewMode.AGENDA -> cal.add(Calendar.WEEK_OF_YEAR, -1)
        }
        _selectedDate.value = cal.timeInMillis
    }

    fun createEvent(
        title: String,
        description: String,
        startHour: Int,
        durationHours: Int,
        category: String
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                timeInMillis = _selectedDate.value
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val start = cal.timeInMillis
            cal.add(Calendar.HOUR_OF_DAY, durationHours)
            val end = cal.timeInMillis

            calendarRepository.createEvent(
                title = title,
                description = description,
                startTime = start,
                endTime = end,
                category = category
            )
        }
    }

    fun toggleTask(taskId: String, currentDone: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(
                taskId,
                if (currentDone) TaskStatus.PENDING else TaskStatus.COMPLETED
            )
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            calendarRepository.deleteEvent(eventId)
        }
    }

    private fun calculateRange(dateMillis: Long, mode: CalendarViewMode): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (mode) {
            CalendarViewMode.DAY -> {
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                start to cal.timeInMillis
            }
            CalendarViewMode.WEEK, CalendarViewMode.AGENDA -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                start to cal.timeInMillis
            }
        }
    }

    private fun formatPeriod(dateMillis: Long, mode: CalendarViewMode): String {
        val date = Date(dateMillis)
        return when (mode) {
            CalendarViewMode.DAY -> dayFormat.format(date).replaceFirstChar { it.uppercase() }
            CalendarViewMode.WEEK, CalendarViewMode.AGENDA -> {
                val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val startText = weekFormat.format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, 6)
                val endText = weekFormat.format(cal.time)
                "Semana: $startText - $endText"
            }
        }
    }
}

class CalendarViewModelFactory(
    private val calendarRepository: CalendarRepository,
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalendarViewModel(calendarRepository, taskRepository) as T
    }
}
