package com.example.numease.presentation.student.intro

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SessionIntroScreen(
    categoryId: Int,
    level: Int,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    // 1. Ánh xạ Category ID thành Tên Khu Vực và Mascot
    val (zoneName, mascot, bgColor) = when (categoryId) {
        1 -> Triple("🌲 Rừng Đếm Số", "🦊", Color(0xFFFFF9C4)) // Vàng nhạt
        3 -> Triple("🥕 Trại Kéo Thả", "🐰", Color(0xFFE8F5E9)) // Xanh lá nhạt
        2 -> Triple("🌊 Biển So Sánh", "🐳", Color(0xFFE1F5FE)) // Xanh dương nhạt
        4 -> Triple("➕ Thung Lũng Phép Cộng", "🦁", Color(0xFFFCE4EC)) // Hồng nhạt
        5 -> Triple("➖ Sa Mạc Phép Trừ", "🐫", Color(0xFFFFF3E0)) // Cam nhạt
        else -> Triple("✨ Khám Phá Bí Ẩn", "🐻", Color(0xFFF3E5F5)) // Tím nhạt
    }

    // 2. Hiệu ứng nhấp nháy/phóng to thu nhỏ cho Nút Play (Thu hút sự chú ý của bé)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_button_scale"
    )

    // 3. Hiệu ứng lơ lửng cho Mascot
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // --- Nút Quay Lại (Góc trái trên) ---
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding()
                .background(Color.White.copy(alpha = 0.6f), CircleShape)
                .size(48.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.Gray)
        }

        // --- Nội dung chính giữa ---
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mascot lơ lửng
            Text(
                text = mascot,
                fontSize = 140.sp,
                modifier = Modifier
                    .offset(y = floatY.dp)
                    .padding(bottom = 32.dp)
            )

            // Khung thông tin bài học
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 32.dp)
                ) {
                    Text(
                        text = zoneName,
                        color = Color(0xFFF57C00),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cửa ải số $level", // Hiển thị Level DB
                        color = Color(0xFF37474F),
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bé đã sẵn sàng chưa?",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Nút BẮT ĐẦU to bự, nhấp nháy
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = CircleShape,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale) // Áp dụng hiệu ứng scale ở đây
                    .shadow(12.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Bắt đầu",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

// ==========================================
// PREVIEW (Xem thử trong Android Studio)
// ==========================================
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun SessionIntroScreenPreview() {
    MaterialTheme {
        SessionIntroScreen(
            categoryId = 1, // Thử đổi số này thành 2 hoặc 3 để xem màu sắc thay đổi
            level = 2,
            onBack = {},
            onStart = {}
        )
    }
}