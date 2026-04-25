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
    onBackToMap: () -> Unit // Cả 2 nút hiện tại ta đều cho về Map để bé thấy cửa mới mở
) {
    val context = LocalContext.current

    val isSoundEnabled = LocalSoundEnabled.current // Gọi thẳng biến toàn cục

    // --- CÀI ĐẶT ÂM THANH CHÚC MỪNG ---
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("vi", "VN")
                // Tự động đọc ngay khi vào màn hình
                val message = when (earnedStars) {
                    3 -> "Tuyệt vời quá! Bé đạt ba sao luôn!"
                    2 -> "Giỏi lắm! Bé được hai sao này!"
                    else -> "Bé làm tốt lắm! Cố gắng thêm nhé!"
                }
                if (isSoundEnabled) { // Chỉ phát nếu bé bật âm thanh
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
    // Tạo 3 state scale để làm hiệu ứng sao nảy lên lần lượt
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

    // Bật lần lượt từng ngôi sao dựa trên số sao đạt được (có delay)
    LaunchedEffect(earnedStars) {
        delay(300)
        if (earnedStars >= 1) star1Visible = true
        delay(400)
        if (earnedStars >= 2) star2Visible = true
        delay(400)
        if (earnedStars >= 3) star3Visible = true
    }

    // --- GIAO DIỆN CHÍNH ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9C4)) // Nền vàng nhạt lễ hội
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
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF57F17),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 2. Mascot vui vẻ
            Text(
                text = if (earnedStars == 3) "🏆" else "🐻",
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
                // Sao 1
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                    Text("⭐", fontSize = 80.sp, color = Color.Gray.copy(alpha = 0.3f)) // Khung sao chìm
                    Text("⭐", fontSize = 80.sp, modifier = Modifier.scale(star1Scale))   // Sao nổi lên
                }
                // Sao 2 (Để cao hơn 1 chút tạo hình vòm)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp).offset(y = (-20).dp)) {
                    Text("⭐", fontSize = 80.sp, color = Color.Gray.copy(alpha = 0.3f))
                    Text("⭐", fontSize = 80.sp, modifier = Modifier.scale(star2Scale))
                }
                // Sao 3
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                    Text("⭐", fontSize = 80.sp, color = Color.Gray.copy(alpha = 0.3f))
                    Text("⭐", fontSize = 80.sp, modifier = Modifier.scale(star3Scale))
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            val message = when (earnedStars) {
                3 -> "Bé trả lời đúng hết luôn!\nTuyệt cú mèo!"
                2 -> "Bé làm rất tốt!\nCố gắng lên 3 sao nhé!"
                else -> "Bé đã hoàn thành bài tập!\nMình cùng luyện tập thêm nhé."
            }

            Text(
                text = message,
                fontSize = 20.sp,
                color = Color(0xFF5D4037),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // 4. Các nút điều hướng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Nút Về Bản đồ
                Button(
                    onClick = onBackToMap,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)), // Xanh nhạt
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(64.dp)
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Bản đồ", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bản đồ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // Nút Chơi tiếp (Hiện tại vẫn dẫn về Bản đồ)
                Button(
                    onClick = onBackToMap, // Có thể đổi luồng nếu sau này bạn muốn nhảy thẳng sang bài mới
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Xanh lá
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(64.dp)
                        .weight(1f)
                        .padding(start = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                ) {
                    Text("Tiếp tục", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.PlayArrow, contentDescription = "Tiếp tục", tint = Color.White)
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