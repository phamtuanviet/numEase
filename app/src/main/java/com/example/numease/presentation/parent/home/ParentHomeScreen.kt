package com.example.numease.presentation.parent.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.presentation.UserPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentHomeScreen(
    viewModel: ParentHomeViewModel = hiltViewModel(),
    userPrefsViewModel: UserPreferencesViewModel = hiltViewModel(),
    onNavigateToManageChildren: () -> Unit,
    onNavigateToStudentWorkspace: () -> Unit,
    onNavigateToChildSelection: () -> Unit,
    onNavigateToDirectStats: (childId: String) -> Unit,
    onLogout: () -> Unit
) {
    val parentName by viewModel.parentName.collectAsState()
    val childrenList by viewModel.childrenList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val userPrefs by userPrefsViewModel.preferences.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    // --- DIALOG CÀI ĐẶT ---
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
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
            title = { Text("Cài đặt", style = MaterialTheme.typography.titleLarge) },
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
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Âm thanh hệ thống", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = userPrefs.isSoundEnabled,
                            onCheckedChange = { userPrefsViewModel.toggleSound(it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = {
                            showSettings = false
                            onNavigateToStudentWorkspace()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Chuyển sang Chế độ Học sinh")
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        // Cấu hình TopAppBar của MD3 sẽ tự động xử lý WindowInsets (tránh đè lên thanh WiFi/Pin)
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Xin chào, $parentName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Trang quản lý Phụ Huynh",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Scaffold sẽ cung cấp padding tương ứng với TopAppBar
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Thẻ thông tin tổng quan sử dụng ElevatedCard của MD3
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar sử dụng Material Icon thay cho Emoji
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Group, // Bạn có thể đổi thành Icons.Default.SupervisorAccount
                                    contentDescription = "Biểu tượng gia đình",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Gia đình của bạn",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Đang quản lý ${childrenList.size} hồ sơ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Chức năng chính",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Tính năng 1: Quản lý hồ sơ
                ParentFeatureCard(
                    title = "Quản lý Hồ sơ",
                    description = "Thêm, sửa đổi hoặc xóa thông tin tài khoản học tập của các bé.",
                    icon = Icons.Default.Face,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onNavigateToManageChildren
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tính năng 2: Thống kê học tập
                ParentFeatureCard(
                    title = "Báo cáo & Thống kê",
                    description = "Theo dõi tiến độ học tập, điểm mạnh và điểm yếu của từng bé.",
                    icon = Icons.Default.Analytics,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = {
                        if (childrenList.isEmpty()) {
                            // Không làm gì (hoặc show Toast)
                        } else if (childrenList.size == 1) {
                            childrenList.first().id?.let { onNavigateToDirectStats(it) }
                        } else {
                            onNavigateToChildSelection()
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// COMPONENT: THẺ TÍNH NĂNG CHUYÊN NGHIỆP
// ==========================================
@Composable
fun ParentFeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(contentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = contentColor, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}