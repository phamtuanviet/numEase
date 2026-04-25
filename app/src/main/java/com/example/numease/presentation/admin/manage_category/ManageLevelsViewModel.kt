package com.example.numease.presentation.admin.manage_category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import io.github.jan.supabase.postgrest.query.Columns

// Class phụ để hứng dữ liệu siêu nhẹ từ Database
@Serializable
data class ExerciseLevelDto(val id: String, val level: Int)

// Class chứa dữ liệu cho UI hiển thị
data class LevelStat(val level: Int, val questionCount: Int)

@HiltViewModel
class ManageLevelsViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _levelStats = MutableStateFlow<List<LevelStat>>(emptyList())
    val levelStats = _levelStats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // Biến lưu trữ level tiếp theo sẽ được tạo
    private val _nextLevelToCreate = MutableStateFlow(1)
    val nextLevelToCreate = _nextLevelToCreate.asStateFlow()

    fun loadLevelsForCategory(categoryId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // ĐÃ SỬA: Truyền columns vào tham số của select() bằng Columns.list()
                val exercises = postgrest.from("exercises").select(
                    columns = Columns.list("id", "level")
                ) {
                    filter { eq("category_id", categoryId) }
                }.decodeList<ExerciseLevelDto>()

                // Dùng Kotlin để gộp nhóm theo Level
                val grouped = exercises.groupBy { it.level }

                val stats = grouped.map { (level, list) ->
                    LevelStat(level = level, questionCount = list.size)
                }.sortedBy { it.level } // Sắp xếp từ Level 1 trở đi

                _levelStats.value = stats

                // Tính toán Level tiếp theo cho nút "Thêm Level"
                _nextLevelToCreate.value = if (stats.isEmpty()) 1 else stats.last().level + 1

            } catch (e: Exception) {
                android.util.Log.e("ManageLevelsVM", "Lỗi tải levels: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}