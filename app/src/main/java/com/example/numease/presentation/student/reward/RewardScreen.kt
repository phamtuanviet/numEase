package com.example.numease.presentation.student.reward

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numease.LocalSoundEnabled
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun RewardScreen(
    earnedStars: Int,
    onBackToMap: () -> Unit
) {
    val context = LocalContext.current
    val isSoundEnabled = LocalSoundEnabled.current
    val colorScheme = MaterialTheme.colorScheme

    // --- CÀI ĐẶT ÂM THANH CHÚC MỪNG ---
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("vi", "VN")
                val message = when (earnedStars) {
                    3 -> "Tuyệt vời quá! Bé đạt ba sao luôn!"
                    2 -> "Giỏi lắm! Bé được hai sao này!"
                    else -> "Bé làm tốt lắm! Cố gắng thêm nhé!"
                }
                if (isSoundEnabled) {
                    tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    // --- HIỆU ỨNG ANIMATION CHO 3 NGÔI SAO ---
    var star1Visible by remember { mutableStateOf(false) }
    var star2Visible by remember { mutableStateOf(false) }
    var star3Visible by remember { mutableStateOf(false) }

    val star1Scale by animateFloatAsState(
        targetValue = if (star1Visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "star1"
    )
    val star2Scale by animateFloatAsState(
        targetValue = if (star2Visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "star2"
    )
    val star3Scale by animateFloatAsState(
        targetValue = if (star3Visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "star3"
    )

    LaunchedEffect(earnedStars) {
        delay(300)
        if (earnedStars >= 1) star1Visible = true
        delay(400)
        if (earnedStars >= 2) star2Visible = true
        delay(400)
        if (earnedStars >= 3) star3Visible = true
    }

    // --- GIAO DIỆN CHÍNH ---
    // Sử dụng tertiaryContainer làm nền cho màn hình phần thưởng (Màu Vàng/Nâu động)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.tertiaryContainer
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Lời chúc mừng
                Text(
                    text = "🎉 CHÚC MỪNG BÉ! 🎉",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onTertiaryContainer, // Chữ tự động tương phản với nền
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 2. Mascot vui vẻ
                Text(
                    text = if (earnedStars == 3) "🏆" else "🦊", // Đổi gấu thành cáo cho đồng bộ app
                    fontSize = 120.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // 3. Khu vực hiển thị Sao
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val inactiveStarColor = colorScheme.onTertiaryContainer.copy(alpha = 0.2f)

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                        Text("⭐", fontSize = 80.sp, color = inactiveStarColor)
                        Text("⭐", fontSize = 80.sp, modifier = Modifier.scale(star1Scale))
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp).offset(y = (-20).dp)) {
                        Text("⭐", fontSize = 80.sp, color = inactiveStarColor)
                        Text("⭐", fontSize = 80.sp, modifier = Modifier.scale(star2Scale))
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                        Text("⭐", fontSize = 80.sp, color = inactiveStarColor)
                        Text("⭐", fontSize = 80.sp, modifier = Modifier.scale(star3Scale))
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                val message = when (earnedStars) {
                    3 -> "Bé trả lời đúng hết luôn!\nTuyệt cú mèo!"
                    2 -> "Bé làm rất tốt!\nCố gắng lên 3 sao nhé!"
                    else -> "Bé đã hoàn thành bài tập!\nMình cùng luyện tập thêm nhé."
                }

                // Khung thông điệp động viên
                Surface(
                    color = colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(64.dp))

                // 4. Các nút điều hướng
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Nút Về Bản đồ (Dùng TonalButton - Nút phụ)
                    FilledTonalButton(
                        onClick = onBackToMap,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colorScheme.secondaryContainer,
                            contentColor = colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .height(64.dp)
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Bản đồ")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bản đồ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    // Nút Chơi tiếp (Dùng Button - Nút chính nổi bật)
                    Button(
                        onClick = onBackToMap,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(64.dp)
                            .weight(1f)
                            .padding(start = 8.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                    ) {
                        Text("Tiếp tục", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.PlayArrow, contentDescription = "Tiếp tục")
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun RewardScreenPreview() {
    MaterialTheme {
        // Bạn có thể đổi số 3 thành 1 hoặc 2 để xem UI thay đổi thế nào
        RewardScreen(earnedStars = 3, onBackToMap = {})
    }
}