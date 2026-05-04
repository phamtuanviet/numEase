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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(childId, categoryId) {
        viewModel.loadChartData(childId, categoryId)
    }

    Scaffold(
        containerColor = colorScheme.background,
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
                Text("Bé chưa có dữ liệu cho kĩ năng này.", color = colorScheme.onSurfaceVariant)
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
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground
                )
                Text(
                    text = "Trục dọc: Số câu trả lời đúng",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Khung chứa biểu đồ sử dụng ElevatedCard của MD3
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
    val colorScheme = MaterialTheme.colorScheme

    // Màu sắc động theo Theme
    val lineColor = colorScheme.primary
    val gridColor = colorScheme.outlineVariant
    val labelTextColor = colorScheme.onSurfaceVariant.toArgb()

    val correctAnswersData = sessions.map { it.correctAnswers.toFloat() }
    val maxQuestions = sessions.maxOfOrNull { it.totalQuestions.toFloat() }?.coerceAtLeast(5f) ?: 5f

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
                .padding(start = 32.dp, bottom = 24.dp)
                .clipToBounds()
        ) {
            val width = size.width
            val height = size.height

            // A. VẼ TRỤC Y VÀ LƯỚI NGANG
            val horizontalLines = 5
            for (i in 0..horizontalLines) {
                val y = height - (i.toFloat() / horizontalLines) * height
                val labelValue = ((i.toFloat() / horizontalLines) * maxQuestions).toInt()

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        labelValue.toString(),
                        -35f,
                        y + 10f,
                        android.graphics.Paint().apply {
                            color = labelTextColor
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.RIGHT
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                    )
                }

                drawLine(
                    color = gridColor,
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
                val smoothCurve = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlX = (p1.x + p2.x) / 2
                        cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                    }
                }

                clipRect(right = width * animationProgress) {
                    // Vẽ Gradient đổ bóng bên dưới
                    val fillPath = Path().apply {
                        addPath(smoothCurve)
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )

                    // Vẽ đường kẻ biểu đồ
                    drawPath(
                        path = smoothCurve,
                        color = lineColor,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Vẽ các điểm nút
                    points.forEach { point ->
                        drawCircle(color = colorScheme.surface, radius = 6.dp.toPx(), center = point)
                        drawCircle(color = lineColor, radius = 4.dp.toPx(), center = point)
                    }
                }
            } else if (points.size == 1) {
                drawCircle(color = lineColor, radius = 6.dp.toPx(), center = points.first())
            }
        }
    }
}