package com.example.numease.presentation.admin.parent

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.ChildProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageParentsScreen(
    viewModel: ManageParentsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onChildClick: (String) -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val parents by viewModel.parents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current

    var showConfirmDialog by remember { mutableStateOf(false) }
    var dialogActionType by remember { mutableStateOf("") } // "PROMOTE" hoặc "TOGGLE_BAN"
    var selectedUser by remember { mutableStateOf<ParentUser?>(null) }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            Surface(
                color = colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .statusBarsPadding()
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = { focusManager.clearFocus() })
                    }
            ) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Tài khoản Hệ thống", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                        }
                    )

                    // --- TAB ROW CHUYỂN ĐỔI GIỮA 3 ROLE ---
                    TabRow(
                        selectedTabIndex = currentTab.ordinal,
                        containerColor = colorScheme.surface,
                        contentColor = colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                                color = colorScheme.primary,
                                height = 3.dp
                            )
                        }
                    ) {
                        AccountTab.entries.forEach { tab ->
                            Tab(
                                selected = currentTab == tab,
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.onTabSelected(tab)
                                },
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 8.dp),
                        placeholder = { Text("Tìm theo email...") }, // Tất cả đều có email
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = colorScheme.outlineVariant)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    },
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                if (parents.isEmpty() && !isLoading) {
                    item { Text("Không tìm thấy dữ liệu.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
                }
                items(parents) { user ->
                    ParentUserCard(
                        parent = user,
                        onExpand = {
                            focusManager.clearFocus()
                            viewModel.loadChildrenForParent(user.id, it)
                        },
                        onChildClick = onChildClick,

                        // Truyền callback khi Admin bấm nút trong Menu
                        onPromoteClick = {
                            selectedUser = user
                            dialogActionType = "PROMOTE"
                            showConfirmDialog = true
                        },
                        onToggleBanClick = {
                            selectedUser = user
                            dialogActionType = "TOGGLE_BAN"
                            showConfirmDialog = true
                        }
                    )
                }
            }



            // --- THANH PHÂN TRANG ---
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
                        "Tổng: $totalCount kết quả",
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
        if (showConfirmDialog && selectedUser != null) {
            val userEmail = selectedUser!!.email ?: "Tài khoản này"
            val isCurrentlyBanned = selectedUser!!.isBanned

            val title = if (dialogActionType == "PROMOTE") "Cấp quyền Admin"
            else if (isCurrentlyBanned) "Mở khóa tài khoản" else "Khóa tài khoản"

            val message = if (dialogActionType == "PROMOTE") "Bạn có chắc chắn muốn cấp quyền ADMIN cho $userEmail? Tài khoản này sẽ có quyền quản trị toàn bộ hệ thống."
            else if (isCurrentlyBanned) "Bạn muốn mở khóa cho $userEmail để họ tiếp tục sử dụng app?"
            else "Bạn có chắc muốn KHÓA $userEmail? Người này sẽ bị văng khỏi app và không thể đăng nhập lại."

            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text(title, fontWeight = FontWeight.Bold) },
                text = { Text(message) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (dialogActionType == "PROMOTE") {
                                viewModel.promoteToAdmin(selectedUser!!.id) { showConfirmDialog = false }
                            } else {
                                viewModel.toggleBanUser(selectedUser!!.id, isCurrentlyBanned) { showConfirmDialog = false }
                            }
                        }
                    ) {
                        Text("Đồng ý", color = if (dialogActionType == "PROMOTE") Color.Blue else Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) { Text("Hủy") }
                }
            )
        }
    }
}

@Composable
fun ParentUserCard(
    parent: ParentUser,
    onExpand: ((List<ChildProfile>) -> Unit) -> Unit,
    onChildClick: (String) -> Unit,
    // THÊM 2 CALLBACK NÀY
    onPromoteClick: () -> Unit,
    onToggleBanClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) } // State cho Dropdown Menu
    var childrenList by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }
    var isChildLoading by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    // Làm mờ thẻ nếu tài khoản bị khóa
    val cardAlpha = if (parent.isBanned) 0.5f else 1f

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .alpha(cardAlpha), // Áp dụng độ mờ
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (parent.isBanned) colorScheme.surfaceVariant else colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (parent.isBanned) colorScheme.error else colorScheme.outlineVariant
            )
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
                        // Gạch ngang email nếu bị khóa
                        textDecoration = if (parent.isBanned) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        val roleColor = when (parent.role) {
                            "ADMIN" -> colorScheme.error
                            "STUDENT" -> colorScheme.tertiary
                            else -> colorScheme.primary
                        }

                        Surface(color = roleColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = if (parent.isBanned) "ĐÃ BỊ KHÓA" else (parent.role ?: "PARENT"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (parent.isBanned) colorScheme.error else roleColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Tạo: ${parent.createdAt?.take(10) ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (parent.role != "ADMIN") {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cấp quyền Admin") },
                                onClick = {
                                    menuExpanded = false
                                    onPromoteClick()
                                },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (parent.isBanned) "Mở khóa tài khoản" else "Khóa tài khoản",
                                        color = colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onToggleBanClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        if (parent.isBanned) Icons.Default.LockOpen else Icons.Default.Block,
                                        null,
                                        tint = colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }

                // CHỈ HIỂN THỊ NÚT EXPAND NẾU LÀ PARENT
                if (parent.role == "PARENT") {
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
            }

            if (expanded && parent.role == "PARENT") {
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