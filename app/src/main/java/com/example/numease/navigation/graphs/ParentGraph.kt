package com.example.numease.navigation.graphs



import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.numease.navigation.routes.AddEditChildRoute
import com.example.numease.navigation.routes.ChildStatsRoute
import com.example.numease.navigation.routes.ManageChildrenRoute
import com.example.numease.navigation.routes.ParentGraph
import com.example.numease.navigation.routes.ParentHomeRoute
import com.example.numease.presentation.parent.home.ParentHomeScreen
import com.example.numease.presentation.viewmodel.AuthViewModel

fun NavGraphBuilder.parentGraph(navController: NavController,  authViewModel: AuthViewModel,) {

    // Khai báo một Graph con dành riêng cho luồng Phụ huynh
    navigation<ParentGraph>(startDestination = ParentHomeRoute) {

        // ==========================================
        // 1. MÀN HÌNH HOME (DASHBOARD)
        // ==========================================
        composable<ParentHomeRoute> {

            // ParentHomeScreen sẽ giống như một bảng điều khiển trung tâm (Dashboard).
            // Có các thẻ (Card) tóm tắt nhanh và các nút để đi vào tính năng chi tiết.
            ParentHomeScreen(
                onNavigateToManageChildren = {
                    navController.navigate(ManageChildrenRoute)
                },
                onNavigateToStats = { childId ->
                    navController.navigate(ChildStatsRoute(childId))
                },
                // Nếu phụ huynh muốn đưa máy cho con chơi lại
                onNavigateToStudentWorkspace = {
                    navController.navigate("student_home") { // Hoặc StudentGraph tùy cách bạn đặt tên
                        popUpTo(ParentGraph) { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                }
            )
        }

        // ==========================================
        // 2. MÀN HÌNH QUẢN LÝ HỒ SƠ (THÊM / SỬA / XÓA)
        // ==========================================
//        composable<ManageChildrenRoute> {
//            ManageChildrenScreen(
//                onBack = { navController.popBackStack() },
//                onNavigateToAddChild = {
//                    // Truyền null để báo cho UI biết đây là hành động Thêm Mới
//                    navController.navigate(AddEditChildRoute(childId = null))
//                },
//                onNavigateToEditChild = { childId ->
//                    // Truyền ID để UI fetch dữ liệu cũ lên Form
//                    navController.navigate(AddEditChildRoute(childId = childId))
//                }
//            )
//        }
//
//        // ==========================================
//        // 3. MÀN HÌNH FORM (THÊM HOẶC SỬA)
//        // ==========================================
//        composable<AddEditChildRoute> { backStackEntry ->
//            // Trích xuất childId an toàn từ Bundle
//            val args = backStackEntry.toRoute<AddEditChildRoute>()
//
//            AddEditChildScreen(
//                childId = args.childId,
//                onBack = { navController.popBackStack() },
//                onSaveSuccess = {
//                    // Lưu thành công thì tự động lùi về màn hình trước
//                    navController.popBackStack()
//                }
//            )
//        }
//
//        // ==========================================
//        // 4. MÀN HÌNH THỐNG KÊ CHI TIẾT
//        // ==========================================
//        composable<ChildStatsRoute> { backStackEntry ->
//            // Lấy ID của bé để truy vấn DB các bài tập, thời gian học, điểm số...
//            val args = backStackEntry.toRoute<ChildStatsRoute>()
//
//            ChildStatsScreen(
//                childId = args.childId,
//                onBack = { navController.popBackStack() }
//            )
//        }
    }
}