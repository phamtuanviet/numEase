package com.example.numease.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.repository.UserPreferencesRepository
import com.example.numease.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPreferencesViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    // Chuyển Flow thành StateFlow để giao diện dễ dàng lắng nghe (Observe)
    val preferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences() // Giá trị mặc định trong lúc chờ đọc file
        )

    fun switchViewMode(isStudentMode: Boolean) {
        viewModelScope.launch {
            repository.updateViewMode(if (isStudentMode) "STUDENT" else "PARENT")
        }
    }

    fun selectChild(childId: String) {
        viewModelScope.launch {
            repository.updateCurrentChild(childId)
        }
    }

    fun clearSelectedChild() {
        viewModelScope.launch {
            repository.updateCurrentChild(null)
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            repository.updateTheme(isDark)
        }
    }

    fun toggleSound(isEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateSound(isEnabled)
        }
    }
}