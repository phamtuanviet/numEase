package com.example.numease.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudySession(
    val id: String? = null,
    @SerialName("child_profile_id")
    val childProfileId: String,
    @SerialName("category_id")
    val categoryId: Int,
    @SerialName("total_questions")
    val totalQuestions: Int,
    @SerialName("correct_answers")
    val correctAnswers: Int,
    val accuracy: Double, // VD: 85.5 (%)
    @SerialName("duration_seconds")
    val durationSeconds: Int,
    @SerialName("created_at")
    val createdAt: String? = null,
    val level  : Int? = null,
)