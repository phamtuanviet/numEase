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
    val colorScheme = MaterialTheme.colorScheme

    // 1. Ánh xạ Category ID thành Tên, Mascot và Cặp màu động (Nền to - Chữ title)
    // Việc dùng các hệ màu Container giúp màn hình có màu sắc riêng biệt nhưng vẫn tự động đổi khi bật Dark Mode
    val zoneData = when (categoryId) {
        1 -> Triple("🌲 Rừng Đếm Số", "🦊", colorScheme.primaryContainer to colorScheme.onPrimaryContainer)
        3 -> Triple("🥕 Trại Kéo Thả", "🐰", colorScheme.secondaryContainer to colorScheme.onSecondaryContainer)
        2 -> Triple("🌊 Biển So Sánh", "🐳", colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer)
        4 -> Triple("➕ Thung Lũng Phép Cộng", "🦁", colorScheme.errorContainer to colorScheme.onErrorContainer)
        5 -> Triple("➖ Sa Mạc Phép Trừ", "🐫", colorScheme.surfaceVariant to colorScheme.onSurfaceVariant)
        else -> Triple("✨ Khám Phá Bí Ẩn", "🐻", colorScheme.surface to colorScheme.onSurface)
    }

    val zoneName = zoneData.first
    val mascot = zoneData.second
    val bgColor = zoneData.third.first
    val titleColor = zoneData.third.second

    // 2. Hiệu ứng nhấp nháy/phóng to thu nhỏ cho Nút Play
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f, // Phóng to nhỉnh hơn một chút để tạo độ nảy tốt hơn
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_button_scale"
    )

    // 3. Hiệu ứng lơ lửng cho Mascot
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor) // Nền thay đổi theo khu vực và chế độ Sáng/Tối
    ) {
        // --- Nút Quay Lại (Góc trái trên) ---
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding()
                .background(colorScheme.surface.copy(alpha = 0.7f), CircleShape) // Làm mờ nhẹ nền nút
                .size(56.dp) // Tăng kích thước touch target cho trẻ dễ bấm
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = colorScheme.onSurface
            )
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

            // Khung thông tin bài học (Sử dụng ElevatedCard chuẩn MD3)
            ElevatedCard(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 32.dp)
                ) {
                    Text(
                        text = zoneName,
                        color = titleColor, // Màu chữ đồng bộ với màu nền khu vực
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Cửa ải số $level",
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineLarge, // Dùng typography thay vì fix fontSize
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bé đã sẵn sàng chưa?",
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Nút BẮT ĐẦU to bự, nhấp nháy
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary, // Lấy màu Primary từ Theme
                    contentColor = colorScheme.onPrimary
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .shadow(12.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Bắt đầu",
                    modifier = Modifier.size(64.dp) // Đưa size vào Modifier của Icon để nó hiển thị sắc nét nhất
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