package com.example.numease.presentation.admin.parent

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = { Text("Tài khoản Phụ huynh", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                    }
                )
                // Thanh tìm kiếm
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Tìm theo email...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(parents) { parent ->
                    ParentUserCard(
                        parent = parent,
                        onExpand = { viewModel.loadChildrenForParent(parent.id, it) },
                        onChildClick = onChildClick
                    )
                }
            }

            // Thanh phân trang (Pagination Controls)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tổng: $totalCount user", fontSize = 14.sp, color = Color.Gray)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.prevPage() }, enabled = currentPage > 0) {
                        Icon(Icons.Default.NavigateBefore, null)
                    }
                    Text("Trang ${currentPage + 1}", fontWeight = FontWeight.Bold)
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

@Composable
fun ParentUserCard(
    parent: ParentUser,
    onExpand: ((List<ChildProfile>) -> Unit) -> Unit,
    onChildClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var childrenList by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }
    var isChildLoading by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFF9800).copy(0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color(0xFFFF9800))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        parent.email ?: "Unknown Email",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Hiển thị Role
                        Surface(
                            color = if (parent.role == "ADMIN") Color.Red.copy(alpha = 0.1f) else Color(
                                0xFF4CAF50
                            ).copy(0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = parent.role ?: "USER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (parent.role == "ADMIN") Color.Red else Color(0xFF4CAF50),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Ngày tạo: ${parent.createdAt?.take(10) ?: "N/A"}",
                            fontSize = 12.sp,
                            color = Color.Gray
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
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
            }

            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                Text(
                    "Danh sách trẻ em:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F)
                )

                if (isChildLoading) {
                    CircularProgressIndicator(modifier = Modifier
                        .size(20.dp)
                        .padding(top = 8.dp))
                } else if (childrenList.isEmpty()) {
                    Text(
                        "Phụ huynh này chưa tạo hồ sơ bé nào.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    childrenList.forEach { child ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChildClick(child.id!!) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ChildCare,
                                null,
                                tint = Color(0xFF9C27B0),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${child.name} (${child.age} tuổi)", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}