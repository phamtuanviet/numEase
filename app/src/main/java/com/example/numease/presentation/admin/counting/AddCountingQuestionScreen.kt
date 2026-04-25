package com.example.numease.presentation.admin.counting

import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack



import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.utils.getEmojiForObject

// Danh sách các khóa (keys) dựa trên hàm getEmojiForObject của bạn
val AVAILABLE_OBJECT_TYPES = listOf(
    "apple", "cat", "dog", "star", "candy", "flower", "ball",
    "car", "banana", "bird", "strawberry", "rabbit", "ice_cream",
    "orange", "bear", "pencil", "book", "hat", "leaf", "butterfly"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCountingQuestionScreen(
    categoryId: Int,
    level: Int,
    viewModel: AddCountingViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    // Trạng thái mở/đóng của Dropdown
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm câu hỏi (Level $level)", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = viewModel.instructionText.value,
                onValueChange = { viewModel.instructionText.value = it },
                label = { Text("Câu lệnh (VD: Có bao nhiêu quả táo?)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // ĐÃ SỬA: DROPDOWN MENU CHO MÃ VẬT THỂ
            // ==========================================
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ô TextField hiển thị mục đang được chọn (Cấm gõ tay)
                val currentType = viewModel.objectType.value
                val currentEmoji = getEmojiForObject(currentType) // Hàm bạn đã định nghĩa

                OutlinedTextField(
                    value = "$currentEmoji  $currentType", // Hiển thị cả Emoji và Text cho trực quan
                    onValueChange = {},
                    readOnly = true, // Khóa không cho Admin gõ phím
                    label = { Text("Chọn vật thể") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                // Danh sách thả xuống
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    AVAILABLE_OBJECT_TYPES.forEach { type ->
                        val emoji = getEmojiForObject(type)
                        DropdownMenuItem(
                            text = { Text("$emoji  $type", fontSize = 16.sp) },
                            onClick = {
                                viewModel.objectType.value = type // Cập nhật vào ViewModel
                                expanded = false // Đóng menu lại
                            }
                        )
                    }
                }
            }
            // ==========================================

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.count.value,
                    onValueChange = { viewModel.count.value = it },
                    label = { Text("Số lượng") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = viewModel.correctAnswer.value,
                    onValueChange = { viewModel.correctAnswer.value = it },
                    label = { Text("Đáp án đúng") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.optionsText.value,
                onValueChange = { viewModel.optionsText.value = it },
                label = { Text("Các lựa chọn (Cách nhau bằng dấu phẩy)") },
                placeholder = { Text("VD: 1, 2, 3") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveQuestion(categoryId, level, onSavedSuccess) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !viewModel.isSaving.value
            ) {
                if (viewModel.isSaving.value) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Lưu câu hỏi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}