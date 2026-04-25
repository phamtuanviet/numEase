package com.example.numease.presentation.parent.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.presentation.UserPreferencesViewModel

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

    // Trạng thái Dialogs
    var showSettings by remember { mutableStateOf(false) }

    // --- DIALOG CÀI ĐẶT DÀNH CHO PHỤ HUYNH ---
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
                    OutlinedButton(
                        onClick = {
                            showSettings = false
                            onNavigateToStudentWorkspace()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Chuyển sang Chế độ Học sinh")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // --- DIALOG CHỌN BÉ ĐỂ XEM THỐNG KÊ ---

    // ==========================================
    // GIAO DIỆN CHÍNH
    // ==========================================
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Xin chào,",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = parentName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(
                    onClick = { showSettings = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Settings, "Cài đặt", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {

                // Thẻ thông tin tổng quan
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👨‍👩‍👧‍👦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Gia đình của bạn",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Đang quản lý ${childrenList.size} hồ sơ học sinh",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Chức năng chính",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Tính năng 1: Quản lý hồ sơ
                ParentFeatureCard(
                    title = "Quản lý Hồ sơ",
                    description = "Thêm, sửa đổi hoặc xóa thông tin tài khoản học tập của các bé.",
                    icon = Icons.Default.Face,
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF1565C0),
                    onClick = onNavigateToManageChildren
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tính năng 2: Thống kê học tập
                ParentFeatureCard(
                    title = "Báo cáo & Thống kê",
                    description = "Theo dõi tiến độ học tập, điểm mạnh và điểm yếu của từng bé.",
                    icon = Icons.Default.Analytics,
                    containerColor = Color(0xFFFFF3E0),
                    contentColor = Color(0xFFE65100),
                    onClick = {
                        if (childrenList.isEmpty()) {
                            // Không làm gì (hoặc bạn có thể show Toast nhắc phụ huynh thêm bé)
                        } else if (childrenList.size == 1) {
                            // Chỉ có 1 bé -> Bỏ qua màn hình chọn, bay thẳng vào Thống kê
                            childrenList.first().id?.let { onNavigateToDirectStats(it) }
                        } else {
                            // Nhiều bé -> Chuyển sang Route ChildSelectionStatsScreen
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
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
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
                Icon(icon, contentDescription = title, tint = contentColor, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}