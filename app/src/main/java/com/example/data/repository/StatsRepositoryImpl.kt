package com.example.data.repository

import com.example.data.local.dao.FocusDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.LearningDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.WeeklyReviewDao
import com.example.data.local.dao.WellnessDao
import com.example.data.local.entity.WeeklyReviewEntity
import com.example.domain.model.DayMetricPoint
import com.example.domain.model.GoalStatus
import com.example.domain.model.WeeklyReviewItem
import com.example.domain.model.WeeklyStatsSummary
import com.example.domain.repository.StatsRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class StatsRepositoryImpl(
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val wellnessDao: WellnessDao,
    private val learningDao: LearningDao,
    private val goalDao: GoalDao,
    private val projectDao: ProjectDao,
    private val focusDao: FocusDao,
    private val weeklyReviewDao: WeeklyReviewDao
) : StatsRepository {

    override fun observeWeeklyStats(startEpochDay: Long, endEpochDay: Long): Flow<WeeklyStatsSummary> {
        val startMillis = startEpochDay * 86400000L
        val endMillis = (endEpochDay + 1) * 86400000L - 1

        val flow1 = combine(
            taskDao.observeCompletedTasksBetween(startMillis, endMillis),
            taskDao.observeCreatedTasksBetween(startMillis, endMillis),
            habitDao.observeActiveHabits(),
            habitDao.observeLogsBetween(startEpochDay, endEpochDay)
        ) { completedTasks, createdTasks, activeHabits, habitLogs ->
            Tuple4(completedTasks, createdTasks, activeHabits, habitLogs)
        }

        val flow2 = combine(
            wellnessDao.observeWaterLogsBetween(startEpochDay, endEpochDay),
            learningDao.observeSessionsBetween(startEpochDay, endEpochDay),
            focusDao.observeSessionsBetween(startEpochDay, endEpochDay),
            goalDao.observeAllGoals(),
            projectDao.observeActiveProjects()
        ) { waterLogs, learningSessions, focusSessions, goals, projects ->
            Tuple5(waterLogs, learningSessions, focusSessions, goals, projects)
        }

        return combine(flow1, flow2) { t4, t5 ->
            val completedTasks = t4.a
            val createdTasks = t4.b
            val activeHabits = t4.c
            val habitLogs = t4.d

            val waterLogs = t5.a
            val learningSessions = t5.b
            val focusSessions = t5.c
            val goals = t5.d
            val projects = t5.e

            val totalDays = ((endEpochDay - startEpochDay) + 1).coerceAtLeast(1)
            val totalCompleted = completedTasks.size
            val totalCreated = createdTasks.size
            val allRelevantTasks = (createdTasks + completedTasks).distinctBy { it.id }
            val completionRate: Float? = if (allRelevantTasks.isEmpty()) {
                null
            } else {
                (totalCompleted.toFloat() / allRelevantTasks.size.toFloat()).coerceIn(0f, 1f)
            }

            var totalExpectedHabitLogs = 0
            for (habit in activeHabits) {
                val freq = com.example.domain.model.HabitFrequency.fromString(habit.frequency)
                for (day in startEpochDay..endEpochDay) {
                    val dayOfWeek = java.time.LocalDate.ofEpochDay(day).dayOfWeek
                    val isExpected = when (freq) {
                        com.example.domain.model.HabitFrequency.DAILY -> true
                        com.example.domain.model.HabitFrequency.WEEKDAYS ->
                            dayOfWeek != java.time.DayOfWeek.SATURDAY && dayOfWeek != java.time.DayOfWeek.SUNDAY
                        com.example.domain.model.HabitFrequency.WEEKENDS ->
                            dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY
                        com.example.domain.model.HabitFrequency.CUSTOM -> true
                    }
                    if (isExpected) {
                        totalExpectedHabitLogs++
                    }
                }
            }
            val successfulHabitLogs = habitLogs.count { it.completed }
            val consistencyRate: Float? = if (activeHabits.isEmpty() || totalExpectedHabitLogs == 0) {
                null
            } else {
                (successfulHabitLogs.toFloat() / totalExpectedHabitLogs.toFloat()).coerceIn(0f, 1f)
            }

            val totalStudyMinutes = learningSessions.sumOf { it.durationMinutes }
            val totalFocusMinutes = focusSessions.filter { it.completed }.sumOf { it.durationMinutes }
            val totalWaterMl = waterLogs.sumOf { it.numericValue.toInt() }
            val avgWaterMl = (totalWaterMl / totalDays).toInt()

            val dayFormatter = SimpleDateFormat("EEE", Locale("es", "ES"))
            val dailyPoints = (startEpochDay..endEpochDay).map { epochDay ->
                val dayStartMillis = epochDay * 86400000L
                val dayEndMillis = (epochDay + 1) * 86400000L - 1
                val cal = Calendar.getInstance().apply { timeInMillis = dayStartMillis }
                val label = dayFormatter.format(cal.time).replace(".", "").replaceFirstChar { it.uppercase() }

                val dayTasks = completedTasks.count {
                    it.completedAt != null && it.completedAt in dayStartMillis..dayEndMillis
                }
                val dayHabits = habitLogs.count { it.dateEpochDay == epochDay && it.completed }
                val dayWater = waterLogs.filter { it.dateEpochDay == epochDay }.sumOf { it.numericValue.toInt() }
                val dayStudy = learningSessions.filter { it.dateEpochDay == epochDay }.sumOf { it.durationMinutes }
                val dayFocus = focusSessions.filter { it.dateEpochDay == epochDay && it.completed }.sumOf { it.durationMinutes }

                DayMetricPoint(
                    dateLabel = label,
                    epochDay = epochDay,
                    tasksCompleted = dayTasks,
                    habitsCompleted = dayHabits,
                    waterMl = dayWater,
                    studyMinutes = dayStudy,
                    focusMinutes = dayFocus
                )
            }

            val activeGoals = goals.count { it.status == GoalStatus.ACTIVE.name }

            WeeklyStatsSummary(
                weekStartEpochDay = startEpochDay,
                weekEndEpochDay = endEpochDay,
                totalTasksCompleted = totalCompleted,
                totalTasksCreated = totalCreated,
                taskCompletionRate = completionRate,
                habitsConsistencyRate = consistencyRate,
                totalHabitsLogged = successfulHabitLogs,
                activeHabitsCount = activeHabits.size,
                totalStudyMinutes = totalStudyMinutes,
                totalFocusMinutes = totalFocusMinutes,
                averageWaterMl = avgWaterMl,
                dailyPoints = dailyPoints,
                activeGoalsCount = activeGoals,
                activeProjectsCount = projects.size
            )
        }
    }

    override fun observeAllReviews(): Flow<List<WeeklyReviewItem>> {
        return weeklyReviewDao.observeAllReviews().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeReviewForWeek(weekStartEpochDay: Long): Flow<WeeklyReviewItem?> {
        return weeklyReviewDao.observeReviewForWeek(weekStartEpochDay).map { it?.toDomain() }
    }

    override suspend fun saveReview(review: WeeklyReviewItem) {
        val entity = WeeklyReviewEntity(
            id = if (review.id.isBlank()) UUID.randomUUID().toString() else review.id,
            weekStartEpochDay = review.weekStartEpochDay,
            weekEndEpochDay = review.weekEndEpochDay,
            accomplishments = review.accomplishments,
            challenges = review.challenges,
            nextWeekFocus = review.nextWeekFocus,
            notes = review.notes,
            energyRating = review.energyRating,
            productivityRating = review.productivityRating,
            createdAt = review.createdAt
        )
        weeklyReviewDao.insertOrUpdateReview(entity)
    }

    override suspend fun deleteReview(id: String) {
        weeklyReviewDao.deleteReviewById(id)
    }

    override suspend fun generateReviewDraft(weekStartEpochDay: Long, weekEndEpochDay: Long): String {
        val stats = observeWeeklyStats(weekStartEpochDay, weekEndEpochDay).first()

        val startCal = Calendar.getInstance().apply { timeInMillis = weekStartEpochDay * 86400000L }
        val endCal = Calendar.getInstance().apply { timeInMillis = weekEndEpochDay * 86400000L }
        val dateFmt = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
        val rangeStr = "${dateFmt.format(startCal.time)} al ${dateFmt.format(endCal.time)}"

        val consistencyStr = if (stats.habitsConsistencyRate != null) {
            "${(stats.habitsConsistencyRate * 100).toInt()}%"
        } else "Sin hábitos activos"

        val taskCompletionStr = if (stats.taskCompletionRate != null) {
            "${(stats.taskCompletionRate * 100).toInt()}%"
        } else "Sin tareas registradas"

        val studyHours = stats.totalStudyMinutes / 60
        val studyMins = stats.totalStudyMinutes % 60
        val focusHours = stats.totalFocusMinutes / 60
        val focusMins = stats.totalFocusMinutes % 60

        return buildString {
            appendLine("## 📊 Revisión Semanal ($rangeStr)")
            appendLine()
            appendLine("### 📈 Resumen Cuantitativo Real:")
            appendLine("• Tareas completadas: ${stats.totalTasksCompleted} (Tasa: $taskCompletionStr)")
            appendLine("• Hábitos cumplidos: ${stats.totalHabitsLogged} registros (Consistencia: $consistencyStr)")
            appendLine("• Estudio y Aprendizaje: ${studyHours}h ${studyMins}m dedicados")
            appendLine("• Enfoque Profundo (Pomodoro): ${focusHours}h ${focusMins}m")
            appendLine("• Hidratación promedio: ${stats.averageWaterMl} ml/día")
            appendLine("• Metas activas en marcha: ${stats.activeGoalsCount}")
            appendLine()
            appendLine("### 🏆 Logros y Avances Destacados:")
            if (stats.totalTasksCompleted > 0) {
                appendLine("- Se completaron exitosamente ${stats.totalTasksCompleted} tareas.")
            }
            if (stats.totalFocusMinutes > 0) {
                appendLine("- Se acumularon ${focusHours}h ${focusMins}m de concentración profunda.")
            }
            if (stats.totalStudyMinutes > 0) {
                appendLine("- Se dedicaron ${studyHours}h ${studyMins}m al aprendizaje.")
            }
            if (stats.totalTasksCompleted == 0 && stats.totalFocusMinutes == 0 && stats.totalStudyMinutes == 0) {
                appendLine("- (Espacio para registrar tus logros personales de la semana)")
            }
            appendLine()
            appendLine("### 🧗 Retos y Fricciones Detectadas:")
            if (stats.taskCompletionRate != null && stats.taskCompletionRate < 0.5f) {
                appendLine("- La tasa de finalización de tareas fue de $taskCompletionStr; evaluar carga o posibles bloqueos.")
            } else {
                appendLine("- (Espacio para registrar dificultades u obstáculos superados)")
            }
            appendLine()
            appendLine("### 🎯 Foco Estratégico para la Próxima Semana:")
            appendLine("- (Espacio para definir las prioridades estratégicas de la siguiente semana)")
        }
    }

    private fun WeeklyReviewEntity.toDomain(): WeeklyReviewItem = WeeklyReviewItem(
        id = id,
        weekStartEpochDay = weekStartEpochDay,
        weekEndEpochDay = weekEndEpochDay,
        accomplishments = accomplishments,
        challenges = challenges,
        nextWeekFocus = nextWeekFocus,
        notes = notes,
        energyRating = energyRating,
        productivityRating = productivityRating,
        createdAt = createdAt
    )

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}
