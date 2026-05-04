package com.example.numease.presentation.admin.children

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageChildrenAdminScreen(
    viewModel: ManageChildrenAdminViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToChildDetail: (String) -> Unit
) {
    val children by viewModel.children.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = colorScheme.background, //
        topBar = {
            Surface(
                color = colorScheme.surface,
                tonalElevation = 3.dp, // Tạo độ nổi nhẹ cho thanh công cụ
                modifier = Modifier.statusBarsPadding()
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = { focusManager.clearFocus() })
                    }
            ) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Danh sách Học sinh", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        placeholder = { Text("Tìm theo email phụ huynh...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search // Hiển thị nút "Tìm kiếm" thay vì nút "Enter"
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus() // Ẩn bàn phím khi nhấn nút Search
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = colorScheme.outlineVariant // Đảm bảo viền rõ nét
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.tertiary // Sử dụng màu tím/hồng cho trẻ em
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    // Hoặc thêm vào đây để khi chạm vào khoảng trống giữa các Card sẽ ẩn phím
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    },
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(children) { child ->
                    ChildAdminCard(
                        child = child,
                        onClick = { onNavigateToChildDetail(child.id) }
                    )
                }
            }

            // Thanh phân trang chuẩn MD3
            Surface(
                color = colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tổng số bé: $totalCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.prevPage() },
                            enabled = currentPage > 0
                        ) {
                            Icon(Icons.Default.NavigateBefore, null)
                        }
                        Text(
                            "Trang ${currentPage + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.nextPage() },
                            enabled = (currentPage + 1) * 10 < totalCount
                        ) {
                            Icon(Icons.Default.NavigateNext, null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChildAdminCard(child: ChildWithParent, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    // Sử dụng OutlinedCard để đảm bảo nhìn thấy viền rõ ràng trên Emulator
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar diện chẩn nhanh
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(colorScheme.tertiaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = child.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Độ tuổi: ${child.age} tuổi",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )

                // Hiển thị email phụ huynh liên kết
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mail,
                        contentDescription = null,
                        tint = colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = child.parent?.email ?: "N/A",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colorScheme.outlineVariant
            )
        }
    }
}