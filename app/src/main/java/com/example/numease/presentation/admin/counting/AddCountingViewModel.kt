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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import io.github.jan.supabase.postgrest.query.Columns

@HiltViewModel
class AddCountingViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    // Khai báo các trạng thái cho Form
    var instructionText = mutableStateOf("")
    var objectType = mutableStateOf("apple")
    var count = mutableStateOf("1")
    var optionsText = mutableStateOf("1,2,3") // Bắt Admin nhập cách nhau bằng dấu phẩy
    var correctAnswer = mutableStateOf("1")

    var isSaving = mutableStateOf(false)

    fun saveQuestion(categoryId: Int, level: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isSaving.value = true
            try {
                // 1. Parse chuỗi optionsText (VD: "1, 2, 3") thành List<Int>
                val parsedOptions = optionsText.value.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }

                // 2. TẠO JSON ĐỘNG thông qua Data Class CountingContent
                val newContent = CountingContent(
                    instruction = Instruction(text = instructionText.value),
                    objectType = objectType.value,
                    count = count.value.toIntOrNull() ?: 1,
                    options = parsedOptions,
                    correctAnswer = correctAnswer.value.toIntOrNull() ?: 1
                )

                // 3. Đóng gói vào Exercise (Tự tạo UUID mới)
                val newExercise = Exercise(
                    id = java.util.UUID.randomUUID().toString(),
                    categoryId = categoryId,
                    level = level,
                    content = newContent
                )

                // 4. Bắn lên Supabase
                postgrest.from("exercises").insert(newExercise)

                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("AddCounting", "Lỗi: ${e.message}")
            } finally {
                isSaving.value = false
            }
        }
    }
}