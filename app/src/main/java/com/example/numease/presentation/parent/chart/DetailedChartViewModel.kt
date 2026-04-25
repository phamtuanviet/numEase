package com.example.numease.presentation.parent.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.StudySession
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailedChartViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<StudySession>>(emptyList())
    val sessions = _sessions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    fun loadChartData(childId: String, categoryId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Lấy 20 bài gần nhất của kĩ năng này
                val result = postgrest.from("study_sessions").select {
                    filter {
                        eq("child_profile_id", childId)
                        eq("category_id", categoryId)
                    }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(20)
                }.decodeList<StudySession>()

                // Đảo ngược mảng để vẽ biểu đồ theo trục thời gian: Cũ (Trái) -> Mới (Phải)
                _sessions.value = result.reversed()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}