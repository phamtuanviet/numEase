package com.example.numease.presentation.admin.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToContent: () -> Unit,
    onNavigateToParents: () -> Unit,
    onNavigateToChildren: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background, // Trả về màu nền gốc sạch sẽ
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý Người dùng", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            // Thanh điều hướng chuẩn MD3
            NavigationBar(
                containerColor = colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Tổng quan") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToContent,
                    icon = { Icon(Icons.Default.MenuBook, null) },
                    label = { Text("Nội dung") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Đang ở đây rồi */ },
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hệ thống tài khoản",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = colorScheme.onBackground
            )
            Text(
                text = "Chọn nhóm người dùng bạn muốn quản lý.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Thẻ 1: Quản lý Phụ huynh
            UserTypeCard(
                title = "Tài khoản hệ thống ",
                description = "Quản lý email, trạng thái khóa/mở tài khoản, phân quyền hệ thống.",
                icon = Icons.Default.SupervisorAccount,
                color = colorScheme.secondary, // Dùng màu Secondary của Theme
                onClick = onNavigateToParents
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Thẻ 2: Quản lý Học sinh
            UserTypeCard(
                title = "Hồ sơ Học sinh",
                description = "Xem danh sách các bé, độ tuổi, và liên kết với tài khoản phụ huynh.",
                icon = Icons.Default.ChildCare,
                color = colorScheme.tertiary, // Dùng màu Tertiary của Theme
                onClick = onNavigateToChildren
            )
        }
    }
}

@Composable
fun UserTypeCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Sử dụng OutlinedCard để đảm bảo nhìn thấy viền rõ ràng trên Emulator
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
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