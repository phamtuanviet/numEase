package com.example.numease.presentation.admin.drag_drop

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.DragDropContent
import com.example.numease.data.model.DraggableItem
import com.example.numease.data.model.DropZone
import com.example.numease.data.model.Exercise
import com.example.numease.data.model.Instruction
import com.example.numease.utils.getEmojiForObject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@HiltViewModel
class AddDragDropViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    var instructionText = mutableStateOf("")
    var objectType = mutableStateOf("apple")
    var numbersText = mutableStateOf("")

    var isSaving = mutableStateOf(false)

    // MỚI: State xử lý lỗi
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun isFormValid(): Boolean {
        return instructionText.value.isNotBlank() && numbersText.value.isNotBlank()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun saveQuestion(categoryId: Int, level: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!isFormValid()) {
                _errorMessage.value = "Vui lòng nhập Đề bài và Các số cần kéo."
                return@launch
            }

            // 1. Tách chuỗi số và loại bỏ các giá trị không hợp lệ
            val numbers = numbersText.value.split(",")
                .mapNotNull { it.trim().toIntOrNull() }

            // Validation 1: Phải có ít nhất 2 số
            if (numbers.size < 2) {
                _errorMessage.value = "Vui lòng nhập ít nhất 2 số (cách nhau bởi dấu phẩy)."
                return@launch
            }

            // Validation 2: Các số không được trùng nhau để tránh lỗi sinh ID
            if (numbers.distinct().size != numbers.size) {
                _errorMessage.value = "Các số không được trùng lặp. Vui lòng kiểm tra lại."
                return@launch
            }

            // Validation 3: Giới hạn số lượng kéo thả để không bị tràn UI
            if (numbers.size > 5) {
                _errorMessage.value = "Chỉ nên nhập tối đa 5 số để đảm bảo hiển thị tốt trên màn hình."
                return@launch
            }

            isSaving.value = true
            try {
                val emoji = getEmojiForObject(objectType.value)

                // 2. Tự động sinh danh sách vật thể kéo (Draggables)
                val draggablesList = numbers.map { num ->
                    DraggableItem(id = "drag_$num", label = num.toString(), type = "number")
                }

                // 3. Tự động sinh danh sách Giỏ hứng (DropZones)
                val dropZonesList = numbers.map { num ->
                    val repeatedEmoji = emoji.repeat(num)
                    DropZone(id = "zone_$num", label = repeatedEmoji, type = "basket")
                }

                // 4. Tự động nối chéo Mapping đúng
                val mapping = numbers.associate { num ->
                    "drag_$num" to "zone_$num"
                }

                // 5. Đóng gói vào Data Class
                val newContent = DragDropContent(
                    instruction = Instruction(text = instructionText.value.trim()),
                    draggables = draggablesList,
                    dropZones = dropZonesList,
                    correctMapping = mapping
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
                Log.e("AddDragDrop", "Lỗi: ${e.message}")
                _errorMessage.value = "Lỗi kết nối mạng: ${e.localizedMessage}"
            } finally {
                isSaving.value = false
            }
        }
    }
}