package com.example.numease.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LevelRecordDto(val stars: Int)

// DTO lấy điểm số 15 bài gần nhất
@Serializable
data class StudySessionDto(
    val accuracy: Double, // Độ chính xác (Ví dụ: 0 đến 100%)
    @SerialName("created_at") val createdAt: String? = null
)