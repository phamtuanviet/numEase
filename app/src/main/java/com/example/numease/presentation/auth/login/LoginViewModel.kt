package com.example.numease.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth : Auth
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    // Đăng nhập bằng Email / Mật khẩu
    fun loginWithEmail() {
        val currentState = _state.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(error = "Vui lòng nhập đầy đủ email và mật khẩu") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                auth.signInWith(Email) {
                    email = currentState.email
                    password = currentState.password
                }
                // Thành công
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                // Thất bại (sai pass, v.v.)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Đăng nhập thất bại: ${e.localizedMessage ?: "Vui lòng kiểm tra lại thông tin"}"
                    )
                }
            }
        }
    }

    // Đăng nhập bằng Google
    fun loginWithGoogle() {
        viewModelScope.launch {
            try {
                // Gọi Web Intent của Supabase để đăng nhập Google
                auth.signInWith(Google)
                // Lưu ý: signInWith(Google) sẽ bật trình duyệt web lên.
                // Việc cập nhật trạng thái success sẽ do AuthViewModel tổng (đã viết bài trước) hứng lấy thông qua DeepLink.
            } catch (e: Exception) {
                _state.update { it.copy(error = "Lỗi Google Auth: ${e.message}") }
            }
        }
    }

    // Reset state khi màn hình bị hủy hoặc chuyển trang
    fun resetState() {
        _state.value = LoginState()
    }

    fun setError(message: String) {
        _state.update {
            it.copy(
                error = message,
                isLoading = false, // Chắc chắn tắt loading
                isSuccess = false
            )
        }
    }

}