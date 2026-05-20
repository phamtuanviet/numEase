package com.example.numease.presentation.admin.dragdrop

import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class EditDragDropViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    // Trạng thái Form
    var instructionText = mutableStateOf("")
    var objectType = mutableStateOf("apple")

    // Thay numbersText (chuỗi có dấu phẩy) bằng 3 ô lựa chọn
    var option1 = mutableStateOf("")
    var option2 = mutableStateOf("")
    var option3 = mutableStateOf("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    var isSaving = mutableStateOf(false)

    // MỚI: State xử lý lỗi
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Kiểm tra form cơ bản
    fun isFormValid(): Boolean {
        return instructionText.value.isNotBlank() &&
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

                val content = exercise.content as? DragDropContent
                if (content != null) {
                    instructionText.value = content.instruction.text

                    val labels = content.draggables.map { it.label }
                    option1.value = labels.getOrNull(0) ?: ""
                    option2.value = labels.getOrNull(1) ?: ""
                    option3.value = labels.getOrNull(2) ?: ""
                }
            } catch (e: Exception) {
                Log.e("EditDragDrop", "Lỗi tải dữ liệu: ${e.message}")
                _errorMessage.value = "Không thể tải dữ liệu câu hỏi."
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
                _errorMessage.value = "Vui lòng nhập Đề bài và ít nhất 2 Số lượng."
                return@launch
            }

            // 1. Lấy danh sách số từ 3 ô (Tự động bỏ qua ô trống)
            val numbers = listOf(option1.value, option2.value, option3.value)
                .mapNotNull { it.trim().toIntOrNull() }

            // Validation 1: Phải có ít nhất 2 số
            if (numbers.size < 2) {
                _errorMessage.value = "Vui lòng nhập ít nhất 2 số ở phần Cặp Số - Giỏ."
                return@launch
            }

            // Validation 2: Các số không được trùng lặp
            if (numbers.distinct().size != numbers.size) {
                _errorMessage.value = "Các số không được trùng lặp. Vui lòng kiểm tra lại 3 ô lựa chọn."
                return@launch
            }

            isSaving.value = true
            try {
                val emoji = getEmojiForObject(objectType.value)

                // 2. Sinh lại danh sách Kéo (Draggables)
                val draggablesList = numbers.map { num ->
                    DraggableItem(id = "drag_$num", label = num.toString(), type = "number")
                }

                // 3. Sinh lại danh sách Hứng (DropZones)
                val dropZonesList = numbers.map { num ->
                    val repeatedEmoji = emoji.repeat(num)
                    DropZone(id = "zone_$num", label = repeatedEmoji, type = "basket")
                }

                // 4. Nối chéo (Mapping)
                val mapping = numbers.associate { num ->
                    "drag_$num" to "zone_$num"
                }

                // 5. Đóng gói Content mới
                val updatedContent = DragDropContent(
                    instruction = Instruction(text = instructionText.value.trim()),
                    draggables = draggablesList,
                    dropZones = dropZonesList,
                    correctMapping = mapping
                )

                // 6. Cập nhật Exercise
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
                Log.e("EditDragDrop", "Lỗi cập nhật: ${e.message}")
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
                Log.e("EditDragDrop", "Lỗi xoá: ${e.message}")
                _errorMessage.value = "Không thể xóa câu hỏi."
            }
        }
    }
}