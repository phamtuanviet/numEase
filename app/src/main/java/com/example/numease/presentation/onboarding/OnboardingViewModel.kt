package com.example.numease.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ChildProfile
import com.example.numease.data.model.UserProfile
import com.example.numease.presentation.auth.forgotpassword.ForgotPasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val auth: Auth,           // Cập nhật ở đây
    private val postgrest: Postgrest  // Cập nhật ở đây
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val uiState: StateFlow<OnboardingState> = _uiState.asStateFlow()

    fun saveRole(role: String) {
        viewModelScope.launch {
            _uiState.value = OnboardingState.Loading
            try {
                // Gọi trực tiếp auth
                val user = auth.currentUserOrNull() ?: throw Exception("Chưa đăng nhập")

                val userProfile = UserProfile(id = user.id, role = role,email = user.email)

                // Gọi trực tiếp postgrest
                postgrest["user_profiles"].upsert(userProfile)

                _uiState.value = OnboardingState.RoleSaved(role)
            } catch (e: Exception) {
                _uiState.value = OnboardingState.Error(e.message ?: "Lỗi khi lưu vai trò")
            }
        }
    }

    fun createChildProfile(name: String, age: Int, gender: String) {
        viewModelScope.launch {
            _uiState.value = OnboardingState.Loading
            try {
                val user = auth.currentUserOrNull() ?: throw Exception("Chưa đăng nhập")

                val childData = ChildProfile(
                    accountId = user.id,
                    name = name,
                    age = age,
                    currentLevel = 1,
                    gender = gender,

                )

                postgrest["child_profiles"].insert(childData)

                _uiState.value = OnboardingState.ProfileCreated
            } catch (e: Exception) {
                _uiState.value = OnboardingState.Error(e.message ?: "Lỗi khi tạo hồ sơ")
            }
        }
    }

    fun resetState() {
        _uiState.value = OnboardingState.Idle
    }
}