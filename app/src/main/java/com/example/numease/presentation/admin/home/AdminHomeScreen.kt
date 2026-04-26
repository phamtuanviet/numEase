package com.example.numease.presentation.admin.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.presentation.UserPreferencesViewModel
import com.example.numease.presentation.component.UserGrowthLineChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    viewModel: AdminHomeViewModel = hiltViewModel(),
    userPrefsViewModel: UserPreferencesViewModel = hiltViewModel(),
    onNavigateToManageContent: () -> Unit,
    onNavigateToManageUsers: () -> Unit,
    onLogout: () -> Unit
) {
    val userPrefs by userPrefsViewModel.preferences.collectAsState()
    val totalExerciseTypes by viewModel.totalExerciseTypes.collectAsState()
    val totalQuestions by viewModel.totalQuestions.collectAsState()
    val totalUsers by viewModel.totalUsers.collectAsState()
    val totalChildren by viewModel.totalChildren.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val growthData by viewModel.growthData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadGrowthStats()
    }

    var showSettings by remember { mutableStateOf(false) }

    // Dialog cài đặt cho Admin
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                Button(
                    onClick = { showSettings = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Đóng")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSettings = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Đăng xuất", fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chế độ tối")
                        Switch(
                            checked = userPrefs.isDarkMode,
                            onCheckedChange = { userPrefsViewModel.toggleDarkMode(it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Âm thanh hệ thống")
                        Switch(
                            checked = userPrefs.isSoundEnabled,
                            onCheckedChange = { userPrefsViewModel.toggleSound(it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Nút đưa máy lại cho con học

                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bảng điều khiển Admin", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Đang ở Home */ },
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Tổng quan") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToManageContent,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chào Admin,",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Hệ thống đang vận hành ổn định",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lưới các thông số (Grid 2x2)
            Box(modifier = Modifier.height(220.dp)) { // Cố định chiều cao cho Grid trong ScrollView
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) {
                    item {
                        AdminStatCard(
                            "Loại bài học",
                            totalExerciseTypes.toString(),
                            Icons.Default.Category,
                            Color(0xFFE91E63)
                        )
                    }
                    // Thẻ 2: Tổng số câu hỏi (Bảng exercises)
                    item {
                        AdminStatCard(
                            "Tổng câu hỏi",
                            totalQuestions.toString(),
                            Icons.Default.Quiz,
                            Color(0xFF2196F3)
                        )
                    }
                    // Thẻ 3: Số phụ huynh
                    item {
                        AdminStatCard(
                            "Phụ huynh",
                            totalUsers.toString(),
                            Icons.Default.SupervisorAccount,
                            Color(0xFFFF9800)
                        )
                    }
                    // Thẻ 4: Số học sinh
                    item {
                        AdminStatCard(
                            "Học sinh",
                            totalChildren.toString(),
                            Icons.Default.ChildCare,
                            Color(0xFF9C27B0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Khu vực Biểu đồ Tăng trưởng (Dùng Canvas vẽ Area Chart đơn giản)
            Text(
                text = "Người dùng mới (10 ngày qua)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shadowElevation = 2.dp
            ) {
                if (growthData.isEmpty()) {
                    Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            UserGrowthLineChart(data = growthData)
                        }

                        // Hiển thị nhãn ngày dưới trục X
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            growthData.filterIndexed { index, _ -> index % 2 == 0 }.forEach {
                                Text(it.first, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape)
                    .padding(6.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = title, fontSize = 14.sp, color = Color.Gray)
        }
    }
}