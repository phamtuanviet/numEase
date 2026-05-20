package com.example.numease.presentation.admin.comparing

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ComparingContent
import com.example.numease.data.model.Exercise
import com.example.numease.data.model.Instruction
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class EditComparingViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    // Trạng thái Form
    var instructionText = mutableStateOf("")
    var leftValue = mutableStateOf("")
    var rightValue = mutableStateOf("")
    var correctAnswer = mutableStateOf("") // Sẽ lưu ">", "=", hoặc "<"

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    var isSaving = mutableStateOf(false)

    // MỚI: State xử lý lỗi
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // MỚI: Kiểm tra form có trống không
    fun isFormValid(): Boolean {
        return instructionText.value.isNotBlank() &&
                leftValue.value.isNotBlank() &&
                rightValue.value.isNotBlank() &&
                correctAnswer.value.isNotBlank()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // 1. LOAD DỮ LIỆU CŨ TỪ SUPABASE
    fun loadQuestionData(exerciseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val exercise = postgrest.from("exercises")
                    .select { filter { eq("id", exerciseId) } }
                    .decodeSingle<Exercise>()

                val content = exercise.content as? ComparingContent
                if (content != null) {
                    instructionText.value = content.instruction.text
                    // Ép kiểu về String để hiển thị lên TextField
                    leftValue.value = content.leftValue.toString()
                    rightValue.value = content.rightValue.toString()
                    correctAnswer.value = content.correctAnswer
                }
            } catch (e: Exception) {
                Log.e("EditComparing", "Lỗi tải dữ liệu: ${e.message}")
                _errorMessage.value = "Không thể tải câu hỏi."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. CẬP NHẬT LÊN SUPABASE
    fun updateQuestion(exerciseId: String, categoryId: Int, level: Int, onSuccess: () -> Unit) {
        if (isSaving.value) return

        viewModelScope.launch {
            if (!isFormValid()) {
                _errorMessage.value = "Vui lòng điền đầy đủ các trường bắt buộc."
                return@launch
            }

            val leftInt = leftValue.value.trim().toIntOrNull()
            val rightInt = rightValue.value.trim().toIntOrNull()

            if (leftInt == null || rightInt == null) {
                _errorMessage.value = "Số bên trái và số bên phải phải là chữ số hợp lệ."
                return@launch
            }

            // MỚI: Validation thông minh
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
                val fixedOptions = listOf(">", "<", "=")
                // Tạo JSON động mới
                val updatedContent = ComparingContent(
                    instruction = Instruction(text = instructionText.value.trim()),
                    leftValue = leftInt,
                    rightValue = rightInt,
                    correctAnswer = correctAnswer.value,
                    options = fixedOptions,
                )

                val updatedExercise = Exercise(
                    id = exerciseId,
                    categoryId = categoryId,
                    level = level,
                    content = updatedContent
                )

                postgrest.from("exercises")
                    .update(updatedExercise) {
                        filter { eq("id", exerciseId) }
                    }

                onSuccess()
            } catch (e: Exception) {
                Log.e("EditComparing", "Lỗi cập nhật: ${e.message}")
                _errorMessage.value = "Lỗi khi cập nhật: ${e.localizedMessage}"
            } finally {
                isSaving.value = false
            }
        }
    }

    // 3. XOÁ CÂU HỎI
    fun deleteQuestion(exerciseId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                postgrest.from("exercises")
                    .delete {
                        filter { eq("id", exerciseId) }
                    }
                onSuccess()
            } catch (e: Exception) {
                Log.e("EditComparing", "Lỗi xoá: ${e.message}")
                _errorMessage.value = "Không thể xóa câu hỏi."
            }
        }
    }
}