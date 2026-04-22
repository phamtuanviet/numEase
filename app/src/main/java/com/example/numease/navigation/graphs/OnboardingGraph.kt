package com.example.numease.navigation.graphs

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.numease.navigation.routes.OnboardingGraph
import com.example.numease.navigation.routes.ParentGraph
import com.example.numease.navigation.routes.ParentSetupRoute
import com.example.numease.navigation.routes.RoleSelectionRoute
import com.example.numease.navigation.routes.StudentGraph
import com.example.numease.navigation.routes.StudentSetupRoute
import com.example.numease.presentation.onboarding.OnboardingViewModel
import com.example.numease.presentation.onboarding.ParentSetupScreen
import com.example.numease.presentation.onboarding.RoleSelectionScreen
import com.example.numease.presentation.onboarding.StudentSetupScreen

fun NavGraphBuilder.onboardingGraph(navController: NavController) {
    navigation<OnboardingGraph>(startDestination = RoleSelectionRoute) {

        // 1. Màn hình chọn Vai trò
        composable<RoleSelectionRoute> {
            val viewModel: OnboardingViewModel = hiltViewModel()

            RoleSelectionScreen(
                viewModel = viewModel,
                onRoleSelected = { role ->
                    if (role == "STUDENT") {
                        navController.navigate(StudentSetupRoute)
                    } else {
                        navController.navigate(ParentSetupRoute)
                    }
                }
            )
        }

        // 2. Màn hình Setup cho bé tự đăng ký (Fun Onboarding)
        composable<StudentSetupRoute> {
            val viewModel: OnboardingViewModel = hiltViewModel()

            StudentSetupScreen(
                viewModel = viewModel,
                onSetupComplete = {
                    // Xong Onboarding -> Chuyển thẳng vào màn hình học của bé
                    navController.navigate(StudentGraph) {
                        popUpTo(OnboardingGraph) { inclusive = true }
                    }
                }
            )
        }

        // 3. Màn hình Setup cho Phụ huynh tạo hồ sơ (Professional Form)
        composable<ParentSetupRoute> {
            val viewModel: OnboardingViewModel = hiltViewModel()

            ParentSetupScreen(
                viewModel = viewModel,
                onSetupComplete = {
                    // Xong Onboarding -> Chuyển vào Dashboard của phụ huynh
                    navController.navigate(ParentGraph) {
                        popUpTo(OnboardingGraph) { inclusive = true }
                    }
                }
            )
        }
    }
}