package com.example.numease.presentation.admin.drag_drop

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
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current

    // Màu tím đặc trưng của chuyên đề Kéo Thả
    val dragDropPrimaryColor = Color(0xFF9C27B0)

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm Kéo Thả (Level $level)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus() // Ẩn bàn phím và bỏ focus
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Card nhập nội dung câu hỏi ---
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
                        text = "Thông tin chung",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.instructionText.value,
                        onValueChange = { viewModel.instructionText.value = it },
                        label = { Text("Câu lệnh hiển thị cho trẻ") },
                        placeholder = { Text("VD: Kéo đúng số vào giỏ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dropdown chọn Icon (Sử dụng hệ thống MD3 chuẩn)
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
                            label = { Text("Icon cho Giỏ hứng") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            AVAILABLE_OBJECT_TYPES.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${getEmojiForObject(type)}  $type", style = MaterialTheme.typography.bodyLarge)
                                    },
                                    onClick = {
                                        viewModel.objectType.value = type
                                        expanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Card cấu hình Logic Kéo Thả ---
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
                        text = "Dữ liệu trò chơi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.numbersText.value,
                        onValueChange = { viewModel.numbersText.value = it },
                        label = { Text("Các số cần kéo") },
                        placeholder = { Text("VD: 2, 3, 5") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Text("Nhập các số cách nhau bằng dấu phẩy")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tip hướng dẫn Admin
                    Surface(
                        color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Hệ thống sẽ tự tạo Giỏ hứng tương ứng với số lượng vật thể bạn nhập.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Nút Lưu ---
            Button(
                onClick = { viewModel.saveQuestion(categoryId, level, onSavedSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = dragDropPrimaryColor),
                shape = RoundedCornerShape(16.dp),
                enabled = !viewModel.isSaving.value && viewModel.numbersText.value.isNotBlank()
            ) {
                if (viewModel.isSaving.value) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Lưu câu hỏi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}