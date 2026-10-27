package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.PreferenceEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.RoutineEntity
import com.example.data.local.entity.RoutineStepEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY priority DESC, createdAt DESC")
    fun observePendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE (dueDate BETWEEN :startOfDay AND :endOfDay) OR (scheduledDate BETWEEN :startOfDay AND :endOfDay) OR (completedAt BETWEEN :startOfDay AND :endOfDay) OR (status != 'COMPLETED' AND status != 'CANCELLED') ORDER BY priority DESC, createdAt DESC")
    fun observeTasksForToday(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY priority DESC, createdAt DESC")
    fun observeTasksForProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE (dueDate BETWEEN :start AND :end) OR (scheduledDate BETWEEN :start AND :end) ORDER BY COALESCE(scheduledDate, dueDate) ASC")
    fun observeTasksBetween(start: Long, end: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' AND completedAt BETWEEN :start AND :end ORDER BY completedAt DESC")
    fun observeCompletedTasksBetween(start: Long, end: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    fun observeCreatedTasksBetween(start: Long, end: Long): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId")
    suspend fun countTasksForProject(projectId: String): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId AND status = 'COMPLETED'")
    suspend fun countCompletedTasksForProject(projectId: String): Int

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String, completedAt: Long?)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("SELECT COUNT(*) FROM tasks WHERE status != 'COMPLETED' AND status != 'CANCELLED'")
    fun observePendingTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED' AND completedAt >= :startOfDay")
    fun observeCompletedTodayCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countAllTasks(): Int

    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun observeActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun observeAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun observeProjectById(id: String): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun countAllProjects(): Int

    @Query("DELETE FROM projects")
    suspend fun clearAll()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateEpochDay = :dateEpochDay")
    fun observeLogsForDate(dateEpochDay: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun observeLogsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getLog(habitId: String, dateEpochDay: Long): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay")
    suspend fun deleteLog(habitId: String, dateEpochDay: Long)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: String)

    @Query("SELECT COUNT(*) FROM habits WHERE isArchived = 0")
    suspend fun countActiveHabits(): Int

    @Query("DELETE FROM habits")
    suspend fun clearAll()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun countAllNotes(): Int

    @Query("DELETE FROM notes")
    suspend fun clearAll()
}

@Dao
interface WellnessDao {
    @Query("SELECT * FROM wellness_logs WHERE type = 'WATER' AND dateEpochDay = :dateEpochDay ORDER BY timestamp DESC")
    fun observeWaterLogsForDate(dateEpochDay: Long): Flow<List<WellnessLogEntity>>

    @Query("SELECT * FROM wellness_logs WHERE type = 'WATER' AND dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY timestamp ASC")
    fun observeWaterLogsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<WellnessLogEntity>>

    @Query("SELECT * FROM wellness_logs WHERE dateEpochDay = :dateEpochDay ORDER BY timestamp DESC")
    fun observeAllLogsForDate(dateEpochDay: Long): Flow<List<WellnessLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WellnessLogEntity)

    @Query("DELETE FROM wellness_logs WHERE id = :id")
    suspend fun deleteLogById(id: String)

    @Query("DELETE FROM wellness_logs WHERE id IN (SELECT id FROM wellness_logs WHERE type = 'WATER' AND dateEpochDay = :dateEpochDay ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteLatestWaterLog(dateEpochDay: Long)

    @Query("DELETE FROM wellness_logs")
    suspend fun clearAll()
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY createdAt ASC")
    fun observeAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: String): RoutineEntity?

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    fun observeRoutineById(id: String): Flow<RoutineEntity?>

    @Query("SELECT * FROM routine_steps WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun observeStepsForRoutine(routineId: String): Flow<List<RoutineStepEntity>>

    @Query("SELECT * FROM routine_steps WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getStepsForRoutine(routineId: String): List<RoutineStepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<RoutineStepEntity>)

    @Query("UPDATE routine_steps SET isCompleted = :isCompleted, isSkipped = :isSkipped WHERE id = :stepId")
    suspend fun updateStepStatus(stepId: String, isCompleted: Boolean, isSkipped: Boolean)

    @Query("UPDATE routine_steps SET isCompleted = 0, isSkipped = 0 WHERE routineId = :routineId")
    suspend fun resetRoutineSteps(routineId: String)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: String)

    @Query("DELETE FROM routine_steps WHERE routineId = :routineId")
    suspend fun deleteStepsForRoutine(routineId: String)

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun countAllRoutines(): Int

    @Query("DELETE FROM routines")
    suspend fun clearAllRoutines()

    @Query("DELETE FROM routine_steps")
    suspend fun clearAllRoutineSteps()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM calendar_events WHERE (startTime BETWEEN :start AND :end) OR (endTime BETWEEN :start AND :end) OR (startTime <= :start AND endTime >= :end) ORDER BY startTime ASC")
    fun observeEventsBetween(start: Long, end: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events ORDER BY startTime ASC")
    fun observeAllEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: String)

    @Query("SELECT COUNT(*) FROM calendar_events")
    suspend fun countAllEvents(): Int

    @Query("DELETE FROM calendar_events")
    suspend fun clearAll()
}

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM user_preferences WHERE `key` = :key LIMIT 1")
    fun observePreference(key: String): Flow<PreferenceEntity?>

    @Query("SELECT * FROM user_preferences WHERE `key` = :key LIMIT 1")
    suspend fun getPreference(key: String): PreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(preference: PreferenceEntity)

    @Query("SELECT * FROM user_preferences")
    fun observeAllPreferences(): Flow<List<PreferenceEntity>>

    @Query("DELETE FROM user_preferences")
    suspend fun clearAll()
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY dateEpochDay DESC")
    fun observeAllDiaryEntries(): Flow<List<com.example.data.local.entity.DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getDiaryEntryByDate(dateEpochDay: Long): com.example.data.local.entity.DiaryEntryEntity?

    @Query("SELECT * FROM diary_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun observeDiaryEntryByDate(dateEpochDay: Long): Flow<com.example.data.local.entity.DiaryEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: com.example.data.local.entity.DiaryEntryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteDiaryEntryById(id: String)

    @Query("SELECT COUNT(*) FROM diary_entries")
    suspend fun countAllDiaryEntries(): Int

    @Query("DELETE FROM diary_entries")
    suspend fun clearAllDiaryEntries()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun observeAllGoals(): Flow<List<com.example.data.local.entity.GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: String): com.example.data.local.entity.GoalEntity?

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    fun observeGoalById(id: String): Flow<com.example.data.local.entity.GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: com.example.data.local.entity.GoalEntity)

    @Update
    suspend fun updateGoal(goal: com.example.data.local.entity.GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: String)

    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY orderIndex ASC")
    fun observeMilestonesForGoal(goalId: String): Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>

    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY orderIndex ASC")
    suspend fun getMilestonesForGoal(goalId: String): List<com.example.data.local.entity.GoalMilestoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: com.example.data.local.entity.GoalMilestoneEntity)

    @Query("UPDATE goal_milestones SET isAchieved = :isAchieved, achievedAt = :achievedAt WHERE id = :milestoneId")
    suspend fun updateMilestoneStatus(milestoneId: String, isAchieved: Boolean, achievedAt: Long?)

    @Query("DELETE FROM goal_milestones WHERE goalId = :goalId")
    suspend fun deleteMilestonesForGoal(goalId: String)

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun countAllGoals(): Int

    @Query("DELETE FROM goals")
    suspend fun clearAllGoals()

    @Query("DELETE FROM goal_milestones")
    suspend fun clearAllMilestones()
}

