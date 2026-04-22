package com.example.numease.presentation.auth.verify

data class VerifyEmailState(
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)