package com.example.numease.presentation.student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.StudySession
import com.example.numease.data.repository.StudentRepository
import com.example.numease.manager.ChildSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentHomeViewModel @Inject constructor(
    private val repository: StudentRepository,
    val childSessionManager: ChildSessionManager
) : ViewModel() {

    private val _totalStars = MutableStateFlow(0)
    val totalStars: StateFlow<Int> = _totalStars.asStateFlow()

    private val _recentSessions = MutableStateFlow<List<StudySession>>(emptyList())
    val recentSessions: StateFlow<List<StudySession>> = _recentSessions.asStateFlow()

    init {
        viewModelScope.launch {
            childSessionManager.activeChild.collect { child ->
                child?.id?.let {
                    fetchTotalStars(it)
                    fetchRecentSessions(it) // Tải lịch sử khi bé hoạt động
                }
            }
        }
    }

    // Quản lý Theme và Sound (Nên dùng DataStore để lưu lâu dài, ở đây ta dùng StateFlow trước)
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    fun fetchRecentSessions(childId: String) {
        viewModelScope.launch {
            _recentSessions.value = repository.getRecentSessions(childId)
        }
    }
    private fun fetchTotalStars(childId: String) {
        viewModelScope.launch {
            _totalStars.value = repository.getTotalStars(childId)
        }
    }


}