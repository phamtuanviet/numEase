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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numease.data.model.CalculationContent

@Composable
fun CalculationUI(
    content: CalculationContent,
    onPlayAudio: (String) -> Unit,
    onAnswerSelected: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isAddition = content.operator == "+"

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Khung đề bài & Nút âm thanh (Phân biệt màu linh hoạt qua Theme)
        // Dùng PrimaryContainer cho phép Cộng, TertiaryContainer cho phép Trừ
        val headerColor = if (isAddition) colorScheme.primaryContainer else colorScheme.tertiaryContainer
        val textColor = if (isAddition) colorScheme.onPrimaryContainer else colorScheme.onTertiaryContainer

        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = headerColor),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 2. KHU VỰC PHÉP TÍNH
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
                fontSize = 44.sp, // Tăng nhẹ size để cân bằng với thẻ số
                fontWeight = FontWeight.Black,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Số bên phải
            MiniNumberCardWithDots(number = content.rightValue)

            // Dấu Bằng
            Text(
                text = "=",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Dấu Hỏi (Khung đáp án)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp) // Kéo to ô trống ra một chút để chứa vừa số có 2 chữ số (vd: 15)
                    .background(colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .border(width = 2.dp, color = colorScheme.outline, shape = RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = "?",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. KHU VỰC CÁC NÚT ĐÁP ÁN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            content.options.forEach { option ->
                val buttonContainerColor = if (isAddition) colorScheme.primary else colorScheme.tertiary
                val buttonContentColor = if (isAddition) colorScheme.onPrimary else colorScheme.onTertiary

                Button(
                    onClick = { onAnswerSelected(option) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonContainerColor,
                        contentColor = buttonContentColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(90.dp)
                        .widthIn(min = 90.dp) // Tự động giãn ngang nếu chữ số dài (như số 10)
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                ) {
                    Text(
                        text = option.toString(),
                        fontSize = 36.sp,
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

// Thẻ số thu nhỏ gọn gàng để vừa màn hình ngang
@Composable
fun MiniNumberCardWithDots(number: Int) {
    val colorScheme = MaterialTheme.colorScheme

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        modifier = Modifier.width(80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = number.toString(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface
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
                                    .size(8.dp)
                                    .background(colorScheme.primary, CircleShape) // Chấm tròn sử dụng màu Primary để luôn nổi bật
                            )
                        }
                    }
                }
            }
        }
    }
}