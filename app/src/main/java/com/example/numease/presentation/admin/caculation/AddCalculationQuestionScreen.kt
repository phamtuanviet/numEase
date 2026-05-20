package com.example.numease.presentation.admin.caculation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.numease.presentation.admin.content.getCategoryStyling
import androidx.compose.ui.text.input.KeyboardType

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
    val colorScheme = MaterialTheme.colorScheme
    val (icon, categoryColor) = getCategoryStyling(categoryCode) // Hàm tiện ích của bạn
    val operatorSymbol = if (categoryCode == "ADDITION") "+" else "-"
    val formTitle = if (categoryCode == "ADDITION") "Phép Cộng" else "Phép Trừ"
    val focusManager = LocalFocusManager.current

    // MỚI: Khởi tạo SnackbarHostState và lắng nghe lỗi từ ViewModel
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError() // Xóa lỗi sau khi đã hiển thị
        }
    }

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // MỚI: Thêm SnackbarHost
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm $formTitle (Level $level)", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus() // Ẩn bàn phím và bỏ focus
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Khối 1: Đề bài và Câu lệnh ---
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
                        text = "Hướng dẫn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = viewModel.instructionText.value,
                        onValueChange = { viewModel.instructionText.value = it },
                        label = { Text("Câu lệnh (VD: Bé hãy làm phép tính sau)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Khối 2: Số liệu Phép tính ---
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
                        text = "Thiết lập bài toán",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))

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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Ép nhập số
                            singleLine = true
                        )

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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Ép nhập số
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = viewModel.optionsText.value,
                        onValueChange = { viewModel.optionsText.value = it },
                        label = { Text("Các lựa chọn (cách nhau bởi dấu phẩy)") },
                        placeholder = { Text("VD: 2, 3, 4") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // Hỗ trợ nhập số (có phẩy)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.correctAnswer.value,
                        onValueChange = { viewModel.correctAnswer.value = it },
                        label = { Text("Đáp án đúng") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Ép nhập số
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Nút Hành động ---
            Button(
                onClick = {
                    focusManager.clearFocus() // Thu gọn bàn phím khi bấm lưu
                    viewModel.saveQuestion(categoryId, categoryCode, level, onSavedSuccess)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                shape = RoundedCornerShape(16.dp),
                // MỚI: Khóa nút nếu form chưa điền đầy đủ hoặc đang trong quá trình lưu
                enabled = viewModel.isFormValid() && !viewModel.isSaving.value
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