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
            // 1. Khởi tạo ViewModel bằng Hilt
            val mapViewModel: MapViewModel = hiltViewModel()

            // 2. Lắng nghe State từ ViewModel
            val totalStars by mapViewModel.totalStars.collectAsState()
            val mapNodes by mapViewModel.mapNodes.collectAsState()

            // 3. Gắn State vào UI (Tách biệt hoàn toàn logic và view)
            MapScreen(
                totalStars = totalStars,
                nodes = mapNodes,
                onBack = {
                    navController.navigateUp()
                },
                onLevelSelected = { clickedLevelId ->
                    // Khi bấm vào 1 Cửa (VD Cửa số 12), hỏi ViewModel xem Cửa 12 đó
                    // thuộc Category mấy, Level mấy trong DB để gửi đi
                    val nodeConfig = mapViewModel.getNodeDefinition(clickedLevelId)

                    // Chuyển sang màn hình Intro trước khi vào bài tập
                    navController.navigate(
                        SessionIntroRoute(
                            categoryId = nodeConfig.categoryId,
                            level = nodeConfig.levelInDb
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
                    // Vào làm bài và xóa màn Intro khỏi lịch sử để trẻ không Back lại được
                    navController.navigate(ExerciseRoute(args.categoryId, args.level)) {
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
                onPauseAndExit = {
                    // Trẻ bấm nút Tạm dừng -> Thoát về Bản đồ
                    navController.navigate(MapRoute) {
                        popUpTo<MapRoute> { inclusive = true }
                    }
                },
                onSessionComplete = { stars ->
                    // Xong bài -> Nhận thưởng
                    navController.navigate(RewardRoute(stars)) {
                        popUpTo<ExerciseRoute> { inclusive = true }
                    }
                }
            )
        }
//
        // --- 5. MÀN HÌNH NHẬN THƯỞNG ---
        composable<RewardRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<RewardRoute>()

            RewardScreen(
                earnedStars = args.earnedStars,
                onBackToMap = {
                    // Khi bé bấm nút nào thì cũng điều hướng về Bản đồ.
                    // MapScreen sẽ tự động lấy dữ liệu mới từ Database và hiệu ứng MỞ CỬA sẽ xảy ra!
                    navController.navigate(MapRoute) {
                        // Xóa toàn bộ lịch sử từ MapRoute trở lên để không bị chồng chất màn hình
                        popUpTo<MapRoute> { inclusive = true }
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