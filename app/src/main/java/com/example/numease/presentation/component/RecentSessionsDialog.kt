package com.example.numease.presentation.component

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numease.data.model.StudySession
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas

import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect

@Composable
fun RecentSessionsDialog(
    sessions: List<StudySession>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Tuyệt vời!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        title = {
            Text(
                text = "Phong độ của bé 🚀",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF9800),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            if (sessions.isEmpty()) {
                Text(
                    text = "Bé chưa có bài tập nào, cùng bắt đầu chơi nhé!",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "10 bài tập gần nhất",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Vẽ biểu đồ
                    KidFriendlyLineChart(sessions = sessions)
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFFFFDE7) // Màu nền vàng cực nhạt
    )
}

@Composable
fun KidFriendlyLineChart(sessions: List<StudySession>) {
    // 1. Chuẩn bị dữ liệu
    // Đảo ngược mảng để vẽ từ TRÁI (cũ nhất) sang PHẢI (mới nhất)
    val chronologicalSessions = sessions.reversed()

    // Tính toán số sao cho từng phiên
    val starData = chronologicalSessions.map { session ->
        when {
            session.totalQuestions == 0 -> 0
            session.correctAnswers == session.totalQuestions -> 3
            session.correctAnswers >= session.totalQuestions * 0.7 -> 2
            session.correctAnswers > 0 -> 1
            else -> 0
        }
    }

    // 2. Hiệu ứng hoạt hình (Vẽ từ trái sang phải)
    var animationProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        ) { value, _ -> animationProgress = value }
    }

    // 3. Khung vẽ Canvas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cột hiển thị Y-axis (3 sao, 2 sao, 1 sao, 0 sao)
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⭐⭐⭐", fontSize = 10.sp)
            Text("⭐⭐", fontSize = 10.sp)
            Text("⭐", fontSize = 10.sp)
            Text("\uD83C\uDF31", fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Khu vực vẽ biểu đồ chính
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds() // Đảm bảo biểu đồ không tràn ra ngoài
        ) {
            val width = size.width
            val height = size.height
            val maxStars = 3f

            // Tính khoảng cách giữa các điểm trên trục X
            val spacing = if (starData.size > 1) width / (starData.size - 1) else width

            // Tạo danh sách tọa độ (X, Y) cho từng điểm dữ liệu
            val points = starData.mapIndexed { index, stars ->
                val x = index * spacing
                // Y = 0 là ở trên cùng, Y = height là ở dưới đáy
                val y = height - (stars / maxStars) * height
                Offset(x, y)
            }

            // Vẽ các đường kẻ ngang mờ (Lưới)
            for (i in 0..3) {
                val y = height - (i / maxStars) * height
                drawLine(
                    color = Color(0xFFEEEEEE),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            if (points.size > 1) {
                // Tạo đường cong mềm mại (Cubic Bezier Curve)
                val smoothCurve = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlX = (p1.x + p2.x) / 2
                        cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                    }
                }

                // Dùng clipRect để làm hiệu ứng vẽ biểu đồ dần dần ra
                clipRect(right = width * animationProgress) {

                    // 1. Vẽ Vùng màu Gradient dưới đường cong
                    val fillPath = Path().apply {
                        addPath(smoothCurve)
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFD54F).copy(alpha = 0.6f), Color.Transparent)
                        )
                    )

                    // 2. Vẽ Đường cong biểu đồ
                    drawPath(
                        path = smoothCurve,
                        color = Color(0xFFFF9800),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // 3. Vẽ các điểm nút (Chấm tròn màu cam viền trắng)
                    points.forEach { point ->
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = Color(0xFFFF9800),
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    }
                }
            } else if (points.size == 1) {
                // Nếu chỉ mới làm 1 bài thì vẽ 1 chấm ở giữa
                drawCircle(color = Color(0xFFFF9800), radius = 6.dp.toPx(), center = points.first())
            }
        }
    }
}