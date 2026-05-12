package com.example.numease.presentation.admin.dragdrop

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.utils.getEmojiForObject
import com.example.numease.presentation.admin.counting.AVAILABLE_OBJECT_TYPES


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDragDropQuestionScreen(
    exerciseId: String,
    categoryId: Int,
    level: Int,
    viewModel: EditDragDropViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current
    val colorScheme = MaterialTheme.colorScheme
    val dragDropThemeColor = Color(0xFF4CAF50) // Màu xanh lá

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(exerciseId) {
        viewModel.loadQuestionData(exerciseId)
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sửa Kéo Thả Số", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { viewModel.deleteQuestion(exerciseId, onSavedSuccess) }) {
                        Icon(Icons.Default.DeleteSweep, "Xóa câu hỏi", tint = colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = dragDropThemeColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- Khối 1: Đề bài & Chọn Vật thể ---
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colorScheme.outlineVariant))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Thông tin cơ bản", style = MaterialTheme.typography.titleSmall, color = colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = viewModel.instructionText.value,
                            onValueChange = { viewModel.instructionText.value = it },
                            label = { Text("Câu lệnh (VD: Kéo số đúng vào giỏ)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dropdown chọn vật thể
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = "${getEmojiForObject(viewModel.objectType.value)}  ${viewModel.objectType.value}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Loại vật thể (Emoji)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                AVAILABLE_OBJECT_TYPES.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text("${getEmojiForObject(type)}  $type") },
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

                // --- Khối 2: Các con số (Tự động sinh giỏ) ---
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colorScheme.outlineVariant))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Các cặp Số - Giỏ", style = MaterialTheme.typography.titleSmall, color = colorScheme.primary)
                        Text(
                            "Nhập 3 con số. Hệ thống sẽ tự tạo 3 cục số để kéo và 3 giỏ chứa Emoji tương ứng.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = viewModel.option1.value,
                                onValueChange = { viewModel.option1.value = it },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Số 1") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                            )
                            OutlinedTextField(
                                value = viewModel.option2.value,
                                onValueChange = { viewModel.option2.value = it },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Số 2") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                            )
                            OutlinedTextField(
                                value = viewModel.option3.value,
                                onValueChange = { viewModel.option3.value = it },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Số 3") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // --- Nút Cập Nhật ---
                Button(
                    onClick = { viewModel.updateQuestion(exerciseId, categoryId, level, onSavedSuccess) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = dragDropThemeColor),
                    enabled = !viewModel.isSaving.value
                ) {
                    if (viewModel.isSaving.value) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Cập nhật câu hỏi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}