package com.example.numease.presentation.admin.caculation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.presentation.admin.content.getCategoryStyling

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCalculationQuestionScreen(
    categoryId: Int,
    categoryCode: String,
    level: Int,
    viewModel: AddCalculationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    val (icon, color) = getCategoryStyling(categoryCode)
    val operatorSymbol = if (categoryCode == "ADDITION") "+" else "-"
    val formTitle = if (categoryCode == "ADDITION") "Phép Cộng" else "Phép Trừ"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm $formTitle (Level $level)", fontWeight = FontWeight.Bold) },
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
                label = { Text("Câu lệnh (VD: Bé hãy làm phép tính sau)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Khu vực nhập số liệu siêu trực quan
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = viewModel.leftValue.value,
                    onValueChange = { viewModel.leftValue.value = it },
                    label = { Text("Số thứ 1") },
                    modifier = Modifier.weight(1f)
                )

                // Hiển thị Dấu ở giữa 2 ô nhập liệu
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .size(48.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(operatorSymbol, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
                }

                OutlinedTextField(
                    value = viewModel.rightValue.value,
                    onValueChange = { viewModel.rightValue.value = it },
                    label = { Text("Số thứ 2") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = viewModel.optionsText.value,
                onValueChange = { viewModel.optionsText.value = it },
                label = { Text("Các lựa chọn (Cách nhau bằng dấu phẩy)") },
                placeholder = { Text("VD: 2, 3, 4") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.correctAnswer.value,
                onValueChange = { viewModel.correctAnswer.value = it },
                label = { Text("Đáp án đúng") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.saveQuestion(categoryId, categoryCode, level, onSavedSuccess) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color),
                enabled = !viewModel.isSaving.value
            ) {
                if (viewModel.isSaving.value) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Lưu câu hỏi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}