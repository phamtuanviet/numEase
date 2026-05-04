package com.example.numease.presentation.parent.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.ChildProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildSelectionStatsScreen(
    viewModel: ChildSelectionStatsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onChildSelected: (String) -> Unit
) {
    val children by viewModel.children.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Chọn trẻ xem thống kê", fontWeight = FontWeight.Bold)
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
        },
        // SỬA LẠI: Trả về màu nền nguyên bản, sạch sẽ và đồng bộ với màn ManageChildrenScreen
        containerColor = colorScheme.background
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Vui lòng chọn một hồ sơ để xem báo cáo chi tiết về tiến độ học tập.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(children) { child ->
                    ChildSelectionCard(
                        child = child,
                        onClick = { child.id?.let { onChildSelected(it) } }
                    )
                }
            }
        }
    }
}

@Composable
fun ChildSelectionCard(
    child: ChildProfile,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val (avatar, avatarBgColor, avatarContentColor) = when (child.gender?.uppercase()) {
        "MALE" -> Triple("👦", colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)
        "FEMALE" -> Triple("👧", colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
        else -> Triple("🦊", colorScheme.primaryContainer, colorScheme.onPrimaryContainer)
    }

    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            // SỬA LẠI: Chỉ giữ viền mờ cực mỏng để tách biệt thẻ khỏi nền, bỏ hoàn toàn các màu loạn xạ
            .border(
                width = 1.dp,
                color = colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(avatarBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(avatar, fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${child.age ?: "?"} tuổi • Màn chơi hiện tại: ${child.currentLevel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Xem thống kê",
                tint = colorScheme.outline
            )
        }
    }
}