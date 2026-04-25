package com.example.numease.presentation.admin.comparing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddComparingQuestionScreen(
    categoryId: Int,
    level: Int,
    viewModel: AddComparingViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm So Sánh (Level $level)", fontWeight = FontWeight.Bold) },
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
                label = { Text("Câu lệnh (VD: Bé hãy điền dấu thích hợp)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Hàng nhập số Trái và Phải
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.leftValue.value,
                    onValueChange = { viewModel.leftValue.value = it },
                    label = { Text("Số bên TRÁI") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = viewModel.rightValue.value,
                    onValueChange = { viewModel.rightValue.value = it },
                    label = { Text("Số bên PHẢI") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text("Chọn đáp án đúng:", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            // Dãy nút chọn Dấu (Trực quan hơn Dropdown nhiều)
            val operators = listOf(">", "=", "<")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                operators.forEach { op ->
                    val isSelected = viewModel.correctAnswer.value == op
                    OutlinedButton(
                        onClick = { viewModel.correctAnswer.value = op },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color(0xFFFF9800) else Color.Transparent,
                            contentColor = if (isSelected) Color.White else Color(0xFFFF9800)
                        )
                    ) {
                        Text(op, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.saveQuestion(categoryId, level, onSavedSuccess) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                enabled = !viewModel.isSaving.value
            ) {
                if (viewModel.isSaving.value) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Lưu câu hỏi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}