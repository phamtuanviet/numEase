package com.example.numease.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.manager.ChildSessionManager
import com.example.numease.presentation.viewmodel.AuthState
import com.example.numease.presentation.viewmodel.AuthViewModel

@Composable
fun ProfileSelectionScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    childSessionManager: ChildSessionManager = hiltViewModel(),
    onNavigateToParentMain: () -> Unit, // Callback khi bấm vào thẻ Phụ Huynh
    onNavigateToStudentMain: () -> Unit // Callback khi bấm vào thẻ của Bé
) {
    val authState by authViewModel.authState.collectAsState()
    val children = (authState as? AuthState.Authenticated)?.childProfiles ?: emptyList()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ai đang dùng máy thế nhỉ?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        // Grid hiển thị các Profile
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. ITEM CỐ ĐỊNH: THẺ PHỤ HUYNH (Luôn ở đầu tiên)
            item {
                Card(
                    onClick = {
                        // Khi Phụ huynh dùng máy -> Xóa Két sắt của bé cho an toàn
                        childSessionManager.clearSession()
                        onNavigateToParentMain()
                    },
                    modifier = Modifier.aspectRatio(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon Phụ huynh (Bạn có thể dùng R.drawable...)
                        Text(text = "👨‍👩‍👧", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Phụ Huynh", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Quản lý", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 2. DANH SÁCH CÁC THẺ CỦA BÉ
            items(children) { child ->
                Card(
                    onClick = {
                        // Khi chọn Bé -> Lưu vào Két sắt và vào Màn hình Học
                        childSessionManager.setActiveChild(child)
                        onNavigateToStudentMain()
                    },
                    modifier = Modifier.aspectRatio(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = child.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Học toán", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}