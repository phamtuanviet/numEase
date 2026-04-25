package com.example.numease.presentation.parent.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.StudySession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedTextStatsScreen(
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
                title = { Text("Lịch sử chi tiết", fontWeight = FontWeight.Bold) },
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
                Text("Bé chưa có lịch sử học cho kĩ năng này.", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header mô tả
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        text = "Danh sách 20 bài gần nhất",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF37474F)
                    )
                    Text(
                        text = "Sắp xếp từ mới nhất đến cũ nhất",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Danh sách (LazyColumn)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(sessions) { index, session ->
                        TextHistoryCard(session = session, index = index)
                    }
                }
            }
        }
    }
}

@Composable
fun TextHistoryCard(session: StudySession, index: Int) {
    // 1. Tính toán sao (Giống logic màn Map)
    val stars = when {
        session.totalQuestions == 0 -> 0
        session.correctAnswers == session.totalQuestions -> 3
        session.correctAnswers >= session.totalQuestions * 0.7 -> 2
        session.correctAnswers > 0 -> 1
        else -> 0
    }

    // 2. Chuyển đổi màu sắc tùy theo kết quả
    val statusColor = when (stars) {
        3 -> Color(0xFF4CAF50) // Xanh lá (Hoàn hảo)
        2 -> Color(0xFF2196F3) // Xanh dương (Tốt)
        1 -> Color(0xFFFF9800) // Cam (Cần cố gắng)
        else -> Color(0xFFF44336) // Đỏ (Làm lại)
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Số thứ tự ở góc trái
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF5F5F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${index + 1}",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Nội dung chính
            Column(modifier = Modifier.weight(1f)) {
                // Thời gian làm bài
                Text(
                    text = formatDateTime(session.createdAt),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Kết quả đúng/sai
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đúng: ${session.correctAnswers} / ${session.totalQuestions}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                }
            }

            // Hiển thị dải Sao
            Row {
                repeat(3) { i ->
                    Text(
                        text = "⭐",
                        fontSize = 14.sp,
                        modifier = Modifier.alpha(if (i < stars) 1f else 0.2f)
                    )
                }
            }
        }
    }
}

// Hàm hỗ trợ format thời gian từ Database (ISO 8601) ra chuỗi dễ đọc
fun formatDateTime(isoString: String?): String {
    if (isoString == null) return "Không rõ thời gian"
    try {
        // Cắt bỏ phần mili giây và múi giờ nếu có (VD: 2026-04-25T16:39:04...)
        val cleanString = isoString.substringBefore('.').substringBefore('+').substringBefore('Z')
        val parts = cleanString.split("T")
        if (parts.size == 2) {
            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")
            if (dateParts.size == 3 && timeParts.size >= 2) {
                // Trả về dạng: 25/04/2026 - 16:39
                return "${dateParts[2]}/${dateParts[1]}/${dateParts[0]} lúc ${timeParts[0]}:${timeParts[1]}"
            }
        }
        return cleanString // Nếu parse lỗi thì in ra chuỗi gốc
    } catch (e: Exception) {
        return "Gần đây"
    }
}