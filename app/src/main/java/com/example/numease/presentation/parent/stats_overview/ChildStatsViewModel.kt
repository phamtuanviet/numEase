package com.example.numease.presentation.parent.stats_overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ChildProfile
import com.example.numease.data.model.StudySession
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChildStatsViewModel @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) : ViewModel() {

    // Thông tin cơ bản
    private val _currentChild = MutableStateFlow<ChildProfile?>(null)
    val currentChild = _currentChild.asStateFlow()

    private val _allChildren = MutableStateFlow<List<ChildProfile>>(emptyList())
    val allChildren = _allChildren.asStateFlow()

    // Trạng thái kĩ năng đang được chọn (Mặc định: 1 - Đếm số)
    private val _selectedCategoryId = MutableStateFlow(1)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    // Dữ liệu thô: Toàn bộ lịch sử học của bé hiện tại
    private val _allSessionsOfChild = MutableStateFlow<List<StudySession>>(emptyList())

    // Dữ liệu đã xử lý (Dùng cho UI)
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // 1. Tiến độ của kĩ năng đang chọn: Pair(Số câu đúng, Tổng số câu)
    val currentCategoryProgress: StateFlow<Pair<Int, Int>> = combine(
        _allSessionsOfChild, _selectedCategoryId
    ) { sessions, categoryId ->
        val filtered = sessions.filter { it.categoryId == categoryId }
        val totalCorrect = filtered.sumOf { it.correctAnswers }
        val totalQuestions = filtered.sumOf { it.totalQuestions }
        Pair(totalCorrect, totalQuestions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

    // 2. 10 bài gần nhất của kĩ năng đang chọn (Cho biểu đồ)
    val recentSessionsForCategory: StateFlow<List<StudySession>> = combine(
        _allSessionsOfChild, _selectedCategoryId
    ) { sessions, categoryId ->
        sessions.filter { it.categoryId == categoryId }
            .sortedByDescending { it.createdAt }
            .take(10)
            .reversed() // Đảo lại để vẽ biểu đồ từ trái (cũ) sang phải (mới)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================

    fun initData(childId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUserOrNull() ?: return@launch

                // 1. Lấy danh sách toàn bộ các bé để làm thanh Switcher bên dưới
                val childrenList = postgrest.from("child_profiles").select {
                    filter { eq("account_id", user.id) }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }.decodeList<ChildProfile>()
                _allChildren.value = childrenList

                // 2. Tìm bé hiện tại trong danh sách
                val child = childrenList.find { it.id == childId }
                _currentChild.value = child

                // 3. Tải TOÀN BỘ lịch sử bài tập của bé này
                val sessions = postgrest.from("study_sessions").select {
                    filter { eq("child_profile_id", childId) }
                }.decodeList<StudySession>()

                _allSessionsOfChild.value = sessions

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Đổi Kĩ năng muốn xem
    fun selectCategory(categoryId: Int) {
        _selectedCategoryId.value = categoryId
    }

    // Đổi sang xem thống kê của bé khác
    fun switchChild(newChildId: String) {
        _selectedCategoryId.value = 1 // Reset về "Đếm số" mỗi khi đổi bé
        initData(newChildId)
    }
}