package com.example.numease.presentation.admin.counting

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.CountingContent
import com.example.numease.data.model.Exercise
import com.example.numease.data.model.Instruction
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID


@HiltViewModel
class AddCountingViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    // Khai báo các trạng thái cho Form (Xóa giá trị mặc định để Admin phải tự nhập)
    var instructionText = mutableStateOf("")
    var objectType = mutableStateOf("apple")
    var count = mutableStateOf("")
    var optionsText = mutableStateOf("")
    var correctAnswer = mutableStateOf("")

    var isSaving = mutableStateOf(false)

    // MỚI: State xử lý lỗi
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // MỚI: Kiểm tra các trường đã được điền chưa
    fun isFormValid(): Boolean {
        return instructionText.value.isNotBlank() &&
                count.value.isNotBlank() &&
                optionsText.value.isNotBlank() &&
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

            // Ép kiểu & Kiểm tra dữ liệu
            val countInt = count.value.trim().toIntOrNull()
            val correctInt = correctAnswer.value.trim().toIntOrNull()

            if (countInt == null || correctInt == null) {
                _errorMessage.value = "Số lượng và Đáp án đúng phải là chữ số hợp lệ."
                return@launch
            }

            // Đối với bài đếm, số lượng vật thể vẽ ra màn hình CHÍNH LÀ đáp án đúng
            if (countInt != correctInt) {
                _errorMessage.value = "Số lượng vật thể ($countInt) phải bằng với Đáp án đúng ($correctInt)."
                return@launch
            }

            val parsedOptions = optionsText.value.split(",")
                .mapNotNull { it.trim().toIntOrNull() }

            if (parsedOptions.size < 2) {
                _errorMessage.value = "Vui lòng nhập ít nhất 2 lựa chọn (cách nhau bởi dấu phẩy)."
                return@launch
            }

            if (!parsedOptions.contains(correctInt)) {
                _errorMessage.value = "Đáp án đúng ($correctInt) phải nằm trong danh sách các lựa chọn (${parsedOptions.joinToString(", ")})."
                return@launch
            }

            isSaving.value = true
            try {
                // TẠO JSON ĐỘNG
                val newContent = CountingContent(
                    instruction = Instruction(text = instructionText.value.trim()),
                    objectType = objectType.value,
                    count = countInt,
                    options = parsedOptions,
                    correctAnswer = correctInt
                )

                // Đóng gói vào Exercise
                val newExercise = Exercise(
                    id = UUID.randomUUID().toString(),
                    categoryId = categoryId,
                    level = level,
                    content = newContent
                )

                // Bắn lên Supabase
                postgrest.from("exercises").insert(newExercise)

                onSuccess()
            } catch (e: Exception) {
                Log.e("AddCounting", "Lỗi: ${e.message}")
                _errorMessage.value = "Lỗi kết nối mạng: ${e.localizedMessage}"
            } finally {
                isSaving.value = false
            }
        }
    }
}