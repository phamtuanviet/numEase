package com.example.numease.navigation.graphs



import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.numease.navigation.routes.AddCalculationQuestionRoute
import com.example.numease.navigation.routes.AddComparingQuestionRoute
import com.example.numease.navigation.routes.AddCountingQuestionRoute
import com.example.numease.navigation.routes.AddDragDropQuestionRoute
import com.example.numease.navigation.routes.AdminGraph
import com.example.numease.navigation.routes.AdminHomeRoute
import com.example.numease.navigation.routes.ManageContentRoute
import com.example.numease.navigation.routes.ManageLevelsRoute
import com.example.numease.navigation.routes.ManageQuestionsRoute
import com.example.numease.navigation.routes.ManageUsersRoute
import com.example.numease.presentation.admin.caculation.AddCalculationQuestionScreen
import com.example.numease.presentation.admin.comparing.AddComparingQuestionScreen
import com.example.numease.presentation.admin.content.ManageContentScreen
import com.example.numease.presentation.admin.counting.AddCountingQuestionScreen
import com.example.numease.presentation.admin.drag_drop.AddDragDropQuestionScreen
import com.example.numease.presentation.admin.home.AdminHomeScreen
import com.example.numease.presentation.admin.manage_category.ManageLevelsScreen
import com.example.numease.presentation.admin.question.ManageQuestionsScreen

fun NavGraphBuilder.adminGraph(navController: NavController) {

    // Khai báo Graph con dành riêng cho luồng Admin
    navigation<AdminGraph>(startDestination = AdminHomeRoute) {

        // ==========================================
        // 1. MÀN HÌNH HOME ADMIN (DASHBOARD)
        // ==========================================
        composable<AdminHomeRoute> {
            AdminHomeScreen(
                onNavigateToManageContent = {
                    navController.navigate(ManageContentRoute)
                },
                onNavigateToManageUsers = {
                    navController.navigate(ManageUsersRoute)
                },
                onLogout = {
                    // Cũng giống như Phụ huynh, gọi AuthViewModel.logout() ở UI
                    // AppNavigation sẽ tự bắt tín hiệu và đẩy văng về màn đăng nhập
                }
            )
        }

        // ==========================================
        // 2. MÀN HÌNH QUẢN LÝ BÀI TẬP & CÂU HỎI
        // ==========================================
        composable<ManageContentRoute> {
            ManageContentScreen(
                // Khi bấm vào thẻ Tổng quan ở BottomBar
                onNavigateToHome = {
                    navController.navigate(AdminHomeRoute) {
                        popUpTo(AdminHomeRoute) { inclusive = true } // Tránh xếp chồng màn hình
                    }
                },

                // Khi bấm vào thẻ Người dùng ở BottomBar
                onNavigateToManageUsers = {
                    navController.navigate(ManageUsersRoute)
                },
                onNavigateToManageLevels = { categoryId, categoryName, categoryCode ->
                    navController.navigate(
                        ManageLevelsRoute(categoryId, categoryName, categoryCode)
                    )
                }

            )
        }

        composable<ManageLevelsRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ManageLevelsRoute>()

            ManageLevelsScreen(
                categoryId = args.categoryId,
                categoryName = args.categoryName,
                categoryCode = args.categoryCode,
                onBack = { navController.popBackStack() },
                onNavigateToLevelDetail = { level ->
                    navController.navigate(
                        ManageQuestionsRoute(
                            args.categoryId,
                            args.categoryCode,
                            level
                        )
                    )
                },
                onNavigateToAddLevel = { newLevel ->
                    if (args.categoryCode == "COUNTING") {
                        navController.navigate(
                            AddCountingQuestionRoute(
                                args.categoryId,
                                args.categoryCode,
                                newLevel
                            )
                        )
                    } else if (args.categoryCode == "COMPARING") {
                        // ĐÃ THÊM: Điều hướng sang Form So sánh
                        navController.navigate(
                            AddComparingQuestionRoute(
                                args.categoryId,
                                args.categoryCode,
                                newLevel
                            )
                        )
                    } else if (args.categoryCode == "DRAG_DROP") {
                        // ĐÃ THÊM: Điều hướng sang Kéo Thả
                        navController.navigate(AddDragDropQuestionRoute(args.categoryId, args.categoryCode, newLevel))
                    } else if (args.categoryCode == "ADDITION" || args.categoryCode == "SUBTRACTION") {
                        // ĐÃ THÊM: Điều hướng sang Kéo Thả
                        navController.navigate(
                            AddCalculationQuestionRoute(
                                args.categoryId,
                                args.categoryCode,
                                newLevel
                            )
                        )
                    } else {
                        // TODO: Các form của So sánh, Kéo thả... sẽ thêm nhánh else if ở đây sau
                    }
                }
            )
        }

        composable<ManageQuestionsRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ManageQuestionsRoute>()
            ManageQuestionsScreen(
                categoryId = args.categoryId,
                categoryCode = args.categoryCode,
                level = args.level,
                onBack = { navController.popBackStack() },
                onNavigateToAddQuestion = {
                    if (args.categoryCode == "COUNTING") {
                        navController.navigate(AddCountingQuestionRoute(args.categoryId, args.categoryCode, args.level))
                    } else if (args.categoryCode == "COMPARING") {
                        // ĐÃ THÊM: Điều hướng sang Form So sánh
                        navController.navigate(AddComparingQuestionRoute(args.categoryId, args.categoryCode, args.level))
                    } else if (args.categoryCode == "DRAG_DROP") {
                        // ĐÃ THÊM: Điều hướng sang Kéo Thả
                        navController.navigate(
                            AddDragDropQuestionRoute(
                                args.categoryId,
                                args.categoryCode,
                                args.level
                            )
                        )
                    } else if (args.categoryCode == "ADDITION" || args.categoryCode == "SUBTRACTION") {
                        // ĐÃ THÊM: Điều hướng sang Kéo Thả
                        navController.navigate(
                            AddCalculationQuestionRoute(
                                args.categoryId,
                                args.categoryCode,
                                args.level
                            )
                        )
                    }
                }
            )
        }

        composable<AddCountingQuestionRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<AddCountingQuestionRoute>()
            AddCountingQuestionScreen(
                categoryId = args.categoryId,
                level = args.level,
                onBack = { navController.popBackStack() },
                onSavedSuccess = { navController.popBackStack() } // Lưu xong thì lùi lại
            )
        }

        composable<AddComparingQuestionRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<AddComparingQuestionRoute>()
            AddComparingQuestionScreen(
                categoryId = args.categoryId,
                level = args.level,
                onBack = { navController.popBackStack() },
                onSavedSuccess = { navController.popBackStack() }
            )
        }

        composable<AddDragDropQuestionRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<AddDragDropQuestionRoute>()
            AddDragDropQuestionScreen(
                categoryId = args.categoryId,
                level = args.level,
                onBack = { navController.popBackStack() },
                onSavedSuccess = { navController.popBackStack() }
            )
        }

        composable<AddCalculationQuestionRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<AddCalculationQuestionRoute>()
            AddCalculationQuestionScreen(
                categoryId = args.categoryId,
                categoryCode = args.categoryCode, // Truyền code vào để xác định Dấu
                level = args.level,
                onBack = { navController.popBackStack() },
                onSavedSuccess = { navController.popBackStack() }
            )
        }
    }
}