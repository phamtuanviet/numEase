package com.example.numease.presentation.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.presentation.component.ParentGateDialog
import com.example.numease.presentation.UserPreferencesViewModel
import com.example.numease.presentation.component.RecentSessionsDialog
import com.example.numease.presentation.viewmodel.AuthState
import com.example.numease.presentation.viewmodel.AuthViewModel

@Composable
fun StudentHomeScreen(
    studentViewModel: StudentHomeViewModel = hiltViewModel(),
    userPrefsViewModel: UserPreferencesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onPlayClicked: () -> Unit,
    onParentGatePassed: () -> Unit,
    isParentAccount: Boolean,
    onLogoutClicked: () -> Unit,
) {
    // 1. Lấy dữ liệu của Bé
    val recentSessions by studentViewModel.recentSessions.collectAsState()
    val activeChild by studentViewModel.childSessionManager.activeChild.collectAsState()
    val totalStars by studentViewModel.totalStars.collectAsState()

    // 2. Lấy dữ liệu Cấu hình toàn cục
    val userPrefs by userPrefsViewModel.preferences.collectAsState()
    val isDarkTheme = userPrefs.isDarkMode
    val isSoundEnabled = userPrefs.isSoundEnabled

    val authState by authViewModel.authState.collectAsState()
    val correctPin = (authState as? AuthState.Authenticated)?.profile?.parentPin
    // State quản lý Dialog
    var showRecentStats by remember { mutableStateOf(false) }
    var showChildInfo by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    var showPinDialog by remember { mutableStateOf(false) }
    var inputPin by remember { mutableStateOf("") }
    var isPinError by remember { mutableStateOf(false) }

    // --- CÁC DIALOGS ---
    if (showRecentStats) {
        RecentSessionsDialog(
            sessions = recentSessions,
            onDismiss = { showRecentStats = false }
        )
    }

    if (showChildInfo) {
        AlertDialog(
            onDismissRequest = { showChildInfo = false },
            confirmButton = {
                Button(onClick = { showChildInfo = false }) { Text("Đóng") }
            },
            title = {
                Text(
                    text = "Hồ sơ của bé",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("🦊", fontSize = 72.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = activeChild?.name ?: "Bé Ngoan",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer, // Màu nền sao (Sáng: Vàng nhạt, Tối: Nâu tối)
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Thành tích: $totalStars ",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text("⭐", fontSize = 20.sp)
                        }
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                if (isParentAccount) {
                    Button(
                        onClick = {
                            showSettings = false
                            if (!correctPin.isNullOrBlank()) {
                                inputPin = ""
                                isPinError = false
                                showPinDialog = true // Mở popup nhập số
                            } else {
                                onParentGatePassed() // Nếu chưa cài PIN thì cho qua luôn
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("Dành cho phụ huynh")
                    }
                } else {
                    Button(
                        onClick = { showSettings = false }
                    ) {
                        Text("Đóng")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSettings = false
                        onLogoutClicked()
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
                        Text("Chế độ tối", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { userPrefsViewModel.toggleDarkMode(it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Âm thanh", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = { userPrefsViewModel.toggleSound(it) }
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Nhập mã PIN", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column {
                    Text("Khu vực này dành cho Phụ huynh. Vui lòng nhập mã PIN 4 số.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                inputPin = it
                                isPinError = false
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = isPinError,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isPinError) {
                        Text("Mã PIN không đúng!", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputPin == correctPin) {
                            showPinDialog = false
                            onParentGatePassed() // PIN đúng -> Cho qua màn Admin/Parent
                        } else {
                            isPinError = true // Xử lý lỗi
                        }
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Hủy") }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // --- GIAO DIỆN CHÍNH ---
    // Background tự động lấy LightBackground hoặc DarkBackground
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {

            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thẻ thông tin bé (Sử dụng Secondary Container)
                ElevatedCard(
                    onClick = { showChildInfo = true },
                    shape = CircleShape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("🦊", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = activeChild?.name ?: "Bé Ngoan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Cụm Ngôi sao & Cài đặt
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Thẻ Ngôi sao (Sử dụng Tertiary Container)
                    ElevatedCard(
                        onClick = {
                            studentViewModel.childSessionManager.activeChild.value?.id?.let {
                                studentViewModel.fetchRecentSessions(it)
                            }
                            showRecentStats = true
                        },
                        shape = CircleShape,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$totalStars",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("⭐", fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .size(48.dp)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // --- MAIN CONTENT ---
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🚀", fontSize = 160.sp)

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Sẵn sàng chưa, ${activeChild?.name ?: "bé"}?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Nút Bắt đầu chơi (Sử dụng Primary Color)
                Button(
                    onClick = onPlayClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(88.dp)
                        .shadow(12.dp, CircleShape)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Chơi",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "BẮT ĐẦU CHƠI",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}