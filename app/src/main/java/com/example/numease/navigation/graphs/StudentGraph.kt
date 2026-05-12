package com.example.numease.navigation.graphs

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.example.numease.navigation.routes.ExerciseRoute
import com.example.numease.navigation.routes.MapRoute
import com.example.numease.navigation.routes.RewardRoute
import com.example.numease.navigation.routes.SessionIntroRoute
import com.example.numease.navigation.routes.StudentGraph
import com.example.numease.navigation.routes.StudentHomeRoute
import com.example.numease.navigation.routes.TrophyRoute
import com.example.numease.presentation.student.exercise.ExerciseScreen
import com.example.numease.presentation.student.home.StudentHomeScreen
import com.example.numease.presentation.student.intro.SessionIntroScreen
import com.example.numease.presentation.student.map.MapScreen
import com.example.numease.presentation.student.map.MapViewModel
import com.example.numease.presentation.student.reward.RewardScreen
import com.example.numease.presentation.viewmodel.AuthViewModel

fun NavGraphBuilder.studentGraph(
    navController: NavController,
    onNavigateToParentWorkspace: () -> Unit,
    authViewModel: AuthViewModel,
) {
    navigation<StudentGraph>(startDestination = StudentHomeRoute) {


        // --- 1. MÀN HÌNH DASHBOARD (TRANG CHỦ) ---
        composable<StudentHomeRoute> {
            val userProfile by authViewModel.userProfile.collectAsState()
            StudentHomeScreen(

                // Kiểm tra xem role của tài khoản có phải là PARENT không
                isParentAccount = userProfile?.role == "PARENT",
                // Gọi hàm logout đã viết sẵn trong AuthViewModel
                onLogoutClicked = {
                    authViewModel.logout()
                },

                onPlayClicked = {
                    // Chuyển sang Bản đồ
                    navController.navigate(MapRoute)
                },
                onParentGatePassed = {
                    // Chỉ khi phụ huynh giải đúng toán/nhập mã PIN mới gọi hàm này
                    onNavigateToParentWorkspace()
                }
            )
        }

        // --- 2. MÀN HÌNH BẢN ĐỒ ---
        composable<MapRoute> {
            val mapViewModel: MapViewModel = hiltViewModel()
            val totalStars by mapViewModel.totalStars.collectAsState()
            val mapNodes by mapViewModel.mapNodes.collectAsState()

            MapScreen(
                totalStars = totalStars,
                nodes = mapNodes,
                onBack = {
                    navController.navigateUp()
                },
                onLevelSelected = { clickedLevelId ->
                    val nodeConfig = mapViewModel.getNodeDefinition(clickedLevelId)

                    // Chuyển sang màn hình Intro, truyền kèm levelId
                    navController.navigate(
                        SessionIntroRoute(
                            categoryId = nodeConfig.categoryId,
                            level = nodeConfig.levelInDb,
                            levelId = nodeConfig.levelId // TRUYỀN VÀO ĐÂY
                        )
                    )
                }
            )
        }

        composable<SessionIntroRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<SessionIntroRoute>()

            SessionIntroScreen(
                categoryId = args.categoryId,
                level = args.level,
                onBack = { navController.navigateUp() },
                onStart = {
                    // Vào làm bài, luồn levelId đi tiếp và xóa Intro khỏi lịch sử
                    navController.navigate(
                        ExerciseRoute(
                            categoryId = args.categoryId,
                            level = args.level,
                            levelId = args.levelId // TRUYỀN TIẾP VÀO ĐÂY
                        )
                    ) {
                        popUpTo<SessionIntroRoute> { inclusive = true }
                    }
                }
            )
        }
//
//        // --- 4. MÀN HÌNH LÀM BÀI ---
        composable<ExerciseRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ExerciseRoute>()

            ExerciseScreen(
                categoryId = args.categoryId,
                level = args.level,
                levelId = args.levelId, // TRUYỀN XUỐNG UI CỦA EXERCISE
                onPauseAndExit = {
                    navController.navigate(MapRoute) {
                        popUpTo<MapRoute> { inclusive = true }
                    }
                },
                onSessionComplete = { stars, currentLevelId ->
                    // Xong bài -> Bắn sao và ID cửa hiện tại sang màn Nhận Thưởng
                    navController.navigate(RewardRoute(stars, currentLevelId)) {
                        popUpTo<ExerciseRoute> { inclusive = true }
                    }
                }
            )
        }
//
        // --- 5. MÀN HÌNH NHẬN THƯỞNG ---
        composable<RewardRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<RewardRoute>()

            // Khởi tạo MapViewModel để dùng thuật toán tính bài tiếp theo
            val mapViewModel: MapViewModel = hiltViewModel()

            RewardScreen(
                earnedStars = args.earnedStars,
                onBackToMap = {
                    navController.navigate(MapRoute) {
                        popUpTo<MapRoute> { inclusive = true }
                    }
                },
                onNextLevel = {
                    // 1. Tính toán ID cửa tiếp theo
                    val nextLevelId = args.levelId + 1

                    // 2. Dùng MapViewModel lấy cấu hình cửa mới
                    val nextNodeDef = mapViewModel.getNodeDefinition(nextLevelId)

                    // 3. Chuyển sang màn Intro của cửa mới, xóa RewardScreen cũ đi
                    navController.navigate(
                        SessionIntroRoute(
                            categoryId = nextNodeDef.categoryId,
                            level = nextNodeDef.levelInDb,
                            levelId = nextNodeDef.levelId
                        )
                    ) {
                        popUpTo<RewardRoute> { inclusive = true }
                    }
                }
            )
        }
//
//        // --- 6. MÀN HÌNH KHO BÁU ---
//        composable<TrophyRoute> {
//            TrophyScreen(
//                onBack = { navController.navigateUp() }
//            )
//        }
    }
}