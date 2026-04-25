package com.example.numease.presentation.component

import androidx.compose.foundation.background
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
import com.example.numease.data.model.ComparingContent

@Composable
fun ComparingUI(
    content: ComparingContent,
    onPlayAudio: (String) -> Unit,
    onAnswerSelected: (String) -> Unit // Gửi chuỗi ">", "<" hoặc "=" về cho ViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Khung đề bài & Nút âm thanh (Kế thừa style của các màn trước)
        Surface(
            color = Color(0xFFE1F5FE), // Màu xanh dương nhạt của Biển So Sánh
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.clickable { onPlayAudio(content.instruction.text) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.VolumeUp, "Nghe lại", tint = Color(0xFF0277BD))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = content.instruction.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0277BD),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 2. KHU VỰC HIỂN THỊ 2 SỐ VÀ DẤU HỎI Ở GIỮA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thẻ số bên TRÁI
            NumberCardWithDots(number = content.leftValue)

            // Vòng tròn Dấu hỏi ở giữa
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0xFFFFF9C4), CircleShape)
                    .shadow(4.dp, CircleShape)
            ) {
                Text("?", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color(0xFFF57F17))
            }

            // Thẻ số bên PHẢI
            NumberCardWithDots(number = content.rightValue)
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. KHU VỰC CÁC NÚT ĐÁP ÁN (>, <, =)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            content.options.forEach { option ->
                Button(
                    onClick = { onAnswerSelected(option) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                ) {
                    Text(
                        text = option,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

// --- UX ĐẶC BIỆT CHO TRẺ DYSCALCULIA ---
// Thẻ hiển thị số kết hợp với lưới các chấm tròn (Dot Grid)
@Composable
fun NumberCardWithDots(number: Int) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // Số siêu to
            Text(
                text = number.toString(),
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF37474F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lưới chấm tròn (Tối đa 5 chấm 1 hàng để bé dễ nhận diện)
            val rows = (number + 4) / 5 // Tính số hàng cần thiết
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (r in 0 until rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val dotsInThisRow = if (r == rows - 1 && number % 5 != 0) number % 5 else 5
                        for (c in 0 until dotsInThisRow) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFFFFCA28), CircleShape) // Chấm vàng nổi bật
                            )
                        }
                    }
                }
            }
        }
    }
}