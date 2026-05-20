package com.example.numease.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.Auth
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
            _state.update { it.copy(error = "Vui lòng nhập đầy đủ email và mật khẩu.") }
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
                // Thất bại -> Gọi hàm phiên dịch lỗi
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = translateAuthError(e)
                    )
                }
            }
        }
    }

    // Đăng nhập bằng Google
    fun loginWithGoogle() {
        viewModelScope.launch {
            try {
                auth.signInWith(Google)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Không thể kết nối với Google. Vui lòng thử lại.") }
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
                isLoading = false,
                isSuccess = false
            )
        }
    }

    // MỚI: Hàm xóa lỗi sau khi Snackbar đã hiển thị xong
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // MỚI: Bộ lọc "phiên dịch" lỗi từ Supabase sang Tiếng Việt thân thiện
    private fun translateAuthError(exception: Exception): String {
        val message = exception.message?.lowercase() ?: return "Đã có lỗi xảy ra. Vui lòng thử lại sau."

        return when {
            message.contains("invalid login credentials") -> "Email hoặc mật khẩu không chính xác."
            message.contains("email not confirmed") -> "Tài khoản chưa được xác thực. Vui lòng kiểm tra email."
            message.contains("user not found") -> "Tài khoản này không tồn tại."
            message.contains("network") || message.contains("host") -> "Lỗi kết nối mạng. Vui lòng kiểm tra Internet."
            message.contains("rate limit") -> "Bạn thao tác quá nhanh, vui lòng thử lại sau ít phút."
            else -> "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin."
        }
    }
}