package com.example.numease.navigation.routes

import kotlinx.serialization.Serializable

@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object ForgotPasswordRoute
@Serializable data class ResetPasswordOtpRoute(val email: String)
@Serializable object CreateNewPasswordRoute // Dành cho Phần 3

@Serializable data class VerifyEmailRoute(val email: String)

@Serializable object ChildSelectionRoute