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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.StudySession
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedPieChartScreen(
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
                title = { Text("Biểu đồ Tròn (Tổng hợp)", fontWeight = FontWeight.Bold) },
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tỉ lệ Chính xác (20 bài)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF37474F),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Khung chứa biểu đồ
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 24.dp)
                ) {
                    CustomDonutChart(sessions = sessions)
                }
            }
        }
    }
}

@Composable
fun CustomDonutChart(sessions: List<StudySession>) {
    // 1. Cộng gộp toàn bộ dữ liệu của 20 bài
    val totalQuestions = sessions.sumOf { it.totalQuestions }
    val totalCorrect = sessions.sumOf { it.correctAnswers }
    val totalIncorrect = totalQuestions - totalCorrect

    // Ngăn chặn chia cho 0 nếu data rỗng hoặc bị lỗi
    if (totalQuestions == 0) return

    val correctRatio = totalCorrect.toFloat() / totalQuestions.toFloat()
    val incorrectRatio = totalIncorrect.toFloat() / totalQuestions.toFloat()

    val correctAngle = correctRatio * 360f
    val incorrectAngle = incorrectRatio * 360f

    val accuracyPercentage = (correctRatio * 100).roundToInt()

    // 2. Hiệu ứng chạy vòng tròn vẽ biểu đồ
    var animationProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(sessions) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        ) { value, _ -> animationProgress = value }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val strokeWidth = 40.dp.toPx() // Độ dày của chiếc nhẫn
                val size = Size(size.width, size.height)

                // Vẽ phần SAI (Màu Cam/Đỏ)
                drawArc(
                    color = Color(0xFFFF7043),
                    startAngle = -90f, // Bắt đầu từ đỉnh trên cùng (12h)
                    sweepAngle = incorrectAngle * animationProgress,
                    useCenter = false, // false để nó rỗng ruột
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = size
                )

                // Vẽ phần ĐÚNG (Màu Xanh lá) đè lên nối tiếp
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = -90f + (incorrectAngle * animationProgress),
                    sweepAngle = correctAngle * animationProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = size
                )
            }

            // Text ở giữa "lõi" chiếc nhẫn
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$accuracyPercentage%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF37474F)
                )
                Text(
                    text = "Chính xác",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Chú thích (Legend)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = Color(0xFF4CAF50), title = "Câu đúng", value = "$totalCorrect")
            LegendItem(color = Color(0xFFFF7043), title = "Câu sai", value = "$totalIncorrect")
        }
    }
}

@Composable
fun LegendItem(color: Color, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color, shape = RoundedCornerShape(4.dp), modifier = Modifier.size(16.dp)) {}
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 14.sp, color = Color.Gray)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        }
    }
}