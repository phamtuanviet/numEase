package com.example.numease.presentation.admin.content
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageContentScreen(
    viewModel: ManageContentViewModel = hiltViewModel(),
    // Các callback điều hướng cho BottomBar
    onNavigateToHome: () -> Unit,
    onNavigateToManageUsers: () -> Unit,
    onNavigateToManageLevels: (categoryId: Int, categoryName: String, categoryCode: String) -> Unit
) {
    val categoryStats by viewModel.categoryStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý Nội dung", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            // Thanh điều hướng dùng chung giống AdminHomeScreen
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Tổng quan") }
                )
                NavigationBarItem(
                    selected = true, // Đang ở màn Nội dung nên báo sáng thẻ này
                    onClick = { /* Đang ở đây rồi */ },
                    icon = { Icon(Icons.Default.MenuBook, null) },
                    label = { Text("Nội dung") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToManageUsers,
                    icon = { Icon(Icons.Default.People, null) },
                    label = { Text("Người dùng") }
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ngân hàng câu hỏi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF37474F)
                )
                Text(
                    text = "Chọn một chuyên đề để quản lý các màn chơi bên trong.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = categoryStats,
                        // Làm cho ô cuối cùng (nếu lẻ) nằm tràn viền ra giữa cho đẹp
                        span = { index, _ ->
                            if (index == categoryStats.lastIndex && categoryStats.size % 2 != 0) {
                                GridItemSpan(2)
                            } else {
                                GridItemSpan(1)
                            }
                        }
                    ) { _, stat ->
                        CategoryBlockCard(
                            stat = stat,
                            onClick = {
                                onNavigateToManageLevels(stat.id, stat.name, stat.code)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBlockCard(stat: CategoryStat, onClick: () -> Unit) {
    // Tự động gán Icon và Màu sắc dựa theo mã CODE của thể loại
    val (icon, color) = getCategoryStyling(stat.code)

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Tạo hình vuông
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
                    .size(56.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stat.name,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stat.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Hiển thị số lượng câu hỏi bằng một cục Chip nhỏ
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${stat.totalQuestions} bài tập",
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Hàm hỗ trợ chọn Icon và Màu sắc tự động
fun getCategoryStyling(code: String): Pair<ImageVector, Color> {
    return when (code.uppercase()) {
        "COUNTING" -> Pair(Icons.Default.Filter1, Color(0xFF4CAF50)) // Xanh lá
        "COMPARING" -> Pair(Icons.Default.CompareArrows, Color(0xFFFF9800)) // Cam
        "DRAG_DROP" -> Pair(Icons.Default.Swipe, Color(0xFF9C27B0)) // Tím
        "ADDITION" -> Pair(Icons.Default.AddCircleOutline, Color(0xFF2196F3)) // Xanh dương
        "SUBTRACTION" -> Pair(Icons.Default.RemoveCircleOutline, Color(0xFFE91E63)) // Hồng đỏ
        else -> Pair(Icons.Default.Category, Color.Gray)
    }
}