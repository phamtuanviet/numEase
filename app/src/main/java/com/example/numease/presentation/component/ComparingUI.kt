package com.example.numease.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numease.data.model.ComparingContent
import kotlinx.coroutines.delay

@Composable
fun ComparingUI(
    content: ComparingContent,
    onPlayAudio: (String) -> Unit,
    onAnswerSelected: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // MỚI: State quản lý UX trực tiếp trên UI
    var wrongAnswers by remember { mutableStateOf(setOf<String>()) }
    var isCorrectlyAnswered by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Khung đề bài & Nút âm thanh
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, // QUAN TRỌNG: Đẩy 2 thẻ ra sát 2 lề
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thẻ số bên TRÁI
            NumberCardWithDots(
                number = content.leftValue,
                modifier = Modifier.width(105.dp) // Cố định bề ngang mỏng lại một chút
            )

            // Vùng an toàn ở giữa (Dùng weight để chiếm trọn phần đất còn lại)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val circleScale by animateFloatAsState(if (isCorrectlyAnswered) 1.25f else 1f, tween(400), label = "circleScale")
                val circleColor by animateColorAsState(
                    targetValue = if (isCorrectlyAnswered) Color(0xFF4CAF50) else colorScheme.tertiaryContainer,
                    animationSpec = tween(300),
                    label = "circleColor"
                )
                val circleTextColor = if (isCorrectlyAnswered) Color.White else colorScheme.onTertiaryContainer

                ElevatedCard(
                    shape = CircleShape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = circleColor
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .size(64.dp) // Giảm size gốc để có dư địa khi scale lên
                        .scale(circleScale)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isCorrectlyAnswered) content.correctAnswer else "?",
                            fontSize = 36.sp, // Giảm nhẹ font size dấu hỏi cho cân đối
                            fontWeight = FontWeight.Black,
                            color = circleTextColor
                        )
                    }
                }
            }

            // Thẻ số bên PHẢI
            NumberCardWithDots(
                number = content.rightValue,
                modifier = Modifier.width(105.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. KHU VỰC CÁC NÚT ĐÁP ÁN (>, <, =)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            content.options.forEach { option ->

                // Logic xác định trạng thái của nút
                val isCorrectAnswer = option == content.correctAnswer
                val isWronglyPressed = wrongAnswers.contains(option)
                val showSuccessEffect = isCorrectlyAnswered && isCorrectAnswer

                // Đổi màu nút linh hoạt
                val buttonColor by animateColorAsState(
                    targetValue = when {
                        showSuccessEffect -> Color(0xFF4CAF50) // Xanh lá cây (Đúng)
                        isWronglyPressed -> colorScheme.surfaceVariant // Xám (Sai)
                        else -> colorScheme.secondary // Màu mặc định
                    },
                    animationSpec = tween(300)
                )

                val contentColor = if (showSuccessEffect) Color.White else if (isWronglyPressed) colorScheme.outline else colorScheme.onSecondary

                // Hiệu ứng phóng to nút khi đúng
                val buttonScale by animateFloatAsState(
                    targetValue = if (showSuccessEffect) 1.15f else 1f,
                    animationSpec = tween(500)
                )

                Button(
                    onClick = {
                        if (isCorrectlyAnswered || isWronglyPressed) return@Button // Block spam click

                        if (isCorrectAnswer) {
                            // 1. TRƯỜNG HỢP ĐÚNG
                            isCorrectlyAnswered = true
                            onPlayAudio("Đúng rồi, bé thật thông minh!")
                        } else {
                            // 2. TRƯỜNG HỢP SAI
                            wrongAnswers = wrongAnswers + option
                            onPlayAudio("Chưa chính xác, thử lại nhé.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = contentColor,
                        disabledContainerColor = buttonColor,
                        disabledContentColor = contentColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    enabled = !isCorrectlyAnswered && !isWronglyPressed, // Khóa nút nếu làm đúng hoặc nút đã bấm sai
                    modifier = Modifier
                        .height(90.dp)
                        .widthIn(min = 90.dp)
                        .scale(buttonScale)
                        .shadow(if (isWronglyPressed) 0.dp else 6.dp, RoundedCornerShape(24.dp))
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

    // --- XỬ LÝ CHUYỂN CÂU KHI BÉ LÀM ĐÚNG ---
    LaunchedEffect(isCorrectlyAnswered) {
        if (isCorrectlyAnswered) {
            delay(1500) // Đợi 1.5s để bé nhìn dấu bay vào giữa và nghe âm thanh
            onAnswerSelected(content.correctAnswer)
            isCorrectlyAnswered = false
            wrongAnswers = emptySet()
        }
    }
}
// --- UX ĐẶC BIỆT CHO TRẺ DYSCALCULIA ---
// --- UX ĐẶC BIỆT CHO TRẺ DYSCALCULIA ---
@Composable
fun NumberCardWithDots(
    number: Int,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp) // Giảm padding ngang để nhường chỗ cho lưới chấm tròn
        ) {
            // Số (Đã giảm size xuống 48)
            Text(
                text = number.toString(),
                fontSize = 48.sp,
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
                                    .background(colorScheme.tertiary, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}