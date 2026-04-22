package com.example.numease.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.numease.navigation.routes.AuthGraph
import com.example.numease.navigation.routes.CreateNewPasswordRoute
import com.example.numease.navigation.routes.ForgotPasswordRoute
import com.example.numease.navigation.routes.LoginRoute
import com.example.numease.navigation.routes.RegisterRoute
import com.example.numease.navigation.routes.ResetPasswordOtpRoute
import com.example.numease.navigation.routes.RoleRouterRoute
import com.example.numease.navigation.routes.VerifyEmailRoute
import com.example.numease.presentation.auth.forgotpassword.CreateNewPasswordScreen
import com.example.numease.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.example.numease.presentation.auth.forgotpassword.ResetPasswordOtpScreen
import com.example.numease.presentation.auth.login.LoginScreen
import com.example.numease.presentation.auth.register.RegisterScreen
import com.example.numease.presentation.auth.verify.VerifyEmailScreen

fun NavGraphBuilder.authGraph(navController: NavController) {
    navigation<AuthGraph>(startDestination = LoginRoute) {

        composable<LoginRoute> {
            LoginScreen(

                onNavigateToRegister = {
                    navController.navigate(RegisterRoute)
                },
                onNavigateToForgotPassword = {
                     navController.navigate(ForgotPasswordRoute) // Điều hướng đến màn hình quên mật khẩu
                },
                onNavigateToRouter = {
                    navController.navigate(RoleRouterRoute) {
                        popUpTo<AuthGraph> { inclusive = true }
                    }
                }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onRegisterSuccess = { email ->
                    navController.navigate(VerifyEmailRoute(email = email))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // MÀN HÌNH MỚI
        composable<VerifyEmailRoute> { backStackEntry ->
            // Màn hình này không cần lấy arguments ở đây vì Hilt ViewModel (SavedStateHandle) đã tự lấy giúp rồi
            VerifyEmailScreen(
                onVerifySuccess = {
                    // Xác nhận xong là có Session -> Bay thẳng vào nhà chính
                    navController.navigate(RoleRouterRoute) {
                        popUpTo<AuthGraph> { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                onEmailSentSuccess = { email ->
                    // Gửi email xong -> Nhảy sang màn nhập mã OTP, truyền kèm email
                    navController.navigate(ResetPasswordOtpRoute(email = email))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ResetPasswordOtpRoute> { backStackEntry ->
            ResetPasswordOtpScreen(
                onOtpVerifiedSuccess = {
                    // Nhập mã đúng -> Nhảy sang màn tạo mật khẩu mới
                    navController.navigate(CreateNewPasswordRoute) {
                        // Xóa các bước trước đó trong luồng quên mật khẩu để không back lại được
                        popUpTo<ForgotPasswordRoute> { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<CreateNewPasswordRoute> {
            CreateNewPasswordScreen(
                onPasswordResetSuccess = {
                    // Đổi xong, đẩy người dùng về lại màn hình Đăng nhập (LoginRoute)
                    navController.navigate(LoginRoute) {
                        // Xóa sạch lịch sử các màn hình quên mật khẩu đi để không bấm Back lại được
                        popUpTo<AuthGraph> { inclusive = true }
                    }
                }
            )
        }
    }
}