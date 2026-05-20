package com.example.numease.presentation.parent.stats_overview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.ChildProfile
import com.example.numease.data.model.StudySession
import com.example.numease.presentation.parent.manage.RecentStatsBarChart


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildStatsScreen(
    childId: String,
    viewModel: ChildStatsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToDetailedStats: (childId: String, categoryId: Int) -> Unit
) {
    val currentChild by viewModel.currentChild.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val progressPair by viewModel.currentCategoryProgress.collectAsState()
    val recentSessions by viewModel.recentSessionsForCategory.collectAsState()
    val allChildren by viewModel.allChildren.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(childId) {
        viewModel.initData(childId)
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Thống kê của ${currentChild?.name ?: "..."}",
                        fontWeight = FontWeight.Bold
                    )
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
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ==========================================
                // 1. THANH CHỌN KĨ NĂNG
                // ==========================================
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(GAME_CATEGORIES) { category ->
                        val isSelected = selectedCategoryId == category.id
                        SkillChip(
                            name = category.name,
                            isSelected = isSelected,
                            onClick = { viewModel.selectCategory(category.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ==========================================
                // 2. PROGRESS BAR (Giữ nguyên text to, trả về màu Primary)
                // ==========================================
                val (correct, total) = progressPair
                val progressRatio = if (total > 0) correct.toFloat() / total.toFloat() else 0f
                val animatedProgress by animateFloatAsState(targetValue = progressRatio, label = "progress")

                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tỉ lệ hoàn thành",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Thanh ProgressBar màu Primary chuẩn
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = colorScheme.primary,
                            trackColor = colorScheme.surfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "$correct / $total câu đúng",
                            fontSize = 36.sp, // Chữ to dễ nhìn
                            fontWeight = FontWeight.Black,
                            color = colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ==========================================
                // 3. BIỂU ĐỒ CỘT (Đã xóa Legend)
                // ==========================================
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Phong độ 10 bài gần nhất",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (recentSessions.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Chưa có dữ liệu cho kĩ năng này.", color = colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                                RecentStatsBarChart(sessions = recentSessions)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = {
                                currentChild?.id?.let { id -> onNavigateToDetailedStats(id, selectedCategoryId) }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Xem thống kê chi tiết", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ==========================================
                // 4. CHUYỂN ĐỔI NHANH GIỮA CÁC BÉ
                // ==========================================
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Xem hồ sơ khác",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(allChildren) { child ->
                            val isSelected = child.id == currentChild?.id
                            ChildSwitcherCard(
                                child = child,
                                isSelected = isSelected,
                                onClick = {
                                    if (!isSelected && child.id != null) {
                                        viewModel.switchChild(child.id)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- CẬP NHẬT SKILL CHIP ĐỂ NHẬN MÀU ĐỘNG ---
@Composable
fun SkillChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    // Trả về màu Primary chuẩn
    val bgColor by animateColorAsState(if (isSelected) colorScheme.primary else colorScheme.surface, label = "bg")
    val textColor by animateColorAsState(if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant, label = "text")

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name, color = textColor, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun ChildSwitcherCard(
    child: ChildProfile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val avatar = if (child.gender?.uppercase() == "MALE") "👦" else "👧"

    // Nếu đang được chọn thì dùng primaryContainer, nếu không dùng surface cơ bản
    val containerColor = if (isSelected) colorScheme.primaryContainer else colorScheme.surface
    val textColor = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = avatar, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = child.name,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

