package com.example.numease.data.repository

import com.example.numease.data.model.UserProfile
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.providers.builtin.Email
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) {
    // 1. Lấy ID từ bộ phận Auth
    fun getCurrentUserId(): String? = auth.currentUserOrNull()?.id

    // 2. Dùng ID đó sang bộ phận Database để lấy Profile và ÁNH XẠ
    suspend fun getUserProfile(): UserProfile? {
        val userId = getCurrentUserId() ?: return null

        return try {
            postgrest.from("user_profiles")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingle<UserProfile>() // <--- ĐÂY CHÍNH LÀ LÚC ÁNH XẠ XẢY RA
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loginWithEmail(email: String, pass: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = pass
        }
    }

    suspend fun logout() {
        auth.signOut()
    }

    // 2. Lấy Google Provider để gọi từ Giao diện
    // Với Android, luồng OAuth cần mở trình duyệt (Custom Tabs) nên ta truyền Provider ra UI
    fun getGoogleProvider() = Google

    suspend fun createInitialUserProfile(userId: String) {
        val newProfile = UserProfile(
            id = userId,
            role = "PARENT" // Mặc định ai đăng ký cũng là Phụ huynh
        )
        postgrest.from("user_profiles").insert(newProfile)
    }
}