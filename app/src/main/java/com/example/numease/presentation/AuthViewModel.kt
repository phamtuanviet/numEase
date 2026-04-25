package com.example.numease.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ChildProfile
import com.example.numease.data.model.UserProfile
import com.example.numease.manager.ChildSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: Auth,
    private val postgrest : Postgrest,
    val childSessionManager: ChildSessionManager
) : ViewModel() {

    // Trạng thái load Profile
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Trạng thái Authentication tổng quát (để màn Splash biết đường đi tiếp)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        _authState.value = AuthState.Loading
        observeAuthStatus()
    }

    fun selectChild(child: ChildProfile) {
        childSessionManager.setActiveChild(child)
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            auth.awaitInitialization()
            auth.sessionStatus.collect { status ->
                Log.d("AuthViewModel", "observeAuthStatus: $status")
                when (status) {
                    is SessionStatus.Authenticated -> {

                        fetchUserProfile()
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _userProfile.value = null
                        _authState.value = AuthState.Unauthenticated

                        // KIỂM TRA LỖI TRẢ VỀ TỪ URL (Deep Link) TẠI ĐÂY
                        // Nếu status có chứa thông tin lỗi từ external auth
                    }
                    is SessionStatus.LoadingFromStorage -> {
                        _authState.value = AuthState.Loading
                    }

                    is SessionStatus.NetworkError -> {
                        // Giữ nguyên trạng thái Loading để UI không nhảy lung tung
                        _authState.value = AuthState.Loading
                    }
                    // Trong một số phiên bản, lỗi sẽ được đẩy qua trạng thái riêng
                    else -> { /* Xử lý các trạng thái khác */ }
                }
            }
        }
    }

    private suspend fun fetchUserProfile() {
        try {
            val user = auth.currentUserOrNull() ?: throw Exception("User null")

            val profile = postgrest["user_profiles"]
                .select { filter { eq("id", user.id) } }
                .decodeSingleOrNull<UserProfile>()

            var children: List<ChildProfile> = emptyList()

            if (profile != null && profile.role != "ADMIN") {
                children = postgrest["child_profiles"]
                    .select { filter { eq("account_id", user.id) } }
                    .decodeList<ChildProfile>()

                // 2. LOGIC LƯU KÉT SẮT
                if (children.size == 1) {
                    // Nếu chỉ có 1 bé, auto chọn luôn cho nhanh
                    childSessionManager.setActiveChild(children.first())
                } else {
                    // Nếu 0 bé hoặc > 1 bé, xóa két sắt để khởi tạo lại từ đầu
                    childSessionManager.clearSession()
                }
            }

            _userProfile.value = profile
            _authState.value = AuthState.Authenticated(profile, children) // Truyền list đi

        } catch (e: Exception) {
            e.printStackTrace()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun logout() {
        viewModelScope.launch {
            childSessionManager.clearSession()
            auth.signOut()
            // Không cần set lại state vì observeAuthStatus() sẽ tự bắt được sự kiện NotAuthenticated
        }
    }
}

// Sealed class để Splash Screen bắt trạng thái cho chuẩn
sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(
        val profile: UserProfile?,
        // Thay vì Boolean, ta lưu luôn danh sách các bé (phòng sau này 1 tk có 2-3 bé)
        val childProfiles: List<ChildProfile> = emptyList()
    ) : AuthState()    object Unauthenticated : AuthState()
}