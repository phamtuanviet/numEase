package com.example.numease.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class Instruction(
    val text: String,
    @SerialName("audio_url") val audioUrl: String? = null
)


@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
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
// 1. Định nghĩa vật thể có thể kéo (Các con số)
@Serializable
data class DraggableItem(
    val id: String,        // VD: "drag_2"
    val label: String,     // VD: "2"
    val type: String       // VD: "number"
)

// 2. Định nghĩa Giỏ thả (Chứa emoji đồ vật)
@Serializable
data class DropZone(
    val id: String,        // VD: "zone_A"
    val label: String,     // VD: "🥕🥕" (Thay thế cho expectedValue cũ)
    val type: String       // VD: "basket"
)

// 3. Nội dung toàn bộ màn chơi Kéo thả
@Serializable
@SerialName("drag_drop")
data class DragDropContent(
    val instruction: Instruction,
    @SerialName("context_image") val contextImage: String? = null,
    val draggables: List<DraggableItem>,
    @SerialName("drop_zones") val dropZones: List<DropZone>,
    @SerialName("correct_mapping") val correctMapping: Map<String, String> // Map: ID Kéo -> ID Giỏ
) : ExerciseContent()


@Serializable
@SerialName("calculation")
data class CalculationContent(
    val instruction: Instruction,
    @SerialName("left_value") val leftValue: Int,
    @SerialName("right_value") val rightValue: Int,
    val operator: String, // Nhận dấu "+" hoặc "-"
    val options: List<Int>,
    @SerialName("correct_answer") val correctAnswer: Int
) : ExerciseContent()