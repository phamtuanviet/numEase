package com.example.numease.presentation.admin.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ExerciseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryStat(
    val id: Int,
    val code: String,
    val name: String,
    val totalQuestions: Int
)

@HiltViewModel
class ManageContentViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _categoryStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val categoryStats = _categoryStats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadContentStats()
    }

    private fun loadContentStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Tải toàn bộ danh mục từ exercise_categories
                val categories = postgrest.from("exercise_categories")
                    .select()
                    .decodeList<ExerciseCategory>()

                // 2. Với mỗi danh mục, đếm số lượng bài tập tương ứng trong bảng exercises
                val statsList = categories.map { category ->
                    val count = postgrest.from("exercises").select(head = true) {
                        filter { eq("category_id", category.id ?: 0) }
                        count(Count.EXACT)
                    }.countOrNull() ?: 0

                    CategoryStat(
                        id = category.id ?: 0,
                        code = category.code,
                        name = category.name,
                        totalQuestions = count.toInt()
                    )
                }

                // Sắp xếp theo ID cho đúng thứ tự 1 -> 5
                _categoryStats.value = statsList.sortedBy { it.id }

            } catch (e: Exception) {
                android.util.Log.e("ManageContentVM", "Lỗi: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}