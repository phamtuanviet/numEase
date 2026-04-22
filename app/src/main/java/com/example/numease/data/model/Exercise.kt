package com.example.numease.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ExerciseCategory(
    val id: Int? = null,
    val code: String, // VD: "COUNTING", "DRAG_DROP", "COMPARING"
    val name: String,
    val description: String? = null
)

@Serializable
data class Exercise(
    val id: String,
    @SerialName("category_id")
    val categoryId: Int,
    val level: Int,
    // Content chứa toàn bộ nội dung JSON động
    val content: ExerciseContent,
    @SerialName("created_at")
    val createdAt: String? = null
)