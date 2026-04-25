package com.example.numease.presentation.parent.selection


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
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
fun ChildSelectionStatsScreen(
    viewModel: ChildSelectionStatsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onChildSelected: (String) -> Unit
) {
    val children by viewModel.children.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn trẻ xem thống kê", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Vui lòng chọn một hồ sơ để xem báo cáo chi tiết về tiến độ học tập.",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(children) { child ->
                    ChildSelectionCard(
                        child = child,
                        onClick = { child.id?.let { onChildSelected(it) } }
                    )
                }
            }
        }
    }
}

@Composable
fun ChildSelectionCard(
    child: ChildProfile,
    onClick: () -> Unit
) {
    val avatar = if (child.gender.uppercase() == "MALE") "👦" else "👧"
    val accentColor = if (child.gender.uppercase() == "MALE") Color(0xFF2196F3) else Color(0xFFE91E63)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar tròn
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(accentColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(avatar, fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = child.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3142)
                )
                Text(
                    text = "${child.age} tuổi • Màn chơi hiện tại: ${child.currentLevel}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Mũi tên chỉ dẫn
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}