@Dao
interface LearningDao {
    @Query("SELECT * FROM learning_topics ORDER BY updatedAt DESC")
    fun observeAllTopics(): Flow<List<com.example.data.local.entity.LearningTopicEntity>>

    @Query("SELECT * FROM learning_topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: String): com.example.data.local.entity.LearningTopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: com.example.data.local.entity.LearningTopicEntity)

    @Query("UPDATE learning_topics SET totalStudyMinutes = totalStudyMinutes + :minutesToAdd, updatedAt = :now WHERE id = :topicId")
    suspend fun addStudyMinutes(topicId: String, minutesToAdd: Int, now: Long)

    @Query("DELETE FROM learning_topics WHERE id = :id")
    suspend fun deleteTopicById(id: String)

    @Query("DELETE FROM learning_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    @Query("SELECT * FROM learning_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): com.example.data.local.entity.LearningSessionEntity?

    @Query("UPDATE learning_topics SET totalStudyMinutes = MAX(0, totalStudyMinutes - :minutesToSubtract), updatedAt = :now WHERE id = :topicId")
    suspend fun subtractStudyMinutes(topicId: String, minutesToSubtract: Int, now: Long)

    @Query("SELECT * FROM learning_sessions WHERE topicId = :topicId ORDER BY timestamp DESC")
    fun observeSessionsForTopic(topicId: String): Flow<List<com.example.data.local.entity.LearningSessionEntity>>

    @Query("SELECT * FROM learning_sessions WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY timestamp DESC")
    fun observeSessionsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<com.example.data.local.entity.LearningSessionEntity>>

    @Query("SELECT * FROM learning_sessions ORDER BY timestamp DESC")
    fun observeAllSessions(): Flow<List<com.example.data.local.entity.LearningSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: com.example.data.local.entity.LearningSessionEntity)

