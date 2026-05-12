package com.example.numease.presentation.admin.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
    val colorScheme = MaterialTheme.colorScheme
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

    // --- DIALOG CÀI ĐẶT ---
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) { Text("Đóng") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSettings = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                ) {
                    Text("Đăng xuất", fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Cài đặt hệ thống", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chế độ tối", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = userPrefs.isDarkMode,
                            onCheckedChange = { userPrefsViewModel.toggleDarkMode(it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Âm thanh", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = userPrefs.isSoundEnabled,
                            onCheckedChange = { userPrefsViewModel.toggleSound(it) }
                        )
                    }
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        // TRẢ VỀ: Màu nền gốc sạch sẽ của hệ thống
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bảng điều khiển Admin", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
                    }
                },
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
                    selected = true,
                    onClick = { },
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
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.primary
            )
            Text(
                text = "Hệ thống đang vận hành ổn định",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // GRID TỰ CO GIÃN (Sửa lỗi che thẻ)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard(
                        title = "Loại bài học",
                        value = totalExerciseTypes.toString(),
                        icon = Icons.Default.Category,
                        color = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Tổng câu hỏi",
                        value = totalQuestions.toString(),
                        icon = Icons.Default.Quiz,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard(
                        title = "Phụ huynh",
                        value = totalUsers.toString(),
                        icon = Icons.Default.SupervisorAccount,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Học sinh",
                        value = totalChildren.toString(),
                        icon = Icons.Default.ChildCare,
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Người dùng mới (10 ngày qua)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // BIỂU ĐỒ VỚI VIỀN RÕ RÀNG
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            ) {
                if (isLoading || growthData.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 24.dp, bottom = 12.dp, end = 24.dp) // Không cần padding start vì đã có yAxisPadding
                    ) {
                        UserGrowthLineChart(data = growthData)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    // SỬ DỤNG OUTLINED CARD: Để đảm bảo luôn nhìn thấy viền dù nền màu gì
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
        )
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
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}