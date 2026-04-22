package com.example.numease.presentation.auth.forgotpassword

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false // true khi đã gửi email thành công
)