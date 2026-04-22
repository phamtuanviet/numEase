package com.example.numease.presentation.onboarding

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numease.presentation.component.AgeInputStep
import com.example.numease.presentation.component.NameInputStep

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StudentSetupScreen(
    viewModel: OnboardingViewModel,
    onSetupComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Quản lý các bước nhập liệu
    var currentStep by remember { mutableStateOf(1) }

    // Dữ liệu thu thập
    var studentName by remember { mutableStateOf("") }

    // Lắng nghe trạng thái lưu Profile
    LaunchedEffect(uiState) {
        when (uiState) {
            is OnboardingState.ProfileCreated -> {
                onSetupComplete()
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Dùng AnimatedContent để tạo hiệu ứng chuyển cảnh mượt mà giữa các câu hỏi
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "onboarding_steps"
            ) { step ->
                when (step) {
                    1 -> NameInputStep(
                        name = studentName,
                        onNameChange = { studentName = it },
                        onNext = {
                            if (studentName.isNotBlank()) currentStep = 2
                        }
                    )
                    2 -> AgeInputStep(
                        studentName = studentName,
                        onAgeSelected = { selectedAge ->
                            // Vừa chọn tuổi xong là gọi API lưu luôn
                            viewModel.createChildProfile(name = studentName, age = selectedAge)
                        }
                    )
                }
            }
        }

        // Lớp phủ Loading
        if (uiState is OnboardingState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp
                )
            }
        }
    }
}
