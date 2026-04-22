package com.example.numease.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Instruction(
    val text: String,
    @SerialName("audio_url") val audioUrl: String? = null
)

@Serializable
sealed class ExerciseContent

// 1. Dạng bài: Đếm số
@Serializable
@SerialName("counting") // Khớp với trường "type": "counting" trong JSON
data class CountingContent(
    val instruction: Instruction,
    @SerialName("object_type") val objectType: String,
    val count: Int,
    val options: List<Int>,
    @SerialName("correct_answer") val correctAnswer: Int
) : ExerciseContent()

// 2. Dạng bài: Kéo thả
@Serializable
@SerialName("drag_drop")
data class DragDropContent(
    val instruction: Instruction,
    @SerialName("context_image") val contextImage: String? = null,
    val draggables: List<DraggableItem>,
    @SerialName("drop_zones") val dropZones: List<DropZone>,
    @SerialName("correct_mapping") val correctMapping: Map<String, String> // id draggable -> id zone
) : ExerciseContent()

// 3. Dạng bài: So sánh (lấy từ tài liệu của bạn)
@Serializable
@SerialName("comparing")
data class ComparingContent(
    val instruction: Instruction,
    @SerialName("left_value") val leftValue: Int,
    @SerialName("right_value") val rightValue: Int,
    val options: List<String>, // Ví dụ: [">", "<", "="]
    @SerialName("correct_answer") val correctAnswer: String
) : ExerciseContent()

// Các Data class phụ trợ cho Kéo thả
@Serializable
data class DraggableItem(val id: String, val value: Int, val imageUrl: String? = null)

@Serializable
data class DropZone(val id: String, @SerialName("expected_value") val expectedValue: Int)