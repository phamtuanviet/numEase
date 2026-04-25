package com.example.numease.presentation.admin.caculation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.CalculationContent
import com.example.numease.data.model.Exercise
import com.example.numease.data.model.Instruction
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
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

    fun saveQuestion(categoryId: Int, categoryCode: String, level: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isSaving.value = true
            try {
                // 1. Tự động xác định Dấu dựa vào Category Code
                val operator = if (categoryCode == "ADDITION") "+" else "-"

                // 2. Parse danh sách đáp án
                val parsedOptions = optionsText.value.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }

                // 3. Đóng gói JSON
                val newContent = CalculationContent(
                    instruction = Instruction(text = instructionText.value),
                    leftValue = leftValue.value.toIntOrNull() ?: 0,
                    rightValue = rightValue.value.toIntOrNull() ?: 0,
                    operator = operator, // Dấu đã được tự động gán
                    options = parsedOptions,
                    correctAnswer = correctAnswer.value.toIntOrNull() ?: 0
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
                android.util.Log.e("AddCalculation", "Lỗi: ${e.message}")
            } finally {
                isSaving.value = false
            }
        }
    }
}