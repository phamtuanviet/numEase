package com.example.numease.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val isDarkMode: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val fontSizeMultiplier: Float = 1.0f,
    val currentChildId: String? = null,
    val currentViewMode: String = "PARENT"
)