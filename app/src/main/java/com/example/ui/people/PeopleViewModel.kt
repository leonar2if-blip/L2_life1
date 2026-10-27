package com.example.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.PersonCategory
import com.example.domain.model.PersonItem
import com.example.domain.repository.PersonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PeopleUiState(
    val people: List<PersonItem> = emptyList(),
    val selectedCategory: PersonCategory? = null
)

class PeopleViewModel(
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<PersonCategory?>(null)
    val selectedCategory: StateFlow<PersonCategory?> = _selectedCategory.asStateFlow()

    val uiState: StateFlow<PeopleUiState> = combine(
        personRepository.observePeople(),
        _selectedCategory
    ) { allPeople, category ->
        val filtered = if (category == null) allPeople else allPeople.filter { it.category == category }
        PeopleUiState(
            people = filtered,
            selectedCategory = category
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PeopleUiState()
    )

    fun filterByCategory(category: PersonCategory?) {
        _selectedCategory.value = category
    }

    fun createPerson(
        fullName: String,
        alias: String,
        category: PersonCategory,
        importantDatesNotes: String,
        notes: String,
        nextAction: String
    ) {
        viewModelScope.launch {
            personRepository.createPerson(
                fullName = fullName,
                alias = alias,
                category = category,
                importantDatesNotes = importantDatesNotes,
                notes = notes,
                nextAction = nextAction
            )
        }
    }

    fun updatePerson(person: PersonItem) {
        viewModelScope.launch {
            personRepository.updatePerson(person)
        }
    }

    fun deletePerson(id: String) {
        viewModelScope.launch {
            personRepository.deletePerson(id)
        }
    }
}

class PeopleViewModelFactory(
    private val personRepository: PersonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PeopleViewModel(personRepository) as T
    }
}
