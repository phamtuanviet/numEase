package com.example.numease.presentation.admin.caculation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.CalculationContent
import com.example.numease.data.model.Exercise
import com.example.numease.data.model.Instruction
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddCalculationViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    var instructionText = mutableStateOf("")
    var leftValue = mutableStateOf("")
    var rightValue = mutableStateOf("")
    var optionsText = mutableStateOf("")
    var correctAnswer = mutableStateOf("")

    var isSaving = mutableStateOf(false)

    // MỚI: State xử lý lỗi để báo ra UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // MỚI: Kiểm tra xem tất cả các ô đã có chữ hay chưa để bật/tắt nút Lưu
    fun isFormValid(): Boolean {
        return instructionText.value.isNotBlank() &&
                leftValue.value.isNotBlank() &&
                rightValue.value.isNotBlank() &&
                optionsText.value.isNotBlank() &&
                correctAnswer.value.isNotBlank()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun saveQuestion(categoryId: Int, categoryCode: String, level: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!isFormValid()) {
                _errorMessage.value = "Vui lòng điền đầy đủ tất cả các trường."
                return@launch
            }

            // 1. Ép kiểu và kiểm tra dữ liệu đầu vào
            val leftInt = leftValue.value.trim().toIntOrNull()
            val rightInt = rightValue.value.trim().toIntOrNull()
            val correctInt = correctAnswer.value.trim().toIntOrNull()

            if (leftInt == null || rightInt == null || correctInt == null) {
                _errorMessage.value = "Số thứ 1, Số thứ 2 và Đáp án đúng phải là chữ số hợp lệ."
                return@launch
            }

            val parsedOptions = optionsText.value.split(",")
                .mapNotNull { it.trim().toIntOrNull() }

            if (parsedOptions.isEmpty() || parsedOptions.size < 2) {
                _errorMessage.value = "Vui lòng nhập ít nhất 2 đáp án (cách nhau bởi dấu phẩy)."
                return@launch
            }

            if (!parsedOptions.contains(correctInt)) {
                _errorMessage.value = "Đáp án đúng ($correctInt) không nằm trong danh sách các lựa chọn (${parsedOptions.joinToString(", ")})."
                return@launch
            }

            // 2. Nếu mọi thứ OK -> Lưu vào DB
            isSaving.value = true
            try {
                val operator = if (categoryCode == "ADDITION") "+" else "-"

                val newContent = CalculationContent(
                    instruction = Instruction(text = instructionText.value.trim()),
                    leftValue = leftInt,
                    rightValue = rightInt,
                    operator = operator,
                    options = parsedOptions,
                    correctAnswer = correctInt
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