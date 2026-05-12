package com.example.numease.presentation.parent.chart

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.StudySession
import com.example.numease.presentation.parent.chart.DetailedChartViewModel


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
                Text("Bé chưa có dữ liệu cho kỹ năng này.", color = colorScheme.onSurfaceVariant)
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
                    text = "Trục dọc: Số câu trả lời đúng | Trục ngang: Lần chơi",
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
                    // Gọi hàm vẽ biểu đồ mới
                    CustomLineChart20Sessions(sessions = sessions)
                }
            }
        }
    }
}

@Composable
fun CustomLineChart20Sessions(sessions: List<StudySession>) {
    val colorScheme = MaterialTheme.colorScheme

    val lineColor = colorScheme.primary
    val gridColor = colorScheme.outlineVariant

    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelMedium.copy(
        color = colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold
    )

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
                .clipToBounds()
        ) {
            // 1. TẠO PADDING CHO CẢ 4 PHÍA (BẢO VỆ CHỮ KHÔNG BỊ CẮT)
            val topPadding = 32.dp.toPx()   // Chừa lề trên để ghi số lớn nhất
            val rightPadding = 32.dp.toPx() // Chừa lề phải để ghi lần chơi cuối cùng
            val yAxisPadding = 80.dp.toPx() // Chừa lề trái
            val xAxisPadding = 60.dp.toPx() // Chừa lề dưới

            // Không gian thực tế để vẽ đường cong đồ thị
            val drawableWidth = size.width - yAxisPadding - rightPadding
            val drawableHeight = size.height - xAxisPadding - topPadding

            // A. VẼ TRỤC Y VÀ LƯỚI NGANG
            val horizontalLines = 5
            for (i in 0..horizontalLines) {
                // Tọa độ Y bây giờ phải cộng thêm topPadding
                val y = topPadding + drawableHeight - (i.toFloat() / horizontalLines) * drawableHeight
                val labelValue = ((i.toFloat() / horizontalLines) * maxQuestions).toInt()

                val textLayoutResult = textMeasurer.measure(text = labelValue.toString(), style = textStyle)
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = yAxisPadding - textLayoutResult.size.width - 24f,
                        y = y - textLayoutResult.size.height / 2f
                    )
                )

                if (i > 0) {
                    drawLine(
                        color = gridColor,
                        start = Offset(yAxisPadding, y),
                        end = Offset(size.width - rightPadding + 16f, y), // Dài ra thêm một chút cho đẹp
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
            }

            // Vẽ đường trục X đậm (Mốc 0)
            val xAxisY = topPadding + drawableHeight
            drawLine(
                color = gridColor,
                start = Offset(yAxisPadding, xAxisY),
                end = Offset(size.width - rightPadding + 16f, xAxisY),
                strokeWidth = 4f
            )

            // B. TÍNH TỌA ĐỘ CÁC ĐIỂM TRÊN ĐỒ THỊ
            val spacing = if (correctAnswersData.size > 1) drawableWidth / (correctAnswersData.size - 1) else drawableWidth
            val points = correctAnswersData.mapIndexed { index, correctNum ->
                val x = yAxisPadding + (index * spacing)
                val y = topPadding + drawableHeight - (correctNum / maxQuestions) * drawableHeight

                val xLabel = "${index + 1}"
                val xTextLayout = textMeasurer.measure(text = xLabel, style = textStyle)
                drawText(
                    textLayoutResult = xTextLayout,
                    topLeft = Offset(
                        x = x - xTextLayout.size.width / 2f,
                        y = xAxisY + 24f
                    )
                )

                Offset(x, y)
            }

            // C. VẼ ĐƯỜNG CONG, HIỆU ỨNG VÀ ĐIỂM NHẤN
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

                // Chạy hiệu ứng clip, cộng thêm rightPadding để không cắt hiệu ứng ở điểm cuối
                clipRect(right = yAxisPadding + drawableWidth * animationProgress + rightPadding) {

                    val fillPath = Path().apply {
                        addPath(smoothCurve)
                        lineTo(points.last().x, xAxisY)
                        lineTo(points.first().x, xAxisY)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                            startY = topPadding,
                            endY = xAxisY // Đổ bóng kết thúc chính xác tại trục X
                        )
                    )

                    drawPath(
                        path = smoothCurve,
                        color = lineColor,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

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