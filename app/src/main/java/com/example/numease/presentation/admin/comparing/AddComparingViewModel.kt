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
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class AddComparingViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {
    var instructionText = mutableStateOf("")
    var leftValue = mutableStateOf("")
    var rightValue = mutableStateOf("")
    var correctAnswer = mutableStateOf(">") // Mặc định là Dấu Lớn

    var isSaving = mutableStateOf(false)

    // MỚI: State xử lý lỗi để báo ra UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Kiểm tra xem tất cả các ô đã có chữ hay chưa
    fun isFormValid(): Boolean {
        return instructionText.value.isNotBlank() &&
                leftValue.value.isNotBlank() &&
                rightValue.value.isNotBlank() &&
                correctAnswer.value.isNotBlank()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun saveQuestion(categoryId: Int, level: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!isFormValid()) {
                _errorMessage.value = "Vui lòng điền đầy đủ tất cả các trường."
                return@launch
            }

            val leftInt = leftValue.value.trim().toIntOrNull()
            val rightInt = rightValue.value.trim().toIntOrNull()

            if (leftInt == null || rightInt == null) {
                _errorMessage.value = "Số bên trái và số bên phải phải là chữ số hợp lệ."
                return@launch
            }

            // MỚI: Validation thông minh - Kiểm tra xem đáp án chọn có đúng logic toán học không
            val isMathCorrect = when (correctAnswer.value) {
                ">" -> leftInt > rightInt
                "<" -> leftInt < rightInt
                "=" -> leftInt == rightInt
                else -> false
            }

            if (!isMathCorrect) {
                _errorMessage.value = "Đáp án bạn chọn (${correctAnswer.value}) không đúng với phép toán: $leftInt và $rightInt."
                return@launch
            }

            isSaving.value = true
            try {
                // Gán cứng options
                val fixedOptions = listOf(">", "<", "=")

                val newContent = ComparingContent(
                    instruction = Instruction(text = instructionText.value.trim()),
                    leftValue = leftInt,
                    rightValue = rightInt,
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
                _errorMessage.value = "Lỗi kết nối mạng: ${e.localizedMessage}"
            } finally {
                isSaving.value = false
            }
        }
    }
}