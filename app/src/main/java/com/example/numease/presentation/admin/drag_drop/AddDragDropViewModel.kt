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

@HiltViewModel
class AddDragDropViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    var instructionText = mutableStateOf("")
    var objectType = mutableStateOf("apple") // Dùng chung list với bài Đếm số
    var numbersText = mutableStateOf("")     // Nhập: 2, 3, 4

    var isSaving = mutableStateOf(false)

    fun saveQuestion(categoryId: Int, level: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isSaving.value = true
            try {
                // 1. Tách chuỗi số Admin nhập (VD: "2, 3" -> List(2, 3))
                val numbers = numbersText.value.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }

                val emoji = getEmojiForObject(objectType.value)

                // 2. Tự động sinh danh sách vật thể kéo (Draggables)
                val draggablesList = numbers.map { num ->
                    DraggableItem(id = "drag_$num", label = num.toString(), type = "number")
                }

                // 3. Tự động sinh danh sách Giỏ hứng (DropZones) và nhân bản Emoji
                val dropZonesList = numbers.map { num ->
                    val repeatedEmoji = emoji.repeat(num) // VD: num = 3 -> "🍎🍎🍎"
                    DropZone(id = "zone_$num", label = repeatedEmoji, type = "basket")
                }

                // 4. Tự động nối chéo Mapping đúng
                val mapping = numbers.associate { num ->
                    "drag_$num" to "zone_$num"
                }

                // 5. Đóng gói vào Data Class
                val newContent = DragDropContent(
                    instruction = Instruction(text = instructionText.value),
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
                android.util.Log.e("AddDragDrop", "Lỗi: ${e.message}")
            } finally {
                isSaving.value = false
            }
        }
    }
}