package com.example.numease.presentation.parent.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedStatsMenuScreen(
    childId: String,
    categoryId: Int,
    onBack: () -> Unit,
    onSelectChartType: (String) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chọn dạng báo cáo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bạn muốn xem phân tích theo định dạng nào?",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Lưới 2x2
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    ChartMenuCard(
                        title = "Biểu đồ Đường",
                        subtitle = "Phong độ theo thời gian",
                        icon = Icons.Default.SsidChart, // Icon dạng đường cong
                        iconColor = Color(0xFF4CAF50),
                        onClick = { onSelectChartType("LINE") }
                    )
                }
                item {
                    ChartMenuCard(
                        title = "Biểu đồ Cột",
                        subtitle = "So sánh các bài tập",
                        icon = Icons.Default.BarChart,
                        iconColor = Color(0xFF2196F3),
                        onClick = { onSelectChartType("BAR") }
                    )
                }
                item {
                    ChartMenuCard(
                        title = "Biểu đồ Tròn",
                        subtitle = "Tỉ lệ đúng / sai",
                        icon = Icons.Default.PieChart,
                        iconColor = Color(0xFFFF9800),
                        onClick = { onSelectChartType("PIE") }
                    )
                }
                item {
                    ChartMenuCard(
                        title = "Dạng Chữ",
                        subtitle = "Lịch sử chi tiết",
                        icon = Icons.Default.FormatListNumbered,
                        iconColor = Color(0xFF9C27B0),
                        onClick = { onSelectChartType("TEXT") }
                    )
                }
            }
        }
    }
}

@Composable
fun ChartMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            // Tỉ lệ 1:1 cho thẻ vuông vức đẹp mắt
            .aspectRatio(0.85f)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}