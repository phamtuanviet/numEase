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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.StudySession
import com.example.numease.presentation.parent.chart.DetailedChartViewModel


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
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(childId, categoryId) {
        viewModel.loadChartData(childId, categoryId)
    }

    Scaffold(
        containerColor = colorScheme.background,
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
                    text = "So sánh Số câu đúng / Tổng câu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground
                )

                // Chú thích biểu đồ động theo Theme
                Row(
                    modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chú thích Cột đúng
                    Surface(color = colorScheme.primary, shape = RoundedCornerShape(4.dp), modifier = Modifier.size(16.dp)) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Câu đúng", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.width(24.dp))

                    // Chú thích Cột tổng
                    Surface(color = colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp), modifier = Modifier.size(16.dp)) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tổng số câu", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                }

                // Khung chứa biểu đồ (Sử dụng ElevatedCard cho MD3)
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
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
    val colorScheme = MaterialTheme.colorScheme

    // Màu sắc động
    val barCorrectColor = colorScheme.primary
    val barTotalColor = colorScheme.surfaceVariant
    val gridLineColor = colorScheme.outlineVariant

    // Cài đặt TextMeasurer chuẩn Compose
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelMedium.copy(
        color = colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold
    )

    val maxQuestions = sessions.maxOfOrNull { it.totalQuestions.toFloat() }?.coerceAtLeast(5f) ?: 5f

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
                .clipToBounds()
        ) {
            // 1. TẠO PADDING CHO CẢ 4 PHÍA (BẢO VỆ CHỮ VÀ CỘT)
            val topPadding = 32.dp.toPx()   // Lề trên cho số to nhất
            val rightPadding = 32.dp.toPx() // Lề phải cho cột cuối cùng
            val yAxisPadding = 80.dp.toPx() // Lề trái cho chữ số trục Y
            val xAxisPadding = 60.dp.toPx() // Lề dưới cho số lần chơi trục X

            // Không gian thực tế để vẽ các cột
            val drawableWidth = size.width - yAxisPadding - rightPadding
            val drawableHeight = size.height - xAxisPadding - topPadding

            // A. VẼ TRỤC Y VÀ LƯỚI NGANG
            val horizontalLines = 5
            for (i in 0..horizontalLines) {
                // Tọa độ Y dịch xuống một khoảng topPadding
                val y = topPadding + drawableHeight - (i.toFloat() / horizontalLines) * drawableHeight
                val labelValue = ((i.toFloat() / horizontalLines) * maxQuestions).toInt()

                // Vẽ chữ trục Y
                val textLayoutResult = textMeasurer.measure(text = labelValue.toString(), style = textStyle)
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = yAxisPadding - textLayoutResult.size.width - 24f, // Dịch sang trái lưới 24px
                        y = y - textLayoutResult.size.height / 2f
                    )
                )

                // Vẽ lưới đứt nét ngang (trừ mốc tọa độ 0)
                if (i > 0) {
                    drawLine(
                        color = gridLineColor,
                        start = Offset(yAxisPadding, y),
                        end = Offset(size.width - rightPadding + 16f, y),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
            }

            // Vẽ đường trục X đậm (Mốc 0)
            val xAxisY = topPadding + drawableHeight
            drawLine(
                color = gridLineColor,
                start = Offset(yAxisPadding, xAxisY),
                end = Offset(size.width - rightPadding + 16f, xAxisY),
                strokeWidth = 4f
            )

            // B. TÍNH TOÁN VÀ VẼ CÁC CỘT
            val maxBars = sessions.size.coerceAtLeast(1)
            val spacing = drawableWidth / maxBars
            val barWidth = spacing * 0.6f
            val cornerRadius = CornerRadius(12f, 12f)

            sessions.forEachIndexed { index, session ->
                val totalHeight = (session.totalQuestions.toFloat() / maxQuestions) * drawableHeight
                val correctHeight = (session.correctAnswers.toFloat() / maxQuestions) * drawableHeight * animationProgress

                // Tọa độ X phải cộng thêm yAxisPadding
                val x = yAxisPadding + (index * spacing) + (spacing - barWidth) / 2

                // Tọa độ Y cũng phải cộng thêm topPadding
                val yTotal = topPadding + drawableHeight - totalHeight
                val yCorrect = topPadding + drawableHeight - correctHeight

                // 1. Vẽ cột NỀN (Tổng số câu)
                if (session.totalQuestions > 0) {
                    drawRoundRect(
                        color = barTotalColor,
                        topLeft = Offset(x, yTotal),
                        size = Size(barWidth, totalHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // 2. Vẽ cột CHÍNH (Số câu đúng)
                if (session.correctAnswers > 0) {
                    drawRoundRect(
                        color = barCorrectColor,
                        topLeft = Offset(x, yCorrect),
                        size = Size(barWidth, correctHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // 3. Vẽ nhãn trục X (Lần chơi thứ 1, 2, 3...)
                val xLabel = "${index + 1}"
                val xTextLayout = textMeasurer.measure(text = xLabel, style = textStyle)
                drawText(
                    textLayoutResult = xTextLayout,
                    topLeft = Offset(
                        x = x + barWidth / 2f - xTextLayout.size.width / 2f, // Căn giữa ngay dưới cột
                        y = xAxisY + 24f // Dịch xuống dưới trục ngang 24px
                    )
                )
            }
        }
    }
}