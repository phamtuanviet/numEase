package com.example.numease.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SessionAnswer(
    // id và created_at có thể để null hoặc giá trị mặc định
    // vì Supabase sẽ tự động sinh ra khi bạn insert
    val id: String? = null,

    @SerialName("session_id")
    val sessionId: String,

    @SerialName("exercise_id")
    val exerciseId: String,

    @SerialName("is_correct")
    val isCorrect: Boolean,

    @SerialName("time_taken_seconds")
    val timeTakenSeconds: Int,

    // Sử dụng JsonElement để chứa mọi loại dữ liệu JSON (String, Int, Object, Array)
    // Tương ứng với trường JSONB trên Supabase
    @SerialName("user_answer")
    val userAnswer: JsonElement? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)