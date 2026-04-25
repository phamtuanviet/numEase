package com.example.numease.navigation.graphs


import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.numease.navigation.routes.AddEditChildRoute
import com.example.numease.navigation.routes.ChartViewerRoute
import com.example.numease.navigation.routes.ChildSelectionStatsRoute
import com.example.numease.navigation.routes.ChildStatsRoute
import com.example.numease.navigation.routes.DetailedStatsMenuRoute
import com.example.numease.navigation.routes.ManageChildrenRoute
import com.example.numease.navigation.routes.ParentGraph
import com.example.numease.navigation.routes.ParentHomeRoute
import com.example.numease.navigation.routes.ProfileSelectionRoute
import com.example.numease.presentation.parent.chart.DetailedBarChartScreen
import com.example.numease.presentation.parent.chart.DetailedLineChartScreen
import com.example.numease.presentation.parent.chart.DetailedPieChartScreen
import com.example.numease.presentation.parent.chart.DetailedTextStatsScreen
import com.example.numease.presentation.parent.detail.DetailedStatsMenuScreen
import com.example.numease.presentation.parent.home.ParentHomeScreen
import com.example.numease.presentation.parent.manage.ManageChildrenScreen
import com.example.numease.presentation.parent.selection.ChildSelectionStatsScreen
import com.example.numease.presentation.parent.stats_overview.ChildStatsScreen
import com.example.numease.presentation.viewmodel.AuthViewModel

fun NavGraphBuilder.parentGraph(navController: NavController, authViewModel: AuthViewModel) {

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
                // ĐÃ CẬP NHẬT 2 LUỒNG CHO THỐNG KÊ
                onNavigateToChildSelection = {
                    navController.navigate(ChildSelectionStatsRoute)
                },
                onNavigateToDirectStats = { childId ->
                    navController.navigate(ChildStatsRoute(childId))
                },
                onNavigateToStudentWorkspace = {
                    // QUAN TRỌNG: Điều hướng về Màn hình Chọn Profile của học sinh
                    navController.navigate(ProfileSelectionRoute) {

                        // BẢO MẬT: Xóa sạch toàn bộ Không gian Phụ huynh (ParentGraph) khỏi bộ nhớ tạm.
                        // Điều này đảm bảo khi bé đang ở màn chọn Profile mà bấm nút "Back" trên điện thoại,
                        // App sẽ thoát ra ngoài chứ không cho phép bé lùi lén vào lại màn hình Thống kê của bố mẹ.
                        popUpTo(ParentGraph) {
                            inclusive = true
                        }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                }
            )
        }

        composable<ChildSelectionStatsRoute> {
            ChildSelectionStatsScreen(
                onBack = { navController.popBackStack() },
                onChildSelected = { childId ->
                    // Sau khi chọn trẻ xong sẽ đi tới màn chi tiết thống kê
                    navController.navigate(ChildStatsRoute(childId))
                }
            )
        }

        composable<ManageChildrenRoute> {
            ManageChildrenScreen(
                onBack = { navController.popBackStack() },
                onNavigateToStats = { childId ->
                    navController.navigate(ChildStatsRoute(childId))
                }
                )
        }

//
//
//        // ==========================================
//        // 4. MÀN HÌNH THỐNG KÊ CHI TIẾT
//        // ==========================================
        composable<ChildStatsRoute> { backStackEntry ->
            // Lấy ID của bé để truy vấn DB các bài tập, thời gian học, điểm số...
            val args = backStackEntry.toRoute<ChildStatsRoute>()

            ChildStatsScreen(
                childId = args.childId,
                onBack = { navController.popBackStack() },
                onNavigateToDetailedStats = { cId, catId ->
                    navController.navigate(DetailedStatsMenuRoute(cId, catId))
                }
            )
        }

        composable<DetailedStatsMenuRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<DetailedStatsMenuRoute>()
            DetailedStatsMenuScreen(
                childId = args.childId,
                categoryId = args.categoryId,
                onBack = { navController.popBackStack() },
                onSelectChartType = { chartType ->
                    // ĐÃ THÊM: Chuyển hướng sang màn hình xem biểu đồ chi tiết
                    navController.navigate(
                        ChartViewerRoute(
                            args.childId,
                            args.categoryId,
                            chartType
                        )
                    )
                }
            )
        }

        composable<ChartViewerRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ChartViewerRoute>()

            // Tạm thời chúng ta rẽ nhánh để gọi màn hình Biểu đồ Đường
            if (args.chartType == "LINE") {
                DetailedLineChartScreen(
                    childId = args.childId,
                    categoryId = args.categoryId,
                    onBack = { navController.popBackStack() }
                )
            } else if (args.chartType == "BAR") {
                // ĐÃ THÊM: Gọi màn hình Biểu đồ cột
                DetailedBarChartScreen(
                    childId = args.childId,
                    categoryId = args.categoryId,
                    onBack = { navController.popBackStack() }
                )
            } else if (args.chartType == "PIE") {
                // ĐÃ THÊM: Gọi màn hình Biểu đồ Tròn
                DetailedPieChartScreen(
                    childId = args.childId,
                    categoryId = args.categoryId,
                    onBack = { navController.popBackStack() }
                )
            } else if (args.chartType == "TEXT") {
                // ĐÃ THÊM: Gọi màn hình Dạng Chữ (Danh sách)
                DetailedTextStatsScreen(
                    childId = args.childId,
                    categoryId = args.categoryId,
                    onBack = { navController.popBackStack() }
                )
            }
            // Các chart khác (BAR, PIE, TEXT) sẽ được add vào các lệnh else if sau
        }
    }
}