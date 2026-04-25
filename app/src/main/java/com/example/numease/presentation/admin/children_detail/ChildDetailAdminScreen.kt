package com.example.numease.presentation.admin.children_detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(childId) {
        viewModel.loadChildDetails(childId)
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hồ sơ Học sinh", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF9C27B0)) }
        } else if (childProfile != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                // 1. HEADER: Thông tin bé
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(72.dp).background(Color(0xFF9C27B0).copy(0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(childProfile!!.name.take(1).uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9C27B0))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(childProfile!!.name, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF37474F))
                        Text("Độ tuổi: ${childProfile!!.age} tuổi", fontSize = 16.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 2. CARD: Tổng số sao
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Tổng số sao tích lũy", fontSize = 14.sp, color = Color.Gray)
                            Text("$totalStars", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFC107))
                        }
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(48.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. BIỂU ĐỒ: Hiệu suất 15 bài gần nhất
                Text("Hiệu suất 15 bài gần nhất", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                Spacer(modifier = Modifier.height(16.dp))

                if (recentSessions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.White, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text("Bé chưa làm bài tập nào.", color = Color.Gray)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        PerformanceLineChart(sessions = recentSessions, modifier = Modifier.fillMaxWidth().height(220.dp).padding(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceLineChart(sessions: List<StudySessionDto>, modifier: Modifier = Modifier) {
    val accuracies = sessions.map { it.accuracy.toFloat() } // Giả sử accuracy tính theo hệ 100%
    val maxAccuracy = 100f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Vẽ lưới dọc đứt khúc
        val xStep = width / (if (accuracies.size > 1) accuracies.size - 1 else 1).coerceAtLeast(1)

        // Khởi tạo đường Path của biểu đồ
        val path = Path()

        accuracies.forEachIndexed { index, accuracy ->
            val x = index * xStep
            // Lộn ngược Y vì trục tọa độ Canvas bắt đầu (0,0) ở góc trái trên
            val y = height - ((accuracy / maxAccuracy) * height)

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Vẽ đường Line biểu đồ
        drawPath(
            path = path,
            color = Color(0xFF2196F3), // Màu xanh dương
            style = Stroke(width = 4.dp.toPx())
        )

        // Vẽ các chấm tròn lên mỗi điểm dữ liệu
        accuracies.forEachIndexed { index, accuracy ->
            val x = index * xStep
            val y = height - ((accuracy / maxAccuracy) * height)
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color(0xFF2196F3),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}