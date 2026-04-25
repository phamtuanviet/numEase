package com.example.numease.utils

// Bạn có thể đặt hàm này ở một file Utils.kt hoặc để ngay trên đầu file ExerciseScreen.kt
fun getEmojiForObject(type: String): String {
    return when (type) {
        "apple" -> "🍎"
        "cat" -> "🐱"
        "dog" -> "🐶"
        "star" -> "⭐"
        "candy" -> "🍬"
        "flower" -> "🌸"
        "ball" -> "⚽"
        "car" -> "🚗"
        "banana" -> "🍌"
        "bird" -> "🐦"
        "strawberry" -> "🍓"
        "rabbit" -> "🐰"
        "ice_cream" -> "🍦"
        "orange" -> "🍊"
        "bear" -> "🧸"
        "pencil" -> "✏️"
        "book" -> "📖"
        "hat" -> "🧢"
        "leaf" -> "🍃"
        "butterfly" -> "🦋"
        else -> "✨" // Mặc định nếu không tìm thấy
    }
}