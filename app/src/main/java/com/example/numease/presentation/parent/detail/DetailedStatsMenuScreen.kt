package com.example.numease.presentation.parent.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedStatsMenuScreen(
    childId: String,
    categoryId: Int,
    onBack: () -> Unit,
    onSelectChartType: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background, // Tự động nền sáng/tối
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Chọn dạng báo cáo", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
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
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant, // Chữ phụ xám mờ chuẩn MD3
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
                        icon = Icons.Default.SsidChart,
                        iconColor = colorScheme.primary, // Xanh lá theo Theme
                        onClick = { onSelectChartType("LINE") }
                    )
                }
                item {
                    ChartMenuCard(
                        title = "Biểu đồ Cột",
                        subtitle = "So sánh các bài tập",
                        icon = Icons.Default.BarChart,
                        iconColor = colorScheme.secondary, // Xanh dương theo Theme
                        onClick = { onSelectChartType("BAR") }
                    )
                }
                item {
                    ChartMenuCard(
                        title = "Biểu đồ Tròn",
                        subtitle = "Tỉ lệ đúng / sai",
                        icon = Icons.Default.PieChart,
                        iconColor = colorScheme.tertiary, // Vàng/Cam theo Theme
                        onClick = { onSelectChartType("PIE") }
                    )
                }
                item {
                    ChartMenuCard(
                        title = "Dạng Chữ",
                        subtitle = "Lịch sử chi tiết",
                        icon = Icons.Default.FormatListNumbered,
                        iconColor = colorScheme.onSurfaceVariant, // Màu trung tính
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
    val colorScheme = MaterialTheme.colorScheme

    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            // Viền mỏng tạo độ nét cho thẻ khi ở chế độ Dark Mode
            .border(
                width = 1.dp,
                color = colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    // Nền icon lấy chính màu icon nhưng giảm alpha xuống 15%
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}