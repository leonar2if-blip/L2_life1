package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DiaryDao
import com.example.data.local.dao.EventDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.LearningDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.PersonDao
import com.example.data.local.dao.PreferenceDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.RoutineDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.WellnessDao
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.DiaryEntryEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.LearningSessionEntity
import com.example.data.local.entity.LearningTopicEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.PersonEntity
import com.example.data.local.entity.PreferenceEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.RoutineEntity
import com.example.data.local.entity.RoutineStepEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity

import com.example.data.local.dao.FocusDao
import com.example.data.local.dao.WeeklyReviewDao
import com.example.data.local.entity.FocusSessionEntity
import com.example.data.local.entity.WeeklyReviewEntity

@Database(
    entities = [
        TaskEntity::class,
        ProjectEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        NoteEntity::class,
        WellnessLogEntity::class,
        PreferenceEntity::class,
        RoutineEntity::class,
        RoutineStepEntity::class,
        CalendarEventEntity::class,
        DiaryEntryEntity::class,
        GoalEntity::class,
        GoalMilestoneEntity::class,
        LearningTopicEntity::class,
        LearningSessionEntity::class,
        PersonEntity::class,
        FocusSessionEntity::class,
        WeeklyReviewEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class L2LifeDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun habitDao(): HabitDao
    abstract fun noteDao(): NoteDao
    abstract fun wellnessDao(): WellnessDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun routineDao(): RoutineDao
    abstract fun eventDao(): EventDao
    abstract fun diaryDao(): DiaryDao
    abstract fun goalDao(): GoalDao
    abstract fun learningDao(): LearningDao
    abstract fun personDao(): PersonDao
    abstract fun focusDao(): FocusDao
    abstract fun weeklyReviewDao(): WeeklyReviewDao

    companion object {
        @Volatile
        private var INSTANCE: L2LifeDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `routines` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `timeOfDaySlot` TEXT NOT NULL,
                        `estimatedMinutes` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `routine_steps` (
                        `id` TEXT NOT NULL,
                        `routineId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL,
                        `habitId` TEXT,
                        `isCompleted` INTEGER NOT NULL,
                        `isSkipped` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `calendar_events` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `startTime` INTEGER NOT NULL,
                        `endTime` INTEGER NOT NULL,
                        `isAllDay` INTEGER NOT NULL,
                        `linkedTaskId` TEXT,
                        `linkedProjectId` TEXT,
                        `colorHex` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `diary_entries` (
                        `id` TEXT NOT NULL,
                        `dateEpochDay` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `derivedSummary` TEXT NOT NULL,
                        `mood` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_diary_entries_dateEpochDay` ON `diary_entries` (`dateEpochDay`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `goals` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `timeHorizon` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `targetDate` INTEGER,
                        `metricName` TEXT NOT NULL,
                        `startValue` REAL NOT NULL,
                        `currentValue` REAL NOT NULL,
                        `targetValue` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `linkedProjectId` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_timeHorizon` ON `goals` (`timeHorizon`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_status` ON `goals` (`status`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `goal_milestones` (
                        `id` TEXT NOT NULL,
                        `goalId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `targetDate` INTEGER,
                        `isAchieved` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL,
                        `achievedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_goal_milestones_goalId_orderIndex` ON `goal_milestones` (`goalId`, `orderIndex`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `learning_topics` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `totalStudyMinutes` INTEGER NOT NULL,
                        `resourcesCount` INTEGER NOT NULL,
                        `linkedGoalId` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `learning_sessions` (
                        `id` TEXT NOT NULL,
                        `topicId` TEXT NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `dateEpochDay` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_learning_sessions_topicId` ON `learning_sessions` (`topicId`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `people` (
                        `id` TEXT NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `alias` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `importantDatesNotes` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `nextAction` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `focus_sessions` (
                        `id` TEXT NOT NULL,
                        `mode` TEXT NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `linkedTaskId` TEXT,
                        `linkedTopicId` TEXT,
                        `notes` TEXT NOT NULL,
                        `completed` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `dateEpochDay` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_sessions_dateEpochDay` ON `focus_sessions` (`dateEpochDay`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_sessions_timestamp` ON `focus_sessions` (`timestamp`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `weekly_reviews` (
                        `id` TEXT NOT NULL,
                        `weekStartEpochDay` INTEGER NOT NULL,
                        `weekEndEpochDay` INTEGER NOT NULL,
                        `accomplishments` TEXT NOT NULL,
                        `challenges` TEXT NOT NULL,
                        `nextWeekFocus` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `energyRating` INTEGER NOT NULL,
                        `productivityRating` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_weekly_reviews_weekStartEpochDay` ON `weekly_reviews` (`weekStartEpochDay`)")
            }
        }

        fun getInstance(context: Context): L2LifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    L2LifeDatabase::class.java,
                    "l2_life_database.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
