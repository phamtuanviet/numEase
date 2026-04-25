package com.example.numease.presentation.parent.stats_overview

data class SkillCategory(val id: Int, val name: String)

val GAME_CATEGORIES = listOf(
    SkillCategory(1, "Đếm số"),
    SkillCategory(3  , "Kéo thả"),
    SkillCategory(2  , "So sánh"),
    SkillCategory(4, "Phép cộng"),
    SkillCategory(5, "Phép trừ")
)