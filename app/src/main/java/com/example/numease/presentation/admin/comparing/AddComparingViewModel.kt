package com.example.numease.presentation.admin.comparing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ComparingContent
import com.example.numease.data.model.Exercise
import com.example.numease.data.model.Instruction
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject



// ==========================================
// 1. VIEWMODEL CHO FORM SO SÁNH
// ==========================================
@HiltViewModel
class AddComparingViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {
    var instructionText = mutableStateOf("")
    var leftValue = mutableStateOf("")
    var rightValue = mutableStateOf("")
    var correctAnswer = mutableStateOf(">") // Mặc định là Dấu Lớn

    var isSaving = mutableStateOf(false)

    fun saveQuestion(categoryId: Int, level: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isSaving.value = true
            try {
                // Tự động gán cứng options
                val fixedOptions = listOf(">", "<", "=")

                val newContent = ComparingContent(
                    instruction = Instruction(text = instructionText.value),
                    leftValue = leftValue.value.toIntOrNull() ?: 0,
                    rightValue = rightValue.value.toIntOrNull() ?: 0,
                    options = fixedOptions,
                    correctAnswer = correctAnswer.value
                )

                val newExercise = Exercise(
                    id = UUID.randomUUID().toString(),
                    categoryId = categoryId,
                    level = level,
                    content = newContent
                )

                postgrest.from("exercises").insert(newExercise)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("AddComparing", "Lỗi: ${e.message}")
            } finally {
                isSaving.value = false
            }
        }
    }
}

