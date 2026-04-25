package com.example.numease.presentation.admin.question

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import io.github.jan.supabase.postgrest.query.Columns


@HiltViewModel
class ManageQuestionsViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {
    private val _questions = MutableStateFlow<List<Exercise>>(emptyList())
    val questions = _questions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    fun loadQuestions(categoryId: Int, level: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Lấy toàn bộ JSON câu hỏi của Level này
                val results = postgrest.from("exercises").select {
                    filter {
                        eq("category_id", categoryId)
                        eq("level", level)
                    }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }.decodeList<Exercise>()
                Log.d("ManageQuestionsVM", "Có ${results.size} câu hỏi")


                _questions.value = results
            } catch (e: Exception) {
                android.util.Log.e("ManageQuestionsVM", "Lỗi Parse JSON: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}