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
import androidx.compose.ui.draw.clip
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
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(childId, categoryId) {
        viewModel.loadChartData(childId, categoryId)
    }

    Scaffold(
        containerColor = colorScheme.background,
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
                Text("Bé chưa có dữ liệu cho kĩ năng này.", color = colorScheme.onSurfaceVariant)
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
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(24.dp))

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
                    CustomDonutChart(sessions = sessions)
                }
            }
        }
    }
}

@Composable
fun CustomDonutChart(sessions: List<StudySession>) {
    val colorScheme = MaterialTheme.colorScheme

    // Màu sắc động lấy từ Theme
    val correctColor = colorScheme.primary
    val incorrectColor = colorScheme.error
    val trackColor = colorScheme.surfaceVariant

    val totalQuestions = sessions.sumOf { it.totalQuestions }
    val totalCorrect = sessions.sumOf { it.correctAnswers }
    val totalIncorrect = totalQuestions - totalCorrect

    if (totalQuestions == 0) return

    val correctRatio = totalCorrect.toFloat() / totalQuestions.toFloat()
    val incorrectRatio = totalIncorrect.toFloat() / totalQuestions.toFloat()

    val correctAngle = correctRatio * 360f
    val incorrectAngle = incorrectRatio * 360f
    val accuracyPercentage = (correctRatio * 100).roundToInt()

    var animationProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(sessions) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        ) { value, _ -> animationProgress = value }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                val strokeWidth = 36.dp.toPx()
                val arcSize = Size(size.width, size.height)

                // Vẽ vòng nền mờ phía dưới (Track)
                drawCircle(
                    color = trackColor,
                    radius = (size.width / 2),
                    style = Stroke(width = strokeWidth)
                )

                // Vẽ phần SAI (Màu Đỏ/Cam)
                drawArc(
                    color = incorrectColor,
                    startAngle = -90f,
                    sweepAngle = incorrectAngle * animationProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = arcSize
                )

                // Vẽ phần ĐÚNG (Màu Xanh lá) nối tiếp
                drawArc(
                    color = correctColor,
                    startAngle = -90f + (incorrectAngle * animationProgress),
                    sweepAngle = correctAngle * animationProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = arcSize
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$accuracyPercentage%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Chính xác",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Chú thích (Legend) chuẩn MD3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = correctColor, title = "Câu đúng", value = "$totalCorrect")
            LegendItem(color = incorrectColor, title = "Câu sai", value = "$totalIncorrect")
        }
    }
}

@Composable
fun LegendItem(color: Color, title: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color, shape = RoundedCornerShape(4.dp), modifier = Modifier.size(16.dp)) {}
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
        }
    }
}