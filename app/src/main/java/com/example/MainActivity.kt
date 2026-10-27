package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.calendar.CalendarScreen
import com.example.ui.calendar.CalendarViewModel
import com.example.ui.calendar.CalendarViewModelFactory
import com.example.ui.diary.DiaryScreen
import com.example.ui.diary.DiaryViewModel
import com.example.ui.diary.DiaryViewModelFactory
import com.example.ui.goals.GoalsScreen
import com.example.ui.goals.GoalsViewModel
import com.example.ui.goals.GoalsViewModelFactory
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.home.HomeViewModelFactory
import com.example.ui.learning.LearningScreen
import com.example.ui.learning.LearningViewModel
import com.example.ui.learning.LearningViewModelFactory
import com.example.ui.modules.ModulesScreen
import com.example.ui.navigation.L2BottomNavigationBar
import com.example.ui.navigation.Screen
import com.example.ui.people.PeopleScreen
import com.example.ui.people.PeopleViewModel
import com.example.ui.people.PeopleViewModelFactory
import com.example.ui.projects.ProjectsScreen
import com.example.ui.projects.ProjectsViewModel
import com.example.ui.projects.ProjectsViewModelFactory
import com.example.ui.routines.RoutinesScreen
import com.example.ui.routines.RoutinesViewModel
import com.example.ui.routines.RoutinesViewModelFactory
import com.example.ui.search.SearchScreen
import com.example.ui.search.SearchViewModel
import com.example.ui.search.SearchViewModelFactory
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.settings.SettingsViewModelFactory
import com.example.ui.focus.FocusScreen
import com.example.ui.focus.FocusViewModel
import com.example.ui.focus.FocusViewModelFactory
import com.example.ui.stats.StatsScreen
import com.example.ui.stats.StatsViewModel
import com.example.ui.stats.StatsViewModelFactory
import com.example.ui.theme.L2LifeTheme
import com.example.ui.theme.ThemeSetting

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as L2LifeApplication

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    preferenceRepository = app.preferenceRepository,
                    taskRepository = app.taskRepository,
                    habitRepository = app.habitRepository,
                    noteRepository = app.noteRepository,
                    wellnessRepository = app.wellnessRepository
                )
            )

            val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()
            val themeSetting = when (preferences.themeMode) {
                "LIGHT" -> ThemeSetting.LIGHT
                "DARK" -> ThemeSetting.DARK
                else -> ThemeSetting.SYSTEM
            }

            L2LifeTheme(themeSetting = themeSetting) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(
                        taskRepository = app.taskRepository,
                        habitRepository = app.habitRepository,
                        noteRepository = app.noteRepository,
                        wellnessRepository = app.wellnessRepository,
                        preferenceRepository = app.preferenceRepository
                    )
                )

                val projectsViewModel: ProjectsViewModel = viewModel(
                    factory = ProjectsViewModelFactory(
                        projectRepository = app.projectRepository,
                        taskRepository = app.taskRepository
                    )
                )

                val calendarViewModel: CalendarViewModel = viewModel(
                    factory = CalendarViewModelFactory(
                        calendarRepository = app.calendarRepository,
                        taskRepository = app.taskRepository
                    )
                )

                val routinesViewModel: RoutinesViewModel = viewModel(
                    factory = RoutinesViewModelFactory(
                        routineRepository = app.routineRepository
                    )
                )

                val searchViewModel: SearchViewModel = viewModel(
                    factory = SearchViewModelFactory(
                        taskRepository = app.taskRepository,
                        habitRepository = app.habitRepository,
                        noteRepository = app.noteRepository
                    )
                )

                val diaryViewModel: DiaryViewModel = viewModel(
                    factory = DiaryViewModelFactory(
                        diaryRepository = app.diaryRepository
                    )
                )

                val goalsViewModel: GoalsViewModel = viewModel(
                    factory = GoalsViewModelFactory(
                        goalRepository = app.goalRepository
                    )
                )

                val learningViewModel: LearningViewModel = viewModel(
                    factory = LearningViewModelFactory(
                        learningRepository = app.learningRepository
                    )
                )

                val peopleViewModel: PeopleViewModel = viewModel(
                    factory = PeopleViewModelFactory(
                        personRepository = app.personRepository
                    )
                )

                val focusViewModel: FocusViewModel = viewModel(
                    factory = FocusViewModelFactory(
                        focusRepository = app.focusRepository,
                        taskRepository = app.taskRepository,
                        learningRepository = app.learningRepository
                    )
                )

                val statsViewModel: StatsViewModel = viewModel(
                    factory = StatsViewModelFactory(
                        statsRepository = app.statsRepository
                    )
                )

                val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            L2BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                                onNavigateToModules = { navController.navigate(Screen.Modules.route) }
                            )
                        }

                        composable(Screen.Modules.route) {
                            ModulesScreen(
                                homeViewModel = homeViewModel,
                                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                                onNavigateToProjects = { navController.navigate(Screen.Projects.route) },
                                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                                onNavigateToRoutines = { navController.navigate(Screen.Routines.route) },
                                onNavigateToDiary = { navController.navigate(Screen.Diary.route) },
                                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                                onNavigateToLearning = { navController.navigate(Screen.Learning.route) },
                                onNavigateToPeople = { navController.navigate(Screen.People.route) },
                                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                                onNavigateToFocus = { navController.navigate(Screen.Focus.route) }
                            )
                        }

                        composable(Screen.Projects.route) {
                            ProjectsScreen(
                                viewModel = projectsViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Calendar.route) {
                            CalendarScreen(
                                viewModel = calendarViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Routines.route) {
                            RoutinesScreen(
                                viewModel = routinesViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Diary.route) {
                            DiaryScreen(
                                viewModel = diaryViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Goals.route) {
                            GoalsScreen(
                                viewModel = goalsViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Learning.route) {
                            LearningScreen(
                                viewModel = learningViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.People.route) {
                            PeopleScreen(
                                viewModel = peopleViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(viewModel = settingsViewModel)
                        }

                        composable(Screen.Search.route) {
                            SearchScreen(
                                viewModel = searchViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Focus.route) {
                            FocusScreen(
                                viewModel = focusViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Stats.route) {
                            StatsScreen(
                                viewModel = statsViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
