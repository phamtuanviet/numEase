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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.presentation.admin.content.getCategoryStyling

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLevelsScreen(
    categoryId: Int,
    categoryName: String,
    categoryCode: String,
    viewModel: ManageLevelsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToLevelDetail: (Int) -> Unit,
    onNavigateToAddLevel: (Int) -> Unit
) {
    val levelStats by viewModel.levelStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nextLevel by viewModel.nextLevelToCreate.collectAsState()

    val colorScheme = MaterialTheme.colorScheme
    val (icon, categoryColor) = getCategoryStyling(categoryCode)

    LaunchedEffect(categoryId) {
        viewModel.loadLevelsForCategory(categoryId)
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            // FAB chuẩn MD3 với màu sắc tương ứng theo Category
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddLevel(nextLevel) },
                containerColor = categoryColor,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm Level $nextLevel", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = categoryColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Header mô tả chuyên đề
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(categoryColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Quản lý Màn chơi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onBackground
                        )
                        Text(
                            "Chọn một Level để xem danh sách câu hỏi",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (levelStats.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "Chưa có màn chơi nào được tạo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(levelStats) { stat ->
                            LevelCard(
                                stat = stat,
                                color = categoryColor,
                                onClick = { onNavigateToLevelDetail(stat.level) }
                            )
                        }
                        // Khoảng trống an toàn cho FAB
                        item { Spacer(modifier = Modifier.height(88.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelCard(stat: LevelStat, color: Color, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    // Sử dụng OutlinedCard để đảm bảo nhìn thấy viền rõ ràng trên Emulator
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vòng tròn chứa số Level với độ tương phản cao
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${stat.level}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Level ${stat.level}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    "Gồm ${stat.questionCount} câu hỏi",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colorScheme.outlineVariant
            )
        }
    }
}