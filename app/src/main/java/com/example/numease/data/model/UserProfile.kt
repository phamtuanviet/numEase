package com.example.numease.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String, // Map trực tiếp với auth.uid() của Supabase
    val role: String, // "PARENT", "STUDENT", "ADMIN"
    @SerialName("created_at")
    val createdAt: String? = null,
    val name : String ? = null,
    val email : String ? = null,
)