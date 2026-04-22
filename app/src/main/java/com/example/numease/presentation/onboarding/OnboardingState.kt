package com.example.numease.presentation.onboarding

sealed class OnboardingState {
    object Idle : OnboardingState()
    object Loading : OnboardingState()
    data class RoleSaved(val role: String) : OnboardingState()
    object ProfileCreated : OnboardingState()
    data class Error(val message: String) : OnboardingState()
}