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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageContentScreen(
    viewModel: ManageContentViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToManageUsers: () -> Unit,
    onNavigateToManageLevels: (categoryId: Int, categoryName: String, categoryCode: String) -> Unit
) {
    val categoryStats by viewModel.categoryStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý Nội dung", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Tổng quan") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
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
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground
                )
                Text(
                    text = "Chọn một chuyên đề để quản lý các màn chơi bên trong.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = categoryStats
                    ) { index, stat ->
                        CategoryBlockCard(
                            stat = stat,
                            onClick = {
                                onNavigateToManageLevels(stat.id, stat.name, stat.code)
                            },
                            // Sửa logic span trực tiếp ở đây hoặc dùng logic cũ của bạn
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBlockCard(
    stat: CategoryStat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val (icon, color) = getCategoryStyling(stat.code)

    // Dùng OutlinedCard để đảm bảo nhìn thấy viền rõ ràng trên Emulator
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
        ),
        modifier = modifier.aspectRatio(1f)
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Chip hiển thị số lượng
            Surface(
                color = color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${stat.totalQuestions} bài tập",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

fun getCategoryStyling(code: String): Pair<ImageVector, Color> {
    return when (code.uppercase()) {
        "COUNTING" -> Pair(Icons.Default.Filter1, Color(0xFF4CAF50))
        "COMPARING" -> Pair(Icons.Default.CompareArrows, Color(0xFFFF9800))
        "DRAG_DROP" -> Pair(Icons.Default.Swipe, Color(0xFF9C27B0))
        "ADDITION" -> Pair(Icons.Default.AddCircleOutline, Color(0xFF2196F3))
        "SUBTRACTION" -> Pair(Icons.Default.RemoveCircleOutline, Color(0xFFE91E63))
        else -> Pair(Icons.Default.Category, Color.Gray)
    }
}