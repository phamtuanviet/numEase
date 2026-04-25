package com.example.numease.presentation.admin.manage_category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.presentation.admin.content.getCategoryStyling

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLevelsScreen(
    categoryId: Int,
    categoryName: String,
    categoryCode: String, // Dùng code để lấy đúng icon và màu như màn hình trước
    viewModel: ManageLevelsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToLevelDetail: (Int) -> Unit, // Tạm thời rỗng
    onNavigateToAddLevel: (Int) -> Unit     // Tạm thời rỗng
) {
    val levelStats by viewModel.levelStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nextLevel by viewModel.nextLevelToCreate.collectAsState()

    // Lấy theme màu tương ứng với Category
    val (icon, color) = getCategoryStyling(categoryCode)

    LaunchedEffect(categoryId) {
        viewModel.loadLevelsForCategory(categoryId)
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            // Nút Thêm Level lơ lửng góc phải dưới
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddLevel(nextLevel) },
                containerColor = color,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Thêm Level $nextLevel", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = color)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Quản lý Màn chơi", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF37474F))
                        Text("Chọn một Level để xem danh sách câu hỏi", fontSize = 14.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (levelStats.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Chưa có câu hỏi nào. Hãy tạo Level đầu tiên!", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(levelStats) { stat ->
                            LevelCard(stat = stat, color = color, onClick = { onNavigateToLevelDetail(stat.level) })
                        }
                        // Thêm một khoảng trống ở đáy để không bị cái nút Floating che mất thẻ cuối cùng
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelCard(stat: LevelStat, color: Color, onClick: () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon vòng tròn chứa số Level
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${stat.level}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Level ${stat.level}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                Text("Gồm ${stat.questionCount} câu hỏi", fontSize = 14.sp, color = Color.Gray)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}