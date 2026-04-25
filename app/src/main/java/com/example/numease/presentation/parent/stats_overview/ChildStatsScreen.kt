package com.example.numease.presentation.parent.stats_overview

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.ChildProfile
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

    // Khởi tạo dữ liệu khi vào màn hình
    LaunchedEffect(childId) {
        viewModel.initData(childId)
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Thống kê của ${currentChild?.name ?: "..."}",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { // Khi bấm sẽ chạy về Home
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
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
                // 1. THANH CHỌN KĨ NĂNG (SCROLLABLE TABS)
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
                // 2. PROGRESS BAR (SỐ CÂU ĐÚNG / TỔNG SỐ)
                // ==========================================
                val (correct, total) = progressPair
                val progressRatio = if (total > 0) correct.toFloat() / total.toFloat() else 0f
                val animatedProgress by animateFloatAsState(targetValue = progressRatio)

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tỉ lệ hoàn thành",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Thanh ProgressBar béo ú bo tròn
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFFE0E0E0),
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "$correct / $total câu đúng",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))


                // ==========================================
                // 3. BIỂU ĐỒ CỘT (10 BÀI GẦN NHẤT) & NÚT CHI TIẾT
                // ==========================================
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .shadow(2.dp, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Phong độ 10 bài gần nhất",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        if (recentSessions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Chưa có dữ liệu cho kĩ năng này.", color = Color.Gray)
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                                // Tái sử dụng biểu đồ cột đã làm ở phần Quản lý
                                RecentStatsBarChart(sessions = recentSessions)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Nút xem chi tiết (Tạm thời chỉ làm hiệu ứng UI)
                        OutlinedButton(
                            onClick = {
                                currentChild?.id?.let { id ->
                                    onNavigateToDetailedStats(id, selectedCategoryId)
                                }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Xem hồ sơ khác",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(allChildren) { child ->
                            val isSelected = child.id == currentChild?.id

                            ChildSwitcherCard(
                                child = child,
                                isSelected = isSelected,
                                onClick = {
                                    if (!isSelected && child.id != null) {
                                        // Gọi ViewModel tải lại toàn bộ data cho bé mới
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

// Component Thẻ Kĩ năng có viền sáng màu khi được chọn
@Composable
fun SkillChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xFF1976D2) else Color.White
    val textColor = if (isSelected) Color.White else Color.Gray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else Color(0xFFE0E0E0),
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
    val avatar = if (child.gender.uppercase() == "MALE") "👦" else "👧"

    // Nếu đang được chọn thì làm nổi bật bằng viền màu và nền sáng
    val borderColor = if (isSelected) Color(0xFF1976D2) else Color.Transparent
    val bgColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
    val textColor = if (isSelected) Color(0xFF1565C0) else Color.Gray

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(100.dp)
            .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = avatar, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = child.name,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}