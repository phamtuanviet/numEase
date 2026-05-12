package com.example.numease.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.manager.ChildSessionManager
import com.example.numease.presentation.viewmodel.AuthState
import com.example.numease.presentation.viewmodel.AuthViewModel


@Composable
fun RoleRouterScreen(
    authViewModel: AuthViewModel,
    onNavigateToOnboarding: () -> Unit,       // Vào màn chọn Role
    onNavigateToParentSetup: () -> Unit,      // Bổ sung: Vào form tạo hồ sơ Phụ huynh
    onNavigateToStudentSetup: () -> Unit,     // Bổ sung: Vào form tạo hồ sơ Học sinh
    onNavigateToParentMain: () -> Unit,
    onNavigateToProfileSelection : () -> Unit,
    onNavigateToStudentMain: () -> Unit,
    onNavigateToAdminMain: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(key1 = authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                val profile = state.profile
                val children = state.childProfiles

                if (profile == null) {
                    onNavigateToOnboarding()
                } else {
                    when (profile.role) {
                        "ADMIN" -> onNavigateToAdminMain()

                        "STUDENT" -> {
                            // HỌC SINH: Rất đơn giản, không cần chọn gì cả
                            if (children.isEmpty()) {
                                onNavigateToStudentSetup()
                            } else {
                                // Auto lưu bé đầu tiên vào Két sắt và vào thẳng app
                                authViewModel.childSessionManager.setActiveChild(children.first())
                                onNavigateToStudentMain()
                            }
                        }

                        "PARENT" -> {
                            // PHỤ HUYNH: Cần màn hình Profile Selection nếu đã có con
                            if (children.isEmpty()) {
                                onNavigateToParentSetup()
                            } else {
                                onNavigateToProfileSelection()
                            }
                        }

                        else -> onNavigateToOnboarding()
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                // Splash/Login tự lo
            }
            is AuthState.Loading -> {
                // UI hiện vòng xoay
            }

            is AuthState.Banned -> {
                // Splash/Login tự lo
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Đang tải dữ liệu...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}