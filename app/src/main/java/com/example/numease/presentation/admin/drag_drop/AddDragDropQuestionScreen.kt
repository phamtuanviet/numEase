package com.example.numease.presentation.admin.drag_drop

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
import com.example.numease.presentation.admin.counting.AVAILABLE_OBJECT_TYPES
import com.example.numease.utils.getEmojiForObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDragDropQuestionScreen(
    categoryId: Int,
    level: Int,
    viewModel: AddDragDropViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm Kéo Thả (Level $level)", fontWeight = FontWeight.Bold) },
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
                label = { Text("Câu lệnh (VD: Kéo đúng số vào giỏ)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown chọn Icon y hệt môn Đếm số
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentType = viewModel.objectType.value
                val currentEmoji = getEmojiForObject(currentType)

                OutlinedTextField(
                    value = "$currentEmoji  $currentType",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Chọn Icon cho Giỏ hứng") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    AVAILABLE_OBJECT_TYPES.forEach { type ->
                        DropdownMenuItem(
                            text = { Text("${getEmojiForObject(type)}  $type", fontSize = 16.sp) },
                            onClick = {
                                viewModel.objectType.value = type
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.numbersText.value,
                onValueChange = { viewModel.numbersText.value = it },
                label = { Text("Các số cần kéo (cách nhau bằng dấu phẩy)") },
                placeholder = { Text("VD: 2, 3") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hệ thống sẽ tự động tạo Giỏ hứng tương ứng. VD nhập '2, 3' sẽ tự tạo 1 giỏ có 2 icon và 1 giỏ có 3 icon.",
                fontSize = 12.sp, color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.saveQuestion(categoryId, level, onSavedSuccess) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)), // Màu tím của Kéo Thả
                enabled = !viewModel.isSaving.value
            ) {
                if (viewModel.isSaving.value) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Lưu câu hỏi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}