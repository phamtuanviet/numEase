package com.example.numease.presentation.admin.calculation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.numease.presentation.admin.content.getCategoryStyling

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCalculationQuestionScreen(
    exerciseId: String,
    categoryId: Int,
    categoryCode: String,
    level: Int,
    viewModel: EditCalculationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current
    val colorScheme = MaterialTheme.colorScheme

    // Lấy giao diện động (Màu sắc và Dấu phép tính)
    val (_, categoryColor) = getCategoryStyling(categoryCode)
    val operatorSymbol = if (categoryCode == "ADDITION") "+" else "-"
    val formTitle = if (categoryCode == "ADDITION") "Phép Cộng" else "Phép Trừ"

    LaunchedEffect(exerciseId) {
        viewModel.loadQuestionData(exerciseId)
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sửa $formTitle", fontWeight = FontWeight.Bold) },
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
                CircularProgressIndicator(color = categoryColor)
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
                            label = { Text("Câu lệnh (VD: Bé hãy làm phép tính sau)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Khối 2: Thiết lập bài toán ---
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colorScheme.outlineVariant))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Dữ liệu phép tính", style = MaterialTheme.typography.titleSmall, color = colorScheme.primary)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Giao diện nhập số trực quan với dấu ở giữa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(
                                value = viewModel.leftValue.value,
                                onValueChange = { viewModel.leftValue.value = it },
                                label = { Text("Số thứ 1") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            // Vòng tròn chứa dấu + hoặc -
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .size(44.dp)
                                    .background(categoryColor.copy(alpha = 0.12f), CircleShape)
                                    .border(1.dp, categoryColor.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(operatorSymbol, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = categoryColor)
                            }

                            OutlinedTextField(
                                value = viewModel.rightValue.value,
                                onValueChange = { viewModel.rightValue.value = it },
                                label = { Text("Số thứ 2") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = viewModel.correctAnswer.value,
                            onValueChange = { viewModel.correctAnswer.value = it },
                            label = { Text("Đáp án đúng") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Khối 3: Các lựa chọn (3 ô) ---
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colorScheme.outlineVariant))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Các lựa chọn hiển thị", style = MaterialTheme.typography.titleSmall, color = colorScheme.primary)
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
                    onClick = { viewModel.updateQuestion(exerciseId, categoryId, categoryCode,level, onSavedSuccess) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
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