package com.example.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Priority
import com.example.domain.model.ProjectDetail
import com.example.domain.model.ProjectItem
import com.example.domain.model.ProjectStatus
import com.example.domain.model.TaskItem
import com.example.domain.model.TaskStatus
import com.example.domain.repository.ProjectRepository
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjectsUiState(
    val projects: List<ProjectDetail> = emptyList(),
    val selectedProjectDetail: ProjectDetail? = null,
    val selectedProjectTasks: List<TaskItem> = emptyList()
)

class ProjectsViewModel(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId: StateFlow<String?> = _selectedProjectId.asStateFlow()

    val uiState: StateFlow<ProjectsUiState> = combine(
        projectRepository.observeActiveProjectsWithStats(),
        _selectedProjectId,
        taskRepository.observeAllTasks()
    ) { projects, selectedId, allTasks ->
        val selectedDetail = projects.find { it.project.id == selectedId }
        val projectTasks = if (selectedId != null) {
            allTasks.filter { it.projectId == selectedId }
        } else emptyList()

        ProjectsUiState(
            projects = projects,
            selectedProjectDetail = selectedDetail,
            selectedProjectTasks = projectTasks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProjectsUiState()
    )

    fun selectProject(projectId: String?) {
        _selectedProjectId.value = projectId
    }

    fun createProject(
        name: String,
        description: String,
        colorHex: String,
        category: String
    ) {
        viewModelScope.launch {
            projectRepository.createProject(
                name = name,
                description = description,
                colorHex = colorHex,
                category = category
            )
        }
    }

    fun addTaskToProject(
        projectId: String,
        title: String,
        priority: Priority
    ) {
        viewModelScope.launch {
            taskRepository.createTask(
                title = title,
                priority = priority,
                projectId = projectId,
                category = "Proyecto"
            )
        }
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            val nextStatus = if (task.isCompleted) TaskStatus.PENDING else TaskStatus.COMPLETED
            taskRepository.updateTaskStatus(task.id, nextStatus)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            if (_selectedProjectId.value == projectId) {
                _selectedProjectId.value = null
            }
            projectRepository.deleteProject(projectId)
        }
    }
}

class ProjectsViewModelFactory(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProjectsViewModel(projectRepository, taskRepository) as T
    }
}
