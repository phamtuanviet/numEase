package com.example.numease.presentation.admin.children_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.dto.LevelRecordDto
import com.example.numease.data.dto.StudySessionDto
import com.example.numease.data.model.ChildProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChildDetailViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    // Trạng thái lưu trữ
    private val _childProfile = MutableStateFlow<ChildProfile?>(null)
    val childProfile = _childProfile.asStateFlow()

    private val _totalStars = MutableStateFlow(0)
    val totalStars = _totalStars.asStateFlow()

    private val _recentSessions = MutableStateFlow<List<StudySessionDto>>(emptyList())
    val recentSessions = _recentSessions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

//    fun loadChildDetails(childId: String) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                // 1. Lấy thông tin cơ bản
//                val profile = postgrest.from("child_profiles").select {
//                    filter { eq("id", childId) }
//                }.decodeSingle<ChildProfile>()
//                _childProfile.value = profile
//
//                // 2. Lấy TỔNG SAO từ bảng level_records
//                val records = postgrest.from("level_records").select(
//                    columns = Columns.list("stars")
//                ) {
//                    filter { eq("child_id", childId) }
//                }.decodeList<LevelRecordDto>()
//                _totalStars.value = records.sumOf { it.stars }
//
//                // 3. Lấy 15 BÀI TẬP GẦN NHẤT từ bảng study_sessions
//                val sessions = postgrest.from("study_sessions").select(
//                    columns = Columns.list("accuracy", "created_at")
//                ) {
//                    filter { eq("child_id", childId) }
//                    order("created_at", order = Order.DESCENDING) // Lấy mới nhất
//                    limit(15) // Giới hạn 15 bài
//                }.decodeList<StudySessionDto>()
//
//                // Đảo ngược list để vẽ biểu đồ từ trái (cũ) sang phải (mới nhất)
//                _recentSessions.value = sessions.reversed()
//
//            } catch (e: Exception) {
//                android.util.Log.e("ChildDetailVM", "Lỗi: ${e.message}")
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }

    fun loadChildDetails(childId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("ChildDetailVM", "Đã gọi từ loadChildDetails")
                // 1. Lấy thông tin cơ bản
                val profile = postgrest.from("child_profiles").select {
                    filter { eq("id", childId) }
                }.decodeSingle<ChildProfile>()
                _childProfile.value = profile

                // 2. Debug TỔNG SAO
                android.util.Log.d("ChildDetailDebug", "Đang tìm sao cho childId: $childId")
                val starResponse = postgrest.from("level_records").select(
                    columns = Columns.list("stars")
                ) {
                    filter { eq("child_profile_id", childId) }
                }
                val records = starResponse.decodeList<LevelRecordDto>()

                // Log để bạn kiểm tra xem lần này đã tìm thấy dòng nào chưa
                android.util.Log.d("ChildDetailDebug", "Số bản ghi sao tìm thấy: ${records.size}")

                _totalStars.value = records.sumOf { it.stars }

                // 3. Debug 15 BÀI GẦN NHẤT
                val sessionResponse = postgrest.from("study_sessions").select(
                    columns = Columns.list("accuracy", "created_at")
                ) {
                    filter { eq("child_profile_id", childId) } // CHÚ Ý: Kiểm tra tên cột 'child_id'
                    order("created_at", order = Order.DESCENDING)
                    limit(15)
                }
                val sessions = sessionResponse.decodeList<StudySessionDto>()
                android.util.Log.d("ChildDetailDebug", "Số bài tập tìm thấy: ${sessions.size}")

                _recentSessions.value = sessions.reversed()

            } catch (e: Exception) {
                android.util.Log.e("ChildDetailVM", "Lỗi cực nặng: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}