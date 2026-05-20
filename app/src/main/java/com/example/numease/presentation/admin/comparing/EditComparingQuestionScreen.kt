package com.example.numease.presentation.admin.comparing

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditComparingQuestionScreen(
    exerciseId: String,
    categoryId: Int,
    level: Int,
    viewModel: EditComparingViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current
    val colorScheme = MaterialTheme.colorScheme
    val comparingColor = Color(0xFFFF9800) // Màu đặc trưng cho So Sánh

    // MỚI: Quản lý thông báo lỗi
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    // Load dữ liệu khi vào màn hình
    LaunchedEffect(exerciseId) {
        viewModel.loadQuestionData(exerciseId)
    }

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // Gắn SnackbarHost
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sửa So Sánh", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    // Nút xoá
                    IconButton(onClick = { viewModel.deleteQuestion(exerciseId, onSavedSuccess) }) {
                        Icon(Icons.Default.DeleteSweep, "Xóa câu hỏi", tint = colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = comparingColor)
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

                // --- Khối 1: Đề bài ---
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colorScheme.outlineVariant))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Hướng dẫn", style = MaterialTheme.typography.titleSmall, color = colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = viewModel.instructionText.value,
                            onValueChange = { viewModel.instructionText.value = it },
                            label = { Text("Câu lệnh (VD: Bé hãy điền dấu thích hợp)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Khối 2: Số liệu Trái - Phải ---
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colorScheme.outlineVariant))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Dữ liệu cần so sánh", style = MaterialTheme.typography.titleSmall, color = colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = viewModel.leftValue.value,
                                onValueChange = { viewModel.leftValue.value = it },
                                label = { Text("Số bên TRÁI") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = viewModel.rightValue.value,
                                onValueChange = { viewModel.rightValue.value = it },
                                label = { Text("Số bên PHẢI") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Khối 3: Chọn Dấu ---
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colorScheme.outlineVariant))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Chọn đáp án đúng", style = MaterialTheme.typography.titleSmall, color = colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))

                        val operators = listOf(">", "=", "<")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            operators.forEach { op ->
                                val isSelected = viewModel.correctAnswer.value == op
                                OutlinedButton(
                                    onClick = {
                                        focusManager.clearFocus() // Ẩn bàn phím khi user bấm chọn đáp án
                                        viewModel.correctAnswer.value = op
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) comparingColor else Color.Transparent,
                                        contentColor = if (isSelected) Color.White else comparingColor
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = SolidColor(if (isSelected) Color.Transparent else comparingColor)
                                    )
                                ) {
                                    Text(op, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // --- Nút Cập Nhật ---
                Button(
                    onClick = {
                        focusManager.clearFocus() // Ẩn bàn phím trước khi lưu
                        viewModel.updateQuestion(exerciseId, categoryId, level, onSavedSuccess)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = comparingColor), // Màu đồng bộ với nút dấu
                    // MỚI: Khóa nút nếu chưa điền đủ hoặc đang lưu
                    enabled = viewModel.isFormValid() && !viewModel.isSaving.value
                ) {
                    if (viewModel.isSaving.value) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Cập nhật câu hỏi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}