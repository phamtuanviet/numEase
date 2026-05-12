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

                        // 🛑 SỬA Ở ĐÂY: Chỉ đưa về Unauthenticated nếu chưa bị khóa
                        if (_authState.value !is AuthState.Banned) {
                            _authState.value = AuthState.Unauthenticated
                        }
                    }
                    is SessionStatus.LoadingFromStorage -> {
                        _authState.value = AuthState.Loading
                    }
                    is SessionStatus.NetworkError -> {
                        _authState.value = AuthState.Loading
                    }
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

            // --- LOGIC CHẶN TÀI KHOẢN BỊ KHÓA NẰM Ở ĐÂY ---
            if (profile != null && profile.isBanned) {
                Log.d("AuthViewModel", "🛑 PHÁT HIỆN TÀI KHOẢN BỊ KHÓA: ${user.email}")

                auth.signOut()
                childSessionManager.clearSession()
                _userProfile.value = null

                // Đặt dòng này cuối cùng để nó là State cuối cùng được phát ra
                _authState.value = AuthState.Banned

                return
            }
            // ---------------------------------------------

            var children: List<ChildProfile> = emptyList()

            if (profile != null && profile.role != "ADMIN") {
                children = postgrest["child_profiles"]
                    .select { filter { eq("account_id", user.id) } }
                    .decodeList<ChildProfile>()


                if (children.size == 1 && childSessionManager.activeChild.value == null) {
                    childSessionManager.setActiveChild(children.first())
                }
                // ------------------------------------
            }

            _userProfile.value = profile
            _authState.value = AuthState.Authenticated(profile, children)

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
    object Banned : AuthState()
}