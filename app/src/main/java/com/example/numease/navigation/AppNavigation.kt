package com.example.numease.navigation


import SplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.numease.navigation.graphs.*
import com.example.numease.navigation.routes.AdminGraph
import com.example.numease.navigation.routes.AuthGraph
import com.example.numease.navigation.routes.ChildSelectionRoute
import com.example.numease.navigation.routes.OnboardingGraph
import com.example.numease.navigation.routes.ParentGraph
import com.example.numease.navigation.routes.ParentSetupRoute
import com.example.numease.navigation.routes.ProfileSelectionRoute
import com.example.numease.navigation.routes.RoleRouterRoute
import com.example.numease.navigation.routes.SplashRoute
import com.example.numease.navigation.routes.StudentGraph
import com.example.numease.navigation.routes.StudentSetupRoute
import com.example.numease.presentation.ProfileSelectionScreen
import com.example.numease.presentation.RoleRouterScreen
import com.example.numease.presentation.UserPreferencesViewModel
import com.example.numease.presentation.viewmodel.AuthState
import com.example.numease.presentation.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    userPrefsViewModel: UserPreferencesViewModel
) {
    val navController = rememberNavController()

    val authState by authViewModel.authState.collectAsState()

    // 2. KÍCH HOẠT CHUYỂN MÀN KHI LOGOUT
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            // Bay về màn Đăng nhập và XÓA SẠCH toàn bộ lịch sử (Backstack)
            navController.navigate(AuthGraph) {
                // popUpTo(0) có nghĩa là dọn dẹp không chừa lại bất kỳ màn hình cũ nào
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = SplashRoute
    ) {
        // 1. Màn hình Splash
        composable<SplashRoute> {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToAuth = {
                    navController.navigate(AuthGraph) { popUpTo(SplashRoute) { inclusive = true } }
                },
                onNavigateToRouter = {
                    navController.navigate(RoleRouterRoute) {
                        popUpTo(SplashRoute) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<RoleRouterRoute> {
            RoleRouterScreen(
                authViewModel = authViewModel,

                onNavigateToOnboarding = {
                    // Chưa có Role hoặc chưa tạo Profile -> Bắt buộc đi qua Onboarding
                    navController.navigate(OnboardingGraph) {
                        popUpTo(RoleRouterRoute) { inclusive = true }
                    }
                },
                onNavigateToParentMain = {
                    navController.navigate(ParentGraph) {
                        popUpTo(RoleRouterRoute) { inclusive = true }
                    }
                },
                onNavigateToStudentMain = {
                    navController.navigate(StudentGraph) {
                        popUpTo(RoleRouterRoute) { inclusive = true }
                    }
                },
                onNavigateToAdminMain = {
                    navController.navigate(AdminGraph) {
                        popUpTo(RoleRouterRoute) { inclusive = true }
                    }
                },
                onNavigateToParentSetup = {
                    navController.navigate(ParentSetupRoute) {
                        popUpTo(RoleRouterRoute) { inclusive = true }
                    }
                },
                onNavigateToStudentSetup = {
                    navController.navigate(StudentSetupRoute) {
                        popUpTo(RoleRouterRoute) { inclusive = true }
                    }
                },
                onNavigateToProfileSelection = {
                    navController.navigate(ProfileSelectionRoute) {
                        popUpTo(RoleRouterRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<ProfileSelectionRoute> {
            ProfileSelectionScreen(
                onNavigateToParentMain = {
                    navController.navigate(ParentGraph) {
                        popUpTo(ProfileSelectionRoute) { inclusive = true }
                    }
                },
                onNavigateToStudentMain = {
                    navController.navigate(StudentGraph) {
                        popUpTo(ProfileSelectionRoute) { inclusive = true }
                    }
                }
            )
        }

        // 2. Gọi các nhánh (Graphs) từ các file bên ngoài vào
        authGraph(navController)
        parentGraph(navController,authViewModel = authViewModel)
        studentGraph(
            navController = navController,
            onNavigateToParentWorkspace = {
                // Cách 1: Chuyển thẳng sang khu vực của Phụ Huynh
                navController.navigate(ParentGraph) {
                    // CỰC KỲ QUAN TRỌNG: Xóa toàn bộ lịch sử của không gian Học sinh
                    // Để phụ huynh bấm nút Back trên điện thoại không bị quay lại màn hình của con
                    popUpTo(StudentGraph) {
                        inclusive = true
                    }
                }


            },
            authViewModel = authViewModel
        )
        adminGraph(navController)
        onboardingGraph(navController)


    }
}