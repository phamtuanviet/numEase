package com.example.numease.presentation.admin.counting

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.CountingContent
import com.example.numease.data.model.Exercise
import com.example.numease.data.model.Instruction
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCountingViewModel @Inject constructor(
    private val postgrest: Postgrest // Tiêm trực tiếp Supabase client
) : ViewModel() {

    // Khai báo các trạng thái cho Form
    var instructionText = mutableStateOf("")
    var objectType = mutableStateOf("apple")
    var count = mutableStateOf("")
    var correctAnswer = mutableStateOf("")

    // 3 Ô lựa chọn riêng biệt (UI nhập chuỗi, lát parse sang Int)
    var option1 = mutableStateOf("")
    var option2 = mutableStateOf("")
    var option3 = mutableStateOf("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    var isSaving = mutableStateOf(false)

    // 1. TẢI DỮ LIỆU CŨ TỪ SUPABASE
    fun loadQuestionData(exerciseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Query trực tiếp từ Supabase bằng ID
                val exercise = postgrest.from("exercises")
                    .select { filter { eq("id", exerciseId) } }
                    .decodeSingle<Exercise>()

                val content = exercise.content as? CountingContent
                if (content != null) {
                    instructionText.value = content.instruction.text
                    objectType.value = content.objectType
                    count.value = content.count.toString()
                    correctAnswer.value = content.correctAnswer.toString()

                    // Chia mảng List<Int> thành 3 ô UI (Chuyển sang String để hiển thị ở TextField)
                    option1.value = content.options.getOrNull(0)?.toString() ?: ""
                    option2.value = content.options.getOrNull(1)?.toString() ?: ""
                    option3.value = content.options.getOrNull(2)?.toString() ?: ""
                }
            } catch (e: Exception) {
                Log.e("EditCounting", "Lỗi tải dữ liệu: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. LƯU CẬP NHẬT LÊN SUPABASE
    fun updateQuestion(exerciseId: String, categoryId: Int, level: Int, onSuccess: () -> Unit) {
        if (isSaving.value) return

        viewModelScope.launch {
            isSaving.value = true
            try {
                // 1. Parse 3 ô chữ thành List<Int> (Bỏ qua ô trống hoặc sai định dạng)
                val parsedOptions = listOf(option1.value, option2.value, option3.value)
                    .mapNotNull { it.trim().toIntOrNull() }

                // 2. Tạo JSON động
                val updatedContent = CountingContent(
                    instruction = Instruction(text = instructionText.value),
                    objectType = objectType.value,
                    count = count.value.toIntOrNull() ?: 1,
                    options = parsedOptions, // Gắn List<Int> vào đây
                    correctAnswer = correctAnswer.value.toIntOrNull() ?: 1
                )

                // 3. Đóng gói vào Exercise (Giữ nguyên ID cũ)
                val updatedExercise = Exercise(
                    id = exerciseId,
                    categoryId = categoryId,
                    level = level,
                    content = updatedContent
                )

                // 4. Bắn lệnh Update lên Supabase theo ID
                postgrest.from("exercises")
                    .update(updatedExercise) {
                        filter { eq("id", exerciseId) }
                    }

                onSuccess()
            } catch (e: Exception) {
                Log.e("EditCounting", "Lỗi cập nhật: ${e.message}")
            } finally {
                isSaving.value = false
            }
        }
    }

    // 3. TÍNH NĂNG XOÁ CÂU HỎI
    fun deleteQuestion(exerciseId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // Xoá trực tiếp dòng có ID tương ứng
                postgrest.from("exercises")
                    .delete {
                        filter { eq("id", exerciseId) }
                    }
                onSuccess()
            } catch (e: Exception) {
                Log.e("EditCounting", "Lỗi xoá: ${e.message}")
            }
        }
    }
}