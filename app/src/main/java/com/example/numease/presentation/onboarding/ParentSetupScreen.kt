package com.example.numease.presentation.onboarding

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParentSetupScreen(
    viewModel: OnboardingViewModel,
    onSetupComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Biến lưu trữ dữ liệu form
    var childName by remember { mutableStateOf("") }
    var childGender by remember { mutableStateOf("") } // Thêm biến lưu giới tính
    var childAgeInput by remember { mutableStateOf("") }

    // Logic Validate (Bắt lỗi) form trực tiếp
    val isAgeValid = childAgeInput.toIntOrNull()?.let { it in 1..18 } ?: false
    // Form chỉ hợp lệ khi điền đủ Tên, Giới tính và Tuổi hợp lệ
    val isFormValid = childName.isNotBlank() && childGender.isNotBlank() && isAgeValid

    // Lắng nghe trạng thái từ ViewModel
    LaunchedEffect(uiState) {
        when (uiState) {
            is OnboardingState.ProfileCreated -> {
                onSetupComplete()
                viewModel.resetState()
            }
            is OnboardingState.Error -> {
                val errorMessage = (uiState as OnboardingState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Tiêu đề mang tính chuyên nghiệp
            Text(
                text = "Thiết lập Hồ sơ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Vui lòng cung cấp thông tin của bé để hệ thống thiết lập lộ trình học phù hợp nhất.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Form điền thông tin
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // 1. Ô nhập Tên
                    OutlinedTextField(
                        value = childName,
                        onValueChange = { childName = it },
                        label = { Text("Tên hoặc Biệt danh của bé") },
                        leadingIcon = {
                            Icon(Icons.Rounded.ChildCare, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Chọn Giới tính
                    Text(
                        text = "Giới tính",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SelectableGenderCard(
                            modifier = Modifier.weight(1f),
                            text = "👦 Bé Trai",
                            isSelected = childGender == "MALE",
                            onClick = { childGender = "MALE" }
                        )

                        SelectableGenderCard(
                            modifier = Modifier.weight(1f),
                            text = "👧 Bé Gái",
                            isSelected = childGender == "FEMALE",
                            onClick = { childGender = "FEMALE" }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Ô nhập Tuổi
                    OutlinedTextField(
                        value = childAgeInput,
                        onValueChange = { input ->
                            // Chỉ cho phép nhập số
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                childAgeInput = input
                            }
                        },
                        label = { Text("Tuổi của bé") },
                        leadingIcon = {
                            Icon(Icons.Rounded.EditCalendar, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = childAgeInput.isNotEmpty() && !isAgeValid,
                        supportingText = {
                            if (childAgeInput.isNotEmpty() && !isAgeValid) {
                                Text("Vui lòng nhập độ tuổi hợp lệ (1-18)")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Đẩy nút bấm xuống cuối màn hình

            // Nút Xác nhận
            Button(
                onClick = {
                    val age = childAgeInput.toIntOrNull() ?: 0
                    // Cập nhật ViewModel: truyền thêm childGender
                    viewModel.createChildProfile(
                        name = childName.trim(),
                        gender = childGender,
                        age = age
                    )
                },
                enabled = isFormValid, // Nút chỉ sáng lên khi điền đúng định dạng và đã chọn giới tính
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Bắt đầu ngay", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Lớp phủ Loading khi đang lưu dữ liệu lên Supabase
        if (uiState is OnboardingState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// Component phụ trợ để tái sử dụng cho nút chọn Giới tính
@Composable
fun SelectableGenderCard(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Đổi màu nền và viền dựa trên trạng thái được chọn hay chưa
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp), // Chiều cao ngang bằng với OutlinedTextField
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        contentColor = contentColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}