    @Query("SELECT COUNT(*) FROM learning_topics")
    suspend fun countAllTopics(): Int

    @Query("DELETE FROM learning_topics")
    suspend fun clearAllTopics()

    @Query("DELETE FROM learning_sessions")
    suspend fun clearAllSessions()
}

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY fullName ASC")
    fun observeAllPeople(): Flow<List<com.example.data.local.entity.PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :id LIMIT 1")
    suspend fun getPersonById(id: String): com.example.data.local.entity.PersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: com.example.data.local.entity.PersonEntity)

    @Update
    suspend fun updatePerson(person: com.example.data.local.entity.PersonEntity)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deletePersonById(id: String)

    @Query("SELECT COUNT(*) FROM people")
    suspend fun countAllPeople(): Int

    @Query("DELETE FROM people")
    suspend fun clearAllPeople()
}

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun observeAllSessions(): Flow<List<com.example.data.local.entity.FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE dateEpochDay = :dateEpochDay ORDER BY timestamp DESC")
    fun observeSessionsForDate(dateEpochDay: Long): Flow<List<com.example.data.local.entity.FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY timestamp DESC")
    fun observeSessionsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<com.example.data.local.entity.FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: com.example.data.local.entity.FocusSessionEntity)

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    @Query("SELECT * FROM focus_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): com.example.data.local.entity.FocusSessionEntity?

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1 AND dateEpochDay = :dateEpochDay")
    fun observeTodayCompletedCount(dateEpochDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions WHERE completed = 1 AND dateEpochDay = :dateEpochDay")
    fun observeTodayFocusMinutes(dateEpochDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM focus_sessions")
    suspend fun countAllSessions(): Int

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}

@Dao
interface WeeklyReviewDao {
    @Query("SELECT * FROM weekly_reviews ORDER BY weekStartEpochDay DESC")
    fun observeAllReviews(): Flow<List<com.example.data.local.entity.WeeklyReviewEntity>>

    @Query("SELECT * FROM weekly_reviews WHERE weekStartEpochDay = :weekStartEpochDay LIMIT 1")
    fun observeReviewForWeek(weekStartEpochDay: Long): Flow<com.example.data.local.entity.WeeklyReviewEntity?>

    @Query("SELECT * FROM weekly_reviews WHERE weekStartEpochDay = :weekStartEpochDay LIMIT 1")
    suspend fun getReviewForWeek(weekStartEpochDay: Long): com.example.data.local.entity.WeeklyReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReview(review: com.example.data.local.entity.WeeklyReviewEntity)

    @Query("DELETE FROM weekly_reviews WHERE id = :id")
    suspend fun deleteReviewById(id: String)

    @Query("SELECT COUNT(*) FROM weekly_reviews")
    suspend fun countAllReviews(): Int

    @Query("DELETE FROM weekly_reviews")
    suspend fun clearAll()
}

