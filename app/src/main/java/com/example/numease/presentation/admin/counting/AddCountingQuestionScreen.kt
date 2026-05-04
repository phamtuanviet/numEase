package com.example.numease.presentation.admin.counting

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.utils.getEmojiForObject

// Danh sách các khóa vật thể
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
    var expanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm Đếm Số (Level $level)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus() // Ẩn bàn phím và bỏ focus
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Khối thông tin đề bài ---
            OutlinedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Nội dung câu hỏi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.instructionText.value,
                        onValueChange = { viewModel.instructionText.value = it },
                        label = { Text("Câu lệnh (VD: Có bao nhiêu quả táo?)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dropdown chọn vật thể
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
                            label = { Text("Loại vật thể hiển thị") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            AVAILABLE_OBJECT_TYPES.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text("${getEmojiForObject(type)}  $type", style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        viewModel.objectType.value = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Khối thông số kỹ thuật ---
            OutlinedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Đáp án & Lựa chọn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = viewModel.count.value,
                            onValueChange = { viewModel.count.value = it },
                            label = { Text("Số lượng") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = viewModel.correctAnswer.value,
                            onValueChange = { viewModel.correctAnswer.value = it },
                            label = { Text("Đáp án đúng") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = viewModel.optionsText.value,
                        onValueChange = { viewModel.optionsText.value = it },
                        label = { Text("Các lựa chọn (cách nhau bởi dấu phẩy)") },
                        placeholder = { Text("VD: 1, 2, 3") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Đảm bảo đáp án đúng phải nằm trong danh sách lựa chọn.",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Nút hành động ---
            Button(
                onClick = { viewModel.saveQuestion(categoryId, level, onSavedSuccess) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !viewModel.isSaving.value
            ) {
                if (viewModel.isSaving.value) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Lưu câu hỏi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}