package com.example.numease.navigation.routes


import kotlinx.serialization.Serializable

// Route gốc bao bọc toàn bộ Không gian Học sinh

// 1. Màn hình chào mừng (Dashboard thực thụ)
@Serializable
object StudentHomeRoute

// 2. Màn hình Bản đồ (Candy Crush style)
@Serializable
object MapRoute

// 3. Màn hình Kho báu / Thành tích
@Serializable
object TrophyRoute

// 4. Màn hình chuẩn bị vào màn

@Serializable
data class SessionIntroRoute(
    val categoryId: Int,
    val level: Int,
    val levelId: Int // MỚI: Thêm ID cửa
)

@Serializable
data class ExerciseRoute(
    val categoryId: Int,
    val level: Int,
    val levelId: Int // MỚI: Thêm ID cửa
)

@Serializable
data class RewardRoute(
    val earnedStars: Int,
    val levelId: Int // MỚI: Thêm ID cửa để tính bài tiếp theo
)