package com.example.numease.presentation.admin.parent

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.ChildProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageParentsScreen(
    viewModel: ManageParentsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onChildClick: (String) -> Unit
) {
    val parents by viewModel.parents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current


    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            Surface(
                color = colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.statusBarsPadding()
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = { focusManager.clearFocus() })
                    }
            ) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Tài khoản Phụ huynh", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                        }
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        placeholder = { Text("Tìm theo email...") },
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
                            unfocusedBorderColor = colorScheme.outlineVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceVariant
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
                items(parents) { parent ->
                    ParentUserCard(
                        parent = parent,
                        onExpand = {
                            focusManager.clearFocus() // Tắt phím khi nhấn mở rộng
                            viewModel.loadChildrenForParent(parent.id, it)
                        },
                        onChildClick = onChildClick
                    )
                }
            }

            // --- THANH PHÂN TRANG CHUẨN MD3 ---
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
                        "Tổng: $totalCount user",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.prevPage() },
                            enabled = currentPage > 0
                        ) {
                            Icon(Icons.Default.NavigateBefore, "Trang trước")
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
                            Icon(Icons.Default.NavigateNext, "Trang sau")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentUserCard(
    parent: ParentUser,
    onExpand: ((List<ChildProfile>) -> Unit) -> Unit,
    onChildClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var childrenList by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }
    var isChildLoading by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = colorScheme.onSecondaryContainer)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        parent.email ?: "Unknown Email",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Badge hiển thị Role
                        val roleColor = if (parent.role == "ADMIN") colorScheme.error else colorScheme.primary
                        Surface(
                            color = roleColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = parent.role ?: "USER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = roleColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Ngày tạo: ${parent.createdAt?.take(10) ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = {
                    expanded = !expanded
                    if (expanded && childrenList.isEmpty()) {
                        isChildLoading = true
                        onExpand {
                            childrenList = it
                            isChildLoading = false
                        }
                    }
                }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Mở rộng"
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = colorScheme.outlineVariant
                )
                Text(
                    "Danh sách trẻ em:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                if (isChildLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else if (childrenList.isEmpty()) {
                    Text(
                        "Phụ huynh này chưa tạo hồ sơ bé nào.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    childrenList.forEach { child ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChildClick(child.id!!) },
                            color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ChildCare,
                                    null,
                                    tint = colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "${child.name} (${child.age} tuổi)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.ChevronRight,
                                    null,
                                    tint = colorScheme.outlineVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}