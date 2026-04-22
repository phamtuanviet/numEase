package com.example.numease.presentation.auth.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: Auth
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    fun register() {
        val currentState = _state.value

        // 1. Kiểm tra rỗng
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(error = "Vui lòng nhập đầy đủ thông tin") }
            return
        }

        // 2. Kiểm tra độ dài mật khẩu (Supabase cần ít nhất 6 ký tự)
        if (currentState.password.length < 6) {
            _state.update { it.copy(error = "Mật khẩu phải có ít nhất 6 ký tự") }
            return
        }

        // 3. Kiểm tra khớp mật khẩu
        if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(error = "Mật khẩu xác nhận không khớp") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Gọi API Đăng ký của Supabase
                auth.signUpWith(Email) {
                    email = currentState.email
                    password = currentState.password
                }

                // Thành công
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                // Lỗi (vd: Email đã tồn tại, sai định dạng email...)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Đăng ký thất bại: ${e.localizedMessage ?: "Lỗi không xác định"}"
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.value = RegisterState()
    }
}