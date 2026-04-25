package com.example.numease.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParentGateDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    // Random 2 số từ 6 đến 9 để làm phép nhân
    val a = remember { (6..9).random() }
    val b = remember { (6..9).random() }
    val correctAnswer = a * b

    var userInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dành cho Phụ huynh", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Để về màn hình Quản lý, vui lòng nhập kết quả phép tính sau:")
                Spacer(modifier = Modifier.height(16.dp))
                Text("$a x $b = ?", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = userInput,
                    onValueChange = {
                        userInput = it
                        showError = false
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError,
                    singleLine = true,
                    label = { Text("Nhập kết quả") }
                )
                if (showError) {
                    Text("Kết quả chưa đúng, vui lòng thử lại!", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (userInput.trim() == correctAnswer.toString()) {
                    onSuccess()
                } else {
                    showError = true
                }
            }) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}