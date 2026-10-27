package com.example

import android.app.Application
import com.example.data.local.L2LifeDatabase
import com.example.data.repository.CalendarRepositoryImpl
import com.example.data.repository.DiaryRepositoryImpl
import com.example.data.repository.GoalRepositoryImpl
import com.example.data.repository.HabitRepositoryImpl
import com.example.data.repository.LearningRepositoryImpl
import com.example.data.repository.NoteRepositoryImpl
import com.example.data.repository.PersonRepositoryImpl
import com.example.data.repository.PreferenceRepositoryImpl
import com.example.data.repository.ProjectRepositoryImpl
import com.example.data.repository.RoutineRepositoryImpl
import com.example.data.repository.TaskRepositoryImpl
import com.example.data.repository.WellnessRepositoryImpl
import com.example.domain.repository.CalendarRepository
import com.example.domain.repository.DiaryRepository
import com.example.domain.repository.GoalRepository
import com.example.domain.repository.HabitRepository
import com.example.domain.repository.LearningRepository
import com.example.domain.repository.NoteRepository
import com.example.domain.repository.PersonRepository
import com.example.domain.repository.PreferenceRepository
import com.example.domain.repository.ProjectRepository
import com.example.domain.repository.RoutineRepository
import com.example.domain.repository.TaskRepository
import com.example.domain.repository.WellnessRepository

import com.example.data.repository.FocusRepositoryImpl
import com.example.data.repository.StatsRepositoryImpl
import com.example.domain.repository.FocusRepository
import com.example.domain.repository.StatsRepository

class L2LifeApplication : Application() {
    lateinit var database: L2LifeDatabase
        private set

    lateinit var taskRepository: TaskRepository
        private set
    lateinit var projectRepository: ProjectRepository
        private set
    lateinit var habitRepository: HabitRepository
        private set
    lateinit var routineRepository: RoutineRepository
        private set
    lateinit var calendarRepository: CalendarRepository
        private set
    lateinit var noteRepository: NoteRepository
        private set
    lateinit var wellnessRepository: WellnessRepository
        private set
    lateinit var diaryRepository: DiaryRepository
        private set
    lateinit var goalRepository: GoalRepository
        private set
    lateinit var learningRepository: LearningRepository
        private set
    lateinit var personRepository: PersonRepository
        private set
    lateinit var preferenceRepository: PreferenceRepository
        private set
    lateinit var statsRepository: StatsRepository
        private set
    lateinit var focusRepository: FocusRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = L2LifeDatabase.getInstance(this)
        taskRepository = TaskRepositoryImpl(database.taskDao())
        projectRepository = ProjectRepositoryImpl(database.projectDao(), database.taskDao())
        habitRepository = HabitRepositoryImpl(database.habitDao())
        routineRepository = RoutineRepositoryImpl(database.routineDao())
        calendarRepository = CalendarRepositoryImpl(database.eventDao(), database.taskDao())
        noteRepository = NoteRepositoryImpl(database.noteDao())
        wellnessRepository = WellnessRepositoryImpl(database.wellnessDao())
        diaryRepository = DiaryRepositoryImpl(
            diaryDao = database.diaryDao(),
            taskDao = database.taskDao(),
            habitDao = database.habitDao(),
            wellnessDao = database.wellnessDao()
        )
        goalRepository = GoalRepositoryImpl(database.goalDao())
        learningRepository = LearningRepositoryImpl(database.learningDao())
        personRepository = PersonRepositoryImpl(database.personDao())

        statsRepository = StatsRepositoryImpl(
            taskDao = database.taskDao(),
            habitDao = database.habitDao(),
            wellnessDao = database.wellnessDao(),
            learningDao = database.learningDao(),
            goalDao = database.goalDao(),
            projectDao = database.projectDao(),
            focusDao = database.focusDao(),
            weeklyReviewDao = database.weeklyReviewDao()
        )

        focusRepository = FocusRepositoryImpl(
            focusDao = database.focusDao(),
            taskDao = database.taskDao(),
            learningDao = database.learningDao()
        )

        preferenceRepository = PreferenceRepositoryImpl(
            preferenceDao = database.preferenceDao(),
            taskDao = database.taskDao(),
            habitDao = database.habitDao(),
            noteDao = database.noteDao(),
            projectDao = database.projectDao(),
            wellnessDao = database.wellnessDao(),
            routineDao = database.routineDao(),
            eventDao = database.eventDao(),
            diaryDao = database.diaryDao(),
            goalDao = database.goalDao(),
            learningDao = database.learningDao(),
            personDao = database.personDao()
        )
    }
}

