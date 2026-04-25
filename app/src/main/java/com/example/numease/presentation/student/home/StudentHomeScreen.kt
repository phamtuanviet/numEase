package com.example.numease.presentation.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import com.example.numease.presentation.component.ParentGateDialog

import androidx.compose.material3.Switch // Nhớ import Switch
import com.example.numease.presentation.UserPreferencesViewModel
import com.example.numease.presentation.component.RecentSessionsDialog

// ... các import khác

@Composable
fun StudentHomeScreen(
    studentViewModel: StudentHomeViewModel = hiltViewModel(),
    userPrefsViewModel: UserPreferencesViewModel = hiltViewModel(),
    onPlayClicked: () -> Unit,
    onParentGatePassed: () -> Unit,
    isParentAccount: Boolean,
    onLogoutClicked: () -> Unit,
) {
    // 1. Lấy dữ liệu của Bé

    val recentSessions by studentViewModel.recentSessions.collectAsState()

    var showRecentStats by remember { mutableStateOf(false) }
    if (showRecentStats) {
        RecentSessionsDialog(
            sessions = recentSessions,
            onDismiss = { showRecentStats = false }
        )
    }
    val activeChild by studentViewModel.childSessionManager.activeChild.collectAsState()
    val totalStars by studentViewModel.totalStars.collectAsState()

    // 2. Lấy dữ liệu Cấu hình toàn cục
    val userPrefs by userPrefsViewModel.preferences.collectAsState()
    val isDarkTheme = userPrefs.isDarkMode
    val isSoundEnabled = userPrefs.isSoundEnabled

    // State quản lý Dialog
    var showChildInfo by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showParentGate by remember { mutableStateOf(false) }

    // --- DIALOG THÔNG TIN BÉ ---
    if (showChildInfo) {
        AlertDialog(
            onDismissRequest = { showChildInfo = false },
            confirmButton = {
                TextButton(onClick = { showChildInfo = false }) { Text("Đóng") }
            },
            title = { Text("Thông tin của bé", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("🐻", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tên: ${activeChild?.name}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Thành tích: $totalStars ⭐", color = Color(0xFFF57F17), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    // --- DIALOG CÀI ĐẶT (Gọi trực tiếp hàm lưu của UserPrefs) ---
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            // Nút bên phải (Xác nhận / Chuyển hướng)
            confirmButton = {
                if (isParentAccount) {
                    // Nếu là tk Phụ huynh -> Hiện nút vào vùng Phụ huynh
                    Button(
                        onClick = {
                            showSettings = false
                            showParentGate = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("Dành cho phụ huynh")
                    }
                } else {
                    // Nếu là tk Học sinh -> Chỉ hiện nút Đóng bình thường
                    Button(
                        onClick = { showSettings = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Đóng")
                    }
                }
            },
            // Nút bên trái (Đăng xuất)
            dismissButton = {
                TextButton(
                    onClick = {
                        showSettings = false
                        onLogoutClicked()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F)) // Màu đỏ cảnh báo
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
                        Text("Chế độ tối", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { userPrefsViewModel.toggleDarkMode(it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Âm thanh", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = { userPrefsViewModel.toggleSound(it) }
                        )
                    }
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    // --- CỔNG PHỤ HUYNH ---
    if (showParentGate) {
        ParentGateDialog(
            onDismiss = { showParentGate = false },
            onSuccess = {
                showParentGate = false
                onParentGatePassed()
            }
        )
    }

    // GIAO DIỆN CHÍNH (Sẽ tự động đổi màu khi isDarkTheme thay đổi)
    val bgColor = MaterialTheme.colorScheme.background // Lấy màu chuẩn từ Theme bạn đã định nghĩa
    val textColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier.fillMaxSize().background(bgColor)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Info Bé
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.shadow(2.dp, RoundedCornerShape(24.dp)),
                onClick = { showChildInfo = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("🐻", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeChild?.name ?: "Bé Ngoan",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Nút Sao & Cài đặt
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFFFF8E1),
                    modifier = Modifier.shadow(2.dp, RoundedCornerShape(24.dp)),
                    onClick = {
                        // Cập nhật dữ liệu mới nhất trước khi hiện
                        studentViewModel.childSessionManager.activeChild.value?.id?.let {
                            studentViewModel.fetchRecentSessions(it)
                        }
                        showRecentStats = true
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("$totalStars", fontWeight = FontWeight.ExtraBold, color = Color(0xFFF57F17))
                        Text(" ⭐")
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = { showSettings = true },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(40.dp)
                ) {
                    Icon(Icons.Default.Settings, "Cài đặt", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // --- MAIN CONTENT ---
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🦊", fontSize = 140.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Chào bé ${activeChild?.name ?: ""}!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onPlayClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth(0.8f).height(80.dp).shadow(8.dp, RoundedCornerShape(32.dp))
            ) {
                Text("BẮT ĐẦU CHƠI", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}