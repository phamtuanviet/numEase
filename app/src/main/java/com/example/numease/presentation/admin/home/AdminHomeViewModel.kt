package com.example.numease.presentation.admin.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminHomeViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    // Số loại bài tập (Lấy từ bảng exercise_categories)
    private val _totalExerciseTypes = MutableStateFlow(0L)
    val totalExerciseTypes = _totalExerciseTypes.asStateFlow()

    // Tổng số câu hỏi (Lấy từ bảng exercises theo như bạn mô tả)
    private val _totalQuestions = MutableStateFlow(0L)
    val totalQuestions = _totalQuestions.asStateFlow()

    private val _totalUsers = MutableStateFlow(0L)
    val totalUsers = _totalUsers.asStateFlow()

    private val _totalChildren = MutableStateFlow(0L)
    val totalChildren = _totalChildren.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _growthData = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val growthData = _growthData.asStateFlow()

    fun loadGrowthStats() {
        viewModelScope.launch {
            try {
                val tenDaysAgo = java.time.ZonedDateTime.now()
                    .minusDays(10)
                    .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                // Lấy danh sách ngày tạo của user trong 10 ngày qua
                val results = postgrest.from("user_profiles").select {
                    filter { gte("created_at", tenDaysAgo) }
                }.decodeList<UserProfile>()

                // Nhóm và đếm theo ngày
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
                val last10Days = (0..9).map { java.time.LocalDate.now().minusDays(it.toLong()) }.reversed()

                val mappedData = last10Days.map { date ->
                    val count = results.count {
                        val createdAt = java.time.ZonedDateTime.parse(it.createdAt).toLocalDate()
                        createdAt == date
                    }
                    date.format(formatter) to count
                }

                _growthData.value = mappedData
            } catch (e: Exception) {
                android.util.Log.e("AdminVM", "Lỗi tải tăng trưởng: ${e.message}")
            }
        }
    }

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sử dụng Count.EXACT để ép Supabase tính toán chính xác số lượng

                _totalExerciseTypes.value = postgrest.from("exercise_categories").select(head = true) {
                    count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                }.countOrNull() ?: 0

                _totalQuestions.value = postgrest.from("exercises").select(head = true) {
                    count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                }.countOrNull() ?: 0

                // FIX BUG ĐẾM 0: Đảm bảo bảng user_profiles và child_profiles không bị chặn bởi RLS
                // Nếu vẫn ra 0, bạn hãy kiểm tra trên Supabase Dashboard xem bảng đó đã tắt RLS
                // hoặc đã có Policy cho phép Admin (Service Role) đọc chưa nhé.
                _totalUsers.value = postgrest.from("user_profiles").select(head = true) {
                    count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                }.countOrNull() ?: 0

                _totalChildren.value = postgrest.from("child_profiles").select(head = true) {
                    count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                }.countOrNull() ?: 0

            } catch (e: Exception) {
                android.util.Log.e("AdminHomeVM", "Lỗi refresh dashboard: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}