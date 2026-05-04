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
import androidx.compose.ui.graphics.toArgb
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
    val labelTextColor = colorScheme.onSurfaceVariant.toArgb()

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
                .padding(start = 32.dp, bottom = 24.dp) // Tăng nhẹ padding cho nhãn trục Y
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
                    color = gridLineColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            // B. TÍNH TOÁN VÀ VẼ CÁC CỘT
            val maxBars = sessions.size.coerceAtLeast(1)
            val spacing = width / maxBars
            val barWidth = spacing * 0.6f // Tăng độ rộng cột một chút để bé dễ nhìn
            val cornerRadius = CornerRadius(12f, 12f)

            sessions.forEachIndexed { index, session ->
                val totalHeight = (session.totalQuestions.toFloat() / maxQuestions) * height
                val correctHeight = (session.correctAnswers.toFloat() / maxQuestions) * height * animationProgress

                val x = (index * spacing) + (spacing - barWidth) / 2
                val yTotal = height - totalHeight
                val yCorrect = height - correctHeight

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
            }
        }
    }
}