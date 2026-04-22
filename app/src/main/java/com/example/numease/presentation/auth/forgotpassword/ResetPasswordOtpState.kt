package com.example.numease.presentation.auth.forgotpassword

data class ResetPasswordOtpState(
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false // true khi xác thực OTP thành công
)