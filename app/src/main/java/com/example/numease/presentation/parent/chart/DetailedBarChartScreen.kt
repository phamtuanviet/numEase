package com.example.numease.presentation.parent.chart

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.StudySession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedBarChartScreen(
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
                title = { Text("Biểu đồ Cột (20 bài)", fontWeight = FontWeight.Bold) },
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
                    text = "So sánh Số câu đúng / Tổng câu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF37474F)
                )

                // Chú thích biểu đồ
                Row(
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chú thích Cột đúng
                    Surface(color = Color(0xFF2196F3), shape = RoundedCornerShape(4.dp), modifier = Modifier.size(16.dp)) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Câu đúng", fontSize = 14.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.width(16.dp))

                    // Chú thích Cột tổng
                    Surface(color = Color(0xFFE0E0E0), shape = RoundedCornerShape(4.dp), modifier = Modifier.size(16.dp)) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tổng số câu", fontSize = 14.sp, color = Color.Gray)
                }

                // Khung chứa biểu đồ
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 24.dp)
                ) {
                    CustomBarChart20Sessions(sessions = sessions)
                }
            }
        }
    }
}

@Composable
fun CustomBarChart20Sessions(sessions: List<StudySession>) {
    // 1. Tìm mốc Y cao nhất (Tổng số câu hỏi lớn nhất mà bé từng làm)
    val maxQuestions = sessions.maxOfOrNull { it.totalQuestions.toFloat() }?.coerceAtLeast(5f) ?: 5f

    // 2. Hiệu ứng mọc cột từ dưới lên
    var animationProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(sessions) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
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
                .padding(start = 24.dp, bottom = 24.dp) // Chừa không gian cho trục tọa độ
                .clipToBounds()
        ) {
            val width = size.width
            val height = size.height

            // A. VẼ TRỤC Y VÀ LƯỚI NGANG (Giống hệt biểu đồ đường)
            val horizontalLines = 5
            for (i in 0..horizontalLines) {
                val y = height - (i.toFloat() / horizontalLines) * height
                val labelValue = ((i.toFloat() / horizontalLines) * maxQuestions).toInt()

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        labelValue.toString(),
                        -30f,
                        y + 10f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                }

                drawLine(
                    color = Color(0xFFEEEEEE),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            // B. TÍNH TOÁN VÀ VẼ CÁC CỘT
            val maxBars = sessions.size.coerceAtLeast(1) // Ít nhất là 1 để tránh lỗi chia 0
            val spacing = width / maxBars
            val barWidth = spacing * 0.5f // Độ rộng cột bằng 50% khoảng cách
            val cornerRadius = CornerRadius(12f, 12f) // Bo tròn đỉnh cột

            sessions.forEachIndexed { index, session ->
                // Tính toán chiều cao
                val totalHeight = (session.totalQuestions.toFloat() / maxQuestions) * height
                // Nhân với animationProgress để cột 'câu đúng' mọc lên từ từ
                val correctHeight = (session.correctAnswers.toFloat() / maxQuestions) * height * animationProgress

                val x = (index * spacing) + (spacing - barWidth) / 2

                val yTotal = height - totalHeight
                val yCorrect = height - correctHeight

                // 1. Vẽ cột NỀN (Màu xám - biểu thị Tổng số câu)
                if (session.totalQuestions > 0) {
                    drawRoundRect(
                        color = Color(0xFFF5F5F5), // Xám cực nhạt
                        topLeft = Offset(x, yTotal),
                        size = Size(barWidth, totalHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // 2. Vẽ cột CHÍNH (Màu xanh dương - biểu thị Số câu đúng)
                if (session.correctAnswers > 0) {
                    drawRoundRect(
                        color = Color(0xFF2196F3), // Xanh dương
                        topLeft = Offset(x, yCorrect),
                        size = Size(barWidth, correctHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }
}