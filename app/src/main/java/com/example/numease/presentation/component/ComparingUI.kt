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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numease.data.model.ComparingContent

@Composable
fun ComparingUI(
    content: ComparingContent,
    onPlayAudio: (String) -> Unit,
    onAnswerSelected: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Khung đề bài & Nút âm thanh (Dùng Secondary Theme cho Biển So Sánh)
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.clickable { onPlayAudio(content.instruction.text) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Nghe lại",
                    tint = colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = content.instruction.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSecondaryContainer,
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

            // Vòng tròn Dấu hỏi ở giữa (Dùng Tertiary Container để nổi bật)
            ElevatedCard(
                shape = CircleShape,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colorScheme.tertiaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                modifier = Modifier.size(70.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "?",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onTertiaryContainer
                    )
                }
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondary, // Đồng bộ màu nút với vùng đề bài
                        contentColor = colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(90.dp)
                        .widthIn(min = 90.dp)
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                ) {
                    Text(
                        text = option,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

// --- UX ĐẶC BIỆT CHO TRẺ DYSCALCULIA ---
@Composable
fun NumberCardWithDots(number: Int) {
    val colorScheme = MaterialTheme.colorScheme

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
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
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lưới chấm tròn
            val rows = (number + 4) / 5
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
                                    // Dùng màu Tertiary (Vàng/Cam) cho các hạt để thu hút ánh nhìn
                                    .background(colorScheme.tertiary, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}