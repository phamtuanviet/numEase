package com.example.numease.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LevelRecord(
    val id: String? = null,
    @SerialName("child_profile_id") val childProfileId: String,
    @SerialName("category_id") val categoryId: Int,
    val level: Int,
    val stars: Int,
    @SerialName("completed_at") val completedAt: String? = null
)