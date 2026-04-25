package com.example.numease.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChildProfile(
    val id: String? = null, // Có thể null khi tạo mới
    @SerialName("account_id") val accountId: String,
    val name: String,
    val age: Int,

    @SerialName("gender")
    val gender: String,

    @SerialName("current_level")
    val currentLevel: Int = 1,
    @SerialName("created_at")
    val createdAt: String? = null
)