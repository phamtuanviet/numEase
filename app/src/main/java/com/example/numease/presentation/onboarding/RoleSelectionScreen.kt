package com.example.numease.presentation.onboarding

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numease.presentation.component.RoleCard

@Composable
fun RoleSelectionScreen(
    viewModel: OnboardingViewModel,
    onRoleSelected: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Lắng nghe trạng thái để chuyển trang hoặc báo lỗi
    LaunchedEffect(uiState) {
        when (uiState) {
            is OnboardingState.RoleSaved -> {
                val savedRole = (uiState as OnboardingState.RoleSaved).role
                onRoleSelected(savedRole)
                viewModel.resetState()
            }
            is OnboardingState.Error -> {
                val errorMessage = (uiState as OnboardingState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // Giao diện chính
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Chào mừng đến với NumEase!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bạn là ai nhỉ?",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Thẻ chọn Học sinh (Màu sắc tươi sáng, vui nhộn)
            RoleCard(
                title = "Mình là Học Sinh",
                subtitle = "Vào học Toán và chơi game thôi!",
                icon = Icons.Rounded.Face,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { viewModel.saveRole("STUDENT") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Thẻ chọn Phụ huynh (Thiết kế viền, lịch sự, đáng tin cậy)
            RoleCard(
                title = "Tôi là Phụ Huynh",
                subtitle = "Thiết lập và theo dõi tiến độ của bé.",
                icon = Icons.Rounded.Person,
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                onClick = { viewModel.saveRole("PARENT") }
            )
        }

        // Lớp phủ Loading khi đang gọi API Supabase
        if (uiState is OnboardingState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) {}, // Chặn click xuyên thấu
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}