package com.example.numease.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numease.data.model.CalculationContent

@Composable
fun CalculationUI(
    content: CalculationContent,
    onPlayAudio: (String) -> Unit,
    onAnswerSelected: (Int) -> Unit // Gửi số bé chọn về ViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Khung đề bài & Nút âm thanh (Màu Hồng nhạt cho Phép Cộng/Trừ)
        val headerColor = if (content.operator == "+") Color(0xFFFCE4EC) else Color(0xFFFFF3E0)
        val textColor = if (content.operator == "+") Color(0xFFC2185B) else Color(0xFFE65100)

        Surface(
            color = headerColor,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.clickable { onPlayAudio(content.instruction.text) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.VolumeUp, "Nghe lại", tint = textColor)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = content.instruction.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 2. KHU VỰC PHÉP TÍNH (Nằm ngang: Số + Số = ?)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Số bên trái
            MiniNumberCardWithDots(number = content.leftValue)

            // Dấu (+ hoặc -)
            Text(
                text = content.operator,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF37474F),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Số bên phải
            MiniNumberCardWithDots(number = content.rightValue)

            // Dấu Bằng
            Text(
                text = "=",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF37474F),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Dấu Hỏi (Khung đáp án)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)) // Nền xám rất nhạt
                    // ĐÃ SỬA: Bỏ shadow bị lỗi đi, dùng border (đường viền) để tạo ô trống
                    .border(width = 2.dp, color = Color(0xFFE0E0E0), shape = RoundedCornerShape(16.dp))
            ) {
                Text("?", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.Gray)
            }

        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. KHU VỰC CÁC NÚT ĐÁP ÁN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            content.options.forEach { option ->
                Button(
                    onClick = { onAnswerSelected(option) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (content.operator == "+") Color(0xFFEC407A) else Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                ) {
                    Text(
                        text = option.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

// Thẻ số thu nhỏ gọn gàng để vừa màn hình ngang
@Composable
fun MiniNumberCardWithDots(number: Int) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 6.dp,
        modifier = Modifier.width(80.dp) // Nhỏ hơn thẻ cũ (120dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = number.toString(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF37474F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lưới chấm tròn siêu mini
            val rows = (number + 4) / 5
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (r in 0 until rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        val dotsInThisRow = if (r == rows - 1 && number % 5 != 0) number % 5 else 5
                        for (c in 0 until dotsInThisRow) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp) // Chấm nhỏ hơn
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}