package com.example.numease.presentation.admin.calculation

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.CalculationContent
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
class EditCalculationViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    // Trạng thái Form
    var instructionText = mutableStateOf("")
    var leftValue = mutableStateOf("")
    var rightValue = mutableStateOf("")
    var correctAnswer = mutableStateOf("")

    // 3 Ô lựa chọn riêng biệt
    var option1 = mutableStateOf("")
    var option2 = mutableStateOf("")
    var option3 = mutableStateOf("")

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
                correctAnswer.value.isNotBlank() &&
                (option1.value.isNotBlank() || option2.value.isNotBlank() || option3.value.isNotBlank())
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // 1. TẢI DỮ LIỆU TỪ SUPABASE
    fun loadQuestionData(exerciseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val exercise = postgrest.from("exercises")
                    .select { filter { eq("id", exerciseId) } }
                    .decodeSingle<Exercise>()

                val content = exercise.content as? CalculationContent
                if (content != null) {
                    instructionText.value = content.instruction.text
                    leftValue.value = content.leftValue.toString()
                    rightValue.value = content.rightValue.toString()
                    correctAnswer.value = content.correctAnswer.toString()

                    // Gắn vào 3 ô option UI
                    option1.value = content.options.getOrNull(0)?.toString() ?: ""
                    option2.value = content.options.getOrNull(1)?.toString() ?: ""
                    option3.value = content.options.getOrNull(2)?.toString() ?: ""
                }
            } catch (e: Exception) {
                Log.e("EditCalculation", "Lỗi tải dữ liệu: ${e.message}")
                _errorMessage.value = "Không thể tải câu hỏi."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. CẬP NHẬT LÊN SUPABASE
    fun updateQuestion(exerciseId: String, categoryId: Int, categoryCode: String, level: Int, onSuccess: () -> Unit) {
        if (isSaving.value) return

        viewModelScope.launch {
            if (!isFormValid()) {
                _errorMessage.value = "Vui lòng điền đầy đủ các trường bắt buộc."
                return@launch
            }

            // Ép kiểu & Validation
            val leftInt = leftValue.value.trim().toIntOrNull()
            val rightInt = rightValue.value.trim().toIntOrNull()
            val correctInt = correctAnswer.value.trim().toIntOrNull()

            if (leftInt == null || rightInt == null || correctInt == null) {
                _errorMessage.value = "Dữ liệu tính toán phải là chữ số hợp lệ."
                return@launch
            }

            val parsedOptions = listOf(option1.value, option2.value, option3.value)
                .mapNotNull { it.trim().toIntOrNull() }

            if (parsedOptions.size < 2) {
                _errorMessage.value = "Vui lòng nhập ít nhất 2 lựa chọn đáp án."
                return@launch
            }

            if (!parsedOptions.contains(correctInt)) {
                _errorMessage.value = "Đáp án đúng ($correctInt) phải nằm trong các lựa chọn (${parsedOptions.joinToString(", ")})."
                return@launch
            }

            isSaving.value = true
            try {
                val operator = if (categoryCode == "ADDITION") "+" else "-"

                val updatedContent = CalculationContent(
                    instruction = Instruction(text = instructionText.value.trim()),
                    leftValue = leftInt,
                    rightValue = rightInt,
                    correctAnswer = correctInt,
                    options = parsedOptions,
                    operator = operator,
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
                Log.e("EditCalculation", "Lỗi cập nhật: ${e.message}")
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
                    .delete { filter { eq("id", exerciseId) } }
                onSuccess()
            } catch (e: Exception) {
                Log.e("EditCalculation", "Lỗi xoá: ${e.message}")
                _errorMessage.value = "Không thể xóa câu hỏi."
            }
        }
    }
}