package com.example.numease.presentation.parent.chart

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.StudySession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedLineChartScreen(
    childId: String,
    categoryId: Int,
    viewModel: DetailedChartViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(childId, categoryId) {
        viewModel.loadChartData(childId, categoryId)
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Biểu đồ Đường (20 bài)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Quay lại") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (sessions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bé chưa có dữ liệu cho kĩ năng này.", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Sự tiến bộ theo thời gian",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF37474F)
                )
                Text(
                    text = "Trục dọc: Số câu trả lời đúng",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Khung chứa biểu đồ
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Chiếm phần lớn màn hình
                        .padding(bottom = 24.dp)
                ) {
                    CustomLineChart20Sessions(sessions = sessions)
                }
            }
        }
    }
}

@Composable
fun CustomLineChart20Sessions(sessions: List<StudySession>) {
    // 1. Chuẩn bị dữ liệu: Số câu đúng của 20 bài
    val correctAnswersData = sessions.map { it.correctAnswers.toFloat() }

    // Tìm mốc Y cao nhất (ví dụ: Bé làm bài 10 câu thì mốc Y cao nhất là 10)
    // Nếu max là 0 thì mặc định Y cao nhất là 5 để biểu đồ không bị lỗi chia cho 0
    val maxQuestions = sessions.maxOfOrNull { it.totalQuestions.toFloat() }?.coerceAtLeast(5f) ?: 5f

    // 2. Hiệu ứng chạy biểu đồ từ trái sang phải
    var animationProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(sessions) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        ) { value, _ -> animationProgress = value }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, bottom = 24.dp) // Chừa chỗ vẽ số trên trục X, Y
                .clipToBounds()
        ) {
            val width = size.width
            val height = size.height

            // A. VẼ TRỤC Y VÀ LƯỚI NGANG
            val horizontalLines = 5 // Vẽ 5 đường vạch ngang
            for (i in 0..horizontalLines) {
                val y = height - (i.toFloat() / horizontalLines) * height

                // Vẽ số trục Y
                val labelValue = ((i.toFloat() / horizontalLines) * maxQuestions).toInt()
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        labelValue.toString(),
                        -30f, // Thụt ra ngoài lề trái
                        y + 10f, // Căn giữa nét chữ
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                }

                // Vẽ đường đứt nét
                drawLine(
                    color = Color(0xFFEEEEEE),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            // B. TÍNH TỌA ĐỘ CÁC ĐIỂM
            val spacing = if (correctAnswersData.size > 1) width / (correctAnswersData.size - 1) else width
            val points = correctAnswersData.mapIndexed { index, correctNum ->
                val x = index * spacing
                val y = height - (correctNum / maxQuestions) * height
                Offset(x, y)
            }

            if (points.size > 1) {
                // Tạo đường cong Cubic Bezier
                val smoothCurve = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlX = (p1.x + p2.x) / 2
                        cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                    }
                }

                // Cắt theo tiến độ Animation
                clipRect(right = width * animationProgress) {

                    // Vẽ Gradient bên dưới biểu đồ
                    val fillPath = Path().apply {
                        addPath(smoothCurve)
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4CAF50).copy(alpha = 0.3f), Color.Transparent)
                        )
                    )

                    // Vẽ nét biểu đồ màu Xanh lá
                    drawPath(
                        path = smoothCurve,
                        color = Color(0xFF4CAF50),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Vẽ các nốt chấm trên đường đi
                    points.forEach { point ->
                        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = point)
                        drawCircle(color = Color(0xFF4CAF50), radius = 4.dp.toPx(), center = point)
                    }
                }
            } else if (points.size == 1) {
                drawCircle(color = Color(0xFF4CAF50), radius = 6.dp.toPx(), center = points.first())
            }
        }
    }
}