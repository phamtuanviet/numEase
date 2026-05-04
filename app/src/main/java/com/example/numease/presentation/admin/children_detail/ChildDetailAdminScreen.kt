package com.example.numease.presentation.admin.children_detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.dto.StudySessionDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailAdminScreen(
    childId: String,
    viewModel: ChildDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val childProfile by viewModel.childProfile.collectAsState()
    val totalStars by viewModel.totalStars.collectAsState()
    val recentSessions by viewModel.recentSessions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(childId) {
        viewModel.loadChildDetails(childId)
    }

    Scaffold(
        containerColor = colorScheme.background, // Trả về màu nền gốc sạch sẽ
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hồ sơ Học sinh", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else if (childProfile != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()) // Hỗ trợ cuộn nếu nội dung dài
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. HEADER: Thông tin bé
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = childProfile!!.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = childProfile!!.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Độ tuổi: ${childProfile!!.age} tuổi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 2. CARD: Tổng số sao tích lũy
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
                    ) // Ép hiển thị viền rõ ràng
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Tổng số sao tích lũy",
                                style = MaterialTheme.typography.labelLarge,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$totalStars",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFC107) // Giữ màu vàng đặc trưng của sao
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. BIỂU ĐỒ: Hiệu suất 15 bài gần nhất
                Text(
                    text = "Hiệu suất 15 bài gần nhất",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
                    )
                ) {
                    if (recentSessions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bé chưa làm bài tập nào.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        PerformanceLineChart(
                            sessions = recentSessions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PerformanceLineChart(sessions: List<StudySessionDto>, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val accuracies = sessions.map { it.accuracy.toFloat() }
    val maxAccuracy = 100f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val xStep = width / (if (accuracies.size > 1) accuracies.size - 1 else 1).coerceAtLeast(1)

        val path = Path()
        accuracies.forEachIndexed { index, accuracy ->
            val x = index * xStep
            val y = height - ((accuracy / maxAccuracy) * height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Vẽ đường Line với màu Primary của Theme
        drawPath(
            path = path,
            color = colorScheme.primary,
            style = Stroke(width = 4.dp.toPx())
        )

        // Vẽ các nốt chấm trên đường đi
        accuracies.forEachIndexed { index, accuracy ->
            val x = index * xStep
            val y = height - ((accuracy / maxAccuracy) * height)
            drawCircle(
                color = colorScheme.surface, // Màu nền của card để tạo độ nổi
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = colorScheme.primary,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}